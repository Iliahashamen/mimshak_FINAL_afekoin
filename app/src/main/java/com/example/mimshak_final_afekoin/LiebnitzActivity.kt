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

/**
 * Hosts the [LiebnitzGameView] spaceship math game.
 * Awards up to 5.00 AFK based on the number of correct answers before crashing.
 */
class LiebnitzActivity : AppCompatActivity() {

    private var rewarded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liebnitz)

        val gameView  = findViewById<LiebnitzGameView>(R.id.liebnitzGameView)
        val tvScore   = findViewById<TextView>(R.id.tvLiebnitzScore)
        val tvEquation = findViewById<TextView>(R.id.tvEquation)

        gameView.onScoreChanged = { score, equation ->
            runOnUiThread {
                tvScore.text = "SCORE: $score"
                if (equation.isNotBlank()) tvEquation.text = equation
            }
        }

        gameView.onCrash = callback@{ finalScore ->
            if (rewarded) return@callback
            rewarded = true

            val reward = min(5.0, finalScore * 0.1)

            runOnUiThread {
                tvEquation.text = "CRASH!"
                tvEquation.setTextColor(getColor(R.color.accent_red))
            }

            lifecycleScope.launch {
                try {
                    if (reward > 0) {
                        FirebaseWallet.addCredits(reward, "Liebnitz — $finalScore correct answers")
                        SoundFx.coinEarned()
                        Toast.makeText(
                            this@LiebnitzActivity,
                            "Run over! +${String.format("%.2f", reward)} AFK (score $finalScore)",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@LiebnitzActivity,
                            "No coins — answer at least one correctly next time!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@LiebnitzActivity,
                        e.message ?: "Could not save reward",
                        Toast.LENGTH_LONG
                    ).show()
                }
                finish()
            }
        }

        findViewById<Button>(R.id.btnLiebnitzExit).setOnClickListener {
            rewarded = true
            finish()
        }
    }
}
