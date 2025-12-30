package com.yandex.divkit.demo.div.customInput

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.yandex.divkit.demo.R

class CustomInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var editText: EditText
    private lateinit var titleText: TextView
    private lateinit var requiredStar: TextView
    private var onTextChangedListener: ((text: String) -> Unit)? = null
    private var isValidationEnabled: Boolean = false
    private var minValue: Double? = null
    private var maxValue: Double? = null
    private var isNumericInput: Boolean = false

    init {
        orientation = VERTICAL
        setupView()
    }

    private fun setupView() {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.custom_input_layout, this, true)
        
        titleText = view.findViewById(R.id.input_title)
        editText = view.findViewById(R.id.edit_text)
        requiredStar = view.findViewById(R.id.required_star)
        
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                
                // Handle numeric min/max validation
                if (isNumericInput && text.isNotEmpty()) {
                    val numericValue = text.toDoubleOrNull()
                    if (numericValue != null) {
                        val minVal = minValue
                        val maxVal = maxValue
                        
                        val clampedValue: Double = when {
                            minVal != null && numericValue < minVal -> minVal
                            maxVal != null && numericValue > maxVal -> maxVal
                            else -> numericValue
                        }
                        
                        val needsUpdate = clampedValue != numericValue
                        
                        if (needsUpdate) {
                            // Format the clamped value (remove decimal if it's a whole number)
                            val formattedValue = if (clampedValue == clampedValue.toLong().toDouble()) {
                                clampedValue.toLong().toString()
                            } else {
                                clampedValue.toString()
                            }
                            editText.removeTextChangedListener(this)
                            editText.setText(formattedValue)
                            editText.setSelection(formattedValue.length)
                            editText.addTextChangedListener(this)
                            onTextChangedListener?.invoke(formattedValue)
                            return
                        }
                    }
                }
                
                onTextChangedListener?.invoke(text)
                
                // Update border based on validation
                if (isValidationEnabled) {
                    updateBorder(text.isEmpty())
                }
            }
        })
    }

    private fun updateBorder(showError: Boolean) {
        if (showError) {
            editText.setBackgroundResource(R.drawable.input_background_error)
        } else {
            editText.setBackgroundResource(R.drawable.input_background)
        }
    }

    fun setHint(hint: String) {
        editText.hint = hint
        titleText.text = hint
        // Show title when hint is set
        if (hint.isNotEmpty()) {
            titleText.visibility = View.VISIBLE
        }
    }

    fun setInputType(type: String) {
        when (type.lowercase()) {
            "text" -> {
                editText.inputType = InputType.TYPE_CLASS_TEXT
                isNumericInput = false
            }
            "number" -> {
                editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
                isNumericInput = true
            }
            "phone" -> {
                editText.inputType = InputType.TYPE_CLASS_PHONE
                isNumericInput = false
            }
            "email" -> {
                editText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                isNumericInput = false
            }
            "password" -> {
                editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                isNumericInput = false
            }
            "multiline" -> {
                editText.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                isNumericInput = false
            }
            else -> {
                editText.inputType = InputType.TYPE_CLASS_TEXT
                isNumericInput = false
            }
        }
    }

    fun setMinValue(min: Double?) {
        this.minValue = min
    }

    fun setMaxValue(max: Double?) {
        this.maxValue = max
    }

    fun setMinMaxValues(min: Double?, max: Double?) {
        this.minValue = min
        this.maxValue = max
    }

    fun setText(text: String) {
        editText.setText(text)
    }

    fun getText(): String {
        return editText.text.toString()
    }

    fun setOnTextChangedListener(listener: (text: String) -> Unit) {
        this.onTextChangedListener = listener
    }

    fun clearText() {
        editText.setText("")
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        editText.isEnabled = enabled
        titleText.isEnabled = enabled
        requiredStar.isEnabled = enabled
        
        // Update visual appearance
        alpha = if (enabled) 1.0f else 0.5f
    }

    fun setMaxLength(maxLength: Int) {
        editText.filters = arrayOf(android.text.InputFilter.LengthFilter(maxLength))
    }

    fun setValidation(enabled: Boolean) {
        isValidationEnabled = enabled
        
        if (enabled) {
            // Show red star
            requiredStar.visibility = View.VISIBLE
            // Show title if it has text
            if (titleText.text.isNotEmpty()) {
                titleText.visibility = View.VISIBLE
            }
            // Update border based on current text
            updateBorder(editText.text.toString().isEmpty())
        } else {
            // Hide red star
            requiredStar.visibility = View.GONE
            // Reset to default border
            editText.setBackgroundResource(R.drawable.input_background)
        }
    }
}
