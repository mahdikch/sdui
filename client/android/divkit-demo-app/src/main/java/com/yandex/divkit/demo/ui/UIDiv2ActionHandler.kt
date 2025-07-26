package com.yandex.divkit.demo.ui

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import com.yandex.div.core.DivActionHandler
import com.yandex.div.core.DivViewFacade
import com.yandex.div.core.actions.DivActionTypedHandlerProxy.handleVisibilityAction
import com.yandex.div.core.downloader.DivDownloadActionHandler.canHandle
import com.yandex.div.core.downloader.DivDownloadActionHandler.handleVisibilityAction
import com.yandex.div.core.view2.Div2View
import com.yandex.div.data.VariableMutationException
import com.yandex.div.internal.Assert
import com.yandex.div.json.expressions.ExpressionResolver
import com.yandex.div2.DivAction
import com.yandex.div2.DivSightAction
import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.div.Div2Activity
import com.yandex.divkit.demo.div.DivActivity
import com.yandex.divkit.demo.persiandatepicker.PersianDatePickerDialog
import com.yandex.divkit.demo.persiandatepicker.api.PersianPickerDate
import com.yandex.divkit.demo.persiandatepicker.api.PersianPickerListener
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
import ir.nrdc.camera.Naji
import ir.nrdc.camera.com.github.dhaval2404.imagepicker.OnCallBackListener
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import saman.zamani.persiandate.PersianDateFormat.PersianDateNumberCharacter
import java.io.Serializable
import java.util.Calendar
import java.util.Date
import java.util.UUID


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
private const val AUTHORITY_SET_IMEI = "set_imei"
private const val AUTHORITY_SET_IP = "set_ip"
private const val AUTHORITY_SET_DEVICE_MODEL = "set_device_model"
private const val AUTHORITY_SET_VARIABLE_TO_DB = "set_variable_to_db"
private const val AUTHORITY_CHANGE_COLOR = "change_color"
private const val AUTHORITY_SET_CAPTCHA = "refresh_captcha"
private const val AUTHORITY_CALL_SERVICE = "call_service"
private const val AUTHORITY_DELETE_VARIABLES_FROM_DB = "delet_variables_frome_db"
private const val AUTHORITY_FORWARD_TO = "forward_to"
private const val AUTHORITY_SHOW_BOTTOM_SHEET_LIST = "bottom_sheet_list"
private const val AUTHORITY_SHOW_BOTTOM_SHEET_PLATE = "bottom_sheet_plate"
private const val AUTHORITY_SHOW_BOTTOM_SHEET_DIV = "bottom_sheet_div"
private const val AUTHORITY_SHOW_TIME_PICKER = "show_time_picker"
private const val AUTHORITY_SHOW_DATE_PICKER = "show_date_picker"
private const val AUTHORITY_SHOW_DATE_PICKER_GREGORIAN = "show_date_picker_gregorian"
private const val AUTHORITY_SHOW_IMAGE = "show_image"
private const val AUTHORITY_GET_PERSIAN_DATE = "get_persian_date"
private const val AUTHORITY_BOTTOM_SHEET_DISMISS = "bottom_sheet_dismiss"
private const val AUTHORITY_SET_VARIABLE_TO_BASE = "set_variable_to_base"
private const val AUTHORITY_CHECK_VERSION = "check_version"
private const val AUTHORITY_SET_OBJECT_TO_DB = "set_object_to_db"
private const val AUTHORITY_SET_PAGE_TO_DB = "set_page_to_db"
private const val AUTHORITY_GET_CAMERA_PERMISSION = "get_camera_permission"
private const val AUTHORITY_GET_LOCATION_PERMISSION = "get_location_permission"
private const val AUTHORITY_GET_WRITE_PERMISSION = "get_write_permission"
private const val AUTHORITY_GET_ALL_PERMISSION = "get_all_permission"
private const val AUTHORITY_UPDATE = "update"
private const val AUTHORITY_RESET_PHID = "reset_phid"
private const val AUTHORITY_START_RECORDIN = "start_recording"
const val SCHEME_DIV_ACTION = "div-action"

private const val ACTIVITY_DEMO = "demo"
private const val ACTIVITY_SAMPLES = "samples"
private const val ACTIVITY_REGRESSION = "regression"
private const val ACTIVITY_SETTINGS = "settings"
private const val ACTIVITY_MEHDI = "mehdi"

private const val PARAM_ACTIVITY = "activity"
private const val PARAM_SCREEN = "screen"
private const val PARAM_UPDATE_URL = "url"
private const val PARAM_TOAST = "massage"
private const val PARAM_DIALOG_TITLE = "title"
private const val PARAM_DIALOG_MASSAGE = "massage"
private const val PARAM_VARIABLE_NAME = "name"
private const val PARAM_VARIABLE_VALUE = "value"
private const val PARAM_CAPTCHA_NAME = "name"
private const val PARAM_CAPTCHA_IMAGE = "image"
private const val PARAM_BOTTOM_SHEET_LIST_ID = "id"
private const val PARAM_BOTTOM_SHEET_LIST_CODE_VAR = "code_var"
private const val PARAM_BOTTOM_SHEET_LIST_TITLE_VAR = "title_var"
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
private const val PARAM_SET_VEHICLE_TYPE = "vehicle_type"
private const val PARAM_SET_VARIABLE_TO_DB_KEY = "key"
private const val PARAM_SET_VARIABLE_TO_DB_value = "value"
private const val PARAM_FORWARD_TO_PATH = "name"
private const val PARAM_FORWARD_TO_SYSTEM = "system"
private const val PARAM_TIME_PICKER = "variableName"
private const val PARAM_DATE_PICKER = "variableName"
private const val PARAM_OPEN_CAMERA = "type"
private const val PARAM_OPEN_CAMERA_iranian = "iranian"
private const val PARAM_OPEN_CAMERA_OCR = "check_ocr"
private const val PARAM_CHECK_VERSION_NAME = "name"
private const val PARAM_SHOW_IMAGE = "image"
private const val PARAM_SET_PAGE_TO_DB = "key"


class UIDiv2ActionHandler(
    uriHandler: DivkitDemoUriHandler,
    private val context: Context,
    private val activity: Activity,
    private val lo: LifecycleOwner,
    private var loadScreenListener: LoadScreenListener,
    private val mehdiViewModel: MehdiViewModel?,
    private var btmSheet_div: BottomSheetDiv? = null
) : /*DemoDivActionHandler(uriHandler),*/ DivActionHandler(), BottomSheetPlate.PlateItemListener,
    OnCallBackListener,
    Serializable,
    AdapterBottomSheetSpinner.CustomItemListener {
    lateinit var view: DivViewFacade
//    private lateinit var btmSheet_div: BottomSheetDiv

    private var flag = 0;
    private var btmSheet_list = BottomSheetDialogFragment();
    private var naji = Naji();

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
//        val scopedResolver = findExpressionResolverById(view as Div2View, action.scopeId)
//        val localResolver = scopedResolver ?: resolver
//        if (DivActionTypedHandlerProxy.handleAction(action, view, localResolver)) {
//            return true
//        }
        val url =
            action.url?.evaluate(resolver) ?: return super.handleAction(action, view, resolver)

        if (action.url == null) return false
//        val uri = action.url!!.evaluate(resolver)

        return (handleActivityActionUrl(url, view) || SettingsActionHandler.handleActionUrl(url)
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

    @SuppressLint("HardwareIds", "SuspiciousIndentation")
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
        } else if (uri.authority == AUTHORITY_UPDATE) {
            val url = uri.getQueryParameter(PARAM_UPDATE_URL)
            if (url != null) {
                loadScreenListener.update(url)
            }

        }  else if (uri.authority == AUTHORITY_START_RECORDIN) {
           loadScreenListener.startRecording()

        } else if (uri.authority == AUTHORITY_GET_PERSIAN_DATE) {
            val varName = uri.getQueryParameter(PARAM_DATE_PICKER)
            val pdate =
                PersianDateFormat.format(
                    PersianDate(),
                    "Y/m/d",
                    PersianDateNumberCharacter.ENGLISH
                );
            val div2View = if (view is Div2View) view as Div2View? else null
            if (div2View != null) {
                if (varName != null) {
                    div2View.setVariable(
                        varName,
                        pdate
                    )
                }
            }

        } else if (uri.authority == AUTHORITY_GET_CAMERA_PERMISSION) {
//            if (ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.CAMERA
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                val permissions = arrayOf(Manifest.permission.CAMERA)
//
//            }
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//                EasyPermissions.requestPermissions(
//                    context,
//                    "You need to accept location , camera and storage permissions to use this app",
//                    103,
////                android.Manifest.permission.ACCESS_COARSE_LOCATION,
////                android.Manifest.permission.ACCESS_FINE_LOCATION,
////                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    android.Manifest.permission.CAMERA
//                )
//            } else {
//                EasyPermissions.requestPermissions(
//                    this,
//                    "You need to accept location , camera and storage permissions to use this app",
//                    103,
//                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION,
//                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
//
//                )
//            }

        } else if (uri.authority == AUTHORITY_GET_LOCATION_PERMISSION) {
            loadScreenListener.getLocationPermission()

//
        } else if (uri.authority == AUTHORITY_GET_CAMERA_PERMISSION) {
            loadScreenListener.getCameraPermission()

//
        } else if (uri.authority == AUTHORITY_GET_WRITE_PERMISSION) {
            loadScreenListener.getWritePermission()

//
        } else if (uri.authority == AUTHORITY_GET_ALL_PERMISSION) {
            loadScreenListener.getAllPermissions()

//
        } else if (uri.authority == AUTHORITY_OPEN_CAMERA) {

            val type = uri.getQueryParameter(PARAM_OPEN_CAMERA)
            val iranian = uri.getQueryParameter(PARAM_OPEN_CAMERA_iranian)
            naji.openCamera(
                context as Activity, this, type, uri.getQueryParameter(
                    PARAM_OPEN_CAMERA_OCR
                ), iranian
            )

        } else if (uri.authority == AUTHORITY_CHECK_VERSION) {

            val name = uri.getQueryParameter(PARAM_CHECK_VERSION_NAME)


        } else if (uri.authority == AUTHORITY_SET_OBJECT_TO_DB) {

            val parameterNames = uri.queryParameterNames
            val map: MutableMap<String, String> = HashMap()
            var sysName = ""
            for (parameterName in parameterNames) {
                map[parameterName] = uri.getQueryParameter(parameterName).toString()
                if (parameterName == "next")
                    sysName = uri.getQueryParameter(parameterName).toString()
            }

            var username = mehdiViewModel?.getValueByKey("userName")?.value
            val pdf = PersianDateFormat("Y/m/d-H:i:s")
            val currentDate = pdf.format(PersianDate())
            map.put("offline_register_date", currentDate)
            val json = Gson().toJson(map).toString()

            mehdiViewModel?.insertItemToDb(PhPlusDB(null, "$username/$sysName/$currentDate", json))

        } else if (uri.authority == AUTHORITY_SET_PAGE_TO_DB) {
            val key = uri.getQueryParameter(PARAM_SET_PAGE_TO_DB)
            if (key != null) {
                loadScreenListener.setPageToDB(key)
            }
//            val parameterNames = uri.queryParameterNames
//            val map: MutableMap<String, String> = HashMap()
//            var sysName = ""
//            for (parameterName in parameterNames) {
//                map[parameterName] = uri.getQueryParameter(parameterName).toString()
//                if (parameterName == "next")
//                    sysName = uri.getQueryParameter(parameterName).toString()
//            }
//
//            var username = mehdiViewModel?.getValueByKey("userName")?.value
//            val pdf = PersianDateFormat("Y/m/d-H:i:s")
//            val currentDate = pdf.format(PersianDate())
//            map.put("offline_register_date", currentDate)
//            val json = Gson().toJson(map).toString()
//
//            mehdiViewModel?.insertItemToDb(PhPlusDB(null, "$username/$sysName/$currentDate", json))

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
                    if (picker.minute.toString().length == 1) {
                        div2View.setVariable(
                            varName,
                            picker.hour.toString() + ":0" + picker.minute.toString()
                        )
                    } else {
                        div2View.setVariable(
                            varName,
                            picker.hour.toString() + ":" + picker.minute.toString()
                        )
                    }
                }

            }

        } else if (uri.authority == AUTHORITY_SHOW_DATE_PICKER) {
            val div2View: Div2View = view as Div2View
            var varName = uri.getQueryParameter(PARAM_TIME_PICKER)

            var picker: PersianDatePickerDialog? = null
            picker = PersianDatePickerDialog(context)
                .setPositiveButtonString("باشه")
                .setNegativeButton("بیخیال")
                .setTodayButton("امروز")
                .setTodayButtonVisible(true)
                .setMinYear(1300)
                .setAllButtonsTextSize(12)
                .setMaxYear(1500)
                .setInitDate(1370, 3, 13)
                .setActionTextColor(Color.GRAY)
                //.setTypeFace(typeface) //  .setShowDayPicker(false)
                .setTitleType(PersianDatePickerDialog.DAY_MONTH_YEAR)
                .setShowInBottomSheet(true)
                .setListener(object : PersianPickerListener {
                    override fun onDateSelected(persianPickerDate: PersianPickerDate) {
                        if (varName != null) {
                            div2View.setVariable(
                                varName,
                                (persianPickerDate.getPersianYear()
                                    .toString() + "/" + persianPickerDate.getPersianMonth()).toString() + "/" + persianPickerDate.getPersianDay()
                                    .toString()
                            )
                        }
//                        Toast.makeText(
//                            context,
//                            (persianPickerDate.getPersianYear().toString() + "/" + persianPickerDate.getPersianMonth()).toString() + "/" + persianPickerDate.getPersianDay().toString(),
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }

                    override fun onDismissed() {
                        Toast.makeText(context, "Dismissed", Toast.LENGTH_SHORT).show()
                    }
                })

            picker.show()


        } else if (uri.authority == AUTHORITY_SHOW_DATE_PICKER_GREGORIAN) {
            val div2View: Div2View = view as Div2View
            var varName = uri.getQueryParameter(PARAM_DATE_PICKER)

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
                    val date = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                    if (varName != null) {
                        div2View.setVariable(varName, date)
                    }
                }, year, month, day)

            datePickerDialog.show()


        } else if (uri.authority == AUTHORITY_SET_PATCH) {

            val div2View: Div2View = view as Div2View
            var patchName = uri.getQueryParameter(PARAM_SET_PATCH)
            var dismiss = "false"
            dismiss = uri.getQueryParameter(PARAM_SET_PATCH_BOTTOMSHEET_DISMISS).toString()

            var patchTitle = ""
            var vehicle_type = ""
            patchTitle = uri.getQueryParameter(PARAM_SET_PATCH_TITLE).toString()
            vehicle_type = uri.getQueryParameter(PARAM_SET_VEHICLE_TYPE).toString()
            var json = ""
//            VariableToGet.value.value = uri.getQueryParameter(PARAM_SET_PATCH)
            if (patchName != null) {
                json = mehdiViewModel?.getValueByKey(patchName)?.value.toString()
            }
            if (patchName != null) {
                loadScreenListener.onApplyOnbase(json, patchName, patchTitle, vehicle_type)

            }
//            view.applyPatch(JSONObject(json).asDivPatchWithTemplates())

        } else if (uri.authority == AUTHORITY_CALL_SERVICE) {
            val parameterNames = uri.queryParameterNames
            val map: java.util.HashMap<String, String> = HashMap()
            var parametersMap: java.util.HashMap<String, String> = HashMap<String, String>()
            for (parameterName in parameterNames) {
                map[parameterName] = uri.getQueryParameter(parameterName).toString()
            }
            loadScreenListener.onRequest(map)


//            RemoteData.value.value = map

        } else if (uri.authority == AUTHORITY_DELETE_VARIABLES_FROM_DB) {
            val parameterNames = uri.queryParameterNames
            for (parameterName in parameterNames) {
                mehdiViewModel?.deleteItemFromDb(parameterName)
            }


//            RemoteData.value.value = map

        } else if (uri.authority == AUTHORITY_SET_VARIABLE) {
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
                        it[0].value?.let { it1 -> div2View?.setVariable(name, it1) }

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
        } else if (uri.authority == AUTHORITY_SET_IMEI) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)

            if (name != null) {
//                    json == it[0].value
                val div2View = if (view is Div2View) view as Div2View? else null
                val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                div2View?.setVariable(name, androidId)
            }
            return true
        }  else if (uri.authority == AUTHORITY_SET_IP) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)

            if (name != null) {
//                    json == it[0].value
                val div2View = if (view is Div2View) view as Div2View? else null
                val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                div2View?.setVariable(name, androidId)
            }
            return true
        }  else if (uri.authority == AUTHORITY_SET_DEVICE_MODEL) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)

            if (name != null) {
//                    json == it[0].value
                val div2View = if (view is Div2View) view as Div2View? else null
                val androidId =android.os.Build.MODEL

//                val androidId =
//                    Settings.Secure.getString(context.contentResolver, Settings.Secure.)
                div2View?.setVariable(name, androidId)
            }
            return true
        }else if (uri.authority == AUTHORITY_RESET_PHID) {
            var phId = UUID.randomUUID().toString()
            mehdiViewModel?.insertItemToDb(PhPlusDB(null, "phid", phId))
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
        } else if (uri.authority == AUTHORITY_SHOW_IMAGE) {
            val image = uri.getQueryParameter(PARAM_SHOW_IMAGE)
            var cleanedBase64String = image?.replace(" ", "+")
//                        cleanedBase64String =cleanedBase64String?.replace("\n", "")
//                        cleanedBase64String =cleanedBase64String?.replace("\\s", "")

//            val test= cleanedBase64String?.replace("[\\r\\n]".toRegex(), "")
//val test="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AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
            // Add padding if necessary
            val imageBytes = Base64.decode(cleanedBase64String, Base64.DEFAULT)
            val decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//            loadScreenListener.showImage(decodedBitmap)
//            dialogImage.setImageBitmap(decodedBitmap)

            val dialogView = LayoutInflater.from(context)
                .inflate(com.yandex.divkit.demo.R.layout.dialog_image, null)
            val dialogImage =
                dialogView.findViewById<ImageView>(com.yandex.divkit.demo.R.id.dialogImageView)
            dialogImage.setImageBitmap(decodedBitmap)

            AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("بستن", null)
                .create()
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
                    loadScreenListener.setVariableToBase(name, value)
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
                uri.getQueryParameter(PARAM_BOTTOM_SHEET_LIST_CODE_VAR).toString(),
                uri.getQueryParameter(PARAM_BOTTOM_SHEET_LIST_TITLE_VAR).toString(),
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

    override fun onClicked(
        programs: ListItemDto,
        varName: String,
        titleVar: String,
        codeVar: String
    ) {

        btmSheet_list.dismiss()
        val div2View = if (view is Div2View) view as Div2View? else null
        div2View?.setVariable(codeVar, programs.id)
        div2View?.setVariable(titleVar, programs.titleFa)
//        try {
//            when (varName) {
//                "usage" -> {
//                    div2View?.setVariable("variable_usage_code", programs.id)
//                    div2View?.setVariable("variable_usage_title", programs.titleFa)
//                }
//
//                "system" -> {
//                    div2View?.setVariable("variable_system_code", programs.id)
//                    div2View?.setVariable("variable_system_title", programs.titleFa)
//                }
//
//                "color" -> {
//                    div2View?.setVariable("variable_color_code", programs.id)
//                    div2View?.setVariable("variable_color_title", programs.titleFa)
//                }
//
//                "violation1" -> {
//                    div2View?.setVariable("violation1_code", programs.id)
//                    div2View?.setVariable("violation1_title", programs.titleFa)
//                }
//
//                "violation2" -> {
//                    div2View?.setVariable("violation2_code", programs.id)
//                    div2View?.setVariable("violation2_title", programs.titleFa)
//                }
//
//                "violation3" -> {
//                    div2View?.setVariable("violation3_code", programs.id)
//                    div2View?.setVariable("violation3_title", programs.titleFa)
//                }
//
//                "cityPolice" -> {
//                    div2View?.setVariable("cityPolice_code", programs.id)
//                    div2View?.setVariable("cityPolice_title", programs.titleFa)
//                }
//                "nationality" -> {
//                    div2View?.setVariable("nationality_code", programs.id)
//                    div2View?.setVariable("nationality", programs.titleFa)
//                }
//            }
//        } catch (e: VariableMutationException) {
//            Assert.fail("Variable '" + "plate" + "' mutation failed: " + e.message, e)
//        }
//        var patch: PhPlusDB? = mehdiViewModel?.getValueByKey(programs.patchName)

//        (view as Div2View).applyPatch(JSONObject(patch?.value).asDivPatchWithTemplates())

    }

    override fun getImageMainAndCropped(
        bitmapMain: String?,
        imageUriCrop: String?,
        type: String?,
        checkOcr: String?,
        iranian: String?
    ) {
        println("bitmapMain = ${bitmapMain}")
        val div2View = if (view is Div2View) view as Div2View? else null
        var cleanedBase64String = imageUriCrop?.replace(" ", "+")

        if (type == "car") {
            if (checkOcr == "true") {
                val map: java.util.HashMap<String, String> = HashMap()
                if (cleanedBase64String != null) {
                    map.put("path", "carPicture")
                    map.put("base64", cleanedBase64String)
                    map.put("ph/token", "empty")
                }
                loadScreenListener.onRequest(map)
            } else {
                div2View?.setVariable("variable_picture_visibility", "visible")
                if (cleanedBase64String != null) {
                    div2View?.setVariable("variable_car_picture", cleanedBase64String)
                }
            }
        }
        if (type == "ocr") {
            val map: java.util.HashMap<String, String> = HashMap()
            if (cleanedBase64String != null) {
                map.put("path", "ocrPicture")
                map.put("base64", cleanedBase64String)
                map.put("ph/token", "empty")

            }
            loadScreenListener.onRequest(map)
        }
        if (type == "person") {
            val map: java.util.HashMap<String, String> = HashMap()
            if (cleanedBase64String != null) {
                map.put("path", "inquiryBiometric/agh")
                map.put("personalImage", cleanedBase64String)
                map.put("ph/token", "empty")
                if (iranian == "true")
                    map.put("type", "3")
                else map.put("type", "2")
                map.put("status", "searchImage")
                map.put("sysName", "agh")

            }
            loadScreenListener.onRequest(map)
        }
    }

    override fun getImageMainCompress(bitmapMainCompress: String?, type: String?) {
        println("bitmapMain = ${bitmapMainCompress}")

    }

    override fun getImageCroppedCompress(imageUriCropCompress: String?, type: String?) {
        println("bitmapMain = ${imageUriCropCompress}")

    }

    fun showCalendar(v: View?) {
//        val typeface = Typeface.createFromAsset(getAssets(), "Shabnam-Light-FD.ttf")
//        val initDate: PersianCalendar = PersianCalendar()
//        initDate.setPersianDate(1370, 3, 13)
        var picker: PersianDatePickerDialog? = null
        picker = PersianDatePickerDialog(context)
            .setPositiveButtonString("باشه")
            .setNegativeButton("بیخیال")
            .setTodayButton("امروز")
            .setTodayButtonVisible(true)
            .setMinYear(1300)
            .setAllButtonsTextSize(12)
            .setMaxYear(1500)
            .setInitDate(1370, 3, 13)
            .setActionTextColor(Color.GRAY)
            //.setTypeFace(typeface) //  .setShowDayPicker(false)
            .setTitleType(PersianDatePickerDialog.DAY_MONTH_YEAR)
            .setShowInBottomSheet(true)
            .setListener(object : PersianPickerListener {
                override fun onDateSelected(persianPickerDate: PersianPickerDate) {

                    Toast.makeText(
                        context,
                        (persianPickerDate.getPersianYear()
                            .toString() + "/" + persianPickerDate.getPersianMonth()).toString() + "/" + persianPickerDate.getPersianDay()
                            .toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onDismissed() {
                    Toast.makeText(context, "Dismissed", Toast.LENGTH_SHORT).show()
                }
            })

        picker.show()
    }

//    override fun onClicked(programs: ListItemDto, varName: String) {
//        TODO("Not yet implemented")
//    }
//    private fun findExpressionResolverById(divView: Div2View, id: String?): ExpressionResolver? {
//        if (id == null) {
//            return null
//        }
//
//        val targetView = findSingleViewWithTag(divView, id)
//        if (targetView is DivHolderView<*>) {
//            val bindingContext = (targetView as DivHolderView<*>).bindingContext
//            if (bindingContext != null) {
//                return bindingContext.expressionResolver
//            }
//        }
//        return null
//    }
//    override fun loadscreen(json: String) {
//        startActivityForLoad(MehdiActivity::class.java,json)
//    }
}

