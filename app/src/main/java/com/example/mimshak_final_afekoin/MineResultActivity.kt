package com.example.mimshak_final_afekoin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MineResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mine_result)

        val reward = intent.getDoubleExtra("REWARD", 0.0)
        val rewardStr = String.format("%.2f", reward)

        val prefs = getSharedPreferences("AFEKOIN_PREFS", Context.MODE_PRIVATE)
        val oldBalStr = prefs.getString("BALANCE", "30.00") ?: "30.00"
        val newBal = oldBalStr.toDouble() + reward
        val finalBalStr = String.format("%.2f", newBal)

        // Save to History
        val history = prefs.getString("HISTORY_DATA", "") ?: ""
        val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())
        val entry = "Mining Reward|+$rewardStr AFK|$date;"

        prefs.edit()
            .putString("BALANCE", finalBalStr)
            .putString("HISTORY_DATA", history + entry)
            .apply()

        findViewById<TextView>(R.id.tvMinedAmount).text = "+$rewardStr AFK"
        findViewById<TextView>(R.id.tvNewBalance).text = "Balance: $finalBalStr AFK"

        findViewById<Button>(R.id.btnMineAgain).setOnClickListener {
            startActivity(Intent(this, MineActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnGoHome).setOnClickListener {
            // No intent extra needed here because MainActivity now loads the name from storage
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}