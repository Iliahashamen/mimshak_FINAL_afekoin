package com.example.mimshak_final_afekoin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mimshak_final_afekoin.firebase.FirebaseWallet
import kotlinx.coroutines.launch

/** Peer-to-peer AFK transfer screen. Sends coins to another user by username. */
class TransferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        val etUser = findViewById<EditText>(R.id.etTransferUsername)
        val etAmount = findViewById<EditText>(R.id.etTransferAmount)

        findViewById<Button>(R.id.btnSendTransfer).setOnClickListener {
            val username = etUser.text.toString().trim()
            val amount = etAmount.text.toString().toDoubleOrNull()
            if (username.isEmpty() || amount == null || amount <= 0) {
                Toast.makeText(this, "Enter a username and a positive amount.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                try {
                    FirebaseWallet.transferToUsername(username, amount)
                    SoundFx.success()
                    Toast.makeText(this@TransferActivity, "Transfer sent!", Toast.LENGTH_LONG).show()
                    finish()
                } catch (e: Exception) {
                    SoundFx.error()
                    Toast.makeText(this@TransferActivity, e.message ?: "Transfer failed", Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<Button>(R.id.btnTransferBack).setOnClickListener { finish() }
    }
}
