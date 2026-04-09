package com.example.mimshak_final_afekoin.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * פעולות ארנק (זיכוי, חיוב, העברה) מול Firestore.
 * העברות כוללות שני מסמכי תנועה — שיתוף מידע בין משתמשים דרך אותו אוסף.
 */
object FirebaseWallet {

    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

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

    suspend fun addCredits(amount: Double, description: String) {
        require(amount > 0)
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        val ref = db.collection(FirestorePaths.USERS).document(uid)
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            val bal = snap.getDouble("balance") ?: 0.0
            tx.update(ref, "balance", bal + amount)
        }.await()
        logTransaction(uid, description, amount)
    }

    suspend fun charge(amount: Double, description: String) {
        require(amount > 0)
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        val ref = db.collection(FirestorePaths.USERS).document(uid)
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

    suspend fun transferToUsername(recipientUsername: String, amount: Double) {
        require(amount > 0)
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        val senderRef = db.collection(FirestorePaths.USERS).document(uid)

        val key = recipientUsername.trim().lowercase()
        val raw = recipientUsername.trim()
        val recipientDoc = listOf(key, raw).distinct().firstNotNullOfOrNull { candidate ->
            val q = db.collection(FirestorePaths.USERS)
                .whereEqualTo("username", candidate)
                .limit(1)
                .get()
                .await()
            q.documents.firstOrNull()
        } ?: throw IllegalStateException("Recipient not found (check username)")

        if (recipientDoc.id == uid) {
            throw IllegalStateException("Cannot transfer to yourself")
        }

        val recipientRef = recipientDoc.reference
        db.runTransaction { tx ->
            val sSnap = tx.get(senderRef)
            val rSnap = tx.get(recipientRef)
            val sb = sSnap.getDouble("balance") ?: 0.0
            val rb = rSnap.getDouble("balance") ?: 0.0
            if (sb < amount) throw IllegalStateException("Insufficient balance")
            tx.update(senderRef, "balance", sb - amount)
            tx.update(recipientRef, "balance", rb + amount)
        }.await()

        val meta = senderRef.get().await()
        val senderUsername = meta.getString("username") ?: "user"
        val recipientName = recipientDoc.getString("username") ?: "peer"

        logTransaction(uid, "Transfer to $recipientName", -amount)
        logTransaction(recipientDoc.id, "Transfer from $senderUsername", amount)
    }
}
