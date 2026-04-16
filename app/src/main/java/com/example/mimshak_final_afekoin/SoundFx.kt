package com.example.mimshak_final_afekoin

import android.media.ToneGenerator
import android.media.AudioManager

/**
 * Lightweight sound feedback using Android's built-in [ToneGenerator].
 * No audio files are bundled — zero impact on APK size.
 */
object SoundFx {

    private fun tone(type: Int, durationMs: Int) {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_SYSTEM, 60)
            tg.startTone(type, durationMs)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                { tg.release() },
                (durationMs + 50).toLong()
            )
        } catch (_: Exception) { /* Silently skip if audio is unavailable */ }
    }

    /** Short click for any button tap. */
    fun click() = tone(ToneGenerator.TONE_PROP_BEEP, 40)

    /** Ascending two-tone for earning coins. */
    fun coinEarned() {
        tone(ToneGenerator.TONE_CDMA_HIGH_L, 80)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
            { tone(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 120) },
            90L
        )
    }

    /** Single positive chime for a successful action (purchase, transfer). */
    fun success() = tone(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 150)

    /** Short low buzz for an error or wrong answer. */
    fun error() = tone(ToneGenerator.TONE_CDMA_LOW_L, 200)
}
