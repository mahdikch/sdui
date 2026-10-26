package com.yandex.divkit.demo.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Multi-signal rooted-environment checks (su/magisk/busybox/test-keys/frida/xposed).
 *
 * Does **not** hard-block the app by default: many field/MDM devices report false positives.
 * Results are available via [lastResult] for telemetry / server attestation.
 */
object RootDetector {

    private const val TAG = "RootDetector"

    @Volatile
    var lastResult: Result = Result(isSuspicious = false, signals = emptyList())
        private set

    data class Result(
        val isSuspicious: Boolean,
        val signals: List<String>,
    )

    fun evaluate(context: Context): Result {
        val signals = mutableListOf<String>()

        if (hasTestKeys()) signals += "test-keys"
        if (hasSuBinary()) signals += "su-binary"
        if (hasMagiskArtifacts()) signals += "magisk"
        if (hasBusyBox()) signals += "busybox"
        if (canExecuteWhichSu()) signals += "which-su"
        if (hasDangerousPackages(context)) signals += "root-apps"
        if (canWriteSystem()) signals += "rw-system"
        if (hasFridaArtifacts()) signals += "frida"
        if (hasXposedArtifacts()) signals += "xposed"
        if (isDebuggerAttached()) signals += "debugger"

        val result = Result(isSuspicious = signals.isNotEmpty(), signals = signals)
        lastResult = result
        if (result.isSuspicious) {
            Log.w(TAG, "Root/hook signals detected: ${signals.joinToString()}")
        } else {
            Log.i(TAG, "No common root signals detected")
        }
        return result
    }

    private fun hasTestKeys(): Boolean {
        val tags = Build.TAGS ?: return false
        return tags.contains("test-keys", ignoreCase = true)
    }

    private fun hasSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/su/bin/su",
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
        )
        return paths.any { File(it).exists() }
    }

    private fun hasMagiskArtifacts(): Boolean {
        val paths = arrayOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/data/adb/modules",
            "/cache/.disable_magisk",
            "/data/adb/magisk.db",
        )
        return paths.any { File(it).exists() }
    }

    private fun hasBusyBox(): Boolean {
        return File("/system/xbin/busybox").exists() ||
            File("/system/bin/busybox").exists() ||
            File("/sbin/busybox").exists()
    }

    private fun canExecuteWhichSu(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                val line = reader.readLine()
                process.waitFor()
                !line.isNullOrBlank()
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun hasDangerousPackages(context: Context): Boolean {
        val packages = arrayOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.noshufou.android.su",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.kingroot.kinguser",
            "com.kingo.root",
            "me.phh.superuser",
            "com.saurik.substrate",
            "de.robv.android.xposed.installer",
            "com.android.vending.billing.InAppBillingService.COIN",
        )
        val pm = context.packageManager
        return packages.any { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    private fun canWriteSystem(): Boolean {
        return try {
            val probe = File("/system/.phplus_root_probe")
            if (probe.exists()) {
                probe.delete()
                true
            } else {
                probe.createNewFile()
                probe.delete()
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun hasFridaArtifacts(): Boolean {
        val paths = arrayOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server",
            "/system/bin/frida-server",
            "/system/xbin/frida-server",
        )
        if (paths.any { File(it).exists() }) return true
        return try {
            val maps = File("/proc/self/maps").readText()
            maps.contains("frida", ignoreCase = true) ||
                maps.contains("gadget", ignoreCase = true)
        } catch (_: Exception) {
            false
        }
    }

    private fun hasXposedArtifacts(): Boolean {
        return try {
            val maps = File("/proc/self/maps").readText()
            maps.contains("XposedBridge", ignoreCase = true) ||
                maps.contains("edxp", ignoreCase = true) ||
                maps.contains("lsposed", ignoreCase = true)
        } catch (_: Exception) {
            false
        } || File("/system/framework/XposedBridge.jar").exists()
    }

    private fun isDebuggerAttached(): Boolean {
        return android.os.Debug.isDebuggerConnected() || android.os.Debug.waitingForDebugger()
    }
}
