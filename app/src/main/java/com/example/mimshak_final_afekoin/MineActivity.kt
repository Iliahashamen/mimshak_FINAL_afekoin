package com.example.mimshak_final_afekoin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.util.*
import kotlin.random.Random

class MineActivity : AppCompatActivity() {

    private var timer: CountDownTimer? = null
    private var isBoosted = false
    private var boostsLeft = 3
    private val sessionMillis: Long = 600000 // 10 Minutes
    private lateinit var facts: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mine)

        val btnStart = findViewById<Button>(R.id.btnStartMine)
        val btnCancel = findViewById<Button>(R.id.btnCancelMine)
        val btnBoost = findViewById<Button>(R.id.btnBoost)
        val tvFact = findViewById<TextView>(R.id.tvMineStatus)

        facts = loadFacts()
        tvFact.text = "Prepare to Mine..."

        btnStart.setOnClickListener {
            btnStart.visibility = View.GONE
            btnBoost.visibility = View.VISIBLE
            btnBoost.text = "BOOST (3x) [$boostsLeft LEFT]"

            // Start with the first fact immediately
            tvFact.text = facts.random()
            startMiningSession()
        }

        btnBoost.setOnClickListener {
            if (boostsLeft > 0) {
                isBoosted = true
                boostsLeft--
                btnBoost.isEnabled = false
                btnBoost.text = "BOOST ACTIVE"
                // The fact at the top (tvFact) remains visible
            }
        }

        btnCancel.setOnClickListener {
            timer?.cancel()
            finish()
        }
    }

    private fun startMiningSession() {
        val progress = findViewById<ProgressBar>(R.id.mineProgressBar)
        val tvTimer = findViewById<TextView>(R.id.tvMineTimer)
        val tvFact = findViewById<TextView>(R.id.tvMineStatus)

        timer = object : CountDownTimer(sessionMillis, 1000) {
            var virtualElapsed: Long = 0

            override fun onTick(m: Long) {
                // Boost speeds up the virtual clock by 3x
                virtualElapsed += if (isBoosted) 3000 else 1000

                if (virtualElapsed >= sessionMillis) {
                    onFinish()
                    cancel()
                    return
                }

                // Rotate facts every 20 virtual seconds
                if ((virtualElapsed / 1000) % 20 == 0L) {
                    tvFact.text = facts.random()
                }

                val remaining = sessionMillis - virtualElapsed
                progress.progress = (virtualElapsed / 1000).toInt()
                tvTimer.text = String.format("%02d:%02d", (remaining / 1000) / 60, (remaining / 1000) % 60)
            }

            override fun onFinish() {
                rewardUser()
            }
        }.start()
    }

    private fun rewardUser() {
        // Random reward between 0.1 and 1.1
        val randomReward = Random.nextDouble(0.1, 1.11)
        val roundedReward = Math.round(randomReward * 100.0) / 100.0

        // Launch the Result Screen
        val intent = Intent(this, MineResultActivity::class.java)
        intent.putExtra("REWARD", roundedReward)
        startActivity(intent)
        finish()
    }

    private fun loadFacts(): List<String> {
        return try {
            val json = assets.open("fun_facts.json").bufferedReader().use { it.readText() }
            val array = JSONArray(json)
            List(array.length()) { i -> array.getString(i) }
        } catch (e: Exception) {
            listOf("Securing the Afekoin network...")
        }
    }
}