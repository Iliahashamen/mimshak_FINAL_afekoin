package com.example.mimshak_final_afekoin.data

/**
 * נתוני משתמש ב-Firestore (מסמך תחת users/{uid}).
 * שיתוף מידע: אותו מסמך נקרא ללוח תוצאות; העדכונים נראים לכל המשתמשים המחוברים לפי חוקי האבטחה.
 */
data class Profile(
    val id: String,
    val username: String,
    val balance: Double,
    val photoUrl: String? = null,
)
