package com.yandex.divkit.demo.div.CircularProgress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View

class DoubleCircularProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0, var innerPercent: Float = 45f,
    var outerPercent: Float = 65f, var centerPercent: Int = 50
) : View(context, attrs, defStyle) {

    // عدد نمایشی وسط

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 25f
    }

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 25f
        strokeCap = Paint.Cap.ROUND
    }

    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 25f
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 64f
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        val outerRadius = Math.min(centerX, centerY) - 20
        val innerRadius = outerRadius - 40

        // بک‌گراند حلقه‌ها
        canvas.drawCircle(centerX, centerY, outerRadius, bgPaint)
        canvas.drawCircle(centerX, centerY, innerRadius, bgPaint)

        // مستطیل‌های قوس
        val outerRect = RectF(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius)
        val innerRect = RectF(centerX - innerRadius, centerY - innerRadius, centerX + innerRadius, centerY + innerRadius)


        // رنگ آبی
        outerPaint.color = Color.parseColor("#3A86FF")
        innerPaint.color = Color.parseColor("#FFD700")

        // رسم قوس‌ها
        canvas.drawArc(outerRect, -90f, 360f * outerPercent / 100f, false, outerPaint)
        canvas.drawArc(innerRect, -90f, 360f * innerPercent / 100f, false, innerPaint)

        // متن وسط با درصد فارسی
        val percentText = toPersianNumber("$centerPercent%")
        canvas.drawText(percentText, centerX, centerY + 20, textPaint)
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
}