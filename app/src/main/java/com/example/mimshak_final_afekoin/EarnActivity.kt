package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class EarnActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earn)

        val layoutQuestion = findViewById<LinearLayout>(R.id.layoutQuestion)
        val layoutEarnButtons = findViewById<LinearLayout>(R.id.layoutEarnButtons)
        val btnAfequiz = findViewById<Button>(R.id.btnAfequiz)
        val btnLiebnitz = findViewById<Button>(R.id.btnLiebnitz)
        val btnMine = findViewById<Button>(R.id.btnMine)
        val tvLockMsg = findViewById<TextView>(R.id.tvLockMessage)

        findViewById<Button>(R.id.btnYesInClass).setOnClickListener {
            layoutQuestion.visibility = View.GONE
            layoutEarnButtons.visibility = View.VISIBLE

            // Lock Logic: Locked until hh:45 if in class
            val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
            if (currentMinute < 45) {
                btnAfequiz.isEnabled = false
                btnAfequiz.alpha = 0.5f
                btnLiebnitz.isEnabled = false
                btnLiebnitz.alpha = 0.5f
                tvLockMsg.text = "Quiz and Leibniz locked until ${Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}:45"
                tvLockMsg.visibility = View.VISIBLE
            }
        }

        findViewById<Button>(R.id.btnNoNotInClass).setOnClickListener {
            layoutQuestion.visibility = View.GONE
            layoutEarnButtons.visibility = View.VISIBLE
        }

        btnAfequiz.setOnClickListener { startActivity(Intent(this, AfequizActivity::class.java)) }
        btnMine.setOnClickListener { startActivity(Intent(this, MineActivity::class.java)) }
        btnLiebnitz.setOnClickListener { Toast.makeText(this, "Leibniz coming soon!", Toast.LENGTH_SHORT).show() }
    }
}