package com.example.mimshak_final_afekoin

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

object RewardCelebration {

    // overlay show
    fun show(activity: AppCompatActivity, reward: Double, onDone: () -> Unit) {
        if (activity.isFinishing || activity.isDestroyed) {
            onDone()
            return
        }

        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val root = FrameLayout(activity).apply {
            setBackgroundColor(Color.parseColor("#B3000000"))
            setPadding(36, 36, 36, 36)
        }

        val fireworksLayer = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        root.addView(fireworksLayer)

        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(42, 46, 42, 46)
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor("#FF00C853"),
                    Color.parseColor("#FF00B0FF"),
                    Color.parseColor("#FFFFC400")
                )
            ).apply { cornerRadius = 34f }
        }

        val amountText = TextView(activity).apply {
            text = "+${String.format("%.2f", reward)} AFK"
            textSize = 34f
            setTextColor(Color.parseColor("#FF111111"))
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val subtitleText = TextView(activity).apply {
            text = "NICE EARN!"
            textSize = 18f
            setTextColor(Color.parseColor("#FF1A1A1A"))
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        card.addView(amountText)
        card.addView(subtitleText)

        root.addView(
            card,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )

        dialog.setContentView(root)
        dialog.show()

        // spark burst
        repeat(22) { idx ->
            val spark = View(activity).apply {
                setBackgroundColor(
                    listOf(
                        Color.parseColor("#FFFF1744"),
                        Color.parseColor("#FFFFEA00"),
                        Color.parseColor("#FF00E5FF"),
                        Color.parseColor("#FF69F0AE"),
                        Color.parseColor("#FFFF9100")
                    )[idx % 5]
                )
                alpha = 0f
            }
            val size = Random.nextInt(8, 20)
            val lp = FrameLayout.LayoutParams(size, size)
            lp.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            lp.bottomMargin = Random.nextInt(120, 320)
            lp.leftMargin = Random.nextInt(50, 560)
            fireworksLayer.addView(spark, lp)

            val rise = ObjectAnimator.ofFloat(spark, "translationY", 0f, -Random.nextInt(180, 420).toFloat())
            val drift = ObjectAnimator.ofFloat(spark, "translationX", 0f, Random.nextInt(-160, 160).toFloat())
            val alphaInOut = ObjectAnimator.ofFloat(spark, "alpha", 0f, 1f, 0f)
            AnimatorSet().apply {
                playTogether(rise, drift, alphaInOut)
                duration = Random.nextLong(800, 1500)
                startDelay = Random.nextLong(0, 550)
                interpolator = AccelerateInterpolator()
                start()
            }
        }

        // auto close
        root.postDelayed({
            if (dialog.isShowing) dialog.dismiss()
            onDone()
        }, 2000L)
    }
}
