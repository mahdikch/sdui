package com.yandex.divkit.demo.div.CircularProgress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    var current: Int = 0,
    var total: Int = 100,
    var size: Int = 32,
) : View(context, attrs, defStyle) {

    private val density = context.resources.displayMetrics.density

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#c8d0e4")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#193d8d")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#193d8d")
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val arcBounds = RectF()

    init {
        // Avoid default huge intrinsic size; DivKit usually sets fixed width/height.
        minimumWidth = (36 * density).toInt()
        minimumHeight = (36 * density).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val fallback = (40 * density).toInt()
        val width = resolveSize(fallback, widthMeasureSpec)
        val height = resolveSize(fallback, heightMeasureSpec)
        val side = min(width, height).coerceAtLeast((28 * density).toInt())
        setMeasuredDimension(side, side)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val stroke = max(width * 0.1f, 2.5f * density)
        trackPaint.strokeWidth = stroke
        progressPaint.strokeWidth = stroke

        val radius = max(min(centerX, centerY) - stroke / 2f - density, stroke)
        if (radius <= 0f) return

        arcBounds.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
        )

        canvas.drawCircle(centerX, centerY, radius, trackPaint)

        val safeTotal = if (total <= 0) 1 else total
        val safeCurrent = current.coerceIn(0, safeTotal)
        val sweepAngle = 360f * safeCurrent / safeTotal.toFloat()
        if (sweepAngle > 0f) {
            canvas.drawArc(arcBounds, -90f, sweepAngle, false, progressPaint)
        }

        val label = "${toPersianNumber(safeCurrent)}/${toPersianNumber(safeTotal)}"
        textPaint.textSize = (width * 0.28f).coerceIn(9f * density, 13f * density)
        val fm = textPaint.fontMetrics
        val baseline = centerY - (fm.ascent + fm.descent) / 2f
        canvas.drawText(label, centerX, baseline, textPaint)
    }

    private fun toPersianNumber(number: Int): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return number.toString().map { ch ->
            if (ch.isDigit()) persianDigits[ch - '0'] else ch
        }.joinToString("")
    }

    fun setValues(current: Int, total: Int, textSizeSp: Int = size) {
        this.current = current
        this.total = total
        this.size = textSizeSp
        invalidate()
        requestLayout()
    }
}
