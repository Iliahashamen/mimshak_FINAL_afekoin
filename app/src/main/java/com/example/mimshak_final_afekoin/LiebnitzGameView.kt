package com.example.mimshak_final_afekoin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt
import kotlin.random.Random

class LiebnitzGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val CLR_BG       = Color.parseColor("#000000")
    private val CLR_GRID     = Color.parseColor("#0A1A0A")
    private val CLR_POD      = Color.parseColor("#00D8FF")
    private val CLR_WRONG    = Color.parseColor("#FF2020")
    private val CLR_SHIP     = Color.parseColor("#00FF41")
    private val CLR_FLAME    = Color.parseColor("#FFB800")
    private val CLR_STAR     = Color.parseColor("#FFFFFF")
    private val CLR_SCANLINE = Color.parseColor("#0A000000")

    private val pBg      = Paint().apply { color = CLR_BG }
    private val pGrid    = Paint().apply { color = CLR_GRID; strokeWidth = 1f }
    private val pPod     = Paint().apply { color = CLR_POD }
    private val pWrong   = Paint().apply { color = CLR_WRONG }
    private val pShip    = Paint().apply { color = CLR_SHIP }
    private val pFlame   = Paint().apply { color = CLR_FLAME }
    private val pStar    = Paint().apply { color = CLR_STAR }
    private val pScanline = Paint().apply { color = CLR_SCANLINE }

    private val pBorder = Paint().apply {
        color = Color.parseColor("#003010")
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    // ANTI_ALIAS only on text so numbers look readable
    private val pText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = CLR_BG
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
        isFakeBoldText = true
    }

    private val pTextWrong = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
        isFakeBoldText = true
    }

    private var running    = true
    private var playerLane = 1
    private var scoreInt   = 0
    private var livesInt   = 3
    private var shakeFrames = 0
    private var shakeOffsetX = 0f

    private data class Star(var x: Float, var y: Float, val size: Float, val speed: Float)
    private val stars    = mutableListOf<Star>()
    private var starsInit = false

    private data class Pod(
        val lane: Int, var y: Float, val answer: Int, val isCorrect: Boolean, val speed: Float
    )
    private val pods = mutableListOf<Pod>()

    private var equationText  = ""
    private var correctAnswer = 0
    private var waveActive    = false

    // ship shape: 1 = filled pixel, 0 = empty
    private val shipPixels = arrayOf(
        intArrayOf(0, 0, 1, 0, 0),
        intArrayOf(0, 1, 1, 1, 0),
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 0, 1, 0, 1),
        intArrayOf(0, 0, 1, 0, 0),
        intArrayOf(0, 0, 1, 0, 0)
    )
    private val flamePixels = arrayOf(
        intArrayOf(0, 0, 1, 0, 0),
        intArrayOf(0, 1, 0, 1, 0)
    )

    var onScoreChanged: ((Int, String, Int) -> Unit)? = null
    var onCrash: ((Int) -> Unit)? = null

    // game loop
    private val loop = object : Runnable {
        override fun run() {
            if (!running || width == 0) return
            tick()
            invalidate()
            postDelayed(this, 34L)   // slightly faster than before
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

    // tick update
    private fun tick() {
        if (width == 0) return
        initStars()

        val h = height.toFloat()
        val w = width.toFloat()
        val laneW = w / 3f
        // shake apply
        if (shakeFrames > 0) {
            shakeFrames--
            shakeOffsetX = Random.nextFloat() * 24f - 12f
        } else {
            shakeOffsetX = 0f
        }

        stars.forEach { s ->
            s.y += s.speed * (1f + scoreInt * 0.03f).coerceAtMost(3f)
            if (s.y > h) s.y = 0f
        }

        // wave spawn
        if (!waveActive && pods.isEmpty()) {
            spawnWave()
            waveActive = true
        }

        val baseSpeed = (h * 0.0045f) * (1f + scoreInt * 0.045f).coerceAtMost(2.35f)
        pods.forEach { it.y += it.speed * baseSpeed }

        val shipCx  = laneW * playerLane + laneW / 2f
        val shipMidY = h * 0.85f

        val iter = pods.iterator()
        // hit check
        while (iter.hasNext()) {
            val pod = iter.next()
            val podCx   = laneW * pod.lane + laneW / 2f
            val podHalf = laneW * 0.34f

            val dx   = podCx - shipCx
            val dy   = pod.y - shipMidY
            val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

            if (dist < podHalf + laneW * 0.22f) {
                if (pod.isCorrect) {
                    scoreInt++
                    onScoreChanged?.invoke(scoreInt, equationText, livesInt)
                    pods.clear()
                    waveActive = false
                    return
                } else {
                    handleMistake()
                    return
                }
            }

            if (pod.y > h + 120f) {
                if (pod.isCorrect) {
                    handleMistake()
                    return
                } else {
                    iter.remove()
                }
            }
        }
    }

    // touch steer
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

    // frame draw
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val laneW = w / 3f

        canvas.save()
        if (shakeOffsetX != 0f) {
            canvas.translate(shakeOffsetX, 0f)
        }

        canvas.drawRect(0f, 0f, w, h, pBg)

        var gx = 0f
        while (gx < w) {
            canvas.drawLine(gx, 0f, gx, h, pGrid)
            gx += laneW
        }

        stars.forEach { s ->
            canvas.drawRect(s.x, s.y, s.x + s.size, s.y + s.size, pStar)
        }

        drawDashedLine(canvas, laneW,     0f, laneW,     h, 12f, 8f)
        drawDashedLine(canvas, laneW * 2, 0f, laneW * 2, h, 12f, 8f)

        for (pod in pods) {
            drawPixelBlock(canvas, pod, laneW)
        }

        drawPixelShip(canvas, laneW * playerLane + laneW / 2f, h * 0.85f, laneW * 0.09f)

        // retro CRT scanlines
        var sy = 0f
        while (sy < h) {
            canvas.drawRect(0f, sy, w, sy + 1f, pScanline)
            sy += 4f
        }
        canvas.restore()
    }

    // pod draw
    private fun drawPixelBlock(canvas: Canvas, pod: Pod, laneW: Float) {
        val cx    = laneW * pod.lane + laneW / 2f
        val half  = laneW * 0.36f
        val left  = cx - half
        val top   = pod.y - half
        val right = cx + half
        val bot   = pod.y + half

        // Keep all options visually identical so the correct answer is never hinted by color.
        canvas.drawRect(left, top, right, bot, pPod)
        pBorder.color = Color.parseColor("#004050")
        canvas.drawRect(left, top, right, bot, pBorder)
        val textPaint = pTextWrong
        textPaint.textSize = half * 0.9f
        canvas.drawText(pod.answer.toString(), cx, pod.y + textPaint.textSize * 0.36f, textPaint)
    }

    // ship draw
    private fun drawPixelShip(canvas: Canvas, cx: Float, cy: Float, px: Float) {
        val cols   = shipPixels[0].size
        val rows   = shipPixels.size
        val startX = cx - (cols / 2f) * px
        val startY = cy - rows * px

        val flameStartY = startY + rows * px
        flamePixels.forEachIndexed { row, rowData ->
            rowData.forEachIndexed { col, cell ->
                if (cell == 1) {
                    val x = startX + col * px
                    val y = flameStartY + row * px
                    canvas.drawRect(x, y, x + px - 1f, y + px - 1f, pFlame)
                }
            }
        }

        shipPixels.forEachIndexed { row, rowData ->
            rowData.forEachIndexed { col, cell ->
                if (cell == 1) {
                    val x = startX + col * px
                    val y = startY + row * px
                    canvas.drawRect(x, y, x + px - 1f, y + px - 1f, pShip)
                }
            }
        }
    }

    // lane draw
    private fun drawDashedLine(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float,
                                dashLen: Float, gapLen: Float) {
        val p = Paint().apply {
            color = Color.parseColor("#1A3020")
            strokeWidth = 2f
        }
        var y = y1
        while (y < y2) {
            canvas.drawLine(x1, y, x2, (y + dashLen).coerceAtMost(y2), p)
            y += dashLen + gapLen
        }
    }

    // wave build
    private fun spawnWave() {
        val correctLane = Random.nextInt(3)
        val (eq, ans)   = generateEquation()
        equationText    = eq
        correctAnswer   = ans

        val wrongAnswers = generateWrongAnswers(ans)
        var wrongIdx = 0
        val speed = 1f + Random.nextFloat() * 0.3f
        repeat(3) { lane ->
            val answer = if (lane == correctLane) ans else wrongAnswers[wrongIdx++]
            pods.add(Pod(lane, -120f, answer, lane == correctLane, speed))
        }
        onScoreChanged?.invoke(scoreInt, equationText, livesInt)
    }

    // math make
    private fun generateEquation(): Pair<String, Int> {
        return when (Random.nextInt(3)) {
            0    -> { val a = Random.nextInt(3, 15); val b = Random.nextInt(3, 15)
                      Pair("$a + $b", a + b) }
            1    -> { val a = Random.nextInt(8, 20); val b = Random.nextInt(1, a)
                      Pair("$a - $b", a - b) }
            else -> { val a = Random.nextInt(2, 10); val b = Random.nextInt(2, 10)
                      Pair("$a x $b", a * b) }
        }
    }

    // wrong make
    private fun generateWrongAnswers(correct: Int): List<Int> {
        val wrongs = mutableSetOf<Int>()
        while (wrongs.size < 2) {
            val offset = Random.nextInt(1, 8) * (if (Random.nextBoolean()) 1 else -1)
            val wrong  = (correct + offset).coerceAtLeast(0)
            if (wrong != correct) wrongs.add(wrong)
        }
        return wrongs.toList()
    }

    // stars init
    private fun initStars() {
        if (starsInit || width == 0) return
        repeat(40) {
            stars.add(Star(
                Random.nextFloat() * width,
                Random.nextFloat() * height,
                if (Random.nextBoolean()) 1f else 2f,
                Random.nextFloat() * 2f + 1f
            ))
        }
        starsInit = true
    }

    fun getCurrentEquation() = equationText

    // game reset
    fun resetGame() {
        pods.clear()
        scoreInt   = 0
        livesInt   = 3
        playerLane = 1
        waveActive = false
        running    = true
        shakeFrames = 0
        shakeOffsetX = 0f
        starsInit  = false
        stars.clear()
        equationText = ""
        onScoreChanged?.invoke(0, "", livesInt)
        removeCallbacks(loop)
        post(loop)
        invalidate()
    }

    // life drop
    private fun handleMistake() {
        shakeFrames = 8
        livesInt--
        if (livesInt <= 0) {
            running = false
            removeCallbacks(loop)
            onCrash?.invoke(scoreInt)
            return
        }
        pods.clear()
        waveActive = false
        onScoreChanged?.invoke(scoreInt, equationText, livesInt)
    }
}
