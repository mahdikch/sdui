package com.yandex.divkit.demo.ui.bottomSheetDiv

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.yandex.div.core.view2.Div2View
import com.yandex.divkit.demo.data.SharePref
import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.data.remote.PhPlusApi
import com.yandex.divkit.demo.data.remote.PhPlusRemoteDataSource
import com.yandex.divkit.demo.data.repository.PhPlusRepository
import com.yandex.divkit.demo.databinding.BottomSheetDivBinding
import com.yandex.divkit.demo.databinding.BottomSheetPlateBinding
import com.yandex.divkit.demo.databinding.BottomSheetSpinnerBinding
import com.yandex.divkit.demo.div.asDivPatchWithTemplates
import com.yandex.divkit.demo.ui.UIDiv2ViewCreator
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import com.yandex.divkit.demo.ui.bottomSheetSpinner.MyViewModelFactory
import com.yandex.divkit.demo.ui.bottomSheetSpinner.ViewModelBottomSheetSpinner
import com.yandex.divkit.demo.utils.SingletonObjects
import com.yandex.divkit.regression.ScenarioLogDelegate
//import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject


//@AndroidEntryPoint
class BottomSheetDiv(
    private val context: Context,
    private val activity: Activity,
    private val jsonName: String,
    private val mehdiViewModel: MehdiViewModel,
    private val lo: LifecycleOwner,
    private val bottomSheetManager: BottomSheetManager? = null
    ) :
    BottomSheetDialogFragment() {
    lateinit var dialog: BottomSheetDialog
    lateinit var rootview: View
    private var _binding: BottomSheetDivBinding? = null
    private val binding get() = _binding!!
    private lateinit var div: Div2View

    //    private lateinit var viewModelBottomSheetDiv: ViewModelBottomSheetDiv
    private lateinit var repository: PhPlusRepository
    private lateinit var remoteDataSource: PhPlusRemoteDataSource
    private lateinit var sharePref: SharePref
    
    // Unique ID assigned by BottomSheetManager
    var assignedId: String? = null
    var fullScreen: Boolean = false
    var initialVariables: Map<String, String> = emptyMap()
    /** Optional JSON already resolved from the service response (avoids DB round-trip / EmptyResultSetException). */
    var jsonContent: String? = null
    /** JSON from the service response; preferred over DB/assets when present. */
    var inlineJson: String? = null

     fun setVariableOnBottomSheet(key: String, value: String) {
        println("BottomSheetDiv: Setting variable - key=$key, value=$value")
        println("BottomSheetDiv: Bottom sheet ID: $assignedId")
        println("BottomSheetDiv: div is initialized? ${::div.isInitialized}")
        try {
            div.setVariable(key, value)
            println("BottomSheetDiv: Variable set successfully!")
        } catch (e: Exception) {
            println("BottomSheetDiv: Failed to set variable: ${e.message}")
            e.printStackTrace()
        }
    }
    fun onApply(json: String, patchName: String) {
        onApplyOnbase(json, patchName, "", "")
    }

    fun getDiv2View(): Div2View? = if (::div.isInitialized) div else null

    fun hostsDiv2View(div2View: Div2View?): Boolean =
        ::div.isInitialized && div2View != null && div === div2View

    fun onApplyOnbase(json: String, patchName: String, patchTitle: String, vehicleType: String) {
        if (!::div.isInitialized || json.isBlank()) return
        try {
            div.applyPatch(JSONObject(json).asDivPatchWithTemplates())
            if (patchTitle.isNotEmpty() && patchTitle != "تست" && patchTitle != "null") {
                try {
                    div.setVariable("variable_patch", patchTitle)
                } catch (_: Exception) {
                }
                try {
                    div.setVariable("patch", patchTitle)
                } catch (_: Exception) {
                }
            }
            if (vehicleType.isNotEmpty() && vehicleType != "تست" && vehicleType != "null") {
                div.setVariable("vehicle_type", vehicleType)
            }
            if (patchName.isNotEmpty() && patchName != "تست" && patchName != "patchEstelam") {
                div.setVariable("plateType", patchName)
            }
        } catch (e: Exception) {
            println("BottomSheetDiv: Failed to apply patch: ${e.message}")
            e.printStackTrace()
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            if (fullScreen) {
                bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
                dialog.behavior.apply {
                    skipCollapsed = true
                    isFitToContents = false
                    expandedOffset = 0
                    state = BottomSheetBehavior.STATE_EXPANDED
                }
                dialog.window?.setLayout(
                    requireContext().resources.displayMetrics.widthPixels,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            } else {
                // Wrap content and stay pinned to the bottom; do not force a fixed
                // window height (that made short sheets appear at the top).
                bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
                bottomSheet?.requestLayout()
                dialog.behavior.apply {
                    skipCollapsed = true
                    isFitToContents = true
                    state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        println("BottomSheetDiv: Bottom sheet dismissed - ID: $assignedId")
        // Remove this bottom sheet from the manager
        bottomSheetManager?.removeBottomSheet(this)
        println("BottomSheetDiv: Removed from BottomSheetManager")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = BottomSheetDivBinding.inflate(inflater, container, false)
        binding.root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            if (fullScreen) ViewGroup.LayoutParams.MATCH_PARENT
            else ViewGroup.LayoutParams.WRAP_CONTENT
        )
//        val retrofit = SingletonObjects.retrofitInstance()
//        val create = retrofit?.create(PhPlusApi::class.java)
//        val database = SingletonObjects.getDbInstance(context)
//        val sharedPreferences = SingletonObjects.sharedPreferencesInstance(context)
//        if (sharedPreferences != null)
//            sharePref = SharePref(sharedPreferences)
//        if (create != null)
//            remoteDataSource = PhPlusRemoteDataSource(create)
//        if (database != null) {
//            repository = PhPlusRepository(remoteDataSource, sharePref, database.phPlusDBDao())
//        }
//        val factory = MyViewModelFactory(repository)
//        viewModelBottomSheetDiv =
//            ViewModelProvider(this, factory)[viewModelBottomSheetDiv::class.java]


        android.util.Log.d(
            "BottomSheetDiv",
            "onCreateView key='$jsonName' contentLen=${jsonContent?.length ?: 0} inlineLen=${inlineJson?.length ?: 0} id=$assignedId"
        )

        val nextJson = resolveBottomSheetJson()

        if (!nextJson.isNullOrBlank()) {
            try {
                val divJson = JSONObject(nextJson)
                applyInitialVariables(divJson, initialVariables)

                android.util.Log.d(
                    "BottomSheetDiv",
                    "Creating UIDiv2ViewCreator for key='$jsonName' jsonLen=${nextJson.length}"
                )
                println("BottomSheetDiv: Creating UIDiv2ViewCreator with BottomSheetManager - is null? ${bottomSheetManager == null}")
                val divCreator = UIDiv2ViewCreator(context, lo, mehdiViewModel, activity, this, bottomSheetManager)
                
                // Update BottomSheetManager reference in action handler
                println("BottomSheetDiv: Updating BottomSheetManager in action handler")
                divCreator.updateBottomSheetManager(bottomSheetManager)
                div = divCreator.createDiv2ViewMehdi(
                    activity,
                    divJson,
                    binding.root,
                    ScenarioLogDelegate.Stub
                )
                div.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    if (fullScreen) ViewGroup.LayoutParams.MATCH_PARENT
                    else ViewGroup.LayoutParams.WRAP_CONTENT
                )
                binding.root.addView(div)
                initialVariables.forEach { (key, value) ->
                    runCatching { div.setVariable(key, value) }
                        .onFailure { println("BottomSheetDiv: setVariable $key failed: ${it.message}") }
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "BottomSheetDiv",
                    "Failed to render bottom sheet key='$jsonName'",
                    e
                )
            }
        } else {
            android.util.Log.e(
                "BottomSheetDiv",
                "No JSON found for bottom sheet key='$jsonName' (not in response, DB, or assets)"
            )
        }

        return binding.root
    }

    private fun resolveBottomSheetJson(): String? {
        (jsonContent ?: inlineJson)?.takeIf { it.isNotBlank() }?.let {
            android.util.Log.d("BottomSheetDiv", "Using response JSON for key='$jsonName' len=${it.length}")
            return it
        }
        val fromDb = runCatching {
            mehdiViewModel.getValueByKey(jsonName)?.value?.takeIf { it.isNotBlank() }
        }.onFailure {
            android.util.Log.w(
                "BottomSheetDiv",
                "DB lookup failed for key='$jsonName': ${it.message}"
            )
        }.getOrNull()
        if (!fromDb.isNullOrBlank()) {
            android.util.Log.d("BottomSheetDiv", "Using DB JSON for key='$jsonName' len=${fromDb.length}")
            return fromDb
        }
        val fromAssets = loadJsonFromAssets(jsonName)
        if (!fromAssets.isNullOrBlank()) {
            android.util.Log.d("BottomSheetDiv", "Using assets JSON for key='$jsonName' len=${fromAssets.length}")
            return fromAssets
        }
        return null
    }

    private fun applyInitialVariables(divJson: JSONObject, vars: Map<String, String>) {
        if (vars.isEmpty()) return
        val variables = divJson.optJSONObject("card")?.optJSONArray("variables")
        if (variables != null) {
            for (i in 0 until variables.length()) {
                val item = variables.optJSONObject(i) ?: continue
                val name = item.optString("name")
                val value = vars[name] ?: continue
                item.put("value", value)
            }
        }
        replaceExpressionSlots(divJson, vars)
    }

    private fun replaceExpressionSlots(node: Any?, vars: Map<String, String>) {
        when (node) {
            is JSONObject -> {
                node.keys().asSequence().toList().forEach { key ->
                    val value = node.opt(key)
                    if (value is String && value.startsWith("@{") && value.endsWith("}")) {
                        val name = value.substring(2, value.length - 1)
                        vars[name]?.let { node.put(key, it) }
                    } else {
                        replaceExpressionSlots(value, vars)
                    }
                }
            }
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    replaceExpressionSlots(node.opt(i), vars)
                }
            }
        }
    }

    private fun loadJsonFromAssets(name: String): String? {
        val underscored = name.replace('/', '_')
        val folder = name.substringBefore('/', missingDelimiterValue = "")
        val leaf = name.substringAfter('/', missingDelimiterValue = name)
        val candidates = ArrayList<String>().apply {
            add("application/$name.json")
            add("application/${underscored}.json")
            if (folder.isNotBlank()) {
                // Accidents/policeCenter → application/Accidents/Accidents_policeCenter.json
                add("application/$folder/${folder}_$leaf.json")
                add("application/$folder/$underscored.json")
                add("application/$folder/$leaf.json")
            }
            add("application/ph/${underscored}.json")
        }
        for (path in candidates) {
            val json = runCatching {
                context.assets.open(path).bufferedReader().use { it.readText() }
            }.getOrNull()
            if (!json.isNullOrBlank()) return json
        }
        return null
    }


}

//    override fun onClicked(item: ListItemDto) {
//        CustomBottomSheetSpinnerClickListener.value.value = item
//        dialog.dismiss()
//    }
