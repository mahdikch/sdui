package com.yandex.divkit.demo.applock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.min

class PatternLockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    interface Listener {
        fun onPatternComplete(cells: List<Int>)
    }

    var listener: Listener? = null

    private val dots = Array(9) { FloatArray(2) }
    private val selected = mutableListOf<Int>()
    private val path = Path()
    private var touchX = 0f
    private var touchY = 0f
    private var tracking = false

    private val paintDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFC8D0E4.toInt()
    }
    private val paintSelected = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF193D8D.toInt()
    }
    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = 0xFF193D8D.toInt()
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    fun clearPattern() {
        selected.clear()
        path.reset()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = min(w, h).toFloat()
        val pad = size * 0.18f
        val step = (size - pad * 2) / 2f
        val offsetX = (w - size) / 2f
        val offsetY = (h - size) / 2f
        for (row in 0 until 3) {
            for (col in 0 until 3) {
                val i = row * 3 + col
                dots[i][0] = offsetX + pad + col * step
                dots[i][1] = offsetY + pad + row * step
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (selected.isNotEmpty()) {
            path.reset()
            val first = selected[0]
            path.moveTo(dots[first][0], dots[first][1])
            for (i in 1 until selected.size) {
                val idx = selected[i]
                path.lineTo(dots[idx][0], dots[idx][1])
            }
            if (tracking) {
                path.lineTo(touchX, touchY)
            }
            canvas.drawPath(path, paintLine)
        }
        val r = min(width, height) * 0.045f
        for (i in 0 until 9) {
            val paint = if (selected.contains(i)) paintSelected else paintDot
            canvas.drawCircle(dots[i][0], dots[i][1], r, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                tracking = true
                selected.clear()
                hitTest(event.x, event.y)?.let { selected.add(it) }
                touchX = event.x
                touchY = event.y
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                touchX = event.x
                touchY = event.y
                hitTest(event.x, event.y)?.let { idx ->
                    if (!selected.contains(idx)) selected.add(idx)
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                tracking = false
                invalidate()
                if (selected.isNotEmpty()) {
                    listener?.onPatternComplete(selected.toList())
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun hitTest(x: Float, y: Float): Int? {
        val threshold = min(width, height) * 0.08f
        for (i in 0 until 9) {
            if (hypot(x - dots[i][0], y - dots[i][1]) <= threshold) return i
        }
        return null
    }
}
