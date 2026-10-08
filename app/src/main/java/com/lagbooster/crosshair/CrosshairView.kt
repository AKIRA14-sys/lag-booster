package com.lagbooster.crosshair

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class CrosshairView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val primaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val secondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.BLACK
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var preset: CrosshairPreset = CrosshairGenerator.getPresetById(1)
    private var customColorHex: String? = null
    private var customSizeDp: Float? = null
    private var customOpacity: Float = 1.0f
    private var customRotationDeg: Float? = null

    fun setPreset(preset: CrosshairPreset) {
        this.preset = preset
        invalidate()
    }

    fun applyCustomizations(
        colorHex: String? = null,
        sizeDp: Float? = null,
        opacity: Float = 1.0f,
        rotationDeg: Float? = null
    ) {
        this.customColorHex = colorHex
        this.customSizeDp = sizeDp
        this.customOpacity = opacity
        this.customRotationDeg = rotationDeg
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val density = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f

        if (cx == 0f || cy == 0f) return

        val primaryColor = parseColorSafely(customColorHex ?: preset.primaryColorHex)
        val secondaryColor = parseColorSafely(preset.secondaryColorHex)
        val outlineColor = parseColorSafely(preset.outlineColorHex)

        val opacityAlpha = (customOpacity.coerceIn(0.1f, 1.0f) * 255).toInt()

        primaryPaint.color = applyAlpha(primaryColor, opacityAlpha)
        secondaryPaint.color = applyAlpha(secondaryColor, opacityAlpha)
        outlinePaint.color = applyAlpha(outlineColor, opacityAlpha)
        fillPaint.color = primaryPaint.color

        val sizeScale = customSizeDp?.let { it / 32f } ?: 1.0f
        val gap = preset.gapDp * density * sizeScale
        val length = preset.lengthDp * density * sizeScale
        val thickness = (preset.thicknessDp * density * sizeScale).coerceAtLeast(1.5f * density)
        val dotSize = preset.dotSizeDp * density * sizeScale
        val ringRadius = preset.ringRadiusDp * density * sizeScale
        val rotation = customRotationDeg ?: preset.rotationDeg

        primaryPaint.strokeWidth = thickness
        secondaryPaint.strokeWidth = thickness
        outlinePaint.strokeWidth = thickness + (2f * density)

        canvas.save()
        canvas.rotate(rotation, cx, cy)

        when (preset.baseStyle) {
            CrosshairBaseStyle.CROSS -> {
                drawCrossLines(canvas, cx, cy, gap, length)
            }
            CrosshairBaseStyle.T_SHAPE -> {
                drawTShapeLines(canvas, cx, cy, gap, length)
            }
            CrosshairBaseStyle.CIRCLE_DOT -> {
                drawCircleDot(canvas, cx, cy, ringRadius)
            }
            CrosshairBaseStyle.DIAMOND -> {
                drawDiamond(canvas, cx, cy, ringRadius)
            }
            CrosshairBaseStyle.CHEVRON -> {
                drawChevrons(canvas, cx, cy, gap, length)
            }
            CrosshairBaseStyle.DUAL_RING -> {
                drawDualRings(canvas, cx, cy, ringRadius)
            }
            CrosshairBaseStyle.SHARINGAN_TRIPLE -> {
                drawSharinganStyle(canvas, cx, cy, ringRadius)
            }
            CrosshairBaseStyle.RINNEGAN_RINGS -> {
                drawRinneganStyle(canvas, cx, cy, ringRadius)
            }
            CrosshairBaseStyle.STARBURST -> {
                drawStarburst(canvas, cx, cy, gap, length)
            }
            CrosshairBaseStyle.TRI_FORCE -> {
                drawTriForce(canvas, cx, cy, ringRadius)
            }
            CrosshairBaseStyle.HEXAGON_SNIPER -> {
                drawPolygonFrame(canvas, cx, cy, ringRadius, 6)
                drawCrossLines(canvas, cx, cy, gap, length * 0.7f)
            }
            CrosshairBaseStyle.OCTAGON_TACTICAL -> {
                drawPolygonFrame(canvas, cx, cy, ringRadius, 8)
                drawCrossLines(canvas, cx, cy, gap, length * 0.8f)
            }
            CrosshairBaseStyle.ULTRA_COMPACT -> {
                drawCrossLines(canvas, cx, cy, gap * 0.5f, length * 0.5f)
            }
            CrosshairBaseStyle.CUSTOM_CROSSHAIR -> {
                drawCustomCrosshair(canvas, cx, cy, gap, length, ringRadius)
            }
            CrosshairBaseStyle.PRO_CUSTOM_CROSSHAIR -> {
                drawProCustomCrosshair(canvas, cx, cy, gap, length, ringRadius)
            }
            CrosshairBaseStyle.CHEVRON_CROSSHAIR -> {
                drawChevronCrosshair(canvas, cx, cy, gap, length)
            }
            CrosshairBaseStyle.CHARGING_CROSSHAIR -> {
                drawChargingCrosshair(canvas, cx, cy, gap, ringRadius)
            }
        }

        if (preset.hasDot) {
            if (preset.hasOutline) {
                fillPaint.color = outlinePaint.color
                canvas.drawCircle(cx, cy, dotSize + (1f * density), fillPaint)
            }
            fillPaint.color = primaryPaint.color
            canvas.drawCircle(cx, cy, dotSize, fillPaint)
        }

        canvas.restore()
    }

    private fun drawCrossLines(canvas: Canvas, cx: Float, cy: Float, gap: Float, length: Float) {
        val offsets = arrayOf(
            floatArrayOf(cx - gap - length, cy, cx - gap, cy),
            floatArrayOf(cx + gap, cy, cx + gap + length, cy),
            floatArrayOf(cx, cy - gap - length, cx, cy - gap),
            floatArrayOf(cx, cy + gap, cx, cy + gap + length)
        )

        for (pts in offsets) {
            if (preset.hasOutline) {
                canvas.drawLine(pts[0], pts[1], pts[2], pts[3], outlinePaint)
            }
            canvas.drawLine(pts[0], pts[1], pts[2], pts[3], primaryPaint)
        }
    }

    private fun drawTShapeLines(canvas: Canvas, cx: Float, cy: Float, gap: Float, length: Float) {
        val offsets = arrayOf(
            floatArrayOf(cx - gap - length, cy, cx - gap, cy),
            floatArrayOf(cx + gap, cy, cx + gap + length, cy),
            floatArrayOf(cx, cy + gap, cx, cy + gap + length)
        )

        for (pts in offsets) {
            if (preset.hasOutline) {
                canvas.drawLine(pts[0], pts[1], pts[2], pts[3], outlinePaint)
            }
            canvas.drawLine(pts[0], pts[1], pts[2], pts[3], primaryPaint)
        }
    }

    private fun drawCircleDot(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        if (preset.hasOutline) {
            canvas.drawCircle(cx, cy, radius, outlinePaint)
        }
        canvas.drawCircle(cx, cy, radius, primaryPaint)
    }

    private fun drawDiamond(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val path = Path().apply {
            moveTo(cx, cy - radius)
            lineTo(cx + radius, cy)
            lineTo(cx, cy + radius)
            lineTo(cx - radius, cy)
            close()
        }
        if (preset.hasOutline) {
            canvas.drawPath(path, outlinePaint)
        }
        canvas.drawPath(path, primaryPaint)
    }

    private fun drawChevrons(canvas: Canvas, cx: Float, cy: Float, gap: Float, length: Float) {
        for (angle in arrayOf(0f, 90f, 180f, 270f)) {
            canvas.save()
            canvas.rotate(angle, cx, cy)
            val path = Path().apply {
                moveTo(cx - length / 2f, cy - gap - length)
                lineTo(cx, cy - gap)
                lineTo(cx + length / 2f, cy - gap - length)
            }
            if (preset.hasOutline) canvas.drawPath(path, outlinePaint)
            canvas.drawPath(path, primaryPaint)
            canvas.restore()
        }
    }

    private fun drawDualRings(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        if (preset.hasOutline) {
            canvas.drawCircle(cx, cy, radius * 0.6f, outlinePaint)
            canvas.drawCircle(cx, cy, radius, outlinePaint)
        }
        canvas.drawCircle(cx, cy, radius * 0.6f, primaryPaint)
        canvas.drawCircle(cx, cy, radius, secondaryPaint)
    }

    private fun drawSharinganStyle(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        if (preset.hasOutline) {
            canvas.drawCircle(cx, cy, radius, outlinePaint)
        }
        canvas.drawCircle(cx, cy, radius, primaryPaint)

        for (i in 0 until 3) {
            val angleRad = Math.toRadians((i * 120.0))
            val tx = (cx + radius * cos(angleRad)).toFloat()
            val ty = (cy + radius * sin(angleRad)).toFloat()

            if (preset.hasOutline) {
                fillPaint.color = outlinePaint.color
                canvas.drawCircle(tx, ty, radius * 0.25f, fillPaint)
            }
            fillPaint.color = secondaryPaint.color
            canvas.drawCircle(tx, ty, radius * 0.2f, fillPaint)
        }
    }

    private fun drawRinneganStyle(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val r1 = radius * 0.4f
        val r2 = radius * 0.7f
        val r3 = radius * 1.0f

        if (preset.hasOutline) {
            canvas.drawCircle(cx, cy, r1, outlinePaint)
            canvas.drawCircle(cx, cy, r2, outlinePaint)
            canvas.drawCircle(cx, cy, r3, outlinePaint)
        }
        canvas.drawCircle(cx, cy, r1, primaryPaint)
        canvas.drawCircle(cx, cy, r2, secondaryPaint)
        canvas.drawCircle(cx, cy, r3, primaryPaint)
    }

    private fun drawStarburst(canvas: Canvas, cx: Float, cy: Float, gap: Float, length: Float) {
        for (i in 0 until 8) {
            val angle = i * 45f
            canvas.save()
            canvas.rotate(angle, cx, cy)
            if (preset.hasOutline) {
                canvas.drawLine(cx, cy - gap - length, cx, cy - gap, outlinePaint)
            }
            canvas.drawLine(cx, cy - gap - length, cx, cy - gap, if (i % 2 == 0) primaryPaint else secondaryPaint)
            canvas.restore()
        }
    }

    private fun drawTriForce(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        for (i in 0 until 3) {
            val angleRad = Math.toRadians((i * 120.0 - 90.0))
            val tx = (cx + radius * 0.7f * cos(angleRad)).toFloat()
            val ty = (cy + radius * 0.7f * sin(angleRad)).toFloat()

            val triPath = Path().apply {
                moveTo(tx, ty - radius * 0.3f)
                lineTo(tx + radius * 0.3f, ty + radius * 0.3f)
                lineTo(tx - radius * 0.3f, ty + radius * 0.3f)
                close()
            }
            if (preset.hasOutline) canvas.drawPath(triPath, outlinePaint)
            canvas.drawPath(triPath, primaryPaint)
        }
    }

    private fun drawPolygonFrame(canvas: Canvas, cx: Float, cy: Float, radius: Float, sides: Int) {
        val path = Path()
        for (i in 0 until sides) {
            val angleRad = Math.toRadians(i * (360.0 / sides) - 90.0)
            val px = (cx + radius * cos(angleRad)).toFloat()
            val py = (cy + radius * sin(angleRad)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()

        if (preset.hasOutline) canvas.drawPath(path, outlinePaint)
        canvas.drawPath(path, secondaryPaint)
    }

    private fun drawCustomCrosshair(canvas: Canvas, cx: Float, cy: Float, gap: Float, length: Float, radius: Float) {
        drawCrossLines(canvas, cx, cy, gap, length)
        val r = radius * 1.2f
        val l = length * 0.4f
        val corners = arrayOf(
            floatArrayOf(cx - r, cy - r + l, cx - r, cy - r, cx - r + l, cy - r),
            floatArrayOf(cx + r - l, cy - r, cx + r, cy - r, cx + r, cy - r + l),
            floatArrayOf(cx + r, cy + r - l, cx + r, cy + r, cx + r - l, cy + r),
            floatArrayOf(cx - r + l, cy + r, cx - r, cy + r, cx - r, cy + r - l)
        )
        for (c in corners) {
            val path = Path().apply {
                moveTo(c[0], c[1])
                lineTo(c[2], c[3])
                lineTo(c[4], c[5])
            }
            if (preset.hasOutline) canvas.drawPath(path, outlinePaint)
            canvas.drawPath(path, secondaryPaint)
        }
    }

    private fun drawProCustomCrosshair(canvas: Canvas, cx: Float, cy: Float, gap: Float, length: Float, radius: Float) {
        drawCrossLines(canvas, cx, cy, gap, length)
        val r = radius * 1.1f
        for (i in 0 until 4) {
            val startAngle = i * 90f + 10f
            val sweepAngle = 70f
            if (preset.hasOutline) {
                canvas.drawArc(cx - r, cy - r, cx + r, cy + r, startAngle, sweepAngle, false, outlinePaint)
            }
            canvas.drawArc(cx - r, cy - r, cx + r, cy + r, startAngle, sweepAngle, false, secondaryPaint)
        }
        val outerRadius = r + length * 0.3f
        val innerRadius = r + length * 0.1f
        for (angle in arrayOf(0f, 90f, 180f, 270f)) {
            val rad = Math.toRadians(angle.toDouble())
            val x1 = (cx + innerRadius * cos(rad)).toFloat()
            val y1 = (cy + innerRadius * sin(rad)).toFloat()
            val x2 = (cx + outerRadius * cos(rad)).toFloat()
            val y2 = (cy + outerRadius * sin(rad)).toFloat()
            if (preset.hasOutline) canvas.drawLine(x1, y1, x2, y2, outlinePaint)
            canvas.drawLine(x1, y1, x2, y2, primaryPaint)
        }
    }

    private fun drawChevronCrosshair(canvas: Canvas, cx: Float, cy: Float, gap: Float, length: Float) {
        drawChevrons(canvas, cx, cy, gap, length)
        drawChevrons(canvas, cx, cy, gap + length * 0.6f, length * 0.8f)
    }

    private fun drawChargingCrosshair(canvas: Canvas, cx: Float, cy: Float, gap: Float, radius: Float) {
        val r = radius * 1.2f
        if (preset.hasOutline) {
            canvas.drawArc(cx - r, cy - r, cx + r, cy + r, 135f, 270f, false, outlinePaint)
        }
        canvas.drawArc(cx - r, cy - r, cx + r, cy + r, 135f, 270f, false, primaryPaint)

        val totalTicks = 8
        val startA = 135f
        val totalSweep = 270f
        for (i in 0..totalTicks) {
            val a = startA + i * (totalSweep / totalTicks)
            val rad = Math.toRadians(a.toDouble())
            val innerR = r - 3f * resources.displayMetrics.density
            val outerR = r + 3f * resources.displayMetrics.density
            val x1 = (cx + innerR * cos(rad)).toFloat()
            val y1 = (cy + innerR * sin(rad)).toFloat()
            val x2 = (cx + outerR * cos(rad)).toFloat()
            val y2 = (cy + outerR * sin(rad)).toFloat()
            if (preset.hasOutline) canvas.drawLine(x1, y1, x2, y2, outlinePaint)
            canvas.drawLine(x1, y1, x2, y2, secondaryPaint)
        }
    }

    private fun parseColorSafely(hex: String): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            Color.RED
        }
    }

    private fun applyAlpha(color: Int, alpha: Int): Int {
        return Color.argb(
            alpha,
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }
}
