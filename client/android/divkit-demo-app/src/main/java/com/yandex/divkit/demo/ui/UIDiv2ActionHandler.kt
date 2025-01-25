package com.yandex.divkit.demo.ui

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.yandex.div.core.DivViewFacade
import com.yandex.div.core.view2.Div2View
import com.yandex.div.data.VariableMutationException
import com.yandex.div.internal.Assert
import com.yandex.div.json.expressions.ExpressionResolver
import com.yandex.div2.DivAction
import com.yandex.div2.DivSightAction
import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.div.DemoDivActionHandler
import com.yandex.divkit.demo.div.Div2Activity
import com.yandex.divkit.demo.div.DivActivity
import com.yandex.divkit.demo.settings.SettingsActionHandler
import com.yandex.divkit.demo.settings.SettingsActivity
import com.yandex.divkit.demo.ui.activity.MehdiActivity
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import com.yandex.divkit.demo.ui.bottomSheetDiv.BottomSheetDiv
import com.yandex.divkit.demo.ui.bottomSheetPlate.BottomSheetPlate
import com.yandex.divkit.demo.ui.bottomSheetSpinner.AdapterBottomSheetSpinner
import com.yandex.divkit.demo.ui.bottomSheetSpinner.BottomSheetSpinner
import com.yandex.divkit.demo.ui.toastDiv.CustomToast
import com.yandex.divkit.demo.utils.DivkitDemoUriHandler
import com.yandex.divkit.regression.RegressionActivity
import java.io.Serializable
import com.yandex.div.core.actions.DivActionTypedHandlerProxy.handleVisibilityAction
import com.yandex.div.core.downloader.DivDownloadActionHandler.canHandle
import com.yandex.div.core.downloader.DivDownloadActionHandler.handleVisibilityAction

private const val AUTHORITY_OPEN_SCREEN = "open_screen"
private const val AUTHORITY_SET_PATCH = "set_patch"
private const val AUTHORITY_LOAD_SCREEN = "load_screen"
private const val AUTHORITY_OPEN_CAMERA = "open_camera"
private const val AUTHORITY_SHOW_MASSAGE = "show_toast"
private const val AUTHORITY_SHOW_DIV_TOAST = "show_toast_div"
private const val AUTHORITY_SHOW_DIV_DIALOG = "show_dialog_div"
private const val AUTHORITY_SHOW_DIALOG = "show_dialog"
private const val AUTHORITY_SET_VARIABLE = "set_variable"
private const val AUTHORITY_SET_VARIABLE_FROM_DB = "set_variable_from_db"
private const val AUTHORITY_SET_VARIABLE_TO_DB = "set_variable_to_db"
private const val AUTHORITY_CHANGE_COLOR = "change_color"
private const val AUTHORITY_SET_CAPTCHA = "refresh_captcha"
private const val AUTHORITY_CALL_SERVICE = "call_service"
private const val AUTHORITY_FORWARD_TO = "forward_to"
private const val AUTHORITY_SHOW_BOTTOM_SHEET_LIST = "bottom_sheet_list"
private const val AUTHORITY_SHOW_BOTTOM_SHEET_PLATE = "bottom_sheet_plate"
private const val AUTHORITY_SHOW_BOTTOM_SHEET_DIV = "bottom_sheet_div"
private const val AUTHORITY_SHOW_TIME_PICKER = "show_time_picker"
private const val AUTHORITY_SHOW_DATE_PICKER = "show_date_picker"
private const val AUTHORITY_BOTTOM_SHEET_DISMISS = "bottom_sheet_dismiss"
private const val AUTHORITY_SET_VARIABLE_TO_BASE = "set_variable_to_base"
private const val AUTHORITY_CHECK_VERSION = "check_version"
const val SCHEME_DIV_ACTION = "div-action"

private const val ACTIVITY_DEMO = "demo"
private const val ACTIVITY_SAMPLES = "samples"
private const val ACTIVITY_REGRESSION = "regression"
private const val ACTIVITY_SETTINGS = "settings"
private const val ACTIVITY_MEHDI = "mehdi"

private const val PARAM_ACTIVITY = "activity"
private const val PARAM_SCREEN = "screen"
private const val PARAM_TOAST = "massage"
private const val PARAM_DIALOG_TITLE = "title"
private const val PARAM_DIALOG_MASSAGE = "massage"
private const val PARAM_VARIABLE_NAME = "name"
private const val PARAM_VARIABLE_VALUE = "value"
private const val PARAM_CAPTCHA_NAME = "name"
private const val PARAM_CAPTCHA_IMAGE = "image"
private const val PARAM_BOTTOM_SHEET_LIST_ID = "id"
private const val PARAM_CHECK_VERSION = "id"
private const val PARAM_BOTTOM_SHEET_LIST_TYPE = "type"
private const val PARAM_BOTTOM_SHEET_LIST_TITLE = "title"
private const val PARAM_BOTTOM_SHEET_LIST_SEARCHABLE = "SEARCHABLE"
private const val PARAM_BOTTOM_SHEET_PLATE_RESPONSE = "PLATE"
private const val PARAM_BOTTOM_SHEET_DIV = "jsonName"
private const val PARAM_TOAST_DIV = "jsonName"
private const val PARAM_DIALOG_DIV = "jsonName"
private const val PARAM_SET_PATCH = "patch"
private const val PARAM_SET_PATCH_BOTTOMSHEET_DISMISS = "btm_dismiss"
private const val PARAM_SET_PATCH_TITLE = "patch_title"
private const val PARAM_SET_VARIABLE_TO_DB_KEY = "key"
private const val PARAM_SET_VARIABLE_TO_DB_value = "value"
private const val PARAM_FORWARD_TO_PATH = "name"
private const val PARAM_FORWARD_TO_SYSTEM = "system"
private const val PARAM_TIME_PICKER = "variableName"
private const val PARAM_DATE_PICKER = "variableName"
private const val PARAM_OPEN_CAMERA = "type"
private const val PARAM_OPEN_CAMERA_OCR = "check_ocr"
private const val PARAM_CHECK_VERSION_NAME = "name"



class UIDiv2ActionHandler(
    uriHandler: DivkitDemoUriHandler,
    private val context: Context,
    private val activity: Activity,
    private val lo: LifecycleOwner,
    private var loadScreenListener: LoadScreenListener,
    private val mehdiViewModel: MehdiViewModel?,
    private  var btmSheet_div: BottomSheetDiv?=null
) : DemoDivActionHandler(uriHandler), BottomSheetPlate.PlateItemListener, /*OnCallBackListener,*/
    Serializable,
    AdapterBottomSheetSpinner.CustomItemListener {
    lateinit var view: DivViewFacade
//    private lateinit var btmSheet_div: BottomSheetDiv

    private var flag = 0;
    private var btmSheet_list = BottomSheetDialogFragment();
//    private var naji = Naji();

//    interface LoadScreenListener {
//        fun onLoad(screenName: String)
//        fun onRequest(request: MutableMap<String, String>)
//        fun onApply(json: String, patchName: String,patchTitle:String)
//
//    }

    override fun handleAction(
        action: DivAction,
        view: DivViewFacade,
        resolver: ExpressionResolver
    ): Boolean {

        if (action.url == null) return false
        val uri = action.url!!.evaluate(resolver)

        return (handleActivityActionUrl(uri, view) || SettingsActionHandler.handleActionUrl(uri)
                || super.handleAction(action, view, resolver))
    }

    override fun handleAction(
        action: DivSightAction,
        view: DivViewFacade,
        resolver: ExpressionResolver,
    ): Boolean {
        if (action.url == null) return false
        val uri = action.url!!.evaluate(resolver)
//        if (uri.authority != AUTHORITY_DOWNLOAD)
        return (handleActivityActionUrl(uri, view) || SettingsActionHandler.handleActionUrl(uri)
                || super.handleAction(action, view, resolver))
//        else
//            return (handleDownloadActionVisibility(
//                uri,
//                view,
//                action,
//                resolver
//            ) || SettingsActionHandler.handleActionUrl(uri)
//                    || super.handleAction(action, view, resolver))
    }

    private fun handleDownloadActionVisibility(
        uri: Uri,
        view: DivViewFacade,
        action: DivSightAction,
        resolver: ExpressionResolver
    ): Boolean {
        if (handleVisibilityAction(action, view, resolver)) {
            return true
        }
        val url = if (action.url != null) action.url!!.evaluate(resolver) else null
        return if (canHandle(url, view)) {
            handleVisibilityAction(action, (view as Div2View), resolver)
        } else handleActionUrl(url, view, resolver)
    }

    private fun handleActivityActionUrl(uri: Uri, view: DivViewFacade): Boolean {
        this.view = view
        if (uri.scheme != SCHEME_DIV_ACTION) return false
        if (uri.authority == AUTHORITY_OPEN_SCREEN) {
            when (uri.getQueryParameter(PARAM_ACTIVITY)) {
                ACTIVITY_DEMO -> startActivityAction(Div2Activity::class.java)
                ACTIVITY_REGRESSION -> startActivityAction(RegressionActivity::class.java)
                ACTIVITY_SAMPLES -> startActivityAction(DivActivity::class.java)
                ACTIVITY_SETTINGS -> startActivityAction(SettingsActivity::class.java)
                ACTIVITY_MEHDI -> startActivityAction(MehdiActivity::class.java)
                else -> return false
            }
        } else if (uri.authority == AUTHORITY_LOAD_SCREEN) {
//            ScreenToLoad.value.value = uri.getQueryParameter(PARAM_SCREEN)
            uri.getQueryParameter(PARAM_SCREEN)?.let {
                loadScreenListener.onLoad(it)
            }

//            startActivityForLoad(
//                MehdiActivity::class.java,
//                uri.getQueryParameter(PARAM_SCREEN).toString()
//            )
        } else if (uri.authority == AUTHORITY_FORWARD_TO) {
            val parameterNames = uri.queryParameterNames
            val map: MutableMap<String, String> = HashMap()
            var parametersMap: MutableMap<String, String> = HashMap<String, String>()
            for (parameterName in parameterNames) {
                map[parameterName] = uri.getQueryParameter(parameterName).toString()
            }
            loadScreenListener.onRequest(map)

//
        } else if (uri.authority == AUTHORITY_OPEN_CAMERA) {

            val type = uri.getQueryParameter(PARAM_OPEN_CAMERA)
//            naji.openCamera(context as Activity, this, type,uri.getQueryParameter(
//                PARAM_OPEN_CAMERA_OCR))

        } else if (uri.authority == AUTHORITY_CHECK_VERSION) {

            val name = uri.getQueryParameter(PARAM_CHECK_VERSION_NAME)


        } else if (uri.authority == AUTHORITY_SHOW_TIME_PICKER) {
            val div2View: Div2View = view as Div2View

            val picker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setInputMode(INPUT_MODE_CLOCK)
                    .setHour(12)
                    .setMinute(10)
                    .setTitleText("انتخاب زمان")
                    .build()
            (context as? AppCompatActivity)?.supportFragmentManager?.let { picker.show(it, "time") }
            var varName = uri.getQueryParameter(PARAM_TIME_PICKER)

            picker.addOnPositiveButtonClickListener {
                if (varName != null) {
                    if (picker.minute.toString().length==1){
                    div2View.setVariable(
                        varName,
                        picker.hour.toString() + ":0" + picker.minute.toString()
                    )}
                    else{
                        div2View.setVariable(
                            varName,
                            picker.hour.toString() + ":" + picker.minute.toString()
                        )
                    }
                }

            }

        } else if (uri.authority == AUTHORITY_SET_PATCH) {

            val div2View: Div2View = view as Div2View
            var patchName = uri.getQueryParameter(PARAM_SET_PATCH)
            var dismiss = "false"
            dismiss = uri.getQueryParameter(PARAM_SET_PATCH_BOTTOMSHEET_DISMISS).toString()

            var patchTitle = ""
            patchTitle = uri.getQueryParameter(PARAM_SET_PATCH_TITLE).toString()
            var json = ""
//            VariableToGet.value.value = uri.getQueryParameter(PARAM_SET_PATCH)
            if (patchName != null) {
                json = mehdiViewModel?.getValueByKey(patchName)?.value.toString()
            }
            if (patchName != null) {
                loadScreenListener.onApplyOnbase(json, patchName, patchTitle)

            }
//            view.applyPatch(JSONObject(json).asDivPatchWithTemplates())

        } else if (uri.authority == AUTHORITY_CALL_SERVICE) {
            val parameterNames = uri.queryParameterNames
            val map: MutableMap<String, String> = HashMap()
            var parametersMap: MutableMap<String, String> = HashMap<String, String>()
            for (parameterName in parameterNames) {
                map[parameterName] = uri.getQueryParameter(parameterName).toString()
            }
            loadScreenListener.onRequest(map)


//            RemoteData.value.value = map

        }
        else if (uri.authority == AUTHORITY_SET_VARIABLE) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)
            if (name == null) {
                Assert.fail(PARAM_VARIABLE_NAME + " param is required")
                return false
            }
            val value = uri.getQueryParameter(PARAM_VARIABLE_VALUE)
            if (value == null) {
                Assert.fail(PARAM_VARIABLE_VALUE + " param unspecified for " + name)
                return false
            }

            val div2View = if (view is Div2View) view as Div2View? else null

            if (div2View == null) {
                Assert.fail(
                    "Variable '" + name + "' mutation failed! View(" +
                            view.javaClass.getSimpleName() + ") not supports variables!"
                )
                return false
            }
            try {
                div2View.setVariable(name, value)
            } catch (e: VariableMutationException) {
                Assert.fail("Variable '" + name + "' mutation failed: " + e.message, e)
                return false
            }
            return true
        } else if (uri.authority == AUTHORITY_SET_VARIABLE_FROM_DB) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)
            if (name == null) {
                Assert.fail(PARAM_VARIABLE_NAME + " param is required")
                return false
            }
//            var json = ""
//            VariableToGet.value.value = uri.getQueryParameter(PARAM_SET_PATCH)
            if (name != null) {
                mehdiViewModel?.getPage(name)?.observe(lo) {
//                    json == it[0].value
                    val div2View = if (view is Div2View) view as Div2View? else null
                    if (it.isNotEmpty())
                        div2View?.setVariable(name, it[0].value)

                }
            }
//            VariableToGet.value.value = name
//            if (flag  == 1)
//            VariableToSet.value.observe(lo) {
//                val div2View = if (view is Div2View) view as Div2View? else null
//                try {
//                    div2View?.setVariable(name, it)
//                } catch (e: VariableMutationException) {
//                    Assert.fail("Variable '" + name + "' mutation failed: " + e.message, e)
//                }
//            }

            return true
        } else if (uri.authority == AUTHORITY_SET_VARIABLE_TO_DB) {
            val key = uri.getQueryParameter(PARAM_SET_VARIABLE_TO_DB_KEY)
            val value = uri.getQueryParameter(PARAM_SET_VARIABLE_TO_DB_value)
            if (key == null) {
                Assert.fail(PARAM_VARIABLE_NAME + " param is required")
                return false
            }

            if (key != null) {
                value?.let { PhPlusDB(null, key, it) }?.let { mehdiViewModel?.insertItemToDb(it) }
            }
            return true
        } else if (uri.authority == AUTHORITY_CHANGE_COLOR) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)
            if (name == null) {
                Assert.fail(PARAM_VARIABLE_NAME + " param is required")
                return false
            }
            var value = uri.getQueryParameter(PARAM_VARIABLE_VALUE)
            value = "#" + value
            if (value == null) {
                Assert.fail(PARAM_VARIABLE_VALUE + " param unspecified for " + name)
                return false
            }

            val div2View = if (view is Div2View) view as Div2View? else null

            if (div2View == null) {
                Assert.fail(
                    "Variable '" + name + "' mutation failed! View(" +
                            view.javaClass.getSimpleName() + ") not supports variables!"
                )
                return false
            }
            try {
                div2View.setVariable(name, value)
            } catch (e: VariableMutationException) {
                Assert.fail("Variable '" + name + "' mutation failed: " + e.message, e)
                return false
            }
            return true
        } else if (uri.authority == AUTHORITY_SET_CAPTCHA) {
            val name = uri.getQueryParameter(PARAM_CAPTCHA_NAME)
            if (name == null) {
                Assert.fail(PARAM_CAPTCHA_NAME + " param is required")
                return false
            }
            val image = "divkit-asset://login.png"
//            if (value == null) {
//                Assert.fail(PARAM_VARIABLE_VALUE + " param unspecified for " + image)
//                return false
//            }

            val div2View = if (view is Div2View) view as Div2View? else null

            if (div2View == null) {
                Assert.fail(
                    "Variable '" + name + "' mutation failed! View(" +
                            view.javaClass.getSimpleName() + ") not supports variables!"
                )
                return false
            }
            try {
                div2View.setVariable(name, image)
            } catch (e: VariableMutationException) {
                Assert.fail("Variable '" + name + "' mutation failed: " + e.message, e)
                return false
            }
            return true
        } else if (uri.authority == AUTHORITY_SHOW_DIALOG) {
            AlertDialog.Builder(context)
                .setTitle(uri.getQueryParameter(PARAM_DIALOG_TITLE))
                .setMessage(uri.getQueryParameter(PARAM_DIALOG_MASSAGE)) // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(
                    R.string.yes,
                    DialogInterface.OnClickListener { dialog, which ->
                        // Continue with delete operation
                    }) // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(R.string.no, null)
                .setIcon(R.drawable.ic_dialog_alert)
                .show()
        } else if (uri.authority == AUTHORITY_SHOW_MASSAGE) {

            Toast.makeText(context, uri.getQueryParameter(PARAM_TOAST), Toast.LENGTH_LONG).show()

        } else if (uri.authority == AUTHORITY_BOTTOM_SHEET_DISMISS) {
            btmSheet_div?.dismiss()
        } else if (uri.authority == AUTHORITY_SET_VARIABLE_TO_BASE) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)
            val value = uri.getQueryParameter(PARAM_VARIABLE_VALUE)
            if (name != null) {
                if (value != null) {
                    loadScreenListener.setVariableToBase(name,value)
                }
            }
//            btmSheet_div.dismiss()
        } else if (uri.authority == AUTHORITY_SHOW_DIV_TOAST) {

            val JsonDto = uri.getQueryParameter(PARAM_TOAST_DIV)
                ?.let { mehdiViewModel?.getValueByKey(it) }
            val json = JsonDto?.value
            val customToast = mehdiViewModel?.let { CustomToast(context, lo, it, activity) }
            if (json != null) {
                customToast?.show(
                    json = json,
                    duration = Toast.LENGTH_LONG
                )
            }


        } else if (uri.authority == AUTHORITY_SHOW_BOTTOM_SHEET_LIST) {
//            Toast.makeText(context, uri.getQueryParameter(PARAM_TOAST), Toast.LENGTH_LONG).show()
            val parameterNames = uri.queryParameterNames
            val map: MutableMap<String, String> = HashMap()
            for (parameterName in parameterNames) {
                map[parameterName] = uri.getQueryParameter(parameterName).toString()
            }
            btmSheet_list = BottomSheetSpinner(
                context,
                this,
                uri.getQueryParameter(PARAM_BOTTOM_SHEET_LIST_ID).toString(),
                uri.getQueryParameter(PARAM_BOTTOM_SHEET_LIST_TITLE).toString(),
                view,
                map
            )
            btmSheet_list.show(
                (context as FragmentActivity).supportFragmentManager,
                "bottomSheetList"
            )

        } else if (uri.authority == AUTHORITY_SHOW_BOTTOM_SHEET_PLATE) {
//            Toast.makeText(context, uri.getQueryParameter(PARAM_TOAST), Toast.LENGTH_LONG).show()
            BottomSheetPlate(
                this
            ).show((context as FragmentActivity).supportFragmentManager, "bottomSheetList")

        } else if (uri.authority == AUTHORITY_SHOW_BOTTOM_SHEET_DIV) {
//            Toast.makeText(
//                context,
//                uri.getQueryParameter(PARAM_BOTTOM_SHEET_DIV),
//                Toast.LENGTH_LONG
//            ).show()
            if (mehdiViewModel != null) {

                uri.getQueryParameter(PARAM_BOTTOM_SHEET_DIV)?.let {
                    btmSheet_div = BottomSheetDiv(
                        context, activity, it, mehdiViewModel, lo
                    )
                    btmSheet_div?.show(
                        (context as FragmentActivity).supportFragmentManager,
                        "bottomSheetList"
                    )
                }
                btmSheet_div?.let { loadScreenListener.getBtmSheetInstance(it) }
            }

        }
//
//        VariableToSetMap.value.observe(lo) {
//            val div2View = if (view is Div2View) view as Div2View? else null
//            for (phPlusDB in it) {
//                try {
//                    if (phPlusDB.key!=null)
//                    div2View?.setVariable(phPlusDB.key, phPlusDB.value)
//                } catch (e: VariableMutationException) {
//                    Assert.fail("Variable '" + phPlusDB.key + "' mutation failed: " + e.message, e)
//                }
//            }
//
//        }
        flag++
        return true
    }

    private fun startActivityAction(klass: Class<out Activity>) {
        if (klass.name == "com.yandex.divkit.demo.ui.activity.MehdiActivity") {
            val intent = Intent(context, klass)
            intent.putExtra("json", "mehdi")
            startActivity(context, intent, null)
        } else {
            startActivity(context, Intent(context, klass), null)
        }
    }

    private fun showBottomSheetList(klass: Class<out Activity>) {
//        BottomSheetSpinner(1).show((context as FragmentActivity).supportFragmentManager, "bottomSheetList")
    }

    private fun startActivityForLoad(klass: Class<out Activity>, jsonName: String) {


        val intent = Intent(context, klass)
        intent.putExtra("json", jsonName)
        startActivity(context, intent, null)


    }

    override fun onClicked(plate: String, color: String, textColor: String) {
        val div2View = if (view is Div2View) view as Div2View? else null
        try {
            div2View?.setVariable("plate", plate)
            div2View?.setVariable("plate1_color", color)
            div2View?.setVariable("plate2_color", color)
            div2View?.setVariable("plate3_color", color)
            div2View?.setVariable("plate4_color", color)
            div2View?.setVariable("plate_text_color", textColor)
        } catch (e: VariableMutationException) {
            Assert.fail("Variable '" + "plate" + "' mutation failed: " + e.message, e)
        }
    }

    override fun onClicked(programs: ListItemDto, varName: String) {
        btmSheet_list.dismiss()
        val div2View = if (view is Div2View) view as Div2View? else null
        try {
            when (varName) {
                "usage" -> {
                    div2View?.setVariable("usage_code", programs.id)
                    div2View?.setVariable("usage_title", programs.titleFa)
                }

                "system" -> {
                    div2View?.setVariable("system_code", programs.id)
                    div2View?.setVariable("system_title", programs.titleFa)
                }

                "color" -> {
                    div2View?.setVariable("color_code", programs.id)
                    div2View?.setVariable("color_title", programs.titleFa)
                }

                "violation1" -> {
                    div2View?.setVariable("violation1_code", programs.id)
                    div2View?.setVariable("violation1_title", programs.titleFa)
                }

                "violation2" -> {
                    div2View?.setVariable("violation2_code", programs.id)
                    div2View?.setVariable("violation2_title", programs.titleFa)
                }

                "violation3" -> {
                    div2View?.setVariable("violation3_code", programs.id)
                    div2View?.setVariable("violation3_title", programs.titleFa)
                }

                "cityPolice" -> {
                    div2View?.setVariable("cityPolice_code", programs.id)
                    div2View?.setVariable("cityPolice_title", programs.titleFa)
                }
            }
        } catch (e: VariableMutationException) {
            Assert.fail("Variable '" + "plate" + "' mutation failed: " + e.message, e)
        }
//        var patch: PhPlusDB? = mehdiViewModel?.getValueByKey(programs.patchName)

//        (view as Div2View).applyPatch(JSONObject(patch?.value).asDivPatchWithTemplates())

    }

//    override fun getImageMainAndCropped(bitmapMain: String?, imageUriCrop: String?, type: String?,checkOcr:String?) {
//        println("bitmapMain = ${bitmapMain}")
//        val div2View = if (view is Div2View) view as Div2View? else null
//
//        if (type == "car") {
//            if (checkOcr=="true"){
//            val map: MutableMap<String, String> = HashMap()
//            if (imageUriCrop != null) {
//                map.put("path", "carPicture")
//                map.put("base64", imageUriCrop)
//                map.put("ph/token", "empty")
//            }
//            loadScreenListener.onRequest(map)}
//            else{
//                div2View?.setVariable("variable_picture_visibility","visible")
//                if (imageUriCrop != null) {
//                    div2View?.setVariable("variable_car_picture",imageUriCrop)
//                }
//            }
//        }
//        if (type == "ocr") {
//            val map: MutableMap<String, String> = HashMap()
//            if (imageUriCrop != null) {
//                map.put("path", "ocrPicture")
//                map.put("base64", imageUriCrop)
//                map.put("ph/token", "empty")
//
//            }
//            loadScreenListener.onRequest(map)
//        }
//        if (type == "person") {
//            val map: MutableMap<String, String> = HashMap()
//            if (imageUriCrop != null) {
//                map.put("path", "parsonPicture")
//                map.put("base64", imageUriCrop)
//                map.put("ph/token", "empty")
//
//            }
//            loadScreenListener.onRequest(map)
//        }
//    }
//
//    override fun getImageMainCompress(bitmapMainCompress: String?, type: String?) {
//        println("bitmapMain = ${bitmapMainCompress}")
//
//    }
//
//    override fun getImageCroppedCompress(imageUriCropCompress: String?, type: String?) {
//        println("bitmapMain = ${imageUriCropCompress}")
//
//    }

//    override fun loadscreen(json: String) {
//        startActivityForLoad(MehdiActivity::class.java,json)
//    }
}

