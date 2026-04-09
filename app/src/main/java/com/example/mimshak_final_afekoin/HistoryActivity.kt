package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mimshak_final_afekoin.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/** היסטוריית תנועות — נקראת מ-Firestore (שיתוף/אחסון מרכזי). */
class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvTransactions)
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
                rv.adapter = HistoryAdapter(rows)
            } catch (e: Exception) {
                val msg = e.message ?: ""
                Toast.makeText(
                    this@HistoryActivity,
                    "Could not load history. If this mentions an index, create it in the Firebase console link from the log.\n$msg",
                    Toast.LENGTH_LONG
                ).show()
                rv.adapter = HistoryAdapter(emptyList())
            }
        }
    }
}
