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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Login screen (existing users only).
 * New users tap CREATE ACCOUNT to go to SignUpActivity where they pick a username.
 */
class LoginActivity : AppCompatActivity() {

    private val auth get() = FirebaseAuth.getInstance()
    private val prefs by lazy { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var checkRememberMe: CheckBox
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText    = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        checkRememberMe  = findViewById(R.id.checkRememberMe)
        loginButton      = findViewById(R.id.loginButton)
        signUpButton     = findViewById(R.id.signUpButton)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        // Auto-navigate if already signed in
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        // Pre-fill remembered email
        val savedEmail = prefs.getString(KEY_EMAIL, null)
        if (!savedEmail.isNullOrBlank()) {
            emailEditText.setText(savedEmail)
            checkRememberMe.isChecked = true
        }

        loginButton.setOnClickListener { handleLogin() }

        // Open the dedicated sign-up screen
        signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        tvForgotPassword.setOnClickListener { handleForgotPassword() }
    }

    private fun handleLogin() {
        val email    = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        loginButton.isEnabled  = false
        loginButton.text = "Signing in…"

        lifecycleScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                saveRememberMe(email)
                navigateToMain()
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Login failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                loginButton.isEnabled = true
                loginButton.text = "LOG IN"
            }
        }
    }

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
                Toast.makeText(
                    this@LoginActivity,
                    "Could not send reset email: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveRememberMe(email: String) {
        prefs.edit().apply {
            if (checkRememberMe.isChecked) putString(KEY_EMAIL, email) else remove(KEY_EMAIL)
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
