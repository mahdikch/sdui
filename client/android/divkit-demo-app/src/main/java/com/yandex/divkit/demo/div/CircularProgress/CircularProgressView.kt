package com.yandex.divkit.demo.div.CircularProgress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    var current: Int = 0,
    var total: Int = 100,
    var size: Int = 32
) : View(context, attrs, defStyle) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0B7D2A") // رنگ سبز
        style = Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = size.toFloat()
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(centerX, centerY) - 30

        // دایره خاکستری
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // زاویه پیشرفت
        val sweepAngle = 360f * current / total

        // دایره سبز (پیشرفت)
        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        // نوشتن عدد بالا و پایین
        val fontMetrics = textPaint.fontMetrics
        val textHeight = fontMetrics.descent - fontMetrics.ascent

        // فاصله دلخواه بین خط و متن
        val textLinePadding = 10f

        // نوشتن عدد بالا و پایین با دقت
        val topText = toPersianNumber(current.toString())
        val bottomText = toPersianNumber(total.toString())

        // متن بالایی بالاتر از خط با در نظر گرفتن ارتفاع خودش
        canvas.drawText(topText, centerX, centerY - textLinePadding - textHeight+70 / 2, textPaint)

        // متن پایینی پایین‌تر از خط با در نظر گرفتن ارتفاع خودش
        canvas.drawText(bottomText, centerX, centerY + textLinePadding + textHeight-20 / 2, textPaint)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {

            color = Color.LTGRAY
            strokeWidth = 6f
        }
        val horizontalPadding = 60f
        canvas.drawLine(
            centerX - (radius - horizontalPadding), centerY,
            centerX + (radius - horizontalPadding), centerY,
            linePaint
        )
    }

    private fun toPersianNumber(number: String): String {
        val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return number.map { if (it.isDigit()) persianDigits[it.toString().toInt()] else it }
            .joinToString("")
    }

    fun setValues(current: Int, total: Int) {
        this.current = current
        this.total = total
        invalidate()
    }
}