package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.supabase.gotrue.auth
import io.supabase.postgrest.postgrest
import io.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvTransactions)
        rv.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            try {
                val user = SupabaseManager.client.auth.currentUserOrNull()
                if (user == null) {
                    startActivity(Intent(this@HistoryActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                    return@launch
                }
                val rows = SupabaseManager.client.postgrest
                    .from("transactions")
                    .select {
                        filter("user_id", "eq", user.id)
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Transaction>()
                rv.adapter = HistoryAdapter(rows)
            } catch (e: Exception) {
                Toast.makeText(
                    this@HistoryActivity,
                    "Could not load history: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                rv.adapter = HistoryAdapter(emptyList())
            }
        }
    }
}
