package com.example.mimshak_final_afekoin.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseWallet {

    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    // ledger write
    private suspend fun logTransaction(userId: String, description: String, amount: Double) {
        db.collection(FirestorePaths.TRANSACTIONS).add(
            hashMapOf(
                "userId" to userId,
                "description" to description,
                "amount" to amount,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    // credits add
    suspend fun addCredits(amount: Double, description: String) {
        require(amount > 0)
        // auth guard
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Please sign in to save your earnings.")
        val ref = db.collection(FirestorePaths.USERS).document(uid)
        // atomic update
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            val bal = snap.getDouble("balance") ?: 0.0
            tx.update(ref, "balance", bal + amount)
        }.await()
        logTransaction(uid, description, amount)
    }

    // balance charge
    suspend fun charge(amount: Double, description: String) {
        require(amount > 0)
        // auth guard
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Please sign in to make payments.")
        val ref = db.collection(FirestorePaths.USERS).document(uid)
        // atomic update
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            val bal = snap.getDouble("balance") ?: 0.0
            if (bal < amount) {
                throw IllegalStateException("Insufficient balance")
            }
            tx.update(ref, "balance", bal - amount)
        }.await()
        logTransaction(uid, description, -amount)
    }

    // +5 AFK once per day; returns true if actually granted
    // daily bonus
    suspend fun checkAndGrantDailyBonus(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val ref = db.collection(FirestorePaths.USERS).document(uid)

        // date check
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())

        var granted = false
        // bonus txn
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            val lastBonus = snap.getString("lastBonusDate") ?: ""
            if (lastBonus != today) {
                val bal = snap.getDouble("balance") ?: 0.0
                tx.update(ref, "balance", bal + 5.0)
                tx.update(ref, "lastBonusDate", today)
                granted = true
            }
        }.await()

        if (granted) {
            logTransaction(uid, "Daily login bonus", 5.0)
        }
        return granted
    }

    // username transfer
    suspend fun transferToUsername(recipientUsername: String, amount: Double) {
        require(amount > 0)
        // auth guard
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Please sign in to transfer coins.")
        val senderRef = db.collection(FirestorePaths.USERS).document(uid)

        val key = recipientUsername.trim().lowercase()
        val raw = recipientUsername.trim()
        // user lookup
        val recipientDoc = listOf(key, raw).distinct().firstNotNullOfOrNull { candidate ->
            val q = db.collection(FirestorePaths.USERS)
                .whereEqualTo("username", candidate)
                .limit(1)
                .get()
                .await()
            q.documents.firstOrNull()
        } ?: throw IllegalStateException("Recipient not found (check username)")

        // self block
        if (recipientDoc.id == uid) {
            throw IllegalStateException("Cannot transfer to yourself")
        }

        val recipientRef = recipientDoc.reference
        // atomic transfer
        db.runTransaction { tx ->
            val sSnap = tx.get(senderRef)
            val rSnap = tx.get(recipientRef)
            val sb = sSnap.getDouble("balance") ?: 0.0
            val rb = rSnap.getDouble("balance") ?: 0.0
            if (sb < amount) throw IllegalStateException("Insufficient balance")
            tx.update(senderRef, "balance", sb - amount)
            tx.update(recipientRef, "balance", rb + amount)
        }.await()

        // names fetch
        val meta = senderRef.get().await()
        val senderUsername = meta.getString("username") ?: "user"
        val recipientName = recipientDoc.getString("username") ?: "peer"

        // dual log
        logTransaction(uid, "Transfer to $recipientName", -amount)
        logTransaction(recipientDoc.id, "Transfer from $senderUsername", amount)
    }
}
