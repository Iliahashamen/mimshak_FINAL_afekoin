package com.example.mimshak_final_afekoin

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mimshak_final_afekoin.firebase.FirebaseWallet
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
        val progressBar = findViewById<ProgressBar>(R.id.timerProgress)

        progressBar.max = 10
        progressBar.progress = 10

        btnLogo.setOnClickListener {
            if (claimed) return@setOnClickListener
            taps++
            tvTaps.text = "Taps: $taps"
            animateTap(btnLogo)
        }

        timer = object : CountDownTimer(10_000L, 250L) {
            override fun onTick(millisUntilFinished: Long) {
                if (claimed) return
                val s = (millisUntilFinished + 999) / 1000
                tvTimer.text = "0:${s.toString().padStart(2, '0')}"
                progressBar.progress = s.toInt()

                // Turn timer red in last 3 seconds
                if (s <= 3) {
                    tvTimer.setTextColor(getColor(R.color.accent_red))
                    progressBar.progressTintList = android.content.res.ColorStateList.valueOf(
                        getColor(R.color.accent_red)
                    )
                }
            }

            override fun onFinish() {
                claimReward()
            }
        }.start()

        findViewById<Button>(R.id.btnAfeklikerDone).setOnClickListener {
            claimReward()
        }
    }

    private fun animateTap(view: ImageButton) {
        val scaleDown = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.88f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.88f)
            )
            duration = 60
        }
        val scaleUp = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 0.88f, 1.08f),
                ObjectAnimator.ofFloat(view, "scaleY", 0.88f, 1.08f)
            )
            duration = 80
            interpolator = DecelerateInterpolator()
        }
        val scaleNormal = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1.08f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1.08f, 1f)
            )
            duration = 60
        }
        AnimatorSet().apply {
            playSequentially(scaleDown, scaleUp, scaleNormal)
        }.start()
    }

    private fun claimReward() {
        if (claimed) return
        claimed = true
        timer?.cancel()

        val btnLogo = findViewById<ImageButton>(R.id.btnAfekoinLogo)
        btnLogo.isEnabled = false

        findViewById<TextView>(R.id.tvAfeklikerTimer).text = "0:00"
        findViewById<ProgressBar>(R.id.timerProgress).progress = 0

        if (taps == 0) {
            Toast.makeText(this, "No taps — no coins!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val reward = min(2.0, taps * 0.02)
        lifecycleScope.launch {
            try {
                FirebaseWallet.addCredits(reward, "Afekliker — $taps taps")
                Toast.makeText(
                    this@AfeklikerActivity,
                    "+${String.format("%.2f", reward)} AFK earned!",
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
