package com.example.mimshak_final_afekoin

import io.supabase.gotrue.auth
import io.supabase.postgrest.postgrest

/**
 * Balance updates line up with the `add_balance` RPC used by [AfequizActivity].
 * If the RPC is missing in your Supabase project, the catch block falls back to a direct profile update.
 */
object SupabaseWallet {

    suspend fun addCredits(amount: Double, description: String) {
        val uid = SupabaseManager.client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Not signed in")
        try {
            SupabaseManager.client.postgrest.rpc(
                function = "add_balance",
                parameters = mapOf("user_id" to uid, "amount_to_add" to amount)
            )
        } catch (_: Exception) {
            val profile = SupabaseManager.client.postgrest
                .from("profiles")
                .select { filter("id", "eq", uid) }
                .decodeSingle<Profile>()
            SupabaseManager.client.postgrest.from("profiles").update(
                Profile(id = profile.id, username = profile.username, balance = profile.balance + amount)
            ) { filter("id", "eq", uid) }
        }
        SupabaseManager.client.postgrest.from("transactions").insert(
            Transaction(user_id = uid, description = description, amount = amount)
        )
    }

    suspend fun charge(amount: Double, description: String) {
        require(amount > 0) { "Amount must be positive" }
        val uid = SupabaseManager.client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Not signed in")
        val profile = SupabaseManager.client.postgrest
            .from("profiles")
            .select { filter("id", "eq", uid) }
            .decodeSingle<Profile>()
        if (profile.balance < amount) throw IllegalStateException("Insufficient balance")
        SupabaseManager.client.postgrest.from("profiles").update(
            Profile(id = profile.id, username = profile.username, balance = profile.balance - amount)
        ) { filter("id", "eq", uid) }
        SupabaseManager.client.postgrest.from("transactions").insert(
            Transaction(user_id = uid, description = description, amount = -amount)
        )
    }

    suspend fun transferToUsername(recipientUsername: String, amount: Double) {
        require(amount > 0) { "Amount must be positive" }
        val uid = SupabaseManager.client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Not signed in")
        val raw = recipientUsername.trim()
        val sender = SupabaseManager.client.postgrest
            .from("profiles")
            .select { filter("id", "eq", uid) }
            .decodeSingle<Profile>()
        if (sender.balance < amount) throw IllegalStateException("Insufficient balance")
        val recipient = listOf(raw.lowercase(), raw).distinct().mapNotNull { candidate ->
            SupabaseManager.client.postgrest
                .from("profiles")
                .select { filter("username", "eq", candidate) }
                .decodeSingleOrNull<Profile>()
        }.firstOrNull() ?: throw IllegalStateException("Recipient not found (check username)")
        if (recipient.id == sender.id) throw IllegalStateException("Cannot transfer to yourself")

        SupabaseManager.client.postgrest.from("profiles").update(
            Profile(id = sender.id, username = sender.username, balance = sender.balance - amount)
        ) { filter("id", "eq", sender.id) }
        SupabaseManager.client.postgrest.from("profiles").update(
            Profile(id = recipient.id, username = recipient.username, balance = recipient.balance + amount)
        ) { filter("id", "eq", recipient.id) }

        SupabaseManager.client.postgrest.from("transactions").insert(
            Transaction(user_id = sender.id, description = "Transfer to ${recipient.username}", amount = -amount)
        )
        SupabaseManager.client.postgrest.from("transactions").insert(
            Transaction(user_id = recipient.id, description = "Transfer from ${sender.username}", amount = amount)
        )
    }
}
