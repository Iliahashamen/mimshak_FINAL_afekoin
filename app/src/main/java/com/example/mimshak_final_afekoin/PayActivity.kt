package com.example.mimshak_final_afekoin

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mimshak_final_afekoin.firebase.FirebaseWallet
import com.example.mimshak_final_afekoin.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class PayActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay)

        tvBalance = findViewById(R.id.tvPayBalance)
        loadBalance()

        fun buy(amount: Double, label: String) {
            lifecycleScope.launch {
                try {
                    FirebaseWallet.charge(amount, "Store: $label")
                    SoundFx.success()
                    Toast.makeText(
                        this@PayActivity,
                        "✓ Purchased $label! Check History to see the transaction.",
                        Toast.LENGTH_LONG
                    ).show()
                    loadBalance()
                } catch (e: Exception) {
                    SoundFx.error()
                    Toast.makeText(
                        this@PayActivity,
                        e.message ?: "Checkout failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        findViewById<Button>(R.id.btnBuyNotebook).setOnClickListener { buy(5.0, "Notebook") }
        findViewById<Button>(R.id.btnBuyCoffee).setOnClickListener { buy(3.0, "Coffee voucher") }
        findViewById<Button>(R.id.btnBuyHoodie).setOnClickListener { buy(25.0, "Afeka hoodie") }
        findViewById<Button>(R.id.btnPayBack).setOnClickListener { finish() }
    }

    private fun loadBalance() {
        lifecycleScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val profile = UserRepository.getProfile(uid)
                tvBalance.text = String.format("%.2f AFK", profile?.balance ?: 0.0)
            } catch (_: Exception) {
                tvBalance.text = "—"
            }
        }
    }
}
