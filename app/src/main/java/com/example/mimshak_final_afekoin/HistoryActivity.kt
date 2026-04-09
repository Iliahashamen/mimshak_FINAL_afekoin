package com.example.mimshak_final_afekoin

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvTransactions)
        rv.layoutManager = LinearLayoutManager(this)

        val prefs = getSharedPreferences("AFEKOIN_PREFS", Context.MODE_PRIVATE)
        val raw = prefs.getString("HISTORY_DATA", "") ?: ""
        val list = mutableListOf<Transaction>()
        if (raw.isNotEmpty()) {
            raw.split(";").filter { it.contains("|") }.forEach {
                val p = it.split("|")
                list.add(Transaction(p[0], p[1], p[2], p[1].contains("+")))
            }
        }
        rv.adapter = HistoryAdapter(list.reversed())
    }
}

class HistoryAdapter(private val list: List<Transaction>) : RecyclerView.Adapter<HistoryAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val t: TextView = v.findViewById(R.id.tvTranTitle)
        val d: TextView = v.findViewById(R.id.tvTranDate)
        val a: TextView = v.findViewById(R.id.tvTranAmount)
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_transaction, p, false))
    override fun onBindViewHolder(h: VH, p: Int) {
        val item = list[p]
        h.t.text = item.title; h.d.text = item.date; h.a.text = item.amount
        h.a.setTextColor(if (item.isPositive) Color.parseColor("#008542") else Color.RED)
    }
    override fun getItemCount() = list.size
}