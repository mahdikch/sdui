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
import androidx.activity.addCallback
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
import androidx.lifecycle.lifecycleScope
import com.dcastalia.localappupdate.DownloadApk
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.div.core.view2.Div2View
import com.yandex.div.internal.Log
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
import com.yandex.divkit.demo.ui.bottomSheetDiv.BottomSheetManager
import com.yandex.divkit.demo.ui.dialogDiv.DialogDiv
import com.yandex.divkit.demo.ui.toastDiv.CustomToast
import com.yandex.divkit.demo.utils.ErrorDialog
import com.yandex.divkit.demo.utils.LoadingDialog
import com.yandex.divkit.demo.utils.SingletonObjects
import com.yandex.divkit.demo.push.PushHelper
import com.yandex.divkit.demo.div.notificationList.NotificationManager
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
    private var pendingRecordingId: String? = null
    private lateinit var observerRemoteData: Observer<HashMap<String, String>>
    private lateinit var observerScreenToLoad: Observer<String>
    private lateinit var observerVariableToSet: Observer<String>
    private lateinit var observerVariableToGet: Observer<String>
    // لیست برای ذخیره متغیرهای شامل "variable"
    private var varlist: ArrayList<String> = arrayListOf()
    private lateinit var div: Div2View
    private var bottomSheetManager: BottomSheetManager? = null
    private lateinit var divViewCreator: UIDiv2ViewCreator
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
        
        // Initialize BottomSheetManager
        try {
            bottomSheetManager = BottomSheetManager()
            println("MehdiActivity: BottomSheetManager initialized successfully")
        } catch (e: Exception) {
            println("MehdiActivity: Failed to initialize BottomSheetManager: ${e.message}")
            e.printStackTrace()
            bottomSheetManager = null
        }
        
        // Add modern back navigation handling
        onBackPressedDispatcher.addCallback(this) {
            android.util.Log.d("MehdiActivity", "=== OnBackPressedDispatcher callback called ===")
            android.util.Log.d("MehdiActivity", "Stopping recording before back navigation")
            stopRecording()
            finish()
        }
        
        val intent = intent
        val bundle = intent.extras
        val json = bundle?.getString("json")
        val sysname = bundle?.getString("sysName")
        binding = ActivityMehdiBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        checkAndRequestPermissions()
        loading = LoadingDialog(this)
        error = ErrorDialog(this)
        
        // Setup push notifications (safe to call multiple times)
        PushHelper.setupPushNotifications(this)
        
        // Handle push notification data if present
        handlePushNotificationData()
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

        // Initialize NotificationManager with database access and action handler
        // Note: actionHandler will be available after divViewCreator is created
        NotificationManager.initialize(mehdiViewModel, null)

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
            divViewCreator = UIDiv2ViewCreator(this, this, mehdiViewModel, this, null, bottomSheetManager)
            
            // Update BottomSheetManager reference in action handler
            try {
                divViewCreator.updateBottomSheetManager(bottomSheetManager)
                println("MehdiActivity: Successfully updated BottomSheetManager in action handler")
            } catch (e: Exception) {
                println("MehdiActivity: Failed to update BottomSheetManager in action handler: ${e.message}")
                e.printStackTrace()
            }
            
            div = divViewCreator.createDiv2View(
                this,
                path,
                binding.root,
                ScenarioLogDelegate.Stub
            )
            
            // Set action handler for NotificationManager now that divViewCreator is created
            NotificationManager.setActionHandler(divViewCreator.actionHandler)
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

                divViewCreator = UIDiv2ViewCreator(this, this, mehdiViewModel, this, null, bottomSheetManager)
                
                // Update BottomSheetManager reference in action handler
                try {
                    divViewCreator.updateBottomSheetManager(bottomSheetManager)
                    println("MehdiActivity: Successfully updated BottomSheetManager in action handler")
                } catch (e: Exception) {
                    println("MehdiActivity: Failed to update BottomSheetManager in action handler: ${e.message}")
                    e.printStackTrace()
                }
                
                div = divViewCreator.createDiv2ViewMehdi(
                    this,
                    divJson,
                    binding.root,
                    ScenarioLogDelegate.Stub
                )
                
                // Set action handler for NotificationManager now that divViewCreator is created
                NotificationManager.setActionHandler(divViewCreator.actionHandler)
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
        android.util.Log.d("MehdiActivity", "=== onPause() called ===")
        // Note: Recording service runs in foreground and should continue even when activity pauses
        // Only stop recording when activity is actually destroyed (onDestroy) or user explicitly stops
        android.util.Log.d("MehdiActivity", "Activity paused - recording service continues in foreground")
        RemoteData.value.removeObserver(observerRemoteData)
        ScreenToLoad.value.removeObserver(observerScreenToLoad)
        VariableToGet.value.removeObserver(observerVariableToGet)
    }

    override fun onStop() {
        super.onStop()
        android.util.Log.d("MehdiActivity", "=== onStop() called ===")
        // Note: Recording service runs in foreground and should continue even when activity stops
        // Only stop recording when activity is actually destroyed (onDestroy) or user explicitly stops
        android.util.Log.d("MehdiActivity", "Activity stopped - recording service continues in foreground")
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

    private fun handlePushNotificationData() {
        try {
            val pushMessage = intent.getStringExtra("push_message")
            val pushTopic = intent.getStringExtra("push_topic")
            
            if (pushMessage != null) {
                android.util.Log.d("MehdiActivity", "=== PUSH NOTIFICATION RECEIVED ===")
                android.util.Log.d("MehdiActivity", "Push message: $pushMessage")
                android.util.Log.d("MehdiActivity", "Push topic: $pushTopic")
                
                // You can parse the push message and load specific JSON screen
                // Example: If push message contains JSON screen name
                // val payload = Gson().fromJson(pushMessage, YourPayloadClass::class.java)
                // loadScreen(payload.screenName)
                
                // Ensure Toast runs on main thread
                runOnUiThread {
                    Toast.makeText(this, "Push notification received: $pushMessage", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MehdiActivity", "Error handling push notification data", e)
        }
    }

    private fun setupObservers() {

        mehdiViewModel.phPlus.observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    lifecycleScope.launch {
                        Log.e("SUCCESS", "start")
                        val varList = java.util.HashMap(it.data ?: emptyMap())
                        var next = ""
                        var reset = ""
                        var refresh = ""
                        var toast = ""
                        var dialog = ""
                        var bottomSheet = ""
                        var bottomSheetPatch = ""
                        var patch = ""
                        var update = ""
                        var permissions = ""
                        var reset_phid = ""

                        if (varList.containsKey("next")) {
                            val nv = varList["next"]
                            if (nv != null) {
                                varList.remove("next")
                                varList["next"] = nv
                            }
                        }

                        val pendingInserts = mutableListOf<PhPlusDB>()

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
                                    "refresh" -> refresh = value
                                    "permissions" -> permissions = value
                                    "reset_phid" -> reset_phid = value
                                    else -> pendingInserts.add(PhPlusDB(null, key, value))
                                }

                                if (key.contains("variable")) {
                                    div.setVariable(key, value)
                                }
                                if (key.contains("bottom_sheet_variable")) {
                                    val parts = key.split("_")
                                    println("MehdiActivity: Processing bottom_sheet_variable key=$key, parts=${parts.toList()}")
                                    println("MehdiActivity: bottomSheetManager is null? ${bottomSheetManager == null}")
                                    val manager = bottomSheetManager
                                    if (manager != null) {
                                        if (parts.size >= 4) {
                                            val target = parts[2]
                                            val variableName = parts.drop(3).joinToString("_")
                                            println("MehdiActivity: Setting variable - target=$target, variableName=$variableName, value=$value")
                                            val success = manager.setVariableOnTarget(target, variableName, value)
                                            println("MehdiActivity: Variable setting success=$success")
                                        } else {
                                            println("MehdiActivity: Fallback to current bottom sheet")
                                            manager.getCurrentBottomSheet()?.setVariableOnBottomSheet(key, value)
                                        }
                                    } else {
                                        println("MehdiActivity: bottomSheetManager is null, skipping variable setting")
                                    }
                                }
                            }
                        }

                        suspend fun ensureInserted(targetKey: String) {
                            if (targetKey.isBlank()) return
                            var targetItem: PhPlusDB? = null
                            val iterator = pendingInserts.listIterator()
                            while (iterator.hasNext()) {
                                val item = iterator.next()
                                if (item.key == targetKey) {
                                    targetItem = item
                                    iterator.remove()
                                    break
                                }
                            }
                            if (targetItem == null) {
                                val rawValue = varList[targetKey]
                                if (rawValue != null) {
                                    targetItem = PhPlusDB(null, targetKey, rawValue)
                                }
                            }
                            if (targetItem != null) {
                                withContext(Dispatchers.IO) {
                                    repository.insertItem(targetItem!!)
                                }
                            }
                        }

                        val keysNeedingImmediateInsert = listOf(
                            next,
                            toast,
                            dialog,
                            bottomSheet,
                            bottomSheetPatch,
                            patch,
                            update,
                            refresh
                        )
                        for (key in keysNeedingImmediateInsert) {
                            ensureInserted(key)
                        }

                        if (next.isNotBlank()) {
                            nextJsonDto = withContext(Dispatchers.IO) {
                                repository.getValueByKey(next)
                            }
                            if (nextJsonDto == null) {
                                val nextValue = varList[next]
                                if (nextValue != null) {
                                    withContext(Dispatchers.IO) {
                                        repository.insertItem(PhPlusDB(null, next, nextValue))
                                    }
                                }
                            }
                            Constants.CURRENT_SCREEN = next
                            startActivityForLoad(
                                MehdiActivity::class.java,
                                next
                            )
                        }
                        if (toast.isNotBlank()) {
                            val customToast =
                                mehdiViewModel?.let { CustomToast(this@MehdiActivity, this@MehdiActivity, it, this@MehdiActivity) }
                            val jsonDto = withContext(Dispatchers.IO) {
                                mehdiViewModel?.getValueByKey(toast)
                            }
                            val json = jsonDto?.value
                            if (json != null) {
                                customToast?.show(
                                    json = json,
                                    duration = Toast.LENGTH_LONG
                                )
                            }
                        }
                        if (refresh.isNotBlank()) {
                            refreshPage(refresh)
                        }
                        if (bottomSheet.isNotBlank()) {
                            println("bottomSheet1:${bottomSheet}")
                            println("MehdiActivity: Creating bottom sheet with manager - bottomSheetManager is null? ${bottomSheetManager == null}")
                            val manager = bottomSheetManager
                            if (manager != null) {
                                val newBottomSheet = manager.createAndAddBottomSheet(
                                    this@MehdiActivity, this@MehdiActivity, bottomSheet, mehdiViewModel, this@MehdiActivity
                                )
                                println("MehdiActivity: Bottom sheet created with ID: ${newBottomSheet.assignedId}")
                                println("MehdiActivity: Total bottom sheets: ${manager.getBottomSheetCount()}")
                                println("MehdiActivity: All bottom sheet IDs: ${manager.getAllBottomSheetIds()}")

                                newBottomSheet.show(
                                    (this@MehdiActivity as FragmentActivity).supportFragmentManager,
                                    "bottomSheetList"
                                )
                            } else {
                                println("MehdiActivity: bottomSheetManager is null, creating bottom sheet without manager")
                                val newBottomSheet = BottomSheetDiv(this@MehdiActivity, this@MehdiActivity, bottomSheet, mehdiViewModel, this@MehdiActivity, null)
                                newBottomSheet.show(
                                    (this@MehdiActivity as FragmentActivity).supportFragmentManager,
                                    "bottomSheetList"
                                )
                            }
                        }
                        if (dialog.isNotBlank()) {
                            DialogDiv(
                                this@MehdiActivity, this@MehdiActivity, bottomSheet, mehdiViewModel, this@MehdiActivity
                            ).show()
                        }
                        if (reset_phid.isNotBlank()) {
                            val phId = UUID.randomUUID().toString()
                            withContext(Dispatchers.IO) {
                                repository.insertItem(PhPlusDB(null, "phid", phId))
                            }
                        }
                        if (patch.isNotBlank()) {
                            val dbPatch = withContext(Dispatchers.IO) {
                                mehdiViewModel.getValueByKey(patch)
                            }
                            println("dppatch: " + dbPatch.toString())
                            val json = dbPatch?.value.orEmpty()
                            if (json.isNotEmpty()) {
                                val patchTitle = "تست"
                                val vehicleType = "تست"
                                onApplyOnbase(json, patch, patchTitle, vehicleType)
                            }

                        }
                        if (bottomSheetPatch.isNotBlank()) {
                            bottomSheetManager?.getCurrentBottomSheet()?.onApply(bottomSheetPatch, "")
                        }
                        if (reset.isNotBlank()) {
                            resetActivityForLoad(
                                MehdiActivity::class.java,
                                reset
                            )

                        }
                        if (update.isNotBlank()) {
                            updateApp(update)
                        }

                        if (pendingInserts.isNotEmpty()) {
                            repository.insertListAsync(pendingInserts)
                        }
                        loading.dismissDialog()
                        Log.e("SUCCESS", "end")
                    }
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

    private fun refreshPage(refresh: String) {
   if (refresh.isBlank()) {
            android.util.Log.w("MehdiActivity", "refreshPage called with empty key")
            return
        }
         try {
            val refreshEntry = repository.getValueByKey(refresh)
            val refreshJson = refreshEntry?.value
            if (refreshJson.isNullOrEmpty()) {
                android.util.Log.w("MehdiActivity", "No JSON found in DB for refresh key=" + refresh)
                return
            }

            val divJson = JSONObject(refreshJson)

            runOnUiThread {
                try {
                    if (::div.isInitialized) {
                        binding.root.removeView(div)
                    }

                    if (!::divViewCreator.isInitialized) {
                        divViewCreator = UIDiv2ViewCreator(this, this, mehdiViewModel, this, null, bottomSheetManager)
                    }

                    try {
                        divViewCreator.updateBottomSheetManager(bottomSheetManager)
                    } catch (updateError: Exception) {
                        android.util.Log.e("MehdiActivity", "Failed to update BottomSheetManager during refresh", updateError)
                    }

                    div = divViewCreator.createDiv2ViewMehdi(
                        this,
                        divJson,
                        binding.root,
                        ScenarioLogDelegate.Stub
                    )
                    NotificationManager.setActionHandler(divViewCreator.actionHandler)
                    div.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        weight = 1F
                    }
                    binding.root.addView(div)
                    Constants.CURRENT_SCREEN = refresh
                } catch (viewError: Exception) {
                    android.util.Log.e("MehdiActivity", "Failed to refresh page for key=" + refresh, viewError)
                }
            }
        } catch (error: Exception) {
            android.util.Log.e("MehdiActivity", "refreshPage unexpected error for key=" + refresh, error)
        }
    }


    override fun onBackPressed() {
        android.util.Log.d("MehdiActivity", "=== onBackPressed() called ===")
        // Stop recording if it's currently active
        android.util.Log.d("MehdiActivity", "Stopping recording before activity destruction")
        stopRecording()
        super.onBackPressed()
    }

    override fun onDestroy() {
        android.util.Log.d("MehdiActivity", "=== onDestroy() called ===")
        // Stop recording if it's currently active
        android.util.Log.d("MehdiActivity", "Stopping recording before activity destruction")
        stopRecording()
        super.onDestroy()
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

    private val audioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            android.util.Log.d("MehdiActivity", "Permission result: $isGranted")
            if (isGranted) {
                onAudioPermissionGranted()
            } else {
                Toast.makeText(this, "دسترسی ضبط صدا رد شد", Toast.LENGTH_SHORT).show()
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
        // Bottom sheet is already managed by BottomSheetManager
        // This method is kept for compatibility but may not be needed anymore
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

    fun requestAudioPermissionAndStart(recordingId: String? = null) {
        android.util.Log.d("MehdiActivity", "=== requestAudioPermissionAndStart() called ===")
        android.util.Log.d("MehdiActivity", "Recording ID: $recordingId")
        pendingRecordingId = recordingId
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        android.util.Log.d("MehdiActivity", "Has RECORD_AUDIO permission: $hasPermission")
        
        if (!hasPermission) {
            android.util.Log.d("MehdiActivity", "Permission not granted, launching permission dialog")
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            android.util.Log.d("MehdiActivity", "Permission already granted, calling onAudioPermissionGranted()")
            onAudioPermissionGranted()
            android.util.Log.d("MehdiActivity", "Starting recording service")
            startRecordingService(recordingId)
        }
        android.util.Log.d("MehdiActivity", "=== requestAudioPermissionAndStart() completed ===")
    }


    fun startRecordingService(recordingId: String? = null) {
        android.util.Log.d("MehdiActivity", "=== startRecordingService() called ===")
        android.util.Log.d("MehdiActivity", "Recording ID: $recordingId")
        val intent = Intent(this, AudioRecordingService::class.java).apply {
            recordingId?.let { putExtra("RECORDING_ID", it) }
        }
        android.util.Log.d("MehdiActivity", "Created intent for AudioRecordingService with ID: $recordingId")
        android.util.Log.d("MehdiActivity", "Starting foreground service")
        ContextCompat.startForegroundService(this, intent)
        android.util.Log.d("MehdiActivity", "=== startRecordingService() completed ===")
    }

    override fun startRecording(recordingId: String?) {
        android.util.Log.d("MehdiActivity", "=== startRecording() called ===")
        android.util.Log.d("MehdiActivity", "Recording ID: $recordingId")
        android.util.Log.d("MehdiActivity", "Calling requestAudioPermissionAndStart()")
        requestAudioPermissionAndStart(recordingId)
        android.util.Log.d("MehdiActivity", "=== startRecording() completed ===")
    }

    override fun stopRecording() {
        android.util.Log.d("MehdiActivity", "=== stopRecording() called ===")
        val intent = Intent(this, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_STOP
        }
        android.util.Log.d("MehdiActivity", "Creating stop intent with action: ${AudioRecordingService.ACTION_STOP}")
        android.util.Log.d("MehdiActivity", "Starting service to stop recording")
        try {
            startService(intent)
            android.util.Log.d("MehdiActivity", "Service start command sent successfully")
        } catch (e: Exception) {
            android.util.Log.e("MehdiActivity", "Error starting service to stop recording: ${e.message}", e)
        }
        android.util.Log.d("MehdiActivity", "=== stopRecording() completed ===")
    }

    override fun onAudioPermissionGranted() {
        // This method can be used to notify AudioRecorderView that permission was granted
        // The actual implementation would depend on how you want to communicate with the view
        android.util.Log.d("MehdiActivity", "=== onAudioPermissionGranted() called ===")
        android.util.Log.d("MehdiActivity", "Pending recording ID: $pendingRecordingId")
        Toast.makeText(this, "مجوز ضبط صدا اعطا شد - حالا می‌توانید ضبط کنید", Toast.LENGTH_LONG).show()
        startRecordingService(pendingRecordingId)
        pendingRecordingId = null
    }

    override fun uploadRecording(recordingId: String?) {
        android.util.Log.d("MehdiActivity", "=== uploadRecording() called ===")
        android.util.Log.d("MehdiActivity", "Recording ID: $recordingId")
        
        if (recordingId == null) {
            android.util.Log.e("MehdiActivity", "Recording ID is null, cannot upload")
            Toast.makeText(this, "شناسه ضبط صدا مشخص نیست", Toast.LENGTH_LONG).show()
            return
        }
        
        // Start upload process in background
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("MehdiActivity", "Starting upload process for recording ID: $recordingId")
                Toast.makeText(this@MehdiActivity, "شروع آپلود فایل‌های ضبط شده...", Toast.LENGTH_LONG).show()
                
                // First stop recording and wait for file to be saved
                android.util.Log.d("MehdiActivity", "Stopping recording before upload")
                stopRecording()
                
                // Wait for recording to stop and file to be saved (2 seconds should be enough)
                android.util.Log.d("MehdiActivity", "Waiting for recording to stop and file to be saved...")
                delay(2000) // 2 second delay
                
                val result = withContext(Dispatchers.IO) {
                    uploadRecordingFiles(recordingId)
                }
                
                if (result.isNotEmpty()) {
                    android.util.Log.d("MehdiActivity", "Upload completed successfully. IDs: $result")
                    Toast.makeText(this@MehdiActivity, "آپلود با موفقیت انجام شد", Toast.LENGTH_LONG).show()
                } else {
                    android.util.Log.w("MehdiActivity", "No files found or uploaded for recording ID: $recordingId")
                    Toast.makeText(this@MehdiActivity, "فایل ضبط شده‌ای یافت نشد", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MehdiActivity", "Error during upload process", e)
                Toast.makeText(this@MehdiActivity, "خطا در آپلود: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        android.util.Log.d("MehdiActivity", "=== uploadRecording() completed ===")
    }
    
    private suspend fun uploadRecordingFiles(recordingId: String): String {
        android.util.Log.d("MehdiActivity", "=== uploadRecordingFiles() called ===")
        
        // Get token from database
        val token = mehdiViewModel?.getValueByKey("ph/token")?.value
        if (token.isNullOrEmpty()) {
            android.util.Log.e("MehdiActivity", "Token not found in database")
            return ""
        }
        
        android.util.Log.d("MehdiActivity", "Using token: $token")
        
        // Find recording files
        val recordingFiles = com.yandex.divkit.demo.utils.FileUploadUtil.findRecordingFiles(this, recordingId)
        android.util.Log.d("MehdiActivity", "Found ${recordingFiles.size} files to upload")
        
        val uploadedIds = mutableListOf<String>()
        
        // Upload each file
        for (file in recordingFiles) {
            try {
                android.util.Log.d("MehdiActivity", "Uploading file: ${file.name}")
                val uploadResult = com.yandex.divkit.demo.utils.FileUploadUtil.uploadFile(this, file, token)
                
                if (uploadResult.success && uploadResult.fileId != null) {
                    uploadedIds.add(uploadResult.fileId)
                    android.util.Log.d("MehdiActivity", "File uploaded successfully. ID: ${uploadResult.fileId}")
                } else {
                    android.util.Log.e("MehdiActivity", "Failed to upload file: ${file.name}. Error: ${uploadResult.error}")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MehdiActivity", "Error uploading file: ${file.name}", e)
            }
        }
        
        // Save uploaded IDs to database
        if (uploadedIds.isNotEmpty()) {
            val uploadedIdsString = uploadedIds.joinToString(",", "{", "}")
            val dbKey = "uploaded_recorded_${recordingId}"
            
            android.util.Log.d("MehdiActivity", "Saving uploaded IDs to database")
            android.util.Log.d("MehdiActivity", "DB Key: $dbKey")
            android.util.Log.d("MehdiActivity", "Uploaded IDs: $uploadedIdsString")
            
            mehdiViewModel?.insertItemToDb(com.yandex.divkit.demo.data.entities.PhPlusDB(null, dbKey, uploadedIdsString))
        }
        
        android.util.Log.d("MehdiActivity", "=== uploadRecordingFiles() completed ===")
        return uploadedIds.joinToString(",")
    }

    override fun uploadAndCallService(recordingId: String?, serviceParams: HashMap<String, String>?) {
        android.util.Log.d("MehdiActivity", "=== uploadAndCallService() called ===")
        android.util.Log.d("MehdiActivity", "Recording ID: $recordingId")
        android.util.Log.d("MehdiActivity", "Service params: $serviceParams")
        
        if (recordingId == null) {
            android.util.Log.e("MehdiActivity", "Recording ID is null, cannot upload and call service")
            Toast.makeText(this, "شناسه ضبط صدا مشخص نیست", Toast.LENGTH_LONG).show()
            return
        }
        
        // Start upload and call service process in background
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("MehdiActivity", "Starting upload and call service process for recording ID: $recordingId")
                Toast.makeText(this@MehdiActivity, "شروع آپلود فایل‌ها و فراخوانی سرویس...", Toast.LENGTH_LONG).show()
                
                // First stop recording and wait for file to be saved
                android.util.Log.d("MehdiActivity", "Stopping recording before upload")
                stopRecording()
                
                // Wait for recording to stop and file to be saved (2 seconds should be enough)
                android.util.Log.d("MehdiActivity", "Waiting for recording to stop and file to be saved...")
                delay(2000) // 2 second delay
                
                val uploadedIds = withContext(Dispatchers.IO) {
                    uploadRecordingFilesForService(recordingId)
                }
                
                if (uploadedIds.isNotEmpty()) {
                    android.util.Log.d("MehdiActivity", "Upload completed successfully. IDs: $uploadedIds")
                    
                    // Merge uploaded IDs with the provided service parameters
                    val finalServiceParams = serviceParams?.clone() as? HashMap<String, String> ?: HashMap()
                    
                    // Add the uploaded recording IDs to the service parameters
                    finalServiceParams["uploaded_recorded_${recordingId}"] = uploadedIds
                    
                    android.util.Log.d("MehdiActivity", "Final service params with uploaded IDs: $finalServiceParams")
                    android.util.Log.d("MehdiActivity", "Calling service with all params: $finalServiceParams")
                    onRequest(finalServiceParams)
                    
                    Toast.makeText(this@MehdiActivity, "آپلود و فراخوانی سرویس با موفقیت انجام شد", Toast.LENGTH_LONG).show()
                } else {
                    android.util.Log.w("MehdiActivity", "No files found or uploaded for recording ID: $recordingId")
                    
                    // Even if no files uploaded, still call the service with the provided parameters
                    if (serviceParams != null) {
                        android.util.Log.d("MehdiActivity", "No files uploaded, but calling service with provided params: $serviceParams")
                        onRequest(serviceParams)
                    }
                    
                    Toast.makeText(this@MehdiActivity, "فایل ضبط شده‌ای یافت نشد، اما سرویس فراخوانی شد", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MehdiActivity", "Error during upload and call service process", e)
                Toast.makeText(this@MehdiActivity, "خطا در آپلود و فراخوانی سرویس: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        android.util.Log.d("MehdiActivity", "=== uploadAndCallService() completed ===")
    }
    
    private suspend fun uploadRecordingFilesForService(recordingId: String): String {
        android.util.Log.d("MehdiActivity", "=== uploadRecordingFilesForService() called ===")
        
        // Get token from database
        val token = mehdiViewModel?.getValueByKey("ph/token")?.value
        if (token.isNullOrEmpty()) {
            android.util.Log.e("MehdiActivity", "Token not found in database")
            return ""
        }
        
        android.util.Log.d("MehdiActivity", "Using token: $token")
        
        // Find recording files
        val recordingFiles = com.yandex.divkit.demo.utils.FileUploadUtil.findRecordingFiles(this, recordingId)
        android.util.Log.d("MehdiActivity", "Found ${recordingFiles.size} files to upload")
        
        val uploadedIds = mutableListOf<String>()
        
        // Upload each file
        for (file in recordingFiles) {
            try {
                android.util.Log.d("MehdiActivity", "Uploading file: ${file.name}")
                val uploadResult = com.yandex.divkit.demo.utils.FileUploadUtil.uploadFile(this, file, token)
                
                if (uploadResult.success && uploadResult.fileId != null) {
                    uploadedIds.add(uploadResult.fileId)
                    android.util.Log.d("MehdiActivity", "File uploaded successfully. ID: ${uploadResult.fileId}")
                } else {
                    android.util.Log.e("MehdiActivity", "Failed to upload file: ${file.name}. Error: ${uploadResult.error}")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MehdiActivity", "Error uploading file: ${file.name}", e)
            }
        }
        
        // Save uploaded IDs to database
        if (uploadedIds.isNotEmpty()) {
            val uploadedIdsString = uploadedIds.joinToString(",", "{", "}")
            val dbKey = "uploaded_recorded_${recordingId}"
            
            android.util.Log.d("MehdiActivity", "Saving uploaded IDs to database")
            android.util.Log.d("MehdiActivity", "DB Key: $dbKey")
            android.util.Log.d("MehdiActivity", "Uploaded IDs: $uploadedIdsString")
            
            mehdiViewModel?.insertItemToDb(com.yandex.divkit.demo.data.entities.PhPlusDB(null, dbKey, uploadedIdsString))
            
            android.util.Log.d("MehdiActivity", "=== uploadRecordingFilesForService() completed ===")
            return uploadedIdsString
        }
        
        android.util.Log.d("MehdiActivity", "=== uploadRecordingFilesForService() completed ===")
        return ""
    }




}
