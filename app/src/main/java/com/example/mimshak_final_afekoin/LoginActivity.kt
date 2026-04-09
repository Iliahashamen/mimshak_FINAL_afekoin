package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.supabase.gotrue.auth
import io.supabase.gotrue.providers.builtin.Email
import io.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// This data class represents the structure of our 'profiles' table
@Serializable
data class Profile(
    val id: String,
    val username: String,
    val balance: Double = 30.0 // Default welcome bonus
)

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Views
        emailEditText = findViewById(R.id.emailEditText) // Use your actual view IDs
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        loginButton = findViewById(R.id.loginButton)

        // --- Session Check ---
        lifecycleScope.launch {
            val session = SupabaseManager.client.auth.session()
            if (session != null) {
                navigateToMain()
            }
        }

        // --- Button Click Listeners ---
        signUpButton.setOnClickListener {
            handleSignUp()
        }

        loginButton.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleSignUp() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // 1. Create the user in Supabase Auth
                val user = SupabaseManager.client.auth.signUp(Email) {
                    this.email = email
                    this.password = password
                }

                // 2. Create the corresponding profile in the 'profiles' table
                if (user != null) {
                    val username = email.substringBefore('@') // Simple username generation
                    val newProfile = Profile(id = user.id, username = username)
                    
                    SupabaseManager.client.postgrest.from("profiles").upsert(newProfile)

                    Toast.makeText(this@LoginActivity, "Sign-up successful! Please log in.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Sign-up failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                SupabaseManager.client.auth.signIn(Email) {
                    this.email = email
                    this.password = password
                }
                
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