package com.example.mimshak_final_afekoin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(private val profiles: List<Profile>) : RecyclerView.Adapter<LeaderboardAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val profile = profiles[position]
        holder.bind(profile, position + 1)
    }

    override fun getItemCount() = profiles.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvBalance: TextView = itemView.findViewById(R.id.tvBalance)

        fun bind(profile: Profile, rank: Int) {
            tvRank.text = rank.toString()
            tvUsername.text = profile.username.uppercase()
            tvBalance.text = String.format("%.2f AFK", profile.balance)
        }
    }
}