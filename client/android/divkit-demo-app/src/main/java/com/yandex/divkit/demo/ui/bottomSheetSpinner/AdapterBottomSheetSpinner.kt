package com.yandex.divkit.demo.ui.bottomSheetSpinner

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.databinding.ItemBottomSheetSpinnerBinding


class AdapterBottomSheetSpinner(private val listener: CustomItemListener) : RecyclerView.Adapter<AdapterBottomSheetSpinner.ViewHolder>() {
    interface CustomItemListener {
        fun onClicked(programs: ListItemDto,varName:String,titleVar:String,codeVar:String)
    }
    private var items = ArrayList<ListItemDto>()
    private var varName = String()
    private var titleVar = String()
    private var codeVar = String()
    private var multiSelect: Boolean = false
    private val selectedIds: MutableSet<String> = linkedSetOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBottomSheetSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, listener, varName, titleVar, codeVar, ::onItemToggle, ::isSelected, multiSelect)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        Log.d("AdapterBottomSheetSpinner", "bind pos=$position id=${item.id} title=${item.titleFa} selected=${isSelected(item.id)} multi=$multiSelect")
        holder.bind(item, isSelected(item.id))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(items: java.util.ArrayList<ListItemDto>, varName: String, titleVar: String, codeVar: String) {
        this.items.clear()
        this.items = items
        this.varName = varName
        this.titleVar = titleVar
        this.codeVar = codeVar
        Log.d("AdapterBottomSheetSpinner", "addItem size=${items.size} varName=$varName titleVar=$titleVar codeVar=$codeVar")
        notifyDataSetChanged()
    }

    fun setMultiSelect(enabled: Boolean) {
        this.multiSelect = enabled
        Log.d("AdapterBottomSheetSpinner", "setMultiSelect enabled=$enabled")
        notifyDataSetChanged()
    }

    fun setPreselected(ids: Collection<String>) {
        selectedIds.clear()
        selectedIds.addAll(ids)
        Log.d("AdapterBottomSheetSpinner", "setPreselected ids=$ids")
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<ListItemDto> {
        if (!multiSelect) {
            Log.d("AdapterBottomSheetSpinner", "getSelectedItems called in single mode -> []")
            return emptyList()
        }
        val selected = items.filter { selectedIds.contains(it.id) }
        Log.d("AdapterBottomSheetSpinner", "getSelectedItems selectedCount=${selected.size} selectedIds=$selectedIds")
        return selected
    }

    private fun onItemToggle(item: ListItemDto) {
        if (!multiSelect) return
        if (selectedIds.contains(item.id)) {
            selectedIds.remove(item.id)
            Log.d("AdapterBottomSheetSpinner", "toggle remove id=${item.id}")
        } else {
            selectedIds.add(item.id)
            Log.d("AdapterBottomSheetSpinner", "toggle add id=${item.id}")
        }
        notifyDataSetChanged()
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
        private val multiSelect: Boolean
    ) : RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

//        val title = itemBinding.title
        private lateinit var item: ListItemDto

        init {
            itemBinding.root.setOnClickListener(this)
            itemBinding.checkbox.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
                if (this::item.isInitialized) {
                    Log.d("AdapterBottomSheetSpinner", "checkbox click id=${item.id} checked=$checked")
                    toggle(item)
                }
            }
        }

        fun bind(item: ListItemDto, selected: Boolean) {
            this.item = item

            itemBinding.title.text = item.titleFa
            if (multiSelect) {
                itemBinding.checkbox.visibility = View.VISIBLE
                // avoid triggering listener when programmatically updating
                itemBinding.checkbox.setOnCheckedChangeListener(null)
                itemBinding.checkbox.isChecked = selected
                itemBinding.checkbox.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
                    Log.d("AdapterBottomSheetSpinner", "checkbox change id=${item.id} checked=$checked")
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
                listener.onClicked(item, varName, titleVar, codeVar)
            }
        }
    }

}