package com.example.mimshak_final_afekoin

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay)

        fun buy(amount: Double, label: String) {
            lifecycleScope.launch {
                try {
                    SupabaseWallet.charge(amount, "Store: $label")
                    Toast.makeText(this@PayActivity, "Purchased $label", Toast.LENGTH_LONG).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@PayActivity, e.message ?: "Checkout failed", Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<Button>(R.id.btnBuyNotebook).setOnClickListener { buy(5.0, "Notebook") }
        findViewById<Button>(R.id.btnBuyCoffee).setOnClickListener { buy(3.0, "Coffee voucher") }
        findViewById<Button>(R.id.btnBuyHoodie).setOnClickListener { buy(25.0, "Afeka hoodie") }
        findViewById<Button>(R.id.btnPayBack).setOnClickListener { finish() }
    }
}
