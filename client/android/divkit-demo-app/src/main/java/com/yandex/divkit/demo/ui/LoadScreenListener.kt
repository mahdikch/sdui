package com.yandex.divkit.demo.ui

import android.graphics.Bitmap
import com.yandex.div.core.util.ImageRepresentation
import com.yandex.divkit.demo.ui.bottomSheetDiv.BottomSheetDiv
import java.util.HashMap

interface LoadScreenListener {
    fun     onLoad(screenName: String)
    fun onRequest(request: HashMap<String, String>)
    fun onApplyOnbase(json: String, patchName: String, patchTitle:String,vehicleType:String)
    fun getBtmSheetInstance(btmSheet: BottomSheetDiv)
    fun setVariableToBase(key: String, value: String)
    fun loadeScreenWithData(data: String, screenName: String)
    fun update(url: String)
    fun getLocationPermission()
    fun getWritePermission()
    fun getCameraPermission()
    fun getAllPermissions()
    fun setPageToDB(key:String)
    fun startRecording(recordingId: String? = null)
    fun stopRecording()
    fun onAudioPermissionGranted()
//    fun showImage(decodedBitmap: Bitmap)
}