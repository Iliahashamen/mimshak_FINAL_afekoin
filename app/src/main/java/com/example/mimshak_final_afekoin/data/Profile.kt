package com.example.mimshak_final_afekoin.data

/**
 * Represents a user profile stored in Firestore under `users/{uid}`.
 *
 * The same document is read by all screens that need the balance or username,
 * so any balance change is visible across the app in real time.
 */
data class Profile(
    val id: String,
    val username: String,
    val balance: Double,
    val photoUrl: String? = null,
)
