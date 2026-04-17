package com.example.mimshak_final_afekoin

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

object SoundFx {

    private val handler = Handler(Looper.getMainLooper())

    private fun tone(type: Int, durationMs: Int, volume: Int = 65) {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_SYSTEM, volume)
            tg.startTone(type, durationMs)
            handler.postDelayed({ tg.release() }, (durationMs + 60).toLong())
        } catch (_: Exception) {}
    }

    private fun sequence(vararg steps: Pair<Int, Int>, startDelayMs: Long = 0L) {
        var delay = startDelayMs
        for ((type, dur) in steps) {
            val d = delay
            handler.postDelayed({ tone(type, dur) }, d)
            delay += dur + 20L
        }
    }

    fun click() = tone(ToneGenerator.TONE_PROP_BEEP, 30, 50)
    fun tapGame() = tone(ToneGenerator.TONE_DTMF_9, 18, 55)

    fun quizCorrect() = sequence(
        ToneGenerator.TONE_CDMA_MED_PBX_L to 70,
        ToneGenerator.TONE_CDMA_HIGH_PBX_L to 100
    )
    fun quizWrong() = tone(ToneGenerator.TONE_PROP_NACK, 280, 70)

    fun gameHit() = sequence(
        ToneGenerator.TONE_DTMF_A to 55,
        ToneGenerator.TONE_DTMF_D to 80
    )
    fun gameDeath() = sequence(
        ToneGenerator.TONE_CDMA_HIGH_L to 120,
        ToneGenerator.TONE_CDMA_MED_L  to 120,
        ToneGenerator.TONE_CDMA_LOW_L  to 280
    )

    fun coinEarned() = sequence(
        ToneGenerator.TONE_CDMA_MED_L      to 80,
        ToneGenerator.TONE_CDMA_HIGH_L     to 80,
        ToneGenerator.TONE_CDMA_HIGH_PBX_L to 150
    )

    fun purchase() = sequence(
        ToneGenerator.TONE_PROP_ACK         to 80,
        ToneGenerator.TONE_CDMA_HIGH_PBX_L  to 120
    )
    fun transfer() = sequence(
        ToneGenerator.TONE_PROP_BEEP  to 60,
        ToneGenerator.TONE_PROP_BEEP2 to 90
    )

    fun error() = tone(ToneGenerator.TONE_CDMA_LOW_L, 300, 70)
}
