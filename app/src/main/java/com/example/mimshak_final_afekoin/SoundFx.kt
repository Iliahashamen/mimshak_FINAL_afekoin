package com.example.mimshak_final_afekoin

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

/**
 * Distinct sound feedback for every UI/game event using Android's built-in [ToneGenerator].
 * No audio files bundled — zero APK size impact.
 *
 * Events and their sounds:
 *  click()        – short UI navigation tap
 *  tapGame()      – Afekliker rapid-tap (snappier, different pitch)
 *  quizCorrect()  – right answer in quiz (bright two-note chime)
 *  quizWrong()    – wrong answer in quiz (low buzzer)
 *  gameHit()      – correct pod hit in Liebnitz (arcade two-note)
 *  gameDeath()    – Liebnitz crash / game over (descending tones)
 *  coinEarned()   – reward payout at end of any game (three-note fanfare)
 *  purchase()     – store purchase confirmed (cash-register feel)
 *  transfer()     – P2P transfer sent (double-beep swoosh)
 *  error()        – any failure / insufficient balance
 */
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

    // ── UI navigation ─────────────────────────────────────────────────────────

    /** Soft tap for any button that navigates between screens. */
    fun click() = tone(ToneGenerator.TONE_PROP_BEEP, 30, 50)

    // ── Afekliker ─────────────────────────────────────────────────────────────

    /** Snappy arcade tick for each logo tap — different pitch from navigation click. */
    fun tapGame() = tone(ToneGenerator.TONE_DTMF_9, 18, 55)

    // ── Quiz ──────────────────────────────────────────────────────────────────

    /** Two-note ascending chime — distinct "ding-ding" for a correct quiz answer. */
    fun quizCorrect() = sequence(
        ToneGenerator.TONE_CDMA_MED_PBX_L to 70,
        ToneGenerator.TONE_CDMA_HIGH_PBX_L to 100
    )

    /** Low flat buzzer — unmistakably wrong. */
    fun quizWrong() = tone(ToneGenerator.TONE_PROP_NACK, 280, 70)

    // ── Liebnitz game ─────────────────────────────────────────────────────────

    /** Quick two-note arcade coin sound when the correct pod is hit. */
    fun gameHit() = sequence(
        ToneGenerator.TONE_DTMF_A to 55,
        ToneGenerator.TONE_DTMF_D to 80
    )

    /** Descending three-tone game-over jingle. */
    fun gameDeath() = sequence(
        ToneGenerator.TONE_CDMA_HIGH_L  to 120,
        ToneGenerator.TONE_CDMA_MED_L   to 120,
        ToneGenerator.TONE_CDMA_LOW_L   to 280
    )

    // ── Rewards ───────────────────────────────────────────────────────────────

    /** Three-note rising fanfare — end-of-game coin payout. */
    fun coinEarned() = sequence(
        ToneGenerator.TONE_CDMA_MED_L     to 80,
        ToneGenerator.TONE_CDMA_HIGH_L    to 80,
        ToneGenerator.TONE_CDMA_HIGH_PBX_L to 150
    )

    // ── Store / Transfer ──────────────────────────────────────────────────────

    /** Cash-register confirmation — store purchase. */
    fun purchase() = sequence(
        ToneGenerator.TONE_PROP_ACK to 80,
        ToneGenerator.TONE_CDMA_HIGH_PBX_L to 120
    )

    /** Quick double-beep swoosh — coins sent. */
    fun transfer() = sequence(
        ToneGenerator.TONE_PROP_BEEP  to 60,
        ToneGenerator.TONE_PROP_BEEP2 to 90
    )

    // ── Errors ────────────────────────────────────────────────────────────────

    /** Single low buzz for any failure. */
    fun error() = tone(ToneGenerator.TONE_CDMA_LOW_L, 300, 70)
}
