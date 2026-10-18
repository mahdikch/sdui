package com.yandex.divkit.demo.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Multi-signal rooted-environment checks.
 *
 * Does **not** hard-block the app: many field/MDM devices report false positives.
 * Results are available via [lastResult] for telemetry / future server attestation.
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
        if (canExecuteWhichSu()) signals += "which-su"
        if (hasDangerousPackages(context)) signals += "root-apps"
        if (canWriteSystem()) signals += "rw-system"

        val result = Result(isSuspicious = signals.isNotEmpty(), signals = signals)
        lastResult = result
        if (result.isSuspicious) {
            Log.w(TAG, "Root signals detected: ${signals.joinToString()}")
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
        )
        return paths.any { File(it).exists() }
    }

    private fun hasMagiskArtifacts(): Boolean {
        val paths = arrayOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/data/adb/modules",
            "/cache/.disable_magisk",
        )
        return paths.any { File(it).exists() } ||
            File("/system/xbin/busybox").exists() ||
            File("/system/bin/busybox").exists()
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
}
