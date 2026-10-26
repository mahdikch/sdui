package com.yandex.divkit.demo.ui

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import com.yandex.div.core.DivActionHandler
import com.yandex.div.core.DivViewFacade
import com.yandex.div.core.actions.DivActionTypedHandlerProxy
import com.yandex.div.core.actions.DivActionTypedHandlerProxy.handleVisibilityAction
import com.yandex.div.core.downloader.DivDownloadActionHandler.canHandle
import com.yandex.div.core.downloader.DivDownloadActionHandler.handleVisibilityAction
import com.yandex.div.core.view2.Div2View
import com.yandex.div.data.Variable
import com.yandex.div.data.VariableMutationException
import com.yandex.div.internal.Assert
import com.yandex.div.internal.Log
import com.yandex.div.json.expressions.ExpressionResolver
import com.yandex.div2.DivAction
import com.yandex.div2.DivSightAction
import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.div.Div2Activity
import com.yandex.divkit.demo.div.DivActivity
import com.yandex.divkit.demo.div.DynamicPatchGenerator
import com.yandex.divkit.demo.div.DynamicPatchGeneratorSingleton
import com.yandex.divkit.demo.div.asDivPatchWithTemplates
import com.yandex.divkit.demo.persiandatepicker.PersianDatePickerDialog
import com.yandex.divkit.demo.persiandatepicker.api.PersianPickerDate
import com.yandex.divkit.demo.persiandatepicker.api.PersianPickerListener
import com.yandex.divkit.demo.service.LocationService
import com.yandex.divkit.demo.settings.SettingsActionHandler
import com.yandex.divkit.demo.settings.SettingsActivity
import com.yandex.divkit.demo.ui.activity.MehdiActivity
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import com.yandex.divkit.demo.ui.bottomSheetDiv.BottomSheetDiv
import com.yandex.divkit.demo.ui.bottomSheetDiv.BottomSheetManager
import com.yandex.divkit.demo.ui.bottomSheetPlate.BottomSheetPlate
import com.yandex.divkit.demo.ui.bottomSheetSpinner.AdapterBottomSheetSpinner
import com.yandex.divkit.demo.ui.bottomSheetSpinner.BottomSheetSpinner
import com.yandex.divkit.demo.ui.DateTimeCompareActionHelper.getTrimmedQueryParameter
import com.yandex.divkit.demo.ui.toastDiv.CustomToast
import com.yandex.divkit.demo.utils.UncompromisingPreviewLoader
import com.yandex.divkit.demo.utils.VehicleEntryCountHelper
import com.yandex.divkit.demo.utils.Div2ViewVariableExporter
import com.yandex.divkit.demo.utils.DivkitDemoUriHandler
import com.yandex.divkit.demo.utils.DraftVariables
import com.yandex.divkit.demo.utils.ArrayListDbHelper
import com.yandex.divkit.demo.utils.PatchGenerator
import com.yandex.divkit.demo.utils.ScreenGenerator
import com.yandex.divkit.demo.utils.ScreenNavigationStack
import com.yandex.divkit.regression.RegressionActivity
import ir.nrdc.camera.Naji
import ir.nrdc.camera.com.github.dhaval2404.imagepicker.OnCallBackListener
import ir.nrdc.facecapture.FaceCapture
import ir.nrdc.facecapture.FaceCaptureConfig
import ir.nrdc.kashfface.ui.plate.PlateParser
import ir.nrdc.kashfface.ui.plate.PlateRead
import ir.nrdc.platecapture.PlateCapture
import ir.nrdc.platecapture.PlateCaptureConfig
import org.json.JSONArray
import org.json.JSONObject
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import saman.zamani.persiandate.PersianDateFormat.PersianDateNumberCharacter
import java.io.File
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.Calendar
import java.util.Locale
import java.util.Date
import java.util.UUID
import kotlin.math.log


private const val AUTHORITY_OPEN_SCREEN = "open_screen"
private const val AUTHORITY_SET_PATCH = "set_patch"
private const val AUTHORITY_BOTTOM_SHEET_SET_PATCH = "bottom_sheet_set_patch"
private const val AUTHORITY_SET_PATCH_FOR_LIST = "set_patch_for_list"
private const val AUTHORITY_SET_TIME_TO_DB = "set_time_to_db"
private const val AUTHORITY_LOAD_SCREEN = "load_screen"
private const val AUTHORITY_LOAD_UNCOMPROMISING_PREVIEW = "load_uncompromising_preview"
private const val AUTHORITY_SUBMIT_COMPROMISING_ACCIDENT = "submit_compromising_accident"
private const val AUTHORITY_POP_AND_LOAD_SCREEN = "pop_and_load_screen"
private const val AUTHORITY_GENERATE_SCREEN = "generateScreen"
private const val AUTHORITY_GENERATE_PATCH = "generatePatch"
private const val AUTHORITY_OPEN_CAMERA = "open_camera"
private const val AUTHORITY_OPEN_KASHFFACE_CAMERA = "open_kashfface_camera"
private const val AUTHORITY_OPEN_KASHFFACE_PLATE = "open_kashfface_plate"
private const val AUTHORITY_SELECT_GUESTHOUSE_START_DAY = "select_guesthouse_start_day"
private const val AUTHORITY_SHOW_MASSAGE = "show_toast"
private const val AUTHORITY_OPEN_KASHFFACE = "open_kashfface"
private const val AUTHORITY_SHOW_DIV_TOAST = "show_toast_div"
private const val AUTHORITY_SHOW_DIV_DIALOG = "show_dialog_div"
private const val AUTHORITY_SHOW_DIALOG = "show_dialog"
private const val AUTHORITY_SET_VARIABLE = "set_variable"
private const val AUTHORITY_SET_VARIABLE_FROM_DB = "set_variable_from_db"
private const val AUTHORITY_SET_VARIABLE_FROM_DRAFT = "set_variable_from_draft"
private const val AUTHORITY_INQUIRY_BIMEH = "inquiry_bimeh"
private const val AUTHORITY_SET_VARIABLE_FROM_DB_AND_SET_PATCH = "set_variable_from_db_and_set_patch"
private const val AUTHORITY_SET_IMEI = "set_imei"
private const val AUTHORITY_SET_IP = "set_ip"
private const val AUTHORITY_SET_DEVICE_MODEL = "set_device_model"
private const val AUTHORITY_SET_VARIABLE_TO_DB = "set_variable_to_db"
private const val AUTHORITY_SET_VARIABLE_TO_DB_PLUS_ONE = "set_variable_to_db_plus_one"
private const val AUTHORITY_CHANGE_COLOR = "change_color"
private const val AUTHORITY_SET_CAPTCHA = "refresh_captcha"
private const val AUTHORITY_CALL_SERVICE = "call_service"
private const val AUTHORITY_LOG_VT_REGISTER_VALIDATION = "log_vt_register_validation"
private const val AUTHORITY_UPLOAD_FILES_AND_CALL_SERVICE = "upload_files_and_call_service"
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
private const val AUTHORITY_GET_PERSIAN_TIME = "get_persian_time"
private const val AUTHORITY_GET_CURRENT_LOCATION = "get_current_location"
private const val AUTHORITY_SYNC_VEHICLES_COUNT = "sync_vehicles_count"
private const val AUTHORITY_IS_DATE_AND_TIME_LESS_EQUAL_TODAY_NOW = "is_date_and_time_less_equal_today_now"
private const val AUTHORITY_IS_BIGGER_DATE_AND_TIME = "is_bigger_date_and_time"
private const val AUTHORITY_BOTTOM_SHEET_DISMISS = "bottom_sheet_dismiss"
private const val AUTHORITY_CLOSE_BOTTOM_SHEET = "close_bottom_sheet"
private const val AUTHORITY_SET_VARIABLE_TO_BASE = "set_variable_to_base"
private const val AUTHORITY_SET_VARIABLE_TO_BOTTOM_SHEET = "set_variable_to_bottomsheet"
private const val AUTHORITY_ADD_ITEM_FROM_TEMPLATE = "add_item_from_template"
private const val AUTHORITY_SET_HOTEL_PRIORITY = "set_hotel_priority"
private const val AUTHORITY_CLEAR_HOTEL_PRIORITY = "clear_hotel_priority"
private const val AUTHORITY_SAVE_HOTEL_PRIORITIES = "save_hotel_priorities"
private const val AUTHORITY_CHECK_VERSION = "check_version"
private const val AUTHORITY_SET_OBJECT_TO_DB = "set_object_to_db"
private const val AUTHORITY_SAVE_VEHICLE_TO_LIST = "save_vehicle_to_list"
private const val AUTHORITY_SAVE_PASSENGER_TO_LIST = "save_passenger_to_list"
private const val AUTHORITY_SAVE_PEDESTRIAN_TO_LIST = "save_pedestrian_to_list"
private const val AUTHORITY_SAVE_ORGANIZATION_TO_LIST = "save_organization_to_list"
private const val AUTHORITY_SAVE_LIVESTOCK_TO_LIST = "save_livestock_to_list"
private const val AUTHORITY_SET_PAGE_TO_DB = "set_page_to_db"
private const val AUTHORITY_SET_PAGE_WITH_CHANGES_TO_DB = "set_page_with_changes_to_db"
private const val AUTHORITY_GET_CAMERA_PERMISSION = "get_camera_permission"
private const val AUTHORITY_GET_LOCATION_PERMISSION = "get_location_permission"
private const val AUTHORITY_GET_WRITE_PERMISSION = "get_write_permission"
private const val AUTHORITY_GET_ALL_PERMISSION = "get_all_permission"
private const val AUTHORITY_UPDATE = "update"
private const val AUTHORITY_RESET_PHID = "reset_phid"
private const val AUTHORITY_SET_RANDOM_ID_TO_DB = "set_random_id_to_db"
private const val AUTHORITY_START_RECORDING = "start_recording"
private const val AUTHORITY_STOP_RECORDING = "stop_recording"
private const val AUTHORITY_UPLOAD_RECORDING = "upload_recording"
private const val AUTHORITY_UPLOAD_AND_CALL_SERVICE = "upload_and_call_service"
private const val AUTHORITY_RESTART_PUSH_NOTIFICATIONS = "restart_push_notifications"
private const val AUTHORITY_APP_LOCK_CHANGE = "app_lock_change"
private const val AUTHORITY_APP_LOCK_SET_BIOMETRIC = "app_lock_set_biometric"
private const val AUTHORITY_ARRAY_TO_LIST_DB = "array_to_list_db"
private const val AUTHORITY_APPEND_TO_ARRAY = "append_to_array"
private const val AUTHORITY_REMOVE_FROM_ARRAY = "remove_from_array"
private const val AUTHORITY_SET_VARIABLE_FROM_ARRAY = "set_variable_from_array"
private const val AUTHORITY_RESTART_LOCATION_SERVICE = "restart_location_service"
private const val AUTHORITY_REFRESH_NOTIFICATION_TYPE_COUNTERS = "refresh_notification_type_counters"
private const val AUTHORITY_PUSH_TO_STACK = "push_to_stack"
private const val AUTHORITY_POP_FROM_STACK = "pop_from_stack"
private const val AUTHORITY_SET_PATCH_FROM_STACK = "set_patch_from_stack"
private const val AUTHORITY_PUSH_AND_SET_PATCH = "push_and_set_patch"
private const val AUTHORITY_POP_AND_SET_PATCH = "pop_and_set_patch"
const val SCHEME_DIV_ACTION = "div-action"

private const val ACTIVITY_DEMO = "demo"
private const val ACTIVITY_SAMPLES = "samples"
private const val ACTIVITY_REGRESSION = "regression"
private const val ACTIVITY_SETTINGS = "settings"
private const val ACTIVITY_MEHDI = "mehdi"

private const val PARAM_CONTAINERID = "containerId"
private const val PARAM_TEMPLATE_NAME = "templateName"
private const val PARAM_SCREEN_NAME = "screenName"
private const val PARAM_PATCH_NAME = "patchName"
private const val PARAM_DATA_JSON = "dataJson"
private const val PARAM_ACTIVITY = "activity"
private const val PARAM_SCREEN = "screen"
private const val PARAM_UPDATE_URL = "url"
private const val PARAM_TOAST = "massage"
private const val PARAM_DIALOG_TITLE = "title"
private const val PARAM_DIALOG_MASSAGE = "massage"
private const val PARAM_VARIABLE_NAME = "name"
private const val PARAM_VARIABLE_VALUE = "value"
private const val PARAM_TARGET_BOTTOM_SHEET = "target"
private const val PARAM_TEMPLATE_KEY = "template"
private const val PARAM_TEMPLATE_TYPE = "template_type"
private const val PARAM_CONTAINER_ID = "container_id"
private const val PARAM_POSITION = "position"
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
private const val PARAM_BOTTOM_SHEET_ARRAY = "array"
private const val PARAM_BOTTOM_SHEET_MATCH_KEY = "match_key"
private const val PARAM_BOTTOM_SHEET_MATCH_VALUE = "match_value"
private const val PARAM_BOTTOM_SHEET_VAR_PREFIX = "prefix"
private const val PARAM_TOAST_DIV = "jsonName"
private const val PARAM_DIALOG_DIV = "jsonName"
private const val PARAM_SET_PATCH = "patch"
private const val PARAM_APPLY_TO = "apply_to"
private const val PARAM_SET_PATCH_TEMPLATE = "template_name"
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
private const val PARAM_OPEN_KASHFFACE_FRONT = "front"
private const val PARAM_OPEN_KASHFFACE_PLATE_MODE = "mode"
private const val PARAM_CHECK_VERSION_NAME = "name"
private const val PARAM_SHOW_IMAGE = "image"
private const val PARAM_SET_PAGE_TO_DB = "key"
private const val PARAM_ARCHIVE_PATH = "archive_path"
private const val PARAM_STACK_KEY = "key"
private const val PARAM_STACK_VALUE = "value"


class UIDiv2ActionHandler(
    uriHandler: DivkitDemoUriHandler,
    private val context: Context,
    private val activity: Activity,
    private val lo: LifecycleOwner,
    private var loadScreenListener: LoadScreenListener,
    private val mehdiViewModel: MehdiViewModel?,
    private var btmSheet_div: BottomSheetDiv? = null,
    private var bottomSheetManager: BottomSheetManager? = null
) : /*DemoDivActionHandler(uriHandler),*/ DivActionHandler(), BottomSheetPlate.PlateItemListener,
    OnCallBackListener,
    Serializable,
    AdapterBottomSheetSpinner.CustomItemListener {
    
    // Method to update the BottomSheetManager reference
    fun setBottomSheetManager(bottomSheetManager: BottomSheetManager?) {
        try {
            this.bottomSheetManager = bottomSheetManager
            println("UIDiv2ActionHandler: BottomSheetManager updated - is null? ${bottomSheetManager == null}")
        } catch (e: Exception) {
            println("UIDiv2ActionHandler: Failed to set BottomSheetManager: ${e.message}")
            e.printStackTrace()
        }
    }
    lateinit var view: DivViewFacade
//    private lateinit var btmSheet_div: BottomSheetDiv

    private var flag = 0;
    private var btmSheet_list = BottomSheetDialogFragment();
    private var naji = Naji();
    /** After letter bottomsheet sets plateB, focus this id when sheet dismisses (works even if JSON lacks ?focus=). */
    private var pendingFocusAfterDismiss: String? = null
    // Use singleton to maintain state across action handler instances
    private val patchGenerator = DynamicPatchGeneratorSingleton.instance

    /**
     * Helper method used by NotificationManager to forward notification service requests
     * through the existing LoadScreenListener.onRequest(...) pipeline.
     */
    fun requestFromNotification(request: HashMap<String, String>) {
        try {
            android.util.Log.d("UIDiv2ActionHandler", "=== requestFromNotification called ===")
            android.util.Log.d("UIDiv2ActionHandler", "Request data: $request")
            android.util.Log.d("UIDiv2ActionHandler", "loadScreenListener is null: ${loadScreenListener == null}")

            if (loadScreenListener != null) {
                android.util.Log.d("UIDiv2ActionHandler", "Calling loadScreenListener.onRequest")
                loadScreenListener.onRequest(request)
                android.util.Log.d("UIDiv2ActionHandler", "loadScreenListener.onRequest completed")
            } else {
                android.util.Log.e("UIDiv2ActionHandler", "loadScreenListener is null, cannot forward request!")
            }

            android.util.Log.d("UIDiv2ActionHandler", "=== requestFromNotification completed ===")
        } catch (e: Exception) {
            android.util.Log.e("UIDiv2ActionHandler", "Error in requestFromNotification", e)
        }
    }

    /**
     * Process cached service response (for check_db_call_service mode)
     * This simulates the same behavior as a fresh service call
     */
    fun processServiceResponse(response: HashMap<String, String>) {
        try {
            Log.d("UIDiv2ActionHandler", "processServiceResponse called with: $response")
            // Forward to onRequest to process the response as if it came from service
            // The response should already be in the format that onRequest expects
            loadScreenListener.onRequest(response)
        } catch (e: Exception) {
            Log.e("UIDiv2ActionHandler", "Error in processServiceResponse", e)
        }
    }

    override fun handleAction(
        action: DivAction,
        view: DivViewFacade,
        resolver: ExpressionResolver
    ): Boolean {
        // First, try to handle typed actions (like set_state, set_variable, etc.)
        if (DivActionTypedHandlerProxy.handleAction(action, view, resolver)) {
            return true
        }
        
        // Debug: Log the action details
        com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "=== handleAction called ===")
        com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "Action URL: ${action.url}")
        com.yandex.div.internal.Log.d(
            "UIDiv2ActionHandler",
            "Action URL type: ${action.url?.javaClass?.simpleName}"
        )
        
        // Check if this might be a recording action
        val urlString = action.url?.toString() ?: ""
        if (urlString.contains("start_recording") || urlString.contains("stop_recording")) {
            com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "*** POTENTIAL RECORDING ACTION DETECTED ***")
            com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "URL string: $urlString")
        }

        // Try to evaluate the URL
        val url = try {
            action.url?.evaluate(resolver)
        } catch (e: Exception) {
            com.yandex.div.internal.Log.e(
                "UIDiv2ActionHandler",
                "Error evaluating URL: ${e.message}",
                e
            )
            null
        }

        com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "Evaluated URL: $url")
        com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "URL is null: ${url == null}")
        
        // Check if evaluated URL contains recording actions
        if (url != null && (url.toString().contains("start_recording") || url.toString().contains("stop_recording"))) {
            com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "*** EVALUATED URL CONTAINS RECORDING ACTION ***")
            com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "Evaluated URL string: ${url.toString()}")
        }

        if (url == null) {
            com.yandex.div.internal.Log.w(
                "UIDiv2ActionHandler",
                "URL evaluation returned null, cannot proceed"
            )
            return super.handleAction(action, view, resolver)
        }

        if (action.url == null) return false

        val actionLogId = runCatching { action.logId.evaluate(resolver) }.getOrNull().orEmpty()

        // First try super (handles built-in actions like set_state), then custom handlers
        return (super.handleAction(action, view, resolver)
                || handleActivityActionUrl(url, view, actionLogId)
                || SettingsActionHandler.handleActionUrl(url))
    }

    override fun handleAction(
        action: DivSightAction,
        view: DivViewFacade,
        resolver: ExpressionResolver,
    ): Boolean {
        if (action.url == null) return false
        val uri = action.url!!.evaluate(resolver)
        val actionLogId = runCatching { action.logId.evaluate(resolver) }.getOrNull().orEmpty()
//        if (uri.authority != AUTHORITY_DOWNLOAD)
        return (handleActivityActionUrl(uri, view, actionLogId)
                || SettingsActionHandler.handleActionUrl(uri)
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
    private fun handleActivityActionUrl(
        uri: Uri,
        view: DivViewFacade,
        actionLogId: String = "",
    ): Boolean {
        this.view = view

        // Debug: Log the URI details
        com.yandex.div.internal.Log.d(
            "UIDiv2ActionHandler",
            "=== handleActivityActionUrl called ==="
        )
        com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "URI: $uri")
        com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "URI scheme: ${uri.scheme}")
        com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "URI authority: ${uri.authority}")
        com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "URI query: ${uri.query}")
        com.yandex.div.internal.Log.d(
            "UIDiv2ActionHandler",
            "URI is empty: ${uri.toString().isEmpty()}"
        )
        
        // Check if this is a recording action
        if (uri.authority == AUTHORITY_START_RECORDING || uri.authority == AUTHORITY_STOP_RECORDING) {
            com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "*** RECORDING ACTION DETECTED ***")
            com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "Action type: ${uri.authority}")
        }

        if (uri.scheme != SCHEME_DIV_ACTION) return false
        if (actionLogId == "open_smart_assistant" || uri.authority == AUTHORITY_OPEN_KASHFFACE) {
            openKashfFace()
            return true
        }
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
            val screen = uri.getQueryParameter(PARAM_SCREEN) ?: return false
            val resolved = resolveScreenName(screen)
            if (resolved.isEmpty()) return false
            val pushResult = ScreenNavigationStack.push(mehdiViewModel, resolved)
            if (pushResult.shouldNavigate) {
                loadScreenListener.onLoadFinishingCurrent(pushResult.targetScreen)
            }
        } else if (uri.authority == AUTHORITY_LOAD_UNCOMPROMISING_PREVIEW) {
            val div2View = view as? Div2View
            if (div2View == null) {
                android.util.Log.w("UIDiv2ActionHandler", "load_uncompromising_preview: Div2View is null")
                return false
            }
            return try {
                val values = UncompromisingPreviewLoader.load(context, mehdiViewModel)
                android.util.Log.d("UIDiv2ActionHandler", "load_uncompromising_preview: setting ${values.size} variables")
                values.forEach { (name, value) ->
                    try {
                        div2View.setVariable(name, value)
                        if (name.contains("sketch")) {
                            android.util.Log.d("UIDiv2ActionHandler", "load_uncompromising_preview: set $name len=${value.length}")
                        }
                    } catch (e: VariableMutationException) {
                        android.util.Log.w(
                            "UIDiv2ActionHandler",
                            "load_uncompromising_preview: cannot set $name (len=${value.length}): ${e.message}",
                        )
                    }
                }
                true
            } catch (e: Exception) {
                android.util.Log.e("UIDiv2ActionHandler", "load_uncompromising_preview failed", e)
                false
            }
        } else if (uri.authority == AUTHORITY_SUBMIT_COMPROMISING_ACCIDENT) {
            return try {
                val div2View = view as? Div2View
                val liveVars = div2View?.let {
                    Div2ViewVariableExporter.captureVariablesMap(it)
                }.orEmpty().mapValues { it.value?.toString().orEmpty() }
                val requestJson = com.yandex.divkit.demo.utils.CompromisingAccidentJsonBuilder.build(
                    mehdiViewModel,
                    liveVars,
                    context,
                )
                android.util.Log.d("UIDiv2ActionHandler", "submit_compromising_accident: requestJson length=${requestJson.length}")
                val map = HashMap<String, String>()
                map["path"] = "primaryRegisterAccidents"
                map["sysName"] = "accidents"
                map["screenName"] = "accidents"
                map["ph/token"] = "empty"
                map["requestJson"] = requestJson
                loadScreenListener.onRequest(map)
                true
            } catch (e: Exception) {
                android.util.Log.e("UIDiv2ActionHandler", "submit_compromising_accident failed", e)
                false
            }
        } else if (uri.authority == AUTHORITY_POP_AND_LOAD_SCREEN) {
            val popResult = ScreenNavigationStack.pop(mehdiViewModel)
            val previousScreen = popResult.previousScreen
            when {
                previousScreen != null && !ScreenNavigationStack.isAuthScreen(previousScreen) -> {
                    loadScreenListener.onLoadFinishingCurrent(previousScreen)
                }
                else -> {
                    // Never finish the activity from nested screens — land on main.
                    ScreenNavigationStack.resetToMain(mehdiViewModel)
                    loadScreenListener.onLoadFinishingCurrent(ScreenNavigationStack.MAIN_SCREEN)
                }
            }
        } else if (uri.authority == AUTHORITY_GENERATE_SCREEN) {
            val containerId = uri.getQueryParameter(PARAM_CONTAINERID)
            val templateName = uri.getQueryParameter(PARAM_TEMPLATE_NAME)
            val screenName = uri.getQueryParameter(PARAM_PATCH_NAME)

            Log.d("UIDiv2ActionHandler", "Generate screen parameters:")
            Log.d("UIDiv2ActionHandler", "containerId: $containerId")
            Log.d("UIDiv2ActionHandler", "templateName: $templateName")
            Log.d("UIDiv2ActionHandler", "screenName: $screenName")

//            val dataJsonName = uri.getQueryParameter(PARAM_DATA_JSON)
//            val dataJson = mehdiViewModel?.getValueByKey(dataJsonName.toString())?.value
            val dataJson = "[\n" +
                    "  {\n" +
                    "    \"name\": \"Alice\",\n" +
                    "    \"family\": \"Johnson\",\n" +
                    "    \"city\": \"New York\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Mohammed\",\n" +
                    "    \"family\": \"Khan\",\n" +
                    "    \"city\": \"London\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Sophie\",\n" +
                    "    \"family\": \"Lemoine\",\n" +
                    "    \"city\": \"Paris\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Kenji\",\n" +
                    "    \"family\": \"Takahashi\",\n" +
                    "    \"city\": \"Tokyo\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Carlos\",\n" +
                    "    \"family\": \"Martínez\",\n" +
                    "    \"city\": \"Madrid\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Fatima\",\n" +
                    "    \"family\": \"Ali\",\n" +
                    "    \"city\": \"Cairo\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Liam\",\n" +
                    "    \"family\": \"O'Reilly\",\n" +
                    "    \"city\": \"Dublin\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Chen\",\n" +
                    "    \"family\": \"Wang\",\n" +
                    "    \"city\": \"Beijing\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Isabella\",\n" +
                    "    \"family\": \"Rossi\",\n" +
                    "    \"city\": \"Rome\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Ava\",\n" +
                    "    \"family\": \"Smith\",\n" +
                    "    \"city\": \"Toronto\"\n" +
                    "  }\n" +
                    "]\n"

            // Check if all required parameters are present
            if (containerId != null && templateName != null && screenName != null && dataJson != null) {
                val screenGenerator = ScreenGenerator(context, mehdiViewModel)

                // Test template replacement logic
                screenGenerator.testTemplateReplacement()

                // Test database fetch
                screenGenerator.testDatabaseFetch(screenName)

                // Save test.json to database for testing
                screenGenerator.saveTestJsonToDatabase()

                // Run comprehensive test
                screenGenerator.testFullScreenGeneration()

                val generatedScreen = screenGenerator.generateScreen(
                    containerId = containerId,
                    templateName = templateName,
                    screenName = screenName,
                    dataJson = dataJson
                )

                // Log the result
                if (generatedScreen.isNotEmpty()) {
                    Log.d("UIDiv2ActionHandler", "Screen generated successfully: $screenName")
                    Log.d("UIDiv2ActionHandler", "generatedScreen: $generatedScreen")
                    uri.getQueryParameter(PARAM_SCREEN_NAME)?.let {
                        loadScreenListener.onLoad(it)
                    }
                } else {
                    Log.e("UIDiv2ActionHandler", "Failed to generate screen: $screenName")
                }
            } else {
                Log.e("UIDiv2ActionHandler", "Missing required parameters for screen generation")
            }
        } else if (uri.authority == AUTHORITY_GENERATE_PATCH) {
            Log.d("UIDiv2ActionHandler", "=== PATCH GENERATION REQUEST ===")
            Log.d("UIDiv2ActionHandler", "URI: $uri")
            Log.d("UIDiv2ActionHandler", "URI authority: ${uri.authority}")
            Log.d("UIDiv2ActionHandler", "URI scheme: ${uri.scheme}")
            Log.d("UIDiv2ActionHandler", "URI host: ${uri.host}")
            Log.d("UIDiv2ActionHandler", "URI path: ${uri.path}")
            Log.d("UIDiv2ActionHandler", "URI query: ${uri.query}")

            val containerId = uri.getQueryParameter("containerId")
            val templateName = uri.getQueryParameter("templateName")
            val patchName = uri.getQueryParameter("patchName")

            Log.d("UIDiv2ActionHandler", "Extracted parameters:")
            Log.d("UIDiv2ActionHandler", "  containerId: $containerId")
            Log.d("UIDiv2ActionHandler", "  templateName: $templateName")
            Log.d("UIDiv2ActionHandler", "  patchName: $patchName")

            val dataJson = "[\n" +
                    "  {\n" +
                    "    \"name\": \"Alice\",\n" +
                    "    \"family\": \"Johnson\",\n" +
                    "    \"city\": \"New York\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Mohammed\",\n" +
                    "    \"family\": \"Khan\",\n" +
                    "    \"city\": \"London\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Sophie\",\n" +
                    "    \"family\": \"Lemoine\",\n" +
                    "    \"city\": \"Paris\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Kenji\",\n" +
                    "    \"family\": \"Takahashi\",\n" +
                    "    \"city\": \"Tokyo\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Carlos\",\n" +
                    "    \"family\": \"Martínez\",\n" +
                    "    \"city\": \"Madrid\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Fatima\",\n" +
                    "    \"family\": \"Ali\",\n" +
                    "    \"city\": \"Cairo\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Liam\",\n" +
                    "    \"family\": \"O'Reilly\",\n" +
                    "    \"city\": \"Dublin\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Chen\",\n" +
                    "    \"family\": \"Wang\",\n" +
                    "    \"city\": \"Beijing\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Isabella\",\n" +
                    "    \"family\": \"Rossi\",\n" +
                    "    \"city\": \"Rome\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Ava\",\n" +
                    "    \"family\": \"Smith\",\n" +
                    "    \"city\": \"Toronto\"\n" +
                    "  }\n" +
                    "]\n"

            Log.d("UIDiv2ActionHandler", "Using test data JSON: $dataJson")

            // Check if all required parameters are present
            if (containerId != null && templateName != null && patchName != null && dataJson != null) {
                Log.d(
                    "UIDiv2ActionHandler",
                    "All required parameters present, creating PatchGenerator"
                )
                Log.d("UIDiv2ActionHandler", "mehdiViewModel: $mehdiViewModel")

                val patchGenerator = PatchGenerator(mehdiViewModel)
                Log.d("UIDiv2ActionHandler", "PatchGenerator created successfully")

                // Test patch generation
                Log.d("UIDiv2ActionHandler", "Running test patch generation...")
                patchGenerator.testPatchGeneration()

                Log.d("UIDiv2ActionHandler", "Generating actual patch...")
                val success = patchGenerator.generatePatch(
                    containerId = containerId,
                    templateName = templateName,
                    patchName = patchName,
                    dataJson = dataJson
                )

                // Log the result
                if (success) {
                    Log.d("UIDiv2ActionHandler", "=== PATCH GENERATION SUCCESS ===")
                    Log.d("UIDiv2ActionHandler", "Patch generated successfully: $patchName")

                    // Fetch the generated patch from database
                    Log.d("UIDiv2ActionHandler", "Fetching patch from database...")
                    val patchJson = patchGenerator.fetchPatchFromDatabase(patchName)

                    if (patchJson != null) {
                        Log.d("UIDiv2ActionHandler", "Patch fetched successfully: $patchJson")
                        Log.d("UIDiv2ActionHandler", "Applying patch to view...")
                        Log.d("UIDiv2ActionHandler", "loadScreenListener: $loadScreenListener")

                        // Apply the patch
                        try {
                            Log.d("UIDiv2ActionHandler", "About to call onApplyOnbase with:")
                            Log.d("UIDiv2ActionHandler", "  patchJson: $patchJson")
                            Log.d("UIDiv2ActionHandler", "  patchName: $patchName")
                            Log.d(
                                "UIDiv2ActionHandler",
                                "  loadScreenListener: $loadScreenListener"
                            )

                            loadScreenListener.onApplyOnbase(patchJson, patchName, "تست", "تست")
                            Log.d(
                                "UIDiv2ActionHandler",
                                "Patch applied successfully via onApplyOnbase"
                            )
                        } catch (e: Exception) {
                            Log.e(
                                "UIDiv2ActionHandler",
                                "Error applying patch via onApplyOnbase",
                                e
                            )
                            Log.e("UIDiv2ActionHandler", "Exception details: ${e.message}")
                            e.printStackTrace()
                            try {
                                // Try alternative method if available
                                Log.d(
                                    "UIDiv2ActionHandler",
                                    "Trying alternative patch application method..."
                                )
                                // You might need to implement this method in your LoadScreenListener
                                Log.d("UIDiv2ActionHandler", "Alternative method not available")
                            } catch (e2: Exception) {
                                Log.e(
                                    "UIDiv2ActionHandler",
                                    "Error with alternative patch application",
                                    e2
                                )
                                Log.e(
                                    "UIDiv2ActionHandler",
                                    "Second exception details: ${e2.message}"
                                )
                                e2.printStackTrace()
                            }
                        }

                        Log.d("UIDiv2ActionHandler", "Patch application process completed")
                    } else {
                        Log.e(
                            "UIDiv2ActionHandler",
                            "Failed to fetch patch from database: $patchName"
                        )
                        Log.e(
                            "UIDiv2ActionHandler",
                            "This might indicate a database issue or the patch wasn't saved properly"
                        )
                    }
                } else {
                    Log.e("UIDiv2ActionHandler", "=== PATCH GENERATION FAILED ===")
                    Log.e("UIDiv2ActionHandler", "Failed to generate patch: $patchName")
                    Log.e("UIDiv2ActionHandler", "Check the PatchGenerator logs for more details")
                }
            } else {
                Log.e("UIDiv2ActionHandler", "=== MISSING PARAMETERS ===")
                Log.e("UIDiv2ActionHandler", "Missing required parameters for patch generation")
                Log.e("UIDiv2ActionHandler", "containerId: $containerId")
                Log.e("UIDiv2ActionHandler", "templateName: $templateName")
                Log.e("UIDiv2ActionHandler", "patchName: $patchName")
                Log.e(
                    "UIDiv2ActionHandler",
                    "dataJson: ${if (dataJson != null) "present" else "null"}"
                )
                Log.e("UIDiv2ActionHandler", "All parameters must be present in the URI query")
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

        } else if (uri.authority == AUTHORITY_START_RECORDING) {
            android.util.Log.d("UIDiv2ActionHandler", "=== START RECORDING ACTION ===")
            val recordingId = uri.getQueryParameter("id")
            android.util.Log.d("UIDiv2ActionHandler", "Recording ID: $recordingId")
            
            // If no recording ID provided, try to get it from database using a default key
            val finalRecordingId = if (recordingId.isNullOrEmpty()) {
                android.util.Log.d("UIDiv2ActionHandler", "No recording ID provided, trying to get from database")
                // Try to get recording ID from database using common keys
                val questionerId = mehdiViewModel?.getValueByKey("questioner_id")?.value
                if (!questionerId.isNullOrEmpty()) {
                    android.util.Log.d("UIDiv2ActionHandler", "Found questioner_id in database: $questionerId")
                    questionerId
                } else {
                    android.util.Log.w("UIDiv2ActionHandler", "No recording ID found in database")
                    null
                }
            } else {
                recordingId
            }
            
            android.util.Log.d("UIDiv2ActionHandler", "Final recording ID: $finalRecordingId")
            android.util.Log.d("UIDiv2ActionHandler", "Calling loadScreenListener.startRecording() with ID: $finalRecordingId")
            loadScreenListener.startRecording(finalRecordingId)
            android.util.Log.d("UIDiv2ActionHandler", "=== END START RECORDING ACTION ===")

        } else if (uri.authority == AUTHORITY_STOP_RECORDING) {
            android.util.Log.d("UIDiv2ActionHandler", "=== STOP RECORDING ACTION ===")
            android.util.Log.d("UIDiv2ActionHandler", "Calling loadScreenListener.stopRecording()")
            loadScreenListener.stopRecording()
            android.util.Log.d("UIDiv2ActionHandler", "=== END STOP RECORDING ACTION ===")

        } else if (uri.authority == AUTHORITY_UPLOAD_RECORDING) {
            android.util.Log.d("UIDiv2ActionHandler", "=== UPLOAD RECORDING ACTION ===")
            val recordingId = uri.getQueryParameter("id")
            android.util.Log.d("UIDiv2ActionHandler", "Recording ID: $recordingId")
            android.util.Log.d("UIDiv2ActionHandler", "Calling loadScreenListener.uploadRecording() with ID: $recordingId")
            loadScreenListener.uploadRecording(recordingId)
            android.util.Log.d("UIDiv2ActionHandler", "=== END UPLOAD RECORDING ACTION ===")

        } else if (uri.authority == AUTHORITY_UPLOAD_AND_CALL_SERVICE) {
            android.util.Log.d("UIDiv2ActionHandler", "=== UPLOAD AND CALL SERVICE ACTION ===")
            android.util.Log.d("UIDiv2ActionHandler", "URI: $uri")
            // Note: stopRecording() will be called inside uploadAndCallService with proper delay

            val recordingId = uri.getQueryParameter("id")
            android.util.Log.d("UIDiv2ActionHandler", "Recording ID: $recordingId")
            
            // Process all parameters like call_service does
            val parameterNames = uri.queryParameterNames
            android.util.Log.d("UIDiv2ActionHandler", "Parameter names: $parameterNames")
            android.util.Log.d("UIDiv2ActionHandler", "Parameter count: ${parameterNames.size}")
            
            val serviceParams: java.util.HashMap<String, String> = HashMap()
            val div2View = if (view is Div2View) view as Div2View? else null
            val expressionResolver = div2View?.expressionResolver
            
            android.util.Log.d("UIDiv2ActionHandler", "Div2View available: ${div2View != null}")
            android.util.Log.d("UIDiv2ActionHandler", "ExpressionResolver available: ${expressionResolver != null}")
            
            for (parameterName in parameterNames) {
                var parameterValue = uri.getQueryParameter(parameterName).toString()
                android.util.Log.d("UIDiv2ActionHandler", "Processing parameter: $parameterName = $parameterValue")
                
                // Process nested variable expressions if expressionResolver is available
                if (expressionResolver != null && parameterValue.contains("@{")) {
                    android.util.Log.d("UIDiv2ActionHandler", "Processing nested variables for: $parameterValue")
                    parameterValue = processNestedVariableExpressions(parameterValue, expressionResolver)
                    android.util.Log.d("UIDiv2ActionHandler", "After processing: $parameterValue")
                }
                
                serviceParams[parameterName] = parameterValue
            }
            
            android.util.Log.d("UIDiv2ActionHandler", "Final service params: $serviceParams")
            android.util.Log.d("UIDiv2ActionHandler", "Calling loadScreenListener.uploadAndCallService() with ID: $recordingId and params: $serviceParams")
            loadScreenListener.uploadAndCallService(recordingId, serviceParams)
            android.util.Log.d("UIDiv2ActionHandler", "=== END UPLOAD AND CALL SERVICE ACTION ===")

        } else if (uri.authority == AUTHORITY_RESTART_PUSH_NOTIFICATIONS) {
                    android.util.Log.i("NajiPush", "restart_push_notifications action")
            try {
                val app = com.yandex.divkit.demo.DivkitApplication.getInstance()
                if (app != null) {
                    android.util.Log.d("UIDiv2ActionHandler", "Calling restartPushNotifications()...")
                    // Use dedicated restart method that properly disconnects and reinitializes
                    app.restartPushNotifications()
                    android.util.Log.d("UIDiv2ActionHandler", "Push notifications restart initiated")

                    // Complete push setup after restart to ensure connection is established
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        android.util.Log.d("UIDiv2ActionHandler", "Completing push setup after restart...")
                        app.completePushSetup(activity)
                        android.util.Log.d("UIDiv2ActionHandler", "Push setup completed after restart")
                    }, 1500)

                    Toast.makeText(context, "سرویس اعلان‌ها راه‌اندازی مجدد شد", Toast.LENGTH_SHORT).show()
                } else {
                    android.util.Log.e("UIDiv2ActionHandler", "DivkitApplication instance is null")
                    Toast.makeText(context, "خطا در راه‌اندازی مجدد سرویس اعلان‌ها", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("UIDiv2ActionHandler", "Error restarting push notifications: ${e.message}", e)
                Toast.makeText(context, "خطا در راه‌اندازی مجدد سرویس اعلان‌ها: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            android.util.Log.d("UIDiv2ActionHandler", "=== END RESTART PUSH NOTIFICATIONS ACTION ===")

        } else if (uri.authority == AUTHORITY_ARRAY_TO_LIST_DB) {
            handleArrayToListDb(uri, view)
        } else if (uri.authority == AUTHORITY_APPEND_TO_ARRAY) {
            handleAppendToArray(uri, view)
            return true
        } else if (uri.authority == AUTHORITY_REMOVE_FROM_ARRAY) {
            handleRemoveFromArray(uri, view)
            return true
        } else if (uri.authority == AUTHORITY_SET_VARIABLE_FROM_ARRAY) {
            handleSetVariableFromArray(uri, view)
        } else if (uri.authority == AUTHORITY_APP_LOCK_CHANGE) {
            val intent = com.yandex.divkit.demo.applock.AppLockActivity.changeIntent(context)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else if (uri.authority == AUTHORITY_APP_LOCK_SET_BIOMETRIC) {
            val store = com.yandex.divkit.demo.applock.AppLockStore(context)
            if (!store.isConfigured) {
                Toast.makeText(context, "ابتدا رمز ورود را تنظیم کنید", Toast.LENGTH_SHORT).show()
            } else if (!com.yandex.divkit.demo.applock.BiometricHelper.canAuthenticate(context)) {
                Toast.makeText(context, "این دستگاه از بیومتریک پشتیبانی نمی‌کند", Toast.LENGTH_SHORT).show()
            } else {
                val enabledParam = uri.getQueryParameter("enabled")
                val enabled = when {
                    enabledParam.equals("true", true) -> true
                    enabledParam.equals("false", true) -> false
                    else -> !store.biometricEnabled
                }
                if (enabled) {
                    val host = (activity as? FragmentActivity) ?: (context as? FragmentActivity)
                    if (host == null) {
                        store.biometricEnabled = true
                        Toast.makeText(context, "ورود بیومتریک فعال شد", Toast.LENGTH_SHORT).show()
                    } else {
                        com.yandex.divkit.demo.applock.BiometricHelper.showPrompt(
                            activity = host,
                            title = "فعال‌سازی بیومتریک",
                            subtitle = "برای تأیید، اثرانگشت یا چهره را اسکن کنید",
                            onSuccess = {
                                store.biometricEnabled = true
                                Toast.makeText(context, "ورود بیومتریک فعال شد", Toast.LENGTH_SHORT).show()
                            },
                            onError = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                } else {
                    store.biometricEnabled = false
                    Toast.makeText(context, "ورود بیومتریک غیرفعال شد", Toast.LENGTH_SHORT).show()
                }
            }

        } else if (uri.authority == AUTHORITY_RESTART_LOCATION_SERVICE) {
            android.util.Log.d("UIDiv2ActionHandler", "=== RESTART LOCATION SERVICE ACTION ===")
            try {
                val app = com.yandex.divkit.demo.DivkitApplication.getInstance()
                if (app != null) {
                    android.util.Log.d("UIDiv2ActionHandler", "Stopping location service...")
                    app.stopLocationService()
                    // Wait a bit before restarting to ensure clean restart
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        android.util.Log.d("UIDiv2ActionHandler", "Starting location service...")
                        app.startLocationService()
                        android.util.Log.d("UIDiv2ActionHandler", "Location service restarted successfully")
                        Toast.makeText(context, "سرویس موقعیت‌یابی راه‌اندازی مجدد شد", Toast.LENGTH_SHORT).show()
                    }, 500) // 500ms delay
                } else {
                    android.util.Log.e("UIDiv2ActionHandler", "DivkitApplication instance is null")
                    Toast.makeText(context, "خطا در راه‌اندازی مجدد سرویس موقعیت‌یابی", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("UIDiv2ActionHandler", "Error restarting location service: ${e.message}", e)
                Toast.makeText(context, "خطا در راه‌اندازی مجدد سرویس موقعیت‌یابی: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            android.util.Log.d("UIDiv2ActionHandler", "=== END RESTART LOCATION SERVICE ACTION ===")

        } else if (uri.authority == AUTHORITY_REFRESH_NOTIFICATION_TYPE_COUNTERS) {
            android.util.Log.d("UIDiv2ActionHandler", "=== REFRESH NOTIFICATION TYPE COUNTERS ACTION ===")
            try {
                com.yandex.divkit.demo.div.notificationList.NotificationManager.initializeTypeCounters()
                android.util.Log.d("UIDiv2ActionHandler", "Notification type counters refreshed successfully")
                Toast.makeText(context, "شمارنده‌های نوتیفیکیشن به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("UIDiv2ActionHandler", "Error refreshing notification type counters: ${e.message}", e)
                Toast.makeText(context, "خطا در به‌روزرسانی شمارنده‌های نوتیفیکیشن: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            android.util.Log.d("UIDiv2ActionHandler", "=== END REFRESH NOTIFICATION TYPE COUNTERS ACTION ===")
            return true

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
            return true

        } else if (uri.authority == AUTHORITY_GET_PERSIAN_TIME) {
            val varName = uri.getQueryParameter(PARAM_TIME_PICKER)
            val now = PersianDate()
            val time = String.format("%02d:%02d", now.hour, now.minute)
            val div2View = if (view is Div2View) view as Div2View? else null
            if (div2View != null && varName != null) {
                div2View.setVariable(varName, time)
            }
            return true

        } else if (uri.authority == AUTHORITY_GET_CURRENT_LOCATION) {
            val latVar = uri.getQueryParameter("lat_var")
            val longVar = uri.getQueryParameter("long_var")
            val div2View = if (view is Div2View) view as Div2View? else null
            if (div2View == null || latVar.isNullOrBlank() || longVar.isNullOrBlank()) {
                return false
            }
            resolveCurrentLocation()?.let { (latitude, longitude) ->
                div2View.setVariable(latVar, formatCoordinate(latitude))
                div2View.setVariable(longVar, formatCoordinate(longitude))
            }
            return true

        } else if (uri.authority == AUTHORITY_SYNC_VEHICLES_COUNT) {
            val div2View = if (view is Div2View) view as Div2View? else null
            val preferredAccidentId = uri.getQueryParameter("accident_id")?.takeIf { it.isNotBlank() }
            VehicleEntryCountHelper.sync(mehdiViewModel, div2View, preferredAccidentId)
            return true

        } else if (uri.authority == AUTHORITY_IS_DATE_AND_TIME_LESS_EQUAL_TODAY_NOW) {
            val div2View = view as? Div2View ?: return false
            // Support both current_time and legacy `val` param names.
            // Prefer date_var+time_var to avoid race with just-set *_date_and_time.
            val currentTime = uri.getTrimmedQueryParameter("current_time")
                ?: uri.getTrimmedQueryParameter("val")
            android.util.Log.d(
                "DateTimeCompare",
                "less_equal_today uri=$uri " +
                    "params=${uri.queryParameterNames.associateWith { uri.getQueryParameter(it) }}"
            )
            return DateTimeCompareActionHelper.handleIsDateTimeLessEqualTodayNow(
                div2View,
                div2View.expressionResolver,
                currentTime,
                uri.getTrimmedQueryParameter("result_var"),
                uri.getTrimmedQueryParameter("date_var"),
                uri.getTrimmedQueryParameter("time_var"),
            )

        } else if (uri.authority == AUTHORITY_IS_BIGGER_DATE_AND_TIME) {
            val div2View = view as? Div2View ?: return false
            android.util.Log.d(
                "DateTimeCompare",
                "action uri=$uri " +
                    "params=${uri.queryParameterNames.associateWith { uri.getQueryParameter(it) }}"
            )
            return DateTimeCompareActionHelper.handleIsBiggerDateTime(
                div2View,
                div2View.expressionResolver,
                uri.getTrimmedQueryParameter("val1"),
                uri.getTrimmedQueryParameter("val2"),
                uri.getTrimmedQueryParameter("result_var"),
                uri.getTrimmedQueryParameter("val1_date_var")
                    ?: uri.getTrimmedQueryParameter("date_var1"),
                uri.getTrimmedQueryParameter("val1_time_var")
                    ?: uri.getTrimmedQueryParameter("time_var1"),
                uri.getTrimmedQueryParameter("val2_date_var")
                    ?: uri.getTrimmedQueryParameter("date_var2"),
                uri.getTrimmedQueryParameter("val2_time_var")
                    ?: uri.getTrimmedQueryParameter("time_var2"),
            )

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

        } else if (uri.authority == AUTHORITY_OPEN_KASHFFACE_CAMERA) {
            openKashfFaceCamera(
                type = uri.getQueryParameter(PARAM_OPEN_CAMERA) ?: "person",
                checkOcr = uri.getQueryParameter(PARAM_OPEN_CAMERA_OCR),
                iranian = uri.getQueryParameter(PARAM_OPEN_CAMERA_iranian),
                frontCamera = uri.getQueryParameter(PARAM_OPEN_KASHFFACE_FRONT)
                    ?: uri.getQueryParameter("front_camera")
            )
            return true

        } else if (uri.authority == AUTHORITY_OPEN_KASHFFACE_PLATE) {
            openKashfFacePlate(
                defaultMode = uri.getQueryParameter(PARAM_OPEN_KASHFFACE_PLATE_MODE)
            )
            return true

        } else if (uri.authority == AUTHORITY_SELECT_GUESTHOUSE_START_DAY) {
            handleSelectGuesthouseStartDay(uri)
            return true

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

            val archivePath = uri.getQueryParameter(PARAM_ARCHIVE_PATH) ?: sysName
            val accidentId = map["accident_id"]?.takeIf { it.isNotBlank() }

            val existingArchive = if (!accidentId.isNullOrBlank() && archivePath == "Accidents") {
                runCatching {
                    mehdiViewModel?.getValueByKey("$username/$archivePath/$accidentId")?.value
                }.getOrNull()
            } else {
                null
            }

            map["offline_register_date"] = if (existingArchive != null) {
                runCatching {
                    JSONObject(existingArchive).optString("offline_register_date")
                }.getOrNull()?.takeIf { it.isNotBlank() } ?: currentDate
            } else {
                currentDate
            }

            val archiveKey = if (!accidentId.isNullOrBlank() && archivePath == "Accidents") {
                "$username/$archivePath/$accidentId"
            } else {
                "$username/$archivePath/$currentDate"
            }
            map["archive_key"] = archiveKey

            val json = Gson().toJson(map).toString()

            if (!accidentId.isNullOrBlank() && archivePath == "Accidents") {
                mehdiViewModel?.upsertAccidentArchiveEntry(
                    username = username.orEmpty(),
                    archivePath = archivePath,
                    accidentId = accidentId,
                    archiveKey = archiveKey,
                    json = json,
                )
            } else {
                mehdiViewModel?.insertItemToDbSync(PhPlusDB(null, archiveKey, json))
            }

        } else if (uri.authority == AUTHORITY_SAVE_VEHICLE_TO_LIST) {
            saveVehicleToOfflineList(uri)
            return true
        } else if (uri.authority == AUTHORITY_SAVE_PASSENGER_TO_LIST) {
            savePassengerToOfflineList(uri)
            return true
        } else if (uri.authority == AUTHORITY_SAVE_PEDESTRIAN_TO_LIST) {
            savePedestrianToOfflineList(uri)
            return true
        } else if (uri.authority == AUTHORITY_SAVE_ORGANIZATION_TO_LIST) {
            saveOrganizationToOfflineList(uri)
            return true
        } else if (uri.authority == AUTHORITY_SAVE_LIVESTOCK_TO_LIST) {
            saveLivestockToOfflineList(uri)
            return true
        } else if (uri.authority == AUTHORITY_SET_PAGE_TO_DB) {
            val key = uri.getQueryParameter(PARAM_SET_PAGE_TO_DB)
//            val div2View = if (view is Div2View) view as Div2View? else null
//            val divData = div2View?.divData
//            Log.d("page_json",divData?.writeToJSON().toString())
            if (key != null) {
//                mehdiViewModel?.insertItemToDb(PhPlusDB(null, key, divData?.writeToJSON().toString()))

                loadScreenListener.setPageToDB(key)
            }


        } else if (uri.authority == AUTHORITY_SET_PAGE_WITH_CHANGES_TO_DB) {
            Log.d(
                "UIDiv2ActionHandler",
                "=== AUTHORITY_SET_PAGE_WITH_CHANGES_TO_DB action triggered ==="
            )

            val key = uri.getQueryParameter(PARAM_SET_PAGE_TO_DB)
            Log.d("UIDiv2ActionHandler", "Key parameter: $key")

            val mapper: ObjectMapper = jacksonObjectMapper()

            val dbPageValue = key?.let { mehdiViewModel?.getValueByKey(it)?.value }
            Log.d("UIDiv2ActionHandler", "DB page value exists: ${dbPageValue != null}")

            val div2View = if (view is Div2View) view as Div2View? else null
            Log.d("UIDiv2ActionHandler", "Div2View exists: ${div2View != null}")

            val currentPageJson = div2View?.divData?.writeToJSON()?.toString() ?: return false
            val currentPage = mapper.readTree(currentPageJson)
            val dbPage = if (dbPageValue.isNullOrBlank()) {
                Log.d("UIDiv2ActionHandler", "No base page in DB — using current page as draft base")
                currentPage.deepCopy()
            } else {
                mapper.readTree(dbPageValue)
            }

            val currentCard = currentPage["states"]
            val runtimeVariablesJson = div2View?.let { Div2ViewVariableExporter.captureVariablesJson(it) }
            val currentVariable = when {
                runtimeVariablesJson != null && runtimeVariablesJson.length() > 0 ->
                    mapper.readTree(runtimeVariablesJson.toString())
                else -> currentPage["variables"]
            }
            Log.d("UIDiv2ActionHandler", "Current states found: ${currentCard != null}")
            Log.d("UIDiv2ActionHandler", "Current variables found: ${currentVariable != null}")

            if (currentCard != null && key != null) {
                val document = mapper.createObjectNode()
                val cardNode: ObjectNode = when {
                    dbPage.has("card") -> (dbPage.get("card").deepCopy() as ObjectNode)
                    dbPage.has("states") -> (dbPage.deepCopy() as ObjectNode)
                    currentPage.has("card") -> (currentPage.get("card").deepCopy() as ObjectNode)
                    else -> mapper.createObjectNode()
                }
                cardNode.set<JsonNode>("states", currentCard.deepCopy())
                if (currentVariable != null) {
                    cardNode.set<JsonNode>("variables", currentVariable.deepCopy())
                } else if (cardNode.has("variables")) {
                    cardNode.remove("variables")
                }
                if (!cardNode.has("log_id") && currentPage.has("log_id")) {
                    cardNode.set<JsonNode>("log_id", currentPage.get("log_id"))
                }
                document.set<JsonNode>("card", cardNode)
                val templates = when {
                    dbPage.has("templates") -> dbPage.get("templates").deepCopy()
                    currentPage.has("templates") -> currentPage.get("templates").deepCopy()
                    else -> mapper.createObjectNode()
                }
                document.set<JsonNode>("templates", templates)

                val modifiedDbPageString = document.toString()
                mehdiViewModel?.insertItemToDbSync(PhPlusDB(null, key, modifiedDbPageString))
                Log.d("UIDiv2ActionHandler", "Successfully saved draft page to database with key: $key")
                syncVehicleArchiveFaultFromDraft(key, div2View)
            } else {
                Log.w("UIDiv2ActionHandler", "Cannot save draft: states or key is null")
            }

            Log.d(
                "UIDiv2ActionHandler",
                "=== AUTHORITY_SET_PAGE_WITH_CHANGES_TO_DB action completed ==="
            )


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

                .setInitDate(PersianDate().toDate())
                .setActionTextColor(Color.GRAY)
                //.setTypeFace(typeface) //  .setShowDayPicker(false)
                .setTitleType(PersianDatePickerDialog.DAY_MONTH_YEAR)
                .setShowInBottomSheet(true)
                .setListener(object : PersianPickerListener {
                    override fun onDateSelected(persianPickerDate: PersianPickerDate) {
                        if (varName != null) {
                            div2View.setVariable(
                                varName,
                                formatPersianDate(
                                    persianPickerDate.getPersianYear(),
                                    persianPickerDate.getPersianMonth(),
                                    persianPickerDate.getPersianDay()
                                )
                            )
                        }
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


        }
            else if (uri.authority == AUTHORITY_SET_PATCH || uri.authority == AUTHORITY_BOTTOM_SHEET_SET_PATCH) {

            val patchName = uri.getQueryParameter(PARAM_SET_PATCH) ?: return true
            val patchTitle = uri.getQueryParameter(PARAM_SET_PATCH_TITLE).orEmpty()
            val vehicleType = uri.getQueryParameter(PARAM_SET_VEHICLE_TYPE).orEmpty()
            var json = mehdiViewModel?.getValueByKey(patchName)?.value.orEmpty()
            if (json.isEmpty()) {
                json = loadPatchJsonFromAssets(patchName).orEmpty()
                if (json.isNotEmpty()) {
                    android.util.Log.d(
                        "UIDiv2ActionHandler",
                        "set_patch: loaded patch from assets for key=$patchName"
                    )
                    runCatching {
                        mehdiViewModel?.insertItemToDbSync(
                            PhPlusDB(null, patchName, json)
                        )
                    }
                }
            }
            if (json.isNotEmpty()) {
                val forceBottomSheet = uri.authority == AUTHORITY_BOTTOM_SHEET_SET_PATCH
                applyPatchFromAction(uri, json, patchName, patchTitle, vehicleType, forceBottomSheet)
            } else {
                android.util.Log.w(
                    "UIDiv2ActionHandler",
                    "set_patch: patch JSON not found for key=$patchName"
                )
            }

        }
            else if (uri.authority == AUTHORITY_SET_PATCH_FOR_LIST) {

            val div2View: Div2View = view as Div2View
            var patchName = uri.getQueryParameter(PARAM_SET_PATCH)
            var templateName = uri.getQueryParameter(PARAM_SET_PATCH_TEMPLATE)
            var patchTitle = uri.getQueryParameter(PARAM_SET_PATCH_TITLE).orEmpty()
            var vehicle_type = uri.getQueryParameter(PARAM_SET_VEHICLE_TYPE).orEmpty()
            if (patchName != null) {
                val json = mehdiViewModel?.getValueByKey(patchName)?.value.toString()
                val jsonObject = JSONObject(json)
                val patch = jsonObject.optJSONObject("patch")
                val changes = patch?.optJSONArray("changes")
                if (changes != null && templateName != null) {
                    for (i in 0 until changes.length()) {
                        val change = changes.getJSONObject(i)
                        val items = change.optJSONArray("items")

                        if (items != null) {
                            for (j in 0 until items.length()) {
                                val item = items.getJSONObject(j)

                                if (item.optString("type") == templateName) {

                                    uri.queryParameterNames.forEach { key ->
                                        if (key != "template_name" && key != "patch_name" && key != "dismiss") {
                                            val value = uri.getQueryParameter(key)
                                            item.put(key, value)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val updatedJson = jsonObject.toString()
                applyPatchFromAction(uri, updatedJson, patchName, patchTitle, vehicle_type, forceBottomSheet = false)
            }

        } else if (uri.authority == AUTHORITY_LOG_VT_REGISTER_VALIDATION) {
            logVtRegisterValidation(view)
        } else if (uri.authority == AUTHORITY_CALL_SERVICE) {
            com.yandex.div.internal.Log.d(
                "UIDiv2ActionHandler",
                "=== Processing AUTHORITY_CALL_SERVICE ==="
            )
            com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "URI: $uri")

            val parameterNames = uri.queryParameterNames
            com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "Parameter names: $parameterNames")
            com.yandex.div.internal.Log.d(
                "UIDiv2ActionHandler",
                "Parameter count: ${parameterNames.size}"
            )

            val map: java.util.HashMap<String, String> = HashMap()
            val div2View = if (view is Div2View) view as Div2View? else null
            val expressionResolver = div2View?.expressionResolver

            com.yandex.div.internal.Log.d(
                "UIDiv2ActionHandler",
                "Div2View available: ${div2View != null}"
            )
            com.yandex.div.internal.Log.d(
                "UIDiv2ActionHandler",
                "ExpressionResolver available: ${expressionResolver != null}"
            )

            for (parameterName in parameterNames) {
                var parameterValue = uri.getQueryParameter(parameterName).toString()
                com.yandex.div.internal.Log.d(
                    "UIDiv2ActionHandler",
                    "Processing parameter: $parameterName = $parameterValue"
                )

                // Process nested variable expressions if expressionResolver is available
                if (expressionResolver != null && parameterValue.contains("@{")) {
                    com.yandex.div.internal.Log.d(
                        "UIDiv2ActionHandler",
                        "Processing nested variables for: $parameterValue"
                    )
                    parameterValue =
                        processNestedVariableExpressions(parameterValue, expressionResolver)
                    com.yandex.div.internal.Log.d(
                        "UIDiv2ActionHandler",
                        "After processing: $parameterValue"
                    )
                }

                map[parameterName] = parameterValue
            }

            com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "Final map: $map")
            loadScreenListener.onRequest(map)


//            RemoteData.value.value = map

        } else if (uri.authority == AUTHORITY_UPLOAD_FILES_AND_CALL_SERVICE) {
            com.yandex.div.internal.Log.d(
                "UIDiv2ActionHandler",
                "=== Processing AUTHORITY_UPLOAD_FILES_AND_CALL_SERVICE ==="
            )
            com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "URI: $uri")

            val parameterNames = uri.queryParameterNames
            val map: java.util.HashMap<String, String> = HashMap()
            val div2View = if (view is Div2View) view as Div2View? else null
            val expressionResolver = div2View?.expressionResolver

            for (parameterName in parameterNames) {
                var parameterValue = uri.getQueryParameter(parameterName).toString()
                if (expressionResolver != null && parameterValue.contains("@{")) {
                    parameterValue =
                        processNestedVariableExpressions(parameterValue, expressionResolver)
                }
                if (parameterName.matches(Regex("file[1-5]_path")) &&
                    expressionResolver != null &&
                    !parameterValue.contains("@{") &&
                    !looksLikeFilePath(parameterValue)
                ) {
                    parameterValue =
                        processNestedVariableExpressions("@{$parameterValue}", expressionResolver)
                }
                map[parameterName] = parameterValue
            }

            com.yandex.div.internal.Log.d(
                "UIDiv2ActionHandler",
                "upload_files_and_call_service map: $map"
            )
            loadScreenListener.uploadFilesAndCallService(map)

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
            var dbKey = uri.getQueryParameter(PARAM_SET_VARIABLE_TO_DB_KEY) ?: name
            val div2View = if (view is Div2View) view as Div2View? else null
            val expressionResolver = div2View?.expressionResolver
            if (expressionResolver != null && dbKey.contains("@{")) {
                dbKey = processNestedVariableExpressions(dbKey, expressionResolver)
            }
            val dbValue = runCatching {
                mehdiViewModel?.getValueByKey(dbKey)?.value.orEmpty()
            }.getOrDefault("")
            if (dbValue.isNotEmpty()) {
                runCatching { div2View?.setVariable(name, dbValue) }
                return true
            }
            mehdiViewModel?.getPage(dbKey)?.observe(lo) {
                if (it.isNotEmpty()) {
                    it[0].value?.let { stored ->
                        div2View?.setVariable(name, stored)
                    }
                }
            }
            return true
        } else if (uri.authority == AUTHORITY_SET_VARIABLE_FROM_DRAFT) {
            var draftKey = uri.getQueryParameter(PARAM_SET_VARIABLE_TO_DB_KEY)
            val namesParam = uri.getQueryParameter("names")
            if (draftKey.isNullOrBlank() || namesParam.isNullOrBlank()) {
                Assert.fail("key and names params are required for set_variable_from_draft")
                return false
            }
            val div2View = view as? Div2View
            val expressionResolver = div2View?.expressionResolver
            if (expressionResolver != null && draftKey.contains("@{")) {
                draftKey = processNestedVariableExpressions(draftKey, expressionResolver)
            }
            val draftJson = runCatching {
                mehdiViewModel?.getValueByKey(draftKey)?.value.orEmpty()
            }.getOrDefault("")
            if (draftJson.isBlank()) {
                android.util.Log.w(
                    "UIDiv2ActionHandler",
                    "set_variable_from_draft: draft not found for key=$draftKey"
                )
                return true
            }
            val draftVars = DraftVariables.extract(draftJson)
            namesParam.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { varName ->
                val value = DraftVariables.run { draftVars.valueOf(varName) }
                android.util.Log.d(
                    "UIDiv2ActionHandler",
                    "set_variable_from_draft: $varName='${value.take(40)}' from $draftKey"
                )
                if (value.isNotEmpty()) {
                    runCatching { div2View?.setVariable(varName, value) }
                }
            }
            return true
        } else if (uri.authority == AUTHORITY_INQUIRY_BIMEH) {
            val div2View = view as? Div2View
            val expressionResolver = div2View?.expressionResolver
            fun resolveParam(name: String): String {
                var value = uri.getQueryParameter(name).orEmpty()
                if (expressionResolver != null && value.contains("@{")) {
                    value = processNestedVariableExpressions(value, expressionResolver)
                }
                return value
            }

            var vehicleId = resolveParam("vehicle_id")
            if (vehicleId.isBlank() && div2View != null) {
                vehicleId = runCatching {
                    getDivVariableValue(div2View, "current_vehicle_id")?.toString().orEmpty()
                }.getOrDefault("")
            }
            if (vehicleId.isBlank()) {
                vehicleId = runCatching {
                    mehdiViewModel?.getValueByKey("current_vehicle_id")?.value.orEmpty()
                }.getOrDefault("")
            }
            if (vehicleId.isBlank()) {
                android.util.Log.w("UIDiv2ActionHandler", "inquiry_bimeh: current_vehicle_id is empty")
                return false
            }

            val draftKey = "draft/$vehicleId/page1"
            val draftJson = runCatching {
                mehdiViewModel?.getValueByKey(draftKey)?.value.orEmpty()
            }.getOrDefault("")
            val draftVars = DraftVariables.extract(draftJson)
            val chassis = DraftVariables.run {
                draftVars.valueOf("variable_chassis_number")
            }.ifBlank {
                runCatching {
                    mehdiViewModel?.getValueByKey("variable_chassis_number/$vehicleId")?.value.orEmpty()
                }.getOrDefault("")
            }
            val engine = DraftVariables.run {
                draftVars.valueOf("variable_compromising_engine_number")
            }.ifBlank {
                runCatching {
                    mehdiViewModel?.getValueByKey("variable_compromising_engine_number/$vehicleId")?.value.orEmpty()
                }.getOrDefault("")
            }

            runCatching { div2View?.setVariable("variable_chassis_number", chassis) }
            runCatching { div2View?.setVariable("variable_compromising_engine_number", engine) }

            android.util.Log.d(
                "UIDiv2ActionHandler",
                "inquiry_bimeh: vehicleId=$vehicleId chassis='$chassis' engine='$engine' draftLen=${draftJson.length}"
            )

            val map = java.util.HashMap<String, String>()
            map["path"] = uri.getQueryParameter("path") ?: "inquiryBimeh"
            map["sysName"] = uri.getQueryParameter("sysName") ?: "accidents"
            map["ph/token"] = uri.getQueryParameter("ph/token") ?: "empty"
            map["variable_chassis_number"] = chassis
            map["variable_compromising_engine_number"] = engine
            loadScreenListener.onRequest(map)
            return true
        } else if (uri.authority == AUTHORITY_SET_VARIABLE_FROM_DB_AND_SET_PATCH) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)
            if (name == null) {
                Assert.fail(PARAM_VARIABLE_NAME + " param is required")
                return false
            }
            if (name != null) {
                val value = mehdiViewModel?.getValueByKey(name)?.value
                val div2View = if (view is Div2View) view as Div2View? else null

                if (value != null) {
                    div2View?.setVariable(name, value)
                }

            }
//

            return true
        } else if (uri.authority == AUTHORITY_SET_IMEI) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)

            if (name != null) {
//                    json == it[0].value
                val div2View = if (view is Div2View) view as Div2View? else null
                val androidId =
                    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                div2View?.setVariable(name, androidId)
            }
            return true
        } else if (uri.authority == AUTHORITY_SET_IP) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)

            if (name != null) {
//                    json == it[0].value
                val div2View = if (view is Div2View) view as Div2View? else null
                val androidId =
                    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                div2View?.setVariable(name, androidId)
            }
            return true
        } else if (uri.authority == AUTHORITY_SET_DEVICE_MODEL) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)

            if (name != null) {
//                    json == it[0].value
                val div2View = if (view is Div2View) view as Div2View? else null
                val androidId = android.os.Build.MODEL

//                val androidId =
//                    Settings.Secure.getString(context.contentResolver, Settings.Secure.)
                div2View?.setVariable(name, androidId)
            }
            return true
        } else if (uri.authority == AUTHORITY_RESET_PHID) {
            var phId = UUID.randomUUID().toString()
            mehdiViewModel?.insertItemToDb(PhPlusDB(null, "phid", phId))
            return true
        } else if (uri.authority == AUTHORITY_SET_RANDOM_ID_TO_DB) {
            val key = uri.getQueryParameter(PARAM_SET_VARIABLE_TO_DB_KEY)
                ?: uri.getQueryParameter(PARAM_VARIABLE_NAME)
            if (key.isNullOrBlank()) {
                Assert.fail("key (or name) param is required")
                return false
            }
            val randomId = "${System.currentTimeMillis()}_${UUID.randomUUID()}"
            mehdiViewModel?.insertItemToDb(PhPlusDB(null, key, randomId))
            return true
        } else if (uri.authority == AUTHORITY_SET_VARIABLE_TO_DB) {
            val key = uri.getQueryParameter(PARAM_SET_VARIABLE_TO_DB_KEY)
            val value = uri.getQueryParameter(PARAM_SET_VARIABLE_TO_DB_value)
            if (key == null) {
                Assert.fail(PARAM_VARIABLE_NAME + " param is required")
                return false
            }

            if (key != null) {
                value?.let { PhPlusDB(null, key, it) }?.let { mehdiViewModel?.insertItemToDbSync(it) }
            }
            return true
        } else if (uri.authority == AUTHORITY_SET_TIME_TO_DB) {
            val key = uri.getQueryParameter(PARAM_SET_VARIABLE_TO_DB_KEY)
            if (key == null) {
                Assert.fail(PARAM_VARIABLE_NAME + " param is required")
                return false
            }

            if (key != null) {
                mehdiViewModel?.insertItemToDb(PhPlusDB(null, key, PersianDate.today().toString()))
            }
            return true
        } else if (uri.authority == AUTHORITY_SET_VARIABLE_TO_DB_PLUS_ONE) {
            val key = uri.getQueryParameter(PARAM_SET_VARIABLE_TO_DB_KEY)
            val rawValue = uri.getQueryParameter(PARAM_SET_VARIABLE_TO_DB_value)
            val value = (rawValue?.toInt()?.plus(1)).toString()
            val div2View = if (view is Div2View) view as Div2View? else null

            if (key == null) {
                Assert.fail(PARAM_VARIABLE_NAME + " param is required")
                return false
            }

            if (key != null) {
                div2View?.setVariable(key, value)
                PhPlusDB(null, key, value).let { mehdiViewModel?.insertItemToDb(it) }
            }
            return true
        } else if (uri.authority == AUTHORITY_CHANGE_COLOR) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)
            if (name == null) {
                Assert.fail(PARAM_VARIABLE_NAME + " param is required")
                return false
            }
            val rawColor = uri.getQueryParameter(PARAM_VARIABLE_VALUE)
            if (rawColor == null) {
                Assert.fail(PARAM_VARIABLE_VALUE + " param unspecified for " + name)
                return false
            }
            val value = "#$rawColor"

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
//val test="/9j/4AAQSkZJRgABAQECWAJYAAD/2wBDACAWGBwYFCAcGhwkIiAmMFA0MCwsMGJGSjpQdGZ6eHJmcG6AkLicgIiuim5woNqirr7EztDOfJri8uDI8LjKzsb/2wBDASIkJDAqMF40NF7GhHCExsbGxsbGxsbGxsbGxsbGxsbGxsbGxsbGxsbGxsbGxsbGxsbGxsbGxsbGxsbGxsbGxsb/wAARCAEsAOEDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDoKKKKACiiigAoopCQOtAC0UisG6UtABRSbhux3o3DdjvQAtFFFABRRSFgCM96AFooooAKKKKACik3DOKWgAoopCcDJoAWikHIpaACikzQCCSPSgBaKKKACiiigApFbcKCQOpxTVI+Y9qADfz049acfTODUZ9FbPtTtrZzu5oABhc80jA7skZFGw/3qXa396gBwwRx0paQDAxRn5se1ADT/rB7igf6xvpSt95fXNLj5s0AIzYPTOOtBbGMDOaaxGSQ3XtUTzrEQjyAHHSgCcOD14PvSN1X61QuNQhjBChnbufSs976VsZduKAN95UQ4ZgDS5yBg4zXMtdSu5YsSTT1uXTGHP50gOk6Dk0ZArEh1CVhh5Mge1akcgkUEyDNMCY4DAY606mBTnLHNOPSgA5z04pG5Q4pQQVzmmoPlI7UAKDhAfahW3cYwabsOMbuKUAg7mNAC5AfGOTSD/WNQSGYY5xSspJyDigBcjn2pN3IHqKbsbpu4PWlCnIJOcUAPooooAQgHqKWiigBAAOgpaKKACiikJx1oAWkZQ3WgMG6UtADQFQZ6e9QPexJnOeO9QajdrE8cQPU5Ye1ZV3cboFA+83JoAvSaqSSUVVHYnkmsye5Z3LseTUDSAcCoWfNIZM03vSK2eo4qDrT8nbxQBMGHbpQzCoQ2MfSgHPWgCUPgirMN06OpB6HvVANg59KA5DZ96BHUw6ijj5/3bD171Jb3SSDlue1cwkjZz71YW5YLjcfwoA6bYvXFPrI0zUAcQytn+6xrWJAGTTAWikHIpaAEAA6ClopAQSR6UALRRRQAUUUUAFIrbhQSB1OKauMse1AC7+enHrSnpjODUZ9FbPtTtr5zuGaAFGFzzxTWHzZIyKNjf3velw/96gBwxjjpVa4k2v94qR0qS4mW2gLtkgfqa5ue7lmZmJxk5wO1ACXlwZZ5HJ5NVmclBQ2eaQYYYFIYirkc0qxk9qmSLA5qQL05ouFir5RHal28VZIyeKQoPxpXHYpleaXoeamMf4Upj9R1p3FYg6ngUmD6VY8v0H40FO2KLhYgBO3r0oVjT3TA4HFQj72KYE6v36Gtex1EqvlzHKnoT2rFHB6VMrcelIDrlI2A9sUK272rN0m4aeExM3MfT3FaIBB3MRTELkB8Y5NIP8AWNQSCwxyRSspzlTigBcj8qTcMgeozTdjcjdwetKFOQSc4oAfRRRQAhAPUUtFFACAAdBS0UUAFFHWmswXqaAMjWb0EG3T1+Y1i596muSWndie5qEDPakMMknCjirEVsW5IqS0t9x5HFaUaAcAdKlspIpi3KjpmmtAT0rT2ijaPSpuVYyWhZO1OWIkdK0igNNKLkDFFwsUfIz2pPI7YrQ2CjYKLhYoeTgYpDD7Vf8ALFGwCi4WM1ocjFUpoSjZrbKDNV54NymmmJoydzD0pVOfY06RCrcimqM59asg0tHm8u9UH+MbTXR1yNtJ5c8bj+FhXWjkUxAAB0FLSAg9O1AbJPtQAtFFFABRRRQAU1W3LmgsF60i8AkjANAAGORkcHpTjzxnFR4ycKTj+VO2H+8aAAEAcZ5NNcFdzDBp3l/7R9qjuAVt5GLnhTQBy8uS5zzk0+2h8yQCo2q/p645qWNF6OMIoAFPopCeRioNB2aKbmmjOeTSGPzRSZopgKaSkNGaAFJppNGaaWpAL2pCMim5pQaBFO7t8jcBzWaOHxW5J9w1jzLhzj1q4smQkTbWzjoeldTaXUd1Huj7da5UdK39E4syx6biKsg0QfnIxSL95qAQzAjt3pSuTkEigBSwAJ9KTd82Pak8vtuOKULg5JzQA6iiigAooooAKKKKACobz/j0l/3DUoOelMm2+U4buDxQByhFaFkKz8fNWnZoQoJqJFItUUnJNOx71BoJigCnZ96XqKYDcUnNOoxQA05pO1PxQRQAzmkK+1P4HemM6jqaLBcYeO1JSl19aDg8ilYVxrn5TWTP941rHkVm3C4kI9aqJMiAV0GiY+yNg/xVz54x9a6LRVxZZ9WNWQaFFJkEkelAbJI9KYC0UUUAFFFFABTVbcDSswXrTV7tjrQAbjnOPlNOOOnrUeAxwuad5f8AtHNAACBkjPXFDKclhg+xo8sepo2f7RoA5hl/fsoH8ZFayKEQCqhixqrp2DFqtyNtUmokXESSZYlyTzVF70k9aZKHlckmkFpk5Zv1oSDUX7eR2qVNRB4IqI2SEcSfrUf2TaeDT0DU0o59/Papw1ZtuDGcVfU5FSUP3ioZZtgpW4qpOd/yg0ARyXpzxUD3LN3zT/s4J4p620fViPzqtCbMq+c2atW10M7WOPrTzFF2x+FQPD/do0CzNEc1VvY+A47dadaucbG6irDrviZfUVOzH0MdhzXR6Qf9CQfWudPUCuj0xNljGx5yM1oZlvPz49RSD/WNRncwwOB3pWXJzkigBdw59qTd8wGOopPLHqcUoTBzkmgB1FFFABRRRQAUUUUAFMeRU+8cU7OelZlyzLdtk5B7Um7FRVxuFbUZnU5G0frRLkjFEPLSPjq2PypxGahspIqu2xeBk1XL5bao8x/0FXZIt3XpUSxCJt0fBoQGe9zKGKnaMdgtPhaRozJjABwSKnltklk3uCD3wcZqRFVUCKPlHbNVoCTGxtuGSKvQjK1AkSgHAxnrVqNcJUDIbg4UkVSPHJq7OMrVfywylTxQhlfzCTgAn2FRfbSo4jXFXEVYidvfrnvVWWyUuSrYGemM1asS7gtwkgBdNuehqVSc9cjsaDEpQRquFHenxQbeAaTAVVw2R1q1H05qIJipAdoqQMuVCJ2QDJ3YFdJatGkMcSsDtGKxyg+2Ekfw5FTqXM6KnHIqri5bmzRSAgnHcUBskj0qyBaKKKACiiigApqtuBpWYL1pq92xwaADcc5x8ppxx09ajwCcKTTvL/2jmgABAyRnriqFwv8ApbN1xV7y/c1UvYyo3ZJyKmRUdyGDBiBHck/rUgFRW/8AqI/pUwqGUIRmozGD1qWgjNAyAxKO1Cx5NTbaXFAEeOgqUdKj71IvSgCOQcVABg1aaoSMGgYwpTfJB7kVOBS4pgVxB7k1IseKlpCaQDCKRhxTqRjxQIgl4aNvwNWbQbrpCR2qnc8beeM1oaemGZzk4FUK+hez8+MdRSD/AFjUZDMMdu9Ky5OckGrMxcjn2pN3zAY6ik8v/aOKUJg5yTQA6iiigAooooAKKKKACobld0J9qlByMimSMNpXqcUMEZcBwu30yKmzUT5ExzxTwazZaJBTu1RZ9KeDxSKFpD60daRjxQMReuTUgqpJcqhxg5+lKlyD7fWmIsvgComwaie5GOtQfaTnhWIosBcXjinVXhmLt0xirGaBic59qQnijNNJpAxCaaTmimM2BmmSQzqZGVV656VsWMeyHJ6ms635mzjtwa14ceUuOmKpbkvYkopAck+1IrZJ9qokdRRRQAUUUUAFNVsrz1FDNjsTQMgEkcntQAgY5BJGD+lKcH5TTNoY8AgU7y196AAMOw6mkdTglTye1L5a0eWtAGbKDv5BHFMB5yelaF4uYcjsaz2NQ0UmP3UoNQhutOLYBNSUiUsB1NI0igE5rMlustwenSonmJ707BzFme5QnOOneoWuQ3UcVBtLt1PSnrDuIGcD1NVsLVjzIoGad9oXv6ZFPa2j28yc1TePBOOgoG7o0oZYscHGashh61hgshAzxU0VwV4NKwcxqk0081DDN5gx6U5j71IxSaa3IximhqcvI60ySSAHdhRknGK1ogVXYeopkEKpCoPXFShQnIBNWkS2GTvx2xxSLw7UAlmBxgClZAxyaYgLDBPpRk78dsUnlrShApyKAHUUUUAFFFFABRRSE4HNAC0UitmkLHOAMmgBJF3xsvqKyW6YzyK11zj5utZt+nly7h0akxordPc0lw+2Fjn8PWkDd8VHO3yY9agozycGlRHbkL1qxbwh23N3q8I1jHAqmwSKaW7NyTj6CphakdHNSPLs7VCb3HQUi7pCmCQ/xj8qheBgPvZ/CpfthzjAo8/d2oC6Kjhu4yagJIbB4rT27hnHWop7ZSMgYNNMloitXKuOeCOKts3BqlFw209ulWAx6UmJEgOas2yeZIq+vWqg9q09PQ4aX04FCEzRpaReQKWrJCikBOTxxSAkuw9KAHUUUUAFFFFABTUzyD1FK2ewzSAEAnuaAGbud27nuKdvGcZFG0seQAKXavoKAG7+Oo60NsPOcH1p+0ego2j0FAApyoJqvfqGtWOMleRVmqeoyiKJc9GYA0AZGfypHIK884olGxz/AHT+lMJJUjrUFDoZAuAKtB84zisgSFWqxHLgcnr1oaGmWp0yvHWs6RSrHParBuCz9cAdqilILdO1NA2QoTmrkMXy56nvVZVAlx71Y87bGMUMSLS4CDFQzTAVGJcDGeKrzNzkdD1pJDbJF+aTdUvSq1u3Jz3FT5yaYh6/NW9aAfZFUdQOfrWHDjduPQVraXJvib35poTLe8BBg80IfmxnNP2j0FJjH3QM0xAchx6GkyFkOeMilAYnLcY7UpAPUZoAb5gwf0oDZZec8U7avoKMAHOKAFooooAKKKKACiikJCjJOBQAtJVd7tR90ZPXHrVK4u5GO0bvwHY0AXpbyGLhnGfQGqF9L9qtSRwCpI59KrojyEbi2eD1HSrJULGq9QAB/SnYRnq/mRgH0qInacE9ehNLtMcjIexxQw3DBFZllSQYajcQKdNkVFnFUSSA85zShjuGT0qLdxRk0wJC3zZo3ZBqItSg56elAEu7rmo2Y0hJAxSdTQBPEMDPrUincfb1qNQSB6VMOKllIV3wm0d+K1NOk8qNTyRisjG5vatG1O2MY7L/AFqkSzWS7hfjeAfQ8VMCD05rKDHjIz6e9ORtvKSEduKdhGpRVJLpwSGAIHepo7lWYgnbjsaQyeikBz0paACiiigAqMOFyHPIqvJcscCMdehquWJG4t1PU9j6UxE73XOUPJ6Z71BJOXIG7JPT29qYTk/MNuTzxyDTXII5wD6E9/oKAGvLx2GeQCfz4FRKis38PHTg1KMYO0f7QwNo9xk0IB1wCPTOfoaYEibVQAcZ6Y7U4/MMZ68fnTHOFBHJ/maUZ5x34pAUrtfmWQcEjke4qPGauzr5itwcfe/xqkOMg9RUyLiRuuetVim0n5c1dxmo3T0pJjaKhUf7ppF4DVK0WT6UzyiOlO5NiLvTiCDwOKk8n3pwiI70XCxCFLHmpY4u5qRUxyakAouNIQClxTgvHNGMsB2zSHsIo4+tXrf7g5/hPH41TUdsVfhGAB1+VeM571ZmKWGO/uRzQpIbqDx9cCmOuGbAPrnr3pwyBgAnBI570AOOQRjp2WlEmG5Of6/SmKTn5sr3zjrTsbuwB9OwoAlS4K5wcY9DxU8d3ll3cgjt/hVMKB247D1/z704cf1I7+1AGj56etFUOf8Ann+oooAa2STwRnOflPUd6TlmPJOduflNNzkcAfx9Hpy9R9V6v7UAG35TkclfT1P1oY4b/gR7egoGMDA/u9Fo6DOD0Y/dxQA0AYAx/dH+c05QO2O/b3oX7w4wdy/yoBJAA7Dr+NACsN2B/tdh6imxHC8HtkUjnA5IJwDzz0NNncxoSCcgNj8+OKAIri9ghbaTuIJ4A7GqwkSXDJnpgg1BsC5J5PB3YpQfLbcBx3HrRYaLANKRnrS4RwDHwSenam9OKzasaJ3GlT2phX1FSk0meaBkWB6UAc8Cpc0CgLDdnrTgoFLRmkAhpAMDPr/jQeafjkjsM4q4oiTHRjgknt/Srqn5iM5+ZRjOeg96z5ZXjwsX334/CnR3E45l+bndyPwqiC1IM84z8n9aXox4/iPVfakdlePeCCCMZIz6UDG44x1bo2O1ACgg7cf7OQD7ehpF+7nJxjJHHrSjPc8fL1INICBkfL0YelAD8tu6jrjrS78AHgZH4AUmSWHDdR0Oe1MzwPovVPegCTcv+3RT+P8AZ/74opgQ9Y8/7Lfwg/ypy5GQM5B7Lj+Go+fLB4HydTx1PqKe2Mtkg8nktntSAbn5hweq5y1Jnj7o6N1b3pcAkZMfVP4adGBlfu9T90e9ABv+fqPv9j6ClGCq9QdvpSqAcZOT+fJpSwVTjA4yB+OKAGvkjGT0YcMBUNz80ZA56cDnqP8A61THmTH+0w+57VG+dnIJG1T0x0pgUzyByx+UdveomDKeARyfTtVjy2xghuAw4NI0YzyEGc9eeozQBXUlD8p+XPTPNTB0def0pDGo5DDHHQGmhe4UHHofelYB+PQ5pCKUYycDFP25GRUNWNE7kXNKDUmyjbUlDKKk20uzuSPU/SmlcTdiNULsMdutSoFGTIQB356ClLxRDczocehBqo7vNgbSsakYAGM59a0SsZt3HKxdtzYHzcAjoAKmj6AEYBx7j1qMZAJ+Yfe96kQgMMEdexx2piGW7lVdMnaTkY7c1bU/PjJI3N1x6VXVcOG9VzVnkNwD9/tj0oATOAMd1XoR60M2GbJYfe6jNNByBuI6KPmX3pcZBIHY/db3oAMqTxtzk+x6UoOOhOML/HTwDv8A4vvN1X2pOQuefur2A70gJ8+//j1FLn3/APHhRTArDhQOh2qPQ9fyp/OT16sfuUko2AkdAUGDyKWMBkz04foaAD5s/wDLTqnYUYO3kOfvHnA70/YuemeU6moXbaOFXo/b3oAU55UZOM4CdOORzS4xuA4yDwvX1qQjgk88nj/gNNh/edeB8vA47UgEOA+cY+dTy/qKibG3H7v7rDk56U2WUocBVxtTt70wztu6L95u3tTAdgM/RD8w6H1FCjaFPyL90+vtTYpC7JuVf4e3tQ0hVAFCjj0/2qAA52n52Py9l9DUUiYLZA/EY96tIpduWb7zDj6UjxjBySchepoArMoBPCAbsdaVDjuPcYNBb5Sdq8kdvapYhviLljn5Tx70rAPCgjIpu0evHcmlHynaDke9R3LFVXB6jJrO2tjW+lwMoH3FB9yaryEytlnA5AxQx3LnAB9qAzM/J/i/pWljMjWOMDOEJwepqUKN3ROq/wAVNJKp1/hNOEp39F+8O3tTESBflOB2b7re9OAbfzu4f0HpSRkOvKr909vepfLXzSBkfN2PtQAzgquWHC9CuOpp/ckKOrHKn2pjkxhQDn7vWnR4kX5lH3W7e9AEi5BA+ccqORntScbP4Pu9xjvUoQB+Mj5h39qXZ8n3m+4f50ANGN/b7/bJ7UY+Tofuf3fepCg3nJJ+cd/aowAQBtH3W/nSAsbj/tfkKKgwPSimI//ZAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
            // Add padding if necessary
            val imageBytes = Base64.decode(cleanedBase64String, Base64.DEFAULT)
            val decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//            loadScreenListener.showImage(decodedBitmap)
//            dialogImage.setImageBitmap(decodedBitmap)

            if (cleanedBase64String.isNullOrBlank()) {
                Toast.makeText(context, "تصویری برای نمایش وجود ندارد", Toast.LENGTH_SHORT).show()
            } else if (decodedBitmap == null) {
                Toast.makeText(context, "خطا در نمایش تصویر", Toast.LENGTH_SHORT).show()
            } else {
                val dialogView = LayoutInflater.from(context)
                    .inflate(com.yandex.divkit.demo.R.layout.dialog_image, null)
                val dialogImage = dialogView.findViewById<ZoomableImageView>(
                    com.yandex.divkit.demo.R.id.dialogImageView
                )
                dialogImage.setImageBitmap(decodedBitmap)

                val dialog = AlertDialog.Builder(
                    context,
                    android.R.style.Theme_Black_NoTitleBar_Fullscreen
                )
                    .setView(dialogView)
                    .create()
                dialogView.findViewById<android.widget.TextView>(
                    com.yandex.divkit.demo.R.id.dialogImageClose
                )?.setOnClickListener { dialog.dismiss() }
                dialog.setCanceledOnTouchOutside(true)
                dialog.show()
                dialog.window?.setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

        } else if (uri.authority == AUTHORITY_SHOW_MASSAGE) {

            Toast.makeText(context, uri.getQueryParameter(PARAM_TOAST), Toast.LENGTH_LONG).show()

        } else if (uri.authority == AUTHORITY_BOTTOM_SHEET_DISMISS
            || uri.authority == AUTHORITY_CLOSE_BOTTOM_SHEET
        ) {
            dismissCurrentBottomSheet()
            val focusId = uri.getQueryParameter("focus")?.trim().orEmpty()
                .ifEmpty { pendingFocusAfterDismiss.orEmpty() }
            pendingFocusAfterDismiss = null
            if (focusId.isNotEmpty()) {
                scheduleFocusOnBase(focusId)
            }
            return true
        } else if (uri.authority == AUTHORITY_SET_VARIABLE_TO_BASE) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)
            val value = uri.getQueryParameter(PARAM_VARIABLE_VALUE)
            if (name != null) {
                if (value != null) {
                    loadScreenListener.setVariableToBase(name, value)
                    // Plate letter sheet: after choosing plateB, focus plateC when sheet closes.
                    if (name == "variable_plateB" && value.isNotBlank()) {
                        pendingFocusAfterDismiss = "plateC"
                    }
                }
            }
//            btmSheet_div.dismiss()
        } else if (uri.authority == AUTHORITY_SET_VARIABLE_TO_BOTTOM_SHEET) {
            val name = uri.getQueryParameter(PARAM_VARIABLE_NAME)
            val value = uri.getQueryParameter(PARAM_VARIABLE_VALUE)
            val target = uri.getQueryParameter(PARAM_TARGET_BOTTOM_SHEET)

            if (name != null && value != null && target != null) {
                setVariableOnTarget(name, value, target, view)
            }
        } else if (uri.authority == AUTHORITY_ADD_ITEM_FROM_TEMPLATE) {
            println("UIDiv2ActionHandler: ========== ADD ITEM FROM TEMPLATE ==========")
            
            val templateKey = uri.getQueryParameter(PARAM_TEMPLATE_KEY)
            val templateType = uri.getQueryParameter(PARAM_TEMPLATE_TYPE)
            val containerId = uri.getQueryParameter(PARAM_CONTAINER_ID)
            val position = uri.getQueryParameter(PARAM_POSITION)?.toIntOrNull()
            
            println("UIDiv2ActionHandler: templateKey=$templateKey")
            println("UIDiv2ActionHandler: templateType=$templateType")
            println("UIDiv2ActionHandler: containerId=$containerId")
            println("UIDiv2ActionHandler: position=$position")
            
            if (containerId != null) {
                try {
                    // Extract variables from query parameters
                    val variables = mutableMapOf<String, String>()
                    val parameterNames = uri.queryParameterNames
                    
                    for (paramName in parameterNames) {
                        // Skip system parameters
                        if (paramName !in listOf(PARAM_TEMPLATE_KEY, PARAM_TEMPLATE_TYPE, PARAM_CONTAINER_ID, PARAM_POSITION)) {
                            val paramValue = uri.getQueryParameter(paramName)
                            if (paramValue != null) {
                                variables[paramName] = paramValue
                            }
                        }
                    }
                    
                    println("UIDiv2ActionHandler: Variables extracted: $variables")
                    
                    // Get current view
                    val div2View = if (view is Div2View) view as Div2View? else null
                    
                    // Generate patch
                    val patch = if (templateType != null) {
                        // Using DivKit template reference (type-based)
                        println("UIDiv2ActionHandler: Generating patch with template type reference")
                        patchGenerator.generateAddItemPatch(
                            "", // Not needed for template type
                            containerId,
                            variables,
                            position,
                            templateType,
                            div2View
                        )
                    } else if (templateKey != null && mehdiViewModel != null) {
                        // Using full template from database
                        println("UIDiv2ActionHandler: Loading template from database")
                        val templateDto = mehdiViewModel.getValueByKey(templateKey)
                        
                        if (templateDto != null && templateDto.value != null) {
                            val templateJson = templateDto.value!!
                            println("UIDiv2ActionHandler: Template loaded successfully")
                            patchGenerator.generateAddItemPatch(
                                templateJson,
                                containerId,
                                variables,
                                position,
                                null,
                                div2View
                            )
                        } else {
                            println("UIDiv2ActionHandler: ERROR - Template not found in database: $templateKey")
                            null
                        }
                    } else {
                        println("UIDiv2ActionHandler: ERROR - Neither template_type nor template provided")
                        null
                    }
                    
                    if (patch != null) {
                        println("UIDiv2ActionHandler: Patch generated, applying to view...")
                        
                        // Apply patch to the current view
                        if (div2View != null) {
                            div2View.applyPatch(patch.asDivPatchWithTemplates())
                            println("UIDiv2ActionHandler: Patch applied successfully!")
                            
                            // Force view to refresh/invalidate to show the new items
                            div2View.post {
                                div2View.requestLayout()
                                div2View.invalidate()
                                println("UIDiv2ActionHandler: View invalidated and layout requested")
                            }
                        } else {
                            println("UIDiv2ActionHandler: ERROR - view is not Div2View")
                        }
                    }
                    
                } catch (e: Exception) {
                    println("UIDiv2ActionHandler: ERROR - Failed to add item from template: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                println("UIDiv2ActionHandler: ERROR - Missing required parameter: container_id")
            }
            
            println("UIDiv2ActionHandler: ========== END ADD ITEM FROM TEMPLATE ==========")
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
            val div2View = if (view is Div2View) view as Div2View? else null
            val expressionResolver = div2View?.expressionResolver
            for (parameterName in parameterNames) {
                var parameterValue = uri.getQueryParameter(parameterName).toString()
                if (expressionResolver != null && parameterValue.contains("@{")) {
                    parameterValue =
                        processNestedVariableExpressions(parameterValue, expressionResolver)
                }
                map[parameterName] = parameterValue
            }
            btmSheet_list = BottomSheetSpinner(
                context,
                this,
                uri.getQueryParameter(PARAM_BOTTOM_SHEET_LIST_ID).toString(),
                uri.getQueryParameter(PARAM_BOTTOM_SHEET_LIST_TITLE).toString(),
                uri.getQueryParameter(PARAM_BOTTOM_SHEET_LIST_CODE_VAR).toString(),
                uri.getQueryParameter(PARAM_BOTTOM_SHEET_LIST_TITLE_VAR).toString(),
                view,
                map,
                loadScreenListener,
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
                    val fullScreen = uri.getBooleanQueryParameter("fullScreen", false)
                    btmSheet_div = bottomSheetManager?.createAndAddBottomSheet(
                        context, activity, it, mehdiViewModel, lo, fullScreen = fullScreen
                    ) ?: BottomSheetDiv(context, activity, it, mehdiViewModel, lo)
                    btmSheet_div?.fullScreen = fullScreen
                    val liveVars = (view as? Div2View)?.let {
                        Div2ViewVariableExporter.captureVariablesMap(it)
                    }.orEmpty()
                    val filtered = liveVars
                        .filterKeys { key ->
                            key.startsWith("profile_") ||
                                key.startsWith("facility_") ||
                                key.startsWith("payslip_") ||
                                key == "image_profile"
                        }
                        .mapValues { entry -> entry.value?.toString().orEmpty() }
                        .toMutableMap()
                    val username = mehdiViewModel?.getValueByKey("userName")?.value.orEmpty()
                    if (it.contains("payslip") && username.isNotEmpty() &&
                        filtered["payslip_personnel_code"].isNullOrEmpty()
                    ) {
                        filtered["payslip_personnel_code"] = username
                    }
                    filtered.putAll(
                        resolveBottomSheetArrayVariables(uri, view as? Div2View, actionLogId)
                    )
                    btmSheet_div?.initialVariables = filtered
                    btmSheet_div?.show(
                        (context as FragmentActivity).supportFragmentManager,
                        "bottomSheet_${it.replace('/', '_')}"
                    )
                }
                btmSheet_div?.let { loadScreenListener.getBtmSheetInstance(it) }
            }

        } else if (uri.authority == AUTHORITY_SET_HOTEL_PRIORITY) {
            handleSetHotelPriority(uri, view)
        } else if (uri.authority == AUTHORITY_CLEAR_HOTEL_PRIORITY) {
            handleClearHotelPriority(uri, view)
        } else if (uri.authority == AUTHORITY_SAVE_HOTEL_PRIORITIES) {
            handleSaveHotelPriorities(view)
        } else if (uri.authority == AUTHORITY_PUSH_TO_STACK) {
            android.util.Log.d("UIDiv2ActionHandler", "=== PUSH TO STACK ACTION ===")

            val stackKey = uri.getQueryParameter(PARAM_STACK_KEY)
            val stackValue = uri.getQueryParameter(PARAM_STACK_VALUE)

            android.util.Log.d("UIDiv2ActionHandler", "Stack Key: $stackKey")
            android.util.Log.d("UIDiv2ActionHandler", "Stack Value: $stackValue")

            if (stackKey != null && stackValue != null) {
                try {
                    // Get current stack from database
                    val currentStackJson = mehdiViewModel?.getValueByKey(stackKey)?.value
                    android.util.Log.d("UIDiv2ActionHandler", "Current stack JSON: $currentStackJson")

                    // Parse existing stack or create new one
                    val stackList = if (currentStackJson != null && currentStackJson.isNotEmpty()) {
                        try {
                            val gson = Gson()
                            gson.fromJson(currentStackJson, Array<String>::class.java).toMutableList()
                        } catch (e: Exception) {
                            android.util.Log.w("UIDiv2ActionHandler", "Failed to parse stack, creating new: ${e.message}")
                            mutableListOf<String>()
                        }
                    } else {
                        mutableListOf<String>()
                    }

                    android.util.Log.d("UIDiv2ActionHandler", "Current stack size: ${stackList.size}")

                    // Push new value to stack
                    stackList.add(stackValue)
                    android.util.Log.d("UIDiv2ActionHandler", "New stack size: ${stackList.size}")

                    // Convert back to JSON and save to database
                    val gson = Gson()
                    val newStackJson = gson.toJson(stackList)
                    android.util.Log.d("UIDiv2ActionHandler", "New stack JSON: $newStackJson")

                    mehdiViewModel?.insertItemToDb(PhPlusDB(null, stackKey, newStackJson))
                    android.util.Log.d("UIDiv2ActionHandler", "Stack saved to database successfully")

                    Toast.makeText(context, "Added to stack: $stackValue", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.util.Log.e("UIDiv2ActionHandler", "Error pushing to stack", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                android.util.Log.w("UIDiv2ActionHandler", "Missing parameters - key: $stackKey, value: $stackValue")
                Toast.makeText(context, "Missing stack key or value", Toast.LENGTH_SHORT).show()
            }

            android.util.Log.d("UIDiv2ActionHandler", "=== END PUSH TO STACK ACTION ===")

        } else if (uri.authority == AUTHORITY_POP_FROM_STACK) {
            android.util.Log.d("UIDiv2ActionHandler", "=== POP FROM STACK ACTION ===")

            val stackKey = uri.getQueryParameter(PARAM_STACK_KEY)
            android.util.Log.d("UIDiv2ActionHandler", "Stack Key: $stackKey")

            if (stackKey != null) {
                try {
                    // Get current stack from database
                    val currentStackJson = mehdiViewModel?.getValueByKey(stackKey)?.value
                    android.util.Log.d("UIDiv2ActionHandler", "Current stack JSON: $currentStackJson")

                    if (currentStackJson != null && currentStackJson.isNotEmpty()) {
                        // Parse stack
                        val gson = Gson()
                        val stackList = try {
                            gson.fromJson(currentStackJson, Array<String>::class.java).toMutableList()
                        } catch (e: Exception) {
                            android.util.Log.e("UIDiv2ActionHandler", "Failed to parse stack", e)
                            Toast.makeText(context, "Invalid stack format", Toast.LENGTH_SHORT).show()
                            return true
                        }

                        android.util.Log.d("UIDiv2ActionHandler", "Current stack size: ${stackList.size}")

                        if (stackList.isNotEmpty()) {
                            // Pop last value from stack
                            val poppedValue = stackList.removeAt(stackList.size - 1)
                            android.util.Log.d("UIDiv2ActionHandler", "Popped value: $poppedValue")
                            android.util.Log.d("UIDiv2ActionHandler", "New stack size: ${stackList.size}")

                            // Convert back to JSON and save to database
                            val newStackJson = gson.toJson(stackList)
                            android.util.Log.d("UIDiv2ActionHandler", "New stack JSON: $newStackJson")

                            mehdiViewModel?.insertItemToDb(PhPlusDB(null, stackKey, newStackJson))
                            android.util.Log.d("UIDiv2ActionHandler", "Stack saved to database successfully")

                            Toast.makeText(context, "Removed from stack: $poppedValue", Toast.LENGTH_SHORT).show()
                        } else {
                            android.util.Log.w("UIDiv2ActionHandler", "Stack is empty, nothing to pop")
                            Toast.makeText(context, "Stack is empty", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.util.Log.w("UIDiv2ActionHandler", "Stack not found in database")
                        Toast.makeText(context, "Stack not found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("UIDiv2ActionHandler", "Error popping from stack", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                android.util.Log.w("UIDiv2ActionHandler", "Missing parameter - key: $stackKey")
                Toast.makeText(context, "Missing stack key", Toast.LENGTH_SHORT).show()
            }

            android.util.Log.d("UIDiv2ActionHandler", "=== END POP FROM STACK ACTION ===")

        } else if (uri.authority == AUTHORITY_SET_PATCH_FROM_STACK) {
            android.util.Log.d("UIDiv2ActionHandler", "=== SET PATCH FROM STACK ACTION ===")

            val stackKey = uri.getQueryParameter(PARAM_STACK_KEY)
            android.util.Log.d("UIDiv2ActionHandler", "Stack Key: $stackKey")

            if (stackKey != null) {
                try {
                    // Get current stack from database
                    val currentStackJson = mehdiViewModel?.getValueByKey(stackKey)?.value
                    android.util.Log.d("UIDiv2ActionHandler", "Current stack JSON: $currentStackJson")

                    if (currentStackJson != null && currentStackJson.isNotEmpty()) {
                        // Parse stack
                        val gson = Gson()
                        val stackList = try {
                            gson.fromJson(currentStackJson, Array<String>::class.java)
                        } catch (e: Exception) {
                            android.util.Log.e("UIDiv2ActionHandler", "Failed to parse stack", e)
                            Toast.makeText(context, "Invalid stack format", Toast.LENGTH_SHORT).show()
                            return true
                        }

                        android.util.Log.d("UIDiv2ActionHandler", "Stack size: ${stackList.size}")

                        if (stackList.isNotEmpty()) {
                            // Get last value from stack
                            val lastValue = stackList[stackList.size - 1]
                            android.util.Log.d("UIDiv2ActionHandler", "Last value in stack: $lastValue")

                            // Get patch JSON from database using the last value
                            val patchJson = mehdiViewModel?.getValueByKey(lastValue)?.value
                            android.util.Log.d("UIDiv2ActionHandler", "Patch JSON: $patchJson")

                            if (patchJson != null && patchJson.isNotEmpty()) {
                                // Apply patch using loadScreenListener
                                if(!lastValue.contains("patch_1_")){
                                android.util.Log.d("UIDiv2ActionHandler", "Applying patch: $lastValue")
                                loadScreenListener.onApplyOnbase(patchJson, lastValue, "", "")
                                android.util.Log.d("UIDiv2ActionHandler", "Patch applied successfully")
                                Toast.makeText(context, "Applied patch: $lastValue", Toast.LENGTH_SHORT).show()}
                            } else {
                                android.util.Log.w("UIDiv2ActionHandler", "Patch not found for key: $lastValue")
                                Toast.makeText(context, "Patch not found: $lastValue", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            android.util.Log.w("UIDiv2ActionHandler", "Stack is empty")
                            Toast.makeText(context, "Stack is empty", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.util.Log.w("UIDiv2ActionHandler", "Stack not found in database")
                        Toast.makeText(context, "Stack not found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("UIDiv2ActionHandler", "Error setting patch from stack", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                android.util.Log.w("UIDiv2ActionHandler", "Missing parameter - key: $stackKey")
                Toast.makeText(context, "Missing stack key", Toast.LENGTH_SHORT).show()
            }

            android.util.Log.d("UIDiv2ActionHandler", "=== END SET PATCH FROM STACK ACTION ===")

        } else if (uri.authority == AUTHORITY_PUSH_AND_SET_PATCH) {
            android.util.Log.d("UIDiv2ActionHandler", "=== PUSH AND SET PATCH ACTION (ATOMIC) ===")

            val stackKey = uri.getQueryParameter(PARAM_STACK_KEY)
            val stackValue = uri.getQueryParameter(PARAM_STACK_VALUE)

            android.util.Log.d("UIDiv2ActionHandler", "Stack Key: $stackKey")
            android.util.Log.d("UIDiv2ActionHandler", "Stack Value (page to push): $stackValue")

            if (stackKey != null && stackValue != null) {
                try {
                    // STEP 1: Push to stack
                    android.util.Log.d("UIDiv2ActionHandler", "STEP 1: Pushing to stack...")

                    val currentStackJson = mehdiViewModel?.getValueByKey(stackKey)?.value
                    android.util.Log.d("UIDiv2ActionHandler", "Current stack JSON: $currentStackJson")

                    val stackList = if (currentStackJson != null && currentStackJson.isNotEmpty()) {
                        try {
                            val gson = Gson()
                            gson.fromJson(currentStackJson, Array<String>::class.java).toMutableList()
                        } catch (e: Exception) {
                            android.util.Log.w("UIDiv2ActionHandler", "Failed to parse stack, creating new: ${e.message}")
                            mutableListOf<String>()
                        }
                    } else {
                        mutableListOf<String>()
                    }

                    android.util.Log.d("UIDiv2ActionHandler", "Stack size before push: ${stackList.size}")
                    stackList.add(stackValue)
                    android.util.Log.d("UIDiv2ActionHandler", "Stack size after push: ${stackList.size}")

                    val gson = Gson()
                    val newStackJson = gson.toJson(stackList)
                    android.util.Log.d("UIDiv2ActionHandler", "New stack JSON: $newStackJson")

                    mehdiViewModel?.insertItemToDb(PhPlusDB(null, stackKey, newStackJson))
                    android.util.Log.d("UIDiv2ActionHandler", "Stack saved to database")

                    // STEP 2: Set patch (using the value we just pushed)
                    android.util.Log.d("UIDiv2ActionHandler", "STEP 2: Setting patch for: $stackValue")

                    val patchJson = mehdiViewModel?.getValueByKey(stackValue)?.value
                    android.util.Log.d("UIDiv2ActionHandler", "Patch JSON retrieved: ${patchJson != null}")

                    if (patchJson != null && patchJson.isNotEmpty()) {
                        android.util.Log.d("UIDiv2ActionHandler", "Applying patch: $stackValue")
                        loadScreenListener.onApplyOnbase(patchJson, stackValue, "", "")
                        android.util.Log.d("UIDiv2ActionHandler", "Patch applied successfully")

                        Toast.makeText(context, "Navigated to: $stackValue", Toast.LENGTH_SHORT).show()
                    } else {
                        android.util.Log.w("UIDiv2ActionHandler", "Patch not found for: $stackValue")
                        Toast.makeText(context, "Patch not found: $stackValue", Toast.LENGTH_SHORT).show()
                    }

                    android.util.Log.d("UIDiv2ActionHandler", "ATOMIC operation completed successfully")

                } catch (e: Exception) {
                    android.util.Log.e("UIDiv2ActionHandler", "Error in push_and_set_patch", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                android.util.Log.w("UIDiv2ActionHandler", "Missing parameters - key: $stackKey, value: $stackValue")
                Toast.makeText(context, "Missing stack key or value", Toast.LENGTH_SHORT).show()
            }

            android.util.Log.d("UIDiv2ActionHandler", "=== END PUSH AND SET PATCH ACTION ===")

        } else if (uri.authority == AUTHORITY_POP_AND_SET_PATCH) {
            android.util.Log.d("UIDiv2ActionHandler", "=== POP AND SET PATCH ACTION (ATOMIC) ===")

            val stackKey = uri.getQueryParameter(PARAM_STACK_KEY)
            android.util.Log.d("UIDiv2ActionHandler", "Stack Key: $stackKey")

            if (stackKey != null) {
                try {
                    // Get current stack from database
                    val currentStackJson = mehdiViewModel?.getValueByKey(stackKey)?.value
                    android.util.Log.d("UIDiv2ActionHandler", "Current stack JSON: $currentStackJson")

                    if (currentStackJson != null && currentStackJson.isNotEmpty()) {
                        val gson = Gson()
                        val stackList = try {
                            gson.fromJson(currentStackJson, Array<String>::class.java).toMutableList()
                        } catch (e: Exception) {
                            android.util.Log.e("UIDiv2ActionHandler", "Failed to parse stack", e)
                            Toast.makeText(context, "Invalid stack format", Toast.LENGTH_SHORT).show()
                            return true
                        }

                        android.util.Log.d("UIDiv2ActionHandler", "Stack size before pop: ${stackList.size}")

                        if (stackList.size >= 2) {
                            // STEP 1: Pop current page from stack
                            android.util.Log.d("UIDiv2ActionHandler", "STEP 1: Popping from stack...")
                            val poppedValue = stackList.removeAt(stackList.size - 1)
                            android.util.Log.d("UIDiv2ActionHandler", "Popped value: $poppedValue")
                            android.util.Log.d("UIDiv2ActionHandler", "Stack size after pop: ${stackList.size}")

                            // Save updated stack
                            val newStackJson = gson.toJson(stackList)
                            android.util.Log.d("UIDiv2ActionHandler", "New stack JSON: $newStackJson")
                            mehdiViewModel?.insertItemToDb(PhPlusDB(null, stackKey, newStackJson))
                            android.util.Log.d("UIDiv2ActionHandler", "Stack saved to database")

                            // STEP 2: Set patch for the new last item
                            val newLastValue = stackList[stackList.size - 1]
                            android.util.Log.d("UIDiv2ActionHandler", "STEP 2: Setting patch for previous page: $newLastValue")

                            val patchJson = mehdiViewModel?.getValueByKey(newLastValue)?.value
                            android.util.Log.d("UIDiv2ActionHandler", "Patch JSON retrieved: ${patchJson != null}")

                            if (patchJson != null && patchJson.isNotEmpty()) {
                                android.util.Log.d("UIDiv2ActionHandler", "Applying patch: $newLastValue")
                                loadScreenListener.onApplyOnbase(patchJson, newLastValue, "", "")
                                android.util.Log.d("UIDiv2ActionHandler", "Patch applied successfully")

                                Toast.makeText(context, "Back to: $newLastValue", Toast.LENGTH_SHORT).show()
                            } else {
                                android.util.Log.w("UIDiv2ActionHandler", "Patch not found for: $newLastValue")
                                Toast.makeText(context, "Patch not found: $newLastValue", Toast.LENGTH_SHORT).show()
                            }

                            android.util.Log.d("UIDiv2ActionHandler", "ATOMIC operation completed successfully")

                        } else if (stackList.size == 1) {
                            android.util.Log.w("UIDiv2ActionHandler", "Stack has only one item, cannot go back further")
                            Toast.makeText(context, "Already at first page", Toast.LENGTH_SHORT).show()
                        } else {
                            android.util.Log.w("UIDiv2ActionHandler", "Stack is empty")
                            Toast.makeText(context, "Stack is empty", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.util.Log.w("UIDiv2ActionHandler", "Stack not found in database")
                        Toast.makeText(context, "Stack not found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("UIDiv2ActionHandler", "Error in pop_and_set_patch", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                android.util.Log.w("UIDiv2ActionHandler", "Missing parameter - key: $stackKey")
                Toast.makeText(context, "Missing stack key", Toast.LENGTH_SHORT).show()
            }

            android.util.Log.d("UIDiv2ActionHandler", "=== END POP AND SET PATCH ACTION ===")

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

    private fun openKashfFace() {
        val intent = Intent(context, ir.nrdc.kashfface.ui.home.HomeActivity::class.java)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private data class GuesthouseDaySlot(
        val dayId: String,
        val date: String,
        val reserveId: String,
    )

    private data class PendingGuesthouseDayOp(
        val op: String, // reserve | delete
        val guesthouseId: String,
        val dayId: String,
        val date: String,
        val reserveId: String = "",
    )

    private fun clearGuesthouseDaySelection(div2View: Div2View) {
        setVariableIfPresent(div2View, "", "selected_day1_id", "selected_day1_date", "selected_day1_reserve_id")
        setVariableIfPresent(div2View, "", "selected_day2_id", "selected_day2_date", "selected_day2_reserve_id")
        setVariableIfPresent(div2View, "", "selected_day3_id", "selected_day3_date", "selected_day3_reserve_id")
        setVariableIfPresent(div2View, "", "selected_window_label")
    }

    private fun readGuesthouseDaySlots(div2View: Div2View): MutableList<GuesthouseDaySlot> {
        val slots = mutableListOf<GuesthouseDaySlot>()
        fun read(i: Int) {
            val id = getDivVariableValue(div2View, "selected_day${i}_id")?.toString().orEmpty()
            val date = getDivVariableValue(div2View, "selected_day${i}_date")?.toString().orEmpty()
            val reserveId =
                getDivVariableValue(div2View, "selected_day${i}_reserve_id")?.toString().orEmpty()
            if (id.isNotBlank() && date.isNotBlank()) {
                slots.add(GuesthouseDaySlot(id, date, reserveId))
            }
        }
        read(1)
        read(2)
        read(3)
        slots.sortBy { it.date }
        return slots
    }

    /**
     * Day tap: reserve (select only after server returns id) or delete reserve (deselect after response).
     * Consecutive validation happens before calling reserve.
     */
    private fun handleSelectGuesthouseStartDay(uri: Uri) {
        val div2View = view as? Div2View ?: return
        if (pendingGuesthouseDayOp != null) {
            Toast.makeText(context, "لطفاً صبر کنید…", Toast.LENGTH_SHORT).show()
            return
        }
        val guesthouseId = uri.getQueryParameter("guesthouse_id")
            ?: getDivVariableValue(div2View, "selected_guesthouse_id")?.toString().orEmpty()
        val dayId = uri.getQueryParameter("day_id").orEmpty()
        val tappedDate = Uri.decode(uri.getQueryParameter("date").orEmpty())
        if (guesthouseId.isBlank() || dayId.isBlank()) {
            Toast.makeText(context, "روز نامعتبر است", Toast.LENGTH_SHORT).show()
            return
        }
        val placesRaw = getDivVariableValue(div2View, "guesthouse_places")
            ?: Div2ViewVariableExporter.captureVariablesMap(div2View)["guesthouse_places"]
        val places = anyToJsonArray(placesRaw)
        var daysArray: JSONArray? = null
        for (i in 0 until places.length()) {
            val place = places.optJSONObject(i) ?: continue
            if (place.optString("id") == guesthouseId) {
                daysArray = place.optJSONArray("calendarPlanDivisionDetail")
                break
            }
        }
        if (daysArray == null || daysArray.length() == 0) {
            Toast.makeText(context, "تقویم این مهمانسرا خالی است", Toast.LENGTH_SHORT).show()
            return
        }
        val catalog = mutableListOf<JSONObject>()
        for (i in 0 until daysArray.length()) {
            daysArray.optJSONObject(i)?.let { catalog.add(it) }
        }
        val tapped = catalog.firstOrNull { it.optString("id") == dayId }
        if (tapped == null) {
            Toast.makeText(context, "روز انتخاب‌شده پیدا نشد", Toast.LENGTH_SHORT).show()
            return
        }
        val date = tapped.optString("date").ifBlank { tappedDate }
        val current = readGuesthouseDaySlots(div2View)
        val existing = current.firstOrNull { it.dayId == dayId }

        if (existing != null) {
            if (existing.reserveId.isBlank()) {
                Toast.makeText(context, "شناسه رزرو موجود نیست", Toast.LENGTH_SHORT).show()
                return
            }
            pendingGuesthouseHandler = WeakReference(this)
            pendingGuesthouseDayOp = PendingGuesthouseDayOp(
                op = "delete",
                guesthouseId = guesthouseId,
                dayId = dayId,
                date = date,
                reserveId = existing.reserveId,
            )
            val map = HashMap<String, String>()
            map["path"] = "guestHouse/deleteReserveOneDayPersonnel"
            map["sysName"] = "guestHouse"
            map["ph/token"] = "empty"
            map["planGuestHouseDivisionId"] = dayId
            map["planGuestHouseDivisionJunctionId"] = existing.reserveId
            loadScreenListener.onRequest(map)
            return
        }

        if (current.size >= 3) {
            Toast.makeText(context, "حداکثر ۳ روز می‌توانید انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }
        if (current.isNotEmpty()) {
            val minDate = current.first().date
            val maxDate = current.last().date
            val extendsBefore = nextPersianDay(date) == minDate
            val extendsAfter = nextPersianDay(maxDate) == date
            if (!extendsBefore && !extendsAfter) {
                Toast.makeText(
                    context,
                    "فقط روزهای پشت‌سرهم قابل انتخاب هستند",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val trial = current.map { it.date }.toMutableList().also { it.add(date) }
            if (!areConsecutivePersianDates(trial)) {
                Toast.makeText(
                    context,
                    "فقط روزهای پشت‌سرهم قابل انتخاب هستند",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        pendingGuesthouseHandler = WeakReference(this)
        pendingGuesthouseDayOp = PendingGuesthouseDayOp(
            op = "reserve",
            guesthouseId = guesthouseId,
            dayId = dayId,
            date = date,
        )
        val map = HashMap<String, String>()
        map["path"] = "guestHouse/reserveOneDayPersonnel"
        map["sysName"] = "guestHouse"
        map["ph/token"] = "empty"
        map["id"] = dayId
        loadScreenListener.onRequest(map)
    }

    private fun applyGuesthouseDaySelection(
        div2View: Div2View,
        selected: List<GuesthouseDaySlot>,
    ) {
        val items = selected.sortedBy { it.date }
        clearGuesthouseDaySelection(div2View)
        if (items.isEmpty()) return
        setVariableIfPresent(div2View, items[0].dayId, "selected_day1_id")
        setVariableIfPresent(div2View, items[0].date, "selected_day1_date")
        setVariableIfPresent(div2View, items[0].reserveId, "selected_day1_reserve_id")
        if (items.size >= 2) {
            setVariableIfPresent(div2View, items[1].dayId, "selected_day2_id")
            setVariableIfPresent(div2View, items[1].date, "selected_day2_date")
            setVariableIfPresent(div2View, items[1].reserveId, "selected_day2_reserve_id")
        }
        if (items.size >= 3) {
            setVariableIfPresent(div2View, items[2].dayId, "selected_day3_id")
            setVariableIfPresent(div2View, items[2].date, "selected_day3_date")
            setVariableIfPresent(div2View, items[2].reserveId, "selected_day3_reserve_id")
        }
        setVariableIfPresent(
            div2View,
            items.joinToString("  ←  ") { it.date },
            "selected_window_label"
        )
    }

    private fun sameGuesthouseId(left: String, right: String): Boolean {
        if (left == right) return true
        val l = left.toLongOrNull()
        val r = right.toLongOrNull()
        return l != null && r != null && l == r
    }

    private fun updateGuesthouseDayCapacity(
        div2View: Div2View,
        guesthouseId: String,
        dayId: String,
        capacity: String,
    ) {
        if (capacity.isBlank()) return
        val placesRaw = getDivVariableValue(div2View, "guesthouse_places")
            ?: Div2ViewVariableExporter.captureVariablesMap(div2View)["guesthouse_places"]
            ?: return
        val places = anyToJsonArray(placesRaw)
        // DivKit ArrayVariable ignores set() when the same JSONArray instance is passed.
        // Always build a new deep-copied array so UI (item_builder) refreshes.
        val updatedPlaces = JSONArray()
        var changed = false
        for (i in 0 until places.length()) {
            val place = places.optJSONObject(i) ?: continue
            val placeCopy = JSONObject(place.toString())
            if (sameGuesthouseId(placeCopy.optString("id"), guesthouseId)) {
                val days = placeCopy.optJSONArray("calendarPlanDivisionDetail")
                if (days != null) {
                    val updatedDays = JSONArray()
                    for (j in 0 until days.length()) {
                        val day = days.optJSONObject(j) ?: continue
                        val dayCopy = JSONObject(day.toString())
                        if (sameGuesthouseId(dayCopy.optString("id"), dayId)) {
                            dayCopy.put("capacity", capacity)
                            changed = true
                        }
                        updatedDays.put(dayCopy)
                    }
                    placeCopy.put("calendarPlanDivisionDetail", updatedDays)
                }
            }
            updatedPlaces.put(placeCopy)
        }
        if (changed) {
            setDivArrayVariable(div2View, "guesthouse_places", updatedPlaces)
            Log.d(
                "UIDiv2ActionHandler",
                "guesthouse capacity updated: place=$guesthouseId day=$dayId capacity=$capacity"
            )
        } else {
            Log.w(
                "UIDiv2ActionHandler",
                "guesthouse capacity update skipped: place=$guesthouseId day=$dayId not found"
            )
        }
    }

    private fun setDivArrayVariable(div2View: Div2View, name: String, array: JSONArray) {
        val apply = {
            runCatching {
                val variable = getMutableDivVariable(div2View, name) as? Variable.ArrayVariable
                if (variable != null) {
                    // New instance required for notifyVariableChanged
                    variable.set(JSONArray(array.toString()))
                } else {
                    Log.e("UIDiv2ActionHandler", "Array variable not found: $name")
                }
            }.onFailure {
                Log.e("UIDiv2ActionHandler", "setDivArrayVariable($name) failed", it)
            }
        }
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            apply()
        } else {
            div2View.post { apply() }
        }
    }

    /**
     * Append a dict item into a DivKit array variable.
     * Example:
     * div-action://append_to_array?array=motor_plates_list&id=123-45&plateC=123&plateD=45&model=...
     * Optional: unique_key=id (default) to skip duplicates; count_var=motor_plates_count
     */
    private fun handleAppendToArray(uri: Uri, view: DivViewFacade) {
        val div2View = view as? Div2View ?: return
        val arrayName = uri.getQueryParameter("array").orEmpty()
        if (arrayName.isBlank()) return

        val uniqueKey = uri.getQueryParameter("unique_key") ?: "id"
        val countVar = uri.getQueryParameter("count_var").orEmpty()
        val item = JSONObject()
        uri.queryParameterNames
            .filter { it != "array" && it != "unique_key" && it != "count_var" }
            .forEach { key ->
                item.put(key, uri.getQueryParameter(key).orEmpty())
            }
        if (!item.has("id") || item.optString("id").isBlank()) {
            item.put("id", System.currentTimeMillis().toString())
        }

        val raw = getDivVariableValue(div2View, arrayName)
            ?: Div2ViewVariableExporter.captureVariablesMap(div2View)[arrayName]
        val current = anyToJsonArray(raw)
        val uniqueValue = item.optString(uniqueKey)
        if (uniqueValue.isNotBlank()) {
            for (i in 0 until current.length()) {
                val existing = current.optJSONObject(i) ?: continue
                if (existing.optString(uniqueKey) == uniqueValue) {
                    Toast.makeText(context, "این پلاک قبلاً به لیست اضافه شده است", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }
        val updated = JSONArray(current.toString())
        updated.put(item)
        setDivArrayVariable(div2View, arrayName, updated)
        if (countVar.isNotBlank()) {
            runCatching { div2View.setVariable(countVar, updated.length().toString()) }
        }
        android.util.Log.d(
            "UIDiv2ActionHandler",
            "append_to_array $arrayName size=${updated.length()} id=${item.optString("id")}",
        )
        Toast.makeText(context, "به لیست اضافه شد", Toast.LENGTH_SHORT).show()
    }

    /**
     * Remove a dict item from a DivKit array variable by matching [match_key]=[match_value].
     * Example: div-action://remove_from_array?array=motor_plates_list&match_key=id&match_value=123-45
     */
    private fun handleRemoveFromArray(uri: Uri, view: DivViewFacade) {
        val div2View = view as? Div2View ?: return
        val arrayName = uri.getQueryParameter("array").orEmpty()
        val matchKey = uri.getQueryParameter("match_key") ?: "id"
        val matchValue = uri.getQueryParameter("match_value")
            ?: uri.getQueryParameter("id").orEmpty()
        val countVar = uri.getQueryParameter("count_var").orEmpty()
        if (arrayName.isBlank() || matchValue.isBlank()) return

        val raw = getDivVariableValue(div2View, arrayName)
            ?: Div2ViewVariableExporter.captureVariablesMap(div2View)[arrayName]
        val current = anyToJsonArray(raw)
        val updated = JSONArray()
        var removed = false
        for (i in 0 until current.length()) {
            val obj = current.optJSONObject(i) ?: continue
            if (!removed && obj.optString(matchKey) == matchValue) {
                removed = true
                continue
            }
            updated.put(obj)
        }
        setDivArrayVariable(div2View, arrayName, updated)
        if (countVar.isNotBlank()) {
            runCatching { div2View.setVariable(countVar, updated.length().toString()) }
        }
        android.util.Log.d(
            "UIDiv2ActionHandler",
            "remove_from_array $arrayName size=${updated.length()} removed=$removed",
        )
    }

    private fun getMutableDivVariable(div2View: Div2View, name: String): Variable? {
        return runCatching {
            val runtimeField = Div2View::class.java.getDeclaredField("expressionsRuntime")
            runtimeField.isAccessible = true
            val runtime = runtimeField.get(div2View) ?: return null
            val vcField = runtime.javaClass.getDeclaredField("variableController")
            vcField.isAccessible = true
            val controller = vcField.get(runtime) ?: return null
            val getter = controller.javaClass.methods.firstOrNull {
                it.name == "getMutableVariable" && it.parameterTypes.size == 1
            } ?: return null
            getter.invoke(controller, name) as? Variable
        }.getOrNull()
    }

    private fun onGuesthouseDayServiceResponse(response: Map<String, String>) {
        val op = pendingGuesthouseDayOp ?: return
        val div2View = view as? Div2View ?: run {
            pendingGuesthouseDayOp = null
            return
        }
        pendingGuesthouseDayOp = null
        val capacity = response["capacity"].orEmpty().ifBlank {
            response.entries.firstOrNull { it.key.equals("capacity", ignoreCase = true) }
                ?.value.orEmpty()
        }
        if (capacity.isNotBlank()) {
            updateGuesthouseDayCapacity(div2View, op.guesthouseId, op.dayId, capacity)
        } else {
            Log.w("UIDiv2ActionHandler", "guesthouse response missing capacity: $response")
        }
        when (op.op) {
            "reserve" -> {
                val reserveId = response["id"].orEmpty()
                if (reserveId.isBlank()) {
                    Toast.makeText(context, "رزرو انجام نشد", Toast.LENGTH_SHORT).show()
                    return
                }
                val current = readGuesthouseDaySlots(div2View)
                if (current.any { it.dayId == op.dayId }) return
                if (current.size >= 3) return
                current.add(GuesthouseDaySlot(op.dayId, op.date, reserveId))
                applyGuesthouseDaySelection(div2View, current)
            }
            "delete" -> {
                val current = readGuesthouseDaySlots(div2View)
                    .filterNot { it.dayId == op.dayId }
                    .toMutableList()
                applyGuesthouseDaySelection(div2View, current)
            }
        }
    }

    private fun areConsecutivePersianDates(dates: List<String>): Boolean {
        if (dates.size <= 1) return true
        val sorted = dates.sorted()
        for (i in 0 until sorted.lastIndex) {
            if (nextPersianDay(sorted[i]) != sorted[i + 1]) return false
        }
        return true
    }

    private fun nextPersianDay(date: String): String? {
        val parts = date.split("/")
        if (parts.size != 3) return null
        var y = parts[0].toIntOrNull() ?: return null
        var m = parts[1].toIntOrNull() ?: return null
        var d = parts[2].toIntOrNull() ?: return null
        val monthLen = when (m) {
            in 1..6 -> 31
            in 7..11 -> 30
            12 -> if (isPersianLeapYear(y)) 30 else 29
            else -> return null
        }
        d += 1
        if (d > monthLen) {
            d = 1
            m += 1
            if (m > 12) {
                m = 1
                y += 1
            }
        }
        return String.format(Locale.US, "%04d/%02d/%02d", y, m, d)
    }

    private fun isPersianLeapYear(year: Int): Boolean {
        // Common Jalali leap cycle approximation used in many Iranian apps.
        val breaks = intArrayOf(
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
            1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
        )
        var jp = breaks[0]
        var jump = 0
        for (i in 1 until breaks.size) {
            val jm = breaks[i]
            jump = jm - jp
            if (year < jm) break
            jp = jm
        }
        var n = year - jp
        if (n < jump) {
            if (jump - n < 6) n = n - jump + ((jump + 4) / 33) * 33
            var leap = ((n + 1) % 33) % 4
            if (leap == -1) leap = 4
            return leap == 1
        }
        return false
    }

    /**
     * Plate camera with on-screen choice: local AI OCR or send crop to server (ocrPicture).
     *
     * URL: div-action://open_kashfface_plate?mode=local
     * mode: local | server  (default selection on camera UI)
     */
    private fun openKashfFacePlate(defaultMode: String?) {
        val host = context as? Activity
        if (host == null || host.isFinishing) {
            Toast.makeText(context, "خطا در باز کردن دوربین پلاک", Toast.LENGTH_SHORT).show()
            return
        }
        pendingKashfPlateHandler = WeakReference(this)
        val mode = if (PlateCapture.MODE_SERVER.equals(defaultMode, ignoreCase = true)) {
            PlateCapture.MODE_SERVER
        } else {
            PlateCapture.MODE_LOCAL
        }
        val config = PlateCaptureConfig.defaults()
            .setShowModeChoice(true)
            .setDefaultMode(mode)
        host.startActivityForResult(
            PlateCapture.createIntent(host, config),
            REQUEST_KASHFFACE_PLATE
        )
    }

    private fun applyLocalPlateToDiv(data: Intent) {
        val div2View = if (view is Div2View) view as Div2View? else null
        val ocr = data.getStringExtra(PlateCapture.EXTRA_OCR_TEXT).orEmpty()
        val motorcycle = data.getBooleanExtra(PlateCapture.EXTRA_MOTORCYCLE, false)
        val twoRow = data.getBooleanExtra(PlateCapture.EXTRA_TWO_ROW, false)
        val read: PlateRead = PlateParser.parse(ocr, motorcycle, twoRow || motorcycle)
        if (read.motorcycle) {
            setVariableIfPresent(div2View, "true", "motor", "variable_motor")
            setVariableIfPresent(
                div2View,
                read.motorcycleTop,
                "variable_plateC_motor",
                "plateC_motor"
            )
            setVariableIfPresent(
                div2View,
                read.motorcycleBottom,
                "variable_plateD_motor",
                "plateD_motor"
            )
        } else {
            setVariableIfPresent(div2View, "false", "motor", "variable_motor")
            setVariableIfPresent(div2View, read.two, "variable_plateA", "plateA")
            setVariableIfPresent(div2View, read.letter, "variable_plateB", "plateB", "plate")
            setVariableIfPresent(div2View, read.three, "variable_plateC", "plateC")
            setVariableIfPresent(div2View, read.city, "variable_plateD", "plateD")
        }
        if (read.two.isEmpty() && read.motorcycleTop.isEmpty() && read.serial5.isEmpty()) {
            Toast.makeText(context, "پلاک خوانده نشد", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "پلاک خوانده شد", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendPlateImageToServerOcr(data: Intent) {
        val path = data.getStringExtra(PlateCapture.EXTRA_IMAGE_PATH)
            ?: data.getStringExtra(PlateCapture.EXTRA_FULL_IMAGE_PATH)
        if (path.isNullOrBlank()) {
            Toast.makeText(context, "عکس پلاک موجود نیست", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val bytes = File(path).readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val map = HashMap<String, String>()
            map["path"] = "ocrPicture"
            map["base64"] = base64
            map["ph/token"] = "empty"
            loadScreenListener.onRequest(map)
        } catch (e: Exception) {
            Log.e("UIDiv2ActionHandler", "KashfFace plate server OCR failed", e)
            Toast.makeText(context, "خطا در ارسال عکس پلاک", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Same post-capture flow as [AUTHORITY_OPEN_CAMERA], but captures with KashfFace
     * face-quality camera ([FaceCapture]) instead of Naji camera.
     *
     * URL: div-action://open_kashfface_camera?type=person&iranian=true&front=false
     */
    private fun openKashfFaceCamera(
        type: String?,
        checkOcr: String?,
        iranian: String?,
        frontCamera: String?
    ) {
        val host = context as? Activity
        if (host == null || host.isFinishing) {
            Toast.makeText(context, "خطا در باز کردن دوربین چهره", Toast.LENGTH_SHORT).show()
            return
        }
        pendingKashfFaceHandler = WeakReference(this)
        pendingKashfFaceType = type ?: "person"
        pendingKashfFaceCheckOcr = checkOcr
        pendingKashfFaceIranian = iranian
        val useFront = frontCamera.equals("true", ignoreCase = true) ||
            frontCamera == "1"
        val config = FaceCaptureConfig.defaults().setUseFrontCamera(useFront)
        host.startActivityForResult(
            FaceCapture.createIntent(host, config),
            REQUEST_KASHFFACE_CAMERA
        )
    }

    companion object {
        const val REQUEST_KASHFFACE_CAMERA = 9101
        const val REQUEST_KASHFFACE_PLATE = 9102

        @Transient
        private var pendingKashfFaceHandler: WeakReference<UIDiv2ActionHandler>? = null
        private var pendingKashfFaceType: String? = null
        private var pendingKashfFaceCheckOcr: String? = null
        private var pendingKashfFaceIranian: String? = null

        @Transient
        private var pendingKashfPlateHandler: WeakReference<UIDiv2ActionHandler>? = null

        @Transient
        private var pendingGuesthouseHandler: WeakReference<UIDiv2ActionHandler>? = null
        private var pendingGuesthouseDayOp: PendingGuesthouseDayOp? = null

        /**
         * Handles guestHouse reserve/delete day responses.
         * Returns true when a pending guesthouse day op was consumed.
         */
        @JvmStatic
        fun handleGuesthouseServiceResponse(response: Map<String, String>): Boolean {
            val handler = pendingGuesthouseHandler?.get()
            val pending = pendingGuesthouseDayOp
            if (handler == null || pending == null) {
                return false
            }
            handler.onGuesthouseDayServiceResponse(response)
            pendingGuesthouseHandler = null
            return true
        }

        @JvmStatic
        fun clearPendingGuesthouseDayOp() {
            pendingGuesthouseDayOp = null
            pendingGuesthouseHandler = null
        }

        /**
         * Called from [MehdiActivity.onActivityResult] after FaceCapture returns.
         * Forwards the captured face image into the same callback path as open_camera.
         */
        @JvmStatic
        fun handleKashfFaceCameraResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
        ): Boolean {
            if (requestCode != REQUEST_KASHFFACE_CAMERA) {
                return false
            }
            val handler = pendingKashfFaceHandler?.get()
            val type = pendingKashfFaceType
            val checkOcr = pendingKashfFaceCheckOcr
            val iranian = pendingKashfFaceIranian
            pendingKashfFaceHandler = null
            pendingKashfFaceType = null
            pendingKashfFaceCheckOcr = null
            pendingKashfFaceIranian = null

            if (resultCode != Activity.RESULT_OK || data == null || handler == null) {
                return true
            }
            val path = data.getStringExtra(FaceCapture.EXTRA_IMAGE_PATH)
            if (path.isNullOrBlank()) {
                return true
            }
            try {
                val bytes = File(path).readBytes()
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                handler.getImageMainAndCropped(base64, base64, type, checkOcr, iranian)
            } catch (e: Exception) {
                Log.e("UIDiv2ActionHandler", "KashfFace camera result failed", e)
                Toast.makeText(
                    handler.context,
                    "خطا در پردازش عکس چهره",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return true
        }

        @JvmStatic
        fun handleKashfFacePlateResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
        ): Boolean {
            if (requestCode != REQUEST_KASHFFACE_PLATE) {
                return false
            }
            val handler = pendingKashfPlateHandler?.get()
            pendingKashfPlateHandler = null
            if (resultCode != Activity.RESULT_OK || data == null || handler == null) {
                return true
            }
            val mode = data.getStringExtra(PlateCapture.EXTRA_RESULT_MODE)
            if (PlateCapture.MODE_SERVER.equals(mode, ignoreCase = true)) {
                handler.sendPlateImageToServerOcr(data)
            } else {
                handler.applyLocalPlateToDiv(data)
            }
            return true
        }
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
        // Accidents screens use variable_* names; older screens still use unprefixed names.
        setVariableIfPresent(div2View, plate, "variable_plateB", "plate")
        setVariableIfPresent(
            div2View,
            color,
            "variable_plate1_color", "plate1_color",
            "variable_plate2_color", "plate2_color",
            "variable_plate3_color", "plate3_color",
            "variable_plate4_color", "plate4_color",
            "variable_plate_color", "plate_color",
        )
        setVariableIfPresent(
            div2View,
            textColor,
            "variable_plate_text_color", "plate_text_color",
        )
    }

    private fun setVariableIfPresent(div2View: Div2View?, value: String, vararg names: String) {
        if (div2View == null) return
        for (name in names) {
            try {
                div2View.setVariable(name, value)
            } catch (_: VariableMutationException) {
                // Variable may not exist on the current screen.
            }
        }
    }

    override fun onClicked(
        programs: ListItemDto,
        varName: String,
        titleVar: String,
        codeVar: String,
        sysName: String,
        path: String,
        allParams: Map<String, String>
    ) {
        if (path != "") {
            val map: java.util.HashMap<String, String> = HashMap()

            // پارامترهای اصلی
            map["path"] = path
            map["sysName"] = sysName
            map["selectedId"] = programs.id
            map["ph/token"] = "empty"

            // فیلتر پارامترهای داخلی BottomSheet و اضافه کردن بقیه پارامترها
            val internalParams = setOf(
                "id", "title", "code_var", "title_var", "varName",
                "sysName", "path", "selection", "selected_codes",
                "sync_from", "sync_fields", "sync_match", "sync_nested",
                "SEARCHABLE", "searchable",
                "service_path", "response_key", "id_field", "name_field",
                "search_param", "page_param", "paged", "has_more_field"
            )

            // اضافه کردن تمام پارامترهای اضافی (مثل arrestType, bstArrestCauseType و غیره)
            allParams.forEach { (key, value) ->
                if (!internalParams.contains(key)) {
                    map[key] = value
                    Log.d("UIDiv2ActionHandler", "Adding extra param: $key = $value")
                }
            }

            Log.d("UIDiv2ActionHandler", "Sending to server: $map")
            loadScreenListener.onRequest(map)

        }
        btmSheet_list.dismiss()
        val div2View = if (view is Div2View) view as Div2View? else null
        div2View?.setVariable(codeVar, programs.id)
        div2View?.setVariable(titleVar, programs.titleFa)
        applyBottomSheetSyncFields(div2View, programs.id, allParams)
    }

    private fun applyBottomSheetSyncFields(
        div2View: Div2View?,
        selectedId: String,
        allParams: Map<String, String>,
    ) {
        if (div2View == null) return
        val syncFrom = allParams["sync_from"]?.takeIf { it.isNotBlank() } ?: return
        val fields = allParams["sync_fields"]?.takeIf { it.isNotBlank() } ?: return
        val matchPath = allParams["sync_match"]?.takeIf { it.isNotBlank() } ?: "id"
        val nested = allParams["sync_nested"]?.takeIf { it.isNotBlank() }
        val raw = getDivVariableValue(div2View, syncFrom)
            ?: Div2ViewVariableExporter.captureVariablesMap(div2View)[syncFrom]
        val item = ArrayListDbHelper.findInArray(raw, matchPath, selectedId, nested) ?: return
        ArrayListDbHelper.parseFieldMaps(fields).forEach { (sourcePath, targetVar, format) ->
            val value = ArrayListDbHelper.formatValue(
                ArrayListDbHelper.pathString(item, sourcePath),
                format,
            )
            runCatching { div2View.setVariable(targetVar, value) }
                .onFailure {
                    android.util.Log.w(
                        "UIDiv2ActionHandler",
                        "sync_fields set $targetVar failed",
                        it
                    )
                }
        }
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
        if (type == "atba") {
            val map: java.util.HashMap<String, String> = HashMap()
            if (cleanedBase64String != null) {
                map.put("path", "registerPictureAtba")
                map.put("personalImage", cleanedBase64String)
                map.put("ph/token", "empty")
                map.put("selected_border_code_atba", "empty")
                map.put("selected_border_title_atba", "empty")
                map.put("sysName", "atba")


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
                        formatPersianDate(
                            persianPickerDate.getPersianYear(),
                            persianPickerDate.getPersianMonth(),
                            persianPickerDate.getPersianDay()
                        ),
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

    /**
     * Processes nested variable expressions like @{variable_name1@{variable_name2}}
     * Similar to the implementation in DemoCustomContainerAdapter
     */
    private fun processNestedVariableExpressions(
        inputString: String,
        expressionResolver: ExpressionResolver
    ): String {
        com.yandex.div.internal.Log.d(
            "UIDiv2ActionHandler",
            "=== processNestedVariableExpressions called ==="
        )
        com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "Input string: $inputString")

        if (!inputString.contains("@{")) {
            com.yandex.div.internal.Log.d(
                "UIDiv2ActionHandler",
                "No @{ patterns found, returning original string"
            )
            return inputString
        }

        try {
            // Use regex to find all @{variable_name} patterns
            val variablePattern = Regex("@\\{([^}]+)\\}")
            var result = inputString

            com.yandex.div.internal.Log.d(
                "UIDiv2ActionHandler",
                "Processing string with regex pattern"
            )

            // Find all matches and replace them
            val matches = variablePattern.findAll(inputString).toList()
            com.yandex.div.internal.Log.d(
                "UIDiv2ActionHandler",
                "Found ${matches.size} variable expressions to process"
            )

            matches.forEach { matchResult ->
                val fullMatch = matchResult.value // e.g., "@{my_variable}"
                val variableName = matchResult.groupValues[1] // e.g., "my_variable"

                com.yandex.div.internal.Log.d(
                    "UIDiv2ActionHandler",
                    "Processing variable: $variableName (full match: $fullMatch)"
                )

                try {
                    // Create expression and evaluate it
                    val expressionString = "@{$variableName}"
                    com.yandex.div.internal.Log.d(
                        "UIDiv2ActionHandler",
                        "Created expression: $expressionString"
                    )
                    val evaluable = com.yandex.div.evaluable.Evaluable.lazy(expressionString)

                    // Get the variable value using the expression resolver
                    com.yandex.div.internal.Log.d(
                        "UIDiv2ActionHandler",
                        "Evaluating variable: $variableName"
                    )
                    val variableValue = expressionResolver.get<Any, Any>(
                        "nested_var_$variableName",
                        expressionString,
                        evaluable,
                        null,
                        object : com.yandex.div.internal.parser.ValueValidator<Any> {
                            override fun isValid(value: Any): Boolean = true
                        },
                        object : com.yandex.div.internal.parser.TypeHelper<Any> {
                            override fun isTypeValid(value: Any): Boolean = true
                            override val typeDefault: Any = ""
                        },
                        com.yandex.div.json.ParsingErrorLogger.LOG
                    )

                    // Convert to string and replace the expression
                    val evaluatedStringValue = variableValue?.toString() ?: ""
                    com.yandex.div.internal.Log.d(
                        "UIDiv2ActionHandler",
                        "Variable '$variableName' evaluated to: '$evaluatedStringValue'"
                    )
                    result = result.replace(fullMatch, evaluatedStringValue)
                    com.yandex.div.internal.Log.d(
                        "UIDiv2ActionHandler",
                        "Result after replacement: $result"
                    )

                } catch (e: Exception) {
                    // If individual variable evaluation fails, keep the original expression
                    // This allows partial interpolation to work
                    com.yandex.div.internal.Log.w(
                        "UIDiv2ActionHandler",
                        "Failed to evaluate variable '$variableName': ${e.message}"
                    )
                }
            }

            com.yandex.div.internal.Log.d("UIDiv2ActionHandler", "Final result: $result")
            return result
        } catch (e: Exception) {
            // If overall evaluation fails, return the original string
            com.yandex.div.internal.Log.e(
                "UIDiv2ActionHandler",
                "Failed to process nested variables: ${e.message}"
            )
            return inputString
        }
    }

    private fun looksLikeFilePath(value: String): Boolean {
        val trimmed = value.trim()
        if (trimmed.isEmpty() || trimmed == "null" || trimmed == "empty") {
            return false
        }
        return trimmed.startsWith("/") ||
            trimmed.startsWith("file:") ||
            trimmed.startsWith("content:") ||
            trimmed.contains("/") ||
            File(trimmed).exists()
    }
    
    /**
     * Set a Div variable on base screen or a bottom sheet target.
     * Use target=base to write to the main screen (e.g. from static_tree_view inside a bottom sheet).
     */
    fun setVariableOnTarget(name: String, value: String, target: String, view: DivViewFacade): Boolean {
        this.view = view
        return when (target.lowercase()) {
            "base" -> {
                loadScreenListener.setVariableToBase(name, value)
                true
            }
            else -> {
                val success = bottomSheetManager?.setVariableOnTarget(target, name, value) ?: false
                if (!success) {
                    val currentSheet = bottomSheetManager?.getCurrentBottomSheet()
                        ?: btmSheet_div
                    currentSheet?.setVariableOnBottomSheet(name, value)
                    currentSheet != null
                } else {
                    true
                }
            }
        }
    }

    fun dismissCurrentBottomSheet() {
        btmSheet_div?.dismiss()
            ?: bottomSheetManager?.getCurrentBottomSheet()?.dismiss()
    }

    private fun scheduleFocusOnBase(elementId: String) {
        val host = (activity as? MehdiActivity)
            ?: (context as? MehdiActivity)
            ?: return
        // Wait until bottomsheet dialog has fully released window focus.
        host.window.decorView.postDelayed({
            host.focusDivElement(elementId)
        }, 350)
    }

    /**
     * Handle action from URI string (for push notifications)
     */
    fun handleActionFromString(actionUrl: String) {
        try {
            val uri = Uri.parse(actionUrl)
            handleActivityActionUrl(uri, view ?: return)
        } catch (e: Exception) {
            Log.e("UIDiv2ActionHandler", "Error handling action from string: $actionUrl", e)
        }
    }

    private enum class PatchApplyTarget {
        BASE,
        BOTTOM_SHEET,
    }

    private fun isBottomSheetActionView(): Boolean {
        val div2View = view as? Div2View ?: return false
        return btmSheet_div?.hostsDiv2View(div2View) == true
    }

    private fun resolvePatchApplyTarget(uri: Uri, forceBottomSheet: Boolean): PatchApplyTarget {
        if (forceBottomSheet) return PatchApplyTarget.BOTTOM_SHEET

        val applyTo = uri.getQueryParameter(PARAM_APPLY_TO)
            ?: uri.getQueryParameter(PARAM_TARGET_BOTTOM_SHEET)

        return when (applyTo?.lowercase()) {
            "bottomsheet", "bottom_sheet", "sheet", "current", "top", "latest" ->
                PatchApplyTarget.BOTTOM_SHEET
            "base", "main", "screen" -> PatchApplyTarget.BASE
            null -> PatchApplyTarget.BASE
            else -> PatchApplyTarget.BASE
        }
    }

    /** Fallback when patch is not yet in PhPlus DB (e.g. ph/main/grid/patch). */
    private fun loadPatchJsonFromAssets(name: String?): String? {
        if (name.isNullOrBlank()) return null
        val underscored = name.replace('/', '_')
        val folder = name.substringBefore('/', missingDelimiterValue = "")
        val leaf = name.substringAfter('/', missingDelimiterValue = name)
        val candidates = ArrayList<String>().apply {
            add("application/$name.json")
            add("application/${underscored}.json")
            if (folder.isNotBlank()) {
                add("application/$folder/${folder}_$leaf.json")
                add("application/$folder/$underscored.json")
                add("application/$folder/$leaf.json")
            }
            add("application/ph/${underscored}.json")
        }
        for (path in candidates) {
            val json = runCatching {
                context.assets.open(path).bufferedReader().use { it.readText() }
            }.getOrNull()
            if (!json.isNullOrBlank() && json.contains("\"patch\"")) return json
        }
        return null
    }

    private fun applyPatchToDiv2View(
        div2View: Div2View,
        patchJson: String,
        patchName: String,
        patchTitle: String,
        vehicleType: String,
    ) {
        try {
            div2View.applyPatch(JSONObject(patchJson).asDivPatchWithTemplates())
            android.util.Log.d(
                "UIDiv2ActionHandler",
                "Patch applied successfully: patchName=$patchName",
            )
            if (patchTitle.isNotEmpty() && patchTitle != "تست" && patchTitle != "null") {
                setVariableIfPresent(div2View, patchTitle, "variable_patch", "patch")
            }
            if (vehicleType.isNotEmpty() && vehicleType != "تست" && vehicleType != "null") {
                div2View.setVariable("vehicle_type", vehicleType)
            }
            if (patchName.isNotEmpty() && patchName != "تست" && patchName != "patchEstelam") {
                div2View.setVariable("plateType", patchName)
            }
        } catch (e: Exception) {
            android.util.Log.e("UIDiv2ActionHandler", "Failed to apply patch to Div2View", e)
        }
    }

    private fun normalizeBottomSheetPatchTarget(target: String): String =
        when (target.lowercase()) {
            "bottomsheet", "bottom_sheet", "sheet" -> "current"
            else -> target
        }

    private fun applyPatchFromAction(
        uri: Uri,
        patchJson: String,
        patchName: String,
        patchTitle: String,
        vehicleType: String,
        forceBottomSheet: Boolean,
    ) {
        val dismiss = uri.getQueryParameter(PARAM_SET_PATCH_BOTTOMSHEET_DISMISS)
            ?.equals("true", ignoreCase = true) == true

        when (resolvePatchApplyTarget(uri, forceBottomSheet)) {
            PatchApplyTarget.BOTTOM_SHEET -> {
                val div2View = view as? Div2View
                if (div2View != null && isBottomSheetActionView()) {
                    applyPatchToDiv2View(div2View, patchJson, patchName, patchTitle, vehicleType)
                    if (dismiss) btmSheet_div?.dismiss()
                    return
                }

                val target = normalizeBottomSheetPatchTarget(
                    uri.getQueryParameter(PARAM_TARGET_BOTTOM_SHEET)
                        ?: uri.getQueryParameter(PARAM_APPLY_TO)
                        ?: "current"
                )
                val appliedOnHost = btmSheet_div?.let { sheet ->
                    sheet.onApplyOnbase(patchJson, patchName, patchTitle, vehicleType)
                    if (dismiss) sheet.dismiss()
                    true
                } ?: false

                if (!appliedOnHost) {
                    val applied = bottomSheetManager?.applyPatchOnTarget(
                        target, patchJson, patchName, patchTitle, vehicleType
                    ) ?: false
                    if (!applied) {
                        android.util.Log.w(
                            "UIDiv2ActionHandler",
                            "No bottom sheet found for patch target=$target, applying to base"
                        )
                        loadScreenListener.onApplyOnbase(patchJson, patchName, patchTitle, vehicleType)
                    } else if (dismiss) {
                        bottomSheetManager?.getCurrentBottomSheet()?.dismiss()
                    }
                }
            }

            PatchApplyTarget.BASE -> {
                loadScreenListener.onApplyOnbase(patchJson, patchName, patchTitle, vehicleType)
                if (dismiss) {
                    btmSheet_div?.dismiss()
                        ?: bottomSheetManager?.getCurrentBottomSheet()?.dismiss()
                }
            }
        }
    }

    private fun saveVehicleToOfflineList(uri: Uri) {
        val accidentId = uri.getQueryParameter("accident_id")
            ?.takeIf { it.isNotBlank() }
            ?: mehdiViewModel?.getValueByKey("current_accident_id")?.value.orEmpty()
        val vehicleId = uri.getQueryParameter("vehicle_id")
            ?.takeIf { it.isNotBlank() }
            ?: mehdiViewModel?.getValueByKey("current_vehicle_id")?.value.orEmpty()

        if (accidentId.isBlank() || vehicleId.isBlank()) {
            android.util.Log.w("UIDiv2ActionHandler", "save_vehicle_to_list: missing accident_id or vehicle_id")
            return
        }

        val div2View = if (view is Div2View) view as Div2View else null
        val liveVariables = div2View?.let { Div2ViewVariableExporter.captureVariablesMap(it) }.orEmpty()

        val username = mehdiViewModel?.getValueByKey("userName")?.value.orEmpty()
        val archiveKeyPreview = "$username/vehicles/$accidentId/$vehicleId"
        if (!VehicleEntryCountHelper.canAddAnotherVehicle(
                mehdiViewModel,
                accidentId,
                vehicleId,
                archiveKeyPreview,
            )
        ) {
            Toast.makeText(
                context,
                "تعداد وسایل نقلیه به حد مجاز (${VehicleEntryCountHelper.resolveRequiredVehicleCount(mehdiViewModel, accidentId)}) رسیده است",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        val pdf = PersianDateFormat("Y/m/d-H:i:s")
        val currentDate = pdf.format(PersianDate())

        val plateOne = readVehicleField(
            vehicleId,
            liveVariables,
            "variable_compromising_vehicle_plate_one",
            "variable_plateA",
            "uncompromising_vehicle_plate_one",
        )
        val plateB = readVehicleField(
            vehicleId,
            liveVariables,
            "variable_plateB",
            "uncompromising_vehicle_plate_alphabet",
        )
        val plateThree = readVehicleField(
            vehicleId,
            liveVariables,
            "variable_compromising_vehicle_plate_three",
            "variable_plateC",
            "uncompromising_vehicle_plate_three",
        )
        val plateFour = readVehicleField(
            vehicleId,
            liveVariables,
            "variable_compromising_vehicle_plate_four",
            "variable_plateD",
            "uncompromising_vehicle_plate_four",
        )
        val plate = listOf(plateOne, plateB, plateThree, plateFour)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        val model = readVehicleField(
            vehicleId,
            liveVariables,
            "variable_compromising_vehicle_factory",
            "uncompromising_vehicle_factory",
        )
        val usage = readVehicleField(
            vehicleId,
            liveVariables,
            "variable_compromising_vehicle_usage",
            "uncompromising_vehicle_usage",
        )
        val driverName = readVehicleField(
            vehicleId,
            liveVariables,
            "variable_compromising_driver_name",
        )
        val driverFamily = readVehicleField(
            vehicleId,
            liveVariables,
            "variable_compromising_driver_family",
        )
        val driver = listOf(driverName, driverFamily).filter { it.isNotBlank() }.joinToString(" ")
        val isAtFault = isVehicleMarkedAtFault(vehicleId, liveVariables)
        val insuranceCompany = readVehicleField(
            vehicleId,
            liveVariables,
            "compromising_insurance_company_name",
            "uncompromising_insurance_company_name",
        )

        val archiveKey = "$username/vehicles/$accidentId/$vehicleId"
        val existingJson = mehdiViewModel?.getValueByKey(archiveKey)?.value
        val existingArchive = existingJson?.let { runCatching { JSONObject(it) }.getOrNull() }
        val offlineRegisterDate = existingArchive
            ?.optString("offline_register_date")
            ?.takeIf { it.isNotBlank() }
            ?: currentDate
        val existingDriverSignature = existingArchive
            ?.optString(VehicleEntryCountHelper.DRIVER_SIGNATURE_ARCHIVE_FIELD)
            .orEmpty()

        val map = linkedMapOf(
            "accident_id" to accidentId,
            "vehicle_id" to vehicleId,
            "process_type" to "vehicle",
            "title" to (plate.ifBlank { "وسیله نقلیه" }),
            "next" to "draft/$vehicleId/page1",
            "offline_register_date" to offlineRegisterDate,
            "plate_dbshow" to plate,
            "plate_persian_lan" to "پلاک",
            "model_dbshow" to model,
            "model_persian_lan" to "مدل",
            "usage_dbshow" to usage,
            "usage_persian_lan" to "کاربری",
            "driver_dbshow" to driver,
            "driver_persian_lan" to "راننده",
            "is_at_fault" to if (isAtFault) "true" else "false",
            "insurance_dbshow" to insuranceCompany,
            "insurance_persian_lan" to "بیمه",
            "archive_key" to archiveKey,
        )
        if (existingDriverSignature.isNotBlank()) {
            map[VehicleEntryCountHelper.DRIVER_SIGNATURE_ARCHIVE_FIELD] = existingDriverSignature
        }

        val json = Gson().toJson(map)
        android.util.Log.d(
            "UIDiv2ActionHandler",
            "save_vehicle_to_list: archiveKey=$archiveKey plate=$plate model=$model driver=$driver atFault=$isAtFault insurance=$insuranceCompany",
        )
        mehdiViewModel?.upsertVehicleArchiveEntry(
            username = username,
            accidentId = accidentId,
            vehicleId = vehicleId,
            archiveKey = archiveKey,
            json = json,
        )
        VehicleEntryCountHelper.sync(mehdiViewModel, div2View, accidentId)
    }

    private fun savePassengerToOfflineList(uri: Uri) {
        val accidentId = uri.getQueryParameter("accident_id")
            ?.takeIf { it.isNotBlank() }
            ?: mehdiViewModel?.getValueByKey("current_accident_id")?.value.orEmpty()
        val vehicleId = uri.getQueryParameter("vehicle_id")
            ?.takeIf { it.isNotBlank() }
            ?: mehdiViewModel?.getValueByKey("current_vehicle_id")?.value.orEmpty()
        val passengerId = uri.getQueryParameter("passenger_id")
            ?.takeIf { it.isNotBlank() }
            ?: mehdiViewModel?.getValueByKey("current_passenger_id")?.value.orEmpty()

        if (vehicleId.isBlank() || passengerId.isBlank()) {
            android.util.Log.w(
                "UIDiv2ActionHandler",
                "save_passenger_to_list: missing vehicle_id or passenger_id",
            )
            return
        }

        val div2View = if (view is Div2View) view as Div2View else null
        val liveVariables = div2View?.let { Div2ViewVariableExporter.captureVariablesMap(it) }.orEmpty()

        val username = mehdiViewModel?.getValueByKey("userName")?.value.orEmpty()
        val pdf = PersianDateFormat("Y/m/d-H:i:s")
        val currentDate = pdf.format(PersianDate())

        val name = readEntityField(passengerId, liveVariables, "variable_name")
        val family = readEntityField(passengerId, liveVariables, "variable_family_name")
        val nationalCode = readEntityField(passengerId, liveVariables, "variable_national_code")
        val gender = readEntityField(passengerId, liveVariables, "variable_gender")
        val age = readEntityField(passengerId, liveVariables, "variable_age")
        val title = listOf(name, family).filter { it.isNotBlank() }.joinToString(" ")
            .ifBlank { "سرنشین" }

        val archiveKey = "$username/passengers/$vehicleId/$passengerId"
        val existingJson = mehdiViewModel?.getValueByKey(archiveKey)?.value
        val offlineRegisterDate = if (existingJson != null) {
            runCatching {
                JSONObject(existingJson).optString("offline_register_date")
            }.getOrNull()?.takeIf { it.isNotBlank() } ?: currentDate
        } else {
            currentDate
        }

        val map = linkedMapOf(
            "accident_id" to accidentId,
            "vehicle_id" to vehicleId,
            "passenger_id" to passengerId,
            "process_type" to "passenger",
            "title" to title,
            "next" to "draft/$passengerId/page1",
            "offline_register_date" to offlineRegisterDate,
            "national_code_dbshow" to nationalCode,
            "national_code_persian_lan" to "کد ملی",
            "gender_dbshow" to gender,
            "gender_persian_lan" to "جنسیت",
            "age_dbshow" to age,
            "age_persian_lan" to "سن",
            "archive_key" to archiveKey,
        )

        val json = Gson().toJson(map)
        android.util.Log.d(
            "UIDiv2ActionHandler",
            "save_passenger_to_list: archiveKey=$archiveKey title=$title",
        )
        mehdiViewModel?.upsertPassengerArchiveEntry(
            username = username,
            vehicleId = vehicleId,
            passengerId = passengerId,
            archiveKey = archiveKey,
            json = json,
        )
    }

    private fun savePedestrianToOfflineList(uri: Uri) {
        val accidentId = uri.getQueryParameter("accident_id")
            ?.takeIf { it.isNotBlank() }
            ?: mehdiViewModel?.getValueByKey("current_accident_id")?.value.orEmpty()
        val pedestrianId = uri.getQueryParameter("pedestrian_id")
            ?.takeIf { it.isNotBlank() }
            ?: mehdiViewModel?.getValueByKey("current_pedestrian_id")?.value.orEmpty()

        if (accidentId.isBlank() || pedestrianId.isBlank()) {
            android.util.Log.w(
                "UIDiv2ActionHandler",
                "save_pedestrian_to_list: missing accident_id or pedestrian_id",
            )
            return
        }

        val div2View = if (view is Div2View) view as Div2View else null
        val liveVariables = div2View?.let { Div2ViewVariableExporter.captureVariablesMap(it) }.orEmpty()

        val username = mehdiViewModel?.getValueByKey("userName")?.value.orEmpty()
        val pdf = PersianDateFormat("Y/m/d-H:i:s")
        val currentDate = pdf.format(PersianDate())

        val name = readEntityField(pedestrianId, liveVariables, "uncompromising_pedestrian_name")
        val family = readEntityField(pedestrianId, liveVariables, "uncompromising_pedestrian_family_name")
        val nationalCode = readEntityField(pedestrianId, liveVariables, "uncompromising_pedestrian_national_code")
        val gender = readEntityField(pedestrianId, liveVariables, "uncompromising_pedestrian_gender")
        val age = readEntityField(pedestrianId, liveVariables, "uncompromising_pedestrian_age")
        val title = listOf(name, family).filter { it.isNotBlank() }.joinToString(" ")
            .ifBlank { "عابر" }

        val archiveKey = "$username/pedestrians/$accidentId/$pedestrianId"
        val existingJson = mehdiViewModel?.getValueByKey(archiveKey)?.value
        val offlineRegisterDate = if (existingJson != null) {
            runCatching {
                JSONObject(existingJson).optString("offline_register_date")
            }.getOrNull()?.takeIf { it.isNotBlank() } ?: currentDate
        } else {
            currentDate
        }

        val map = linkedMapOf(
            "accident_id" to accidentId,
            "pedestrian_id" to pedestrianId,
            "process_type" to "pedestrian",
            "title" to title,
            "next" to "draft/$pedestrianId/page1",
            "offline_register_date" to offlineRegisterDate,
            "national_code_dbshow" to nationalCode,
            "national_code_persian_lan" to "کد ملی",
            "name_dbshow" to name,
            "name_persian_lan" to "نام",
            "family_dbshow" to family,
            "family_persian_lan" to "نام خانوادگی",
            "gender_dbshow" to gender,
            "gender_persian_lan" to "جنسیت",
            "age_dbshow" to age,
            "age_persian_lan" to "سن",
            "archive_key" to archiveKey,
        )

        val json = Gson().toJson(map)
        android.util.Log.d(
            "UIDiv2ActionHandler",
            "save_pedestrian_to_list: archiveKey=$archiveKey title=$title",
        )
        mehdiViewModel?.upsertPedestrianArchiveEntry(
            username = username,
            accidentId = accidentId,
            pedestrianId = pedestrianId,
            archiveKey = archiveKey,
            json = json,
        )
    }

    private fun saveOrganizationToOfflineList(uri: Uri) {
        val accidentId = uri.getQueryParameter("accident_id")
            ?.takeIf { it.isNotBlank() }
            ?: mehdiViewModel?.getValueByKey("current_accident_id")?.value.orEmpty()
        val organizationId = uri.getQueryParameter("organization_id")
            ?.takeIf { it.isNotBlank() }
            ?: mehdiViewModel?.getValueByKey("current_organization_id")?.value.orEmpty()

        if (accidentId.isBlank() || organizationId.isBlank()) {
            android.util.Log.w(
                "UIDiv2ActionHandler",
                "save_organization_to_list: missing accident_id or organization_id",
            )
            return
        }

        val div2View = if (view is Div2View) view as Div2View else null
        val liveVariables = div2View?.let { Div2ViewVariableExporter.captureVariablesMap(it) }.orEmpty()

        val username = mehdiViewModel?.getValueByKey("userName")?.value.orEmpty()
        val pdf = PersianDateFormat("Y/m/d-H:i:s")
        val currentDate = pdf.format(PersianDate())

        val name = readEntityField(organizationId, liveVariables, "uncompromising_organization_name")
        val faultStatus = readEntityField(
            organizationId,
            liveVariables,
            "uncompromising_organization_fault_status",
        )
        val judicialCause = readEntityField(
            organizationId,
            liveVariables,
            "uncompromising_organization_judicial_cause",
        )
        val mainCause = readEntityField(
            organizationId,
            liveVariables,
            "uncompromising_organization_main_cause",
        )
        val title = name.ifBlank { "سازمان مقصر" }

        val archiveKey = "$username/organizations/$accidentId/$organizationId"
        val existingJson = mehdiViewModel?.getValueByKey(archiveKey)?.value
        val offlineRegisterDate = if (existingJson != null) {
            runCatching {
                JSONObject(existingJson).optString("offline_register_date")
            }.getOrNull()?.takeIf { it.isNotBlank() } ?: currentDate
        } else {
            currentDate
        }

        val map = linkedMapOf(
            "accident_id" to accidentId,
            "organization_id" to organizationId,
            "process_type" to "organization",
            "title" to title,
            "next" to "draft/$organizationId/page1",
            "offline_register_date" to offlineRegisterDate,
            "fault_status_dbshow" to faultStatus,
            "fault_status_persian_lan" to "وضعیت تقصیر",
            "judicial_cause_dbshow" to judicialCause,
            "judicial_cause_persian_lan" to "علت قضایی تصادف",
            "main_cause_dbshow" to mainCause,
            "main_cause_persian_lan" to "علت تامه",
            "archive_key" to archiveKey,
        )

        val json = Gson().toJson(map)
        android.util.Log.d(
            "UIDiv2ActionHandler",
            "save_organization_to_list: archiveKey=$archiveKey title=$title",
        )
        mehdiViewModel?.upsertOrganizationArchiveEntry(
            username = username,
            accidentId = accidentId,
            organizationId = organizationId,
            archiveKey = archiveKey,
            json = json,
        )
    }

    private fun saveLivestockToOfflineList(uri: Uri) {
        val accidentId = uri.getQueryParameter("accident_id")
            ?.takeIf { it.isNotBlank() }
            ?: mehdiViewModel?.getValueByKey("current_accident_id")?.value.orEmpty()
        val livestockId = uri.getQueryParameter("livestock_id")
            ?.takeIf { it.isNotBlank() }
            ?: mehdiViewModel?.getValueByKey("current_livestock_id")?.value.orEmpty()

        if (accidentId.isBlank() || livestockId.isBlank()) {
            android.util.Log.w(
                "UIDiv2ActionHandler",
                "save_livestock_to_list: missing accident_id or livestock_id",
            )
            return
        }

        val div2View = if (view is Div2View) view as Div2View else null
        val liveVariables = div2View?.let { Div2ViewVariableExporter.captureVariablesMap(it) }.orEmpty()

        val typeTitle = liveVariables["compromising_livestock_type"]?.toString().orEmpty()
        val typeCode = liveVariables["compromising_livestock_type_code"]?.toString().orEmpty()
        val count = liveVariables["variable_livestock_count"]?.toString().orEmpty()

        if (typeCode.isBlank() || typeTitle.isBlank() || typeTitle == "انتخاب کنید") {
            Toast.makeText(context, "لطفاً نوع حیوان را انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }
        if (count.isBlank() || count == "0") {
            Toast.makeText(context, "لطفاً تعداد را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val username = mehdiViewModel?.getValueByKey("userName")?.value.orEmpty()
        val pdf = PersianDateFormat("Y/m/d-H:i:s")
        val currentDate = pdf.format(PersianDate())

        val archiveKey = "$username/livestock/$accidentId/$livestockId"
        val existingJson = mehdiViewModel?.getValueByKey(archiveKey)?.value
        val offlineRegisterDate = if (existingJson != null) {
            runCatching {
                JSONObject(existingJson).optString("offline_register_date")
            }.getOrNull()?.takeIf { it.isNotBlank() } ?: currentDate
        } else {
            currentDate
        }

        val title = listOf(typeTitle, "× $count").joinToString(" ")
        val map = linkedMapOf(
            "accident_id" to accidentId,
            "livestock_id" to livestockId,
            "process_type" to "livestock",
            "title" to title,
            "type_code" to typeCode,
            "type_title" to typeTitle,
            "count" to count,
            "next" to "Accidents/livestock_bottomsheet",
            "offline_register_date" to offlineRegisterDate,
            "type_dbshow" to typeTitle,
            "type_persian_lan" to "نوع حیوان",
            "count_dbshow" to count,
            "count_persian_lan" to "تعداد",
            "archive_key" to archiveKey,
        )

        val json = Gson().toJson(map)
        android.util.Log.d(
            "UIDiv2ActionHandler",
            "save_livestock_to_list: archiveKey=$archiveKey title=$title",
        )
        mehdiViewModel?.upsertLivestockArchiveEntry(
            username = username,
            accidentId = accidentId,
            livestockId = livestockId,
            archiveKey = archiveKey,
            json = json,
        )

        mehdiViewModel?.insertItemToDbSync(PhPlusDB(null, "compromising_livestock_type", "انتخاب کنید"))
        mehdiViewModel?.insertItemToDbSync(PhPlusDB(null, "compromising_livestock_type_code", ""))
        mehdiViewModel?.insertItemToDbSync(PhPlusDB(null, "variable_livestock_count", ""))
        dismissCurrentBottomSheet()
    }

    private fun isVehicleMarkedAtFault(
        vehicleId: String,
        liveVariables: Map<String, Any>,
    ): Boolean {
        fun asString(value: Any?): String = value?.toString().orEmpty()

        liveVariables.forEach { (key, value) ->
            val name = key.trim()
            if (
                name == "variable_validation_vehicle_select_fault_state" ||
                name == "validation_vehicle_select_fault_state" ||
                name == "validation_fault_state"
            ) {
                val raw = asString(value).trim()
                if (raw.equals("true", ignoreCase = true)) return true
                if (raw.equals("false", ignoreCase = true)) return false
            }
        }

        val fromDraft = readVehicleField(
            vehicleId,
            liveVariables,
            "variable_validation_vehicle_select_fault_state",
            "validation_vehicle_select_fault_state",
            "validation_fault_state",
        ).trim()
        if (fromDraft.equals("true", ignoreCase = true)) return true
        if (fromDraft.equals("false", ignoreCase = true)) return false

        val faultBg = firstNonBlankColor(
            asString(liveVariables["variable_vehicle_fault_background_name"]),
            readVehicleField(vehicleId, liveVariables, "variable_vehicle_fault_background_name"),
        )
        val nonFaultBg = firstNonBlankColor(
            asString(liveVariables["variable_vehicle_non_fault_background_name"]),
            readVehicleField(vehicleId, liveVariables, "variable_vehicle_non_fault_background_name"),
        )
        val faultSelected = faultBg.removePrefix("#").equals("ffffff", ignoreCase = true)
        val nonFaultSelected = nonFaultBg.removePrefix("#").equals("ffffff", ignoreCase = true)
        return when {
            faultSelected && !nonFaultSelected -> true
            nonFaultSelected && !faultSelected -> false
            else -> faultSelected
        }
    }

    private fun syncVehicleArchiveFaultFromDraft(draftKey: String, div2View: Div2View?) {
        val match = Regex("^draft/([^/]+)/page1$").find(draftKey) ?: return
        val vehicleId = match.groupValues[1]
        val accidentId = mehdiViewModel?.getValueByKey("current_accident_id")?.value.orEmpty()
        val username = mehdiViewModel?.getValueByKey("userName")?.value.orEmpty()
        if (vehicleId.isBlank() || accidentId.isBlank() || username.isBlank()) return

        val archiveKey = "$username/vehicles/$accidentId/$vehicleId"
        val existingJson = mehdiViewModel?.getValueByKey(archiveKey)?.value ?: return
        val liveVariables = div2View?.let { Div2ViewVariableExporter.captureVariablesMap(it) }.orEmpty()
        val isAtFault = isVehicleMarkedAtFault(vehicleId, liveVariables)
        val updated = runCatching {
            JSONObject(existingJson).apply {
                put("is_at_fault", if (isAtFault) "true" else "false")
            }.toString()
        }.getOrNull() ?: return

        mehdiViewModel?.upsertVehicleArchiveEntry(
            username = username,
            accidentId = accidentId,
            vehicleId = vehicleId,
            archiveKey = archiveKey,
            json = updated,
        )
        android.util.Log.d(
            "UIDiv2ActionHandler",
            "syncVehicleArchiveFaultFromDraft: vehicleId=$vehicleId isAtFault=$isAtFault",
        )
    }

    private fun firstNonBlankColor(vararg values: String): String {
        return values.firstOrNull { it.isNotBlank() && it != "null" }.orEmpty()
    }

    private fun readVehicleField(
        vehicleId: String,
        liveVariables: Map<String, Any>,
        vararg variableNames: String,
    ): String = readEntityField(vehicleId, liveVariables, *variableNames)

    private fun readEntityField(
        entityId: String,
        liveVariables: Map<String, Any>,
        vararg variableNames: String,
    ): String {
        for (variableName in variableNames) {
            liveVariables[variableName]?.toString()?.takeIf { it.isNotBlank() }?.let { return it }
            listOf("page4", "page3", "page2", "page1").forEach { page ->
                val draftValue = readDraftVariable(entityId, page, variableName)
                if (draftValue.isNotBlank()) {
                    return draftValue
                }
            }
        }
        return ""
    }

    private fun readDraftVariable(vehicleId: String, page: String, variableName: String): String {
        val draftKey = "draft/$vehicleId/$page"
        val draftJson = mehdiViewModel?.getValueByKey(draftKey)?.value ?: return ""
        val vars = DraftVariables.extract(draftJson)
        val alias = if (variableName.startsWith("variable_")) {
            variableName.removePrefix("variable_")
        } else {
            "variable_$variableName"
        }
        return DraftVariables.run { vars.valueOf(variableName, alias) }
    }

    private fun resolveScreenName(screenName: String): String {
        if (screenName.isEmpty()) return ""
        return if (screenName == "nextSplash") {
            mehdiViewModel?.getValueByKey(screenName)?.value.orEmpty()
        } else {
            screenName
        }
    }

    private fun resolveCurrentLocation(): Pair<Double, Double>? {
        LocationService.getLastLocation(context)?.let { location ->
            return location.latitude to location.longitude
        }

        val lat = mehdiViewModel?.getValueByKey("latitude")?.value?.toDoubleOrNull()
        val lng = mehdiViewModel?.getValueByKey("longitude")?.value?.toDoubleOrNull()
        if (lat != null && lng != null) {
            return lat to lng
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        return lastKnown?.let { it.latitude to it.longitude }
    }

    private fun resolveBottomSheetArrayVariables(
        uri: Uri,
        div2View: Div2View?,
        actionLogId: String,
    ): Map<String, String> {
        val arrayName = uri.getQueryParameter(PARAM_BOTTOM_SHEET_ARRAY)?.takeIf { it.isNotBlank() }
            ?: return emptyMap()
        val matchKey = uri.getQueryParameter(PARAM_BOTTOM_SHEET_MATCH_KEY)?.takeIf { it.isNotBlank() }
            ?: return emptyMap()
        val matchValue = uri.getQueryParameter(PARAM_BOTTOM_SHEET_MATCH_VALUE)?.takeIf { it.isNotBlank() }
            ?: actionLogId.takeIf { it.isNotBlank() }
            ?: return emptyMap()
        val prefix = uri.getQueryParameter(PARAM_BOTTOM_SHEET_VAR_PREFIX).orEmpty()
        val item = findArrayItemByKey(div2View, arrayName, matchKey, matchValue)
        if (item.isEmpty()) {
            android.util.Log.w(
                "UIDiv2ActionHandler",
                "bottom_sheet_div array lookup empty array=$arrayName key=$matchKey value=$matchValue"
            )
            return mapOf(prefix + matchKey to matchValue)
        }
        return item.mapKeys { (key, _) -> prefix + key }
            .mapValues { (_, value) -> value.ifBlank { "-" } }
            .toMutableMap()
            .also { mapped ->
                if (mapped[prefix + matchKey].isNullOrBlank() || mapped[prefix + matchKey] == "-") {
                    mapped[prefix + matchKey] = matchValue
                }
            }
    }

    private fun findArrayItemByKey(
        div2View: Div2View?,
        arrayName: String,
        matchKey: String,
        matchValue: String,
    ): Map<String, String> {
        if (div2View == null) return emptyMap()
        val raw = getDivVariableValue(div2View, arrayName)
            ?: Div2ViewVariableExporter.captureVariablesMap(div2View)[arrayName]
        val array = anyToJsonArray(raw)
        android.util.Log.d(
            "UIDiv2ActionHandler",
            "array lookup name=$arrayName type=${raw?.javaClass?.name} size=${array.length()} key=$matchKey value=$matchValue"
        )
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: jsonObjectFrom(array.opt(i)) ?: continue
            val candidate = obj.opt(matchKey)?.toString().orEmpty()
            if (candidate == matchValue) {
                return obj.keys().asSequence()
                    .associateWith { key -> obj.opt(key)?.toString().orEmpty() }
            }
        }
        return emptyMap()
    }

    private fun getDivVariableValue(div2View: Div2View, name: String): Any? {
        return runCatching {
            val runtimeField = Div2View::class.java.getDeclaredField("expressionsRuntime")
            runtimeField.isAccessible = true
            val runtime = runtimeField.get(div2View) ?: return null
            val vcField = runtime.javaClass.getDeclaredField("variableController")
            vcField.isAccessible = true
            val controller = vcField.get(runtime) ?: return null
            val getter = controller.javaClass.methods.firstOrNull {
                it.name == "getMutableVariable" && it.parameterTypes.size == 1
            } ?: return null
            val variable = getter.invoke(controller, name)
            variable?.javaClass?.methods
                ?.firstOrNull { it.name == "getValue" && it.parameterTypes.isEmpty() }
                ?.invoke(variable)
        }.onFailure {
            android.util.Log.w("UIDiv2ActionHandler", "getDivVariableValue($name) failed", it)
        }.getOrNull()
    }

    private fun logVtRegisterValidation(view: DivViewFacade) {
        val tag = "VtRegisterValidation"
        val div2View = view as? Div2View
        if (div2View == null) {
            android.util.Log.e(tag, "Div2View is null — cannot dump validation state")
            return
        }

        fun v(name: String): String {
            val raw = getDivVariableValue(div2View, name)
            return when (raw) {
                null -> "<null>"
                is String -> if (raw.length > 80) "${raw.take(20)}…(len=${raw.length})" else raw
                else -> raw.toString()
            }
        }

        fun isTruthy(name: String): Boolean {
            return when (val raw = getDivVariableValue(div2View, name)) {
                is Boolean -> raw
                is String -> raw.equals("true", ignoreCase = true)
                is Number -> raw.toInt() != 0
                else -> false
            }
        }

        fun isNonEmpty(name: String): Boolean {
            val raw = getDivVariableValue(div2View, name)?.toString().orEmpty()
            return raw.isNotEmpty()
        }

        val onStatus = isTruthy("on_status")
        val motor = isTruthy("motor")
        val taslimi = isTruthy("taslimi")
        val onlineValidation = isTruthy("online_validation")
        val offlineValidation = isTruthy("offline_validation")
        val lastCheck = isTruthy("last_check")
        val lastCheckOffline = isTruthy("last_check_offline")
        val register = isTruthy("register")
        val registerOffline = isTruthy("register_offline")
        val isValid = isTruthy("is_valid")
        val isValidOffline = isTruthy("is_valid_offline")
        val hasPicture = isNonEmpty("variable_car_picture")
        val pictureValidation = v("picture_validation")

        val wouldCallOnlineMotor = onlineValidation && motor
        val wouldCallOnlineCar = onlineValidation && !motor
        val wouldSaveOffline = offlineValidation

        val blockers = mutableListOf<String>()
        if (!isNonEmpty("variable_usage_title")) blockers += "usage_title خالی"
        if (!isNonEmpty("variable_system_title")) blockers += "system_title خالی"
        if (!isNonEmpty("variable_color_title")) blockers += "color_title خالی"
        if (!isNonEmpty("address")) blockers += "address خالی"
        if (!isNonEmpty("time")) blockers += "time خالی"
        if (v("variable_inquiry_validation") != "true") blockers += "استعلام پلاک نشده (variable_inquiry_validation)"
        if (v("violation_validation") != "true") blockers += "تخلف انتخاب نشده (violation_validation)"
        if (taslimi && !isNonEmpty("national_code")) blockers += "کد ملی خالی (تسلیمی)"
        if (taslimi && v("variable_inquiry_national_code") != "true") {
            blockers += "استعلام کد ملی نشده (تسلیمی)"
        }
        if (!register) blockers += "register=false"
        if (pictureValidation == "true" && !hasPicture) {
            blockers += "عکس خودرو خالی است ولی پیام UI مخفی است (picture_validation=true نیاز به variable_car_picture)"
        }
        if (!lastCheck) blockers += "last_check=false → online_validation روشن نمی‌شود"
        if (onStatus) blockers += "on_status=true (حالت آفلاین) → online call غیرفعال"
        if (!onStatus && !onlineValidation) blockers += "online_validation=false"
        if (onStatus && !offlineValidation) blockers += "offline_validation=false"
        if (!wouldCallOnlineMotor && !wouldCallOnlineCar && !wouldSaveOffline) {
            blockers += "هیچ اکشن call_service/offline enabled نیست"
        }

        android.util.Log.d(tag, "========== VT REGISTER TAP ==========")
        android.util.Log.d(tag, "mode: taslimi=$taslimi motor=$motor on_status(offlineSwitch)=$onStatus")
        android.util.Log.d(tag, "gates: is_valid=$isValid is_valid_offline=$isValidOffline")
        android.util.Log.d(tag, "gates: register=$register register_offline=$registerOffline")
        android.util.Log.d(tag, "gates: last_check=$lastCheck last_check_offline=$lastCheckOffline")
        android.util.Log.d(
            tag,
            "gates: online_validation=$onlineValidation offline_validation=$offlineValidation"
        )
        android.util.Log.d(
            tag,
            "willCall: onlineMotor=$wouldCallOnlineMotor onlineCar=$wouldCallOnlineCar offlineSave=$wouldSaveOffline"
        )
        android.util.Log.d(
            tag,
            "inquiry: plate=${v("variable_inquiry_validation")} national=${v("variable_inquiry_national_code")}"
        )
        android.util.Log.d(
            tag,
            "fields: usage='${v("variable_usage_title")}' system='${v("variable_system_title")}' color='${v("variable_color_title")}'"
        )
        android.util.Log.d(tag, "fields: address='${v("address")}' time='${v("time")}' date='${v("date")}'")
        android.util.Log.d(
            tag,
            "plate: A='${v("variable_plateA")}' B='${v("variable_plateB")}' C='${v("variable_plateC")}' D='${v("variable_plateD")}' car_plate_validation=${v("car_plate_validation")}"
        )
        android.util.Log.d(
            tag,
            "violations: v1='${v("violation1_code")}' v2='${v("violation2_code")}' v3='${v("violation3_code")}' violation_validation=${v("violation_validation")}"
        )
        android.util.Log.d(
            tag,
            "picture: picture_validation=$pictureValidation hasPicture=$hasPicture national_code='${v("national_code")}'"
        )
        if (blockers.isEmpty()) {
            android.util.Log.d(tag, "blockers: NONE — service/offline action should run")
        } else {
            android.util.Log.w(tag, "blockers (${blockers.size}):")
            blockers.forEachIndexed { i, msg ->
                android.util.Log.w(tag, "  ${i + 1}. $msg")
            }
        }
        android.util.Log.d(tag, "========== END VT REGISTER TAP ==========")

        val toastMsg = if (blockers.isEmpty()) {
            "VT: validation OK — سرویس باید کال شود"
        } else {
            "VT block: ${blockers.take(3).joinToString(" | ")}"
        }
        Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
    }

    private fun jsonObjectFrom(raw: Any?): JSONObject? {
        return when (raw) {
            is JSONObject -> raw
            is Map<*, *> -> JSONObject().also { obj ->
                raw.forEach { (k, v) -> if (k != null) obj.put(k.toString(), v ?: JSONObject.NULL) }
            }
            is String -> runCatching { JSONObject(raw) }.getOrNull()
            else -> runCatching { JSONObject(raw.toString()) }.getOrNull()
        }
    }

    private fun anyToJsonArray(raw: Any?): JSONArray {
        return when (raw) {
            is JSONArray -> raw
            is List<*> -> JSONArray().also { arr ->
                raw.forEach { item ->
                    when (item) {
                        is JSONObject -> arr.put(item)
                        is Map<*, *> -> {
                            val obj = JSONObject()
                            item.forEach { (k, v) ->
                                if (k != null) obj.put(k.toString(), v ?: JSONObject.NULL)
                            }
                            arr.put(obj)
                        }
                        is String -> runCatching { arr.put(JSONObject(item)) }
                        else -> runCatching { arr.put(JSONObject(item.toString())) }
                    }
                }
            }
            is String -> if (raw.isBlank()) JSONArray() else JSONArray(raw)
            else -> runCatching { JSONArray(raw?.toString().orEmpty()) }.getOrDefault(JSONArray())
        }
    }

    private fun handleArrayToListDb(uri: Uri, view: DivViewFacade) {
        val div2View = view as? Div2View ?: return
        val arrayName = uri.getQueryParameter("array")?.takeIf { it.isNotBlank() } ?: return
        val key = uri.getQueryParameter("key")?.takeIf { it.isNotBlank() } ?: return
        val idPath = uri.getQueryParameter("id")?.takeIf { it.isNotBlank() } ?: "id"
        val titleTemplate = uri.getQueryParameter("title")?.takeIf { it.isNotBlank() } ?: "{$idPath}"
        val nested = uri.getQueryParameter("nested")?.takeIf { it.isNotBlank() }
        val unique = uri.getBooleanQueryParameter("unique", true)
        val raw = getDivVariableValue(div2View, arrayName)
            ?: Div2ViewVariableExporter.captureVariablesMap(div2View)[arrayName]
        val json = ArrayListDbHelper.buildListJson(
            arrayRaw = raw,
            idPath = idPath,
            titleTemplate = titleTemplate,
            nested = nested,
            unique = unique,
        )
        android.util.Log.d(
            "UIDiv2ActionHandler",
            "array_to_list_db array=$arrayName key=$key nested=$nested jsonLength=${json.length}"
        )
        mehdiViewModel?.insertItemToDbSync(PhPlusDB(null, key, json))
    }

    private fun handleSetVariableFromArray(uri: Uri, view: DivViewFacade) {
        val div2View = view as? Div2View ?: return
        val arrayName = uri.getQueryParameter("array")?.takeIf { it.isNotBlank() } ?: return
        val name = uri.getQueryParameter("name")?.takeIf { it.isNotBlank() } ?: return
        val path = uri.getQueryParameter("path")?.takeIf { it.isNotBlank() } ?: return
        val index = uri.getQueryParameter("index")?.toIntOrNull() ?: 0
        val format = uri.getQueryParameter("format")
        val mapSpec = uri.getQueryParameter("map").orEmpty()
        val defaultValue = uri.getQueryParameter("default")
        val raw = getDivVariableValue(div2View, arrayName)
            ?: Div2ViewVariableExporter.captureVariablesMap(div2View)[arrayName]
        var value = ArrayListDbHelper.formatValue(
            ArrayListDbHelper.pathValue(raw, index, path),
            format,
        )
        if (mapSpec.isNotBlank()) {
            val mapped = mapSpec.split(',')
                .mapNotNull { part ->
                    val trimmed = part.trim()
                    val sep = trimmed.indexOf(':')
                    if (sep <= 0) null
                    else trimmed.substring(0, sep).trim() to trimmed.substring(sep + 1).trim()
                }
                .firstOrNull { (key, _) -> key == value }
                ?.second
            value = mapped ?: defaultValue.orEmpty()
        } else if (value.isBlank() && !defaultValue.isNullOrBlank()) {
            value = defaultValue
        }
        runCatching { div2View.setVariable(name, value) }
            .onFailure {
                android.util.Log.w("UIDiv2ActionHandler", "set_variable_from_array $name failed", it)
            }
    }

    private fun handleSetHotelPriority(uri: Uri, view: DivViewFacade) {
        val div2View = view as? Div2View ?: return
        val turnNo = uri.getQueryParameter("turnNo")
            ?: uri.getQueryParameter("period_id")
            ?: uri.getQueryParameter("id").orEmpty()
        val hotelId = uri.getQueryParameter("hotel_id").orEmpty()
        if (turnNo.isBlank() || hotelId.isBlank()) return
        val hotel = uri.getQueryParameter("hotel").orEmpty()
        val startDate = uri.getQueryParameter("start_date").orEmpty()
        val endDate = uri.getQueryParameter("end_date").orEmpty()
        val startDateMiladi = uri.getQueryParameter("start_date_miladi").orEmpty()
        val endDateMiladi = uri.getQueryParameter("end_date_miladi").orEmpty()
        val subStateType = uri.getQueryParameter("subStateType").orEmpty()
        val dates = uri.getQueryParameter("dates").orEmpty().ifBlank {
            if (startDate.isNotBlank() && endDate.isNotBlank()) "$startDate تا $endDate" else startDate.ifBlank { endDate }
        }
        val slots = readHotelPrioritySlots(div2View).toMutableList()

        val existingIndex = slots.indexOfFirst { it.id == turnNo && it.hotelId == hotelId }
        if (existingIndex >= 0) {
            slots.removeAt(existingIndex)
            slots.add(emptyHotelPrioritySlot())
        } else {
            val emptyIndex = slots.indexOfFirst { it.id.isBlank() }
            if (emptyIndex < 0) {
                Toast.makeText(context, "حداکثر ۳ اولویت می‌توانید انتخاب کنید", Toast.LENGTH_SHORT).show()
                return
            }
            slots[emptyIndex] = HotelPrioritySlot(
                index = emptyIndex + 1,
                id = turnNo,
                hotelId = hotelId,
                hotel = hotel,
                dates = dates,
                startDate = startDate,
                endDate = endDate,
                lastMinute = "false",
                startDateMiladi = startDateMiladi,
                endDateMiladi = endDateMiladi,
                subStateType = subStateType
            )
        }
        writeHotelPrioritySlots(div2View, slots)
    }

    private fun handleClearHotelPriority(uri: Uri, view: DivViewFacade) {
        val div2View = view as? Div2View ?: return
        val slot = uri.getQueryParameter("slot")?.toIntOrNull() ?: return
        if (slot !in 1..3) return
        val slots = readHotelPrioritySlots(div2View).toMutableList()
        slots.removeAt(slot - 1)
        slots.add(emptyHotelPrioritySlot())
        writeHotelPrioritySlots(div2View, slots)
    }

    private fun handleSaveHotelPriorities(view: DivViewFacade) {
        val slots = (view as? Div2View)?.let { readHotelPrioritySlots(it) }
            ?.filter { it.hotelId.isNotBlank() && it.id.isNotBlank() }
            .orEmpty()
        if (slots.isEmpty()) {
            Toast.makeText(context, "هیچ اولویتی انتخاب نشده است", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readHotelPrioritySlots(div2View: Div2View): List<HotelPrioritySlot> {
        val vars = Div2ViewVariableExporter.captureVariablesMap(div2View)
        return (1..3).map { index ->
            HotelPrioritySlot(
                index = index,
                id = vars["hotel_priority_${index}_id"]?.toString().orEmpty(),
                hotelId = vars["hotel_priority_${index}_hotel_id"]?.toString().orEmpty(),
                hotel = vars["hotel_priority_${index}_hotel"]?.toString().orEmpty(),
                dates = vars["hotel_priority_${index}_dates"]?.toString().orEmpty(),
                startDate = vars["hotel_priority_${index}_start_date"]?.toString().orEmpty(),
                endDate = vars["hotel_priority_${index}_end_date"]?.toString().orEmpty(),
                lastMinute = vars["hotel_priority_${index}_last_minute"]?.toString().orEmpty(),
                startDateMiladi = vars["hotel_priority_${index}_start_date_miladi"]?.toString().orEmpty(),
                endDateMiladi = vars["hotel_priority_${index}_end_date_miladi"]?.toString().orEmpty(),
                subStateType = vars["hotel_priority_${index}_sub_state_type"]?.toString().orEmpty()
            )
        }
    }

    private fun writeHotelPrioritySlots(div2View: Div2View, slots: List<HotelPrioritySlot>) {
        slots.take(3).forEachIndexed { index, slot ->
            val n = index + 1
            div2View.setVariable("hotel_priority_${n}_id", slot.id)
            div2View.setVariable("hotel_priority_${n}_hotel_id", slot.hotelId)
            div2View.setVariable("hotel_priority_${n}_hotel", slot.hotel)
            div2View.setVariable("hotel_priority_${n}_dates", slot.dates)
            div2View.setVariable("hotel_priority_${n}_start_date", slot.startDate)
            div2View.setVariable("hotel_priority_${n}_end_date", slot.endDate)
            div2View.setVariable(
                "hotel_priority_${n}_last_minute",
                slot.lastMinute.ifBlank { "false" }
            )
            div2View.setVariable("hotel_priority_${n}_start_date_miladi", slot.startDateMiladi)
            div2View.setVariable("hotel_priority_${n}_end_date_miladi", slot.endDateMiladi)
            div2View.setVariable("hotel_priority_${n}_sub_state_type", slot.subStateType)
        }
    }

    private fun emptyHotelPrioritySlot(): HotelPrioritySlot {
        return HotelPrioritySlot(0, "", "", "", "", "", "", "false", "", "", "")
    }

    private data class HotelPrioritySlot(
        val index: Int,
        val id: String,
        val hotelId: String,
        val hotel: String,
        val dates: String,
        val startDate: String,
        val endDate: String,
        val lastMinute: String,
        val startDateMiladi: String,
        val endDateMiladi: String,
        val subStateType: String
    )

    private fun formatCoordinate(value: Double): String {
        return String.format(Locale.US, "%.6f", value)
    }

    private fun formatPersianDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.US, "%04d/%02d/%02d", year, month, day)
    }
}

