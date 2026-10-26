package com.yandex.divkit.demo.ui.bottomSheetSpinner

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.databinding.ItemBottomSheetSpinnerBinding


class AdapterBottomSheetSpinner(private val listener: CustomItemListener) :
    RecyclerView.Adapter<AdapterBottomSheetSpinner.ViewHolder>() {
    interface CustomItemListener {
        fun onClicked(programs: ListItemDto, varName: String, titleVar: String, codeVar: String, sysName: String, path: String, allParams: Map<String, String>)
    }

    private var items = ArrayList<ListItemDto>()
    private var varName = String()
    private var titleVar = String()
    private var codeVar = String()
    private var sysName = String()
    private var path = ""
    private var multiSelect: Boolean = false
    private val selectedIds: MutableSet<String> = linkedSetOf()
    private var allParams: Map<String, String> = emptyMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBottomSheetSpinnerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(
            binding,
            listener,
            varName,
            titleVar,
            codeVar,
            ::onItemToggle,
            ::isSelected,
            multiSelect,
            sysName,
            path,
            allParams
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        Log.d(
            "AdapterBottomSheetSpinner",
            "bind pos=$position id=${item.id} title=${item.titleFa} selected=${isSelected(item.id)} multi=$multiSelect"
        )
        holder.bind(item, isSelected(item.id))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(
        items: java.util.ArrayList<ListItemDto>,
        varName: String,
        titleVar: String,
        codeVar: String,
        sysName: String? = "",
        path: String? = "",
        allParams: Map<String, String> = emptyMap()
    ) {
        this.items.clear()
        this.items = items
        this.varName = varName
        this.titleVar = titleVar
        this.codeVar = codeVar
        if (sysName != null) {
            this.sysName = sysName
        }
        if (path != null) {
            this.path = path
        }
        this.allParams = allParams
        Log.d(
            "AdapterBottomSheetSpinner",
            "addItem size=${items.size} varName=$varName titleVar=$titleVar codeVar=$codeVar allParams=$allParams"
        )
        notifyDataSetChanged()
    }

    fun appendItems(newItems: List<ListItemDto>) {
        if (newItems.isEmpty()) return
        val existing = items.map { it.id }.toHashSet()
        val toAdd = newItems.filter { it.id.isNotBlank() && !existing.contains(it.id) }
        if (toAdd.isEmpty()) return
        val start = items.size
        items.addAll(toAdd)
        notifyItemRangeInserted(start, toAdd.size)
    }

    fun currentItems(): List<ListItemDto> = items.toList()

    fun setMultiSelect(enabled: Boolean) {
        this.multiSelect = enabled
        Log.d("AdapterBottomSheetSpinner", "setMultiSelect enabled=$enabled")
        notifyDataSetChanged()
    }

    fun setPreselected(ids: Collection<String>) {
        selectedIds.clear()
        selectedIds.addAll(ids)
        notifyDataSetChanged()  // اینجا ایرادی نداره چون فقط موقع init صدا زده میشه
    }

    fun getSelectedItems(): List<ListItemDto> {
        if (!multiSelect) {
            Log.d("AdapterBottomSheetSpinner", "getSelectedItems called in single mode -> []")
            return emptyList()
        }
        val selected = items.filter { selectedIds.contains(it.id) }
        Log.d(
            "AdapterBottomSheetSpinner",
            "getSelectedItems selectedCount=${selected.size} selectedIds=$selectedIds"
        )
        return selected
    }

    private fun onItemToggle(item: ListItemDto) {
        if (!multiSelect) return
        if (selectedIds.contains(item.id)) {
            selectedIds.remove(item.id)
        } else {
            selectedIds.add(item.id)
        }
        val position = items.indexOfFirst { it.id == item.id }
        if (position != -1) {
            notifyItemChanged(position)  // فقط همون آیتم رو آپدیت کن
        }
    }

    private fun isSelected(id: String): Boolean = selectedIds.contains(id)

    class ViewHolder(
        private val itemBinding: ItemBottomSheetSpinnerBinding,
        private val listener: CustomItemListener,
        private val varName: String,
        private val titleVar: String,
        private val codeVar: String,
        private val toggle: (ListItemDto) -> Unit,
        private val isSelected: (String) -> Boolean,
        private val multiSelect: Boolean,
        private val sysName: String,
        private val path: String,
        private val allParams: Map<String, String>
    ) : RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        //        val title = itemBinding.title
        private lateinit var item: ListItemDto

        init {
            itemBinding.itemCard.setOnClickListener(this)
            itemBinding.root.setOnClickListener(this)
            itemBinding.checkbox.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
                if (this::item.isInitialized) {
                    Log.d(
                        "AdapterBottomSheetSpinner",
                        "checkbox click id=${item.id} checked=$checked"
                    )
                    toggle(item)
                }
            }
        }

        fun bind(item: ListItemDto, selected: Boolean) {
            this.item = item

            itemBinding.title.text = item.titleFa
            itemBinding.itemCard.isSelected = selected
            if (multiSelect) {
                itemBinding.checkbox.visibility = View.VISIBLE
                // avoid triggering listener when programmatically updating
                itemBinding.checkbox.setOnCheckedChangeListener(null)
                itemBinding.checkbox.isChecked = selected
                itemBinding.checkbox.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
                    Log.d(
                        "AdapterBottomSheetSpinner",
                        "checkbox change id=${item.id} checked=$checked"
                    )
                    toggle(item)
                }
            } else {
                itemBinding.checkbox.visibility = View.GONE
            }
        }

        override fun onClick(p0: View?) {
            if (multiSelect) {
                Log.d("AdapterBottomSheetSpinner", "row click toggle id=${item.id}")
                toggle(item)
            } else {
                listener.onClicked(item, varName, titleVar, codeVar, sysName, path, allParams)
            }
        }
    }

}