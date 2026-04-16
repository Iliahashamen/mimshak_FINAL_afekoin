package com.example.mimshak_final_afekoin

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
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

// Quiz game: 15 random questions from questions.json, 10 min timer, 1 AFK per correct answer
class AfequizActivity : AppCompatActivity() {

    private lateinit var questionList: List<QuizQuestion>
    private var currentQuestionIndex = 0
    private var score = 0
    private var timeRemainingMillis: Long = 600_000L
    private var timer: CountDownTimer? = null
    private lateinit var optionButtons: List<Button>

    private val labels = listOf("A:", "B:", "C:", "D:")

    // Theme colours from colors.xml
    private val colorCard   by lazy { getColor(R.color.bg_card) }
    private val colorGreen  by lazy { getColor(R.color.accent_green) }
    private val colorRed    by lazy { getColor(R.color.accent_red) }
    private val colorTimerWarn by lazy { getColor(R.color.accent_red) }
    private val colorTimerOk   by lazy { getColor(R.color.accent_green) }

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

    private fun loadQuestions() {
        try {
            val inputStream: InputStream = assets.open("questions.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val allQuestions = mutableListOf<QuizQuestion>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val optsJson = obj.getJSONArray("options")
                val optsList = (0 until optsJson.length()).map { optsJson.getString(it) }
                allQuestions.add(
                    QuizQuestion(
                        text = obj.getString("text"),
                        options = optsList,
                        correctAnswer = obj.getString("correctAnswer")
                    )
                )
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
        val questionNum = currentQuestionIndex + 1

        findViewById<TextView>(R.id.tvProgress).text = "Question $questionNum / 15"
        findViewById<ProgressBar>(R.id.quizProgressBar).progress = questionNum
        findViewById<TextView>(R.id.tvQuestionText).text = q.text

        val shuffledOptions = q.options.shuffled()
        optionButtons.forEachIndexed { i, btn ->
            btn.isEnabled = true
            btn.text = "${labels[i]}   ${shuffledOptions[i]}"
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(colorCard)
            btn.setTextColor(getColor(R.color.text_primary))
            btn.setOnClickListener { checkAnswer(btn, q.correctAnswer, shuffledOptions[i]) }
        }
    }

    private fun checkAnswer(btn: Button, correct: String, selectedText: String) {
        optionButtons.forEach { it.isEnabled = false }

        if (selectedText == correct) {
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(colorGreen)
            btn.setTextColor(getColor(R.color.bg_primary))
            score++
            timeRemainingMillis += 10_000L
            startNewTimer(timeRemainingMillis)
            SoundFx.quizCorrect()
        } else {
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(colorRed)
            btn.setTextColor(getColor(R.color.white))
            optionButtons.find { b ->
                val rawText = b.text.toString().substringAfter("   ")
                rawText == correct
            }?.let { correct_btn ->
                correct_btn.backgroundTintList = android.content.res.ColorStateList.valueOf(colorGreen)
                correct_btn.setTextColor(getColor(R.color.bg_primary))
            }
            SoundFx.quizWrong()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            currentQuestionIndex++
            showNextQuestion()
        }, 1800)
    }

    private fun startNewTimer(millis: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(millis, 1000) {
            override fun onTick(t: Long) {
                timeRemainingMillis = t
                val mins = (t / 1000) / 60
                val secs = (t / 1000) % 60
                val tvTimer = findViewById<TextView>(R.id.tvTimer)
                tvTimer.text = String.format("%02d:%02d", mins, secs)
                tvTimer.setTextColor(if (t < 30_000L) colorTimerWarn else colorTimerOk)
            }
            override fun onFinish() { endGame() }
        }.start()
    }

    private fun endGame() {
        timer?.cancel()
        if (score > 0) {
            lifecycleScope.launch {
                try {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        navigateToLogin()
                        return@launch
                    }
                    val reward = score.toDouble()
                    FirebaseWallet.addCredits(reward, "Quiz — $score/15 correct")
                    withContext(Dispatchers.Main) {
                        SoundFx.coinEarned()
                        Toast.makeText(
                            this@AfequizActivity,
                            "+${reward.toInt()} AFK earned ($score correct answers)!",
                            Toast.LENGTH_LONG
                        ).show()
                        navigateToResults()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AfequizActivity,
                            "Error saving reward: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToResults()
                    }
                }
            }
        } else {
            navigateToResults()
        }
    }

    private fun navigateToResults() {
        startActivity(Intent(this, QuizResultActivity::class.java).apply {
            putExtra("SCORE", score)
            putExtra("SESSION_ID", System.currentTimeMillis())
        })
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
