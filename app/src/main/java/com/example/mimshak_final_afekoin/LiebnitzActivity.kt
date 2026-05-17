package com.example.mimshak_final_afekoin

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mimshak_final_afekoin.firebase.FirebaseWallet
import kotlinx.coroutines.launch
import kotlin.math.min

class LiebnitzActivity : AppCompatActivity() {

    private var rewarded = false

    // screen init
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liebnitz)

        val gameView  = findViewById<LiebnitzGameView>(R.id.liebnitzGameView)
        val tvScore   = findViewById<TextView>(R.id.tvLiebnitzScore)
        val tvLives   = findViewById<TextView>(R.id.tvLiebnitzLives)
        val tvEquation = findViewById<TextView>(R.id.tvEquation)

        // hud update
        gameView.onScoreChanged = { score, equation, lives ->
            runOnUiThread {
                tvScore.text = "SCORE: $score"
                tvLives.text = "LIVES: $lives"
                if (equation.isNotBlank()) tvEquation.text = equation
                if (score > 0) SoundFx.gameHit()
            }
        }

        // crash handle
        gameView.onCrash = callback@{ finalScore ->
            if (rewarded) return@callback
            rewarded = true

            // reward calc
            val reward = min(5.0, finalScore * 0.1)

            runOnUiThread {
                SoundFx.gameDeath()
                tvEquation.text = "CRASH!"
                tvEquation.setTextColor(getColor(R.color.accent_red))
            }

            // reward save
            lifecycleScope.launch {
                try {
                    if (reward > 0) {
                        FirebaseWallet.addCredits(reward, "Liebnitz — $finalScore correct answers")
                        SoundFx.coinEarned()
                        RewardCelebration.show(this@LiebnitzActivity, reward) { finish() }
                    } else {
                        RewardCelebration.show(this@LiebnitzActivity, 0.0) { finish() }
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@LiebnitzActivity,
                        e.message ?: "Could not save reward",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }

        findViewById<Button>(R.id.btnLiebnitzExit).setOnClickListener {
            rewarded = true
            finish()
        }
    }
}
