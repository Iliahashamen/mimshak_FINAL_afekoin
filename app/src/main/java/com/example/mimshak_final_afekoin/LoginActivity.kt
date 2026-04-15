package com.example.mimshak_final_afekoin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mimshak_final_afekoin.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Login / Sign-up screen.
 * Firebase Authentication (email + password) — server component #1.
 * Features: persistent "Remember me" (saves email to SharedPreferences),
 *           "Forgot password" (sends a Firebase reset email).
 */
class LoginActivity : AppCompatActivity() {

    private val auth get() = FirebaseAuth.getInstance()
    private val prefs by lazy { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var checkRememberMe: CheckBox
    private lateinit var signUpButton: Button
    private lateinit var loginButton: Button
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText    = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        checkRememberMe  = findViewById(R.id.checkRememberMe)
        signUpButton     = findViewById(R.id.signUpButton)
        loginButton      = findViewById(R.id.loginButton)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        // Auto-navigate if already logged in
        if (auth.currentUser != null || DevLogin.isDevSession(this)) {
            navigateToMain()
            return
        }

        // Pre-fill email if "Remember me" was checked last time
        val savedEmail = prefs.getString(KEY_EMAIL, null)
        if (!savedEmail.isNullOrBlank()) {
            emailEditText.setText(savedEmail)
            checkRememberMe.isChecked = true
        }

        loginButton.setOnClickListener { handleLogin() }
        signUpButton.setOnClickListener { handleSignUp() }
        tvForgotPassword.setOnClickListener { handleForgotPassword() }
    }

    // ── Sign-up ──────────────────────────────────────────────────────────────

    private fun handleSignUp() {
        val email    = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (DevLogin.ENABLED) {
            DevLogin.startDevSession(this)
            saveRememberMe(email)
            Toast.makeText(this, "Dev login — hello ${DevLogin.DISPLAY_USERNAME}", Toast.LENGTH_SHORT).show()
            navigateToMain()
            return
        }

        loginButton.isEnabled  = false
        signUpButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val username = email.substringBefore('@').lowercase()
                UserRepository.createUserDocument(result.user!!.uid, username)
                saveRememberMe(email)
                Toast.makeText(this@LoginActivity, "Welcome to Afekoin, $username!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Sign-up failed: ${e.message}", Toast.LENGTH_SHORT).show()
                loginButton.isEnabled  = true
                signUpButton.isEnabled = true
            }
        }
    }

    // ── Login ────────────────────────────────────────────────────────────────

    private fun handleLogin() {
        val email    = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (DevLogin.ENABLED) {
            DevLogin.startDevSession(this)
            saveRememberMe(email)
            navigateToMain()
            return
        }

        loginButton.isEnabled  = false
        signUpButton.isEnabled = false

        lifecycleScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                saveRememberMe(email)
                navigateToMain()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                loginButton.isEnabled  = true
                signUpButton.isEnabled = true
            }
        }
    }

    // ── Forgot password ──────────────────────────────────────────────────────

    private fun handleForgotPassword() {
        val email = emailEditText.text.toString().trim()

        if (email.isBlank()) {
            Toast.makeText(this, "Enter your email first, then tap Forgot password", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                Toast.makeText(
                    this@LoginActivity,
                    "Reset email sent to $email — check your inbox",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Could not send reset email: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Saves or clears the remembered email based on the checkbox state. */
    private fun saveRememberMe(email: String) {
        prefs.edit().apply {
            if (checkRememberMe.isChecked) {
                putString(KEY_EMAIL, email)
            } else {
                remove(KEY_EMAIL)
            }
            apply()
        }
    }

    private fun navigateToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    companion object {
        private const val PREFS_NAME = "afekoin_login_prefs"
        private const val KEY_EMAIL  = "remembered_email"
    }
}
