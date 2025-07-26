package com.yandex.divkit.demo.div.offlineView

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class OfflineViewListAdapter@JvmOverloads constructor(
    private val system:String = "",
    private val listener: OfflineViewListItemListener
) :
    ListAdapter<Map<String, String>, OfflineViewListAdapter.EntryViewHolder>(DiffCallback()) {
    interface OfflineViewListItemListener {
        fun onClickedOfflineView(data: String,next: String)
    }
    var onSendClick: ((position: Int, item: Map<String, String>) -> Unit)? = null

    inner class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(com.yandex.divkit.demo.R.id.titleText)
        val dateText: TextView = itemView.findViewById(com.yandex.divkit.demo.R.id.dateText)
        val recycler: RecyclerView =
            itemView.findViewById(com.yandex.divkit.demo.R.id.keyValueRecycler)
        val sendButton: Button = itemView.findViewById(com.yandex.divkit.demo.R.id.sendButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.yandex.divkit.demo.R.layout.row_offline_vt_reports, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val item = getItem(position)
        val restoredMap: MutableMap<String, String> = Gson().fromJson(
            item["data"],
            object : TypeToken<MutableMap<String, String>>() {}.type
        )
        holder.titleText.text = restoredMap["title"] ?: "بدون عنوان"
        holder.dateText.text = restoredMap["offline_register_date"] ?: "بدون تاریخ"

        val keyValueList = restoredMap.filterKeys {
//            it != "title" && it != "offline_register_date"  && it != "car_picture"&&
            it.contains("dbshow")
        }
            .map {
                var key = it.key.replace("_dbshow", "")
                restoredMap[key+"_persian_lan"]?.let { it1 -> KeyValueItem(it1, it.value) }
            }

        holder.recycler.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.recycler.adapter = KeyValueAdapter(keyValueList)

        holder.sendButton.setOnClickListener {

            item["data"]?.let { it1 -> restoredMap["next"]?.let { it2 -> listener.onClickedOfflineView(it1, next = it2) } }
//            onSendClick?.invoke(position, item)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Map<String, String>>() {
        override fun areItemsTheSame(
            oldItem: Map<String, String>,
            newItem: Map<String, String>
        ): Boolean {
            return oldItem["title"] == newItem["title"] && oldItem["date"] == newItem["date"]
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: Map<String, String>,
            newItem: Map<String, String>
        ): Boolean {
            return oldItem == newItem
        }
    }
}
