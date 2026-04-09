package com.example.mimshak_final_afekoin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.supabase.postgrest.postgrest
import io.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var rvLeaderboard: RecyclerView
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        rvLeaderboard = findViewById(R.id.rvLeaderboard)
        rvLeaderboard.layoutManager = LinearLayoutManager(this)

        fetchLeaderboardData()
    }

    private fun fetchLeaderboardData() {
        lifecycleScope.launch {
            try {
                val profiles = SupabaseManager.client.postgrest
                    .from("profiles")
                    .select {
                        order("balance", Order.DESCENDING)
                    }.decodeList<Profile>()

                leaderboardAdapter = LeaderboardAdapter(profiles)
                rvLeaderboard.adapter = leaderboardAdapter

            } catch (e: Exception) {
                Toast.makeText(this@LeaderboardActivity, "Failed to load leaderboard: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}