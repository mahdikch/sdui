package com.yandex.divkit.demo.ui.bottomSheetSpinner

//import com.yandex.divkit.demo.data.CustomBottomSheetSpinnerClickListener

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.div.core.DivViewFacade
import com.yandex.divkit.demo.data.SharePref
import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.data.local.PhPlusDBDao
import com.yandex.divkit.demo.data.remote.PhPlusApi
import com.yandex.divkit.demo.data.remote.PhPlusRemoteDataSource
import com.yandex.divkit.demo.data.repository.PhPlusRepository
import com.yandex.divkit.demo.databinding.BottomSheetSpinnerBinding
import com.yandex.divkit.demo.screenshot.DivAssetReader
import com.yandex.divkit.demo.utils.SingletonObjects

//import dagger.hilt.android.AndroidEntryPoint


//@AndroidEntryPoint
class BottomSheetSpinner(
    private val context: Context,
    private val listener: AdapterBottomSheetSpinner.CustomItemListener,
    private val key: String,
    private val title: String,
    private val codeVar: String,
    private val titleVar: String,
    private val view: DivViewFacade,
    private val map: MutableMap<String, String> = HashMap()

) :
    BottomSheetDialogFragment() {
    lateinit var dialog: BottomSheetDialog
    lateinit var rootview: View
    private var _binding: BottomSheetSpinnerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdapterBottomSheetSpinner
    private lateinit var viewModelBottomSheetSpinner: ViewModelBottomSheetSpinner
    private lateinit var repository: PhPlusRepository
    private lateinit var remoteDataSource: PhPlusRemoteDataSource
    private lateinit var sharePref: SharePref
    private lateinit var phPlusDBDao: PhPlusDBDao
    private lateinit var assetReader: DivAssetReader


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val metrics = requireContext().resources.displayMetrics
        val height = (metrics.heightPixels) * 60 / 100
        val width = metrics.heightPixels
//        dialog.behavior.peekHeight = height * 60 / 100
        dialog.window?.setLayout(width, height)

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = BottomSheetSpinnerBinding.inflate(inflater, container, false)
        val retrofit = SingletonObjects.retrofitInstance()
        val create = retrofit?.create(PhPlusApi::class.java)
        val database = SingletonObjects.getDbInstance(context)
        val sharedPreferences = SingletonObjects.sharedPreferencesInstance(context)
        if (sharedPreferences != null)
            sharePref = SharePref(sharedPreferences)
        if (create != null)
            remoteDataSource = PhPlusRemoteDataSource(create)
        if (database != null) {
            repository = PhPlusRepository(remoteDataSource, sharePref, database.phPlusDBDao())
        }
        val factory = MyViewModelFactory(repository)
        viewModelBottomSheetSpinner =
            ViewModelProvider(this, factory)[ViewModelBottomSheetSpinner::class.java]
//        assetReader = container?.let { DivAssetReader(it.context) }!!

        setUp()
//        var json: PhPlusDB = viewModelBottomSheetSpinner.getValueByKey("ph/testbottomsheet")
//        val div = UIDiv2ViewCreator(context, this, viewModelBottomSheetSpinner).createDiv2ViewMehdi(
//            context,
//            JSONObject(json),
//            binding.root,
//            ScenarioLogDelegate.Stub
//        )
//        div.layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.MATCH_PARENT
//        ).apply {
//            weight = 1F
//        }

        return binding.root
    }

    private fun setUp() {


        binding.title.text = title
        adapter = AdapterBottomSheetSpinner(listener)
//        viewModel.getBasicInfoItems(parentId).observe(viewLifecycleOwner) {
//        var divMotorJson:PhPlusDB = viewModelBottomSheetSpinner.getValueByKey("patchMotorPlate")
//        var divVehicleJson:PhPlusDB = viewModelBottomSheetSpinner.getValueByKey("patchVehiclePlate")
        var varName = map["varName"]
        var path = map["path"]
        var sysName = map["sysName"]
        val selectionMode = map["selection"] ?: "single"
        val isMulti = selectionMode.equals("multi", ignoreCase = true)
        Log.d(
            "BottomSheetSpinner",
            "setUp: selectionMode=$selectionMode isMulti=$isMulti key=$key title=$title codeVar=$codeVar titleVar=$titleVar"
        )
        adapter.setMultiSelect(isMulti)

        var json: PhPlusDB = viewModelBottomSheetSpinner.getValueByKey(key)
        Log.d("BottomSheetSpinner", "DB getValueByKey('$key'): valueLength=${json.value?.length}")
        val turnsType = object : TypeToken<List<ListItemDto>>() {}.type
        var items: List<ListItemDto> = Gson().fromJson<List<ListItemDto>>(json.value, turnsType)
        Log.d("BottomSheetSpinner", "Parsed items size=${items.size}")
        var arrayList: ArrayList<ListItemDto> = arrayListOf<ListItemDto>()
        arrayList.addAll(items)
        for (item in items) {
            for (constraint in item.constraintList) {
                if (constraint.variableName == "usage.res") {
                    if (map.contains("usage")) {
                        if (constraint.disallowedList.contains(map["usage"])) {
                            arrayList.remove(item)
                        }
                    }
                }
                if (constraint.variableName == "delivery.res") {
                    if (map.contains("delivery")) {
                        if (constraint.disallowedList.contains(map["delivery"])) {
                            arrayList.remove(item)
                        }
                    }
                }
                if (constraint.variableName == "plate.res") {
                    if (map.contains("plate")) {
                        if (constraint.disallowedList.contains(map["plate"])) {
                            arrayList.remove(item)
                        }
                    }
                }

                if (constraint.variableName == "violation.res") {
                    if (map.contains("violation1")) {
                        if (constraint.disallowedList.contains(map["violation1"])) {
                            arrayList.remove(item)
                        }
                    }
                    if (map.contains("violation2")) {
                        if (constraint.disallowedList.contains(map["violation2"])) {
                            arrayList.remove(item)
                        }
                    }
                    if (map.contains("violation3")) {
                        if (constraint.disallowedList.contains(map["violation3"])) {
                            arrayList.remove(item)
                        }
                    }
//                    if (map.contains("delivery")) {
//                        if (constraint.disallowedList.contains(map["violation3"])) {
//                            arrayList.remove(item)
//                        }
//                    }

                }
                if (constraint.variableName == "system.res") {
                    if (map.contains("system")) {
                        if (constraint.disallowedList.contains(map.get("system"))) {
                            arrayList.remove(item)
                        }
                    }

                }
                if (constraint.variableName == "violations.res") {
                    if (map.contains("violations")) {
                        if (constraint.disallowedList.contains(map.get("violations"))) {
                            arrayList.remove(item)
                        }
                    }

                }
                if (constraint.variableName == "city.res") {
                    if (map.contains("city")) {
                        if (constraint.disallowedList.contains(map.get("city"))) {
                            arrayList.remove(item)
                        }
                    }

                }
                if (constraint.variableName == "province.res") {
                    if (map.contains("province")) {
                        if (constraint.disallowedList.contains(map.get("province"))) {
                            arrayList.remove(item)
                        }
                    }

                }
                if (constraint.variableName == "category.res") {
                    if (map.contains("category")) {
                        if (constraint.disallowedList.contains(map.get("category"))) {
                            arrayList.remove(item)
                        }
                    }

                }

            }
        }
        Log.d("BottomSheetSpinner", "After filtering: size=${arrayList.size} filters=${map}")
        if (varName != null) {
            Log.d(
                "BottomSheetSpinner",
                "Adapter addItem with varName=$varName, titleVar=$titleVar, codeVar=$codeVar"
            )
            adapter.addItem(ArrayList(arrayList), varName, titleVar, codeVar, sysName, path)
        }

        if (isMulti) {
            // Preselect using selected_codes query param (comma-separated)
            val selectedCodesCsv = map["selected_codes"]
            val preselected =
                selectedCodesCsv?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
                    ?: emptyList()
            Log.d(
                "BottomSheetSpinner",
                "Preselect ids=$preselected from selected_codes='$selectedCodesCsv'"
            )
            adapter.setPreselected(preselected)

            // Show multi-select action bar (Done only per spec)
            binding.actions.visibility = View.VISIBLE
            binding.btnDone.visibility = View.VISIBLE
//            binding.btnCancel.visibility = View.GONE
//            binding.btnClear.visibility = View.GONE

            binding.btnDone.setOnClickListener {
                val selected = adapter.getSelectedItems()
                Log.d("BottomSheetSpinner", "Done clicked, selected count=${selected.size}")
                val codes = selected.joinToString(separator = ",") { it.id }
                val titles = selected.joinToString(separator = ",") { it.titleFa }
                // per spec: if zero selected, set empty strings
                val div2View = view as? com.yandex.div.core.view2.Div2View
                div2View?.setVariable(codeVar, codes)
                div2View?.setVariable(titleVar, titles)
                Log.d("BottomSheetSpinner", "Set variables: $codeVar='$codes' $titleVar='$titles'")
                dismiss()
            }
            // Cancel/Clear kept hidden
        } else {


            ;
            // Single select: hide actions
            binding.actions.visibility = View.GONE
        }
//            val gridLayoutManager = GridLayoutManager(context, 1)
        val gridLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = gridLayoutManager
        binding.recyclerView.adapter = adapter
        binding.recyclerView.hasFixedSize()

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                var fiterList = arrayList!!.filter {
                    it.titleFa.contains(query.toString()) || it.id.contains(query.toString())
                }
                if (fiterList.isNotEmpty()) {
                    if (varName != null) {
                        adapter.addItem(ArrayList(fiterList), varName, titleVar, codeVar)
                    }
                } else {
                    Toast.makeText(context, "No Match Found", Toast.LENGTH_SHORT).show()
                }
//                if (arrayList.contains(query)) {
//                    arrayAdapter.filter.filter(query)
//                } else {
//                    Toast.makeText(this@MainActivity, "No Match Found", Toast.LENGTH_SHORT).show()
//                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                var fiterList = arrayList!!.filter {
                    it.titleFa.contains(newText.toString()) || it.id.contains(newText.toString())
                }
                if (fiterList.isNotEmpty()) {
                    if (varName != null) {
                        adapter.addItem(ArrayList(fiterList), varName, titleVar, codeVar)
                    }
                } else {
                    Toast.makeText(context, "No Match Found", Toast.LENGTH_SHORT).show()
                }
                return false
            }

        })
    }


}


class MyViewModelFactory(private val repository: PhPlusRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewModelBottomSheetSpinner::class.java)) {
            return ViewModelBottomSheetSpinner(repository) as T
        }
        throw Exception("asd")
    }
}