package com.example.mimshak_final_afekoin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.random.Random

class LiebnitzGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintPlayer = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#7CB342") }
    private val paintObstacle = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E53935") }
    private val paintLane = Paint().apply { color = Color.parseColor("#22303C") }
    private val paintDivider =
        Paint().apply { color = Color.parseColor("#384D5C"); strokeWidth = 4f }

    private var running = true
    private var playerLane = 1
    private var scoreInt = 0
    private val obstacles = mutableListOf<Obstacle>()
    private var tickCount = 0

    var onScoreChanged: ((Int) -> Unit)? = null
    var onCrash: ((Int) -> Unit)? = null

    private data class Obstacle(val lane: Int, var y: Float, val speed: Float)

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

    private fun tick() {
        val h = height.toFloat()
        val w = width.toFloat()
        val laneW = w / 3f
        val speedBase = min(14f, h * 0.006f)

        obstacles.forEach { it.y += it.speed * speedBase }

        obstacles.removeAll { obstacle ->
            if (obstacle.y > h + 80f) {
                scoreInt++
                onScoreChanged?.invoke(scoreInt)
                true
            } else false
        }

        tickCount++
        if (tickCount % 38 == 0) {
            val lane = Random.nextInt(0, 3)
            obstacles.add(Obstacle(lane, -80f, Random.nextDouble(0.9, 1.25).toFloat()))
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

        canvas.drawRect(0f, 0f, w, h, paintLane)
        for (i in 1 until 3) {
            canvas.drawLine(laneW * i, 0f, laneW * i, h, paintDivider)
        }

        val px = laneW * playerLane + laneW * 0.2f
        val playerTop = h * 0.78f
        canvas.drawRoundRect(
            RectF(px, playerTop, px + laneW * 0.6f, h * 0.92f),
            16f, 16f, paintPlayer
        )

        for (o in obstacles) {
            val ox = laneW * o.lane + laneW * 0.15f
            canvas.drawRoundRect(
                RectF(ox, o.y, ox + laneW * 0.7f, o.y + h * 0.07f),
                8f, 8f, paintObstacle
            )
        }
    }

    fun resetGame() {
        obstacles.clear()
        scoreInt = 0
        playerLane = 1
        tickCount = 0
        running = true
        onScoreChanged?.invoke(0)
        removeCallbacks(loop)
        post(loop)
        invalidate()
    }
}
