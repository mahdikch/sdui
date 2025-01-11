package com.yandex.divkit.demo.ui.bottomSheetDiv

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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
import org.json.JSONObject
import javax.inject.Inject


//@AndroidEntryPoint
class BottomSheetDiv(
    private val context: Context,
    private val activity: Activity,
    private val jsonName: String,
    private val mehdiViewModel: MehdiViewModel,
    private val lo: LifecycleOwner,

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

     fun setVariableOnBottomSheet(key: String, value: String) {
        div.setVariable(key,value)
    }
    fun onApply(json: String, patchName: String) {
        if (json != null)
            div.applyPatch(JSONObject(json).asDivPatchWithTemplates())

    }


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

        _binding = BottomSheetDivBinding.inflate(inflater, container, false)
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


        val JsonDto = mehdiViewModel.getValueByKey(jsonName)
//        Toast.makeText(context, jsonName, Toast.LENGTH_LONG).show()

        if (JsonDto != null) {
            val nextJson = JsonDto!!.value
            val divJson = JSONObject(nextJson)

            div = UIDiv2ViewCreator(context, lo, mehdiViewModel, activity,this).createDiv2ViewMehdi(
                activity,
                divJson,
                binding.root,
                ScenarioLogDelegate.Stub
            )
            div.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                weight = 1F
            }
            binding.root.addView(div)

        }

        return binding.root
    }


}

//    override fun onClicked(item: ListItemDto) {
//        CustomBottomSheetSpinnerClickListener.value.value = item
//        dialog.dismiss()
//    }
