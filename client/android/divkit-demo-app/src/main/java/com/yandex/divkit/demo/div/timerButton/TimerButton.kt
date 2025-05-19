package com.yandex.divkit.demo.div.timerButton

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat

class TimerButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private var seconds: Int = 60,
    defStyleAttr: Int = android.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var timer: CountDownTimer? = null
     // پیش‌فرض ۲ دقیقه

    init {
        setupButton()
        startCountdown()
    }

    private fun setupButton() {
        isEnabled = false
        updateBackground()
        text = "منتظر بمانید"
        setTextColor(ContextCompat.getColor(context, android.R.color.white))
        textSize = 18f
        setOnClickListener {

        }
    }
    private fun updateBackground() {
        if (isEnabled) {
            setBackgroundResource(com.yandex.divkit.demo.R.drawable.button_background)
        } else {
            setBackgroundResource(com.yandex.divkit.demo.R.drawable.button_disabled_background)
        }
    }
    private fun startCountdown() {
        timer?.cancel()

        timer = object : CountDownTimer(seconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                text = "منتظر بمانید (${toPersianNumber(seconds.toString())})"
            }

            override fun onFinish() {
                isEnabled = true
                text = "ادامه"
                updateBackground()
            }
        }.start()
    }

    fun setDuration(minutes: Int) {
        seconds = (minutes * 60 * 1000L).toInt()
        resetTimer()
    }

    fun resetTimer() {
        isEnabled = false
        updateBackground()
        startCountdown()
    }

    private fun toPersianNumber(number: String): String {
        val persianDigits = listOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
        return number.map { if (it.isDigit()) persianDigits[it.toString().toInt()] else it }.joinToString("")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timer?.cancel()
    }
}
