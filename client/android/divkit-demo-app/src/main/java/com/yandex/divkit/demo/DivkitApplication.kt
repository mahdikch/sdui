package com.yandex.divkit.demo

import android.app.ActivityManager
import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.stetho.Stetho
import com.yandex.android.beacon.SendBeaconConfiguration
import com.yandex.android.beacon.SendBeaconPerWorkerLogger
import com.yandex.div.core.DivKit
import com.yandex.div.core.DivKitConfiguration
import com.yandex.div.internal.Assert
import com.yandex.divkit.demo.beacon.SendBeaconRequestExecutorImpl
import com.yandex.divkit.demo.beacon.SendBeaconWorkerSchedulerImpl
import com.yandex.divkit.demo.div.notificationList.NotificationManager
import com.yandex.divkit.demo.utils.VisualAssertionErrorHandler
import com.yandex.divkit.regression.di.HasRegressionTesting
import com.yandex.divkit.regression.di.RegressionComponent
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import ir.naja.pushsdk.android.Constant
import ir.naja.pushsdk.android.NajiPush
import ir.naja.pushsdk.android.NajiPushCallBack
import ir.naja.pushsdk.android.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.util.concurrent.Executors


class DivkitApplication : Application(), HasRegressionTesting, NajiPushCallBack {

    companion object {
        private const val TAG = "DivkitApplication"

        @Volatile
        private var INSTANCE: DivkitApplication? = null

        fun getInstance(): DivkitApplication? = INSTANCE
    }

    private lateinit var najiPush: NajiPush
    private var isPushSetupCompleted = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override val regressionComponent: RegressionComponent
        get() = Container.regressionComponent

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this
        Container.initialize(applicationContext)
        // Create an InitializerBuilder
        val initializerBuilder = Stetho.newInitializerBuilder(this)

        // Enable Chrome DevTools
        initializerBuilder.enableWebKitInspector(
            Stetho.defaultInspectorModulesProvider(this)
        )

        // Enable command line interface
        initializerBuilder.enableDumpapp(
            Stetho.defaultDumperPluginsProvider(this)
        )

        // Use the InitializerBuilder to generate an Initializer
        val initializer = initializerBuilder.build()

        // Initialize Stetho with the Initializer
        Stetho.initialize(initializer)
        AppMetrica.activate(
            this,
            AppMetricaConfig.newConfigBuilder("e48dd638-f5ba-4cb8-b272-53b6d275062f")
                .withCrashReporting(!BuildConfig.DEBUG)
                .withLocationTracking(false)
                .withNativeCrashReporting(false)
                .withLogs().build()
        )

        // Internal API used, do not reproduce this line in your app
        Assert.setAssertPerformer(VisualAssertionErrorHandler(this))
        DivKit.apply {
            enableLogging(true)
            enableAssertions(BuildConfig.THROW_ASSERTS)
        }
        DivKit.configure(
            DivKitConfiguration.Builder()
                .sendBeaconConfiguration(::configureSendBeacon)
                .histogramConfiguration(Container::histogramConfiguration)
                .build()
        )

        // Initialize Push Notifications
//        initializePushNotifications()

        AppCompatDelegate.setDefaultNightMode(Container.preferences.nightMode)
    }

    private fun initializePushNotifications() {
        try {
            najiPush = NajiPush()
            najiPush.setListener(this)

            Log.d(TAG, "Push notifications initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize push notifications", e)
        }
    }

    // Method to be called from MehdiActivity to complete push setup (ONLY ONCE)
    fun completePushSetup(activity: android.app.Activity) {
        try {
            // Check if push setup is already completed
            if (isPushSetupCompleted) {
                Log.d(TAG, "Push setup already completed, skipping...")
                return
            }

            // Create intent to open MehdiActivity when notification is tapped
            val mainIntent =
                Intent(this, com.yandex.divkit.demo.ui.activity.MehdiActivity::class.java)

            runBlocking {
                launch(Dispatchers.IO) {
                    if (!najiPush.isConnected) {
                        najiPush.ActiveNajiFicationTwoApp(
                            activity, // Use the activity parameter
                            mainIntent,
                            Constant.API_KEY,
                            Constant.API_KEY2,
                            Constant.USERNAME,
                            "",
                            "", Constant.USERNAME_PORTAL, Constant.PASSWORD_PORTAL
                        )
                        /*       najiPush.ActiveNajiFication(
                                   activity, // Use the activity parameter
                                   mainIntent,
                                   "e573552c-b409-4f25-99b5-27da8f2a29e6",
                                   "PH_NOTIF/"+Constant.USERNAME,
                                   "",
                                   "","policehamrah","123456"
                               )*/


                        // Mark setup as completed
                        isPushSetupCompleted = true
                        Log.d(TAG, "Push setup completed with activity context - FIRST TIME ONLY")
                    } else {
                        Log.d(TAG, "Push already connected, marking setup as completed")
                        isPushSetupCompleted = true
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete push setup", e)
        }
    }

    private fun configureSendBeacon(): SendBeaconConfiguration {
        return SendBeaconConfiguration(
            executor = Executors.newSingleThreadExecutor(),
            requestExecutor = SendBeaconRequestExecutorImpl(OkHttpClient.Builder().build()),
            workerScheduler = SendBeaconWorkerSchedulerImpl(this),
            perWorkerLogger = SendBeaconPerWorkerLogger.Logcat,
            databaseName = "mordaSendBeacon.db"
        )
    }

    // NajiPushCallBack implementations
    override fun reconnected(s: String?) {
        Log.d(TAG, "Push reconnected: $s")
    }

    override fun connected(s: String?) {
        Log.d(TAG, "Push connected: $s")
    }

    override fun connectFailed(s: String?) {
        Log.e(TAG, "Push connection failed: $s")
    }

    override fun connectionLost(s: String?) {
        Log.w(TAG, "Push connection lost: $s")
    }

    override fun messageArrived(topic: String?, message: Message?) {
        Log.d(TAG, "Push message arrived - topic: $topic")

        message?.let { msg ->
            try {
                // Add notification to the notification list
                NotificationManager.createNotificationFromPushMessage(msg.payload)

                // Handle the push message
                handlePushMessage(msg)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling push message", e)
            }
        }
    }

    override fun deliveryComplete(s: String?) {
        Log.d(TAG, "Push delivery complete: $s")
    }

    override fun deliveryNotComplete(s: String?) {
        Log.w(TAG, "Push delivery not complete: $s")
    }

    override fun Subscribed(s: String?) {
        Log.d(TAG, "Push subscribed: $s")
    }

    override fun SubscribeFailed(s: String?) {
        Log.e(TAG, "Push subscribe failed: $s")
    }

    override fun errorPublishing(s: String?) {
        Log.e(TAG, "Push error publishing: $s")
    }

    override fun bufferedMessageCount(s: String?) {
        Log.d(TAG, "Push buffered message count: $s")
    }

    private fun handlePushMessage(message: Message) {
        Log.d(TAG, "Handling push message: ${message.payload}")

        try {
            // Check if app is currently in foreground
            val isAppInForeground = isAppInForeground()
            Log.d(TAG, "App is in foreground: $isAppInForeground")

            // Ensure UI operations run on main thread
            mainHandler.post {
                try {
                    if (!isAppInForeground) {
                        // App is in background - start activity
                        val intent = Intent(
                            this@DivkitApplication,
                            com.yandex.divkit.demo.ui.activity.MehdiActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

                            // Add data from push message
                            putExtra("push_message", message.payload)
                            putExtra("push_topic", message.topic)

                            // If your payload contains specific screen/JSON info, add it here
                            // putExtra("json", payload.screenName)
                        }

                        startActivity(intent)
                        Log.d(
                            TAG,
                            "Activity started from push notification (app was in background)"
                        )
                    } else {
                        // App is in foreground - just add notification to list
                        Log.d(TAG, "App is in foreground - notification added to list only")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error handling push message", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing push message", e)
        }
    }

    private fun isAppInForeground(): Boolean {
        return try {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val runningAppProcesses = activityManager.runningAppProcesses

            runningAppProcesses?.any { processInfo ->
                processInfo.processName == packageName &&
                        processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if app is in foreground", e)
            false
        }
    }
}
