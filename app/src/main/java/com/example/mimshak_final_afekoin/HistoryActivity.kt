package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mimshak_final_afekoin.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/** Displays the signed-in user's full transaction history loaded from Firestore. */
class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvTransactions)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyHistory)
        rv.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) {
                    startActivity(Intent(this@HistoryActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                    return@launch
                }
                val rows = UserRepository.transactionsForUser(uid)
                if (rows.isEmpty()) {
                    rv.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    rv.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE
                    rv.adapter = HistoryAdapter(rows)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@HistoryActivity,
                    "Could not load history: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                rv.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Could not load transactions.\nCheck your internet connection and try again."
            }
        }
    }
}
