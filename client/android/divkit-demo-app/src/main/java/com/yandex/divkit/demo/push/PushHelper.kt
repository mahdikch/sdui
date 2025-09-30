package com.yandex.divkit.demo.push

import android.app.Activity
import android.util.Log
import com.yandex.divkit.demo.DivkitApplication
import ir.naja.pushsdk.android.Constant

object PushHelper {
    private const val TAG = "PushHelper"
    
    /**
     * Call this method from MehdiActivity.onCreate() to complete push setup
     * 
     * SAFE TO CALL MULTIPLE TIMES - Setup happens only once, subsequent calls are ignored
     * This is needed because ActiveNajiFication requires an Activity context
     * 
     * Uses API_KEY and USERNAME from ir.naja.pushsdk.android.Constant
     */
    fun setupPushNotifications(activity: Activity) {
        try {
            val app = DivkitApplication.getInstance()
            if (app != null) {
                app.completePushSetup(activity)
                Log.d(TAG, "Push notifications setup call from activity")
            } else {
                Log.e(TAG, "DivkitApplication instance is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up push notifications", e)
        }
    }
}
