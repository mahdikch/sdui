package com.yandex.divkit.demo.div.multiSelection

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.yandex.divkit.demo.R

class MultiSelectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val selectionItems = mutableListOf<SelectionItem>()
    private val selectedItems = mutableSetOf<SelectionItem>()
    private var selectionLimit: Int = 0
    private var onSelectionChangedListener: ((selectedIds: String) -> Unit)? = null
    private var isValidationEnabled: Boolean = false
    private var titleView: TextView? = null
    private var requiredStar: TextView? = null
    private val itemViews = mutableListOf<View>()

    data class SelectionItem(
        val title: String,
        val id: String,
        val checkBox: CheckBox
    )

    init {
        orientation = VERTICAL
        setPadding(16, 16, 16, 16)
        setBackgroundResource(R.drawable.multi_selection_background)
    }

    fun setItems(
        options: List<String>,
        ids: List<String>,
        limit: Int = 0
    ) {
        if (options.size != ids.size) {
            throw IllegalArgumentException("Options and IDs lists must have the same size")
        }

        this.selectionLimit = limit
        removeAllViews()
        selectionItems.clear()
        selectedItems.clear()
        itemViews.clear()

        // Add title with red star container
        val titleContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(0, 0, 0, 16)
        }

        titleView = TextView(context).apply {
            text = "انتخاب گزینه‌ها"
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

        // Create selection items
        options.forEachIndexed { index, option ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_multi_selection, this, false)
            
            val checkBox = itemView.findViewById<CheckBox>(R.id.checkbox)
            val titleText = itemView.findViewById<TextView>(R.id.title)
            
            titleText.text = option
            
            val selectionItem = SelectionItem(option, ids[index], checkBox)
            selectionItems.add(selectionItem)
            
            checkBox.setOnClickListener {
                // Prevent click when disabled
                if (!isEnabled) {
                    checkBox.isChecked = !checkBox.isChecked
                    return@setOnClickListener
                }
                handleSelectionChange(selectionItem, checkBox.isChecked)
            }
            
            // Make the whole item clickable
            itemView.setOnClickListener {
                // Prevent click when disabled
                if (!isEnabled) {
                    return@setOnClickListener
                }
                checkBox.isChecked = !checkBox.isChecked
            }
            
            itemViews.add(itemView)
            addView(itemView)
        }

        // Add selection info at the bottom
        if (limit > 0) {
            val infoView = TextView(context).apply {
                text = "حداکثر $limit مورد قابل انتخاب است"
                textSize = 12f
                setPadding(0, 16, 0, 0)
                setTextColor(context.getColor(android.R.color.darker_gray))
            }
            addView(infoView)
        }
    }

    private fun handleSelectionChange(item: SelectionItem, isChecked: Boolean) {
        if (isChecked) {
            // Check selection limit
            if (selectionLimit > 0 && selectedItems.size >= selectionLimit) {
                // Prevent selection if limit reached
                item.checkBox.isChecked = false
                Toast.makeText(
                    context, 
                    "حداکثر $selectionLimit مورد قابل انتخاب است", 
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            selectedItems.add(item)
        } else {
            selectedItems.remove(item)
        }
        
        // Update border based on validation
        if (isValidationEnabled) {
            updateBorder(selectedItems.isEmpty())
        }
        
        // Notify listener with comma-separated IDs
        val selectedIds = selectedItems.joinToString(",") { it.id }
        onSelectionChangedListener?.invoke(selectedIds)
    }

    private fun updateBorder(showError: Boolean) {
        if (showError) {
            setBackgroundResource(R.drawable.multi_selection_background_error)
        } else {
            setBackgroundResource(R.drawable.multi_selection_background)
        }
    }

    fun setOnSelectionChangedListener(listener: (selectedIds: String) -> Unit) {
        this.onSelectionChangedListener = listener
    }

    fun getSelectedIds(): String {
        return selectedItems.joinToString(",") { it.id }
    }

    fun getSelectedTitles(): String {
        return selectedItems.joinToString(",") { it.title }
    }

    fun clearSelection() {
        selectedItems.clear()
        selectionItems.forEach { it.checkBox.isChecked = false }
        onSelectionChangedListener?.invoke("")
    }

    fun setSelectedItems(selectedIds: List<String>) {
        clearSelection()
        selectionItems.forEach { item ->
            if (selectedIds.contains(item.id)) {
                item.checkBox.isChecked = true
                selectedItems.add(item)
            }
        }
        val selectedIdsString = selectedItems.joinToString(",") { it.id }
        onSelectionChangedListener?.invoke(selectedIdsString)
    }

    fun setValidation(enabled: Boolean) {
        isValidationEnabled = enabled
        
        if (enabled) {
            // Show red star
            requiredStar?.visibility = View.VISIBLE
            // Update border based on current selection
            updateBorder(selectedItems.isEmpty())
        } else {
            // Hide red star
            requiredStar?.visibility = View.GONE
            // Reset to default border
            setBackgroundResource(R.drawable.multi_selection_background)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        
        // Enable/disable all checkboxes
        selectionItems.forEach { item ->
            item.checkBox.isEnabled = enabled
        }
        
        // Enable/disable all item views (to prevent clicking)
        itemViews.forEach { itemView ->
            itemView.isEnabled = enabled
            itemView.isClickable = enabled
        }
        
        // Enable/disable title and star
        titleView?.isEnabled = enabled
        requiredStar?.isEnabled = enabled
        
        // Update visual appearance
        alpha = if (enabled) 1.0f else 0.5f
    }
}
