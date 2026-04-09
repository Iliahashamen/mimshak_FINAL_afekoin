package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mimshak_final_afekoin.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuizResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afequiz_result)

        val score = intent.getIntExtra("SCORE", 0)
        findViewById<TextView>(R.id.tvFinalScore).text = "You got $score / 15 correct!"
        findViewById<TextView>(R.id.tvNewBalance).text = getString(R.string.syncing_balance)

        lifecycleScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val balText = if (uid != null) {
                    val p = UserRepository.getProfile(uid)
                    if (p != null) "Balance: ${String.format("%.2f", p.balance)} AFK"
                    else "Balance unavailable"
                } else {
                    "Sign in to see your balance"
                }
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.tvNewBalance).text = balText
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.tvNewBalance).text =
                        getString(R.string.balance_updated_check_home)
                }
            }
        }

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
}
