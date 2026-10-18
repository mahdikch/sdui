package com.yandex.divkit.demo.div.accidentSketch

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.widget.TextView

object SketchTextBitmapHelper {

    fun textViewToBitmap(textView: TextView): Bitmap {
        textView.isDrawingCacheEnabled = true
        textView.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(textView.drawingCache)
        textView.isDrawingCacheEnabled = false
        return bitmap
    }

    fun renderTextBitmap(text: String, textColor: Int, textSizeSp: Float, bold: Boolean = true): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = textSizeSp * 3f
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val width = (paint.measureText(text) + 16).toInt().coerceAtLeast(1)
        val height = (paint.fontMetrics.bottom - paint.fontMetrics.top + 16).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, 8f, height - 8f - paint.fontMetrics.bottom, paint)
        return bitmap
    }

    fun renderDigitBitmap(text: String, textColor: Int, circleDrawable: android.graphics.drawable.Drawable): Bitmap {
        val size = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        circleDrawable.setBounds(0, 0, size, size)
        circleDrawable.draw(canvas)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 48f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val y = size / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(text, size / 2f, y, paint)
        return bitmap
    }

    fun viewToBitmap(view: View): Bitmap {
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        return bitmap
    }
}
