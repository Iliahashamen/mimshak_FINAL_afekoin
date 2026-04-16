package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.mimshak_final_afekoin.data.Profile
import com.example.mimshak_final_afekoin.firebase.FirebaseWallet
import com.example.mimshak_final_afekoin.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Main screen: loads user profile from Firestore, shows balance, handles
 * profile photo upload (Firebase Storage), daily bonus, and sign-out.
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
                    goToLogin()
                    return@launch
                }

                var profile = UserRepository.getProfile(user.uid, forceServer = true)
                if (profile == null && user.email != null) {
                    val uname = user.email!!.substringBefore('@').lowercase()
                    UserRepository.createUserDocument(user.uid, uname)
                    profile = UserRepository.getProfile(user.uid, forceServer = true)
                }
                if (profile != null) {
                    currentUserProfile = profile
                    updateUI()
                    checkDailyBonus()
                } else {
                    Toast.makeText(this@MainActivity, "Could not load user profile.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun checkDailyBonus() {
        try {
            val granted = FirebaseWallet.checkAndGrantDailyBonus()
            if (granted) {
                // Reload balance after bonus
                val user = auth.currentUser ?: return
                currentUserProfile = UserRepository.getProfile(user.uid, forceServer = true) ?: currentUserProfile
                updateUI()
                Toast.makeText(
                    this,
                    "Daily bonus: +5.00 AFK",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (_: Exception) {
            // Bonus check failure is non-critical, silently ignore
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
            tvBalance.text = String.format("%.2f", currentUserProfile?.balance ?: 0.0)
            btnEye.setImageResource(R.drawable.ic_visibility)
        }
    }

    private fun setupButtons() {
        btnEye.setOnClickListener {
            SoundFx.click()
            isHidden = !isHidden
            updateBalanceDisplay()
        }

        findViewById<Button>(R.id.btnEarn).setOnClickListener {
            SoundFx.click()
            startActivity(Intent(this, EarnActivity::class.java))
            overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        }

        findViewById<ImageButton>(R.id.btnHistory).setOnClickListener {
            SoundFx.click()
            startActivity(Intent(this, HistoryActivity::class.java))
            overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        }

        findViewById<Button>(R.id.btnPay).setOnClickListener {
            SoundFx.click()
            startActivity(Intent(this, PayActivity::class.java))
            overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        }

        findViewById<Button>(R.id.btnTransfer).setOnClickListener {
            SoundFx.click()
            startActivity(Intent(this, TransferActivity::class.java))
            overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        }

        findViewById<ImageButton>(R.id.btnSignOut).setOnClickListener {
            SoundFx.click()
            confirmSignOut()
        }
    }

    private fun confirmSignOut() {
        AlertDialog.Builder(this)
            .setTitle("Sign out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign out") { _, _ ->
                auth.signOut()
                goToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
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
