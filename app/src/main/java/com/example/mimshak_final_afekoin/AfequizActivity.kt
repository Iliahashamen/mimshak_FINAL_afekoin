package com.example.mimshak_final_afekoin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mimshak_final_afekoin.firebase.FirebaseWallet
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStream

class AfequizActivity : AppCompatActivity() {

    private lateinit var questionList: List<QuizQuestion>
    private var currentQuestionIndex = 0
    private var score = 0
    private var timeRemainingMillis: Long = 600000
    private var timer: CountDownTimer? = null
    private lateinit var optionButtons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afequiz)

        optionButtons = listOf(
            findViewById(R.id.btnOpt1), findViewById(R.id.btnOpt2),
            findViewById(R.id.btnOpt3), findViewById(R.id.btnOpt4)
        )

        loadQuestions()
        startNewTimer(timeRemainingMillis)
        showNextQuestion()
    }

    private fun removeCitations(input: String): String {
        if (!input.contains("[")) return input
        return input.split("[")[0].trim()
    }

    private fun loadQuestions() {
        try {
            val inputStream: InputStream = assets.open("questions.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val allQuestions = mutableListOf<QuizQuestion>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val cleanText = removeCitations(obj.getString("text"))
                val optsJson = obj.getJSONArray("options")
                val optsList = mutableListOf<String>()
                for (j in 0 until optsJson.length()) {
                    optsList.add(removeCitations(optsJson.getString(j)))
                }
                allQuestions.add(QuizQuestion(cleanText, optsList, removeCitations(obj.getString("correctAnswer"))))
            }
            questionList = allQuestions.shuffled().take(15)
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun showNextQuestion() {
        if (currentQuestionIndex >= questionList.size) {
            endGame()
            return
        }

        val q = questionList[currentQuestionIndex]
        findViewById<TextView>(R.id.tvProgress).text = "שאלה ${currentQuestionIndex + 1}/15"
        findViewById<TextView>(R.id.tvQuestionText).text = q.text

        val shuffledOptions = q.options.shuffled()
        for (i in 0..3) {
            optionButtons[i].isEnabled = true
            optionButtons[i].text = shuffledOptions[i]
            optionButtons[i].setBackgroundColor(Color.LTGRAY)
            optionButtons[i].setOnClickListener { checkAnswer(optionButtons[i], q.correctAnswer) }
        }
    }

    private fun checkAnswer(btn: Button, correct: String) {
        optionButtons.forEach { it.isEnabled = false }
        if (btn.text == correct) {
            score++
            btn.setBackgroundColor(Color.GREEN)
            timeRemainingMillis += 10000
            startNewTimer(timeRemainingMillis)
        } else {
            btn.setBackgroundColor(Color.RED)
            optionButtons.find { it.text == correct }?.setBackgroundColor(Color.GREEN)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            currentQuestionIndex++
            showNextQuestion()
        }, 2000)
    }

    private fun startNewTimer(millis: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(millis, 1000) {
            override fun onTick(t: Long) {
                timeRemainingMillis = t
                val mins = (t / 1000) / 60
                val secs = (t / 1000) % 60
                findViewById<TextView>(R.id.tvTimer).text = String.format("%02d:%02d", mins, secs)
            }
            override fun onFinish() { endGame() }
        }.start()
    }

    private fun endGame() {
        timer?.cancel()

        // Only award points for a perfect score
        if (score == 15) {
            lifecycleScope.launch {
                try {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        navigateToLogin()
                        return@launch
                    }
                    val quizReward = 4.00
                    FirebaseWallet.addCredits(quizReward, "Quiz reward (perfect score)")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AfequizActivity, "Congratulations! You earned +$quizReward AFK!", Toast.LENGTH_LONG).show()
                        navigateToResults()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AfequizActivity, "Error saving reward: ${e.message}", Toast.LENGTH_SHORT).show()
                        navigateToResults()
                    }
                }
            }
        } else {
            navigateToResults()
        }
    }

    private fun navigateToResults() {
        val intent = Intent(this, QuizResultActivity::class.java).apply {
            putExtra("SCORE", score)
            putExtra("SESSION_ID", System.currentTimeMillis())
        }
        startActivity(intent)
        finish()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}