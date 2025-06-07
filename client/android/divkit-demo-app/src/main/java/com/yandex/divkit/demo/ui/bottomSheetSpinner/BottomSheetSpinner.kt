package com.yandex.divkit.demo.ui.bottomSheetSpinner

//import com.yandex.divkit.demo.data.CustomBottomSheetSpinnerClickListener

import android.app.Dialog
import android.content.Context
import android.os.Bundle
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

        var json: PhPlusDB = viewModelBottomSheetSpinner.getValueByKey(key)
        val turnsType = object : TypeToken<List<ListItemDto>>() {}.type
        var items: List<ListItemDto> = Gson().fromJson<List<ListItemDto>>(json.value, turnsType)
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

            }
        }
        if (varName != null) {
            adapter.addItem(ArrayList(arrayList), varName,titleVar,codeVar)
        }
//            val gridLayoutManager = GridLayoutManager(context, 1)
        val gridLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = gridLayoutManager

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                var fiterList = arrayList!!.filter { it.titleFa.contains(query.toString()) || it.id.contains(query.toString()) }
                if (fiterList.isNotEmpty()) {
                    if (varName != null) {
                        adapter.addItem(ArrayList(fiterList), varName,titleVar,codeVar)
                    }
                }
                else {
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
                var fiterList = arrayList!!.filter { it.titleFa.contains(newText.toString()) ||  it.id.contains(newText.toString()) }
                if (fiterList.isNotEmpty()) {
                    if (varName != null) {
                        adapter.addItem(ArrayList(fiterList), varName,titleVar,codeVar)
                    }
                }
                else {
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