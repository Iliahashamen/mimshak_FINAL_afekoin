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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liebnitz)

        val gameView = findViewById<LiebnitzGameView>(R.id.liebnitzGameView)
        val tvScore = findViewById<TextView>(R.id.tvLiebnitzScore)

        gameView.onScoreChanged = { s -> tvScore.text = "Score: $s" }

        gameView.onCrash = callback@{ finalScore ->
            if (rewarded) return@callback
            rewarded = true
            val reward = min(3.0, finalScore * 0.05)
            lifecycleScope.launch {
                try {
                    if (reward > 0) {
                        FirebaseWallet.addCredits(reward, "Liebnitz run (score $finalScore)")
                        Toast.makeText(
                            this@LiebnitzActivity,
                            "Run complete! +${String.format("%.2f", reward)} AFK",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(this@LiebnitzActivity, "Keep practicing!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LiebnitzActivity, e.message ?: "Could not save reward", Toast.LENGTH_LONG).show()
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
