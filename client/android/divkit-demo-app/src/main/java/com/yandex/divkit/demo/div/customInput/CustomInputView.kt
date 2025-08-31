package com.yandex.divkit.demo.div.customInput

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.yandex.divkit.demo.R

class CustomInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var editText: EditText
    private lateinit var titleText: TextView
    private var onTextChangedListener: ((text: String) -> Unit)? = null

    init {
        orientation = VERTICAL
        setupView()
    }

    private fun setupView() {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.custom_input_layout, this, true)
        
        titleText = view.findViewById(R.id.input_title)
        editText = view.findViewById(R.id.edit_text)
        
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                onTextChangedListener?.invoke(s?.toString() ?: "")
            }
        })
    }

    fun setHint(hint: String) {
        editText.hint = hint
        titleText.text = hint
    }

    fun setInputType(type: String) {
        when (type.lowercase()) {
            "text" -> editText.inputType = InputType.TYPE_CLASS_TEXT
            "number" -> editText.inputType = InputType.TYPE_CLASS_NUMBER
            "phone" -> editText.inputType = InputType.TYPE_CLASS_PHONE
            "email" -> editText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            "password" -> editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            "multiline" -> editText.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            else -> editText.inputType = InputType.TYPE_CLASS_TEXT
        }
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
    }

    fun setMaxLength(maxLength: Int) {
        editText.filters = arrayOf(android.text.InputFilter.LengthFilter(maxLength))
    }
}
