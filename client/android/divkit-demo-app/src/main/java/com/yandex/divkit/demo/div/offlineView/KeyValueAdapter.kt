package com.yandex.divkit.demo.div.offlineView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class KeyValueAdapter(
    private val items: List<KeyValueItem?>
) : RecyclerView.Adapter<KeyValueAdapter.KeyValueViewHolder>() {

    inner class KeyValueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val keyText: TextView = itemView.findViewById(com.yandex.divkit.demo.R.id.keyText)
        val valueText: TextView = itemView.findViewById(com.yandex.divkit.demo.R.id.valueText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyValueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.yandex.divkit.demo.R.layout.row_item_offline_vt_reports, parent, false)
        return KeyValueViewHolder(view)
    }

    override fun onBindViewHolder(holder: KeyValueViewHolder, position: Int) {
        val item = items[position]
        holder.keyText.text = item?.key
        holder.valueText.text = item?.value
    }

    override fun getItemCount(): Int = items.size
}