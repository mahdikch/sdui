package com.yandex.divkit.demo.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.divkit.demo.BuildConfig
import com.yandex.divkit.demo.ui.LoadScreenListener
import com.yandex.divkit.demo.ui.bottomSheetDiv.BottomSheetDiv
import com.yandex.divkit.demo.ui.dialogDiv.DialogDiv
import com.yandex.divkit.demo.ui.toastDiv.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    private val REQUEST_CODE_STORAGE = 1001
//    private lateinit var observerRemoteData: Observer<String>
//    private lateinit var observerRemoteData: Observer<MutableMap<String, String>>

    //    private lateinit var next: String
    private var nextJson: String = ""
    private var nextJsonDto: PhPlusDB? = null
    private var divPageName: String = ""
    private var nextScreen: String = ""


    private var page: String = "application/startPoint.json"

    //    val mehdiViewModel: MehdiViewModel by viewModels()
//
//        val mehdiViewModel: MehdiViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val bundle = intent.extras
        val json = bundle?.getString("json")
        val sysname = bundle?.getString("sysName")
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
                if (sysname != null && sysname == "vt") {
                    val data = bundle.getString("data")
                    val restoredMap: MutableMap<String, String> = Gson().fromJson(
                        data,
                        object : TypeToken<MutableMap<String, String>>() {}.type
                    )

                    restoredMap["ticket_type"]?.let { div.setVariable("ticket_type", it) }
                    restoredMap["national_code"]?.let { div.setVariable("national_code", it) }
                    restoredMap["usage_code"]?.let { div.setVariable("variable_usage_code", it) }
                    restoredMap["color_code"]?.let { div.setVariable("variable_color_code", it) }
                    restoredMap["system_code"]?.let { div.setVariable("variable_system_code", it) }
                    restoredMap["sysName"]?.let { div.setVariable("sysName", it) }
                    restoredMap["police_code"]?.let { div.setVariable("policeCode", it) }
                    restoredMap["address"]?.let { div.setVariable("address", it) }
                    restoredMap["iranian"]?.let { div.setVariable("iranian", it) }
                    restoredMap["plateA"]?.let { div.setVariable("variable_plateA", it) }
                    restoredMap["plateB"]?.let { div.setVariable("variable_plateB", it) }
                    restoredMap["plateC"]?.let { div.setVariable("variable_plateC", it) }
                    restoredMap["plateD"]?.let { div.setVariable("variable_plateD", it) }
                    restoredMap["car_model"]?.let {
                        div.setVariable(
                            "car_model_information_vt",
                            it
                        )
                    }
                    restoredMap["plate_description"]?.let {
                        div.setVariable(
                            "plate_description",
                            it
                        )
                    }
                    restoredMap["countPeople"]?.let { div.setVariable("number_of_passengers", it) }
                    restoredMap["name"]?.let { div.setVariable("variable_driver_name_vt", it) }
                    restoredMap["license_number"]?.let {
                        div.setVariable(
                            "variable_driver_license_number_vt",
                            it
                        )
                    }
                    restoredMap["violationType1"]?.let { div.setVariable("violation1_code", it) }
                    restoredMap["violationType1_title"]?.let {
                        div.setVariable(
                            "violation1_title",
                            it
                        )
                    }
                    restoredMap["violationType2"]?.let { div.setVariable("violation2_code", it) }
                    restoredMap["violationType2_title"]?.let {
                        div.setVariable(
                            "violation2_title",
                            it
                        )
                    }
                    restoredMap["violationType3"]?.let { div.setVariable("violation3_code", it) }
                    restoredMap["violationType3_title"]?.let {
                        div.setVariable(
                            "violation3_title",
                            it
                        )
                    }
                    restoredMap["timePda"]?.let { div.setVariable("time", it) }
                    restoredMap["datePda"]?.let { div.setVariable("date", it) }
                    restoredMap["city_code"]?.let { div.setVariable("cityPolice_code", it) }
                    restoredMap["isInternal"]?.let { div.setVariable("isInternal", it) }
                    restoredMap["isOnline"]?.let { div.setVariable("isOnline", it) }
                    restoredMap["variable_system_title"]?.let {
                        div.setVariable(
                            "variable_system_title",
                            it
                        )
                    }
                    restoredMap["variable_color_title"]?.let {
                        div.setVariable(
                            "variable_color_title",
                            it
                        )
                    }
                    restoredMap["variable_usage_title"]?.let {
                        div.setVariable(
                            "variable_usage_title",
                            it
                        )
                    }

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
                    var reset = ""
                    var toast = ""
                    var dialog = ""
                    var bottomSheet = ""
                    var bottomSheetPatch = ""
                    var patch = ""
                    var update = ""
                    var permissions = ""
                    var res: MutableMap<String, String>? = it.data
                    if (res != null) {
//                        var i: Long = 1
                        for ((key, value) in res) {
                            if (key != null && value != null) {
                                when (key) {
                                    "next" -> next = value
                                    "show_toast" -> toast = value
                                    "show_dialog" -> dialog = value
                                    "show_bottomSheet" -> bottomSheet = value
                                    "set_patch" -> patch = value
                                    "bottom_sheet_set_patch" -> bottomSheetPatch = value
                                    "reset" -> reset = value
                                    "update" -> update = value
                                    "permissions" -> permissions = value
                                    else -> mehdiViewModel.insertItemToDb(
                                        PhPlusDB(
                                            null,
                                            key,
                                            value
                                        )
                                    )
                                }
//                                if (key == "next") {
//                                    next = value
//                                } else if (key == "show_toast") {
//                                    toast = value
//                                } else if (key == "show_dialog") {
//                                    dialog = value
//                                } else if (key == "show_bottomSheet") {
//                                    bottomSheet = value
//                                } else if (key == "set_patch") {
//                                    patch = value
//                                } else if (key == "bottom_sheet_set_patch") {
//                                    bottomSheetPatch = value
//                                } else if (key == "close_bottom_sheet") {
////                                   btmSheet.dismiss()
//                                } else if (key == "reset") {
//                                   reset=value
//                                } else {
//                                    mehdiViewModel.insertItemToDb(PhPlusDB(null, key, value))
//                                }
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
                            if (json != null) {
                                customToast?.show(
                                    json = json,
                                    duration = Toast.LENGTH_LONG
                                )
                            }
                        }
                        if (bottomSheet != "") {
//                            if (btmSheet!=null)
//                                btmSheet.dismiss()
                            println("bottomSheet1:${bottomSheet}")
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
                            btmSheet.onApply(bottomSheetPatch, "")

                        }
                        if (reset != "") {
                            resetActivityForLoad(
                                MehdiActivity::class.java,
                                reset
                            )

                        }
                        if (update != "") {
                            updateApp(update)
                        }
                        if (permissions != "") {
                            checkAndRequestPermissions()
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
                    if (nextScreen != "") {
                        startActivityForLoad(MehdiActivity::class.java, nextScreen)
                        nextScreen = ""
                    }
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

    private fun resetActivityForLoad(klass: Class<out Activity>, jsonName: String) {

//        if (Constants.CURRENT_SCREEN != jsonName) {
        val intent = Intent(this, klass)
        intent.putExtra("json", jsonName)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(this, intent, null)
        finish()


    }

    private fun startActivityWithDataForLoad(
        klass: Class<out Activity>,
        jsonName: String,
        data: String
    ) {

//        if (Constants.CURRENT_SCREEN != jsonName) {
        val intent = Intent(this, klass)
        intent.putExtra("json", jsonName)
        intent.putExtra("sysName", "vt")
        intent.putExtra("data", data)
        ContextCompat.startActivity(this, intent, null)
        if (Constants.CURRENT_SCREEN == "ph/splash" || Constants.CURRENT_SCREEN == "ph/login" || Constants.CURRENT_SCREEN == "ph/main") {
            finish()
        }


    }

    private fun updateApp(url: String) {
        val APK_URL = url
        if (checkPermissions()) {
            startDownload(APK_URL)
        }


    }


    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            // Request permissions
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // All permissions are already granted
            proceedWithCameraAndLocation()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions.all { it.value }
            if (allPermissionsGranted) {
                // All permissions are granted, proceed with camera and location access
                proceedWithCameraAndLocation()
            } else {
                // Some permissions are denied, show a message or disable functionality
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun proceedWithCameraAndLocation() {
        // Start using the camera and location
        Toast.makeText(this, "Camera and location access granted", Toast.LENGTH_SHORT).show()
    }

    private fun showPermissionRationale(permission: String) {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs the $permission to function properly.")
            .setPositiveButton("OK") { _, _ ->
                // Request the permission again
                requestPermissionLauncher.launch(arrayOf(permission))
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
    private fun startDownload(url: String) {
        // Launch a coroutine to handle the download
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val downloadId = withContext(Dispatchers.IO) {
                    downloadApk(url)
                }
                monitorDownload(downloadId)
            } catch (e: Exception) {
                Toast.makeText(
                    this@MehdiActivity,
                    "Download failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun downloadApk(url: String): Long {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading Update")
            .setDescription("Downloading the latest version of the app")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app_update.apk")

        return downloadManager.enqueue(request)
    }

    @SuppressLint("Range")
    private suspend fun monitorDownload(downloadId: Long) {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var downloading = true

        while (downloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        // Download completed, start installation
                        val uri = downloadManager.getUriForDownloadedFile(downloadId)
                        uri?.let { installApk(it) }
                        downloading = false
                    }

                    DownloadManager.STATUS_FAILED -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MehdiActivity,
                                "Download failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        downloading = false
                    }
                }
            }
            cursor.close()
            delay(1000) // Check every second
        }
    }

    private fun installApk(apkUri: Uri) {
        val file = File(apkUri.path ?: return)
        val contentUri =
            FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }
//    @SuppressLint("Range")
//    private fun downloadApk(url: String) {
//        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        val request = DownloadManager.Request(Uri.parse(url))
//            .setTitle("Downloading Update")
//            .setDescription("Downloading the latest version of the app")
//            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app_update.apk")
//
//        val downloadId = downloadManager.enqueue(request)
//
//        // Monitor the download progress
//        Thread {
//            var downloading = true
//            while (downloading) {
//                val query = DownloadManager.Query().setFilterById(downloadId)
//                val cursor = downloadManager.query(query)
//                if (cursor.moveToFirst()) {
//                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
//                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
//                        // Download completed, start installation
//                        val uri = downloadManager.getUriForDownloadedFile(downloadId)
//                        uri?.let { installApk(it) }
//                        downloading = false
//                    } else if (status == DownloadManager.STATUS_FAILED) {
//                        Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
//                        downloading = false
//                    }
//                }
//                cursor.close()
//            }
//        }.start()
//    }

    private fun checkPermissions(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE
            )
            false
        } else {
            true
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
                if (mutableEntry.key == "screenName")
                    nextScreen = mutableEntry.value

                if (mutableEntry.value == "empty" || mutableEntry.value == "null")
                    if (mutableEntry.key == "currentScreen") {
                        map.put(mutableEntry.key, Constants.CURRENT_SCREEN)
                    } else if (mutableEntry.key == "versionCodePhPlus") {
                        map.put(mutableEntry.key, BuildConfig.VERSION_CODE.toString())

                    }else if (mutableEntry.key == "versionNamePhPlus") {
                        map.put(mutableEntry.key, BuildConfig.VERSION_NAME)

                    } else {
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
                    println(map.toString())

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

    override fun loadeScreenWithData(data: String, screenName: String) {
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
                startActivityWithDataForLoad(
                    MehdiActivity::class.java,
                    next,
                    data
                )
//                }
            }
        }
    }

    override fun update(url: String) {
        updateApp(url)
    }
}
