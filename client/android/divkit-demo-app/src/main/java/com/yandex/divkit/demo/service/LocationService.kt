package com.yandex.divkit.demo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.yandex.divkit.demo.DivkitApplication
import com.yandex.divkit.demo.R
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.ui.activity.MehdiActivity
import com.yandex.divkit.demo.utils.SingletonObjects
import ir.naja.pushsdk.android.Constant.APP_NAME_SEND
import ir.naja.pushsdk.android.NajiPush
import ir.naja.pushsdk.android.SharedPrefs
import ir.naja.pushsdk.android.model.Message
import ir.naja.pushsdk.android.model.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LocationService : Service() {
    companion object {
        const val CHANNEL_ID = "location_channel"
        const val NOTIFICATION_ID = 2
        const val ACTION_STOP = "action_stop_location"

        // SharedPreferences keys
        const val PREFS_NAME = "location_prefs"
        const val KEY_LATITUDE = "last_latitude"
        const val KEY_LONGITUDE = "last_longitude"
        const val KEY_ACCURACY = "last_accuracy"
        const val KEY_TIMESTAMP = "last_location_timestamp"

        /**
         * Get last known location from SharedPreferences
         * Can be called from anywhere in the app
         */
        fun getLastLocation(context: Context): Location? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lat = prefs.getString(KEY_LATITUDE, null)
            val lng = prefs.getString(KEY_LONGITUDE, null)

            if (lat != null && lng != null) {
                return Location("saved").apply {
                    latitude = lat.toDouble()
                    longitude = lng.toDouble()
                    accuracy = prefs.getString(KEY_ACCURACY, "0")?.toFloat() ?: 0f
                    time = prefs.getLong(KEY_TIMESTAMP, System.currentTimeMillis())
                }
            }
            return null
        }

        fun getLastLocationString(context: Context): String? {
            val location = getLastLocation(context)
            return location?.let { "${it.latitude},${it.longitude}" }
        }
    }

    private lateinit var locationManager: LocationManager
    private lateinit var sharedPreferences: SharedPreferences
    private var isTracking = false
    private var initialNotification: Notification? = null
    private var najiPush: NajiPush? = null
    private var lastLocationUpdateTime: Long = 0
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Location update parameters (GPS only)
    private val minTime: Long = 5000 // 1 second - faster updates for GPS
    private val minDistance: Float = 0f // 0 meters - get updates even if device doesn't move

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lastLocationUpdateTime = System.currentTimeMillis()
            android.util.Log.d(
                "LocationService",
                "=== onLocationChanged() called === lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}m, provider=${location.provider}"
            )

            // Save to SharedPreferences
            saveLocation(location)

            // Update notification with current location
            updateNotification(location)

            // Send location to server via NajiPush
            sendLocationToServer(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
            val statusText = when (status) {
                android.location.LocationProvider.AVAILABLE -> "AVAILABLE"
                android.location.LocationProvider.OUT_OF_SERVICE -> "OUT_OF_SERVICE"
                android.location.LocationProvider.TEMPORARILY_UNAVAILABLE -> "TEMPORARILY_UNAVAILABLE"
                else -> "UNKNOWN($status)"
            }
            android.util.Log.d(
                "LocationService",
                "Location provider status changed: provider=$provider, status=$statusText"
            )
        }

        override fun onProviderEnabled(provider: String) {
            android.util.Log.d("LocationService", "Location provider enabled: $provider")
            // Only handle GPS provider (ignore network provider)
            if (provider == LocationManager.GPS_PROVIDER) {
                android.util.Log.d("LocationService", "GPS provider enabled - requesting location")
                // Try to get last known location when GPS provider is enabled
                try {
                    val lastLocation =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null) {
                        val ageSeconds = (System.currentTimeMillis() - lastLocation.time) / 1000
                        android.util.Log.d(
                            "LocationService",
                            "Got GPS location after provider enabled: lat=${lastLocation.latitude}, lng=${lastLocation.longitude}, age=${ageSeconds}s"
                        )
                        // Only use if location is not too old (less than 5 minutes)
                        if (System.currentTimeMillis() - lastLocation.time < 5 * 60 * 1000) {
                            onLocationChanged(lastLocation)
                        } else {
                            android.util.Log.d(
                                "LocationService",
                                "GPS location too old, waiting for new fix"
                            )
                        }
                    } else {
                        android.util.Log.d(
                            "LocationService",
                            "No last known GPS location, waiting for GPS fix"
                        )
                    }
                } catch (e: SecurityException) {
                    android.util.Log.e(
                        "LocationService",
                        "SecurityException getting GPS location after provider enabled: ${e.message}",
                        e
                    )
                }
            }
        }

        override fun onProviderDisabled(provider: String) {
            android.util.Log.w("LocationService", "Location provider disabled: $provider")
        }
    }

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("LocationService", "=== onCreate() called ===")

        // Create notification channel early (before onStartCommand)
        createNotificationChannel()

        // Prepare initial notification in onCreate() to make onStartCommand() faster
        prepareInitialNotification()

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Get NajiPush instance from DivkitApplication
        try {
            val app = DivkitApplication.getInstance()
            najiPush = app?.getNajiPush()
            if (najiPush != null) {
                android.util.Log.d(
                    "LocationService",
                    "NajiPush instance obtained from DivkitApplication"
                )
            } else {
                android.util.Log.w("LocationService", "NajiPush instance is null")
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "LocationService",
                "Error getting NajiPush instance: ${e.message}",
                e
            )
        }

        android.util.Log.d("LocationService", "LocationService initialized")
    }

    private fun prepareInitialNotification() {
        try {
            val stopIntent = Intent(this, LocationService::class.java).apply {
                action = ACTION_STOP
            }

            val stopPendingIntent = PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val mainIntent = Intent(this, MehdiActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                this,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Create a simple notification (don't read SharedPreferences here - it's slow)
            initialNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ردیابی موقعیت")
                .setContentText("در حال دریافت لوکیشن...")
                .setSmallIcon(R.drawable.baseline_my_location_24)
                .setContentIntent(mainPendingIntent)
                .addAction(
                    R.drawable.baseline_stop_24,
                    "توقف",
                    stopPendingIntent
                )
                .setOngoing(true) // Cannot be dismissed by swiping
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            android.util.Log.d("LocationService", "Initial notification prepared in onCreate()")
        } catch (e: Exception) {
            android.util.Log.e(
                "LocationService",
                "Error preparing initial notification: ${e.message}",
                e
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("LocationService", "=== onStartCommand called ===")
        android.util.Log.d("LocationService", "Intent action: ${intent?.action}")

        when (intent?.action) {
            ACTION_STOP -> {
                android.util.Log.d(
                    "LocationService",
                    "ACTION_STOP received, stopping location tracking"
                )
                stopLocationTracking()
                stopForeground(true)
                stopSelf()
                android.util.Log.d("LocationService", "Service stopped")
                return START_NOT_STICKY
            }

            else -> {
                if (isTracking) {
                    android.util.Log.d(
                        "LocationService",
                        "Already tracking location, ignoring duplicate start request"
                    )
                    return START_STICKY
                }

                android.util.Log.d("LocationService", "Starting location tracking service")

                // CRITICAL: Call startForeground() IMMEDIATELY with pre-prepared notification
                // This must happen within 5 seconds of startForegroundService() call
                initialNotification?.let { notification ->
                    startForeground(NOTIFICATION_ID, notification)
                    android.util.Log.d(
                        "LocationService",
                        "startForeground() called immediately with pre-prepared notification"
                    )
                } ?: run {
                    // Fallback: if notification wasn't prepared, create it now (shouldn't happen)
                    android.util.Log.w(
                        "LocationService",
                        "Initial notification is null, creating it now"
                    )
                    startForegroundServiceWithNotification()
                }

                // Now do other work (this can take time, but startForeground() is already called)
                startLocationTracking()
                updateNotificationWithLastLocation()

                android.util.Log.d("LocationService", "Location tracking service started")
                return START_STICKY
            }
        }
    }

    private fun startLocationTracking() {
        try {
            // Check location permissions
            if (!hasLocationPermissions()) {
                android.util.Log.e("LocationService", "Location permissions not granted")
                stopSelf()
                return
            }

            // Check if GPS provider is enabled (only using GPS, not network)
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            android.util.Log.d(
                "LocationService",
                "Location provider status - GPS: $isGpsEnabled (Network provider disabled by user preference)"
            )

            if (!isGpsEnabled) {
                android.util.Log.e(
                    "LocationService",
                    "GPS provider is not enabled. Please enable GPS in device settings."
                )
                stopSelf()
                return
            }

            // Try to get last known GPS location immediately (this might be null, but worth trying)
            try {
                val lastGpsLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastGpsLocation != null) {
                    val ageSeconds = (System.currentTimeMillis() - lastGpsLocation.time) / 1000
                    android.util.Log.d(
                        "LocationService",
                        "Got last known GPS location: lat=${lastGpsLocation.latitude}, lng=${lastGpsLocation.longitude}, age=${ageSeconds}s"
                    )
                    // Only use if location is not too old (less than 5 minutes)
                    if (System.currentTimeMillis() - lastGpsLocation.time < 5 * 60 * 1000) {
                        android.util.Log.d(
                            "LocationService",
                            "Using last known GPS location (age < 5 minutes)"
                        )
                        locationListener.onLocationChanged(lastGpsLocation)
                    } else {
                        android.util.Log.d(
                            "LocationService",
                            "Last known GPS location is too old (age >= 5 minutes), waiting for new fix"
                        )
                    }
                } else {
                    android.util.Log.d(
                        "LocationService",
                        "Last known GPS location is null - waiting for GPS fix"
                    )
                }
            } catch (e: SecurityException) {
                android.util.Log.e(
                    "LocationService",
                    "SecurityException getting last known GPS location: ${e.message}",
                    e
                )
            }

            // Request location updates from GPS provider only
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    locationListener,
                    Looper.getMainLooper()
                )
                android.util.Log.d(
                    "LocationService",
                    "GPS location updates requested - minTime: ${minTime}ms, minDistance: ${minDistance}m"
                )
                android.util.Log.d(
                    "LocationService",
                    "Using GPS provider only (Network provider disabled)"
                )
                isTracking = true
            } catch (e: SecurityException) {
                android.util.Log.e(
                    "LocationService",
                    "SecurityException requesting GPS location: ${e.message}",
                    e
                )
                stopSelf()
            } catch (e: Exception) {
                android.util.Log.e(
                    "LocationService",
                    "Exception requesting GPS location: ${e.message}",
                    e
                )
                stopSelf()
            }

            if (isTracking) {
                android.util.Log.d("LocationService", "Location tracking started successfully")
                android.util.Log.d(
                    "LocationService",
                    "Waiting for location updates... (GPS fix may take 10-60 seconds, especially indoors)"
                )
                android.util.Log.d(
                    "LocationService",
                    "If no updates received, check: 1) GPS enabled in settings, 2) Location permission granted, 3) Device has clear view of sky (for GPS)"
                )
            } else {
                android.util.Log.e("LocationService", "Failed to start location tracking")
                stopSelf()
            }
        } catch (e: SecurityException) {
            android.util.Log.e("LocationService", "SecurityException: ${e.message}", e)
            stopSelf()
        } catch (e: Exception) {
            android.util.Log.e(
                "LocationService",
                "Error starting location tracking: ${e.message}",
                e
            )
            stopSelf()
        }
    }

    private fun stopLocationTracking() {
        try {
            locationManager.removeUpdates(locationListener)
            isTracking = false
            android.util.Log.d("LocationService", "Location tracking stopped")
        } catch (e: Exception) {
            android.util.Log.e(
                "LocationService",
                "Error stopping location tracking: ${e.message}",
                e
            )
        }
    }

    private fun hasLocationPermissions(): Boolean {
        val hasFineLocation =
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation =
            checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ requires background location permission for background updates
            val hasBackgroundLocation =
                checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED

            android.util.Log.d(
                "LocationService",
                "Permission check - Fine: $hasFineLocation, Coarse: $hasCoarseLocation, Background: $hasBackgroundLocation"
            )

            // For foreground service, we can work without background permission, but it's better to have it
            return (hasFineLocation || hasCoarseLocation)
        } else {
            android.util.Log.d(
                "LocationService",
                "Permission check - Fine: $hasFineLocation, Coarse: $hasCoarseLocation"
            )
            return hasFineLocation || hasCoarseLocation
        }
    }

    private fun saveLocation(location: Location) {
        try {
            sharedPreferences.edit().apply {
                putString(KEY_LATITUDE, location.latitude.toString())
                putString(KEY_LONGITUDE, location.longitude.toString())
                putString(KEY_ACCURACY, location.accuracy.toString())
                putLong(KEY_TIMESTAMP, location.time)
                apply()
            }
            android.util.Log.d("LocationService", "Location saved to SharedPreferences")
        } catch (e: Exception) {
            android.util.Log.e("LocationService", "Error saving location: ${e.message}", e)
        }

        saveLocationToDatabase(location)
    }

    /**
     * Persist latest coordinates in PhPlus DB as two key/value records:
     * key="latitude", key="longitude" (REPLACE on conflict).
     */
    private fun saveLocationToDatabase(location: Location) {
        serviceScope.launch {
            try {
                val dao = SingletonObjects.getDbInstance(applicationContext).phPlusDBDao()
                dao.insert(PhPlusDB(null, "latitude", location.latitude.toString()))
                dao.insert(PhPlusDB(null, "longitude", location.longitude.toString()))
                android.util.Log.d(
                    "LocationService",
                    "Location saved to DB: latitude=${location.latitude}, longitude=${location.longitude}"
                )
            } catch (e: Exception) {
                android.util.Log.e(
                    "LocationService",
                    "Error saving location to DB: ${e.message}",
                    e
                )
            }
        }
    }

    private fun startForegroundServiceWithNotification() {
        android.util.Log.d(
            "LocationService",
            "=== startForegroundServiceWithNotification() called ==="
        )

        // CRITICAL: Create a simple notification IMMEDIATELY and call startForeground()
        // This must happen within 5 seconds of startForegroundService() call
        // Note: Notification channel is already created in onCreate()
        val stopIntent = Intent(this, LocationService::class.java).apply {
            action = ACTION_STOP
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mainIntent = Intent(this, MehdiActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create a simple notification first (don't read SharedPreferences here - it's slow)
        val initialNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ردیابی موقعیت")
            .setContentText("در حال دریافت لوکیشن...")
            .setSmallIcon(R.drawable.baseline_my_location_24)
            .setContentIntent(mainPendingIntent)
            .addAction(
                R.drawable.baseline_stop_24,
                "توقف",
                stopPendingIntent
            )
            .setOngoing(true) // Cannot be dismissed by swiping
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // CRITICAL: Call startForeground() IMMEDIATELY (within 5 seconds)
        startForeground(NOTIFICATION_ID, initialNotification)
        android.util.Log.d(
            "LocationService",
            "Started foreground service with initial notification"
        )

        // Now update notification with last known location (this can take time, but startForeground() is already called)
        updateNotificationWithLastLocation()
    }

    private fun updateNotificationWithLastLocation() {
        try {
            // Get last known location (this might be slow, but it's OK because startForeground() is already called)
            val lastLocation = getLastKnownLocation()
            val locationText = if (lastLocation != null) {
                "Lat: ${String.format("%.6f", lastLocation.latitude)}, Lng: ${
                    String.format(
                        "%.6f",
                        lastLocation.longitude
                    )
                }"
            } else {
                "در حال دریافت لوکیشن..."
            }

            // Update notification with location
            val stopIntent = Intent(this, LocationService::class.java).apply {
                action = ACTION_STOP
            }

            val stopPendingIntent = PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val mainIntent = Intent(this, MehdiActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                this,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ردیابی موقعیت")
                .setContentText(locationText)
                .setSmallIcon(R.drawable.baseline_my_location_24)
                .setContentIntent(mainPendingIntent)
                .addAction(
                    R.drawable.baseline_stop_24,
                    "توقف",
                    stopPendingIntent
                )
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true) // Only alert (vibrate/sound) once, not on every update
                .build()

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, notification)
            android.util.Log.d("LocationService", "Notification updated with last known location")
        } catch (e: Exception) {
            android.util.Log.e(
                "LocationService",
                "Error updating notification with last location: ${e.message}",
                e
            )
        }
    }

    private fun updateNotification(location: Location) {
        try {
            val locationText = "Lat: ${String.format("%.6f", location.latitude)}, Lng: ${
                String.format(
                    "%.6f",
                    location.longitude
                )
            }"

            val stopIntent = Intent(this, LocationService::class.java).apply {
                action = ACTION_STOP
            }

            val stopPendingIntent = PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val mainIntent = Intent(this, MehdiActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                this,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ردیابی موقعیت")
                .setContentText(locationText)
                .setSmallIcon(R.drawable.baseline_my_location_24)
                .setContentIntent(mainPendingIntent)
                .addAction(
                    R.drawable.baseline_stop_24,
                    "توقف",
                    stopPendingIntent
                )
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true) // Only alert (vibrate/sound) once, not on every update
                .build()

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, notification)

            android.util.Log.d("LocationService", "Notification updated with new location")
        } catch (e: Exception) {
            android.util.Log.e("LocationService", "Error updating notification: ${e.message}", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows current location tracking status"
                setShowBadge(false)
                enableVibration(false) // Disable vibration for location updates
                enableLights(false) // Disable lights
                setSound(null, null) // Disable sound
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            android.util.Log.d("LocationService", "Notification channel created")
        }
    }

    private fun getLastKnownLocation(): Location? {
        return try {
            val lastLocation = sharedPreferences.getString(KEY_LATITUDE, null)
            val lastLng = sharedPreferences.getString(KEY_LONGITUDE, null)

            if (lastLocation != null && lastLng != null) {
                Location("saved").apply {
                    latitude = lastLocation.toDouble()
                    longitude = lastLng.toDouble()
                    accuracy = sharedPreferences.getString(KEY_ACCURACY, "0")?.toFloat() ?: 0f
                    time = sharedPreferences.getLong(KEY_TIMESTAMP, System.currentTimeMillis())
                }
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "LocationService",
                "Error getting last known location: ${e.message}",
                e
            )
            null
        }
    }

    /**
     * Set NajiPush instance for sending location updates
     */
    fun setNajiPush(najiPush: NajiPush) {
        this.najiPush = najiPush
        android.util.Log.d("LocationService", "NajiPush instance set")
    }

    /**
     * Send location to server via NajiPush MQTT
     */
    private fun sendLocationToServer(location: Location) {
        try {
            if (najiPush == null) {
                android.util.Log.w("LocationService", "NajiPush is null, cannot send location")
                return
            }

            // Check if NajiPush is connected
            if (!najiPush!!.isConnected()) {
                android.util.Log.w(
                    "LocationService",
                    "NajiPush is not connected, cannot send location"
                )
                return
            }

            // Get topic from SharedPreferences (saved by NajiPush)
            val topic = SharedPrefs.getString(this, SharedPrefs.topic1)
            if (topic.isNullOrEmpty()) {
                android.util.Log.w("LocationService", "Topic is empty, cannot send location")
                return
            }

            // Create location data JSON
            val locationData = HashMap<String, Any>()
            if (location.latitude == 0.0 && location.longitude == 0.0) {
                locationData["latitude"] = 35.675154
                locationData["longitude"] =51.54
            } else {
                locationData["latitude"] = location.latitude
                locationData["longitude"] = location.longitude
            }
            locationData["accuracy"] = location.accuracy
            locationData["timestamp"] = location.time
            locationData["provider"] = location.provider ?: "unknown"
            if (location.hasSpeed()) {
                locationData["speed"] = location.speed
            }
            if (location.hasBearing()) {
                locationData["bearing"] = location.bearing
            }
            if (location.hasAltitude()) {
                locationData["altitude"] = location.altitude
            }


            // Publish location to topic "location/{topic}"
            var topicfinal = "$topic".replace("PHPLUS_APP", APP_NAME_SEND);
//            var topicfinal = "$topic".replace("ROKHDAD_APP_MISSION", APP_NAME_SEND);
            var payload = Payload()
            payload.title = "location"
            payload.body = Gson().toJson(locationData)
            var message = Message()
            message.payload = Gson().toJson(payload)
            message.topic = topicfinal
            najiPush!!.publishMessage(topicfinal, Gson().toJson(message))

            android.util.Log.d(
                "LocationService",
                "Location sent to server: topic=$topicfinal, lat=${location.latitude}, lng=${location.longitude}"
            )
        } catch (e: Exception) {
            android.util.Log.e(
                "LocationService",
                "Error sending location to server: ${e.message}",
                e
            )
        }
    }

    override fun onDestroy() {
        android.util.Log.d("LocationService", "=== onDestroy() called ===")
        super.onDestroy()
        stopLocationTracking()
        android.util.Log.d("LocationService", "=== onDestroy() completed ===")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
