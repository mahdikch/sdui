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
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import android.webkit.MimeTypeMap
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.div.core.view2.Div2View
import com.yandex.div.internal.Log
import com.yandex.divkit.demo.R
import com.yandex.divkit.demo.applock.AppLockActivity
import com.yandex.divkit.demo.applock.AppLockSession
import com.yandex.divkit.demo.applock.AppLockStore
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
import com.yandex.divkit.demo.utils.ApkDownloader
import com.yandex.divkit.demo.utils.AccidentRegisterDateTimeHelper
import com.yandex.divkit.demo.utils.ScreenNavigationStack
import com.yandex.divkit.demo.utils.VehicleEntryCountHelper
import com.yandex.divkit.demo.utils.VehicleWizardDraftResolver
import com.yandex.divkit.demo.ui.toastDiv.CustomToast
import com.yandex.divkit.demo.utils.ErrorDialog
import com.yandex.divkit.demo.utils.LoadingDialog
import com.yandex.divkit.demo.utils.LoginDeviceInfoHelper
import com.yandex.divkit.demo.utils.SingletonObjects
import com.yandex.divkit.demo.push.PushHelper
import com.yandex.divkit.demo.div.notificationList.NotificationManager
import com.yandex.divkit.regression.ScenarioLogDelegate
import ir.naja.pushsdk.android.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.UUID


//@AndroidEntryPoint
class MehdiActivity : AppCompatActivity(), LoadScreenListener,
    com.yandex.divkit.demo.div.notificationList.NotificationManager.NotificationListener {
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

    // Variables for notification service caching
    private var pendingNotificationMode: String? = null
    private var pendingNotificationPath: String? = null
    private var pendingNotificationRequestId: String? = null

    // Debounce mechanism for notification count updates
    private var lastNotificationUpdateTime: Long = 0
    private val NOTIFICATION_UPDATE_DEBOUNCE_MS = 100L // 100ms debounce

    // Flag to prevent observer updates when we're updating from callback
    private var isUpdatingFromCallback: Boolean = false
    private lateinit var observerRemoteData: Observer<HashMap<String, String>>
    private lateinit var observerScreenToLoad: Observer<String>
    private lateinit var observerVariableToSet: Observer<String>
    private lateinit var observerVariableToGet: Observer<String>

    // لیست برای ذخیره متغیرهای شامل "variable"
    private var varlist: ArrayList<String> = arrayListOf()
    private lateinit var div: Div2View
    private var bottomSheetManager: BottomSheetManager? = null
    private var exitConfirmSheet: BottomSheetDialog? = null
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

        if (!AppLockSession.unlockedThisProcess) {
            val lockStore = AppLockStore(this)
            if (lockStore.isConfigured) {
                startActivity(AppLockActivity.unlockIntent(this))
                finish()
                return
            }
        }

        // Register as notification listener to get real-time updates
        NotificationManager.addListener(this)

        getAllPermissions()
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

            // Prefer dismissing an open bottom sheet before navigating back
            val openSheet = bottomSheetManager?.getCurrentBottomSheet()
            if (openSheet != null) {
                android.util.Log.d("MehdiActivity", "Dismissing open bottom sheet on back")
                openSheet.dismiss()
                return@addCallback
            }

            val stack = if (::mehdiViewModel.isInitialized) {
                ScreenNavigationStack.readStack(mehdiViewModel)
            } else {
                emptyList()
            }
            val currentScreen = stack.lastOrNull()
                ?: Constants.CURRENT_SCREEN.takeIf { it.isNotBlank() }

            android.util.Log.d(
                "MehdiActivity",
                "Back press: current=$currentScreen stack=$stack"
            )

            // Exit sheet only on main / auth roots — never on nested screens.
            if (ScreenNavigationStack.shouldShowExitConfirm(currentScreen)) {
                showExitConfirmBottomSheet()
                return@addCallback
            }

            if (!::mehdiViewModel.isInitialized) {
                showExitConfirmBottomSheet()
                return@addCallback
            }

            if (stack.size >= 2) {
                android.util.Log.d("MehdiActivity", "Stopping recording before back navigation")
                stopRecording()
                val popResult = ScreenNavigationStack.pop(mehdiViewModel)
                val previousScreen = popResult.previousScreen
                android.util.Log.d(
                    "MehdiActivity",
                    "Back pop result: previous=$previousScreen stack=${popResult.stack}"
                )
                if (previousScreen != null) {
                    onLoadFinishingCurrent(previousScreen)
                } else {
                    ScreenNavigationStack.resetToMain(mehdiViewModel)
                    onLoadFinishingCurrent(ScreenNavigationStack.MAIN_SCREEN)
                }
            } else {
                // Nested screen with broken/empty stack — recover to main, do not exit.
                android.util.Log.d(
                    "MehdiActivity",
                    "Broken stack on non-main screen; recovering to main"
                )
                stopRecording()
                ScreenNavigationStack.resetToMain(mehdiViewModel)
                onLoadFinishingCurrent(ScreenNavigationStack.MAIN_SCREEN)
            }
        }

        val intent = intent
        val bundle = intent.extras
        var json = bundle?.getString("json")

        val open_push = intent.getStringExtra("div_action_open_push")
        if (open_push == "open") {
            json = "ph/notification"
        }


        val sysname = bundle?.getString("sysName")
        binding = ActivityMehdiBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        checkAndRequestPermissions()
        loading = LoadingDialog(this)
        error = ErrorDialog(this)

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

        if (!AppLockSession.unlockedThisProcess) {
            val lockStore = AppLockStore(this)
            val sessionUser = try {
                mehdiViewModel.getValueByKey("userName")?.value.orEmpty()
            } catch (_: Exception) {
                ""
            }
            if (!lockStore.isConfigured && sessionUser.isNotBlank()) {
                if (maybeRequireAppLockSetup()) return
            }
            // No lock yet and no session — allow splash/login without blocking.
            AppLockSession.markUnlocked()
        }

        // Initialize NotificationManager with database access and action handler
        // Note: actionHandler will be available after divViewCreator is created
        NotificationManager.initialize(mehdiViewModel, null)
        PushHelper.setupPushNotifications(this)

        // تنظیم امنیت صفحه (جلوگیری از گرفتن اسکرین‌شات)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_SECURE,
//            WindowManager.LayoutParams.FLAG_SECURE
//        )
//        if (isDeviceRooted()) {
//            Toast.makeText(this, "دستگاه شما روت است", Toast.LENGTH_LONG).show()
//            finishAffinity() // close the app
//        }
        setupObservers()

        var path = ""
        var divJson = JSONObject()


        if (json == null) {
            // Fresh app launch — drop stale navigation stack so startPoint can route correctly.
            ScreenNavigationStack.clear(mehdiViewModel)
            // تولید phid جدید برای دستگاه
            var phId = UUID.randomUUID().toString()
            mehdiViewModel.insertItemToDb(PhPlusDB(null, "phid", phId))
            var testForPatchGeneratorTemplate =
                assetReader.read("application/testForPatchGeneratorTemplate.json")

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
                    "testForPatchGeneratorTemplate",
                    testForPatchGeneratorTemplate.toString()
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
//                Configuration.ORIENTATION_PORTRAIT -> "application/test2.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/temp.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/switch.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/patchTest.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/main.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/cargo_getCmrInfo.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/testBaseMain.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/login.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/vt-register-ticket.json"
                Configuration.ORIENTATION_PORTRAIT -> "application/startPoint.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/testStartPoint146.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/sabte-takhalof.json"
//                Configuration.ORIENTATION_PORTRAIT -> "application/navigation.json"
//                Configuration.ORIENTATION_LANDSCAPE -> "application/login.json"
                else -> "application/startPoint.json"
            }
            divViewCreator =
                UIDiv2ViewCreator(this, this, mehdiViewModel, this, null, bottomSheetManager)

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

            // Cache container definitions for dynamic patching
            cacheContainerDefinitionsFromAsset(path)

        } else {
            val contentKey = VehicleWizardDraftResolver.resolveContentKey(json, mehdiViewModel)
            nextJsonDto = repository.getValueByKey(contentKey)
            if (nextJsonDto == null && contentKey != json) {
                nextJsonDto = repository.getValueByKey(json)
            }
            var screenJson = nextJsonDto?.value
            if (screenJson.isNullOrBlank()) {
                screenJson = loadScreenJsonFromAssets(json)
                if (!screenJson.isNullOrBlank()) {
                    mehdiViewModel.insertItemToDbSync(PhPlusDB(null, json, screenJson))
                    android.util.Log.d("MehdiActivity", "Loaded screen from assets and cached: $json")
                }
            }
            if (!screenJson.isNullOrBlank()) {
                nextJson = screenJson
                divJson = JSONObject(nextJson)

                divViewCreator =
                    UIDiv2ViewCreator(this, this, mehdiViewModel, this, null, bottomSheetManager)

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

                // Cache container definitions for dynamic patching
                cacheContainerDefinitionsFromJson(divJson)
                syncVehicleEntryCountIfNeeded(json)
                AccidentRegisterDateTimeHelper.applyIfNeeded(json, div)

            }
        }
        if (json != null) {
            syncScreenStackWithCurrentScreen(json)
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
                        var phId: String = mehdiViewModel.getValueByKey("phid")?.value.orEmpty()
                        mehdiViewModel.setphPlusRequest(phId, map)

                    }

                }
        }
        RemoteData.value.observe(this, observerRemoteData)

        var next = ""

        observerScreenToLoad = Observer {
            if (it.isNotEmpty()) {
                val next = mehdiViewModel.getValueByKey(it)?.value.orEmpty()
                if (next.isNotEmpty()) {
                    onLoad(next)
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
        android.util.Log.d("MehdiActivity", "=== onResume() called ===")

        // Continue APK install after user grants unknown-sources permission (no re-download).
        ApkDownloader.onHostResumed(this)

        // Update actionHandler in NotificationManager to ensure it points to current Activity
        if (::divViewCreator.isInitialized) {
            NotificationManager.setActionHandler(divViewCreator.actionHandler)
            android.util.Log.d("MehdiActivity", "ActionHandler updated in onResume")
        }

        // Re-setup notification count observers in case div was recreated
        if (::div.isInitialized) {
            setupNotificationCountObservers()
        }

        android.util.Log.d("MehdiActivity", "=== onResume() completed ===")
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d("MehdiActivity", "=== onPause() called ===")
        // Note: Recording service runs in foreground and should continue even when activity pauses
        // Only stop recording when activity is actually destroyed (onDestroy) or user explicitly stops
        android.util.Log.d(
            "MehdiActivity",
            "Activity paused - recording service continues in foreground"
        )
        RemoteData.value.removeObserver(observerRemoteData)
        ScreenToLoad.value.removeObserver(observerScreenToLoad)
        VariableToGet.value.removeObserver(observerVariableToGet)
    }

    override fun onStop() {
        super.onStop()
        android.util.Log.d("MehdiActivity", "=== onStop() called ===")
        // Note: Recording service runs in foreground and should continue even when activity stops
        // Only stop recording when activity is actually destroyed (onDestroy) or user explicitly stops
        android.util.Log.d(
            "MehdiActivity",
            "Activity stopped - recording service continues in foreground"
        )
    }

    fun setVariable(key: String, value: String) {
        try {
            if (::div.isInitialized) {
                android.util.Log.d("MehdiActivity", "Setting variable in DivKit: $key = $value")
                div.setVariable(key, value)
                android.util.Log.d("MehdiActivity", "Variable set successfully: $key = $value")
            } else {
                android.util.Log.w(
                    "MehdiActivity",
                    "Div not initialized, cannot set variable: $key = $value"
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("MehdiActivity", "Error setting variable: $key = $value", e)
            e.printStackTrace()
        }
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

                // Setup notification count observers
                setupNotificationCountObservers()

                // You can parse the push message and load specific JSON screen
                // Example: If push message contains JSON screen name
                // val payload = Gson().fromJson(pushMessage, YourPayloadClass::class.java)
                // loadScreen(payload.screenName)

                // Ensure Toast runs on main thread
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Push notification received: $pushMessage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MehdiActivity", "Error handling push notification data", e)
        }
    }

    /**
     * Setup observers for notification count variables
     * This will automatically update DivKit variables when notification counts change in database
     */
    private fun setupNotificationCountObservers() {
        if (!::mehdiViewModel.isInitialized) {
            return
        }
        try {
            android.util.Log.d("MehdiActivity", "=== Setting up notification count observers ===")

            // Observer for total unread notifications count
            mehdiViewModel.getPage("unread_notifications_count").observe(this) {
                // Skip if we're updating from callback (to prevent duplicate/old values)
                if (isUpdatingFromCallback) {
                    android.util.Log.d(
                        "MehdiActivity",
                        "Skipping observer update for unread_notifications_count (updating from callback)"
                    )
                    return@observe
                }

                if (it.isNotEmpty()) {
                    val value = it[0].value
                    if (value != null && value.isNotEmpty()) {
                        android.util.Log.d(
                            "MehdiActivity",
                            "Updating unread_notifications_count variable from observer: $value"
                        )
                        setVariable("unread_notifications_count", value)
                    }
                }
            }

            // Observer for each notification type count
            // Load notification basic info to get all available types
            val basicInfoJson = mehdiViewModel.getValueByKey("notificationBasicInfo")?.value
            if (basicInfoJson != null && basicInfoJson.isNotEmpty()) {
                try {
                    val gson = Gson()
                    val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                    val basicInfo = gson.fromJson<List<Map<String, Any>>>(basicInfoJson, listType)

                    for (typeInfo in basicInfo) {
                        val typeId = typeInfo["id"]?.toString() ?: continue
                        val key = "unread_notifications_count_type_$typeId"

                        android.util.Log.d("MehdiActivity", "Setting up observer for: $key")
                        mehdiViewModel.getPage(key).observe(this) {
                            // Skip if we're updating from callback (to prevent duplicate/old values)
                            if (isUpdatingFromCallback) {
                                android.util.Log.d(
                                    "MehdiActivity",
                                    "Skipping observer update for $key (updating from callback)"
                                )
                                return@observe
                            }

                            if (it.isNotEmpty()) {
                                val value = it[0].value
                                if (value != null && value.isNotEmpty()) {
                                    android.util.Log.d(
                                        "MehdiActivity",
                                        "Updating $key variable from observer: $value"
                                    )
                                    setVariable(key, value)
                                }
                            }
                        }
                    }
                    android.util.Log.d(
                        "MehdiActivity",
                        "Set up observers for ${basicInfo.size} notification types"
                    )
                } catch (e: Exception) {
                    android.util.Log.e("MehdiActivity", "Error parsing notificationBasicInfo", e)
                }
            } else {
                android.util.Log.d(
                    "MehdiActivity",
                    "notificationBasicInfo not found, setting up default observers"
                )
                // Set up observer for type_3 as fallback
                mehdiViewModel.getPage("unread_notifications_count_type_3").observe(this) {
                    // Skip if we're updating from callback (to prevent duplicate/old values)
                    if (isUpdatingFromCallback) {
                        android.util.Log.d(
                            "MehdiActivity",
                            "Skipping observer update for unread_notifications_count_type_3 (updating from callback)"
                        )
                        return@observe
                    }

                    if (it.isNotEmpty()) {
                        val value = it[0].value
                        if (value != null && value.isNotEmpty()) {
                            android.util.Log.d(
                                "MehdiActivity",
                                "Updating unread_notifications_count_type_3 variable from observer: $value"
                            )
                            setVariable("unread_notifications_count_type_3", value)
                        }
                    }
                }
            }

            android.util.Log.d(
                "MehdiActivity",
                "=== Notification count observers setup completed ==="
            )
        } catch (e: Exception) {
            android.util.Log.e("MehdiActivity", "Error setting up notification count observers", e)
        }
    }

    private fun setupObservers() {

        mehdiViewModel.phPlus.observe(this) {
            android.util.Log.d("MehdiActivity", "=== phPlus observer triggered ===")
            android.util.Log.d("MehdiActivity", "Status: ${it.status}")
            when (it.status) {




                Resource.Status.SUCCESS -> {
                    android.util.Log.d("MehdiActivity", "SUCCESS: Processing response")
                    lifecycleScope.launch {
                        Log.e("SUCCESS", "start")
                        val varList = java.util.HashMap(it.data ?: emptyMap())

                        if (pendingNotificationMode == "check_db_call_service" &&
                            pendingNotificationPath != null &&
                            pendingNotificationRequestId != null
                        ) {
                            try {
                                val dbKey =
                                    "notification_service_response_${pendingNotificationPath}_${pendingNotificationRequestId}"
                                val responseJson = com.google.gson.Gson().toJson(varList)

                                android.util.Log.d(
                                    "MehdiActivity",
                                    "Caching notification service response"
                                )
                                android.util.Log.d("MehdiActivity", "DB Key: $dbKey")
                                android.util.Log.d("MehdiActivity", "Response: $responseJson")

                                withContext(Dispatchers.IO) {
                                    repository.insertItem(
                                        com.yandex.divkit.demo.data.entities.PhPlusDB(
                                            null,
                                            dbKey,
                                            responseJson
                                        )
                                    )
                                }

                                android.util.Log.d(
                                    "MehdiActivity",
                                    "Notification service response cached successfully"
                                )
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "MehdiActivity",
                                    "Error caching notification service response",
                                    e
                                )
                            }

                            // Clear pending notification variables
                            pendingNotificationMode = null
                            pendingNotificationPath = null
                            pendingNotificationRequestId = null
                        }
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
                        var show_pdf = ""
                        var show_image = ""
                        var initializePushNotifications = ""
                        var initializeLocationService = ""
                        var clearNotificationPrefs = ""
                        var closeBottomSheet = false

                        if (varList.containsKey("next")) {
                            val nv = varList["next"]
                            if (nv != null) {
                                varList.remove("next")
                                varList["next"] = nv
                            }
                        }
                        if (varList.containsKey("userName")) {
                            try {
                                val userName = varList["userName"]
                                if (userName != null && userName.isNotEmpty()) {
                                    val previousUsername = Constant.USERNAME
                                    Constant.USERNAME = userName
                                    android.util.Log.d(
                                        "MehdiActivity",
                                        "Constant.USERNAME updated to: $userName"
                                    )
                                    com.yandex.divkit.demo.div.notificationList.NotificationManager
                                        .onUserChanged(userName)
                                    if (previousUsername != userName) {
                                        PushHelper.setupPushNotifications(this@MehdiActivity)
                                    }
                                } else {
                                    android.util.Log.w(
                                        "MehdiActivity",
                                        "userName is empty, not updating Constant.USERNAME"
                                    )
                                }
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "MehdiActivity",
                                    "Error updating Constant.USERNAME: ${e.message}",
                                    e
                                )
                            }
                        }

                        val pendingInserts = mutableListOf<PhPlusDB>()

                        val handledGuesthouseDay =
                            com.yandex.divkit.demo.ui.UIDiv2ActionHandler
                                .handleGuesthouseServiceResponse(varList)
                        if (handledGuesthouseDay) {
                            varList.remove("id")
                            varList.remove("capacity")
                            varList.remove("reserveId")
                        }

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
                                    "show_pdf" -> show_pdf = value
                                    "show_image" -> show_image = value
                                    "initializePushNotifications" -> initializePushNotifications =
                                        value

                                    "initializeLocationService" -> initializeLocationService = value
                                    "clearNotificationPrefs" -> clearNotificationPrefs = value
                                    "close_bottom_sheet" -> closeBottomSheet =
                                        shouldCloseBottomSheet(value)

                                    else -> pendingInserts.add(PhPlusDB(null, key, value))
                                }

                                // Handle bottom_sheet_variable first (before general variable check)
                                if (key.contains("bottom_sheet_variable")) {
                                    println("MehdiActivity: Processing bottom_sheet_variable key=$key, value=$value")
                                    val manager = bottomSheetManager
                                    if (manager != null) {
                                        // Set the variable with its full name on current bottom sheet
                                        val success =
                                            manager.setVariableOnTarget("current", key, value)
                                        println("MehdiActivity: Variable setting on current bottom sheet, success=$success")
                                    } else {
                                        println("MehdiActivity: bottomSheetManager is null, skipping variable setting")
                                    }
                                } else if (key.contains("variable")) {
                                    // Only set on main div if it's NOT a bottom_sheet_variable
                                    div.setVariable(key, value)
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

                        // Flush remaining keys (including userName) before lock gate.
                        if (pendingInserts.isNotEmpty()) {
                            withContext(Dispatchers.IO) {
                                pendingInserts.forEach { repository.insertItem(it) }
                            }
                            pendingInserts.clear()
                        }

                        val goingToApp =
                            next.contains("main", ignoreCase = true) ||
                                next.equals("ph/main", ignoreCase = true) ||
                                varList.containsKey("userName")
                        if (goingToApp &&
                            hasLoggedInSession(varList["userName"]) &&
                            maybeRequireAppLockSetup()
                        ) {
                            return@launch
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
                                mehdiViewModel?.let {
                                    CustomToast(
                                        this@MehdiActivity,
                                        this@MehdiActivity,
                                        it,
                                        this@MehdiActivity
                                    )
                                }
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
                            val sheetJson = varList[bottomSheet]
                            android.util.Log.d(
                                "MehdiActivity",
                                "bottomSheet key=$bottomSheet jsonPresent=${!sheetJson.isNullOrBlank()} jsonLen=${sheetJson?.length ?: 0} responseKeys=${varList.keys}"
                            )
                            println("bottomSheet1:${bottomSheet}")
                            println("MehdiActivity: Creating bottom sheet with manager - bottomSheetManager is null? ${bottomSheetManager == null}")
                            showBottomSheetFromResponse(bottomSheet, sheetJson)
                        }
                        if (closeBottomSheet) {
                            dismissOpenBottomSheet()
                        }
                        if (dialog.isNotBlank()) {
                            DialogDiv(
                                this@MehdiActivity,
                                this@MehdiActivity,
                                bottomSheet,
                                mehdiViewModel,
                                this@MehdiActivity
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
                            val dbPatch = withContext(Dispatchers.IO) {
                                mehdiViewModel.getValueByKey(bottomSheetPatch)
                            }
                            val json = dbPatch?.value.orEmpty()
                            if (json.isNotEmpty()) {
                                bottomSheetManager?.getCurrentBottomSheet()
                                    ?.onApplyOnbase(json, bottomSheetPatch, "", "")
                            }
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

                        if (show_pdf != "") {
                            showPdfFromBase64(show_pdf)
                        }

                        if (show_image != "") {
                            showImageFromBase64(show_image)
                        }

                        // Handle initializePushNotifications
                        if (initializePushNotifications != "") {
                            try {
                                val app = com.yandex.divkit.demo.DivkitApplication.getInstance()
                                if (initializePushNotifications.lowercase() == "true") {
                                    android.util.Log.d(
                                        "MehdiActivity",
                                        "Starting PushNotifications service"
                                    )
                                    app?.initializePushNotifications()
                                    com.yandex.divkit.demo.push.PushHelper.setupPushNotifications(
                                        this@MehdiActivity
                                    )
                                } else if (initializePushNotifications.lowercase() == "false") {
                                    android.util.Log.d(
                                        "MehdiActivity",
                                        "Stopping PushNotifications service"
                                    )
                                    app?.stopPushNotifications()
                                }
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "MehdiActivity",
                                    "Error handling initializePushNotifications: ${e.message}",
                                    e
                                )
                            }
                        }

                        // Handle clearNotificationPrefs → clear prefs + stop receiving
                        if (clearNotificationPrefs.isNotBlank() &&
                            clearNotificationPrefs.lowercase() == "true"
                        ) {

                            try {
                                android.util.Log.d(
                                    "MehdiActivity",
                                    "clearNotificationPrefs=true → clearing prefs and stopping push receiving"
                                )
                                com.yandex.divkit.demo.DivkitApplication.getInstance()
                                    ?.clearNotificationPrefsAndStopReceiving()
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "MehdiActivity",
                                    "Error handling clearNotificationPrefs: ${e.message}",
                                    e
                                )
                            }
                        }

                        // Handle initializeLocationService
                        if (initializeLocationService != "") {
                            try {
                                val app = com.yandex.divkit.demo.DivkitApplication.getInstance()
                                if (initializeLocationService.lowercase() == "true") {
                                    android.util.Log.d("MehdiActivity", "Starting LocationService")
                                    app?.startLocationService()
                                } else if (initializeLocationService.lowercase() == "false") {
                                    android.util.Log.d("MehdiActivity", "Stopping LocationService")
                                    app?.stopLocationService()
                                }
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "MehdiActivity",
                                    "Error handling initializeLocationService: ${e.message}",
                                    e
                                )
                            }
                        }

                        if (pendingInserts.isNotEmpty()) {
                            repository.insertListAsync(pendingInserts)
                        }
                        loading.dismissDialog()
                        Log.e("SUCCESS", "end")
                    }
                }

                Resource.Status.ERROR -> {
                    android.util.Log.e("MehdiActivity", "ERROR: ${it.message}")
                    com.yandex.divkit.demo.ui.UIDiv2ActionHandler.clearPendingGuesthouseDayOp()
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
                    android.util.Log.d("MehdiActivity", "LOADING: Service request in progress")
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
                android.util.Log.w(
                    "MehdiActivity",
                    "No JSON found in DB for refresh key=" + refresh
                )
                return
            }

            val divJson = JSONObject(refreshJson)

            runOnUiThread {
                try {
                    if (::div.isInitialized) {
                        binding.root.removeView(div)
                    }

                    if (!::divViewCreator.isInitialized) {
                        divViewCreator = UIDiv2ViewCreator(
                            this,
                            this,
                            mehdiViewModel,
                            this,
                            null,
                            bottomSheetManager
                        )
                    }

                    try {
                        divViewCreator.updateBottomSheetManager(bottomSheetManager)
                    } catch (updateError: Exception) {
                        android.util.Log.e(
                            "MehdiActivity",
                            "Failed to update BottomSheetManager during refresh",
                            updateError
                        )
                    }

                    div = divViewCreator.createDiv2ViewMehdi(
                        this,
                        divJson,
                        binding.root,
                        ScenarioLogDelegate.Stub
                    )
                    // Always update actionHandler in NotificationManager when div is recreated
                    NotificationManager.setActionHandler(divViewCreator.actionHandler)
                    android.util.Log.d(
                        "MehdiActivity",
                        "ActionHandler updated in NotificationManager during refresh"
                    )
                    div.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        weight = 1F
                    }
                    binding.root.addView(div)
                    Constants.CURRENT_SCREEN = refresh
                    AccidentRegisterDateTimeHelper.applyIfNeeded(refresh, div)
                } catch (viewError: Exception) {
                    android.util.Log.e(
                        "MehdiActivity",
                        "Failed to refresh page for key=" + refresh,
                        viewError
                    )
                }
            }
        } catch (error: Exception) {
            android.util.Log.e(
                "MehdiActivity",
                "refreshPage unexpected error for key=" + refresh,
                error
            )
        }
    }


    private fun showExitConfirmBottomSheet() {
        if (isFinishing || isDestroyed) return
        if (exitConfirmSheet?.isShowing == true) return

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_exit_confirm, null)
        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        view.findViewById<android.view.View>(R.id.btnExitCancel).setOnClickListener {
            dialog.dismiss()
        }
        view.findViewById<android.view.View>(R.id.btnExitConfirm).setOnClickListener {
            dialog.dismiss()
            android.util.Log.d("MehdiActivity", "Exit confirmed by user")
            stopRecording()
            finishAffinity()
        }
        dialog.setOnDismissListener {
            if (exitConfirmSheet === dialog) {
                exitConfirmSheet = null
            }
        }
        exitConfirmSheet = dialog
        dialog.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        android.util.Log.d("MehdiActivity", "=== onBackPressed() called ===")
        // Always delegate to the dispatcher so our registered callback runs.
        // Do not skip this when callbacks exist — otherwise Back does nothing.
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroy() {
        android.util.Log.d("MehdiActivity", "=== onDestroy() called ===")
        // Stop recording if it's currently active
        android.util.Log.d("MehdiActivity", "Stopping recording before activity destruction")
        stopRecording()

        // Unregister notification listener
        NotificationManager.removeListener(this)

        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle ImagePickerView results
        if (requestCode == com.yandex.divkit.demo.div.imagePickerView.ImagePickerView.REQUEST_CODE_GALLERY ||
            requestCode == com.yandex.divkit.demo.div.imagePickerView.ImagePickerView.REQUEST_CODE_CAMERA
        ) {
            com.yandex.divkit.demo.div.imagePickerView.ImagePickerView.handleActivityResult(
                requestCode,
                resultCode,
                data
            )
        }

        if (requestCode == com.yandex.divkit.demo.div.accidentSketch.AccidentSketchView.REQUEST_CODE) {
            com.yandex.divkit.demo.div.accidentSketch.AccidentSketchView.handleActivityResult(
                requestCode,
                resultCode,
                data
            )
        }

        com.yandex.divkit.demo.ui.UIDiv2ActionHandler.handleKashfFaceCameraResult(
            requestCode,
            resultCode,
            data
        )
        com.yandex.divkit.demo.ui.UIDiv2ActionHandler.handleKashfFacePlateResult(
            requestCode,
            resultCode,
            data
        )
    }

    private fun startActivityForLoad(klass: Class<out Activity>, jsonName: String) {
        if (klass == MehdiActivity::class.java) {
            if (jsonName != "ph/splash") {
                if (jsonName.contains("main", ignoreCase = true) &&
                    hasLoggedInSession() &&
                    maybeRequireAppLockSetup()
                ) {
                    return
                }
                loadScreenFromServer(jsonName)
                return
            }
        }
        val intent = Intent(this, klass)
        intent.putExtra("json", jsonName)
        ContextCompat.startActivity(this, intent, null)
    }

    /** Server `next` must always load the target screen, even if stack already has it on top. */
    private fun loadScreenFromServer(screenName: String) {
        val resolved = resolveScreenName(screenName)
        if (resolved.isEmpty()) return
        val pushResult = ScreenNavigationStack.push(mehdiViewModel, resolved, forceNavigate = true)
        if (pushResult.shouldNavigate) {
            onLoadFinishingCurrent(pushResult.targetScreen)
        }
    }

    // ریست اکتیویتی و لود صفحه جدید
    private fun resetActivityForLoad(klass: Class<out Activity>, jsonName: String) {
        if (klass == MehdiActivity::class.java) {
            ScreenNavigationStack.clear(mehdiViewModel)
            onLoad(jsonName)
            return
        }
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
        val pushResult = ScreenNavigationStack.push(mehdiViewModel, resolveScreenName(screenName))
        if (pushResult.shouldNavigate) {
            onLoadFinishingCurrent(pushResult.targetScreen)
        }
    }

    override fun onLoadFinishingCurrent(screenName: String) {
        val next = resolveScreenName(screenName)
        if (next.isEmpty()) return
        if (next.contains("main", ignoreCase = true) &&
            hasLoggedInSession() &&
            maybeRequireAppLockSetup()
        ) {
            return
        }
        Constants.CURRENT_SCREEN = next
        mehdiViewModel.insertItemToDb(PhPlusDB(null, "currentScreen", next))
        val intent = Intent(this, MehdiActivity::class.java)
        intent.putExtra("json", next)
        startActivity(intent)
        finish()
    }

    private fun syncScreenStackWithCurrentScreen(screenKey: String) {
        val resolved = resolveScreenName(screenKey)
        if (resolved.isEmpty()) return
        Constants.CURRENT_SCREEN = resolved
        mehdiViewModel.insertItemToDb(PhPlusDB(null, "currentScreen", resolved))
        val stack = ScreenNavigationStack.readStack(mehdiViewModel)
        if (stack.isEmpty()) {
            val seeded = if (ScreenNavigationStack.isAuthScreen(resolved)) {
                listOf(resolved)
            } else if (ScreenNavigationStack.isMainScreen(resolved)) {
                listOf(ScreenNavigationStack.MAIN_SCREEN)
            } else {
                listOf(ScreenNavigationStack.MAIN_SCREEN, resolved)
            }
            ScreenNavigationStack.saveStack(mehdiViewModel, seeded)
            android.util.Log.d("MehdiActivity", "Seeded screen stack with: $seeded")
        } else if (!ScreenNavigationStack.isAuthScreen(resolved) &&
            !ScreenNavigationStack.isMainScreen(resolved) &&
            stack.none { ScreenNavigationStack.isMainScreen(it) }
        ) {
            val fixed = stack.toMutableList()
            fixed.add(0, ScreenNavigationStack.MAIN_SCREEN)
            ScreenNavigationStack.saveStack(mehdiViewModel, fixed)
            android.util.Log.d("MehdiActivity", "Inserted main under stack: $fixed")
        }
    }

    private fun resolveScreenName(screenName: String): String {
        if (screenName.isEmpty()) return ""
        return if (screenName == "nextSplash") {
            mehdiViewModel.getValueByKey(screenName)?.value.orEmpty()
        } else {
            screenName
        }
    }

    /** Resolve local SDUI screens from assets when not yet present in PhPlus DB. */
    private fun loadScreenJsonFromAssets(name: String?): String? {
        if (name.isNullOrBlank()) return null
        val underscored = name.replace('/', '_')
        val folder = name.substringBefore('/', missingDelimiterValue = "")
        val leaf = name.substringAfter('/', missingDelimiterValue = name)
        val candidates = ArrayList<String>().apply {
            add("application/$name.json")
            add("application/${underscored}.json")
            if (folder.isNotBlank()) {
                add("application/$folder/${folder}_$leaf.json")
                add("application/$folder/$underscored.json")
                add("application/$folder/$leaf.json")
            }
            add("application/ph/${underscored}.json")
        }
        for (path in candidates) {
            val json = runCatching {
                assets.open(path).bufferedReader().use { it.readText() }
            }.getOrNull()
            if (!json.isNullOrBlank()) return json
        }
        return null
    }

    private fun syncVehicleEntryCountIfNeeded(screenKey: String?) {
        val resolved = screenKey?.let { resolveScreenName(it) }.orEmpty()
        if (resolved != "Accidents/vehicleInformationEntry" &&
            resolved != "Accidents/unConpromised/VehicleInformationEntry"
        ) {
            return
        }
        if (!::div.isInitialized) return
        VehicleEntryCountHelper.sync(mehdiViewModel, div, null)
    }


    override fun onRequest(map: HashMap<String, String>) {
        android.util.Log.d("MehdiActivity", "=== onRequest called ===")
        android.util.Log.d("MehdiActivity", "Request map: $map")
        varlist.clear()

        // Check if this is a cached response (from check_db_call_service mode)
        val isCachedResponse = map["_cached_response"] == "true"
        if (isCachedResponse) {
            // Remove the marker
            map.remove("_cached_response")
            // Process the cached response directly without sending to server
            processServiceResponseDirectly(map)
            return
        }

        // Check if this is a notification service request that needs caching
        pendingNotificationMode = map["_notification_mode"]
        pendingNotificationPath = map["path"]
        pendingNotificationRequestId = map["_notification_request_id"]

        // Remove internal notification keys from request before sending to server
        map.remove("_notification_mode")
        map.remove("_notification_request_id")
        map.remove("_notification_id")

        if (map.isNotEmpty()) {
            val phId = prepareCoordinatorRequestMap(map)
            android.util.Log.d(
                "MehdiActivity",
                "Calling mehdiViewModel.setphPlusRequest with phId: $phId"
            )
            android.util.Log.d("MehdiActivity", "Request map after processing: $map")
            if (map["newRequestId"].toString().isNotEmpty() && map["newRequestId"].toString()!="null")
                map["requestId"] = map["newRequestId"].toString()
            mehdiViewModel.setphPlusRequest(phId, map)
            android.util.Log.d(
                "MehdiActivity",
                "setphPlusRequest called, waiting for observer response"
            )
             println(map.toString())
        }

    }

    private fun prepareCoordinatorRequestMap(map: HashMap<String, String>): String {
        val keys = map.keys.toList()
        for (key in keys) {
            val value = map[key] ?: continue
            if (key == "screenName") {
                nextScreen = value
            }
            if (value == "empty" || value == "null") {
                if (key == "currentScreen") {
                    map[key] = Constants.CURRENT_SCREEN
                } else {
                    val dbValue = mehdiViewModel.getValueByKey(key)
                    dbValue?.value?.let { map[key] = it }
                }
            }
            if (key.contains("variable")) {
                varlist.add(value)
            }
        }

        val phId = mehdiViewModel.getValueByKey("phid")?.value.orEmpty()
        map["phId"] = phId
        val policeCode = mehdiViewModel.getValueByKey("policeCode")?.value.orEmpty()
        map["policeCode"] = policeCode
        val userName = mehdiViewModel.getValueByKey("userName")?.value.orEmpty()
        map["userName"] = userName

        listOf("base64", "base64Image", "car_picture").forEach { field ->
            val text = map[field]?.replace("\n", "")?.replace(" ", "+")
            if (text != null) {
                map[field] = text
            }
        }

        map["versionCodePhPlus"] = BuildConfig.VERSION_CODE.toString()
        map["versionNamePhPlus"] = BuildConfig.VERSION_NAME
        map["androidVersionNamePhPlus"] = Build.VERSION.SDK_INT.toString()
        map["imei"] = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        map["deviceModel"] = Build.MODEL

        // getLoginData: fill device/client payload expected by server (skip keys already set).
        LoginDeviceInfoHelper.enrichIfLoginRequest(this, map, mehdiViewModel)

        return phId
    }

    /**
     * Process service response directly without sending to server
     * Used for cached responses from check_db_call_service mode
     */
    private fun processServiceResponseDirectly(response: HashMap<String, String>) {
        lifecycleScope.launch {
            try {
                android.util.Log.d("MehdiActivity", "Processing cached service response directly")
                android.util.Log.d("MehdiActivity", "Response: $response")

                // Use the same processing logic as phPlus.observe SUCCESS case
                val varList = response
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
                var show_pdf = ""
                var show_image = ""
                var clearNotificationPrefs = ""
                var closeBottomSheet = false

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
                            "show_pdf" -> show_pdf = value
                            "show_image" -> show_image = value
                            "clearNotificationPrefs" -> clearNotificationPrefs = value
                            "close_bottom_sheet" -> closeBottomSheet = shouldCloseBottomSheet(value)

                            else -> pendingInserts.add(PhPlusDB(null, key, value))
                        }

                        // Handle bottom_sheet_variable first (before general variable check)
                        if (key.contains("bottom_sheet_variable")) {
                            println("MehdiActivity: Processing bottom_sheet_variable key=$key, value=$value")
                            val manager = bottomSheetManager
                            if (manager != null) {
                                val success = manager.setVariableOnTarget("current", key, value)
                                println("MehdiActivity: Variable setting on current bottom sheet, success=$success")
                            } else {
                                println("MehdiActivity: bottomSheetManager is null, skipping variable setting")
                            }
                        } else if (key.contains("variable")) {
                            div.setVariable(key, value)
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
                    var nextJsonDto = withContext(Dispatchers.IO) {
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
                        mehdiViewModel?.let {
                            CustomToast(
                                this@MehdiActivity,
                                this@MehdiActivity,
                                it,
                                this@MehdiActivity
                            )
                        }
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
                    showBottomSheetFromResponse(bottomSheet)
                }
                if (closeBottomSheet) {
                    dismissOpenBottomSheet()
                }
                if (dialog.isNotBlank()) {
                    DialogDiv(
                        this@MehdiActivity,
                        this@MehdiActivity,
                        dialog,
                        mehdiViewModel,
                        this@MehdiActivity
                    ).show()
                }
                if (patch.isNotBlank()) {
                    var dbPatch = withContext(Dispatchers.IO) {
                        mehdiViewModel?.getValueByKey(patch)
                    }
                    if (dbPatch != null) {
                        div.applyPatch(JSONObject(dbPatch.value).asDivPatchWithTemplates())
                    }
                }
                if (bottomSheetPatch.isNotBlank()) {
                    val dbPatch = withContext(Dispatchers.IO) {
                        mehdiViewModel?.getValueByKey(bottomSheetPatch)
                    }
                    val json = dbPatch?.value.orEmpty()
                    if (json.isNotEmpty()) {
                        bottomSheetManager?.getCurrentBottomSheet()
                            ?.onApplyOnbase(json, bottomSheetPatch, "", "")
                    }
                }
                if (show_pdf.isNotBlank()) {
                    showPdfFromBase64(show_pdf)
                }
                if (show_image.isNotBlank()) {
                    showImageFromBase64(show_image)
                }
                if (clearNotificationPrefs.isNotBlank() &&
                    clearNotificationPrefs.lowercase() == "true"
                ) {
                    try {
                        android.util.Log.d(
                            "MehdiActivity",
                            "Cached response clearNotificationPrefs=true → clearing prefs and stopping push receiving"
                        )
                        com.yandex.divkit.demo.DivkitApplication.getInstance()
                            ?.clearNotificationPrefsAndStopReceiving()
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "MehdiActivity",
                            "Error handling clearNotificationPrefs in cached response: ${e.message}",
                            e
                        )
                    }
                }

                android.util.Log.d(
                    "MehdiActivity",
                    "Cached service response processed successfully"
                )
            } catch (e: Exception) {
                android.util.Log.e("MehdiActivity", "Error processing cached service response", e)
            }
        }
    }

    override fun onApplyOnbase(
        json: String,
        patchName: String,
        patchTitle: String,
        vehicleType: String
    ) {
        try {
            val applied = div.applyPatch(JSONObject(json).asDivPatchWithTemplates())
            android.util.Log.d(
                "MehdiActivity",
                "applyPatch on base: patchName=$patchName applied=$applied"
            )
            if (!applied) {
                android.util.Log.w(
                    "MehdiActivity",
                    "applyPatch returned false for patchName=$patchName (id not found or parse/bind failed)"
                )
            }
            if (patchTitle != "" && patchTitle != "تست") {
                try {
                    div.setVariable("variable_patch", patchTitle)
                } catch (_: Exception) {
                    div.setVariable("patch", patchTitle)
                }
                try {
                    div.setVariable("patch", patchTitle)
                } catch (_: Exception) {
                }
            }
            if (vehicleType != "" && vehicleType != "تست")
                div.setVariable("vehicle_type", vehicleType)
            if (patchName != "" && patchName != "تست" && patchName != "patchEstelam")
                div.setVariable("plateType", patchName)
        } catch (e: Exception) {
            android.util.Log.e(
                "MehdiActivity",
                "Failed to apply patch on base screen: $patchName",
                e
            )
        }
    }

    private fun shouldCloseBottomSheet(value: String): Boolean {
        val normalized = value.trim()
        if (normalized.isEmpty()) return true
        return !normalized.equals("false", ignoreCase = true)
                && normalized != "0"
                && !normalized.equals("no", ignoreCase = true)
    }

    private fun dismissOpenBottomSheet() {
        bottomSheetManager?.getCurrentBottomSheet()?.dismiss()
    }

    private fun showBottomSheetFromResponse(bottomSheetKey: String, jsonOverride: String? = null) {
        val fullScreen = NotificationManager.consumePendingBottomSheetFullScreen()
        val jsonFromDb = runCatching { mehdiViewModel.getValueByKey(bottomSheetKey)?.value }
            .onFailure {
                android.util.Log.w(
                    "MehdiActivity",
                    "showBottomSheet getValueByKey('$bottomSheetKey') failed: ${it.message}"
                )
            }
            .getOrNull()
        val resolvedJson = jsonOverride?.takeIf { it.isNotBlank() }
            ?: jsonFromDb?.takeIf { it.isNotBlank() }
        android.util.Log.d(
            "MehdiActivity",
            "showBottomSheetFromResponse key=$bottomSheetKey overrideLen=${jsonOverride?.length ?: 0} " +
                "dbLen=${jsonFromDb?.length ?: 0} resolvedLen=${resolvedJson?.length ?: 0} fullScreen=$fullScreen"
        )
        val manager = bottomSheetManager
        try {
            if (manager != null) {
                val newBottomSheet = manager.createAndAddBottomSheet(
                    this@MehdiActivity,
                    this@MehdiActivity,
                    bottomSheetKey,
                    mehdiViewModel,
                    this@MehdiActivity,
                    fullScreen = fullScreen,
                )
                newBottomSheet.jsonContent = resolvedJson
                newBottomSheet.inlineJson = resolvedJson
                newBottomSheet.show(
                    (this@MehdiActivity as FragmentActivity).supportFragmentManager,
                    "bottomSheetList"
                )
            } else {
                val newBottomSheet = BottomSheetDiv(
                    this@MehdiActivity,
                    this@MehdiActivity,
                    bottomSheetKey,
                    mehdiViewModel,
                    this@MehdiActivity,
                    null
                )
                newBottomSheet.fullScreen = fullScreen
                newBottomSheet.jsonContent = resolvedJson
                newBottomSheet.inlineJson = resolvedJson
                newBottomSheet.show(
                    (this@MehdiActivity as FragmentActivity).supportFragmentManager,
                    "bottomSheetList"
                )
            }
            android.util.Log.d("MehdiActivity", "bottom sheet show() called for key=$bottomSheetKey")
        } catch (e: Exception) {
            android.util.Log.e("MehdiActivity", "Failed to show bottom sheet key=$bottomSheetKey", e)
        }
    }

    override fun getBtmSheetInstance(btmSheet: BottomSheetDiv) {
        // Bottom sheet is already managed by BottomSheetManager
        // This method is kept for compatibility but may not be needed anymore
    }

    override fun setVariableToBase(key: String, value: String) {
        div.setVariable(key, value)
    }

    fun focusDivElement(elementId: String, attempt: Int = 0) {
        if (!::div.isInitialized || elementId.isBlank()) return
        val maxAttempts = 15
        val target = div.findViewWithTag<android.view.View>(elementId)
        if (target == null) {
            if (attempt < maxAttempts) {
                div.postDelayed({ focusDivElement(elementId, attempt + 1) }, 60)
            } else {
                android.util.Log.w(
                    "MehdiActivity",
                    "focusDivElement: view not found for id=$elementId"
                )
            }
            return
        }
        if (!hasWindowFocus() && attempt < maxAttempts) {
            window.decorView.postDelayed({ focusDivElement(elementId, attempt + 1) }, 60)
            return
        }
        target.isFocusable = true
        target.isFocusableInTouchMode = true
        val gotFocus = target.requestFocus()
        if (target is android.widget.EditText) {
            val len = target.text?.length ?: 0
            try {
                target.setSelection(len)
            } catch (_: Exception) {
            }
        }
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        target.post {
            imm?.showSoftInput(target, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
        if ((!gotFocus || !target.hasFocus()) && attempt < maxAttempts) {
            target.postDelayed({ focusDivElement(elementId, attempt + 1) }, 80)
            return
        }
        android.util.Log.d(
            "MehdiActivity",
            "focusDivElement: focused=$gotFocus id=$elementId attempt=$attempt"
        )
    }

    private fun maybeRequireAppLockSetup(): Boolean {
        val store = AppLockStore(this)
        if (store.isConfigured) return false
        android.util.Log.i("AppLock", "Mandatory lock setup required")
        val intent = AppLockActivity.setupIntent(this, reopenMain = true).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        finish()
        return true
    }

    private fun hasLoggedInSession(extraUserName: String? = null): Boolean {
        if (!extraUserName.isNullOrBlank()) return true
        if (Constant.USERNAME.isNotBlank()) return true
        return try {
            mehdiViewModel.getValueByKey("userName")?.value?.isNotBlank() == true
        } catch (_: Exception) {
            false
        }
    }

    override fun openBottomSheet(screenName: String) {
        showBottomSheetFromResponse(screenName)
    }

    override fun loadeScreenWithData(data: String, screenName: String) {
        var next = ""

        if (screenName.isNotEmpty()) {
//            next = mehdiViewModel.getValueByKey(screenName).value
            if (screenName == "nextSplash")
                next = mehdiViewModel.getValueByKey(screenName)?.value.orEmpty()
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
        ApkDownloader(this).downloadAndInstall(url)
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
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        android.util.Log.d("MehdiActivity", "Has RECORD_AUDIO permission: $hasPermission")

        if (!hasPermission) {
            android.util.Log.d(
                "MehdiActivity",
                "Permission not granted, launching permission dialog"
            )
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            android.util.Log.d(
                "MehdiActivity",
                "Permission already granted, calling onAudioPermissionGranted()"
            )
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
        android.util.Log.d(
            "MehdiActivity",
            "Created intent for AudioRecordingService with ID: $recordingId"
        )
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
        android.util.Log.d(
            "MehdiActivity",
            "Creating stop intent with action: ${AudioRecordingService.ACTION_STOP}"
        )
        android.util.Log.d("MehdiActivity", "Starting service to stop recording")
        try {
            startService(intent)
            android.util.Log.d("MehdiActivity", "Service start command sent successfully")
        } catch (e: Exception) {
            android.util.Log.e(
                "MehdiActivity",
                "Error starting service to stop recording: ${e.message}",
                e
            )
        }
        android.util.Log.d("MehdiActivity", "=== stopRecording() completed ===")
    }

    /**
     * Start location tracking service
     * This will start a foreground service that continuously tracks user location
     */
    fun startLocationService() {
        android.util.Log.d("MehdiActivity", "=== startLocationService() called ===")

        // Check location permissions first
        if (!hasLocationPermissions()) {
            android.util.Log.w("MehdiActivity", "Location permissions not granted, requesting...")
            getLocationPermission()
            return
        }

        val intent = Intent(this, com.yandex.divkit.demo.service.LocationService::class.java)
        android.util.Log.d("MehdiActivity", "Starting foreground location service")
        try {
            ContextCompat.startForegroundService(this, intent)
            android.util.Log.d("MehdiActivity", "Location service started successfully")
            Toast.makeText(this, "ردیابی موقعیت شروع شد", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.util.Log.e("MehdiActivity", "Error starting location service: ${e.message}", e)
            Toast.makeText(this, "خطا در شروع ردیابی موقعیت: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
        android.util.Log.d("MehdiActivity", "=== startLocationService() completed ===")
    }

    /**
     * Stop location tracking service
     */
    fun stopLocationService() {
        android.util.Log.d("MehdiActivity", "=== stopLocationService() called ===")
        val intent =
            Intent(this, com.yandex.divkit.demo.service.LocationService::class.java).apply {
                action = com.yandex.divkit.demo.service.LocationService.ACTION_STOP
            }
        android.util.Log.d("MehdiActivity", "Stopping location service")
        try {
            startService(intent)
            android.util.Log.d("MehdiActivity", "Location service stop command sent successfully")
            Toast.makeText(this, "ردیابی موقعیت متوقف شد", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.util.Log.e("MehdiActivity", "Error stopping location service: ${e.message}", e)
        }
        android.util.Log.d("MehdiActivity", "=== stopLocationService() completed ===")
    }

    /**
     * Get last known location from LocationService
     */
    fun getLastLocation(): android.location.Location? {
        return com.yandex.divkit.demo.service.LocationService.getLastLocation(this)
    }

    /**
     * Get last known location as string (lat,lng)
     */
    fun getLastLocationString(): String? {
        return com.yandex.divkit.demo.service.LocationService.getLastLocationString(this)
    }

    private fun hasLocationPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires both fine and coarse location
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onAudioPermissionGranted() {
        // This method can be used to notify AudioRecorderView that permission was granted
        // The actual implementation would depend on how you want to communicate with the view
        android.util.Log.d("MehdiActivity", "=== onAudioPermissionGranted() called ===")
        android.util.Log.d("MehdiActivity", "Pending recording ID: $pendingRecordingId")
        Toast.makeText(this, "مجوز ضبط صدا اعطا شد - حالا می‌توانید ضبط کنید", Toast.LENGTH_LONG)
            .show()
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
                android.util.Log.d(
                    "MehdiActivity",
                    "Starting upload process for recording ID: $recordingId"
                )
                Toast.makeText(
                    this@MehdiActivity,
                    "شروع آپلود فایل‌های ضبط شده...",
                    Toast.LENGTH_LONG
                ).show()

                // First stop recording and wait for file to be saved
                android.util.Log.d("MehdiActivity", "Stopping recording before upload")
                stopRecording()

                // Wait for recording to stop and file to be saved (2 seconds should be enough)
                android.util.Log.d(
                    "MehdiActivity",
                    "Waiting for recording to stop and file to be saved..."
                )
                delay(2000) // 2 second delay

                val result = withContext(Dispatchers.IO) {
                    uploadRecordingFiles(recordingId)
                }

                if (result.isNotEmpty()) {
                    android.util.Log.d(
                        "MehdiActivity",
                        "Upload completed successfully. IDs: $result"
                    )
                    Toast.makeText(
                        this@MehdiActivity,
                        "آپلود با موفقیت انجام شد",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    android.util.Log.w(
                        "MehdiActivity",
                        "No files found or uploaded for recording ID: $recordingId"
                    )
                    Toast.makeText(
                        this@MehdiActivity,
                        "فایل ضبط شده‌ای یافت نشد",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                android.util.Log.e("MehdiActivity", "Error during upload process", e)
                Toast.makeText(this@MehdiActivity, "خطا در آپلود: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }

        android.util.Log.d("MehdiActivity", "=== uploadRecording() completed ===")
    }

    private suspend fun uploadRecordingFiles(recordingId: String): String {
        android.util.Log.d("MehdiActivity", "=== uploadRecordingFiles() called ===")

        // Get token from database
        val token = mehdiViewModel?.getValueByKey(" ")?.value
        if (token.isNullOrEmpty()) {
            android.util.Log.e("MehdiActivity", "Token not found in database")
            return ""
        }

        android.util.Log.d("MehdiActivity", "Using token: $token")

        // Find recording files
        val recordingFiles =
            com.yandex.divkit.demo.utils.FileUploadUtil.findRecordingFiles(this, recordingId)
        android.util.Log.d("MehdiActivity", "Found ${recordingFiles.size} files to upload")

        val uploadedIds = mutableListOf<String>()

        // Upload each file
        for (file in recordingFiles) {
            try {
                android.util.Log.d("MehdiActivity", "Uploading file: ${file.name}")
                val uploadResult =
                    com.yandex.divkit.demo.utils.FileUploadUtil.uploadFile(this, file, token)

                if (uploadResult.success && uploadResult.fileId != null) {
                    uploadedIds.add(uploadResult.fileId)
                    android.util.Log.d(
                        "MehdiActivity",
                        "File uploaded successfully. ID: ${uploadResult.fileId}"
                    )
                } else {
                    android.util.Log.e(
                        "MehdiActivity",
                        "Failed to upload file: ${file.name}. Error: ${uploadResult.error}"
                    )
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

            mehdiViewModel?.insertItemToDb(
                com.yandex.divkit.demo.data.entities.PhPlusDB(
                    null,
                    dbKey,
                    uploadedIdsString
                )
            )
        }

        android.util.Log.d("MehdiActivity", "=== uploadRecordingFiles() completed ===")
        return uploadedIds.joinToString(",")
    }

    override fun uploadAndCallService(
        recordingId: String?,
        serviceParams: HashMap<String, String>?
    ) {
        android.util.Log.d("MehdiActivity", "=== uploadAndCallService() called ===")
        android.util.Log.d("MehdiActivity", "Recording ID: $recordingId")
        android.util.Log.d("MehdiActivity", "Service params: $serviceParams")

        if (recordingId == null) {
            android.util.Log.e(
                "MehdiActivity",
                "Recording ID is null, cannot upload and call service"
            )
            Toast.makeText(this, "شناسه ضبط صدا مشخص نیست", Toast.LENGTH_LONG).show()
            return
        }

        // Start upload and call service process in background
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d(
                    "MehdiActivity",
                    "Starting upload and call service process for recording ID: $recordingId"
                )
                Toast.makeText(
                    this@MehdiActivity,
                    "شروع آپلود فایل‌ها و فراخوانی سرویس...",
                    Toast.LENGTH_LONG
                ).show()

                // First stop recording and wait for file to be saved
                android.util.Log.d("MehdiActivity", "Stopping recording before upload")
                stopRecording()

                // Wait for recording to stop and file to be saved (2 seconds should be enough)
                android.util.Log.d(
                    "MehdiActivity",
                    "Waiting for recording to stop and file to be saved..."
                )
                delay(2000) // 2 second delay

                val uploadedIds = withContext(Dispatchers.IO) {
                    uploadRecordingFilesForService(recordingId)
                }

                if (uploadedIds.isNotEmpty()) {
                    android.util.Log.d(
                        "MehdiActivity",
                        "Upload completed successfully. IDs: $uploadedIds"
                    )

                    // Merge uploaded IDs with the provided service parameters
                    val finalServiceParams =
                        serviceParams?.clone() as? HashMap<String, String> ?: HashMap()

                    // Add the uploaded recording IDs to the service parameters
                    finalServiceParams["uploaded_recorded_${recordingId}"] = uploadedIds

                    android.util.Log.d(
                        "MehdiActivity",
                        "Final service params with uploaded IDs: $finalServiceParams"
                    )
                    android.util.Log.d(
                        "MehdiActivity",
                        "Calling service with all params: $finalServiceParams"
                    )
                    onRequest(finalServiceParams)

                    Toast.makeText(
                        this@MehdiActivity,
                        "آپلود و فراخوانی سرویس با موفقیت انجام شد",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    android.util.Log.w(
                        "MehdiActivity",
                        "No files found or uploaded for recording ID: $recordingId"
                    )

                    // Even if no files uploaded, still call the service with the provided parameters
                    if (serviceParams != null) {
                        android.util.Log.d(
                            "MehdiActivity",
                            "No files uploaded, but calling service with provided params: $serviceParams"
                        )
                        onRequest(serviceParams)
                    }

                    Toast.makeText(
                        this@MehdiActivity,
                        "فایل ضبط شده‌ای یافت نشد، اما سرویس فراخوانی شد",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                android.util.Log.e(
                    "MehdiActivity",
                    "Error during upload and call service process",
                    e
                )
                Toast.makeText(
                    this@MehdiActivity,
                    "خطا در آپلود و فراخوانی سرویس: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        android.util.Log.d("MehdiActivity", "=== uploadAndCallService() completed ===")
    }

    override fun uploadFilesAndCallService(params: HashMap<String, String>) {
        android.util.Log.d("MehdiActivity", "=== uploadFilesAndCallService() called ===")
        android.util.Log.d("MehdiActivity", "Params: $params")

        lifecycleScope.launch {
            val tempFiles = mutableListOf<File>()
            try {
                loading.showLoadingDialog("لطفا شکیبا باشید...")

                val requestMap = HashMap(params)
                val fileParts = mutableListOf<MultipartBody.Part>()
                val fileLabels = JSONObject()

                for (index in 1..5) {
                    val label = requestMap.remove("file${index}_name")?.trim().orEmpty()
                    val rawPath = requestMap.remove("file${index}_path")?.trim().orEmpty()
                    if (label.isEmpty() || rawPath.isEmpty() || rawPath == "null" || rawPath == "empty") {
                        continue
                    }

                    val resolved = withContext(Dispatchers.IO) {
                        resolveUploadFile(rawPath)
                    }
                    if (resolved == null) {
                        android.util.Log.e(
                            "MehdiActivity",
                            "upload_files_and_call_service file$index not found: $rawPath"
                        )
                        continue
                    }

                    if (resolved.isTemporary) {
                        tempFiles.add(resolved.file)
                    }
                    fileLabels.put("file$index", label)
                    fileParts.add(createCoordinatorFilePart("file$index", resolved.file))
                }

                if (fileParts.isEmpty()) {
                    loading.dismissDialog()
                    Toast.makeText(
                        this@MehdiActivity,
                        "فایل برای آپلود یافت نشد",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val phId = prepareCoordinatorRequestMap(requestMap)
                val requestJson = JSONObject()
                for ((key, value) in requestMap) {
                    requestJson.put(key, value)
                }
                requestJson.put("fileLabels", fileLabels)

                android.util.Log.d(
                    "MehdiActivity",
                    "upload_files_and_call_service requestScreen=$requestJson"
                )
                android.util.Log.d(
                    "MehdiActivity",
                    "upload_files_and_call_service files=${fileParts.size} labels=$fileLabels"
                )

                val result = mehdiViewModel.uploadFilesAndCallService(
                    phId,
                    requestJson.toString(),
                    fileParts
                )

                loading.dismissDialog()

                when (result.status) {
                    Resource.Status.SUCCESS -> {
                        val data = result.data
                        if (data == null) {
                            error.showErrorDialog("پاسخ دریافتی از سرور نادرست است")
                            return@launch
                        }

                        val responseMap = HashMap<String, String>()
                        for ((key, value) in data) {
                            if (key != null && value != null) {
                                responseMap[key] = value
                            }
                        }

                        val isUploaded =
                            responseMap["isUploaded"]?.equals("true", ignoreCase = true) == true
                        android.util.Log.d(
                            "MehdiActivity",
                            "upload_files_and_call_service isUploaded=$isUploaded"
                        )
                        try {
                            div.setVariable("isUploaded", if (isUploaded) "true" else "false")
                        } catch (e: Exception) {
                            android.util.Log.d(
                                "MehdiActivity",
                                "isUploaded variable is not declared on the current card"
                            )
                        }

                        processServiceResponseDirectly(responseMap)
                    }

                    Resource.Status.ERROR -> {
                        result.message?.let { error.showErrorDialog(it) }
                    }

                    Resource.Status.LOADING -> {
                    }
                }
            } catch (e: Exception) {
                loading.dismissDialog()
                android.util.Log.e("MehdiActivity", "upload_files_and_call_service failed", e)
                error.showErrorDialog(e.message ?: "خطا در آپلود فایل")
            } finally {
                tempFiles.forEach { file ->
                    try {
                        file.delete()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private data class ResolvedUploadFile(
        val file: File,
        val isTemporary: Boolean,
    )

    private fun resolveUploadFile(rawPath: String): ResolvedUploadFile? {
        val trimmed = rawPath.trim()
        if (trimmed.isEmpty() || trimmed == "null" || trimmed == "empty") {
            return null
        }

        if (trimmed.startsWith("content://")) {
            val copied = copyContentUriToCache(Uri.parse(trimmed)) ?: return null
            return ResolvedUploadFile(copied, isTemporary = true)
        }

        val path = if (trimmed.startsWith("file://")) {
            Uri.parse(trimmed).path ?: trimmed.removePrefix("file://")
        } else {
            trimmed
        }

        val direct = File(path)
        if (direct.exists() && direct.isFile && direct.canRead()) {
            return ResolvedUploadFile(direct, isTemporary = false)
        }

        val inFiles = File(filesDir, path)
        if (inFiles.exists() && inFiles.isFile && inFiles.canRead()) {
            return ResolvedUploadFile(inFiles, isTemporary = false)
        }

        val inCache = File(cacheDir, path)
        if (inCache.exists() && inCache.isFile && inCache.canRead()) {
            return ResolvedUploadFile(inCache, isTemporary = false)
        }

        if (!trimmed.contains("/") && !trimmed.contains(":")) {
            val fromDb = mehdiViewModel.getValueByKey(trimmed)?.value
            if (!fromDb.isNullOrBlank() && fromDb != trimmed) {
                return resolveUploadFile(fromDb)
            }
        }

        return null
    }

    private fun copyContentUriToCache(uri: Uri): File? {
        return try {
            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(contentResolver.getType(uri))
                ?: "bin"
            val target = File(cacheDir, "upload_${System.currentTimeMillis()}.$extension")
            contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            if (target.exists() && target.length() > 0) target else null
        } catch (e: Exception) {
            android.util.Log.e("MehdiActivity", "Failed to copy content uri: $uri", e)
            null
        }
    }

    private fun createCoordinatorFilePart(formName: String, file: File): MultipartBody.Part {
        val mime = mimeTypeForFile(file)
        return MultipartBody.Part.createFormData(
            formName,
            file.name,
            file.asRequestBody(mime.toMediaType())
        )
    }

    private fun mimeTypeForFile(file: File): String {
        val extension = file.extension.lowercase()
        val fromMap = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (!fromMap.isNullOrBlank()) {
            return fromMap
        }
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "wav" -> "audio/wav"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
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
        val recordingFiles =
            com.yandex.divkit.demo.utils.FileUploadUtil.findRecordingFiles(this, recordingId)
        android.util.Log.d("MehdiActivity", "Found ${recordingFiles.size} files to upload")

        val uploadedIds = mutableListOf<String>()

        // Upload each file
        for (file in recordingFiles) {
            try {
                android.util.Log.d("MehdiActivity", "Uploading file: ${file.name}")
                val uploadResult =
                    com.yandex.divkit.demo.utils.FileUploadUtil.uploadFile(this, file, token)

                if (uploadResult.success && uploadResult.fileId != null) {
                    uploadedIds.add(uploadResult.fileId)
                    android.util.Log.d(
                        "MehdiActivity",
                        "File uploaded successfully. ID: ${uploadResult.fileId}"
                    )
                } else {
                    android.util.Log.e(
                        "MehdiActivity",
                        "Failed to upload file: ${file.name}. Error: ${uploadResult.error}"
                    )
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

            mehdiViewModel?.insertItemToDb(
                com.yandex.divkit.demo.data.entities.PhPlusDB(
                    null,
                    dbKey,
                    uploadedIdsString
                )
            )

            android.util.Log.d(
                "MehdiActivity",
                "=== uploadRecordingFilesForService() completed ==="
            )
            return uploadedIdsString
        }

        android.util.Log.d("MehdiActivity", "=== uploadRecordingFilesForService() completed ===")
        return ""
    }

    /**
     * Show PDF from base64 string
     */
    private fun showPdfFromBase64(base64String: String) {
        try {
            println("MehdiActivity: showPdfFromBase64 - Starting PDF display")
            println("MehdiActivity: Base64 length: ${base64String.length}")

            // Clean the base64 string (remove whitespace, newlines, etc.)
            var cleanedBase64 = base64String.trim()
                .replace("\n", "")
                .replace("\r", "")
                .replace(" ", "")

            // Remove data URI prefix if present (e.g., "data:application/pdf;base64,")
            if (cleanedBase64.contains(",")) {
                cleanedBase64 = cleanedBase64.substringAfter(",")
                println("MehdiActivity: Removed data URI prefix")
            }

            println("MehdiActivity: Cleaned Base64 length: ${cleanedBase64.length}")
            println("MehdiActivity: First 100 chars: ${cleanedBase64.take(100)}")

            // Decode base64 string to bytes - try different flags
            val decodedBytes = try {
                android.util.Base64.decode(cleanedBase64, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                println("MehdiActivity: DEFAULT flag failed, trying NO_WRAP")
                try {
                    android.util.Base64.decode(cleanedBase64, android.util.Base64.NO_WRAP)
                } catch (e2: Exception) {
                    println("MehdiActivity: NO_WRAP failed, trying NO_PADDING")
                    android.util.Base64.decode(cleanedBase64, android.util.Base64.NO_PADDING)
                }
            }

            println("MehdiActivity: Decoded bytes length: ${decodedBytes.size}")

            // Check if it's a valid PDF by checking the header
            if (decodedBytes.size < 4) {
                throw Exception("فایل خیلی کوچک است")
            }

            val pdfHeader = String(decodedBytes.take(4).toByteArray())
            println("MehdiActivity: File header: $pdfHeader (should be %PDF)")

            if (!pdfHeader.startsWith("%PDF")) {
                println("MehdiActivity: WARNING - File does not start with PDF header!")
                println("MehdiActivity: First 20 bytes: ${decodedBytes.take(20).joinToString(",")}")
            }

            // Create a temporary file in cache directory
            val tempFile = File(cacheDir, "temp_pdf_${System.currentTimeMillis()}.pdf")
            tempFile.writeBytes(decodedBytes)
            println("MehdiActivity: File created at: ${tempFile.absolutePath}")
            println("MehdiActivity: File exists: ${tempFile.exists()}, Size: ${tempFile.length()} bytes")

            // Open with custom PDF viewer activity
            val intent = Intent(this, PdfViewerActivity::class.java)
            intent.putExtra("PDF_FILE_PATH", tempFile.absolutePath)
            println("MehdiActivity: Opening PDF with custom viewer")
            startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()
            println("MehdiActivity: Error showing PDF: ${e.message}")
            Toast.makeText(this, "خطا در نمایش فایل PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Cache container definitions from asset file for dynamic patching
     */
    private fun cacheContainerDefinitionsFromAsset(assetPath: String) {
        try {
            println("MehdiActivity: Caching container definitions from asset: $assetPath")
            val jsonString = assetReader.read(assetPath)
            val jsonObject = JSONObject(jsonString.toString())
            cacheContainerDefinitionsFromJson(jsonObject)
        } catch (e: Exception) {
            println("MehdiActivity: Error caching container definitions: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Cache container definitions from JSON object for dynamic patching
     */
    private fun cacheContainerDefinitionsFromJson(jsonObject: JSONObject) {
        try {
            println("MehdiActivity: Searching for containers with IDs to cache...")
            findAndCacheContainers(jsonObject)
        } catch (e: Exception) {
            println("MehdiActivity: Error caching container definitions: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Recursively find and cache container definitions
     */
    private fun findAndCacheContainers(json: Any) {
        when (json) {
            is JSONObject -> {
                // Check if this object has an "id" and "type"
                if (json.has("id") && json.has("type")) {
                    val containerId = json.getString("id")
                    val containerType = json.getString("type")

                    // Cache gallery and container types
                    if (containerType == "gallery" || containerType == "container") {
                        println("MehdiActivity: Found container to cache - id=$containerId, type=$containerType")
                        com.yandex.divkit.demo.div.DynamicPatchGeneratorSingleton.instance.setContainerDefinition(
                            containerId,
                            json
                        )
                    }
                }

                // Recursively search all keys
                json.keys().forEach { key ->
                    findAndCacheContainers(json.get(key))
                }
            }

            is org.json.JSONArray -> {
                for (i in 0 until json.length()) {
                    findAndCacheContainers(json.get(i))
                }
            }
        }
    }

    /**
     * Show Image from base64 string
     */
    private fun showImageFromBase64(base64String: String) {
        try {
            println("MehdiActivity: showImageFromBase64 - Starting image display")
            println("MehdiActivity: Base64 length: ${base64String.length}")

            // Decode base64 string to bytes
            val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            println("MehdiActivity: Decoded bytes length: ${decodedBytes.size}")

            // Create a temporary file in cache directory
            val tempFile = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            tempFile.writeBytes(decodedBytes)
            println("MehdiActivity: File created at: ${tempFile.absolutePath}")
            println("MehdiActivity: File exists: ${tempFile.exists()}, Size: ${tempFile.length()} bytes")

            // Get URI using FileProvider
            val uri = FileProvider.getUriForFile(
                this,
                "com.yandex.divkit.demo.fileprovider",
                tempFile
            )
            println("MehdiActivity: FileProvider URI: $uri")

            // Create intent with proper flags
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "image/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            // Check if there's an app that can handle images
            val resolveInfo = packageManager.queryIntentActivities(intent, 0)
            println("MehdiActivity: Available image viewers: ${resolveInfo.size}")

            if (resolveInfo.isNotEmpty()) {
                println("MehdiActivity: Opening image with viewer")
                startActivity(intent)
            } else {
                println("MehdiActivity: No image viewer found")
                Toast.makeText(this, "نمی‌توان تصویر را باز کرد", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("MehdiActivity: Error showing image: ${e.message}")
            Toast.makeText(this, "خطا در نمایش تصویر: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Get the action handler from divViewCreator.
     * This is used by custom views (like NotificationListView) to update NotificationManager.
     */
    fun getActionHandler(): com.yandex.divkit.demo.ui.UIDiv2ActionHandler? {
        return if (::divViewCreator.isInitialized) {
            divViewCreator.actionHandler
        } else {
            android.util.Log.w("MehdiActivity", "divViewCreator is not initialized yet")
            null
        }
    }

    // NotificationListener implementation
    override fun onNotificationAdded(notification: com.yandex.divkit.demo.div.notificationList.NotificationItem) {
        android.util.Log.d("MehdiActivity", "=== onNotificationAdded callback ===")
        android.util.Log.d("MehdiActivity", "New notification: ${notification.title}")
        android.util.Log.d("MehdiActivity", "Notification type: ${notification.rawType}")
        android.util.Log.d("MehdiActivity", "Is read: ${notification.isRead}")

        // Update notification count variables immediately on main thread
        runOnUiThread {
            android.util.Log.d(
                "MehdiActivity",
                "Calling updateNotificationCountVariables from onNotificationAdded"
            )
            updateNotificationCountVariables()
        }
    }

    override fun onNotificationRead(notificationId: String) {
        android.util.Log.d("MehdiActivity", "=== onNotificationRead callback ===")
        android.util.Log.d("MehdiActivity", "Notification read: $notificationId")

        // Update notification count variables immediately on main thread
        runOnUiThread {
            updateNotificationCountVariables()
        }
    }

    override fun onNotificationsChanged(notifications: List<com.yandex.divkit.demo.div.notificationList.NotificationItem>) {
        android.util.Log.d("MehdiActivity", "=== onNotificationsChanged callback ===")
        android.util.Log.d("MehdiActivity", "Total notifications: ${notifications.size}")

        // Don't update variables here - onNotificationAdded and onNotificationRead already handle it
        // This prevents duplicate updates
        android.util.Log.d(
            "MehdiActivity",
            "Skipping variable update in onNotificationsChanged (handled by onNotificationAdded/onNotificationRead)"
        )
    }

    /**
     * Update all notification count variables in DivKit
     * This is called whenever notification counts change
     * Uses debounce to prevent duplicate updates
     */
    private fun updateNotificationCountVariables() {
        try {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastUpdate = currentTime - lastNotificationUpdateTime

            // Debounce: if called within 100ms of last update, skip it
            if (timeSinceLastUpdate < NOTIFICATION_UPDATE_DEBOUNCE_MS) {
                android.util.Log.d(
                    "MehdiActivity",
                    "=== updateNotificationCountVariables called (DEBOUNCED) ==="
                )
                android.util.Log.d(
                    "MehdiActivity",
                    "Skipping update - only ${timeSinceLastUpdate}ms since last update"
                )
                return
            }

            lastNotificationUpdateTime = currentTime
            android.util.Log.d("MehdiActivity", "=== updateNotificationCountVariables called ===")
            android.util.Log.d("MehdiActivity", "div.isInitialized: ${::div.isInitialized}")

            if (!::div.isInitialized) {
                android.util.Log.w("MehdiActivity", "Div not initialized yet, cannot set variables")
                return
            }

            // Set flag to prevent observer updates (they read from DB which may have old values)
            isUpdatingFromCallback = true
            android.util.Log.d(
                "MehdiActivity",
                "Set isUpdatingFromCallback flag to prevent observer updates"
            )

            // Get total unread count from NotificationManager (reads from memory, not DB)
            val totalUnreadCount = NotificationManager.getUnreadNotificationsCount()
            android.util.Log.d("MehdiActivity", "Total unread count from memory: $totalUnreadCount")
            setVariable("unread_notifications_count", totalUnreadCount.toString())

            // Get per-type unread counts from NotificationManager (reads from memory, not DB)
            val basicInfoJson = mehdiViewModel.getValueByKey("notificationBasicInfo")?.value
            if (basicInfoJson != null && basicInfoJson.isNotEmpty()) {
                try {
                    val gson = Gson()
                    val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                    val basicInfo = gson.fromJson<List<Map<String, Any>>>(basicInfoJson, listType)

                    android.util.Log.d(
                        "MehdiActivity",
                        "Found ${basicInfo.size} notification types"
                    )

                    for (typeInfo in basicInfo) {
                        val typeId = typeInfo["id"]?.toString() ?: continue
                        val key = "unread_notifications_count_type_$typeId"

                        // Get count from NotificationManager (reads from memory, not DB)
                        val typeCount =
                            NotificationManager.getUnreadNotificationsCountByType(typeId)
                        android.util.Log.d(
                            "MehdiActivity",
                            "Setting $key: $typeCount (from memory)"
                        )
                        setVariable(key, typeCount.toString())
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MehdiActivity", "Error updating type counts", e)
                    e.printStackTrace()
                }
            } else {
                android.util.Log.w(
                    "MehdiActivity",
                    "notificationBasicInfo not found, cannot update type counts"
                )
            }

            android.util.Log.d(
                "MehdiActivity",
                "=== updateNotificationCountVariables completed ==="
            )

            // Reset flag after a delay to allow DB write to complete
            // This prevents observer updates from reading old values from DB
            Handler(Looper.getMainLooper()).postDelayed({
                isUpdatingFromCallback = false
                android.util.Log.d("MehdiActivity", "Reset isUpdatingFromCallback flag after 500ms")
            }, 500) // 500ms delay to ensure DB write completes
        } catch (e: Exception) {
            android.util.Log.e("MehdiActivity", "Error in updateNotificationCountVariables", e)
            e.printStackTrace()
            // Reset flag even on error
            isUpdatingFromCallback = false
        }
    }

}
