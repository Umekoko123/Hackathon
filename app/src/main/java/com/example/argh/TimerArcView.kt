package com.example.argh

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class TimerArcView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // Match your Compose design: start 135°, sweep 270°
    private val startAngle = 135f
    private val sweepAngle = 270f

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        color = 0xFF333333.toInt()
        strokeWidth = dp(16f)
    }

    private val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = 0xFFFFFFFF.toInt()
        strokeWidth = dp(16f)
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFFFFFFF.toInt()
    }

    private val arcRect = RectF()

    // 1.0 = full remaining, 0.0 = done
    private var progress = 1f

    fun setProgress(p: Float) {
        progress = p.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val size = min(w, h)

        val stroke = bgPaint.strokeWidth
        val radius = size / 2f - stroke / 2f
        val cx = w / 2f
        val cy = h / 2f

        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // Background arc
        canvas.drawArc(arcRect, startAngle, sweepAngle, false, bgPaint)

        // Foreground arc (remaining)
        val remainingSweep = sweepAngle * progress
        val fgStart = startAngle + sweepAngle - remainingSweep
        canvas.drawArc(arcRect, fgStart, remainingSweep, false, fgPaint)

        // Dot position (moves with time)
        val currentAngle = startAngle + sweepAngle * (1f - progress)
        val rad = Math.toRadians(currentAngle.toDouble())
        val dotX = cx + radius * cos(rad).toFloat()
        val dotY = cy + radius * sin(rad).toFloat()

        canvas.drawCircle(dotX, dotY, dp(14f), dotPaint)
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
}
