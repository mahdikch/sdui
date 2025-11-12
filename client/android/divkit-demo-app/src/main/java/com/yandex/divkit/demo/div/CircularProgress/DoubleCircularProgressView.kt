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
import kotlin.math.roundToInt

class DoubleCircularProgressView @JvmOverloads constructor(
    context: Context, 
    attrs: AttributeSet? = null, 
    defStyle: Int = 0, 
    var innerPercent: Float = 45f,
    var outerPercent: Float = 65f, 
    var centerPercent: Int = 50,
    var innerCircleColor: String = "#FFD700",
    var outerCircleColor: String = "#3A86FF"
) : View(context, attrs, defStyle) {

    // عدد نمایشی وسط
    private val density = context.resources.displayMetrics.density
    private val scaledDensity = context.resources.displayMetrics.scaledDensity

    private val outerStrokeWidth = 18f * density
    private val innerStrokeWidth = 18f * density
    private val ringSpacing = 18f * density
    private val outerPadding = 12f * density
    private val centerPadding = 12f * density

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E5E7EB")
        style = Paint.Style.STROKE
        strokeWidth = outerStrokeWidth
    }

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = outerStrokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = innerStrokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    private val outerBounds = RectF()
    private val innerBounds = RectF()

    init {
        updateTextSize()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val fontMetrics = textPaint.fontMetrics
        val textHalfHeight = (fontMetrics.descent - fontMetrics.ascent) / 2f
        val minInnerRadiusForText = textHalfHeight + centerPadding + innerStrokeWidth / 2f
        val radiusOffsetBetweenRings = ringSpacing + (outerStrokeWidth - innerStrokeWidth) / 2f
        val minOuterRadius =
            minInnerRadiusForText + radiusOffsetBetweenRings
        val minTotalRadius = minOuterRadius + outerStrokeWidth / 2f + outerPadding
        val minSize = max((minTotalRadius * 2f).roundToInt(), (220f * density).roundToInt())

        val width = resolveSize(minSize, widthMeasureSpec)
        val height = resolveSize(minSize, heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val minCenter = min(centerX, centerY)

        val outerRadius = max(minCenter - outerPadding - outerStrokeWidth / 2f, outerStrokeWidth / 2f)
        val innerRadiusCandidate =
            outerRadius - ringSpacing - (outerStrokeWidth - innerStrokeWidth) / 2f

        val fontMetrics = textPaint.fontMetrics
        val textHalfHeight = (fontMetrics.descent - fontMetrics.ascent) / 2f
        val minInnerRadiusForText = textHalfHeight + centerPadding + innerStrokeWidth / 2f
        val safeInnerRadius = max(innerRadiusCandidate, minInnerRadiusForText)

        // بک‌گراند حلقه‌ها
        canvas.drawCircle(centerX, centerY, outerRadius, bgPaint)
        bgPaint.strokeWidth = innerStrokeWidth
        canvas.drawCircle(centerX, centerY, safeInnerRadius, bgPaint)
        bgPaint.strokeWidth = outerStrokeWidth

        // مستطیل‌های قوس
        outerBounds.set(
            centerX - outerRadius,
            centerY - outerRadius,
            centerX + outerRadius,
            centerY + outerRadius
        )
        innerBounds.set(
            centerX - safeInnerRadius,
            centerY - safeInnerRadius,
            centerX + safeInnerRadius,
            centerY + safeInnerRadius
        )

        // رنگ‌های حلقه‌ها
        outerPaint.color = Color.parseColor(outerCircleColor)
        innerPaint.color = Color.parseColor(innerCircleColor)

        // رسم قوس‌ها
        canvas.drawArc(outerBounds, -90f, 360f * outerPercent / 100f, false, outerPaint)
        canvas.drawArc(innerBounds, -90f, 360f * innerPercent / 100f, false, innerPaint)

        // متن وسط با درصد فارسی - با استفاده از font metrics برای مرکز کردن دقیق
        val percentText = toPersianNumber("$centerPercent%")
        val baselineOffset = (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(percentText, centerX, centerY - baselineOffset, textPaint)
    }

    private fun toPersianNumber(input: String): String {
        val persianDigits = listOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
        return input.map { if (it.isDigit()) persianDigits[it.toString().toInt()] else it }.joinToString("")
    }

    fun setValues(inner: Float, outer: Float, center: Int) {
        this.innerPercent = inner
        this.outerPercent = outer
        this.centerPercent = center
        invalidate()
    }

    fun setColors(innerColor: String, outerColor: String) {
        this.innerCircleColor = innerColor
        this.outerCircleColor = outerColor
        invalidate()
    }

    private fun updateTextSize() {
        textPaint.textSize = 28f * scaledDensity
    }
}