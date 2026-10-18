package com.yandex.divkit.demo.utils

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.yandex.divkit.demo.BuildConfig
import com.yandex.divkit.demo.service.LocationService
import com.yandex.divkit.demo.ui.activity.MehdiViewModel

/**
 * Fills device/client fields required by getLoginData.
 * Does not overwrite keys that are already present (e.g. username/password/captcha from login UI).
 */
object LoginDeviceInfoHelper {

    fun enrichIfLoginRequest(
        context: Context,
        map: HashMap<String, String>,
        mehdiViewModel: MehdiViewModel?,
    ) {
        if (map["path"] != "getLoginData") return

        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ).orEmpty()

        putMissing(map, "deviceKey", androidId)
        putMissing(
            map,
            "deviceLabel",
            listOf(Build.MANUFACTURER, Build.MODEL)
                .map { it.orEmpty().trim() }
                .filter { it.isNotEmpty() }
                .joinToString(" ")
        )
        putMissing(map, "androidId", androidId)
        putMissing(map, "serial", readSerialSafe())
        putMissing(map, "model", Build.MODEL.orEmpty())
        putMissing(map, "manufacturer", Build.MANUFACTURER.orEmpty())
        putMissing(map, "brand", Build.BRAND.orEmpty())
        putMissing(map, "device", Build.DEVICE.orEmpty())
        putMissing(map, "product", Build.PRODUCT.orEmpty())
        putMissing(map, "hardware", Build.HARDWARE.orEmpty())
        putMissing(map, "cpu", Build.SUPPORTED_ABIS.firstOrNull().orEmpty())
        putMissing(map, "ramMb", readTotalRamMb(context))
        putMissing(map, "androidVersion", Build.VERSION.RELEASE.orEmpty())
        putMissing(map, "sdk", Build.VERSION.SDK_INT.toString())
        putMissing(map, "appVersion", BuildConfig.VERSION_NAME)
        putMissing(map, "clientTimestamp", System.currentTimeMillis().toString())

        val location = resolveLocation(context, mehdiViewModel)
        putMissing(map, "latitude", location?.first.orEmpty())
        putMissing(map, "longitude", location?.second.orEmpty())

        // Keep legacy imei if already filled by prepareCoordinatorRequestMap;
        // only set when completely missing.
        putMissing(map, "imei", androidId)
    }

    private fun putMissing(map: HashMap<String, String>, key: String, value: String) {
        val current = map[key]
        if (current.isNullOrBlank() || current == "empty" || current == "null") {
            map[key] = value
        }
    }

    private fun readTotalRamMb(context: Context): String {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = ActivityManager.MemoryInfo()
            am.getMemoryInfo(info)
            (info.totalMem / (1024L * 1024L)).toString()
        } catch (_: Exception) {
            ""
        }
    }

    @Suppress("DEPRECATION")
    private fun readSerialSafe(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // May throw SecurityException without READ_PHONE_STATE.
                Build.getSerial().takeUnless { it.equals(Build.UNKNOWN, ignoreCase = true) }.orEmpty()
            } else {
                Build.SERIAL.takeUnless { it.equals(Build.UNKNOWN, ignoreCase = true) }.orEmpty()
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun resolveLocation(
        context: Context,
        mehdiViewModel: MehdiViewModel?,
    ): Pair<String, String>? {
        LocationService.getLastLocation(context)?.let {
            return format(it.latitude) to format(it.longitude)
        }

        val latDb = mehdiViewModel?.getValueByKey("latitude")?.value
        val lngDb = mehdiViewModel?.getValueByKey("longitude")?.value
        if (!latDb.isNullOrBlank() && !lngDb.isNullOrBlank()) {
            return latDb to lngDb
        }

        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return null

        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null
            val last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            last?.let { format(it.latitude) to format(it.longitude) }
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun format(value: Double): String =
        String.format(java.util.Locale.US, "%.6f", value)
}
