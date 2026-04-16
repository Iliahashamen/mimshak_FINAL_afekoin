package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mimshak_final_afekoin.firebase.FirestorePaths
import com.example.mimshak_final_afekoin.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Sign-up screen: email, password, and a custom username.
 * The username is what others use to send coins and what is displayed in the app.
 */
class SignUpActivity : AppCompatActivity() {

    private val auth get() = FirebaseAuth.getInstance()
    private val db   get() = FirebaseFirestore.getInstance()

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirm: EditText
    private lateinit var btnCreate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        etUsername = findViewById(R.id.etUsername)
        etEmail    = findViewById(R.id.etSignUpEmail)
        etPassword = findViewById(R.id.etSignUpPassword)
        etConfirm  = findViewById(R.id.etSignUpConfirmPassword)
        btnCreate  = findViewById(R.id.btnCreateAccount)

        btnCreate.setOnClickListener { attemptSignUp() }

        findViewById<Button>(R.id.btnBackToLogin).setOnClickListener {
            finish()
        }
    }

    private fun attemptSignUp() {
        val username = etUsername.text.toString().trim().lowercase()
        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirm  = etConfirm.text.toString()

        // validate inputs
        if (username.isBlank()) {
            etUsername.error = "Username is required"
            etUsername.requestFocus()
            return
        }
        if (!username.matches(Regex("^[a-z0-9_]{3,20}$"))) {
            etUsername.error = "3–20 characters: letters, numbers and _ only"
            etUsername.requestFocus()
            return
        }
        if (email.isBlank()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }
        if (password != confirm) {
            etConfirm.error = "Passwords do not match"
            etConfirm.requestFocus()
            return
        }

        btnCreate.isEnabled = false
        btnCreate.text = "Creating…"

        lifecycleScope.launch {
            try {
                // Check username is not already taken
                val existing = db.collection(FirestorePaths.USERS)
                    .whereEqualTo("username", username)
                    .limit(1)
                    .get()
                    .await()

                if (!existing.isEmpty) {
                    etUsername.error = "Username already taken — choose another"
                    etUsername.requestFocus()
                    resetButton()
                    return@launch
                }

                // Create Firebase Auth user
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                // Save profile with the custom username to Firestore
                UserRepository.createUserDocument(result.user!!.uid, username)

                Toast.makeText(
                    this@SignUpActivity,
                    "Welcome, $username! You have 30 AFK to start.",
                    Toast.LENGTH_LONG
                ).show()

                // Go straight to main screen
                startActivity(
                    Intent(this@SignUpActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )

            } catch (e: Exception) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Sign-up failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                resetButton()
            }
        }
    }

    private fun resetButton() {
        btnCreate.isEnabled = true
        btnCreate.text = "CREATE ACCOUNT"
    }
}
