package com.example.mimshak_final_afekoin

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.math.min

class AfeklikerActivity : AppCompatActivity() {

    private var taps = 0
    private var timer: CountDownTimer? = null
    private var claimed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afekliker)

        val tvTimer = findViewById<TextView>(R.id.tvAfeklikerTimer)
        val tvTaps = findViewById<TextView>(R.id.tvAfeklikerTaps)
        val btnLogo = findViewById<ImageButton>(R.id.btnAfekoinLogo)

        btnLogo.setOnClickListener {
            if (claimed) return@setOnClickListener
            taps++
            tvTaps.text = "Taps: $taps"
        }

        timer = object : CountDownTimer(20_000L, 250L) {
            override fun onTick(millisUntilFinished: Long) {
                if (claimed) return
                val s = (millisUntilFinished + 999) / 1000
                tvTimer.text = "0:${s.toString().padStart(2, '0')}"
            }

            override fun onFinish() {
                claimReward()
            }
        }.start()

        findViewById<Button>(R.id.btnAfeklikerDone).setOnClickListener {
            claimReward()
        }
    }

    private fun claimReward() {
        if (claimed) return
        claimed = true
        timer?.cancel()

        val btnLogo = findViewById<ImageButton>(R.id.btnAfekoinLogo)
        btnLogo.isEnabled = false

        findViewById<TextView>(R.id.tvAfeklikerTimer).text = "0:00"

        if (taps == 0) {
            Toast.makeText(this, "No taps — no coins.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val reward = min(2.0, taps * 0.02)
        lifecycleScope.launch {
            try {
                SupabaseWallet.addCredits(reward, "Afekliker session")
                Toast.makeText(
                    this@AfeklikerActivity,
                    "Earned +${String.format("%.2f", reward)} AFK",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(this@AfeklikerActivity, e.message ?: "Save failed", Toast.LENGTH_LONG).show()
            }
            finish()
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
