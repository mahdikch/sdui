package com.yandex.divkit.demo.ui.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yandex.div.core.view2.Div2View
import com.yandex.divkit.demo.data.Constants
import com.yandex.divkit.demo.data.RemoteData
import com.yandex.divkit.demo.data.ScreenToLoad
import com.yandex.divkit.demo.data.SharePref
import com.yandex.divkit.demo.data.VariableToGet
import com.yandex.divkit.demo.data.VariableToSet
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.data.local.PhPlusDBDao
import com.yandex.divkit.demo.data.remote.PhPlusApi
import com.yandex.divkit.demo.data.remote.PhPlusRemoteDataSource
import com.yandex.divkit.demo.data.remote.Resource
import com.yandex.divkit.demo.data.repository.PhPlusRepository
import com.yandex.divkit.demo.databinding.ActivityMehdiBinding
import com.yandex.divkit.demo.div.asDivPatchWithTemplates
import com.yandex.divkit.demo.screenshot.DivAssetReader
import com.yandex.divkit.demo.ui.UIDiv2ViewCreator
import com.yandex.divkit.demo.utils.ErrorDialog
import com.yandex.divkit.demo.utils.LoadingDialog
import com.yandex.divkit.demo.utils.SingletonObjects
import com.yandex.divkit.regression.ScenarioLogDelegate
//import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

import androidx.fragment.app.FragmentActivity
import com.yandex.divkit.demo.ui.LoadScreenListener
import com.yandex.divkit.demo.ui.bottomSheetDiv.BottomSheetDiv
import com.yandex.divkit.demo.ui.dialogDiv.DialogDiv
import com.yandex.divkit.demo.ui.toastDiv.CustomToast

//@AndroidEntryPoint
class MehdiActivity : AppCompatActivity(), LoadScreenListener {
    private lateinit var binding: ActivityMehdiBinding
    private lateinit var dbGetListener: DbGetListener
    private lateinit var loading: LoadingDialog
    private lateinit var error: ErrorDialog

    interface DbGetListener {
        fun fetch(value: String)
    }

    private val assetReader = DivAssetReader(this)


    //    @Inject
//    lateinit var mehdiViewModel: MehdiViewModel
    private lateinit var mehdiViewModel: MehdiViewModel
    private lateinit var repository: PhPlusRepository
    private lateinit var remoteDataSource: PhPlusRemoteDataSource
    private lateinit var sharePref: SharePref
    private lateinit var phPlusDBDao: PhPlusDBDao
    private lateinit var observerRemoteData: Observer<MutableMap<String, String>>
    private lateinit var observerScreenToLoad: Observer<String>
    private lateinit var observerVariableToSet: Observer<String>
    private lateinit var observerVariableToGet: Observer<String>
    private var varlist: ArrayList<String> = arrayListOf()
    private lateinit var div: Div2View
    private lateinit var btmSheet: BottomSheetDiv

//    private lateinit var observerRemoteData: Observer<String>
//    private lateinit var observerRemoteData: Observer<MutableMap<String, String>>

    //    private lateinit var next: String
    private var nextJson: String = ""
    private var nextJsonDto: PhPlusDB? = null
    private var divPageName: String = ""

    private var page: String = "application/startPoint.json"

    //    val mehdiViewModel: MehdiViewModel by viewModels()
//
//        val mehdiViewModel: MehdiViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val bundle = intent.extras
        val json = bundle?.getString("json")
        binding = ActivityMehdiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = LoadingDialog(this)
        error = ErrorDialog(this)
//        loading.showLoadingDialog("لطفا شکیبا باشید...")
        val retrofit = SingletonObjects.retrofitInstance()
        val create = retrofit?.create(PhPlusApi::class.java)
        val database = SingletonObjects.getDbInstance(this)
        val sharedPreferences = SingletonObjects.sharedPreferencesInstance(this)
        val androidId = SingletonObjects.androidIdInstance(this)
        if (sharedPreferences != null)
            sharePref = SharePref(sharedPreferences)
        if (create != null)
            remoteDataSource = PhPlusRemoteDataSource(create)
        repository = PhPlusRepository(remoteDataSource, sharePref, database.phPlusDBDao())
        val factory = MyViewModelFactory(repository)
        mehdiViewModel = ViewModelProvider(this, factory)[MehdiViewModel::class.java]



        setupObservers()

        var path = ""
        var divJson = JSONObject()
        if (json == null) {
            var divMotorJson = assetReader.read("application/patchMotorPlate.json")

            var divpatchTestJson = assetReader.read("application/patchTest.json")
            var divpatchReportsJson = assetReader.read("application/patchReports.json")
            var divpatchProfilesJson = assetReader.read("application/patchProfiles.json")
            var divpatchSettingJson = assetReader.read("application/patchSetting.json")
            var divpatchHomeJson = assetReader.read("application/patchHome.json")
            var divreportsJson = assetReader.read("application/reports.json")
            var divreportsIemJson = assetReader.read("application/reports-item.json")
            var divrinqueryJson = assetReader.read("application/inqueryHuman.json")
            var divrpatchInqueryEntezamiJson =
                assetReader.read("application/patchInqueryEntezami.json")
            var divrpatchInqueryGovahinameJson =
                assetReader.read("application/patchInqueryGovahiname.json")
            var divrpatchInqueryHoviatJson = assetReader.read("application/patchInqueryHoviat.json")
            var divrpatchInqueryNezamVazifeJson =
                assetReader.read("application/patchInqueryNezamVazife.json")
            var divrInqueryPassportJson = assetReader.read("application/patchInqueryPassport.json")
            var divrpatchInqueryPlatesJson = assetReader.read("application/patchInqueryPlates.json")
            var divVtBottomSheet = assetReader.read("application/vt_bottom_sheet.json")
//            var divVehicleJson = assetReader.read("application/vt-register-ticket.json")
            mehdiViewModel.insertItemToDb(
                PhPlusDB(
                    null,
                    "ph/vt/bottomsheet",
                    divVtBottomSheet.toString()
                )
            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/patchInqueryEntezami",
//                    divrpatchInqueryEntezamiJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/patchInqueryGovahiname",
//                    divrpatchInqueryGovahinameJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/patchInqueryHoviat",
//                    divrpatchInqueryHoviatJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/patchInqueryNezamVazife",
//                    divrpatchInqueryNezamVazifeJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/InqueryPassport",
//                    divrInqueryPassportJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/patchInqueryPlates",
//                    divrpatchInqueryPlatesJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/reports",
//                    divreportsJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/inqueryHuman",
//                    divrinqueryJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/reports-item",
//                    divreportsIemJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "vt/plates/bottomsheet",
//                    divMotorJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/main/grid/patch",
//                    divpatchTestJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/main/home",
//                    divpatchTestJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/main/setting",
//                    divpatchSettingJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/main/reports",
//                    divpatchReportsJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/main/profile",
//                    divpatchProfilesJson.toString()
//                )
//            )
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/main/home",
//                    divpatchHomeJson.toString()
//                )
//            )

            path = when (resources.configuration.orientation) {
//                Configuration.ORIENTATION_PORTRAIT -> "application/sabte-takhalof.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/container-initial.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/splash.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/mehdi.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/menu.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/test.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/switch.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/patchTest.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/main.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/baseMain.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/testBaseMain.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/login.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/vt-register-ticket.json"
                Configuration.ORIENTATION_PORTRAIT -> "application/startPoint.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/testStartPoint.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/sabte-takhalof.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/navigation.json"
//                Configuration.ORIENTATION_LANDSCAPE -> "application/login.json"
                else -> "application/menu.json"
            }
            div = UIDiv2ViewCreator(this, this, mehdiViewModel, this).createDiv2View(
                this,
                path,
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

        } else {
            nextJsonDto = mehdiViewModel.getValueByKey(json)
            if (nextJsonDto != null) {
                nextJson = nextJsonDto!!.value
                divJson = JSONObject(nextJson)

                div = UIDiv2ViewCreator(this, this, mehdiViewModel, this).createDiv2ViewMehdi(
                    this,
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
        }
        observerRemoteData = Observer { map ->
//            Log.d("", "")
//            if (flagRemoteData) {
//            var emptyMap: MutableMap<String, String> = HashMap<String, String>()
//            loading.showLoadingDialog("لطفا شکیبا باشید...")
            error.showErrorDialog("ffffffffffffff")

            varlist.clear()
            var counter = 0
            if (map.isNotEmpty())
                for (mutableEntry in map) {
                    if (mutableEntry.value == "empty" || mutableEntry.value == "null")
                        if (mutableEntry.key == "currentScreen") {
                            map.put(mutableEntry.key, Constants.CURRENT_SCREEN)
                        } else {
                            var dbValue = mehdiViewModel.getValueByKey(mutableEntry.key)
                            if (dbValue != null)
                                map.put(mutableEntry.key, dbValue.value)

                        }
                    if (mutableEntry.key.contains("variable")) {
                        varlist?.add(mutableEntry.value)
//                        map.remove(mutableEntry.key)

                    }
                    counter++
                    if (counter == map.size) {

                        mehdiViewModel.setphPlusRequest(map)

                    }

                }
        }
        RemoteData.value.observe(this, observerRemoteData)

        var next = ""

        observerScreenToLoad = Observer {
            if (it.isNotEmpty()) {
                next = mehdiViewModel.getValueByKey(it).value
                if (next != null || next != "") {
                    if (Constants.CURRENT_SCREEN != next) {
                        Constants.CURRENT_SCREEN = next
                        mehdiViewModel.insertItemToDb(PhPlusDB(null, "currentScreen", next))
                        startActivityForLoad(
                            MehdiActivity::class.java,
                            next
                        )
                    }
                }
            }

        }

        ScreenToLoad.value.observe(this, observerScreenToLoad)



        observerVariableToGet = Observer {
            if (it.isNotEmpty())
                mehdiViewModel.getVariable(it).observe(this) { itph ->
                    if (itph.isNotEmpty())
                        VariableToSet.value.value = itph[0].value
                }
        }
        VariableToGet.value.observe(this, observerVariableToGet)


    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        RemoteData.value.removeObserver(observerRemoteData)
        ScreenToLoad.value.removeObserver(observerScreenToLoad)
        VariableToGet.value.removeObserver(observerVariableToGet)
    }

    override fun onStop() {
        super.onStop()

    }

    fun setVariable(key: String, value: String) {
    }

    class MyViewModelFactory(private val repository: PhPlusRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MehdiViewModel::class.java)) {
                return MehdiViewModel(repository) as T
            }
            throw Exception("asd")
        }
    }

    private fun setupObservers() {

        mehdiViewModel.phPlus.observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
//                    var varList: MutableList<PhPlusDB> = mutableListOf()
                    println("key:${it.data.toString()}")

                    var next = ""
                    var toast = ""
                    var dialog = ""
                    var bottomSheet = ""
                    var bottomSheetPatch = ""
                    var patch = ""
                    var res: MutableMap<String, String>? = it.data
                    if (res != null) {
//                        var i: Long = 1
                        for ((key, value) in res) {
                            if (key != null && value != null) {
                                if (key == "next") {
                                    next = value
                                } else if (key == "show_toast") {
                                    toast = value
                                } else if (key == "show_dialog") {
                                    dialog = value
                                } else if (key == "show_bottomSheet") {
                                    bottomSheet = value
                                } else if (key == "set_patch") {
                                    patch = value
                                } else if (key == "bottom_sheet_set_patch") {
                                    bottomSheetPatch = value
                                } else if (key == "close_bottom_sheet") {
//                                   btmSheet.dismiss()
                                } else {
                                    mehdiViewModel.insertItemToDb(PhPlusDB(null, key, value))
                                }
//                                if (key == "ph/token") {
//                                    EncryptionConstant.TOKEN = value
//                                }
                                if (key.contains("variable"))
                                    div.setVariable(key, value)
                                if (key.contains("bottom_sheet_variable"))
                                    btmSheet.setVariableOnBottomSheet(key, value)

                            }
                        }
                        if (next != "") {
                            Constants.CURRENT_SCREEN = next
                            startActivityForLoad(
                                MehdiActivity::class.java,
                                next
                            )
                        }
                        if (toast != "") {
                            val customToast =
                                mehdiViewModel?.let { CustomToast(this, this, it, this) }
                            val jsonDto = mehdiViewModel?.getValueByKey(toast)
                            val json = jsonDto?.value
                            customToast?.show(
                                json = json,
                                duration = Toast.LENGTH_LONG
                            )
                        }
                        if (bottomSheet != "") {
//                            if (btmSheet!=null)
//                                btmSheet.dismiss()

                            btmSheet = BottomSheetDiv(this, this, bottomSheet, mehdiViewModel, this)

                            btmSheet.show(
                                (this as FragmentActivity).supportFragmentManager,
                                "bottomSheetList"
                            )
                        }
                        if (dialog != "") {
                            DialogDiv(
                                this, this, bottomSheet, mehdiViewModel, this
                            ).show()
                        }
                        if (patch != "") {
                            var json = mehdiViewModel?.getValueByKey(patch)?.value.toString()
                            if (json != null) {
                                var patchTitle = "تست"
                                onApplyOnbase(json, patch, patchTitle)
                            }

                        }
                        if (bottomSheetPatch != "") {
                          btmSheet.onApply(bottomSheetPatch,"")

                        }
                    }
                    loading.dismissDialog()


//                    if (next!=null)
//                    mehdiViewModel.getPage(next).observe(this){
//                        page = it[0].value
//                    }
//                    page = mehdiViewModel.getPage(page).toString()
                }

                Resource.Status.ERROR -> {
//                     binding.loading.visibility = View.GONE
                    loading.dismissDialog()
                    it.message?.let { it1 ->
                        error.showErrorDialog(it1)
                    }
//                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()

                }

                Resource.Status.LOADING -> {
//                    Toast.makeText(this, "loading", Toast.LENGTH_LONG).show()
                    loading.showLoadingDialog("لطفا شکیبا باشید...")

//                    binding.loading.visibility = View.VISIBLE

                }


            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()

    }

    private fun startActivityForLoad(klass: Class<out Activity>, jsonName: String) {

//        if (Constants.CURRENT_SCREEN != jsonName) {
        val intent = Intent(this, klass)
        intent.putExtra("json", jsonName)
        ContextCompat.startActivity(this, intent, null)
        if (Constants.CURRENT_SCREEN == "ph/splash" || Constants.CURRENT_SCREEN == "ph/login" || Constants.CURRENT_SCREEN == "ph/main") {
            finish()
        }


    }

    override fun onLoad(screenName: String) {
        var next = ""

        if (screenName.isNotEmpty()) {
//            next = mehdiViewModel.getValueByKey(screenName).value
            if (screenName == "nextSplash")
                next = mehdiViewModel.getValueByKey(screenName).value
            else
                next = screenName
            if (next != null || next != "") {
//                if (Constants.CURRENT_SCREEN != next) {
                Constants.CURRENT_SCREEN = next
                mehdiViewModel.insertItemToDb(PhPlusDB(null, "currentScreen", next))
                startActivityForLoad(
                    MehdiActivity::class.java,
                    next
                )
//                }
            }
        }
    }

    override fun onRequest(map: MutableMap<String, String>) {
        varlist.clear()
        var counter = 0
        if (map.isNotEmpty())
            for (mutableEntry in map) {
                if (mutableEntry.value == "empty" || mutableEntry.value == "null")
                    if (mutableEntry.key == "currentScreen") {
                        map.put(mutableEntry.key, Constants.CURRENT_SCREEN)
                    }
//                        else if (mutableEntry.key == "ph/token") {
//                            map.put(mutableEntry.key, EncryptionConstant.TOKEN)
//
//                        }
                    else {
                        var dbValue = mehdiViewModel.getValueByKey(mutableEntry.key)
                        if (dbValue != null)
                            map.put(mutableEntry.key, dbValue.value)
//                            mehdiViewModel.getRequestParam(mutableEntry.value).observe(this) {
//                                if (it.isNotEmpty())
//                                    map.put(mutableEntry.key, it[0].value)
//                            }
                    }
                if (mutableEntry.key.contains("variable")) {
                    varlist?.add(mutableEntry.value)
//                        map.remove(mutableEntry.key)

                }
                counter++
                if (counter == map.size) {
                    mehdiViewModel.setphPlusRequest(map)

                }

            }
    }

    override fun onApplyOnbase(json: String, patchName: String, patchTitle: String) {
//        if (dismiss=="true")
//            btmSheet.dismiss()
//        var json=mehdiViewModel?.getValueByKey(patchName)?.value.toString()
        if (json != null)
            div.applyPatch(JSONObject(json).asDivPatchWithTemplates())
        if (patchTitle != "")
            div.setVariable("patch", patchTitle)
    }

    override fun getBtmSheetInstance(btmSheet: BottomSheetDiv) {
        this.btmSheet = btmSheet
    }

    override fun setVariableToBase(key: String, value: String) {
        div.setVariable(key, value)
    }
}
