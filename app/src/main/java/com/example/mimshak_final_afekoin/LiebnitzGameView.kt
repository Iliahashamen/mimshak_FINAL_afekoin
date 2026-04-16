package com.example.mimshak_final_afekoin

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Spaceship math game:
 * - An equation is shown at the top
 * - Three pods fall, each with a number — only one is the correct answer
 * - Player steers the spaceship (tap left / right) into the correct pod lane
 * - Hitting the correct pod → score +1, new equation
 * - Hitting a wrong pod → game over
 */
class LiebnitzGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ── Paints ───────────────────────────────────────────────────────────────

    private val paintBg = Paint().apply { color = Color.parseColor("#0D1117") }
    private val paintLane = Paint().apply {
        color = Color.parseColor("#21262D")
        strokeWidth = 2f
    }
    private val paintStars = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 100
    }

    // Correct pod (green)
    private val paintCorrect = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00C853")
    }
    private val paintCorrectGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5500C853")
        maskFilter = BlurMaskFilter(24f, BlurMaskFilter.Blur.NORMAL)
    }

    // Wrong pod (red)
    private val paintWrong = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF453A")
    }
    private val paintWrongGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#55FF453A")
        maskFilter = BlurMaskFilter(24f, BlurMaskFilter.Blur.NORMAL)
    }

    // Spaceship
    private val paintShip = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00C853")
    }
    private val paintShipGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4400C853")
        maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
    }
    private val paintEngine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD60A")
        maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
    }

    // Text on pods
    private val paintPodText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 44f
        isFakeBoldText = true
    }

    // ── Game state ───────────────────────────────────────────────────────────

    private var running = true
    private var playerLane = 1
    private var scoreInt = 0

    private data class Star(var x: Float, var y: Float, val r: Float, val speed: Float)
    private val stars = mutableListOf<Star>()
    private var starsInit = false

    private data class Pod(
        val lane: Int,
        var y: Float,
        val answer: Int,
        val isCorrect: Boolean,
        val speed: Float
    )
    private val pods = mutableListOf<Pod>()

    // Current equation
    private var equationText = ""
    private var correctAnswer = 0
    private var waveActive = false

    var onScoreChanged: ((Int, String) -> Unit)? = null
    var onCrash: ((Int) -> Unit)? = null

    // ── Loop ─────────────────────────────────────────────────────────────────

    private val loop = object : Runnable {
        override fun run() {
            if (!running || width == 0) return
            tick()
            invalidate()
            postDelayed(this, 32L)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(loop)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(loop)
        running = false
        super.onDetachedFromWindow()
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    private fun tick() {
        if (width == 0) return
        initStars()

        val h = height.toFloat()
        val w = width.toFloat()
        val laneW = w / 3f

        // Star parallax
        val speedMult = 1f + (scoreInt / 8f) * 0.2f
        stars.forEach { s ->
            s.y += s.speed * speedMult.coerceAtMost(3f) * 0.5f
            if (s.y > h) s.y = -4f
        }

        // Spawn a new wave when no pods on screen
        if (!waveActive && pods.isEmpty()) {
            spawnWave()
            waveActive = true
        }

        // Move pods
        val baseSpeed = (h * 0.004f) * (1f + scoreInt * 0.04f).coerceAtMost(2.2f)
        pods.forEach { it.y += it.speed * baseSpeed }

        // Ship position
        val shipCx = laneW * playerLane + laneW / 2f
        val shipTop = h * 0.76f
        val shipBottom = h * 0.94f
        val shipHalfW = laneW * 0.28f

        // Collision check
        val iter = pods.iterator()
        while (iter.hasNext()) {
            val pod = iter.next()
            val podCx = laneW * pod.lane + laneW / 2f
            val podR = laneW * 0.32f
            val podCy = pod.y

            // Check if pod overlaps with ship
            val dx = podCx - shipCx
            val dy = podCy - (shipTop + shipBottom) / 2f
            val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

            if (dist < podR + laneW * 0.25f) {
                if (pod.isCorrect) {
                    // Correct — score and next wave
                    scoreInt++
                    onScoreChanged?.invoke(scoreInt, equationText)
                    iter.remove()
                    pods.clear()
                    waveActive = false
                } else {
                    // Wrong — game over
                    running = false
                    removeCallbacks(loop)
                    onCrash?.invoke(scoreInt)
                    return
                }
            }

            // Pod exited screen — if correct pod missed, game over
            if (pod.y > h + 100f) {
                if (pod.isCorrect) {
                    running = false
                    removeCallbacks(loop)
                    onCrash?.invoke(scoreInt)
                    return
                } else {
                    iter.remove()
                }
            }
        }
    }

    private fun spawnWave() {
        val correctLane = Random.nextInt(3)
        val (eq, ans) = generateEquation()
        equationText = eq
        correctAnswer = ans

        val wrongAnswers = generateWrongAnswers(ans)
        var wrongIdx = 0

        val speed = 1f + Random.nextFloat() * 0.3f
        repeat(3) { lane ->
            val answer = if (lane == correctLane) ans else wrongAnswers[wrongIdx++]
            pods.add(Pod(lane, -120f, answer, lane == correctLane, speed))
        }
        onScoreChanged?.invoke(scoreInt, equationText)
    }

    private fun generateEquation(): Pair<String, Int> {
        val op = Random.nextInt(3)
        return when (op) {
            0 -> {
                val a = Random.nextInt(3, 15)
                val b = Random.nextInt(3, 15)
                Pair("$a + $b", a + b)
            }
            1 -> {
                val a = Random.nextInt(8, 20)
                val b = Random.nextInt(1, a)
                Pair("$a − $b", a - b)
            }
            else -> {
                val a = Random.nextInt(2, 10)
                val b = Random.nextInt(2, 10)
                Pair("$a × $b", a * b)
            }
        }
    }

    private fun generateWrongAnswers(correct: Int): List<Int> {
        val wrongs = mutableSetOf<Int>()
        while (wrongs.size < 2) {
            val offset = Random.nextInt(1, 8) * (if (Random.nextBoolean()) 1 else -1)
            val wrong = (correct + offset).coerceAtLeast(0)
            if (wrong != correct) wrongs.add(wrong)
        }
        return wrongs.toList()
    }

    // ── Touch ────────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!running) return false
        if (event.action == MotionEvent.ACTION_DOWN) {
            playerLane = if (event.x < width / 2f)
                (playerLane - 1).coerceAtLeast(0)
            else
                (playerLane + 1).coerceAtMost(2)
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    // ── Draw ─────────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val laneW = w / 3f

        // Background
        canvas.drawRect(0f, 0f, w, h, paintBg)

        // Stars
        stars.forEach { canvas.drawCircle(it.x, it.y, it.r, paintStars) }

        // Lane dividers
        for (i in 1..2) {
            canvas.drawLine(laneW * i, 0f, laneW * i, h, paintLane)
        }

        // Pods
        for (pod in pods) {
            val cx = laneW * pod.lane + laneW / 2f
            val r = laneW * 0.32f
            val glowPaint = if (pod.isCorrect) paintCorrectGlow else paintWrongGlow
            val fillPaint = if (pod.isCorrect) paintCorrect else paintWrong
            canvas.drawCircle(cx, pod.y, r + 8f, glowPaint)
            canvas.drawCircle(cx, pod.y, r, fillPaint)
            paintPodText.textSize = r * 0.72f
            canvas.drawText(pod.answer.toString(), cx, pod.y + paintPodText.textSize * 0.36f, paintPodText)
        }

        // Spaceship
        drawSpaceship(canvas, laneW * playerLane + laneW / 2f, h * 0.85f, laneW * 0.32f)
    }

    private fun drawSpaceship(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        // Engine flame
        val flamePath = Path().apply {
            moveTo(cx, cy + size * 1.1f)
            lineTo(cx - size * 0.18f, cy + size * 0.55f)
            lineTo(cx + size * 0.18f, cy + size * 0.55f)
            close()
        }
        canvas.drawPath(flamePath, paintEngine)

        // Ship body (glow)
        val glowPath = Path().apply {
            moveTo(cx, cy - size)
            lineTo(cx - size * 0.6f, cy + size * 0.6f)
            lineTo(cx - size * 0.22f, cy + size * 0.35f)
            lineTo(cx + size * 0.22f, cy + size * 0.35f)
            lineTo(cx + size * 0.6f, cy + size * 0.6f)
            close()
        }
        canvas.drawPath(glowPath, paintShipGlow)

        // Ship body
        val bodyPath = Path().apply {
            moveTo(cx, cy - size)
            lineTo(cx - size * 0.5f, cy + size * 0.5f)
            lineTo(cx - size * 0.18f, cy + size * 0.25f)
            lineTo(cx + size * 0.18f, cy + size * 0.25f)
            lineTo(cx + size * 0.5f, cy + size * 0.5f)
            close()
        }
        canvas.drawPath(bodyPath, paintShip)

        // Cockpit window
        val cockpitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#001F0E")
        }
        canvas.drawOval(
            RectF(cx - size * 0.2f, cy - size * 0.4f, cx + size * 0.2f, cy + size * 0.05f),
            cockpitPaint
        )
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun initStars() {
        if (starsInit || width == 0) return
        repeat(50) {
            stars.add(Star(
                Random.nextFloat() * width,
                Random.nextFloat() * height,
                Random.nextFloat() * 2f + 0.5f,
                Random.nextFloat() * 2f + 1f
            ))
        }
        starsInit = true
    }

    fun getCurrentEquation() = equationText

    fun resetGame() {
        pods.clear()
        scoreInt = 0
        playerLane = 1
        waveActive = false
        running = true
        starsInit = false
        stars.clear()
        equationText = ""
        onScoreChanged?.invoke(0, "")
        removeCallbacks(loop)
        post(loop)
        invalidate()
    }
}
