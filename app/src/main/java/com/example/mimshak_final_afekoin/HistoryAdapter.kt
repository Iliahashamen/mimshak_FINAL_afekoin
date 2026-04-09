package com.example.mimshak_final_afekoin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mimshak_final_afekoin.data.LedgerEntry
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(private val entries: List<LedgerEntry>) :
    RecyclerView.Adapter<HistoryAdapter.TransactionViewHolder>() {

    private val displayFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount() = entries.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTranTitle)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvTranAmount)
        private val tvDate: TextView = itemView.findViewById(R.id.tvTranDate)

        fun bind(entry: LedgerEntry) {
            tvTitle.text = entry.description
            val isPositive = entry.amount >= 0
            val amountText = String.format("%s%.2f AFK", if (isPositive) "+" else "", entry.amount)
            tvAmount.text = amountText
            tvAmount.setTextColor(if (isPositive) Color.parseColor("#2E7D32") else Color.parseColor("#C62828"))
            tvDate.text = formatTimestamp(entry.createdAt)
        }
    }

    private fun formatTimestamp(ts: Timestamp?): String {
        if (ts == null) return ""
        return displayFormat.format(ts.toDate())
    }
}
