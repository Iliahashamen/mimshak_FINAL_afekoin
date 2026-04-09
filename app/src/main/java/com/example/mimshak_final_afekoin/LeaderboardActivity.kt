package com.example.mimshak_final_afekoin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mimshak_final_afekoin.firebase.UserRepository
import kotlinx.coroutines.launch

/** לוח תוצאות גלובלי — כל המשתמשים רואים איזונים מעודכנים (שיתוף מידע). */
class LeaderboardActivity : AppCompatActivity() {

    private lateinit var rvLeaderboard: RecyclerView

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
                val profiles = UserRepository.leaderboardTop()
                rvLeaderboard.adapter = LeaderboardAdapter(profiles)
            } catch (e: Exception) {
                Toast.makeText(this@LeaderboardActivity, "Failed to load leaderboard: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
