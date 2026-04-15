package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mimshak_final_afekoin.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * [התחברות] — רכיב שרת ראשון: Firebase Authentication (אימייל/סיסמה).
 * לאחר הרשמה נוצר גם מסמך ב-Firestore עם איזון התחלתי.
 */
class LoginActivity : AppCompatActivity() {

    private val auth get() = FirebaseAuth.getInstance()

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        loginButton = findViewById(R.id.loginButton)

        if (auth.currentUser != null) {
            navigateToMain()
            return
        }
        if (DevLogin.isDevSession(this)) {
            navigateToMain()
            return
        }

        signUpButton.setOnClickListener { handleSignUp() }
        loginButton.setOnClickListener { handleLogin() }
    }

    private fun handleSignUp() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (DevLogin.ENABLED) {
            DevLogin.startDevSession(this)
            Toast.makeText(this, "Dev login — hello ${DevLogin.DISPLAY_USERNAME}", Toast.LENGTH_SHORT).show()
            navigateToMain()
            return
        }

        lifecycleScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val username = email.substringBefore('@').lowercase()
                UserRepository.createUserDocument(result.user!!.uid, username)
                Toast.makeText(this@LoginActivity, "Welcome to Afekoin!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Sign-up failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (DevLogin.ENABLED) {
            DevLogin.startDevSession(this)
            navigateToMain()
            return
        }

        lifecycleScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                navigateToMain()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
