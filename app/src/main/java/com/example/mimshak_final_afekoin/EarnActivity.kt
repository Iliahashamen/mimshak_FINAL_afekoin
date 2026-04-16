package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

// Pick a game to earn coins. Games lock until :45 if user is in class.
class EarnActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earn)

        val layoutQuestion = findViewById<LinearLayout>(R.id.layoutQuestion)
        val layoutEarnButtons = findViewById<LinearLayout>(R.id.layoutEarnButtons)
        val btnAfequiz = findViewById<Button>(R.id.btnAfequiz)
        val btnAfekliker = findViewById<Button>(R.id.btnAfekliker)
        val btnLiebnitz = findViewById<Button>(R.id.btnLiebnitz)
        val tvLockMsg = findViewById<TextView>(R.id.tvLockMessage)

        fun applyClassLock() {
            val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (currentMinute >= 45) return
            listOf(btnAfequiz, btnAfekliker, btnLiebnitz).forEach { b ->
                b.isEnabled = false
                b.alpha = 0.4f
            }
            tvLockMsg.text = "Games unlocked at $hour:45 — focus on class!"
            tvLockMsg.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.btnYesInClass).setOnClickListener {
            SoundFx.click()
            layoutQuestion.visibility = View.GONE
            layoutEarnButtons.visibility = View.VISIBLE
            applyClassLock()
        }

        findViewById<Button>(R.id.btnNoNotInClass).setOnClickListener {
            SoundFx.click()
            layoutQuestion.visibility = View.GONE
            layoutEarnButtons.visibility = View.VISIBLE
        }

        btnAfequiz.setOnClickListener {
            SoundFx.click()
            startActivity(Intent(this, AfequizActivity::class.java))
            overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        }
        btnAfekliker.setOnClickListener {
            SoundFx.click()
            startActivity(Intent(this, AfeklikerActivity::class.java))
            overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        }
        btnLiebnitz.setOnClickListener {
            SoundFx.click()
            startActivity(Intent(this, LiebnitzActivity::class.java))
            overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        }
    }
}
