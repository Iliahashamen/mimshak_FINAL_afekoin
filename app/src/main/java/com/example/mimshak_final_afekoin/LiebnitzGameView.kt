package com.example.mimshak_final_afekoin

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.random.Random

class LiebnitzGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintPlayer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00C853")
    }
    private val paintPlayerGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6600C853")
        maskFilter = BlurMaskFilter(32f, BlurMaskFilter.Blur.NORMAL)
    }
    private val paintObstacle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF453A")
    }
    private val paintObstacleGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66FF453A")
        maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
    }
    private val paintLane = Paint().apply { color = Color.parseColor("#0D1117") }
    private val paintDivider = Paint().apply {
        color = Color.parseColor("#21262D")
        strokeWidth = 2f
    }
    private val paintStars = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 120
    }

    private var running = true
    private var playerLane = 1
    private var scoreInt = 0
    private val obstacles = mutableListOf<Obstacle>()
    private var tickCount = 0

    // Stars for background parallax
    private val stars = mutableListOf<Star>()
    private var starsInitialized = false

    var onScoreChanged: ((Int) -> Unit)? = null
    var onCrash: ((Int) -> Unit)? = null

    private data class Obstacle(val lane: Int, var y: Float, val speed: Float)
    private data class Star(var x: Float, var y: Float, val radius: Float, val speed: Float)

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
        super.onDetachedFromWindow()
        removeCallbacks(loop)
        running = false
    }

    private fun initStars() {
        if (starsInitialized || width == 0) return
        repeat(60) {
            stars.add(
                Star(
                    Random.nextFloat() * width,
                    Random.nextFloat() * height,
                    Random.nextFloat() * 2f + 0.5f,
                    Random.nextFloat() * 2f + 1f
                )
            )
        }
        starsInitialized = true
    }

    private fun tick() {
        initStars()
        val h = height.toFloat()
        val w = width.toFloat()
        val laneW = w / 3f

        // Speed ramp: increases every 10 points, capped at 2.5x
        val speedMultiplier = 1f + (scoreInt / 10f) * 0.15f
        val speedBase = min(14f, h * 0.006f) * speedMultiplier.coerceAtMost(2.5f)

        // Move stars (parallax)
        stars.forEach { s ->
            s.y += s.speed * speedMultiplier.coerceAtMost(2f) * 0.5f
            if (s.y > h) s.y = -4f
        }

        obstacles.forEach { it.y += it.speed * speedBase }

        obstacles.removeAll { obstacle ->
            if (obstacle.y > h + 80f) {
                scoreInt++
                onScoreChanged?.invoke(scoreInt)
                true
            } else false
        }

        tickCount++
        // Spawn rate increases with score
        val spawnInterval = (38 - scoreInt / 5).coerceAtLeast(18)
        if (tickCount % spawnInterval == 0) {
            val lane = Random.nextInt(0, 3)
            obstacles.add(Obstacle(lane, -80f, Random.nextDouble(0.9, 1.3).toFloat()))
        }

        val playerTop = h * 0.78f
        val playerBottom = h * 0.92f
        val px = laneW * playerLane + laneW * 0.2f
        val playerRect = RectF(px, playerTop, px + laneW * 0.6f, playerBottom)

        for (o in obstacles) {
            val ox = laneW * o.lane + laneW * 0.15f
            val oy = o.y
            val obs = RectF(ox, oy, ox + laneW * 0.7f, oy + h * 0.07f)
            if (RectF.intersects(playerRect, obs)) {
                running = false
                removeCallbacks(loop)
                onCrash?.invoke(scoreInt)
                return
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!running) return false
        if (event.action == MotionEvent.ACTION_DOWN) {
            playerLane = if (event.x < width / 2f) {
                (playerLane - 1).coerceAtLeast(0)
            } else {
                (playerLane + 1).coerceAtMost(2)
            }
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val laneW = w / 3f

        // Background
        canvas.drawRect(0f, 0f, w, h, paintLane)

        // Stars
        stars.forEach { s -> canvas.drawCircle(s.x, s.y, s.radius, paintStars) }

        // Lane dividers
        for (i in 1 until 3) {
            canvas.drawLine(laneW * i, 0f, laneW * i, h, paintDivider)
        }

        // Player glow + body
        val px = laneW * playerLane + laneW * 0.2f
        val playerTop = h * 0.78f
        val playerRect = RectF(px, playerTop, px + laneW * 0.6f, h * 0.92f)
        canvas.drawRoundRect(playerRect, 16f, 16f, paintPlayerGlow)
        canvas.drawRoundRect(playerRect, 16f, 16f, paintPlayer)

        // Obstacles glow + body
        for (o in obstacles) {
            val ox = laneW * o.lane + laneW * 0.15f
            val obsRect = RectF(ox, o.y, ox + laneW * 0.7f, o.y + h * 0.07f)
            canvas.drawRoundRect(obsRect, 8f, 8f, paintObstacleGlow)
            canvas.drawRoundRect(obsRect, 8f, 8f, paintObstacle)
        }
    }

    fun resetGame() {
        obstacles.clear()
        scoreInt = 0
        playerLane = 1
        tickCount = 0
        running = true
        starsInitialized = false
        stars.clear()
        onScoreChanged?.invoke(0)
        removeCallbacks(loop)
        post(loop)
        invalidate()
    }
}
