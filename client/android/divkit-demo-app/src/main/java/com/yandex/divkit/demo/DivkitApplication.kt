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
import com.yandex.divkit.demo.push.MqttClientHandleCrashGuard
import com.yandex.divkit.demo.utils.VisualAssertionErrorHandler
import com.yandex.divkit.regression.di.HasRegressionTesting
import com.yandex.divkit.regression.di.RegressionComponent
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import ir.naja.pushsdk.android.Constant
import ir.naja.pushsdk.android.NajiPush
import ir.naja.pushsdk.android.NajiPushCallBack
import ir.naja.pushsdk.android.model.Message
import okhttp3.OkHttpClient
import java.lang.ref.WeakReference
import java.util.concurrent.Executors


class DivkitApplication : Application(), HasRegressionTesting, NajiPushCallBack {

    companion object {
        private const val TAG = "DivkitApplication"
        private const val MQTT_RECONNECT_DELAY_MS = 1500L
        private const val MQTT_HANDLE_CRASH_MAX_RETRIES = 3

        @Volatile
        private var INSTANCE: DivkitApplication? = null

        fun getInstance(): DivkitApplication? = INSTANCE
    }

    private lateinit var najiPush: NajiPush
    private var isPushSetupCompleted = false
    @Volatile
    private var connectedPushUsername: String = ""
    @Volatile
    private var connectingUsername: String = ""
    @Volatile
    private var pushReceivingEnabled = true
    @Volatile
    private var pushState = PushConnectionState.IDLE
    private val pushLock = Any()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pushExecutor = Executors.newSingleThreadExecutor()
    private var pendingActivityRef: WeakReference<android.app.Activity>? = null
    private var lastDisconnectAtMs = 0L
    private var mqttHandleCrashRetries = 0

    private enum class PushConnectionState {
        IDLE, CONNECTING, CONNECTED, DISCONNECTING
    }

    override val regressionComponent: RegressionComponent
        get() = Container.regressionComponent

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this
        Container.initialize(applicationContext)
        ir.naja.pushsdk.android.PushSsl.init(this)
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

        MqttClientHandleCrashGuard.install()
        initializePushNotifications()

        AppCompatDelegate.setDefaultNightMode(Container.preferences.nightMode)
    }

    fun initializePushNotifications() {
        try {
            pushReceivingEnabled = true
            if (!::najiPush.isInitialized) {
                najiPush = NajiPush()
            }
            najiPush.setListener(this)
            Log.d(TAG, "Push notifications initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize push notifications", e)
        }
    }

    fun getNajiPush(): NajiPush? {
        return if (::najiPush.isInitialized) najiPush else null
    }

    fun stopPushNotifications() {
        try {
            pushReceivingEnabled = false
            mainHandler.removeCallbacks(connectPushRunnable)
            disconnectNajiPush("stop")
            Log.d(TAG, "Push notifications stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop push notifications", e)
        }
    }

    /**
     * Server reserved key clearNotificationPrefs=true:
     * clear push SharedPreferences and stop receiving notifications.
     */
    fun clearNotificationPrefsAndStopReceiving() {
        try {
            Log.d(TAG, "clearNotificationPrefsAndStopReceiving: clearing prefs and disconnecting")
            // Disconnect first: clearing session keys while MQTT is still connected lets
            // messages arrive that can no longer be decrypted.
            stopPushNotifications()
            ir.naja.pushsdk.android.SharedPrefs.clear(this)
            ir.naja.pushsdk.android.AES.clearKey()
            Constant.sessionId = ""
            Log.d(TAG, "Notification prefs cleared and push receiving stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear notification prefs and stop receiving", e)
        }
    }

    fun restartPushNotifications() {
        try {
            Log.d(TAG, "Restarting push notifications...")
            stopPushNotifications()
            // Rebuild OkHttp/TLS stack so Conscrypt + TLS1.3 settings apply after code updates.
            ir.naja.pushsdk.android.PushSsl.reset()
            ir.naja.pushsdk.android.PushSsl.init(this)
            initializePushNotifications()
            Log.d(TAG, "Push notifications restart initiated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restart push notifications", e)
        }
    }

    fun startLocationService() {
        try {
            val intent = Intent(this, com.yandex.divkit.demo.service.LocationService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(this, intent)
            Log.d(TAG, "Location service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start location service", e)
        }
    }

    fun stopLocationService() {
        try {
            val intent = Intent(this, com.yandex.divkit.demo.service.LocationService::class.java).apply {
                action = com.yandex.divkit.demo.service.LocationService.ACTION_STOP
            }
            startService(intent)
            Log.d(TAG, "Location service stop command sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop location service", e)
        }
    }

    // Method to be called from MehdiActivity to complete push setup (ONLY ONCE per username)
    fun completePushSetup(activity: android.app.Activity) {
        try {
            if (!pushReceivingEnabled) {
                Log.d(TAG, "Push receiving disabled, skipping completePushSetup")
                return
            }

            val username = resolvePushUsername()
            if (username.isBlank()) {
                Log.w(TAG, "Push setup skipped: username is empty")
                isPushSetupCompleted = false
                return
            }
            Constant.USERNAME = username
            pendingActivityRef = WeakReference(activity)

            if (!::najiPush.isInitialized) {
                initializePushNotifications()
            }

            synchronized(pushLock) {
                if (pushState == PushConnectionState.CONNECTED &&
                    isNajiConnected() &&
                    connectedPushUsername == username
                ) {
                    Log.d(TAG, "Push setup already completed for username=$username, skipping...")
                    return
                }
                if (pushState == PushConnectionState.CONNECTING && connectingUsername == username) {
                    Log.d(TAG, "Push connect already in progress for username=$username, skipping...")
                    return
                }
            }

            val needsDisconnect = synchronized(pushLock) {
                val activeUser = connectedPushUsername.ifBlank { connectingUsername }
                (pushState == PushConnectionState.CONNECTING ||
                    pushState == PushConnectionState.CONNECTED ||
                    isNajiConnected()) &&
                    activeUser.isNotBlank() &&
                    activeUser != username
            }
            if (needsDisconnect) {
                Log.d(
                    TAG,
                    "Push username changed ($connectedPushUsername -> $username), reconnecting"
                )
                disconnectNajiPush("username_changed")
            }

            scheduleConnect(username)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete push setup", e)
        }
    }

    private val connectPushRunnable = Runnable {
        val username = resolvePushUsername()
        activateNajiPush(username)
    }

    private fun scheduleConnect(username: String) {
        mainHandler.removeCallbacks(connectPushRunnable)
        connectingUsername = username
        val elapsed = android.os.SystemClock.elapsedRealtime() - lastDisconnectAtMs
        val delay = if (lastDisconnectAtMs > 0L && elapsed < MQTT_RECONNECT_DELAY_MS) {
            MQTT_RECONNECT_DELAY_MS - elapsed
        } else {
            0L
        }
        if (delay > 0L) {
            Log.d(TAG, "Delaying MQTT connect by ${delay}ms to avoid Invalid ClientHandle")
            mainHandler.postDelayed(connectPushRunnable, delay)
        } else if (Looper.myLooper() == Looper.getMainLooper()) {
            activateNajiPush(username)
        } else {
            mainHandler.post(connectPushRunnable)
        }
    }

    private fun activateNajiPush(username: String) {
        if (!pushReceivingEnabled) {
            Log.d(TAG, "Push receiving disabled, skipping MQTT connect")
            return
        }
        if (username.isBlank()) {
            Log.w(TAG, "MQTT connect skipped: username is empty")
            return
        }

        synchronized(pushLock) {
            if (pushState == PushConnectionState.CONNECTED &&
                isNajiConnected() &&
                connectedPushUsername == username
            ) {
                Log.d(TAG, "MQTT already connected for username=$username")
                return
            }
            if (pushState == PushConnectionState.CONNECTING && connectingUsername == username) {
                Log.d(TAG, "MQTT connect already in progress for username=$username")
                return
            }
            if (isNajiConnected() && connectedPushUsername == username) {
                pushState = PushConnectionState.CONNECTED
                isPushSetupCompleted = true
                return
            }
            pushState = PushConnectionState.CONNECTING
            connectingUsername = username
            isPushSetupCompleted = false
        }

        val activity = pendingActivityRef?.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Log.w("NajiPush", "No valid activity for ActiveNajiFication")
            synchronized(pushLock) {
                if (pushState == PushConnectionState.CONNECTING) {
                    pushState = PushConnectionState.IDLE
                    connectingUsername = ""
                }
            }
            return
        }

        if (!::najiPush.isInitialized) {
            initializePushNotifications()
        }

        try {
            Log.i("NajiPush", "Activating NajiPush username=$username apiKey=${Constant.API_KEY.take(8)}…")
            val mainIntent = Intent(this, com.yandex.divkit.demo.ui.activity.MehdiActivity::class.java)
            pushExecutor.execute {
                try {
                    Log.i("NajiPush", "ActiveNajiFication starting background thread=${Thread.currentThread().name}")
                    najiPush.ActiveNajiFication(
                        activity,
                        mainIntent,
                        Constant.API_KEY,
                        username,
                        "",
                        "",
                        Constant.USERNAME_PORTAL,
                        Constant.PASSWORD_PORTAL
                    )
                    Log.i("NajiPush", "ActiveNajiFication returned")
                } catch (e: Exception) {
                    Log.e("NajiPush", "ActiveNajiFication failed", e)
                    synchronized(pushLock) {
                        pushState = PushConnectionState.IDLE
                        connectingUsername = ""
                        isPushSetupCompleted = false
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule ActiveNajiFication", e)
            synchronized(pushLock) {
                pushState = PushConnectionState.IDLE
                connectingUsername = ""
                isPushSetupCompleted = false
            }
        }
    }

    private fun disconnectNajiPush(reason: String) {
        mainHandler.removeCallbacks(connectPushRunnable)
        synchronized(pushLock) {
            pushState = PushConnectionState.DISCONNECTING
            isPushSetupCompleted = false
            connectedPushUsername = ""
            connectingUsername = ""
        }
        lastDisconnectAtMs = android.os.SystemClock.elapsedRealtime()
        try {
            if (::najiPush.isInitialized && isNajiConnected()) {
                najiPush.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect NajiPush ($reason)", e)
        }
        synchronized(pushLock) {
            if (pushState == PushConnectionState.DISCONNECTING) {
                pushState = PushConnectionState.IDLE
            }
        }
        Log.d(TAG, "NajiPush disconnected ($reason)")
    }

    private fun isNajiConnected(): Boolean {
        return try {
            ::najiPush.isInitialized && najiPush.isConnected
        } catch (e: Exception) {
            Log.e(TAG, "isConnected check failed", e)
            false
        }
    }

    private fun resolvePushUsername(): String {
        Constant.USERNAME.trim().takeIf { it.isNotEmpty() }?.let { return it }
        NotificationManager.getCurrentOwner()?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        return ""
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
        markPushConnected()
    }

    override fun connected(s: String?) {
        Log.i("NajiPush", "Push connected: $s")
        markPushConnected()
    }

    override fun connectFailed(s: String?) {
        Log.e("NajiPush", "Push connection failed: $s")
        synchronized(pushLock) {
            pushState = PushConnectionState.IDLE
            isPushSetupCompleted = false
            connectingUsername = ""
        }
    }

    override fun connectionLost(s: String?) {
        Log.w("NajiPush", "Push connection lost: $s")
        synchronized(pushLock) {
            if (pushState != PushConnectionState.DISCONNECTING) {
                pushState = PushConnectionState.IDLE
                isPushSetupCompleted = false
                connectedPushUsername = ""
                connectingUsername = ""
            }
        }
    }

    private fun markPushConnected() {
        synchronized(pushLock) {
            pushState = PushConnectionState.CONNECTED
            isPushSetupCompleted = true
            connectedPushUsername = connectingUsername.ifBlank { resolvePushUsername() }
            connectingUsername = connectedPushUsername
            mqttHandleCrashRetries = 0
        }
    }

    fun onMqttClientHandleCrash() {
        Log.e(TAG, "Resetting MQTT state after Invalid ClientHandle")
        synchronized(pushLock) {
            pushState = PushConnectionState.IDLE
            isPushSetupCompleted = false
            connectingUsername = ""
            connectedPushUsername = ""
        }
        lastDisconnectAtMs = android.os.SystemClock.elapsedRealtime()
        if (!pushReceivingEnabled) return
        if (mqttHandleCrashRetries >= MQTT_HANDLE_CRASH_MAX_RETRIES) {
            Log.e(TAG, "MQTT Invalid ClientHandle retry limit reached")
            return
        }
        mqttHandleCrashRetries += 1
        val username = resolvePushUsername()
        if (username.isNotBlank()) {
            scheduleConnect(username)
        }
    }

    override fun messageArrived(topic: String?, message: Message?) {
        Log.i("NajiPush", "Push message arrived topic=$topic")

        if (!pushReceivingEnabled) {
            Log.d(TAG, "Push receiving disabled, ignoring message")
            return
        }

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
