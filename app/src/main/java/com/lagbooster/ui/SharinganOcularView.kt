package com.lagbooster.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.sin

class SharinganOcularView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val outerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#FF1744")
        strokeWidth = 4f
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#40FF1744")
        strokeWidth = 2f
    }

    private val irisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val tomoePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FF1744")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private var rotationAngle = 0f
    private var corePulseRadius = 0f
    private var onPlayClickListener: (() -> Unit)? = null

    private val rotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 10000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            rotationAngle = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        rotationAnimator.start()
    }

    fun setOnPlayClickListener(listener: () -> Unit) {
        this.onPlayClickListener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cx = w / 2f
        val cy = h / 2f
        val radius = Math.min(w, h) / 2f * 0.8f

        val gradient = RadialGradient(
            cx, cy, radius,
            intArrayOf(Color.parseColor("#FF1744"), Color.parseColor("#800000"), Color.BLACK),
            floatArrayOf(0.0f, 0.7f, 1.0f),
            Shader.TileMode.CLAMP
        )
        irisPaint.shader = gradient
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val baseRadius = Math.min(width, height) / 2f * 0.8f

        if (baseRadius <= 0) return

        // 1. Draw outer tech ring with ticks
        canvas.drawCircle(cx, cy, baseRadius, outerRingPaint)
        canvas.drawCircle(cx, cy, baseRadius * 0.95f, outerRingPaint)

        for (i in 0 until 36) {
            val angleRad = Math.toRadians((i * 10.0))
            val x1 = (cx + (baseRadius * 0.95f) * cos(angleRad)).toFloat()
            val y1 = (cy + (baseRadius * 0.95f) * sin(angleRad)).toFloat()
            val x2 = (cx + baseRadius * cos(angleRad)).toFloat()
            val y2 = (cy + baseRadius * sin(angleRad)).toFloat()
            canvas.drawLine(x1, y1, x2, y2, tickPaint)
        }

        // 2. Draw Red Iris Gradient
        canvas.drawCircle(cx, cy, baseRadius * 0.9f, irisPaint)

        // 3. Draw Rotated Tomoe Ring
        canvas.save()
        canvas.rotate(rotationAngle, cx, cy)

        val tomoeDistance = baseRadius * 0.55f
        for (i in 0 until 3) {
            val angleRad = Math.toRadians((i * 120.0))
            val tx = (cx + tomoeDistance * cos(angleRad)).toFloat()
            val ty = (cy + tomoeDistance * sin(angleRad)).toFloat()

            // Draw Tomoe Head & Tail
            canvas.drawCircle(tx, ty, baseRadius * 0.12f, tomoePaint)
            val path = Path().apply {
                moveTo(tx, ty - baseRadius * 0.12f)
                quadTo(
                    tx + baseRadius * 0.2f, ty - baseRadius * 0.2f,
                    tx + baseRadius * 0.15f, ty + baseRadius * 0.15f
                )
                close()
            }
            canvas.drawPath(path, tomoePaint)
        }
        canvas.restore()

        // 4. Draw Glowing Central PLAY Core
        val coreRadius = baseRadius * 0.3f + corePulseRadius
        canvas.drawCircle(cx, cy, coreRadius, corePaint)

        val fontMetrics = textPaint.fontMetrics
        val textY = cy - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText("PLAY", cx, textY, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val cx = width / 2f
            val cy = height / 2f
            val dx = event.x - cx
            val dy = event.y - cy
            val dist = Math.sqrt((dx * dx + dy * dy).toDouble())

            val baseRadius = Math.min(width, height) / 2f * 0.8f
            if (dist <= baseRadius * 0.4f) {
                // Trigger pulse animation
                val pulseAnim = ValueAnimator.ofFloat(0f, 15f, 0f).apply {
                    duration = 300
                    addUpdateListener {
                        corePulseRadius = it.animatedValue as Float
                        invalidate()
                    }
                }
                pulseAnim.start()
                onPlayClickListener?.invoke()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        rotationAnimator.cancel()
    }
}
