package com.example.mimshak_final_afekoin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private val transactions: List<Transaction>) : RecyclerView.Adapter<HistoryAdapter.TransactionViewHolder>() {

    // Standard ISO 8601 format from Supabase with timezone
    private val supabaseDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
    // Desired output format
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount() = transactions.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTranTitle)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvTranAmount)
        private val tvDate: TextView = itemView.findViewById(R.id.tvTranDate)

        fun bind(transaction: Transaction) {
            tvTitle.text = transaction.description

            val isPositive = transaction.amount >= 0
            val amountText = String.format("%s%.2f AFK", if (isPositive) "+" else "", transaction.amount)
            tvAmount.text = amountText
            tvAmount.setTextColor(if (isPositive) Color.parseColor("#2E7D32") else Color.parseColor("#C62828"))

            tvDate.text = formatCreatedAt(transaction.created_at)
        }
    }

    private fun formatCreatedAt(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) return ""
        return try {
            val instant = java.time.Instant.parse(createdAt)
            val local = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
            val fmt = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm", Locale.getDefault())
            local.format(fmt)
        } catch (_: Exception) {
            try {
                val date = supabaseDateFormat.parse(createdAt)
                if (date != null) displayDateFormat.format(date) else createdAt.take(16)
            } catch (_: Exception) {
                createdAt.take(16)
            }
        }
    }
}