package com.example.mimshak_final_afekoin.firebase

import android.net.Uri
import com.example.mimshak_final_afekoin.data.LedgerEntry
import com.example.mimshak_final_afekoin.data.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

// Fetches/stores user data from Firestore and Firebase Storage
object UserRepository {

    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()

    private fun docToProfile(doc: com.google.firebase.firestore.DocumentSnapshot): Profile? {
        if (!doc.exists()) return null
        return Profile(
            id = doc.id,
            username = doc.getString("username") ?: "",
            balance = doc.getDouble("balance") ?: 0.0,
            photoUrl = doc.getString("photoUrl"),
        )
    }

    // forceServer=true skips the local cache so balance is always up to date
    suspend fun getProfile(uid: String, forceServer: Boolean = false): Profile? {
        val source = if (forceServer) Source.SERVER else Source.DEFAULT
        val snap = db.collection(FirestorePaths.USERS).document(uid).get(source).await()
        return docToProfile(snap)
    }

    // upload photo and save URL to user doc
    suspend fun uploadProfilePhoto(localUri: Uri): String {
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        val ref = storage.reference.child("${FirestorePaths.PROFILE_IMAGES}/$uid.jpg")
        ref.putFile(localUri).await()
        val download = ref.downloadUrl.await().toString()
        db.collection(FirestorePaths.USERS).document(uid).update("photoUrl", download).await()
        return download
    }

    suspend fun transactionsForUser(uid: String): List<LedgerEntry> {
        val snap = db.collection(FirestorePaths.TRANSACTIONS)
            .whereEqualTo("userId", uid)
            .get()
            .await()
        return snap.documents
            .map { doc ->
                LedgerEntry(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    description = doc.getString("description") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    createdAt = doc.getTimestamp("createdAt"),
                )
            }
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
    }

    // create the user doc in Firestore on first registration
    suspend fun createUserDocument(uid: String, username: String, startingBalance: Double = 30.0) {
        db.collection(FirestorePaths.USERS).document(uid).set(
            hashMapOf(
                "username" to username,
                "balance" to startingBalance,
                "photoUrl" to null,
            )
        ).await()
    }
}
