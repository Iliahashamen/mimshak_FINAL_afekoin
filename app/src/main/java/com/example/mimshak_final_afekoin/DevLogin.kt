package com.example.mimshak_final_afekoin

import android.content.Context

/**
 * מצב פיתוח זמני: כל אימייל/סיסמה לא ריקים עוברים למסך ראשי בלי Firebase.
 * לפני הגשה למרצה — שנה ל-[false].
 */
object DevLogin {

    const val ENABLED = false

    /** שם שיוצג במסך הראשי אחרי “התחברות” במצב dev */
    const val DISPLAY_USERNAME = "userevich"

    private const val PREF = "afekoin_dev_prefs"
    private const val KEY_DEV_SESSION = "dev_session_active"

    fun isDevSession(context: Context): Boolean {
        if (!ENABLED) return false
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_DEV_SESSION, false)
    }

    fun startDevSession(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_DEV_SESSION, true)
            .apply()
    }

    /** לבדיקות logout ידני; אפשר לקרוא ממסך הגדרות בעתיד */
    fun clearDevSession(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_DEV_SESSION, false)
            .apply()
    }
}
