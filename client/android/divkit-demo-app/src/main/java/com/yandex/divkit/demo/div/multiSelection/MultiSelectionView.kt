package com.yandex.divkit.demo.div.multiSelection

import android.content.Context
import android.util.AttributeSet
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

    data class SelectionItem(
        val title: String,
        val id: String,
        val checkBox: CheckBox
    )

    init {
        orientation = VERTICAL
        setPadding(16, 16, 16, 16)
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

        // Add title if needed
        val titleView = TextView(context).apply {
            text = "انتخاب گزینه‌ها"
            textSize = 16f
            setPadding(0, 0, 0, 16)
        }
        addView(titleView)

        // Create selection items
        options.forEachIndexed { index, option ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_multi_selection, this, false)
            
            val checkBox = itemView.findViewById<CheckBox>(R.id.checkbox)
            val titleText = itemView.findViewById<TextView>(R.id.title)
            
            titleText.text = option
            
            val selectionItem = SelectionItem(option, ids[index], checkBox)
            selectionItems.add(selectionItem)
            
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                handleSelectionChange(selectionItem, isChecked)
            }
            
            // Make the whole item clickable
            itemView.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
            
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
        
        // Notify listener with comma-separated IDs
        val selectedIds = selectedItems.joinToString(",") { it.id }
        onSelectionChangedListener?.invoke(selectedIds)
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
}
