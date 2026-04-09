package com.example.mimshak_final_afekoin.data

import com.google.firebase.Timestamp

/** רשומת תנועה באוסף transactions ב-Firestore */
data class LedgerEntry(
    val id: String,
    val userId: String,
    val description: String,
    val amount: Double,
    val createdAt: Timestamp? = null,
)
