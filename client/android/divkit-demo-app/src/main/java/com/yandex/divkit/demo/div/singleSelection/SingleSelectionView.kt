package com.yandex.divkit.demo.div.singleSelection

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.yandex.divkit.demo.R

class SingleSelectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val selectionItems = mutableListOf<SelectionItem>()
    private var selectedItem: SelectionItem? = null
    private var onSelectionChangedListener: ((selectedId: String) -> Unit)? = null
    private var isValidationEnabled: Boolean = false
    private var titleView: TextView? = null
    private var requiredStar: TextView? = null
    private var radioGroup: RadioGroup? = null

    data class SelectionItem(
        val title: String,
        val id: String,
        val radioButton: RadioButton
    )

    init {
        orientation = VERTICAL
        setPadding(16, 16, 16, 16)
        setBackgroundResource(R.drawable.multi_selection_background)
    }

    fun setItems(
        options: List<String>,
        ids: List<String>
    ) {
        if (options.size != ids.size) {
            throw IllegalArgumentException("Options and IDs lists must have the same size")
        }

        removeAllViews()
        selectionItems.clear()
        selectedItem = null

        // Add title with red star container
        val titleContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(0, 0, 0, 16)
        }

        titleView = TextView(context).apply {
            text = "انتخاب گزینه"
            textSize = 16f
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }
        
        requiredStar = TextView(context).apply {
            text = "*"
            textSize = 18f
            setTextColor(android.graphics.Color.RED)
            setTypeface(null, Typeface.BOLD)
            visibility = View.GONE
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        titleContainer.addView(titleView)
        titleContainer.addView(requiredStar)
        addView(titleContainer)

        // Create RadioGroup for single selection
        radioGroup = RadioGroup(context).apply {
            orientation = RadioGroup.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        // Create selection items
        options.forEachIndexed { index, option ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_single_selection, null, false)
            
            val radioButton = itemView.findViewById<RadioButton>(R.id.radio_button)
            val titleText = itemView.findViewById<TextView>(R.id.title)
            
            titleText.text = option
            
            val selectionItem = SelectionItem(option, ids[index], radioButton)
            selectionItems.add(selectionItem)
            
            // Set unique ID for radio button
            radioButton.id = View.generateViewId()
            
            radioButton.setOnClickListener {
                handleSelectionChange(selectionItem)
            }
            
            // Make the whole item clickable
            itemView.setOnClickListener {
                radioButton.isChecked = true
                handleSelectionChange(selectionItem)
            }
            
            radioGroup?.addView(itemView)
        }

        addView(radioGroup)
    }

    private fun handleSelectionChange(item: SelectionItem) {
        // Uncheck all other radio buttons
        selectionItems.forEach { it.radioButton.isChecked = false }
        
        // Check the selected one
        item.radioButton.isChecked = true
        selectedItem = item
        
        // Update border based on validation
        if (isValidationEnabled) {
            updateBorder(false) // Something is selected, so no error
        }
        
        // Notify listener with selected ID
        onSelectionChangedListener?.invoke(item.id)
    }

    private fun updateBorder(showError: Boolean) {
        if (showError) {
            setBackgroundResource(R.drawable.multi_selection_background_error)
        } else {
            setBackgroundResource(R.drawable.multi_selection_background)
        }
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
        selectionItems.forEach { it.radioButton.isChecked = false }
        
        // Update border based on validation
        if (isValidationEnabled) {
            updateBorder(true) // Nothing selected, show error
        }
        
        onSelectionChangedListener?.invoke("")
    }

    fun setSelectedItem(selectedId: String) {
        clearSelection()
        selectionItems.forEach { item ->
            if (item.id == selectedId) {
                item.radioButton.isChecked = true
                selectedItem = item
                
                // Update border based on validation
                if (isValidationEnabled) {
                    updateBorder(false) // Something selected, no error
                }
            }
        }
        val selectedIdString = selectedItem?.id ?: ""
        onSelectionChangedListener?.invoke(selectedIdString)
    }

    fun setValidation(enabled: Boolean) {
        isValidationEnabled = enabled
        
        if (enabled) {
            // Show red star
            requiredStar?.visibility = View.VISIBLE
            // Update border based on current selection
            updateBorder(selectedItem == null)
        } else {
            // Hide red star
            requiredStar?.visibility = View.GONE
            // Reset to default border
            setBackgroundResource(R.drawable.multi_selection_background)
        }
    }
}

