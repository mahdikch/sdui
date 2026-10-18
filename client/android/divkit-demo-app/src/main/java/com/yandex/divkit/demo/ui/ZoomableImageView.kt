package com.yandex.divkit.demo.ui

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

/**
 * ImageView with pinch-zoom and pan for fullscreen/dialog previews.
 */
class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private val start = PointF()
    private val mid = PointF()

    private var mode = NONE
    private var minScale = 1f
    private var maxScale = 5f
    private var currentScale = 1f
    private var lastTouchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()

    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val target = if (currentScale > minScale + 0.05f) minScale else min(maxScale, minScale * 2.5f)
            zoomTo(target, e.x, e.y)
            return true
        }
    })

    init {
        scaleType = ScaleType.MATRIX
        imageMatrix = matrix
    }

    override fun setImageBitmap(bm: android.graphics.Bitmap?) {
        super.setImageBitmap(bm)
        post { resetZoom() }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (drawable != null) {
            resetZoom()
        }
    }

    fun resetZoom() {
        val d = drawable ?: return
        if (width == 0 || height == 0) return

        val dw = d.intrinsicWidth.toFloat()
        val dh = d.intrinsicHeight.toFloat()
        if (dw <= 0f || dh <= 0f) return

        val scaleX = width / dw
        val scaleY = height / dh
        minScale = min(scaleX, scaleY)
        currentScale = minScale
        maxScale = max(minScale * 5f, 5f)

        matrix.reset()
        matrix.postScale(minScale, minScale)
        val dx = (width - dw * minScale) / 2f
        val dy = (height - dh * minScale) / 2f
        matrix.postTranslate(dx, dy)
        imageMatrix = matrix
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                savedMatrix.set(matrix)
                midPoint(mid, event)
                mode = ZOOM
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG && !scaleDetector.isInProgress && currentScale > minScale + 0.01f) {
                    matrix.set(savedMatrix)
                    val dx = event.x - start.x
                    val dy = event.y - start.y
                    if (dx * dx + dy * dy > lastTouchSlop * lastTouchSlop) {
                        matrix.postTranslate(dx, dy)
                        fixTranslation()
                        imageMatrix = matrix
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                mode = NONE
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun zoomTo(targetScale: Float, focusX: Float, focusY: Float) {
        val scaleFactor = targetScale / currentScale
        matrix.postScale(scaleFactor, scaleFactor, focusX, focusY)
        currentScale = targetScale
        fixTranslation()
        imageMatrix = matrix
    }

    private fun fixTranslation() {
        val values = FloatArray(9)
        matrix.getValues(values)
        val d = drawable ?: return
        val dw = d.intrinsicWidth * values[Matrix.MSCALE_X]
        val dh = d.intrinsicHeight * values[Matrix.MSCALE_Y]
        var tx = values[Matrix.MTRANS_X]
        var ty = values[Matrix.MTRANS_Y]

        tx = when {
            dw <= width -> (width - dw) / 2f
            tx > 0f -> 0f
            tx + dw < width -> width - dw
            else -> tx
        }
        ty = when {
            dh <= height -> (height - dh) / 2f
            ty > 0f -> 0f
            ty + dh < height -> height - dh
            else -> ty
        }

        values[Matrix.MTRANS_X] = tx
        values[Matrix.MTRANS_Y] = ty
        matrix.setValues(values)
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        if (event.pointerCount < 2) return
        point.set((event.getX(0) + event.getX(1)) / 2f, (event.getY(0) + event.getY(1)) / 2f)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor
            val next = currentScale * scaleFactor
            when {
                next < minScale -> scaleFactor = minScale / currentScale
                next > maxScale -> scaleFactor = maxScale / currentScale
            }
            matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            currentScale *= scaleFactor
            fixTranslation()
            imageMatrix = matrix
            return true
        }
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }
}
