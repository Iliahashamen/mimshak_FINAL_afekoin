package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.supabase.gotrue.auth
import io.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private var isHidden = false
    private var currentUserProfile: Profile? = null
    private lateinit var tvGreeting: TextView
    private lateinit var tvBalance: TextView
    private lateinit var btnEye: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        tvGreeting = findViewById(R.id.tvGreeting)
        tvBalance = findViewById(R.id.tvBalance)
        btnEye = findViewById(R.id.btnHideBalance)

        // Setup navigation and placeholder buttons
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        fetchUserProfile() // Fetch/refresh user data every time the activity is resumed
    }

    private fun fetchUserProfile() {
        lifecycleScope.launch {
            try {
                val user = SupabaseManager.client.auth.currentUserOrNull()
                if (user == null) {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    return@launch
                }

                val profile = SupabaseManager.client.postgrest
                    .from("profiles")
                    .select { filter("id", "eq", user.id) }
                    .decodeSingleOrNull<Profile>()

                if (profile != null) {
                    currentUserProfile = profile
                    updateUI()
                } else {
                    Toast.makeText(this@MainActivity, "Could not load user profile.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI() {
        currentUserProfile?.let {
            tvGreeting.text = "${getGreetingPrefix()}, ${it.username.uppercase()}!"
            updateBalanceDisplay()
        }
    }

    private fun updateBalanceDisplay() {
        if (isHidden) {
            tvBalance.text = "••••"
            btnEye.setImageResource(R.drawable.ic_visibility_off)
        } else {
            val formattedBalance = String.format("%.2f", currentUserProfile?.balance ?: 0.0)
            tvBalance.text = formattedBalance
            btnEye.setImageResource(R.drawable.ic_visibility)
        }
    }

    private fun setupButtons() {
        btnEye.setOnClickListener {
            isHidden = !isHidden
            updateBalanceDisplay()
        }

        findViewById<Button>(R.id.btnEarn).setOnClickListener {
            startActivity(Intent(this, EarnActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<Button>(R.id.btnLeaderboard).setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        findViewById<Button>(R.id.btnPay).setOnClickListener {
            Toast.makeText(this, "Pay coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.btnTransfer).setOnClickListener {
            Toast.makeText(this, "Transfer coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getGreetingPrefix(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "GOOD MORNING"
            in 12..16 -> "GOOD AFTERNOON"
            in 17..20 -> "GOOD EVENING"
            else -> "GOOD NIGHT"
        }
    }
}