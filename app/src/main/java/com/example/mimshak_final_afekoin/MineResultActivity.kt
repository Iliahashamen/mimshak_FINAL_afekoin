package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mimshak_final_afekoin.firebase.FirebaseWallet
import com.example.mimshak_final_afekoin.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MineResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mine_result)

        val reward = intent.getDoubleExtra("REWARD", 0.0)
        val rewardStr = String.format("%.2f", reward)

        findViewById<TextView>(R.id.tvMinedAmount).text = "+$rewardStr AFK"
        findViewById<TextView>(R.id.tvNewBalance).text = getString(R.string.syncing_balance)

        lifecycleScope.launch {
            try {
                FirebaseWallet.addCredits(reward, "Mining session reward")
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val bal = if (uid != null) UserRepository.getProfile(uid)?.balance else null
                findViewById<TextView>(R.id.tvNewBalance).text =
                    if (bal != null) "Balance: ${String.format("%.2f", bal)} AFK"
                    else getString(R.string.balance_updated_check_home)
            } catch (e: Exception) {
                Toast.makeText(this@MineResultActivity, e.message ?: "Save failed", Toast.LENGTH_LONG).show()
                findViewById<TextView>(R.id.tvNewBalance).text = getString(R.string.balance_updated_check_home)
            }
        }

        findViewById<Button>(R.id.btnMineAgain).setOnClickListener {
            startActivity(Intent(this, MineActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnGoHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }
    }
}
