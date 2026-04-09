package com.example.mimshak_final_afekoin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class QuizResultActivity : AppCompatActivity() {

    companion object { private var lastSessionId: Long = 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afequiz_result)

        val score = intent.getIntExtra("SCORE", 0)
        val sessionId = intent.getLongExtra("SESSION_ID", 0)

        if (sessionId != 0L && sessionId != lastSessionId) {
            processReward(score)
            lastSessionId = sessionId
        }

        findViewById<TextView>(R.id.tvFinalScore).text = "You got $score correct!"

        findViewById<Button>(R.id.btnBackToEarn).setOnClickListener {
            startActivity(Intent(this, EarnActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
        }
        findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
            startActivity(Intent(this, AfequizActivity::class.java))
        }
        findViewById<Button>(R.id.btnGoHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
        }
    }

    private fun processReward(score: Int) {
        val prefs = getSharedPreferences("AFEKOIN_PREFS", Context.MODE_PRIVATE)
        val old = (prefs.getString("BALANCE", "30.00") ?: "30.00").toDouble()
        val new = old + score
        val balStr = String.format("%.2f", new)

        if (score > 0) {
            val history = prefs.getString("HISTORY_DATA", "") ?: ""
            val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
            val entry = "Quiz Reward|+$score.00 AFK|$date;"
            prefs.edit().putString("BALANCE", balStr).putString("HISTORY_DATA", history + entry).apply()
        }
        findViewById<TextView>(R.id.tvNewBalance).text = "New Balance: $balStr AFK"
    }
}