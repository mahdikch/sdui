package com.yandex.divkit.demo.div.labelledSliderView

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout


class LabelledSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val seekBar: SeekBar
    private val labelsLayout: LinearLayout
    private var items: List<String> = emptyList()
    private var onItemSelected: ((String, Int) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(com.yandex.divkit.demo.R.layout.view_labelled_slider, this, true)
        seekBar = findViewById(com.yandex.divkit.demo.R.id.slider_seekbar)
        labelsLayout = findViewById(com.yandex.divkit.demo.R.id.slider_labels_layout)
    }

    fun setItems(newItems: List<String>) {
        items = newItems
        seekBar.max = items.size - 1
        buildLabels()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress in items.indices) {
                    onItemSelected?.invoke(items[progress], progress)
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    fun setOnItemSelectedListener(listener: (String, Int) -> Unit) {
        onItemSelected = listener
    }

    fun setSelectedItem(index: Int) {
        if (index in items.indices) {
            seekBar.progress = index
        }
    }

    private fun buildLabels() {
        labelsLayout.removeAllViews()
        for (item in items) {
            val tv = TextView(context).apply {
                text = item
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            }
            labelsLayout.addView(tv)
        }
    }
}