package com.yandex.divkit.demo.ui.activity

//import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dcastalia.localappupdate.DownloadApk
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.div.core.view2.Div2View
import com.yandex.divkit.demo.BuildConfig
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
import com.yandex.divkit.demo.service.AudioRecordingService
import com.yandex.divkit.demo.ui.LoadScreenListener
import com.yandex.divkit.demo.ui.UIDiv2ViewCreator
import com.yandex.divkit.demo.ui.bottomSheetDiv.BottomSheetDiv
import com.yandex.divkit.demo.ui.dialogDiv.DialogDiv
import com.yandex.divkit.demo.ui.toastDiv.CustomToast
import com.yandex.divkit.demo.utils.ErrorDialog
import com.yandex.divkit.demo.utils.LoadingDialog
import com.yandex.divkit.demo.utils.SingletonObjects
import com.yandex.divkit.regression.ScenarioLogDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.UUID


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
    private lateinit var observerRemoteData: Observer<HashMap<String, String>>
    private lateinit var observerScreenToLoad: Observer<String>
    private lateinit var observerVariableToSet: Observer<String>
    private lateinit var observerVariableToGet: Observer<String>
    // لیست برای ذخیره متغیرهای شامل "variable"
    private var varlist: ArrayList<String> = arrayListOf()
    private lateinit var div: Div2View
    private lateinit var btmSheet: BottomSheetDiv
    private val REQUEST_CODE_STORAGE = 1001
//    private lateinit var observerRemoteData: Observer<String>
//    private lateinit var observerRemoteData: Observer<MutableMap<String, String>>

    //    private lateinit var next: String
    // متغیرهای مربوط به ناوبری و JSON
    private var nextJson: String = ""
    private var nextJsonDto: PhPlusDB? = null
    private var divPageName: String = ""
    private var nextScreen: String = ""

//    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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
        var requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startRecordingService()
                } else {
                    Toast.makeText(this, "دسترسی ضبط صدا رد شد", Toast.LENGTH_SHORT).show()
                }
            }
        binding = ActivityMehdiBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        checkAndRequestPermissions()
        loading = LoadingDialog(this)
        error = ErrorDialog(this)
//        loading.showLoadingDialog("لطفا شکیبا باشید...")

        // مقداردهی اولیه سرویس‌ها و Repository
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

        // تنظیم امنیت صفحه (جلوگیری از گرفتن اسکرین‌شات)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
//        if (isDeviceRooted()) {
//            Toast.makeText(this, "دستگاه شما روت است", Toast.LENGTH_LONG).show()
//            finishAffinity() // close the app
//        }
        setupObservers()

        var path = ""
        var divJson = JSONObject()
        if (json == null) {
            // تولید phid جدید برای دستگاه
            var phId = UUID.randomUUID().toString()
            mehdiViewModel.insertItemToDb(PhPlusDB(null, "phid", phId))
            var divMotorJson = assetReader.read("application/patchMotorPlate.json")
            var testJson = assetReader.read("application/test.json")
            var testPatch = assetReader.read("application/testPatch.json")
//            var testJsondata = assetReader.read("application/ph_test_data.json")

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
//            mehdiViewModel.insertItemToDb(
//                PhPlusDB(
//                    null,
//                    "ph/vt/bottomsheet",
//                    divVtBottomSheet.toString()
//                )
//            )
            mehdiViewModel.insertItemToDb(
                PhPlusDB(
                    null,
                    "ph/test",
                    testJson.toString()
                )
            )
            mehdiViewModel.insertItemToDb(
                PhPlusDB(
                    null,
                    "ph/test/patch",
                    testPatch.toString()
                )
            )
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
//                Configuration.ORIENTATION_PORTRAIT -> "application/temp.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/switch.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/patchTest.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/main.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/baseMain.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/testBaseMain.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/login.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/vt-register-ticket.json"
                Configuration.ORIENTATION_PORTRAIT -> "application/startPoint.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/testStartPoint46.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/sabte-takhalof.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/navigation.json"
//                Configuration.ORIENTATION_LANDSCAPE -> "application/login.json"
                else -> "application/startPoint.json"
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
            nextJsonDto = repository.getValueByKey(json)
            if (nextJsonDto != null) {
                nextJson = nextJsonDto!!.value.toString()
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
                // اعمال متغیرها برای سیستم "vt"
                if (sysname != null && sysname == "vt") {
                    val data = bundle.getString("data")
                    val restoredMap: MutableMap<String, String> = Gson().fromJson(
                        data,
                        object : TypeToken<MutableMap<String, String>>() {}.type
                    )
                    for (entry in restoredMap) {
                        div.setVariable(entry.key, entry.value)
                    }


                }
                binding.root.addView(div)

            }
        }
        observerRemoteData = Observer { map ->

            error.showErrorDialog("ffffffffffffff")

            varlist.clear()
            var counter = 0
            if (map.isNotEmpty())
                for (mutableEntry in map) {
                    // جایگزینی مقادیر "empty" یا "null" با داده‌های دیتابیس
                    if (mutableEntry.value == "empty" || mutableEntry.value == "null")
                        if (mutableEntry.key == "currentScreen") {
                            map.put(mutableEntry.key, Constants.CURRENT_SCREEN)
                        } else {
                            var dbValue = mehdiViewModel.getValueByKey(mutableEntry.key)
                            if (dbValue != null)
                                dbValue.value?.let { map.put(mutableEntry.key, it) }

                        }
                    // ذخیره متغیرهای شامل "variable" در لیست
                    if (mutableEntry.key.contains("variable")) {
                        varlist?.add(mutableEntry.value)
//                        map.remove(mutableEntry.key)

                    }
                    counter++
                    if (counter == map.size) {
                        var phId: String = mehdiViewModel.getValueByKey("phid").value.toString()
                        mehdiViewModel.setphPlusRequest(phId, map)

                    }

                }
        }
        RemoteData.value.observe(this, observerRemoteData)

        var next = ""

        observerScreenToLoad = Observer {
            if (it.isNotEmpty()) {
                next = mehdiViewModel.getValueByKey(it).value.toString()
                if (next != null || next != "") {
                    // به‌روزرسانی صفحه فعلی و لود صفحه جدید
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
                    var varList = java.util.HashMap(it.data)
                    println("key:${it.data.toString()}")
                    val data: java.util.HashMap<String, String>
                    var next = ""
                    var reset = ""
                    var toast = ""
                    var dialog = ""
                    var bottomSheet = ""
                    var bottomSheetPatch = ""
                    var patch = ""
                    var update = ""
                    var permissions = ""
                    var reset_phid = ""
//                    var res: MutableMap<String, String>?= sortedMapOf()
                    if (varList?.contains("next") == true) {
                        val nv = varList["next"]
                        varList.remove("next")
                        varList["next"] = nv!!
                    }
//                    it.data?.let { data-> res?.putAll(data) }

                    if (varList != null) {
//                        var i: Long = 1
                        for ((key, value) in varList) {
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
                                    "reset_phid" -> reset_phid = value
                                    else -> {
                                        repository.insertItem(
                                            PhPlusDB(
                                                null,
                                                key,
                                                value
                                            )
                                        )
                                    }
                                }

                                if (key.contains("variable"))
                                    div.setVariable(key, value)
                                if (key.contains("bottom_sheet_variable"))
                                    btmSheet.setVariableOnBottomSheet(key, value)

                            }
                        }
                        // اجرای اقدامات بر اساس keywordها
                        if (next != "") {
                            nextJsonDto = repository.getValueByKey(next)
                            if (nextJsonDto == null)
                                repository.insertItem(
                                    PhPlusDB(
                                        null,
                                        next,
                                        varList[next]
                                    )
                                )
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
                        if (reset_phid != "") {
                            var phId = UUID.randomUUID().toString()
                            mehdiViewModel.insertItemToDb(PhPlusDB(null, "phid", phId))
                        }
                        if (patch != "") {
                            var json = ""
                            val dbPatch = mehdiViewModel.getValueByKey(patch)
                            println("dppatch: " + dbPatch.toString())
                            if (dbPatch != null)
                                json = dbPatch.value.toString()
                            if (json != null && json != "") {
                                var patchTitle = "تست"
                                var vehicleType = "تست"
                                onApplyOnbase(json, patch, patchTitle, vehicleType)
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

                    }
                    loading.dismissDialog()



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
    // ریست اکتیویتی و لود صفحه جدید
    private fun resetActivityForLoad(klass: Class<out Activity>, jsonName: String) {

//        if (Constants.CURRENT_SCREEN != jsonName) {
        val intent = Intent(this, klass)
        intent.putExtra("json", jsonName)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(this, intent, null)
        finish()


    }
    // لود صفحه با داده خاص
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location , camera and storage permissions to use this app",
                103,
//                android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                android.Manifest.permission.ACCESS_FINE_LOCATION,
//                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location , camera and storage permissions to use this app",
                103,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION

            )
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

    //
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

    // بررسی روت بودن دستگاه
    fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su",
            "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
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
                next = mehdiViewModel.getValueByKey(screenName).value.toString()
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

    override fun onRequest(map: HashMap<String, String>) {
        varlist.clear()
        var counter = 0
        if (map.isNotEmpty())
            for (mutableEntry in map) {
                if (mutableEntry.key == "screenName")
                    nextScreen = mutableEntry.value

                if (mutableEntry.value == "empty" || mutableEntry.value == "null")
                    if (mutableEntry.key == "currentScreen") {
                        map.put(mutableEntry.key, Constants.CURRENT_SCREEN)
                    }
//                    else if (mutableEntry.key == "versionCodePhPlus") {
//                        map.put(mutableEntry.key, BuildConfig.VERSION_CODE.toString())
//
//                    } else if (mutableEntry.key == "versionNamePhPlus") {
//                        map.put(mutableEntry.key, BuildConfig.VERSION_NAME)
//
//                    }
                    else {
                        var dbValue = mehdiViewModel.getValueByKey(mutableEntry.key)
                        if (dbValue != null)
                            dbValue.value?.let { map.put(mutableEntry.key, it) }
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
                    var phId = mehdiViewModel.getValueByKey("phid").value.toString()
                    map.put("phId", phId)

                    if (map.contains("base64")) {

                        var text = map.get("base64")?.replace("\n", "")
                        text?.replace(" ", "+")
                        if (text != null) {
                            map.put("base64", text)
                        }
                    }
                    if (map.contains("base64Image")) {
                        var text = map.get("base64Image")?.replace("\n", "")
                        text?.replace(" ", "+")
                        if (text != null) {
                            map.put("base64Image", text)
                        }
                    }
                    if (map.contains("car_picture")) {
                        var text = map.get("car_picture")?.replace("\n", "")
                        text?.replace(" ", "+")
                        if (text != null) {
                            map.put("car_picture", text)
                        }
                    }
                    map.put("versionCodePhPlus", BuildConfig.VERSION_CODE.toString())
                    map.put("versionNamePhPlus", BuildConfig.VERSION_NAME)
                    map.put("androidVersionNamePhPlus", Build.VERSION.SDK_INT.toString())
                    map.put(
                        "imei",
                        Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
                    )
                    map.put("deviceModel", Build.MODEL)
                    mehdiViewModel.setphPlusRequest(phId, map)
                    println(map.toString())

                }

            }
    }

    override fun onApplyOnbase(
        json: String,
        patchName: String,
        patchTitle: String,
        vehicleType: String
    ) {

        div.applyPatch(JSONObject(json).asDivPatchWithTemplates())
        if (patchTitle != "" && patchTitle != "تست")
            div.setVariable("patch", patchTitle)
        if (vehicleType != "" && vehicleType != "تست")
            div.setVariable("vehicle_type", vehicleType)
        if (patchName != "" && patchName != "تست" && patchName != "patchEstelam")
            div.setVariable("plateType", patchName)
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
                next = mehdiViewModel.getValueByKey(screenName).value.toString()
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
//        updateApp(url)
//        ApkDownloader(this).downloadAndInstall(url)
        val downloadApk = DownloadApk(this)


// With standard fileName 'App Update.apk'
        downloadApk.startDownloadingApk(url)
    }

    override fun getLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location , camera and storage permissions to use this app",
                103,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location , camera and storage permissions to use this app",
                103,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    override fun getWritePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location , camera and storage permissions to use this app",
                103,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location , camera and storage permissions to use this app",
                103,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun getCameraPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location , camera and storage permissions to use this app",
                103,
                android.Manifest.permission.CAMERA
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location , camera and storage permissions to use this app",
                103,
                android.Manifest.permission.CAMERA
            )
        }
    }

    override fun getAllPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "برنامه به دسترسی های حافظه , دوربین و لوکیشن نیاز دارد.",
                103,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION

            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "برنامه به دسترسی های حافظه , دوربین و لوکیشن نیاز دارد.",
                103,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION

            )
        }
    }

    override fun setPageToDB(key: String) {
        mehdiViewModel.insertItemToDb(PhPlusDB(null, key, nextJson))
    }

    fun requestAudioPermissionAndStart() {


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        } else {
            startRecordingService()
        }
    }


    fun startRecordingService() {
        val intent = Intent(this, AudioRecordingService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    override fun startRecording() {
        requestAudioPermissionAndStart()
    }




}
