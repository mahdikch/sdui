package com.yandex.divkit.demo.ui.bottomSheetSpinner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.databinding.ItemBottomSheetSpinnerBinding


class AdapterBottomSheetSpinner(private val listener: CustomItemListener) : RecyclerView.Adapter<AdapterBottomSheetSpinner.ViewHolder>() {
    interface CustomItemListener {
        fun onClicked(programs: ListItemDto,varName:String)
    }
    private var items = ArrayList<ListItemDto>()
    private var varName = String()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemBottomSheetSpinnerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding,listener,varName)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = items[position].titleFa
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(items: java.util.ArrayList<ListItemDto>,varName:String) {
        this.items.clear()
        this.items = items
        this.varName = varName
        notifyDataSetChanged()
    }

    class ViewHolder(
        itemBinding : ItemBottomSheetSpinnerBinding,
        private val listener:CustomItemListener,
        private val varName:String

    )
        : RecyclerView.ViewHolder(itemBinding.root),View.OnClickListener{

        val title = itemBinding.title
        private lateinit var item : ListItemDto
        init {
            itemBinding.root.setOnClickListener(this)
        }
        fun bind(item : ListItemDto){
            this.item = item
        }

        override fun onClick(p0: View?) {
            listener.onClicked(item, varName)
        }

    }

}