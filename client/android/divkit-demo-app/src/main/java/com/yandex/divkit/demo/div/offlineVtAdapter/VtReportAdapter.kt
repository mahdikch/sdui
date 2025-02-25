package com.yandex.divkit.demo.div.offlineVtAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.divkit.demo.databinding.RowOfflineVtReportsBinding
import java.util.ArrayList

class VtReportAdapter(private val listener: VtReportItemListener) :
    RecyclerView.Adapter<VtReportAdapter.ProgramsViewHolder>() {

    interface VtReportItemListener {
        fun onClickedVtReport(data: String)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: ArrayList<String>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramsViewHolder {
        val binding: RowOfflineVtReportsBinding =
            RowOfflineVtReportsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProgramsViewHolder(binding,listener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ProgramsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    private val items = ArrayList<String>()

    class ProgramsViewHolder(
        private val itemBinding: RowOfflineVtReportsBinding,
        private val listener: VtReportAdapter.VtReportItemListener
    ) : RecyclerView.ViewHolder(itemBinding.root),
        View.OnClickListener {

        private lateinit var program: String

        init {
            itemBinding.send.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: String) {
            this.program = item
            val restoredMap: MutableMap<String, String> = Gson().fromJson(item, object : TypeToken<MutableMap<String, String>>() {}.type)
            itemBinding.model.text= restoredMap["variable_system_title"] +"-"+ restoredMap["variable_color_title"]
            itemBinding.time.text=restoredMap["timePda"]
            itemBinding.location.text=restoredMap["address"]
            itemBinding.violation.text=restoredMap["violationType1_title"] +"-"+ restoredMap["violationType2_title"]+"-"+restoredMap["violationType3_title"]


        }

        override fun onClick(v: View?) {
            listener.onClickedVtReport(program)
        }
    }


}