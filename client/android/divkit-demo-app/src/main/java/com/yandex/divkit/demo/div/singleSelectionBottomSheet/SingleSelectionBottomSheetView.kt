package com.yandex.divkit.demo.div.singleSelectionBottomSheet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yandex.divkit.demo.R

class SingleSelectionBottomSheetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val selectionItems = mutableListOf<SelectionItem>()
    private var selectedItem: SelectionItem? = null
    private var onSelectionChangedListener: ((selectedId: String) -> Unit)? = null
    private var isValidationEnabled: Boolean = false
    private var titleView: TextView? = null
    private var selectedTextView: TextView? = null
    private var requiredStar: TextView? = null
    private var isSearchable: Boolean = false
    private var title: String = ""
    private var options: List<String> = emptyList()
    private var ids: List<String> = emptyList()

    data class SelectionItem(
        val title: String,
        val id: String,
        var radioButton: RadioButton? = null,
        var itemView: View? = null
    )

    // Custom dropdown arrow drawable
    private class DropdownArrowDrawable : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#666666")
            style = Paint.Style.FILL
        }
        
        override fun draw(canvas: Canvas) {
            val width = bounds.width().toFloat()
            val height = bounds.height().toFloat()
            
            val path = Path().apply {
                moveTo(width * 0.2f, height * 0.4f)
                lineTo(width * 0.5f, height * 0.7f)
                lineTo(width * 0.8f, height * 0.4f)
                close()
            }
            
            canvas.drawPath(path, paint)
        }
        
        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }
        
        override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
            paint.colorFilter = colorFilter
        }
        
        override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
        
        override fun getIntrinsicWidth(): Int = 24
        override fun getIntrinsicHeight(): Int = 24
    }

    init {
        orientation = VERTICAL
        setPadding(16, 16, 16, 16)
        
        // Add margin to the component
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(24, 8, 24, 8)
        }
        layoutParams = params
        
        setupUI()
    }

    private fun setupUI() {
        // Title with red star container
        val titleContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        titleView = TextView(context).apply {
            text = title
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setPadding(8, 0, 0, 0)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        requiredStar = TextView(context).apply {
            text = " *"
            textSize = 16f
            setTextColor(Color.RED)
            visibility = View.GONE
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        titleContainer.addView(titleView)
        titleContainer.addView(requiredStar)
        addView(titleContainer)

        // Selected item display (clickable to open BottomSheet)
        selectedTextView = TextView(context).apply {
            text = "انتخاب کنید..."
            textSize = 14f
            setPadding(24, 20, 24, 20)
            setTextColor(Color.parseColor("#666666"))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = 12
            }
            
            // Add border and background
            val drawable = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(2, Color.parseColor("#CCCCCC"))
                cornerRadius = 12f
            }
            background = drawable
            
            // Make it look like a dropdown (not editable)
            isClickable = true
            isFocusable = false
            isCursorVisible = false
            setTextIsSelectable(false)
            inputType = 0  // Disable any input
            
            // Add dropdown arrow icon on the right
            val arrowDrawable = DropdownArrowDrawable()
            setCompoundDrawablesWithIntrinsicBounds(null, null, arrowDrawable, null)
            compoundDrawablePadding = 16
            
            setOnClickListener {
                if (isEnabled) {
                    openBottomSheet()
                }
            }
        }
        addView(selectedTextView)
    }

    fun setTitle(newTitle: String) {
        this.title = newTitle
        titleView?.text = newTitle
    }

    fun setItems(
        options: List<String>,
        ids: List<String>
    ) {
        if (options.size != ids.size) {
            throw IllegalArgumentException("Options and IDs lists must have the same size")
        }

        this.options = options
        this.ids = ids
        
        // Clear previous items
        selectionItems.clear()
        selectedItem = null
        
        // Create selection items
        options.forEachIndexed { index, option ->
            val selectionItem = SelectionItem(option, ids[index])
            selectionItems.add(selectionItem)
        }
        
        updateSelectedText()
    }

    private fun openBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(context)
        val bottomSheetView = LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(32, 24, 32, 24)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Title
        val dialogTitle = TextView(context).apply {
            text = title
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 24)
        }
        bottomSheetView.addView(dialogTitle)

        // Search box (if searchable)
        var searchEditText: EditText? = null
        if (isSearchable) {
            searchEditText = EditText(context).apply {
                hint = "جستجو..."
                setPadding(24, 16, 24, 16)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = 16
                }
                
                val drawable = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    setStroke(2, Color.parseColor("#CCCCCC"))
                    cornerRadius = 8f
                }
                background = drawable
            }
            bottomSheetView.addView(searchEditText)
        }

        // Items container
        val itemsContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        // Track temporary selection
        var tempSelectedItem: SelectionItem? = selectedItem
        val itemViews = mutableListOf<View>()
        
        selectionItems.forEach { selectionItem ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_single_selection, itemsContainer, false)
            
            val radioButton = itemView.findViewById<RadioButton>(R.id.radio_button)
            val titleText = itemView.findViewById<TextView>(R.id.title)
            
            titleText.text = selectionItem.title
            radioButton.isChecked = (tempSelectedItem?.id == selectionItem.id)
            radioButton.id = View.generateViewId()
            
            selectionItem.radioButton = radioButton
            selectionItem.itemView = itemView
            
            radioButton.setOnClickListener {
                // Uncheck all others
                selectionItems.forEach { it.radioButton?.isChecked = false }
                radioButton.isChecked = true
                tempSelectedItem = selectionItem
            }
            
            itemView.setOnClickListener {
                // Uncheck all others
                selectionItems.forEach { it.radioButton?.isChecked = false }
                radioButton.isChecked = true
                tempSelectedItem = selectionItem
            }
            
            itemViews.add(itemView)
            itemsContainer.addView(itemView)
        }

        bottomSheetView.addView(itemsContainer)

        // Search functionality
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase().trim()
                itemViews.forEachIndexed { index, view ->
                    val matchesQuery = query.isEmpty() || 
                                     selectionItems[index].title.lowercase().contains(query)
                    view.visibility = if (matchesQuery) View.VISIBLE else View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Buttons container
        val buttonsContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = 24
            }
            gravity = Gravity.END
        }

        // Cancel button
        val cancelButton = Button(context).apply {
            text = "انصراف"
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 16
            }
            setBackgroundColor(Color.parseColor("#CCCCCC"))
            setTextColor(Color.WHITE)
            
            val drawable = GradientDrawable().apply {
                setColor(Color.parseColor("#CCCCCC"))
                cornerRadius = 12f
            }
            background = drawable
            
            setOnClickListener {
                bottomSheetDialog.dismiss()
            }
        }
        buttonsContainer.addView(cancelButton)

        // Save button
        val saveButton = Button(context).apply {
            text = "ذخیره"
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            setBackgroundColor(Color.parseColor("#193d8d"))
            setTextColor(Color.WHITE)
            
            val drawable = GradientDrawable().apply {
                setColor(Color.parseColor("#193d8d"))
                cornerRadius = 12f
            }
            background = drawable
            
            setOnClickListener {
                // Apply selection
                selectedItem = tempSelectedItem
                
                updateSelectedText()
                
                // Update border based on validation
                if (isValidationEnabled) {
                    updateBorder(selectedItem == null)
                }
                
                // Notify listener
                val selectedId = selectedItem?.id ?: ""
                onSelectionChangedListener?.invoke(selectedId)
                
                bottomSheetDialog.dismiss()
            }
        }
        buttonsContainer.addView(saveButton)

        bottomSheetView.addView(buttonsContainer)

        bottomSheetDialog.setContentView(bottomSheetView)
        
        // Make BottomSheet non-cancelable and non-draggable
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
                
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })
            }
        }
        
        bottomSheetDialog.show()
    }

    private fun updateSelectedText() {
        if (selectedItem == null) {
            selectedTextView?.text = "انتخاب کنید..."
            selectedTextView?.setTextColor(Color.parseColor("#666666"))
        } else {
            selectedTextView?.text = selectedItem?.title
            selectedTextView?.setTextColor(Color.parseColor("#000000"))
        }
    }

    private fun updateBorder(showError: Boolean) {
        val drawable = GradientDrawable().apply {
            setColor(Color.WHITE)
            if (showError) {
                setStroke(2, Color.RED)
            } else {
                setStroke(2, Color.parseColor("#CCCCCC"))
            }
            cornerRadius = 12f
        }
        selectedTextView?.background = drawable
    }

    fun setSearchable(searchable: Boolean) {
        this.isSearchable = searchable
    }

    fun setOnSelectionChangedListener(listener: (selectedId: String) -> Unit) {
        this.onSelectionChangedListener = listener
    }

    fun getSelectedId(): String {
        return selectedItem?.id ?: ""
    }

    fun getSelectedTitle(): String {
        return selectedItem?.title ?: ""
    }

    fun clearSelection() {
        selectedItem = null
        updateSelectedText()
        onSelectionChangedListener?.invoke("")
    }

    fun setValidation(enabled: Boolean) {
        isValidationEnabled = enabled
        
        if (enabled) {
            requiredStar?.visibility = View.VISIBLE
            updateBorder(selectedItem == null)
        } else {
            requiredStar?.visibility = View.GONE
            updateBorder(false)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        selectedTextView?.isEnabled = enabled
        selectedTextView?.isClickable = enabled
        titleView?.isEnabled = enabled
        requiredStar?.isEnabled = enabled
        alpha = if (enabled) 1.0f else 0.5f
    }
}

