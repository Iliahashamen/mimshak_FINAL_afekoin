package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.mimshak_final_afekoin.data.Profile
import com.example.mimshak_final_afekoin.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * מסך ראשי: מציג פרופיל מ-Firestore, איזון, ואפשרות להעלאת תמונת פרופיל ל-Storage.
 */
class MainActivity : AppCompatActivity() {

    private val auth get() = FirebaseAuth.getInstance()

    private var isHidden = false
    private var currentUserProfile: Profile? = null
    private lateinit var tvGreeting: TextView
    private lateinit var tvBalance: TextView
    private lateinit var btnEye: ImageButton
    private lateinit var ivProfileAvatar: ImageView

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            try {
                UserRepository.uploadProfilePhoto(uri)
                Toast.makeText(this@MainActivity, "Profile photo updated", Toast.LENGTH_SHORT).show()
                fetchUserProfile()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, e.message ?: "Upload failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvGreeting = findViewById(R.id.tvGreeting)
        tvBalance = findViewById(R.id.tvBalance)
        btnEye = findViewById(R.id.btnHideBalance)
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar)

        ivProfileAvatar.setOnClickListener {
            pickImage.launch("image/*")
        }

        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        lifecycleScope.launch {
            try {
                val user = auth.currentUser
                if (user == null) {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    return@launch
                }

                var profile = UserRepository.getProfile(user.uid)
                if (profile == null && user.email != null) {
                    // שחזור: משתמש ב-Auth בלי מסמך Firestore (למשל אחרי מעבר ממערכת אחרת)
                    val uname = user.email!!.substringBefore('@').lowercase()
                    UserRepository.createUserDocument(user.uid, uname)
                    profile = UserRepository.getProfile(user.uid)
                }
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
        currentUserProfile?.let { p ->
            tvGreeting.text = "${getGreetingPrefix()}, ${p.username.uppercase()}!"
            updateBalanceDisplay()
            ivProfileAvatar.load(p.photoUrl) {
                placeholder(R.mipmap.afekoin_logo)
                error(R.mipmap.afekoin_logo)
                crossfade(true)
            }
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
            startActivity(Intent(this, PayActivity::class.java))
        }

        findViewById<Button>(R.id.btnTransfer).setOnClickListener {
            startActivity(Intent(this, TransferActivity::class.java))
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
