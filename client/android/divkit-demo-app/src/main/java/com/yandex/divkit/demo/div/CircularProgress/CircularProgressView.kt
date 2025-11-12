package com.yandex.divkit.demo.div.CircularProgress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    var current: Int = 0,
    var total: Int = 100,
    var size: Int = 32
) : View(context, attrs, defStyle) {

    private val density = context.resources.displayMetrics.density
    private val scaledDensity = context.resources.displayMetrics.scaledDensity

    private val strokeWidth = 8f * density
    private val separatorStrokeWidth = 4f * density
    private val textSpacing = 6f * density

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E5E5E5")
        style = Paint.Style.STROKE
        strokeWidth = this@CircularProgressView.strokeWidth
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0B7D2A")
        style = Paint.Style.STROKE
        strokeWidth = this@CircularProgressView.strokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val separatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C9CCD2")
        strokeWidth = separatorStrokeWidth
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    init {
        updateTextSize()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = (160 * density).toInt()
        val fontMetrics = textPaint.fontMetrics
        val textHeight = fontMetrics.descent - fontMetrics.ascent
        val minSizeForText = ceil(textHeight * 3f).toInt()
        val minSize = max(desiredSize, minSizeForText)

        val width = resolveSize(minSize, widthMeasureSpec)
        val height = resolveSize(minSize, heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = max(min(centerX, centerY) - strokeWidth / 2f, 0f)
        if (maxRadius <= 0f) return

        val arcBounds = RectF(
            centerX - maxRadius,
            centerY - maxRadius,
            centerX + maxRadius,
            centerY + maxRadius
        )

        // پیش‌زمینه
        canvas.drawCircle(centerX, centerY, maxRadius, backgroundPaint)

        // پیشرفت
        val safeTotal = if (total <= 0) 1 else total
        val sweepAngle = 360f * current / safeTotal.toFloat()
        canvas.drawArc(arcBounds, -90f, sweepAngle, false, progressPaint)

        // خط جداکننده
        val horizontalRadius = max(maxRadius - strokeWidth, 0f)
        canvas.drawLine(
            centerX - horizontalRadius,
            centerY,
            centerX + horizontalRadius,
            centerY,
            separatorPaint
        )

        // اعداد
        val fontMetrics = textPaint.fontMetrics
        val baselineOffset = (fontMetrics.ascent + fontMetrics.descent) / 2f
        val labelOffset = maxRadius * 0.35f + textSpacing
        val topBaseline = centerY - labelOffset - baselineOffset
        val bottomBaseline = centerY + labelOffset - baselineOffset

        canvas.drawText(
            toPersianNumber(current.toString()),
            centerX,
            topBaseline,
            textPaint
        )
        canvas.drawText(
            toPersianNumber(safeTotal.toString()),
            centerX,
            bottomBaseline,
            textPaint
        )
    }

    private fun updateTextSize() {
        val textSizeSp = if (size <= 0) 28 else size
        textPaint.textSize = textSizeSp * scaledDensity
        invalidate()
        requestLayout()
    }

    private fun toPersianNumber(number: String): String {
        val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return number.map { if (it.isDigit()) persianDigits[it.toString().toInt()] else it }
            .joinToString("")
    }

    fun setValues(current: Int, total: Int, textSizeSp: Int = size) {
        this.current = current
        this.total = total
        this.size = textSizeSp
        updateTextSize()
    }
}