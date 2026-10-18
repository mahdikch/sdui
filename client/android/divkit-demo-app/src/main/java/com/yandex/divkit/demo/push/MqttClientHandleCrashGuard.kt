package com.yandex.divkit.demo.push

import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * NajiPush/Paho calls [ir.naja.pushsdk.android.service.MqttService.setBufferOpts] from
 * the MQTT connect-success broadcast on the main thread. If the client was already
 * disconnected, the SDK throws [IllegalArgumentException] ("Invalid ClientHandle")
 * and kills the process. This cannot be caught at the call site.
 */
object MqttClientHandleCrashGuard {
    private const val TAG = "MqttCrashGuard"

    @Volatile
    private var installed = false

    fun install() {
        if (installed) return
        installed = true

        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (isInvalidClientHandle(throwable)) {
                Log.e(TAG, "Ignored MQTT Invalid ClientHandle on ${thread.name}", throwable)
                if (thread != Looper.getMainLooper().thread) {
                    com.yandex.divkit.demo.DivkitApplication.getInstance()
                        ?.onMqttClientHandleCrash()
                }
                return@setDefaultUncaughtExceptionHandler
            }
            previous?.uncaughtException(thread, throwable)
        }

        Handler(Looper.getMainLooper()).post {
            while (true) {
                try {
                    Looper.loop()
                } catch (t: Throwable) {
                    if (isInvalidClientHandle(t)) {
                        Log.e(TAG, "Ignored MQTT Invalid ClientHandle on main looper", t)
                        com.yandex.divkit.demo.DivkitApplication.getInstance()
                            ?.onMqttClientHandleCrash()
                    } else {
                        throw t
                    }
                }
            }
        }
    }

    fun isInvalidClientHandle(throwable: Throwable): Boolean {
        var current: Throwable? = throwable
        while (current != null) {
            if (current is IllegalArgumentException &&
                current.message.orEmpty().contains("Invalid ClientHandle")
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }
}
