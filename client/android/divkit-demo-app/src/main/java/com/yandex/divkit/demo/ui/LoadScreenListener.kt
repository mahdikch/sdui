package com.yandex.divkit.demo.ui

import com.yandex.divkit.demo.ui.bottomSheetDiv.BottomSheetDiv

interface LoadScreenListener {
    fun onLoad(screenName: String)
    fun onRequest(request: MutableMap<String, String>)
    fun onApplyOnbase(json: String, patchName: String, patchTitle:String)
    fun getBtmSheetInstance(btmSheet: BottomSheetDiv)
    fun setVariableToBase(key: String, value: String)
    fun loadeScreenWithData(data: String, screenName: String)
    fun update(url: String)
}