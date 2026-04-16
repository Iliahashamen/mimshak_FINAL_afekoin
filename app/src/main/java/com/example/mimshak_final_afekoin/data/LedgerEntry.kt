package com.example.mimshak_final_afekoin.data

import com.google.firebase.Timestamp

/** A single transaction row from the Firestore `transactions` collection. */
data class LedgerEntry(
    val id: String,
    val userId: String,
    val description: String,
    val amount: Double,
    val createdAt: Timestamp? = null,
)
