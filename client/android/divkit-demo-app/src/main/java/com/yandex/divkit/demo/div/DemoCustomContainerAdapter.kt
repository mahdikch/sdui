package com.yandex.divkit.demo.div

import OfflineViewList
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import org.json.JSONObject
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.yandex.divkit.demo.data.entities.RoutBody
import com.yandex.divkit.demo.data.remote.RoutingApi
import com.yandex.divkit.demo.utils.ArchiveWizardNavigator
import com.yandex.divkit.demo.utils.Div2ViewVariableExporter
import com.yandex.divkit.demo.utils.VehicleEntryCountHelper
import com.yandex.divkit.demo.utils.SingletonObjects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.yandex.div.core.DivCustomContainerViewAdapter
import com.yandex.div.core.DivCustomViewAdapter.Companion.getDivChildFactory
import com.yandex.div.core.state.DivStatePath
import com.yandex.div.core.view2.Div2View
import com.yandex.div.evaluable.Evaluable
import com.yandex.div.json.ParsingErrorLogger
import com.yandex.div.json.expressions.Expression
import com.yandex.div.json.expressions.ExpressionResolver
import com.yandex.div.internal.parser.TypeHelper
import com.yandex.div.internal.parser.ValueValidator
import com.yandex.div.internal.parser.Converter
import com.yandex.div2.DivCustom
import com.yandex.divkit.demo.data.SharePref
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.div.CircularProgress.CircularProgressView
import com.yandex.divkit.demo.div.CircularProgress.DoubleCircularProgressView
import com.yandex.divkit.demo.div.offlineView.OfflineViewListAdapter
import com.yandex.divkit.demo.div.offlineView.VtReportAdapter
import com.yandex.divkit.demo.div.timerButton.TimerButton
import com.yandex.divkit.demo.div.labelledSliderView.LabelledSliderView
import com.yandex.divkit.demo.div.audioPlayerView.AudioPlayerView
import com.yandex.divkit.demo.div.audioRecorderView.AudioRecorderView
import com.yandex.divkit.demo.div.multiSelection.MultiSelectionView
import com.yandex.divkit.demo.div.singleSelection.SingleSelectionView
import com.yandex.divkit.demo.div.multiSelectionBottomSheet.MultiSelectionBottomSheetView
import com.yandex.divkit.demo.div.singleSelectionBottomSheet.SingleSelectionBottomSheetView
import com.yandex.divkit.demo.div.customInput.CustomInputView
import com.yandex.divkit.demo.div.notificationList.NotificationListView
import com.yandex.divkit.demo.div.faultDrivers.FaultDriversListView
import com.yandex.divkit.demo.div.treeview.TreeChartView
import com.yandex.divkit.demo.div.accidentSketch.AccidentSketchView
import com.yandex.divkit.demo.div.signatureView.SignatureView
import com.yandex.divkit.demo.div.imagePickerView.ImagePickerView
import com.yandex.divkit.demo.ui.LoadScreenListener
import com.yandex.divkit.demo.ui.UIDiv2ActionHandler
import com.yandex.divkit.demo.ui.activity.MehdiActivity
import com.yandex.divkit.demo.ui.bottomSheetDiv.BottomSheetDiv
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import android.util.Log
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.yandex.divkit.demo.div.treeview.StaticTreeView

class DemoCustomContainerAdapter(
    mehdiViewModel: MehdiViewModel? = null,
    lo: LifecycleOwner? = null,
    loadScreenListener: LoadScreenListener? = null
) : DivCustomContainerViewAdapter,
    OfflineViewListAdapter.OfflineViewListItemListener {
    private lateinit var mapStyle: Style
    private var map: MapboxMap? = null
    private var loadScreenListener = loadScreenListener

    init {
        android.util.Log.d(
            "DemoCustomContainerAdapter",
            "Constructor called with loadScreenListener: ${loadScreenListener != null}"
        )
        // Store loadScreenListener in NotificationManager so it can be accessed from NotificationListView
        loadScreenListener?.let {
            com.yandex.divkit.demo.div.notificationList.NotificationManager.setLoadScreenListener(it)
        }
    }

    private var lo = lo
    private var mehdiViewModel = mehdiViewModel
    private var current: Int = 0
    private var currentTest: String = ""
    private var total: Int = 0
    private var innerPercent: Int = 0
    private var outerPercent: Int = 0
    private var centerPercent: Int = 0
    private var innerColor: String = ""
    private var outerColor: String = ""
    private var seconds: Int = 0
    private var textSize: Int = 0
    private var counter: Int = 0
    private var timerButtonNext: String = ""
    private var timerButtonPath: String = ""
    private var timerButtonUploadRecordingId: String = ""
    private var timerButtonUploadPath: String = ""

    private val sourceId = "line-source"
    private lateinit var geoJsonSource: GeoJsonSource

    //    private var routeCoordinates = ArrayList<Point>()
    //    private var timerButtonParam1: String = ""
//    private var timerButtonParam2: String = ""
//    private var timerButtonParam3: String = ""
//    private var timerButtonParam4: String = ""
//    private var timerButtonParam5: String = ""
//    private var timerButtonParam6: String = ""
//    private var timerButtonParam7: String = ""
//    private var timerButtonParam8: String = ""
//    private var timerButtonParam9: String = ""
//    private var timerButtonParam10: String = ""
//    private var timerButtonParam11: String = ""
//    private var timerButtonParam12: String = ""
//    private var timerButtonParam13: String = ""
//    private var timerButtonParam14: String = ""
//    private var timerButtonParam15: String = ""
//    private var timerButtonParam16: String = ""
//    private var timerButtonParam17: String = ""
//    private var timerButtonParam18: String = ""
//    private var timerButtonParam19: String = ""
//    private var timerButtonParam20: String = ""
//    private var timerButtonParam21: String = ""
//    private var timerButtonParam22: String = ""
//    private var timerButtonParam23: String = ""
//    private var timerButtonParam24: String = ""
//    private var timerButtonParam25: String = ""
//    private var timerButtonParam26: String = ""
//    private var timerButtonParam27: String = ""
//    private var timerButtonParam28: String = ""
//    private var timerButtonParam29: String = ""
//    private var timerButtonParam30: String = ""
    private var paramList = mutableListOf<String>()

    private var systemForOffline: String = ""
    private var accidentIdForOffline: String = ""
    private var vehicleIdForOffline: String = ""
    private var offlineListDiv2View: Div2View? = null
    private var audioUrl: String = ""
    private lateinit var sharePref: SharePref
    private lateinit var adapter: VtReportAdapter

    private val factories = mapOf(
        "new_custom_card_1" to { context: Context -> context.createCustomCard() },
        "new_custom_card_2" to { context: Context -> context.createCustomCard() },
        "new_custom_container_1" to { context: Context -> context.createCustomContainer() },
        "map" to { context: Context -> context.createCustomMap() },
        "circular_progress" to { context: Context -> context.circularProgress() },
        "double_circular_progressView" to { context: Context -> context.doubleCircularProgressView() },
        "timer_button" to { context: Context -> context.timerButton() },
        "timer_button_call_service" to { context: Context -> context.timerButtonCallService() },
        "timer_button_upload_and_call_service" to { context: Context -> context.timerButtonUploadAndCallService() },
        "audioPlayerView" to { context: Context -> context.audioPlayerView() },
        "audioRecorderView" to { context: Context -> context.audioRecorderView() },
        "labelledSliderView" to { context: Context ->
            // This will be handled specially in createView method
            context.labelledSliderView("", "", "")
        },
        "multi_selection" to { context: Context ->
            // This will be handled specially in createView method
            context.createMultiSelection("", "", "", 0, "false", "true", "false")
        },
        "single_selection" to { context: Context ->
            // This will be handled specially in createView method
            context.createSingleSelection("", "", "", "false", "true", "false")
        },
        "multi_selection_bottomsheet" to { context: Context ->
            // This will be handled specially in createView method
            context.createMultiSelection("", "", "", 0, "false", "true", "false")
        },
        "single_selection_bottomsheet" to { context: Context ->
            // This will be handled specially in createView method
            context.createSingleSelection("", "", "", "false", "true", "false")
        },
        "notification_list" to { context: Context -> context.createNotificationList("") },
        "a" to { context: Context ->
            // Legacy alias — prefer custom_type "navigation"
            context.createNavigationMap("0", "0", "true")
        },
        "navigation" to { context: Context ->
            // Coordinates are applied in createView via custom_props
            context.createNavigationMap("0", "0", "true")
        },
        "signature" to { context: Context ->
            // This will be handled specially in createView method
            context.createSignatureView("")
        },
        "image_picker" to { context: Context ->
            context.createImagePickerView()
        },
        "accident_sketch" to { context: Context ->
            context.createAccidentSketchView("", "", "", previewMode = false)
        },
//        "offline_vt_reports_container" to { context: Context -> context.createOfflineVtReportsContainer() },
        "offline_list_container" to { context: Context ->
            throw IllegalStateException("offline_list_container must be created with Div2View context")
        },
        "fault_drivers_list" to { context: Context ->
            FaultDriversListView(context)
        },
    )

    override fun isCustomTypeSupported(type: String): Boolean =
        type in factories.keys ||
            type == "custom_input" ||
            type == "tree_view" ||
            type == "static_tree_view" ||
            type == "signature" ||
            type == "image_picker" ||
            type == "accident_sketch" ||
            type == "navigation" ||
            type == "fault_drivers_list"

    override fun release(view: View, div: DivCustom) = Unit

    override fun createView(
        div: DivCustom,
        divView: Div2View,
        expressionResolver: ExpressionResolver,
        path: DivStatePath
    ): View {
        if (div.customType == "circular_progress") {
            counter++
            current = evaluateCustomProp(div, "current", expressionResolver, 0)
            total = evaluateCustomProp(div, "total", expressionResolver, 0)
            textSize = evaluateCustomProp(div, "textSize", expressionResolver, 0)
        }
        if (div.customType == "double_circular_progressView") {
            innerPercent = evaluateCustomProp(div, "inner_percent", expressionResolver, 0)
            outerPercent = evaluateCustomProp(div, "outer_percent", expressionResolver, 0)
            centerPercent = evaluateCustomProp(div, "center_percent", expressionResolver, 0)
            innerColor = evaluateCustomPropString(
                div,
                "inner_color",
                expressionResolver,
                "#ff7816"
            ).toString()
            outerColor = evaluateCustomPropString(
                div,
                "outer_color",
                expressionResolver,
                "#ff7816"
            ).toString()
        }
        if (div.customType == "timer_button") {
            Log.d("DemoCustomContainer", "=== TIMER BUTTON DEBUG ===")
            Log.d("DemoCustomContainer", "div.customProps: ${div.customProps}")
            Log.d("DemoCustomContainer", "div.customType: ${div.customType}")

            seconds = evaluateCustomProp(div, "seconds", expressionResolver, 0)
            Log.d("DemoCustomContainer", "evaluateCustomProp result for 'seconds': $seconds")

            timerButtonNext = evaluateCustomPropString(div, "next_page", expressionResolver, "")
            Log.d(
                "DemoCustomContainer",
                "evaluateCustomPropString result for 'next_page': $timerButtonNext"
            )
            Log.d("DemoCustomContainer", "=== END TIMER BUTTON DEBUG ===")
        }
        if (div.customType == "timer_button_call_service") {
            Log.d("DemoCustomContainer", "=== TIMER BUTTON DEBUG ===")
            Log.d("DemoCustomContainer", "div.customProps: ${div.customProps}")
            Log.d("DemoCustomContainer", "div.customType: ${div.customType}")
            seconds = evaluateCustomProp(div, "seconds", expressionResolver, 0)
            Log.d("DemoCustomContainer", "evaluateCustomProp result for 'seconds': $seconds")
            timerButtonPath = evaluateCustomPropString(div, "path", expressionResolver, "")
            for (i in 1..200)
                paramList.add(evaluateCustomPropString(div, "param$i", expressionResolver, ""))
            Log.d(
                "DemoCustomContainer",
                "evaluateCustomPropString result for 'next_page': $timerButtonNext"
            )
            Log.d("DemoCustomContainer", "=== END TIMER BUTTON DEBUG ===")
        }
        if (div.customType == "timer_button_upload_and_call_service") {
            Log.d("DemoCustomContainer", "=== TIMER BUTTON UPLOAD AND CALL SERVICE DEBUG ===")
            Log.d("DemoCustomContainer", "div.customProps: ${div.customProps}")
            Log.d("DemoCustomContainer", "div.customType: ${div.customType}")
            seconds = evaluateCustomProp(div, "seconds", expressionResolver, 0)
            Log.d("DemoCustomContainer", "evaluateCustomProp result for 'seconds': $seconds")
            timerButtonUploadRecordingId =
                evaluateCustomPropString(div, "recording_id", expressionResolver, "")
            timerButtonUploadPath = evaluateCustomPropString(div, "path", expressionResolver, "")
            for (i in 1..200)
                paramList.add(evaluateCustomPropString(div, "param$i", expressionResolver, ""))
            Log.d("DemoCustomContainer", "Recording ID: $timerButtonUploadRecordingId")
            Log.d("DemoCustomContainer", "Path: $timerButtonUploadPath")
            Log.d("DemoCustomContainer", "=== END TIMER BUTTON UPLOAD AND CALL SERVICE DEBUG ===")
        }
        if (div.customType == "offline_list_container") {
            systemForOffline = evaluateCustomPropString(div, "screen", expressionResolver, "")
            accidentIdForOffline = evaluateCustomPropString(div, "accident_id", expressionResolver, "")
            vehicleIdForOffline = evaluateCustomPropString(div, "vehicle_id", expressionResolver, "")
        }
        if (div.customType == "audioPlayerView") {
            audioUrl = evaluateCustomPropString(div, "url", expressionResolver, "")
        }
        if (div.customType == "navigation") {
            // Navigation parameters will be handled in createView
        }
        if (div.customType == "notification_list") {
            // Notification list type filter will be handled in createView
        }
        if (div.customType == "signature") {
            // Signature variable name will be handled in createView
        }
        // labelledSliderView and multi_selection are now handled specially in the customView creation
        val customView = if (div.customType == "custom_input") {
            // Handle custom_input specially to ensure unique variable names
            val currentInputVariableName =
                evaluateCustomPropString(div, "variable_name", expressionResolver, "")
            val currentInputHint = evaluateCustomPropString(div, "hint", expressionResolver, "")
            val currentInputType =
                evaluateCustomPropString(div, "input_type", expressionResolver, "text")
            val currentInputValidation =
                evaluateCustomPropString(div, "validation", expressionResolver, "false")
            val currentInputEnabled =
                evaluateCustomPropString(div, "enabled", expressionResolver, "true")
            val currentInputMin =
                evaluateCustomPropString(div, "min", expressionResolver, "")
            val currentInputMax =
                evaluateCustomPropString(div, "max", expressionResolver, "")
            divView.context.createCustomInput(
                currentInputVariableName,
                currentInputHint,
                currentInputType,
                currentInputValidation,
                currentInputEnabled,
                currentInputMin,
                currentInputMax
            )
        } else if (div.customType == "labelledSliderView") {
            // Handle labelledSliderView specially to pass parameters directly
            val currentLabelList =
                evaluateCustomPropString(div, "label_list", expressionResolver, "")
            val currentLabelId =
                evaluateCustomPropString(div, "label_id_list", expressionResolver, "")
            val currentLabelVariable =
                evaluateCustomPropString(div, "variable_name", expressionResolver, "")
            divView.context.labelledSliderView(
                currentLabelList,
                currentLabelId,
                currentLabelVariable
            )
        } else if (div.customType == "multi_selection") {
            // Handle multi_selection specially to pass parameters directly
            val currentMultiOptions =
                evaluateCustomPropString(div, "options", expressionResolver, "")
            val currentMultiIds = evaluateCustomPropString(div, "ids", expressionResolver, "")
            val currentMultiVariableName =
                evaluateCustomPropString(div, "variable_name", expressionResolver, "")
            val currentMultiSelectionLimit =
                evaluateCustomProp(div, "selection_limit", expressionResolver, 0)
            val currentMultiValidation =
                evaluateCustomPropString(div, "validation", expressionResolver, "false")
            val currentMultiEnabled =
                evaluateCustomPropString(div, "enabled", expressionResolver, "true")
            val currentMultiSearchable =
                evaluateCustomPropString(div, "searchable", expressionResolver, "false")
            divView.context.createMultiSelection(
                currentMultiOptions,
                currentMultiIds,
                currentMultiVariableName,
                currentMultiSelectionLimit,
                currentMultiValidation,
                currentMultiEnabled,
                currentMultiSearchable
            )
        } else if (div.customType == "single_selection") {
            // Handle single_selection specially to pass parameters directly
            val currentSingleOptions =
                evaluateCustomPropString(div, "options", expressionResolver, "")
            val currentSingleIds = evaluateCustomPropString(div, "ids", expressionResolver, "")
            val currentSingleVariableName =
                evaluateCustomPropString(div, "variable_name", expressionResolver, "")
            val currentSingleValidation =
                evaluateCustomPropString(div, "validation", expressionResolver, "false")
            val currentSingleEnabled =
                evaluateCustomPropString(div, "enabled", expressionResolver, "true")
            val currentSingleSearchable =
                evaluateCustomPropString(div, "searchable", expressionResolver, "false")
            divView.context.createSingleSelection(
                currentSingleOptions,
                currentSingleIds,
                currentSingleVariableName,
                currentSingleValidation,
                currentSingleEnabled,
                currentSingleSearchable
            )
        } else if (div.customType == "multi_selection_bottomsheet") {
            // Handle multi_selection_bottomsheet
            val currentMultiOptions = evaluateCustomPropString(div, "options", expressionResolver, "")
            val currentMultiIds = evaluateCustomPropString(div, "ids", expressionResolver, "")
            val currentMultiVariableName = evaluateCustomPropString(div, "variable_name", expressionResolver, "")
            val currentMultiTitle = evaluateCustomPropString(div, "title", expressionResolver, "انتخاب موارد")
            val currentMultiSelectionLimit = evaluateCustomProp(div, "selection_limit", expressionResolver, 0)
            val currentMultiValidation = evaluateCustomPropString(div, "validation", expressionResolver, "false")
            val currentMultiEnabled = evaluateCustomPropString(div, "enabled", expressionResolver, "true")
            val currentMultiSearchable = evaluateCustomPropString(div, "searchable", expressionResolver, "false")
            divView.context.createMultiSelectionBottomSheet(
                currentMultiOptions,
                currentMultiIds,
                currentMultiVariableName,
                currentMultiTitle,
                currentMultiSelectionLimit,
                currentMultiValidation,
                currentMultiEnabled,
                currentMultiSearchable
            )
        } else if (div.customType == "single_selection_bottomsheet") {
            // Handle single_selection_bottomsheet
            val currentSingleOptions = evaluateCustomPropString(div, "options", expressionResolver, "")
            val currentSingleIds = evaluateCustomPropString(div, "ids", expressionResolver, "")
            val currentSingleVariableName = evaluateCustomPropString(div, "variable_name", expressionResolver, "")
            val currentSingleTitle =
                evaluateCustomPropString(div, "title", expressionResolver, "انتخاب یک مورد")
            val currentSingleValidation =
                evaluateCustomPropString(div, "validation", expressionResolver, "false")
            val currentSingleEnabled =
                evaluateCustomPropString(div, "" +
                        "" +
                        "", expressionResolver, "true")
            val currentSingleSearchable =
                evaluateCustomPropString(div, "searchable", expressionResolver, "false")
            divView.context.createSingleSelectionBottomSheet(
                currentSingleOptions,
                currentSingleIds,
                currentSingleVariableName,
                currentSingleTitle,
                currentSingleValidation,
                currentSingleEnabled,
                currentSingleSearchable
            )
        }
        else if (div.customType == "tree_view") {
            val servicePath = evaluateCustomPropString(div, "service_path", expressionResolver, "")
            val parentParam = evaluateCustomPropString(div, "parent_param", expressionResolver, "parent_id")
            val tokenValue = evaluateCustomPropString(div, "token", expressionResolver, "empty")
            val rootId = evaluateCustomPropString(div, "root_id", expressionResolver, "root")
            val rootLabel = evaluateCustomPropString(div, "root_label", expressionResolver, "Root")
            val rootHasChildren = evaluateCustomPropString(div, "root_has_children", expressionResolver, "true")
                    .equals("true", ignoreCase = true)
            val dataKeyPrefix = evaluateCustomPropString(div, "data_key_prefix", expressionResolver, "tree_nodes_")
            val autoExpandRoot = evaluateCustomPropString(div, "auto_expand_root", expressionResolver, "true")
                    .equals("true", ignoreCase = true)
            val extraParamsRaw = evaluateCustomPropString(div, "extra_params", expressionResolver, "")
            val extraParams = parseKeyValuePairs(extraParamsRaw)

            val config = TreeChartView.Config(
                rootId = rootId,
                rootLabel = rootLabel,
                rootHasChildren = rootHasChildren,
                servicePath = servicePath,
                parentParamName = parentParam,
                token = tokenValue,
                extraParams = extraParams,
                dataKeyPrefix = dataKeyPrefix,
                autoExpandRoot = autoExpandRoot
            )

            divView.context.createTreeView(config, mehdiViewModel, lo, loadScreenListener)
        }
        else if (div.customType == "static_tree_view")
        {
            var json="{\n" +
                    "  \"tree\": {\n" +
                    "    \"Id\": 1,\n" +
                    "    \"parentId\": 0,\n" +
                    "    \"name\": \"مديريت تصادفات كشور\",\n" +
                    "    \"code\": \"1\",\n" +
                    "    \"childs\": [\n" +
                    "      {\n" +
                    "        \"Id\": 6,\n" +
                    "        \"parentId\": 1,\n" +
                    "        \"name\": \"ناجا\",\n" +
                    "        \"code\": \"9\",\n" +
                    "        \"childs\": [\n" +
                    "          {\n" +
                    "            \"Id\": 2,\n" +
                    "            \"parentId\": 6,\n" +
                    "            \"name\": \"پليس راهور ناجا\",\n" +
                    "            \"code\": \"19\",\n" +
                    "            \"childs\": [\n" +
                    "              {\n" +
                    "                \"Id\": 5,\n" +
                    "                \"parentId\": 2,\n" +
                    "                \"name\": \"پليس ترافيك شهري\",\n" +
                    "                \"code\": \"5\",\n" +
                    "                \"childs\": [\n" +
                    "                  {\n" +
                    "                    \"Id\": 1021,\n" +
                    "                    \"parentId\": 5,\n" +
                    "                    \"name\": \"تهران بزرگ _ پليس راهور استان\",\n" +
                    "                    \"code\": \"400\",\n" +
                    "                    \"childs\": [\n" +
                    "                      {\n" +
                    "                        \"Id\": 1478,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه1 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4001\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1479,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه2 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4002\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1480,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه3 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4003\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1481,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه4 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4004\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1482,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه5 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4005\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1483,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه6 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4006\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1484,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه7 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4007\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1485,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه8 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4008\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1486,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه9 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4009\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1487,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه10 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4010\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1488,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه11 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4011\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1489,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه12 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4012\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1490,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه13 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4013\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1491,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 14 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4014\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1492,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 15 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4015\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1697,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 16 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4016\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1698,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 17راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4017\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1699,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 18 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4018\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1700,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 19 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4019\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1701,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 20 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4020\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1702,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 21راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4021\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1703,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 22راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4022\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 1706,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 23 راهور تهران بزرگ(ستاد كنترل ترافيك بزرگ راهها)\",\n" +
                    "                        \"code\": \"4023\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 2940,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"فرماندهي يگان عمليات\",\n" +
                    "                        \"code\": \"4051\",\n" +
                    "                        \"childs\": [\n" +
                    "                          {\n" +
                    "                            \"Id\": 2960,\n" +
                    "                            \"parentId\": 2940,\n" +
                    "                            \"name\": \"خطوط ويژه\",\n" +
                    "                            \"code\": \"405101\"\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"Id\": 2961,\n" +
                    "                            \"parentId\": 2940,\n" +
                    "                            \"name\": \"گروه ضربت\",\n" +
                    "                            \"code\": \"405102\"\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"Id\": 2962,\n" +
                    "                            \"parentId\": 2940,\n" +
                    "                            \"name\": \"طرح ترافيك\",\n" +
                    "                            \"code\": \"405103\"\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"Id\": 2963,\n" +
                    "                            \"parentId\": 2940,\n" +
                    "                            \"name\": \"طرح توقيف\",\n" +
                    "                            \"code\": \"405104\"\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"Id\": 2964,\n" +
                    "                            \"parentId\": 2940,\n" +
                    "                            \"name\": \"طرح اضطرار\",\n" +
                    "                            \"code\": \"405105\"\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"Id\": 81417,\n" +
                    "                            \"parentId\": 2940,\n" +
                    "                            \"name\": \"رسوبي ثبت نشده\",\n" +
                    "                            \"code\": \"45106\"\n" +
                    "                          }\n" +
                    "                        ]\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 70300,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"گشت شب\",\n" +
                    "                        \"code\": \"4030\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 71920,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 24 راهور تهران بزرگ(ستاد كنترل ترافيك بزرگ راهها)\",\n" +
                    "                        \"code\": \"4024\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 71921,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 25 راهور تهران بزرگ(ستاد كنترل ترافيك بزرگ راهها)\",\n" +
                    "                        \"code\": \"4025\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 73320,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"كنترل نامحسوس\",\n" +
                    "                        \"code\": \"405106\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 74540,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 26 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4026\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 80611,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 28 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4028\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 80815,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 27 راهور تهران بزرگ\",\n" +
                    "                        \"code\": \"4027\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"Id\": 85036,\n" +
                    "                        \"parentId\": 1021,\n" +
                    "                        \"name\": \"منطقه 29 تهران بزرگ\",\n" +
                    "                        \"code\": \"4029\"\n" +
                    "                      }\n" +
                    "                    ]\n" +
                    "                  }\n" +
                    "                ]\n" +
                    "              }\n" +
                    "            ]\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}\n"
            val fromProp = evaluateCustomPropString(div, "json_data", expressionResolver, "")
            val fromDb = mehdiViewModel?.getValueByKey("org_tree_json")?.value.orEmpty()
            val jsonData = when {
                fromProp.isNotBlank() -> fromProp
                fromDb.isNotBlank() -> fromDb
                else -> json
            }
            // Cache org tree so submit can resolve policeCenterID from selection.
            runCatching {
                mehdiViewModel?.insertItemToDbSync(
                    com.yandex.divkit.demo.data.entities.PhPlusDB(null, "org_tree_json", jsonData)
                )
            }
            val rootKey = evaluateCustomPropString(div, "root_key", expressionResolver, "tree")
            val idKey = evaluateCustomPropString(div, "id_key", expressionResolver, "Id")
            val parentIdKey = evaluateCustomPropString(div, "parent_id_key", expressionResolver, "parentId")
            val nameKey = evaluateCustomPropString(div, "name_key", expressionResolver, "name")
            val codeKey = evaluateCustomPropString(div, "code_key", expressionResolver, "code")
            val childsKey = evaluateCustomPropString(div, "childs_key", expressionResolver, "childs")
            val autoExpandRootStr = evaluateCustomPropString(div, "auto_expand_root", expressionResolver, "true")
            val autoExpandSingleChildStr = evaluateCustomPropString(
                div, "auto_expand_single_child", expressionResolver, "true"
            )
            val selectionModeStr = evaluateCustomPropString(div, "selection_mode", expressionResolver, "none")
            val nameVariable = evaluateCustomPropString(div, "name_variable", expressionResolver, "")
                .ifEmpty { evaluateCustomPropString(div, "title_variable", expressionResolver, "") }
            val codeVariable = evaluateCustomPropString(div, "code_variable", expressionResolver, "")
            val parentIdVariable = evaluateCustomPropString(div, "parent_id_variable", expressionResolver, "")
            // Selected node's own Id (police center), distinct from parent_id_variable.
            val idVariable = evaluateCustomPropString(div, "id_variable", expressionResolver, "")
            val confirmText = evaluateCustomPropString(div, "confirm_text", expressionResolver, "تایید")
            val confirmUrl = evaluateCustomPropString(div, "confirm_url", expressionResolver, "")
            val clearVariablesOnConfirm = evaluateCustomPropString(
                div, "clear_variables_on_confirm", expressionResolver, ""
            )
            val showConfirmStr = evaluateCustomPropString(div, "show_confirm_button", expressionResolver, "")
            val variableTarget = evaluateCustomPropString(div, "variable_target", expressionResolver, "base")
            val dismissOnConfirm = !evaluateCustomPropString(
                div, "dismiss_on_confirm", expressionResolver, "true"
            ).equals("false", ignoreCase = true)

            // Parse boolean
            val autoExpandRoot = autoExpandRootStr.equals("true", ignoreCase = true)
            val autoExpandSingleChild = autoExpandSingleChildStr.equals("true", ignoreCase = true)
            val showConfirm = when {
                showConfirmStr.equals("true", ignoreCase = true) -> true
                showConfirmStr.equals("false", ignoreCase = true) -> false
                else -> nameVariable.isNotEmpty() || codeVariable.isNotEmpty() ||
                    parentIdVariable.isNotEmpty() || idVariable.isNotEmpty() ||
                    confirmUrl.isNotEmpty()
            }

            // Parse selection mode
            val selectionMode = when (selectionModeStr.lowercase()) {
                "single" -> StaticTreeView.SelectionMode.SINGLE
                "multi" -> StaticTreeView.SelectionMode.MULTI
                else -> if (showConfirm) StaticTreeView.SelectionMode.SINGLE else StaticTreeView.SelectionMode.NONE
            }

            // Build config
            val config = StaticTreeView.Config(
                rootKey = rootKey,
                idKey = idKey,
                parentIdKey = parentIdKey,
                nameKey = nameKey,
                codeKey = codeKey,
                childsKey = childsKey,
                autoExpandRoot = autoExpandRoot,
                autoExpandSingleChild = autoExpandSingleChild,
                selectionMode = selectionMode
            )

            divView.context.createStaticTreeView(
                jsonData = jsonData,
                config = config,
                nameVariable = nameVariable,
                codeVariable = codeVariable,
                parentIdVariable = parentIdVariable,
                idVariable = idVariable,
                confirmText = confirmText,
                confirmUrl = confirmUrl,
                clearVariablesOnConfirm = clearVariablesOnConfirm,
                showConfirmButton = showConfirm,
                divView = divView,
                variableTarget = variableTarget,
                dismissOnConfirm = dismissOnConfirm,
                loadScreenListener = loadScreenListener,
            )
        }
        else if (div.customType == "navigation") {
            // Handle navigation specially to pass destination coordinates
            val destinationLat = evaluateCustomPropString(div, "lat", expressionResolver, "0")
            val destinationLon = evaluateCustomPropString(div, "lon", expressionResolver, "0")
            val routingRequired = evaluateCustomPropString(div, "routingRequired", expressionResolver, "true")
            divView.context.createNavigationMap(destinationLat, destinationLon, routingRequired)
        } else if (div.customType == "notification_list") {
            // Handle notification_list specially to pass type/title filters
            val notificationType = evaluateCustomPropString(div, "type", expressionResolver, "")
            val notificationTitle = evaluateCustomPropString(div, "title", expressionResolver, "")
            divView.context.createNotificationList(notificationType, notificationTitle)
        } else if (div.customType == "signature") {
            // Handle signature specially to pass variable name
            val variableName = evaluateCustomPropString(div, "variable_name", expressionResolver, "")
            divView.context.createSignatureView(variableName)
        } else if (div.customType == "image_picker") {
            divView.context.createImagePickerView().also { picker ->
                bindImagePicker(picker, div, expressionResolver)
            }
        } else if (div.customType == "accident_sketch") {
            val jsonVariable = evaluateCustomPropString(div, "variable_name", expressionResolver, "")
            val imageVariable = evaluateCustomPropString(div, "image_variable", expressionResolver, jsonVariable)
            val soundVariable = evaluateCustomPropString(div, "sound_variable", expressionResolver, "")
            val previewMode = evaluateCustomPropString(div, "preview_mode", expressionResolver, "false")
                .equals("true", ignoreCase = true)
            divView.context.createAccidentSketchView(jsonVariable, imageVariable, soundVariable, previewMode)
        } else if (div.customType == "offline_list_container") {
            offlineListDiv2View = divView
            divView.context.createOfflineListContainer(divView)
        } else if (div.customType == "fault_drivers_list") {
            val accidentId = evaluateCustomPropString(div, "accident_id", expressionResolver, "")
            FaultDriversListView(divView.context).apply {
                bind(mehdiViewModel, accidentId)
            }
        } else {
            factories[div.customType]?.invoke(divView.context)
                ?: throw IllegalStateException("Can not create view for unsupported custom type ${div.customType}")
        }
        if (div.customType == "new_custom_container_1" && div.items != null) {
            div.items!!.forEach {
                val childDivView = getDivChildFactory(divView).createChildView(
                    it,
                    path,
                    divView
                )
                (customView as ViewGroup).addView(childDivView)
            }
        }

        // Apply visibility and enabled properties to all custom views
        applyVisibilityAndEnabled(customView, div, expressionResolver)

        return customView
    }

    /**
     * Apply visibility and enabled properties to a custom view
     */
    private fun applyVisibilityAndEnabled(
        view: View,
        div: DivCustom,
        expressionResolver: ExpressionResolver
    ) {
        // Get visibility property (default: "visible")
        val visibilityValue = evaluateCustomPropString(div, "visibility", expressionResolver, "visible")
        
        // Apply visibility
        when (visibilityValue.lowercase()) {
            "gone" -> view.visibility = View.GONE
            "visible" -> view.visibility = View.VISIBLE
            "invisible" -> view.visibility = View.INVISIBLE
            else -> view.visibility = View.VISIBLE // Default to visible
        }
        
        // Get enabled property (default: "true")
        val enabledValue = evaluateCustomPropString(div, "enabled", expressionResolver, "true")
        val isEnabled = enabledValue.equals("true", ignoreCase = true)
        
        // Apply enabled state
        view.isEnabled = isEnabled
        
        // For views that support alpha (to show disabled state visually)
        if (!isEnabled) {
            view.alpha = 0.5f // Make it look disabled
        } else {
            view.alpha = 1.0f // Full opacity when enabled
        }
        
        // For specific view types that have their own enabled/disabled methods
        // These views might have additional logic in their setEnabled methods
        when (view) {
            is CustomInputView -> view.setEnabled(isEnabled)
            is LabelledSliderView -> view.setEnabled(isEnabled)
            is MultiSelectionView -> view.setEnabled(isEnabled)
            is SingleSelectionView -> view.setEnabled(isEnabled)
            is MultiSelectionBottomSheetView -> view.setEnabled(isEnabled)
            is SingleSelectionBottomSheetView -> view.setEnabled(isEnabled)
            // Other view types will use the default isEnabled property set above
        }
    }

    /**
     * Helper function to evaluate custom_props values with variable substitution
     */
    private fun evaluateCustomProp(
        div: DivCustom,
        key: String,
        expressionResolver: ExpressionResolver,
        defaultValue: Int
    ): Int {
        Log.d("DemoCustomContainer", "evaluateCustomProp called with key: '$key'")
        Log.d(
            "DemoCustomContainer",
            "div.customProps type: ${div.customProps?.javaClass?.simpleName}"
        )
        Log.d("DemoCustomContainer", "div.customProps content: ${div.customProps}")

        // Safely get the raw value from customProps
        var rawValue: Any? = null

        try {
            // Check if customProps is a JSONObject
            if (div.customProps is org.json.JSONObject) {
                val jsonObject = div.customProps as org.json.JSONObject

                // First try the exact key using opt() to avoid exceptions
                rawValue = jsonObject.opt(key)
                Log.d("DemoCustomContainer", "Tried key '$key' with opt(), got: $rawValue")

                // If not found, try with $ prefix (for template variables)
                if (rawValue == null || rawValue == org.json.JSONObject.NULL) {
                    rawValue = jsonObject.opt("$$key")
                    Log.d("DemoCustomContainer", "Tried key '\$$key' with opt(), got: $rawValue")
                }
            } else {
                // Fallback to Map-like access
                rawValue = div.customProps?.get(key)
                Log.d("DemoCustomContainer", "Tried key '$key' with get(), got: $rawValue")

                if (rawValue == null) {
                    rawValue = div.customProps?.get("$$key")
                    Log.d("DemoCustomContainer", "Tried key '\$$key' with get(), got: $rawValue")
                }
            }
        } catch (e: Exception) {
            // If there's an exception accessing customProps, return default value
            Log.e(
                "DemoCustomContainer",
                "Exception accessing customProps for key '$key': ${e.message}"
            )
            return defaultValue
        }

        Log.d(
            "DemoCustomContainer",
            "Processing rawValue: $rawValue (type: ${rawValue?.javaClass?.simpleName})"
        )

        return when (rawValue) {
            is String -> {
                Log.d("DemoCustomContainer", "rawValue is String: '$rawValue'")
                if (rawValue.startsWith("@{") && rawValue.endsWith("}")) {
                    Log.d("DemoCustomContainer", "rawValue is a variable expression")
                    try {
                        // Extract the variable name from @{variable_name}
                        val variableName = rawValue.substring(2, rawValue.length - 1)
                        Log.d("DemoCustomContainer", "Extracted variable name: '$variableName'")

                        // Create a proper expression string and evaluate it
                        val expressionString = "@{$variableName}"
                        val evaluable = com.yandex.div.evaluable.Evaluable.lazy(expressionString)
                        Log.d(
                            "DemoCustomContainer",
                            "Created evaluable for expression: '$expressionString'"
                        )

                        // Use the expression resolver to get the value
                        val result = expressionResolver.get<Any, Any>(
                            key,
                            expressionString,
                            evaluable,
                            null,
                            object : ValueValidator<Any> {
                                override fun isValid(value: Any): Boolean = true
                            },
                            object : TypeHelper<Any> {
                                override fun isTypeValid(value: Any): Boolean = true
                                override val typeDefault: Any = defaultValue
                            },
                            ParsingErrorLogger.LOG
                        )

                        Log.d(
                            "DemoCustomContainer",
                            "Expression resolver result: $result (type: ${result?.javaClass?.simpleName})"
                        )

                        val finalResult = when (result) {
                            is Number -> result.toInt()
                            is String -> result.toIntOrNull() ?: defaultValue
                            else -> defaultValue
                        }
                        Log.d("DemoCustomContainer", "Final result: $finalResult")
                        finalResult
                    } catch (e: Exception) {
                        Log.e(
                            "DemoCustomContainer",
                            "Exception evaluating expression: ${e.message}"
                        )
                        // If evaluation fails, try to parse as integer directly
                        val fallbackResult = rawValue.toIntOrNull() ?: defaultValue
                        Log.d("DemoCustomContainer", "Fallback result: $fallbackResult")
                        fallbackResult
                    }
                } else {
                    // Check if this might be a direct variable name (not wrapped in @{})
                    Log.d(
                        "DemoCustomContainer",
                        "rawValue is not a variable expression, checking if it's a direct variable name"
                    )
                    try {
                        // Try to evaluate it as a direct variable name
                        val expressionString = "@{$rawValue}"
                        val evaluable = com.yandex.div.evaluable.Evaluable.lazy(expressionString)
                        Log.d(
                            "DemoCustomContainer",
                            "Trying to evaluate as direct variable: '$expressionString'"
                        )

                        val result = expressionResolver.get<Any, Any>(
                            key,
                            expressionString,
                            evaluable,
                            null,
                            object : ValueValidator<Any> {
                                override fun isValid(value: Any): Boolean = true
                            },
                            object : TypeHelper<Any> {
                                override fun isTypeValid(value: Any): Boolean = true
                                override val typeDefault: Any = defaultValue
                            },
                            ParsingErrorLogger.LOG
                        )

                        Log.d(
                            "DemoCustomContainer",
                            "Direct variable evaluation result: $result (type: ${result?.javaClass?.simpleName})"
                        )

                        val finalResult = when (result) {
                            is Number -> result.toInt()
                            is String -> result.toIntOrNull() ?: defaultValue
                            else -> defaultValue
                        }
                        Log.d(
                            "DemoCustomContainer",
                            "Final result from direct variable: $finalResult"
                        )
                        finalResult
                    } catch (e: Exception) {
                        Log.e(
                            "DemoCustomContainer",
                            "Exception evaluating direct variable: ${e.message}"
                        )
                        // If evaluation fails, try to parse as integer directly
                        val result = rawValue.toIntOrNull() ?: defaultValue
                        Log.d("DemoCustomContainer", "Parsed integer result: $result")
                        result
                    }
                }
            }

            is Number -> {
                val result = rawValue.toInt()
                Log.d("DemoCustomContainer", "rawValue is Number, converted to int: $result")
                result
            }

            else -> {
                Log.d(
                    "DemoCustomContainer",
                    "rawValue is neither String nor Number, using default: $defaultValue"
                )
                defaultValue
            }
        }
    }

    /**
     * Helper function to evaluate custom_props string values with variable substitution
     */
    private fun evaluateCustomPropString(
        div: DivCustom,
        key: String,
        expressionResolver: ExpressionResolver,
        defaultValue: String
    ): String {
        // Safely get the raw value from customProps
        var rawValue: Any? = null

        try {
            // Check if customProps is a JSONObject
            if (div.customProps is org.json.JSONObject) {
                val jsonObject = div.customProps as org.json.JSONObject

                // First try the exact key using opt() to avoid exceptions
                rawValue = jsonObject.opt(key)

                // If not found, try with $ prefix (for template variables)
                if (rawValue == null || rawValue == org.json.JSONObject.NULL) {
                    rawValue = jsonObject.opt("$$key")
                }
            } else {
                // Fallback to Map-like access
                rawValue = div.customProps?.get(key)

                if (rawValue == null) {
                    rawValue = div.customProps?.get("$$key")
                }
            }
        } catch (e: Exception) {
            // If there's an exception accessing customProps, return default value
            return defaultValue
        }

        return when (rawValue) {
            is String -> {
                val stringValue = rawValue as String
                // Check if string contains variable expressions @{...}
                if (stringValue.contains("@{")) {
                    try {
                        // Use regex to find all @{variable_name} patterns
                        val variablePattern = Regex("@\\{([^}]+)\\}")
                        var result = stringValue

                        // Find all matches and replace them
                        variablePattern.findAll(stringValue).forEach { matchResult ->
                            val fullMatch = matchResult.value // e.g., "@{my_variable}"
                            val variableName = matchResult.groupValues[1] // e.g., "my_variable"

                            try {
                                // Create expression and evaluate it
                                val expressionString = "@{$variableName}"
                                val evaluable =
                                    com.yandex.div.evaluable.Evaluable.lazy(expressionString)

                                // Get the variable value (using same pattern as original code)
                                val variableValue = expressionResolver.get<Any, Any>(
                                    "${key}_${variableName}",
                                    expressionString,
                                    evaluable,
                                    null,
                                    object : ValueValidator<Any> {
                                        override fun isValid(value: Any): Boolean = true
                                    },
                                    object : TypeHelper<Any> {
                                        override fun isTypeValid(value: Any): Boolean = true
                                        override val typeDefault: Any = ""
                                    },
                                    ParsingErrorLogger.LOG
                                )

                                // Convert to string (handle any type like the original code)
                                val evaluatedStringValue = variableValue?.toString() ?: ""
                                result = result.replace(fullMatch, evaluatedStringValue)

                            } catch (e: Exception) {
                                // If individual variable evaluation fails, keep the original expression
                                // This allows partial interpolation to work
                            }
                        }

                        result
                    } catch (e: Exception) {
                        // If overall evaluation fails, return the raw value
                        stringValue
                    }
                } else {
                    // No variable expressions, return as-is
                    stringValue
                }
            }

            is Boolean, is Number -> rawValue.toString()
            else -> defaultValue
        }
    }

    private fun hasCustomProp(div: DivCustom, key: String): Boolean {
        return try {
            val props = div.customProps
            if (props is org.json.JSONObject) {
                props.has(key) || props.has("$$key")
            } else {
                props?.get(key) != null || props?.get("$$key") != null
            }
        } catch (_: Exception) {
            false
        }
    }

    override fun bindView(
        customView: View,
        div: DivCustom,
        divView: Div2View,
        expressionResolver: ExpressionResolver,
        path: DivStatePath
    ) {
        // Apply visibility and enabled properties to all custom views
        applyVisibilityAndEnabled(customView, div, expressionResolver)
        
        when (div.customType) {
            "image_picker" -> {
                (customView as? ImagePickerView)?.let { picker ->
                    bindImagePicker(picker, div, expressionResolver)
                }
            }
            "accident_sketch" -> {
                (customView as? AccidentSketchView)?.let { sketch ->
                    val jsonVariable = evaluateCustomPropString(div, "variable_name", expressionResolver, "")
                    val imageVariable = evaluateCustomPropString(div, "image_variable", expressionResolver, jsonVariable)
                    val soundVariable = evaluateCustomPropString(div, "sound_variable", expressionResolver, "")
                    val previewMode = evaluateCustomPropString(div, "preview_mode", expressionResolver, "false")
                        .equals("true", ignoreCase = true)
                    sketch.configure(jsonVariable, imageVariable, soundVariable)
                    sketch.setPreviewMode(previewMode)
                    val vars = Div2ViewVariableExporter.captureVariablesMap(divView)
                    val jsonVal = vars[jsonVariable]?.toString()
                    val imageVal = vars[imageVariable]?.toString()
                    val soundVal = vars[soundVariable]?.toString()
                    android.util.Log.d("AccidentSketchBind", "bindView: preview=$previewMode jsonVar=$jsonVariable imageVar=$imageVariable jsonLen=${jsonVal?.length ?: -1} imageLen=${imageVal?.length ?: -1} soundLen=${soundVal?.length ?: -1}")
                    sketch.setSketchData(jsonVal, imageVal, soundVal)
                }
            }
            "new_custom_container_1" -> {
                if (div.items != null && customView is ViewGroup) {
                    if (div.items!!.size != customView.childCount) {
                        throw IllegalStateException("Custom view childCount not equal to div child count! Div type is ${div.customType}")
                    }
                    for (i in div.items!!.indices) {
                        val childDivView = customView.getChildAt(i)
                        val childDiv = div.items!![i]
                        getDivChildFactory(divView).bindChildView(
                            childDivView,
                            i,
                            childDiv,
                            path,
                            divView,
                            expressionResolver,
                        )
                    }
                }
            }
            "fault_drivers_list" -> {
                val accidentId = evaluateCustomPropString(div, "accident_id", expressionResolver, "")
                (customView as? FaultDriversListView)?.bind(mehdiViewModel, accidentId)
            }
            "notification_list" -> {
                // Re-set listeners and type filter when view is rebound (after screen navigation)
                android.util.Log.d("DemoCustomContainerAdapter", "=== bindView called for notification_list ===")
                (customView as? com.yandex.divkit.demo.div.notificationList.NotificationListView)?.apply {
                    android.util.Log.d("DemoCustomContainerAdapter", "Configuring listeners in bindView")
                    
                    // Try to get actionHandler from context (MehdiActivity) and update NotificationManager
                    try {
                        val activity = divView.context as? android.app.Activity
                        if (activity is com.yandex.divkit.demo.ui.activity.MehdiActivity) {
                            val actionHandlerFromActivity = activity.getActionHandler()
                            if (actionHandlerFromActivity != null) {
                                com.yandex.divkit.demo.div.notificationList.NotificationManager.setActionHandler(actionHandlerFromActivity)
                                android.util.Log.d("DemoCustomContainerAdapter", "ActionHandler updated from MehdiActivity in bindView")
                            } else {
                                android.util.Log.w("DemoCustomContainerAdapter", "ActionHandler from MehdiActivity is null")
                            }
                        } else {
                            android.util.Log.w("DemoCustomContainerAdapter", "Context is not MehdiActivity")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DemoCustomContainerAdapter", "Error getting actionHandler from context", e)
                        // Check if actionHandler is already set in NotificationManager
                        val existingActionHandler = com.yandex.divkit.demo.div.notificationList.NotificationManager.getActionHandler()
                        if (existingActionHandler == null) {
                            android.util.Log.w("DemoCustomContainerAdapter", "ActionHandler is null in NotificationManager and cannot be updated")
                        } else {
                            android.util.Log.d("DemoCustomContainerAdapter", "ActionHandler already exists in NotificationManager")
                        }
                    }
                    
                    // Configure listeners with references so they can be recreated in onAttachedToWindow
                    configureListeners(loadScreenListener, mehdiViewModel)
                    
                    // Update type/title filters from custom_props
                    val notificationType = evaluateCustomPropString(div, "type", expressionResolver, "")
                    setTypeFilter(notificationType)
                    val notificationTitle = evaluateCustomPropString(div, "title", expressionResolver, "")
                    if (notificationTitle.isNotEmpty()) {
                        setTitleFilter(notificationTitle)
                    }
                    
                    // Force refresh of notifications to ensure adapter listeners are fresh
                    android.util.Log.d("DemoCustomContainerAdapter", "Refreshing notifications in bindView")
                    setNotifications(com.yandex.divkit.demo.div.notificationList.NotificationManager.getAllNotifications())
                    
                    android.util.Log.d("DemoCustomContainerAdapter", "Listeners configured and type filter set in bindView")
                    android.util.Log.d("DemoCustomContainerAdapter", "=== bindView completed for notification_list ===")
                } ?: android.util.Log.e("DemoCustomContainerAdapter", "customView is not NotificationListView!")
            }

            else -> {
                if (customView.parent != null) {
                    (customView as? Chronometer)?.bind()
                    return
                }
                Handler(Looper.getMainLooper()).post {
                    (customView as? Chronometer)?.bind()
                }
            }
        }
    }

    private fun Context.createCustomCard(): View = Chronometer(this)
    private fun Context.circularProgress(): View =
        CircularProgressView(this, current = current, total = total, size = textSize)

    private fun Context.timerButton(): View = TimerButton(this, seconds = seconds).apply {
        setOnClickListener {
            loadScreenListener?.onLoad(timerButtonNext)
        }
    }

    private fun Context.timerButtonCallService(): View =
        TimerButton(this, seconds = seconds).apply {
            setOnClickListener {
                val map: HashMap<String, String> = HashMap()
                map["path"] = timerButtonPath
                map["ph/token"] = "empty"
                for (i in paramList)
                    map[i] = "empty"
//                map[timerButtonParam1] = "empty"
//                map[timerButtonParam2] = "empty"
//                map[timerButtonParam3] = "empty"
//                map[timerButtonParam4] = "empty"
//                map[timerButtonParam5] = "empty"
//                map[timerButtonParam6] = "empty"
//                map[timerButtonParam7] = "empty"
//                map[timerButtonParam8] = "empty"
//                map[timerButtonParam9] = "empty"
//                map[timerButtonParam10] = "empty"
//                map[timerButtonParam11] = "empty"
//                map[timerButtonParam12] = "empty"
//                map[timerButtonParam13] = "empty"
//                map[timerButtonParam14] = "empty"
//                map[timerButtonParam15] = "empty"
//                map[timerButtonParam16] = "empty"
//                map[timerButtonParam17] = "empty"
//                map[timerButtonParam18] = "empty"
//                map[timerButtonParam19] = "empty"
//                map[timerButtonParam20] = "empty"
//                map[timerButtonParam21] = "empty"
//                map[timerButtonParam22] = "empty"
//                map[timerButtonParam23] = "empty"
//                map[timerButtonParam24] = "empty"
//                map[timerButtonParam25] = "empty"
//                map[timerButtonParam26] = "empty"
//                map[timerButtonParam27] = "empty"
//                map[timerButtonParam28] = "empty"
//                map[timerButtonParam29] = "empty"
//                map[timerButtonParam30] = "empty"
                loadScreenListener?.onRequest(map)
            }
        }

    private fun Context.timerButtonUploadAndCallService(): View =
        TimerButton(this, seconds = seconds).apply {
            setOnClickListener {
                android.util.Log.d(
                    "DemoCustomContainer",
                    "=== Timer Button Upload and Call Service Clicked ==="
                )
                android.util.Log.d(
                    "DemoCustomContainer",
                    "Recording ID: $timerButtonUploadRecordingId"
                )
                android.util.Log.d("DemoCustomContainer", "Path: $timerButtonUploadPath")
                // Note: stopRecording() will be called inside uploadAndCallService with proper delay

                if (timerButtonUploadRecordingId.isNotEmpty()) {
                    // Call uploadAndCallService with the recording ID and service parameters
                    val serviceParams: HashMap<String, String> = HashMap()
                    serviceParams["path"] = timerButtonUploadPath
                    serviceParams["ph/token"] = "empty"

                    // Add all param values from paramList
                    for (param in paramList) {
                        if (param.isNotEmpty()) {
                            serviceParams[param] = "empty"
                        }
                    }

                    android.util.Log.d(
                        "DemoCustomContainer",
                        "Calling uploadAndCallService with params: $serviceParams"
                    )
                    // Get the actual recording ID from database using the key from custom_props
                    val actualRecordingId =
                        mehdiViewModel?.getValueByKey(timerButtonUploadRecordingId)?.value
                    android.util.Log.d(
                        "DemoCustomContainer",
                        "Recording ID key: $timerButtonUploadRecordingId"
                    )
                    android.util.Log.d(
                        "DemoCustomContainer",
                        "Actual recording ID from DB: $actualRecordingId"
                    )

                    if (actualRecordingId != null && actualRecordingId.isNotEmpty()) {
                        loadScreenListener?.uploadAndCallService(actualRecordingId, serviceParams)
                    } else {
                        android.util.Log.e(
                            "DemoCustomContainer",
                            "Recording ID not found in database for key: $timerButtonUploadRecordingId"
                        )
                        Toast.makeText(
                            context,
                            "شناسه ضبط صدا در پایگاه داده یافت نشد",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    android.util.Log.e(
                        "DemoCustomContainer",
                        "Recording ID key is empty, cannot upload"
                    )
                    Toast.makeText(context, "کلید شناسه ضبط صدا مشخص نیست", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    private fun Context.labelledSliderView(
        labelList: String,
        labelId: String,
        labelVariable: String
    ): View = LabelledSliderView(this).apply {
        val list = labelList.split(",").map { it.trim() }
        val idList = labelId.split(",").map { it.trim() }

        setItems(list)

        setOnItemSelectedListener { label, index ->
            Log.d("variable_name", labelVariable)
            loadScreenListener?.setVariableToBase(labelVariable, idList[index])
            Toast.makeText(context, "انتخاب شده: $label", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Context.createMultiSelection(
        multiOptions: String,
        multiIds: String,
        multiVariableName: String,
        multiSelectionLimit: Int,
        validation: String,
        enabled: String,
        searchable: String
    ): View = MultiSelectionView(this).apply {
        val optionsList = multiOptions.split(",").map { it.trim() }
        val idsList = multiIds.split(",").map { it.trim() }

        // Set searchable before setItems (it needs to be set before creating the UI)
        val isSearchable = searchable.equals("true", ignoreCase = true)
        setSearchable(isSearchable)

        if (optionsList.isNotEmpty() && idsList.isNotEmpty()) {
            setItems(optionsList, idsList, multiSelectionLimit)

            // Set validation based on the custom property
            val isValidationEnabled = validation.equals("true", ignoreCase = true)
            setValidation(isValidationEnabled)

            // Set enabled/disabled based on the custom property
            val isEnabled = enabled.equals("true", ignoreCase = true)
            setEnabled(isEnabled)

            setOnSelectionChangedListener { selectedIds ->
                if (multiVariableName.isNotEmpty()) {
                    loadScreenListener?.setVariableToBase(multiVariableName, selectedIds)
                }
            }
        }
    }

    private fun Context.createSingleSelection(
        singleOptions: String,
        singleIds: String,
        singleVariableName: String,
        validation: String,
        enabled: String,
        searchable: String
    ): View = SingleSelectionView(this).apply {
        val optionsList = singleOptions.split(",").map { it.trim() }
        val idsList = singleIds.split(",").map { it.trim() }

        // Set searchable before setItems (it needs to be set before creating the UI)
        val isSearchable = searchable.equals("true", ignoreCase = true)
        setSearchable(isSearchable)

        if (optionsList.isNotEmpty() && idsList.isNotEmpty()) {
            setItems(optionsList, idsList)

            // Set validation based on the custom property
            val isValidationEnabled = validation.equals("true", ignoreCase = true)
            setValidation(isValidationEnabled)

            // Set enabled/disabled based on the custom property
            val isEnabled = enabled.equals("true", ignoreCase = true)
            setEnabled(isEnabled)

            setOnSelectionChangedListener { selectedId ->
                if (singleVariableName.isNotEmpty()) {
                    loadScreenListener?.setVariableToBase(singleVariableName, selectedId)
                }
            }
        }
    }

    private fun Context.createMultiSelectionBottomSheet(
        multiOptions: String,
        multiIds: String,
        multiVariableName: String,
        multiTitle: String,
        multiSelectionLimit: Int,
        validation: String,
        enabled: String,
        searchable: String
    ): View = MultiSelectionBottomSheetView(this).apply {
        val optionsList = multiOptions.split(",").map { it.trim() }
        val idsList = multiIds.split(",").map { it.trim() }

        // Set searchable before setItems
        val isSearchable = searchable.equals("true", ignoreCase = true)
        setSearchable(isSearchable)

        // Set title
        setTitle(multiTitle)

        if (optionsList.isNotEmpty() && idsList.isNotEmpty()) {
            setItems(optionsList, idsList, multiSelectionLimit)

            // Set validation
            val isValidationEnabled = validation.equals("true", ignoreCase = true)
            setValidation(isValidationEnabled)

            // Set enabled/disabled
            val isEnabled = enabled.equals("true", ignoreCase = true)
            setEnabled(isEnabled)

            setOnSelectionChangedListener { selectedIds ->
                if (multiVariableName.isNotEmpty()) {
                    loadScreenListener?.setVariableToBase(multiVariableName, selectedIds)
                }
            }
        }
    }

    private fun Context.createSingleSelectionBottomSheet(
        singleOptions: String,
        singleIds: String,
        singleVariableName: String,
        singleTitle: String,
        validation: String,
        enabled: String,
        searchable: String
    ): View = SingleSelectionBottomSheetView(this).apply {
        val optionsList = singleOptions.split(",").map { it.trim() }
        val idsList = singleIds.split(",").map { it.trim() }

        // Set searchable before setItems
        val isSearchable = searchable.equals("true", ignoreCase = true)
        setSearchable(isSearchable)

        // Set title
        setTitle(singleTitle)

        if (optionsList.isNotEmpty() && idsList.isNotEmpty()) {
            setItems(optionsList, idsList)

            // Set validation
            val isValidationEnabled = validation.equals("true", ignoreCase = true)
            setValidation(isValidationEnabled)

            // Set enabled/disabled
            val isEnabled = enabled.equals("true", ignoreCase = true)
            setEnabled(isEnabled)

            setOnSelectionChangedListener { selectedId ->
                if (singleVariableName.isNotEmpty()) {
                    loadScreenListener?.setVariableToBase(singleVariableName, selectedId)
                }
            }
        }
    }

    private fun Context.createCustomInput(
        variableName: String,
        hint: String,
        inputType: String,
        validation: String,
        enabled: String,
        min: String,
        max: String
    ): View = CustomInputView(this).apply {
        if (hint.isNotEmpty()) {
            setHint(hint)
        }

        if (inputType.isNotEmpty()) {
            setInputType(inputType)
        }

        // Set min/max values for numeric inputs
        if (min.isNotEmpty()) {
            min.toDoubleOrNull()?.let { setMinValue(it) }
        }
        if (max.isNotEmpty()) {
            max.toDoubleOrNull()?.let { setMaxValue(it) }
        }

        // Set validation based on the custom property
        val isValidationEnabled = validation.equals("true", ignoreCase = true)
        setValidation(isValidationEnabled)

        // Set enabled/disabled based on the custom property
        val isEnabled = enabled.equals("true", ignoreCase = true)
        setEnabled(isEnabled)

        setOnTextChangedListener { text ->
            if (variableName.isNotEmpty()) {
                loadScreenListener?.setVariableToBase(variableName, text)
            }
        }
    }

    private fun Context.audioPlayerView(): View = AudioPlayerView(this).apply {

//   setAudioUrl("https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3")
        setAudioUrl(audioUrl)
    }

    private fun Context.audioRecorderView(): View = AudioRecorderView(this).apply {
        android.util.Log.d(
            "DemoCustomContainerAdapter",
            "Creating AudioRecorderView with loadScreenListener: ${loadScreenListener != null}"
        )
        setLoadScreenListener(loadScreenListener)
    }

    private fun Context.createNotificationList(
        notificationType: String = "",
        notificationTitle: String = "",
    ): View = NotificationListView(this).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        // Configure listeners with references so they can be recreated in onAttachedToWindow
        configureListeners(loadScreenListener, mehdiViewModel)
        // Set type filter if provided
        if (notificationType.isNotEmpty()) {
            setTypeFilter(notificationType)
            android.util.Log.d("DemoCustomContainerAdapter", "Created NotificationListView with type filter: $notificationType")
        }
        if (notificationTitle.isNotEmpty()) {
            setTitleFilter(notificationTitle)
            android.util.Log.d("DemoCustomContainerAdapter", "Created NotificationListView with title filter: $notificationTitle")
        }
    }

    private fun Context.createSignatureView(variableName: String): View = SignatureView(this).apply {
        if (variableName.isNotEmpty()) {
            setOnSignatureSavedListener { base64String ->
                android.util.Log.d("DemoCustomContainerAdapter", "Signature saved to variable: $variableName")
                // Update DivKit variable only
                loadScreenListener?.setVariableToBase(variableName, base64String)
            }
        } else {
            android.util.Log.w("DemoCustomContainerAdapter", "Variable name not provided for signature view")
        }
    }

    private fun Context.createAccidentSketchView(
        jsonVariable: String,
        imageVariable: String,
        soundVariable: String,
        previewMode: Boolean = false,
    ): View =
        AccidentSketchView(this).apply {
            configure(jsonVariable, imageVariable, soundVariable)
            setPreviewMode(previewMode)
            if (!previewMode && (jsonVariable.isNotEmpty() || imageVariable.isNotEmpty() || soundVariable.isNotEmpty())) {
                setOnSketchSavedListener { json, imageBase64, soundPath ->
                    if (jsonVariable.isNotEmpty()) {
                        loadScreenListener?.setVariableToBase(jsonVariable, json)
                    }
                    if (imageVariable.isNotEmpty()) {
                        loadScreenListener?.setVariableToBase(imageVariable, imageBase64)
                    }
                    if (soundVariable.isNotEmpty() && !soundPath.isNullOrEmpty()) {
                        loadScreenListener?.setVariableToBase(soundVariable, soundPath)
                    }
                }
            }
        }

    private fun Context.createImagePickerView(): ImagePickerView = ImagePickerView(this)

    private fun bindImagePicker(
        picker: ImagePickerView,
        div: DivCustom,
        expressionResolver: ExpressionResolver,
    ) {
        val variableName = evaluateCustomPropString(div, "variable_name", expressionResolver, "")
        val title = evaluateCustomPropString(div, "title", expressionResolver, "")
        val minImages = evaluateCustomProp(div, "minImages", expressionResolver, 0)
        val maxImages = evaluateCustomProp(div, "maxImages", expressionResolver, Int.MAX_VALUE)
        val imageSource = evaluateCustomPropString(div, "imageSource", expressionResolver, "both")
        val withDescriptions = evaluateCustomPropString(div, "with_descriptions", expressionResolver, "true")
            .equals("true", ignoreCase = true)
        val sceneLabel = evaluateCustomPropString(
            div,
            "scene_label",
            expressionResolver,
            "توضیحات از نحوه و چگونگی تصویر",
        )
        val expertLabel = evaluateCustomPropString(
            div,
            "expert_label",
            expressionResolver,
            "توضیحات کارشناس در خصوص تصویر",
        )
        val initialValue = evaluateCustomPropString(div, "value", expressionResolver, "")
        val storeScope = evaluateCustomPropString(div, "store_key", expressionResolver, "")
            .ifBlank {
                val needsVehicleScope = hasCustomProp(div, "store_key") ||
                    variableName == "variable_compromising_vehicle_damage_parts_images"
                if (needsVehicleScope) {
                    mehdiViewModel?.getValueByKey("current_vehicle_id")?.value.orEmpty()
                } else {
                    ""
                }
            }
        val storeKey = if (storeScope.isBlank()) variableName else "$variableName/$storeScope"
        picker.bindPicker(
            newVariableName = variableName,
            newTitle = title,
            min = minImages,
            max = maxImages,
            source = imageSource,
            descriptionsEnabled = withDescriptions,
            newSceneLabel = sceneLabel,
            newExpertLabel = expertLabel,
            initialValue = initialValue,
            listener = { storedValue ->
                if (variableName.isNotEmpty()) {
                    loadScreenListener?.setVariableToBase(variableName, storedValue)
                }
            },
            newStoreKey = storeKey,
        )
        if (variableName.isEmpty()) {
            android.util.Log.w("DemoCustomContainerAdapter", "Variable name not provided for image picker view")
        }
    }

    private fun Context.doubleCircularProgressView(): View =
        DoubleCircularProgressView(
            this,
            innerPercent = innerPercent.toFloat(),
            outerPercent = outerPercent.toFloat(),
            centerPercent = centerPercent,
            innerCircleColor = innerColor,
            outerCircleColor = outerColor
        )

    private fun Context.createTreeView(
        config: TreeChartView.Config,
        mehdiViewModel: MehdiViewModel?,
        lifecycleOwner: LifecycleOwner?,
        loadScreenListener: LoadScreenListener?
    ): View = TreeChartView(this).apply {
        initialize(config, mehdiViewModel, lifecycleOwner, loadScreenListener)
    }

    private fun resolveActionHandler(context: Context): UIDiv2ActionHandler? {
        var ctx: Context? = context
        while (ctx != null) {
            if (ctx is MehdiActivity) {
                return ctx.getActionHandler()
            }
            ctx = if (ctx is ContextWrapper) ctx.baseContext else null
        }
        return null
    }

    private fun dismissHostingBottomSheet(context: Context) {
        resolveActionHandler(context)?.dismissCurrentBottomSheet()
        findHostActivity(context)?.supportFragmentManager?.fragments
            ?.filterIsInstance<BottomSheetDiv>()
            ?.lastOrNull()
            ?.takeIf { it.isAdded }
            ?.dismiss()
    }

    private fun findHostActivity(context: Context): FragmentActivity? {
        var ctx: Context? = context
        while (ctx != null) {
            if (ctx is FragmentActivity) return ctx
            ctx = if (ctx is ContextWrapper) ctx.baseContext else null
        }
        return null
    }

    private fun Context.createStaticTreeView(
        jsonData: String,
        config: StaticTreeView.Config,
        nameVariable: String = "",
        codeVariable: String = "",
        parentIdVariable: String = "",
        idVariable: String = "",
        confirmText: String = "تایید",
        confirmUrl: String = "",
        clearVariablesOnConfirm: String = "",
        showConfirmButton: Boolean = false,
        divView: Div2View? = null,
        variableTarget: String = "base",
        dismissOnConfirm: Boolean = false,
        loadScreenListener: LoadScreenListener? = null,
    ): View {
        val treeView = StaticTreeView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            initialize(jsonData, config)
        }

        fun wrapTreeInScrollView(): DemoNestedScrollView {
            return DemoNestedScrollView(this).apply {
                isFillViewport = true
                isNestedScrollingEnabled = true
                overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                addView(
                    treeView,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ),
                )
            }
        }

        if (!showConfirmButton) {
            return wrapTreeInScrollView()
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            addView(
                wrapTreeInScrollView().apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f,
                    )
                },
            )

            val confirmButton = Button(context).apply {
                text = confirmText
                textSize = 16f
                setTextColor(Color.WHITE)
                isAllCaps = false
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12 * resources.displayMetrics.density
                    setColor(0xFF2196F3.toInt())
                }
                val paddingV = (12 * resources.displayMetrics.density).toInt()
                val paddingH = (16 * resources.displayMetrics.density).toInt()
                setPadding(paddingH, paddingV, paddingH, paddingV)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    val margin = (12 * resources.displayMetrics.density).toInt()
                    setMargins(margin, margin, margin, margin)
                }
                setOnClickListener {
                    val selectedNode = treeView.getPrimarySelectedNode()
                    if (selectedNode == null) {
                        Toast.makeText(context, "لطفاً یک مورد انتخاب کنید", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val actionHandler = resolveActionHandler(context)

                    fun setVar(name: String, value: String) {
                        if (name.isEmpty()) return
                        if (actionHandler != null && divView != null) {
                            actionHandler.setVariableOnTarget(name, value, variableTarget, divView)
                        } else if (variableTarget.equals("base", ignoreCase = true)) {
                            loadScreenListener?.setVariableToBase(name, value)
                        }
                    }

                    setVar(nameVariable, selectedNode.name)
                    setVar(codeVariable, selectedNode.code)
                    setVar(idVariable, selectedNode.id)
                    setVar(parentIdVariable, selectedNode.parentId)

                    clearVariablesOnConfirm
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .forEach { name ->
                            val cleared = if (name.contains("ready", ignoreCase = true)) "false" else ""
                            setVar(name, cleared)
                        }

                    // Reveal road field as soon as police center is confirmed;
                    // roadsAccidents may return empty/missing "road" and must not hide UI.
                    if (confirmUrl.contains("roadsAccidents")) {
                        setVar("variable_roads_ready", "true")
                    }

                    // Police-center selection: persist selected node Id + code.
                    // Legacy road-tree selection: persist parent Id as policeCenterId.
                    if (idVariable.isNotBlank() || confirmUrl.contains("roadsAccidents")) {
                        persistPoliceCenterSelectionToDb(selectedNode.id, selectedNode.code)
                    } else {
                        persistRoadSelectionToDb(
                            selectedNode.code,
                            selectedNode.parentId,
                            parentIdVariable
                        )
                    }

                    if (confirmUrl.isNotBlank()) {
                        dispatchStaticTreeConfirmUrl(
                            confirmUrl = confirmUrl,
                            selectedNode = selectedNode,
                            codeVariable = codeVariable,
                            nameVariable = nameVariable,
                            idVariable = idVariable,
                            loadScreenListener = loadScreenListener,
                        )
                    }

                    if (dismissOnConfirm) {
                        dismissHostingBottomSheet(context)
                    }
                }
            }
            addView(confirmButton)
        }
    }

    /**
     * Executes confirm_url after selection. For call_service, injects selected node code into
     * policeCenterCode so @{variable_policeCenterCode} is never sent empty (vars are on base,
     * while the sheet URL may evaluate before set).
     */
    private fun dispatchStaticTreeConfirmUrl(
        confirmUrl: String,
        selectedNode: StaticTreeView.TreeNode,
        codeVariable: String,
        nameVariable: String,
        idVariable: String,
        loadScreenListener: LoadScreenListener?,
    ) {
        val resolvedUrl = confirmUrl
            .replace("@{variable_policeCenterCode}", selectedNode.code)
            .replace("@{variable_policeCenter}", selectedNode.name)
            .replace("@{variable_policeCenterId}", selectedNode.id)
            .let { url ->
                var out = url
                if (codeVariable.isNotBlank()) {
                    out = out.replace("@{$codeVariable}", selectedNode.code)
                }
                if (nameVariable.isNotBlank()) {
                    out = out.replace("@{$nameVariable}", selectedNode.name)
                }
                if (idVariable.isNotBlank()) {
                    out = out.replace("@{$idVariable}", selectedNode.id)
                }
                out
            }

        val uri = runCatching { Uri.parse(resolvedUrl) }.getOrNull() ?: return
        if (uri.scheme != "div-action" || uri.authority != "call_service") {
            android.util.Log.w(
                "DemoCustomContainerAdapter",
                "confirm_url not call_service, ignored: $resolvedUrl"
            )
            return
        }

        val map = java.util.HashMap<String, String>()
        for (parameterName in uri.queryParameterNames) {
            map[parameterName] = uri.getQueryParameter(parameterName).orEmpty()
        }
        // Always send the selected node's code — never an empty expression residue.
        map["policeCenterCode"] = selectedNode.code
        if (map["ph/token"].isNullOrBlank()) {
            map["ph/token"] = "empty"
        }
        if (map["sysName"].isNullOrBlank()) {
            map["sysName"] = "accidents"
        }
        if (map["path"].isNullOrBlank()) {
            map["path"] = "roadsAccidents"
        }

        android.util.Log.d(
            "DemoCustomContainerAdapter",
            "static_tree confirm call_service map=$map"
        )
        loadScreenListener?.onRequest(map)
    }

    private fun persistPoliceCenterSelectionToDb(policeCenterId: String, policeCenterCode: String) {
        val accidentId = mehdiViewModel?.getValueByKey("current_accident_id")?.value.orEmpty()
        if (accidentId.isBlank()) {
            android.util.Log.w(
                "DemoCustomContainerAdapter",
                "persistPoliceCenterSelectionToDb: current_accident_id empty; id=$policeCenterId code=$policeCenterCode"
            )
            // Still persist global keys used by roadsAccidents if needed later.
        }
        runCatching {
            if (accidentId.isNotBlank()) {
                mehdiViewModel?.insertItemToDbSync(
                    com.yandex.divkit.demo.data.entities.PhPlusDB(
                        null,
                        "variable_policeCenterId/$accidentId",
                        policeCenterId
                    )
                )
                mehdiViewModel?.insertItemToDbSync(
                    com.yandex.divkit.demo.data.entities.PhPlusDB(
                        null,
                        "variable_policeCenterCode/$accidentId",
                        policeCenterCode
                    )
                )
            }
            mehdiViewModel?.insertItemToDbSync(
                com.yandex.divkit.demo.data.entities.PhPlusDB(
                    null,
                    "variable_policeCenterId",
                    policeCenterId
                )
            )
            mehdiViewModel?.insertItemToDbSync(
                com.yandex.divkit.demo.data.entities.PhPlusDB(
                    null,
                    "variable_policeCenterCode",
                    policeCenterCode
                )
            )
            android.util.Log.d(
                "DemoCustomContainerAdapter",
                "Saved policeCenterId=$policeCenterId policeCenterCode=$policeCenterCode accidentId=$accidentId"
            )
        }.onFailure {
            android.util.Log.e("DemoCustomContainerAdapter", "persistPoliceCenterSelectionToDb failed", it)
        }
    }

    private fun persistRoadSelectionToDb(
        roadCode: String,
        parentId: String,
        parentIdVariable: String,
    ) {
        val accidentId = mehdiViewModel?.getValueByKey("current_accident_id")?.value.orEmpty()
        if (accidentId.isBlank()) {
            android.util.Log.w(
                "DemoCustomContainerAdapter",
                "persistRoadSelectionToDb: current_accident_id empty; parentId=$parentId code=$roadCode"
            )
            return
        }
        val policeKey = if (parentIdVariable.isNotBlank()) {
            "$parentIdVariable/$accidentId"
        } else {
            "variable_policeCenterId/$accidentId"
        }
        runCatching {
            mehdiViewModel?.insertItemToDbSync(
                com.yandex.divkit.demo.data.entities.PhPlusDB(null, policeKey, parentId)
            )
            mehdiViewModel?.insertItemToDbSync(
                com.yandex.divkit.demo.data.entities.PhPlusDB(
                    null,
                    "variable_road_code/$accidentId",
                    roadCode
                )
            )
            android.util.Log.d(
                "DemoCustomContainerAdapter",
                "Saved policeCenterId=$parentId roadCode=$roadCode for accidentId=$accidentId"
            )
        }.onFailure {
            android.util.Log.e("DemoCustomContainerAdapter", "persistRoadSelectionToDb failed", it)
        }
    }

    private fun parseKeyValuePairs(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        val pairs = raw.split(",", "&").mapNotNull { entry ->
            val trimmed = entry.trim()
            if (trimmed.isEmpty() || !trimmed.contains("=")) return@mapNotNull null
            val parts = trimmed.split("=", limit = 2)
            val key = parts[0].trim()
            val value = parts.getOrNull(1)?.trim() ?: ""
            if (key.isEmpty()) null else key to value
        }
        return pairs.toMap()
    }

    private fun Context.createCustomMap(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        var lat: String = "35.711"
        var lon: String = "51.711"
        val childView = LayoutInflater.from(this@createCustomMap)
            .inflate(com.yandex.divkit.demo.R.layout.map, this, false)
        val mapContainer =
            childView.findViewById(com.yandex.divkit.demo.R.id.map_container) as FrameLayout
        val btn_select: Button = childView.findViewById(com.yandex.divkit.demo.R.id.btn_select)
        val myLocationBtn: FloatingActionButton =
            childView.findViewById(com.yandex.divkit.demo.R.id.my_location_btn)

        Mapbox.getInstance(this@createCustomMap, "")
        val mapView = com.mapbox.mapboxsdk.maps.MapView(this@createCustomMap).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            // Initialize the MapView
//            onCreate(null)


        }
        /*        mapView.addOnAttachStateChangeListener( object:View.OnAttachStateChangeListener{
                    override fun onViewAttachedToWindow(v: View) {

                    }

                    override fun onViewDetachedFromWindow(v: View) {
                    }

                })*/
        mapView.getMapAsync { mapboxMap ->

            // Set the custom style URL
            mapboxMap.setStyle("http://mapbox-gis.naja.net:7070/styles/rezvani-style/style.json") {
                // Optional: Add additional setup after the style loads
                Toast.makeText(context, "Map style loaded successfully!", Toast.LENGTH_SHORT)
                    .show()

                removeView(childView)
                mapContainer.removeView(mapView)
                addView(childView)
                mapContainer.addView(mapView, 0)
            }
            mapboxMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(35.720755, 51.427661),
                    12.0
                )
            )
            mapboxMap.addOnMapLongClickListener { point ->
                mapboxMap.clear()
                val markerOptions = MarkerOptions()
                markerOptions.setPosition(LatLng(point.latitude, point.longitude))
                    .title("New Marker")
                mapboxMap.addMarker(markerOptions)
                lat = point.latitude.toString()
                lon = point.longitude.toString()
                true
            }
        }
        addView(childView)

        mapContainer.addView(mapView, 0)

        btn_select.setOnClickListener {
            if (lat != "" && lon != "") {

                val map: java.util.HashMap<String, String> = HashMap()
                map.put("path", "address")
                map.put("sysName", "vt")
                map.put("status", "location")
                map.put("latitude", lat)
                map.put("longitude", lat)
                map.put("ph/token", "empty")

                loadScreenListener?.onRequest(map)
            }
        }
//        mapContainer.post(Runnable {

//        })

//        val mapView = MapView(this@createCustomMap)
//        mapView.layoutParams = FrameLayout.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        )

    }
//    private fun Context.createOfflineVtReportsContainer(): View = LinearLayout(this).apply {
//        orientation = LinearLayout.VERTICAL
//        layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.MATCH_PARENT
//        )
//        removeAllViews()
//        val childView = LayoutInflater.from(this@createOfflineVtReportsContainer)
//            .inflate(com.yandex.divkit.demo.R.layout.offline_vt_reports, this, false)
//        val recyclerview =
//            childView.findViewById<RecyclerView>(com.yandex.divkit.demo.R.id.rvReports)
//        recyclerview.layoutManager = LinearLayoutManager(this@createOfflineVtReportsContainer)
//        adapter = VtReportAdapter(this@DemoCustomContainerAdapter)
//
//        val items = ArrayList<String>()
//        var username = mehdiViewModel?.getValueByKey("userName")?.value
//        lo?.let {
//            mehdiViewModel?.getVtOfflineReports("%$username/vt%")?.observe(it) {
//                for (ph: PhPlusDB in it) {
//                    ph.value?.let { it1 -> items.add(it1) }
//                }
//                adapter.setItems(items)
//                recyclerview.adapter = adapter
//
//
//            }
//        }
//        addView(childView)
//    }

    private fun Context.createOfflineListContainer(div2View: Div2View): View =
        OfflineViewList(
            this,
            system = systemForOffline,
            listener = this@DemoCustomContainerAdapter
        ).apply {
            val username = mehdiViewModel?.getValueByKey("userName")?.value
            val resolvedAccidentId = accidentIdForOffline.takeIf { it.isNotBlank() }
                ?: runCatching {
                    mehdiViewModel?.getValueByKey("current_accident_id")?.value.orEmpty()
                }.getOrDefault("")
                .ifBlank {
                    VehicleEntryCountHelper.resolveAccidentId(mehdiViewModel)
                }
            val resolvedVehicleId = vehicleIdForOffline.ifBlank {
                mehdiViewModel?.getValueByKey("current_vehicle_id")?.value.orEmpty()
            }
            val queryPattern = when {
                systemForOffline == "vehicles" && resolvedAccidentId.isNotBlank() ->
                    "%$username/vehicles/$resolvedAccidentId%"
                systemForOffline == "passengers" && resolvedVehicleId.isNotBlank() ->
                    "%$username/passengers/$resolvedVehicleId%"
                systemForOffline == "pedestrians" && resolvedAccidentId.isNotBlank() ->
                    "%$username/pedestrians/$resolvedAccidentId%"
                systemForOffline == "organizations" && resolvedAccidentId.isNotBlank() ->
                    "%$username/organizations/$resolvedAccidentId%"
                systemForOffline == "livestock" && resolvedAccidentId.isNotBlank() ->
                    "%$username/livestock/$resolvedAccidentId%"
                else -> "%$username/$systemForOffline%"
            }
            lo?.let {
                mehdiViewModel?.getVtOfflineReports(queryPattern)?.observe(it) { rows ->
                    val items = rows.mapNotNull { ph ->
                        val value = ph.value ?: return@mapNotNull null
                        val key = ph.key ?: return@mapNotNull null
                        mapOf("data" to value, "key" to key)
                    }
                    setData(items.toMutableList())
                    if (systemForOffline == "vehicles") {
                        VehicleEntryCountHelper.sync(
                            mehdiViewModel,
                            div2View,
                            resolvedAccidentId,
                        )
                    }
                }
            }

        }

    private fun Context.createCustomContainer(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
    }

    private fun Context.createCustomText(message: String): View = TextView(this).apply {
        text = message
    }

    private fun Chronometer.bind() {
        setPadding(30, 30, 30, 30)
        val gd = GradientDrawable()
        gd.orientation = GradientDrawable.Orientation.BL_TR
        gd.colors =
            intArrayOf(-0xFF0000, -0xFF7F00, -0xF00F00, -0x00FF00, -0x0000FF, -0x2E2B5F, -0x8B00FF)
        background = gd
        textSize = 20f
        setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                isTheFinalCountDown
            } else {
                Toast.makeText(context, "no final countdown for you", Toast.LENGTH_SHORT).show()
            }
        }
        start()
    }

//    override fun onClickedVtReport(data: String) {
//        loadScreenListener?.loadeScreenWithData(data, "vt/main")
//    }

    override fun onClickedOfflineView(data: String, next: String) {
        runCatching {
            val json = JSONObject(data)
            json.optString("accident_id").takeIf { it.isNotBlank() }?.let { id ->
                mehdiViewModel?.insertItemToDbSync(
                    PhPlusDB(null, "current_accident_id", id)
                )
            }
            json.optString("vehicle_id").takeIf { it.isNotBlank() }?.let { id ->
                mehdiViewModel?.insertItemToDbSync(
                    PhPlusDB(null, "current_vehicle_id", id)
                )
            }
            json.optString("passenger_id").takeIf { it.isNotBlank() }?.let { id ->
                mehdiViewModel?.insertItemToDbSync(
                    PhPlusDB(null, "current_passenger_id", id)
                )
            }
            json.optString("pedestrian_id").takeIf { it.isNotBlank() }?.let { id ->
                mehdiViewModel?.insertItemToDbSync(
                    PhPlusDB(null, "current_pedestrian_id", id)
                )
            }
            json.optString("organization_id").takeIf { it.isNotBlank() }?.let { id ->
                mehdiViewModel?.insertItemToDbSync(
                    PhPlusDB(null, "current_organization_id", id)
                )
            }
        }
        val processType = runCatching { JSONObject(data).optString("process_type") }.getOrDefault("")
        if (processType == "livestock") {
            runCatching {
                val json = JSONObject(data)
                json.optString("livestock_id").takeIf { it.isNotBlank() }?.let { id ->
                    mehdiViewModel?.insertItemToDbSync(PhPlusDB(null, "current_livestock_id", id))
                }
                json.optString("type_title").takeIf { it.isNotBlank() }?.let { title ->
                    mehdiViewModel?.insertItemToDbSync(PhPlusDB(null, "compromising_livestock_type", title))
                }
                json.optString("type_code").takeIf { it.isNotBlank() }?.let { code ->
                    mehdiViewModel?.insertItemToDbSync(PhPlusDB(null, "compromising_livestock_type_code", code))
                }
                json.optString("count").takeIf { it.isNotBlank() }?.let { count ->
                    mehdiViewModel?.insertItemToDbSync(PhPlusDB(null, "variable_livestock_count", count))
                }
            }
            loadScreenListener?.openBottomSheet("Accidents/livestock_bottomsheet")
            return
        }
        val wizardScreen = ArchiveWizardNavigator.seedStack(mehdiViewModel, data, next)
        if (wizardScreen != null) {
            loadScreenListener?.onLoadFinishingCurrent(wizardScreen)
        } else if (next.startsWith("draft/")) {
            loadScreenListener?.onLoad(next)
        } else {
            loadScreenListener?.loadeScreenWithData(data, next)
        }
    }

    override fun onVehicleSignatureSaved(archiveKey: String, signatureBase64: String) {
        if (archiveKey.isBlank() || signatureBase64.isBlank()) return
        val existingJson = mehdiViewModel?.getValueByKey(archiveKey)?.value.orEmpty()
        if (existingJson.isBlank()) {
            android.util.Log.w(
                "DemoCustomContainerAdapter",
                "onVehicleSignatureSaved: archive missing key=$archiveKey",
            )
            return
        }
        val updated = runCatching {
            JSONObject(existingJson).apply {
                put(VehicleEntryCountHelper.DRIVER_SIGNATURE_ARCHIVE_FIELD, signatureBase64)
            }
        }.getOrNull() ?: return

        val username = mehdiViewModel?.getValueByKey("userName")?.value.orEmpty()
        val accidentId = updated.optString("accident_id")
            .ifBlank { VehicleEntryCountHelper.resolveAccidentId(mehdiViewModel) }
        val vehicleId = updated.optString("vehicle_id")
            .ifBlank { archiveKey.substringAfterLast('/') }

        if (username.isBlank() || accidentId.isBlank() || vehicleId.isBlank()) {
            android.util.Log.w(
                "DemoCustomContainerAdapter",
                "onVehicleSignatureSaved: missing ids username=$username accidentId=$accidentId vehicleId=$vehicleId",
            )
            return
        }

        mehdiViewModel?.upsertVehicleArchiveEntry(
            username = username,
            accidentId = accidentId,
            vehicleId = vehicleId,
            archiveKey = archiveKey,
            json = updated.toString(),
        )
        VehicleEntryCountHelper.sync(mehdiViewModel, offlineListDiv2View, accidentId)
    }

    override fun onDeleteOfflineView(archiveKey: String, data: String) {
        val json = JSONObject(data)
        val processType = json.optString("process_type")
        if (processType == "passenger") {
            val passengerId = json.optString("passenger_id").ifBlank {
                Regex("draft/([^/]+)/").find(json.optString("next"))?.groupValues?.getOrNull(1).orEmpty()
            }
            mehdiViewModel?.deletePassengerArchiveAndDrafts(
                archiveKey = archiveKey,
                passengerId = passengerId,
            )
            return
        }
        if (processType == "pedestrian") {
            val pedestrianId = json.optString("pedestrian_id").ifBlank {
                Regex("draft/([^/]+)/").find(json.optString("next"))?.groupValues?.getOrNull(1).orEmpty()
            }
            mehdiViewModel?.deletePedestrianArchiveAndDrafts(
                archiveKey = archiveKey,
                pedestrianId = pedestrianId,
            )
            return
        }
        if (processType == "organization") {
            val organizationId = json.optString("organization_id").ifBlank {
                Regex("draft/([^/]+)/").find(json.optString("next"))?.groupValues?.getOrNull(1).orEmpty()
            }
            mehdiViewModel?.deleteOrganizationArchiveAndDrafts(
                archiveKey = archiveKey,
                organizationId = organizationId,
            )
            return
        }
        if (processType == "vehicle") {
            val vehicleId = json.optString("vehicle_id").ifBlank {
                Regex("draft/([^/]+)/").find(json.optString("next"))?.groupValues?.getOrNull(1).orEmpty()
            }
            mehdiViewModel?.deleteVehicleArchiveAndDrafts(
                archiveKey = archiveKey,
                vehicleId = vehicleId,
            )
            json.optString("accident_id").takeIf { it.isNotBlank() }?.let { accidentId ->
                VehicleEntryCountHelper.sync(
                    mehdiViewModel,
                    offlineListDiv2View,
                    accidentId,
                )
            }
            return
        }
        if (processType == "livestock") {
            mehdiViewModel?.deleteLivestockArchive(archiveKey)
            return
        }
        var accidentId = json.optString("accident_id")
        if (accidentId.isBlank()) {
            val next = json.optString("next")
            accidentId = Regex("draft/([^/]+)/").find(next)?.groupValues?.getOrNull(1).orEmpty()
        }
        mehdiViewModel?.deleteAccidentArchiveAndDrafts(
            archiveKey = archiveKey,
            accidentId = accidentId,
        )
    }

    /**
     * Create a blue marker icon for user location
     */
    private fun createBlueMarkerIcon(context: Context, iconFactory: IconFactory): com.mapbox.mapboxsdk.annotations.Icon {
        // Get the default marker drawable
        val markerDrawable = ContextCompat.getDrawable(context, com.mapbox.mapboxsdk.R.drawable.mapbox_marker_icon_default)
        
        if (markerDrawable != null) {
            // Create a mutable bitmap
            val bitmap = Bitmap.createBitmap(
                markerDrawable.intrinsicWidth,
                markerDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            
            // Set blue color filter
            markerDrawable.setColorFilter(Color.parseColor("#2196F3"), PorterDuff.Mode.SRC_IN)
            markerDrawable.setBounds(0, 0, canvas.width, canvas.height)
            markerDrawable.draw(canvas)
            
            // Create icon from bitmap
            return iconFactory.fromBitmap(bitmap)
        }
        
        // Fallback to default marker if drawable not found
        return iconFactory.defaultMarker()
    }

    private fun Context.createNavigationMap(destinationLat: String, destinationLon: String, routingRequired: String = "true"): View =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            val childView = LayoutInflater.from(this@createNavigationMap)
                .inflate(com.yandex.divkit.demo.R.layout.map, this, false)
            val mapContainer =
                childView.findViewById(com.yandex.divkit.demo.R.id.map_container) as FrameLayout
            val myLocationBtn: FloatingActionButton =
                childView.findViewById(com.yandex.divkit.demo.R.id.my_location_btn)

            // Hide select button for navigation
            val btn_select: Button = childView.findViewById(com.yandex.divkit.demo.R.id.btn_select)
            btn_select.visibility = View.GONE

            Mapbox.getInstance(this@createNavigationMap, "")
            val mapView = com.mapbox.mapboxsdk.maps.MapView(this@createNavigationMap).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }

            // Parse routingRequired flag (default: true) - outside getMapAsync to be accessible in onClickListener
            val isRoutingRequired = routingRequired.equals("true", ignoreCase = true)

            mapView.getMapAsync { mapboxMap ->
                mapboxMap.setStyle("http://mapbox-gis.naja.net:7070/styles/rezvani-style/style.json") {
                    Toast.makeText(context, "Navigation map loaded!", Toast.LENGTH_SHORT).show()
                    mapStyle = it

                    // Get user's current location from LocationService
                    val userLocation =
                        com.yandex.divkit.demo.service.LocationService.getLastLocation(context)

                    // Parse destination coordinates
                    val destLat = destinationLat.toDoubleOrNull() ?: 0.0
                    val destLon = destinationLon.toDoubleOrNull() ?: 0.0

                    if (destLat != 0.0 && destLon != 0.0) {
                        // Add destination marker
                        val destinationMarker = MarkerOptions()
                            .setPosition(LatLng(destLat, destLon))
                            .title("مقصد")
                        mapboxMap.addMarker(destinationMarker)

                        // If user location is available, add user location marker and draw route
                        if (userLocation != null) {
                            val iconFactory = IconFactory.getInstance(context)
                            val blueIcon = createBlueMarkerIcon(context, iconFactory)
                            val userMarker = MarkerOptions()
                                .setPosition(LatLng(userLocation.latitude, userLocation.longitude))
                                .title("موقعیت شما")
                                .icon(blueIcon)
                            mapboxMap.addMarker(userMarker)

                            // Call routing service to get route (only if routingRequired is true)
                            if (isRoutingRequired) {
                            callRoutingService(
                                userLocation.latitude,
                                userLocation.longitude,
                                destLat,
                                destLon,
                                mapboxMap,
                                context
                            )
                            }

                            // Move camera to show both markers
                            val bounds = com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                                .include(LatLng(userLocation.latitude, userLocation.longitude))
                                .include(LatLng(destLat, destLon))
                                .build()

                            mapboxMap.moveCamera(
                                CameraUpdateFactory.newLatLngBounds(bounds, 100)
                            )
                        } else {
                            // Only destination available, center on destination
                            mapboxMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(destLat, destLon),
                                    14.0
                                )
                            )
                            // Call routing service only if routingRequired is true
                            if (isRoutingRequired) {
                            callRoutingService(
                                35.15,
                                52.20,
                                35.26,
                                52.54,
                                mapboxMap,
                                context
                            )
                            }
                            Toast.makeText(context, "موقعیت شما در دسترس نیست", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        // No destination coordinates, show user location if available
                        if (userLocation != null) {
                            val iconFactory = IconFactory.getInstance(context)
                            val blueIcon = createBlueMarkerIcon(context, iconFactory)
                            val userMarker = MarkerOptions()
                                .setPosition(LatLng(userLocation.latitude, userLocation.longitude))
                                .title("موقعیت شما")
                                .icon(blueIcon)
                            mapboxMap.addMarker(userMarker)

                            mapboxMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(userLocation.latitude, userLocation.longitude),
                                    14.0
                                )
                            )
                        } else {
                            // Default location (Tehran)
                            mapboxMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(35.720755, 51.427661),
                                    12.0
                                )
                            )
                            Toast.makeText(context, "مختصات مقصد مشخص نشده است", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                // My location button click listener
                myLocationBtn.setOnClickListener {
                    val userLocation =
                        com.yandex.divkit.demo.service.LocationService.getLastLocation(context)
                    if (userLocation != null) {
                        mapboxMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(userLocation.latitude, userLocation.longitude),
                                16.0
                            )
                        )
                        Toast.makeText(context, "موقعیت شما", Toast.LENGTH_SHORT).show()
                    } else {
                        // Call routing service only if routingRequired is true
                        if (isRoutingRequired) {
                        callRoutingService(
                            35.15,
                            52.20,
                            35.26,
                            52.54,
                            mapboxMap,
                            context
                        )
                        }
                        Toast.makeText(context, "موقعیت شما در دسترس نیست", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            addView(childView)
            mapContainer.addView(mapView, 0)
        }

    private fun callRoutingService(
        startLat: Double? = 51.4070744,
        startLon: Double? = 35.709912,
        endLat: Double? = 51.41549,
        endLon: Double? = 35.659111,
        mapboxMap: com.mapbox.mapboxsdk.maps.MapboxMap,
        context: Context
    ) {
        val routingBody = RoutBody().apply {
            latitudeStart = startLat
            longitudeStart = startLon
            latitudeEnd = endLat
            longitudeEnd = endLon
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = SingletonObjects.routingRetrofitInstance()
                val routingApi = retrofit?.create(RoutingApi::class.java)
                val response = routingApi?.routing(routingBody)

                response?.let { routeResponse ->
                    // Parse route coordinates from shape string
//                    val routeCoordinates = mutableListOf<LatLng>()
                    val routeCoordinates = mutableListOf<Point>()

                    routeResponse.shape
                        .replace("LINESTRING", "")
                        .replace("(", "")
                        .replace(")", "")
                        .split(",")
                        .forEach { coordinateString ->
                            val parts = coordinateString.trim().split(" ")
                            // Format: "lon lat" (parts[0] = lon, parts[1] = lat)
                            if (parts.size >= 2) {
                                val lon = parts[0].toDoubleOrNull()
                                val lat = parts[1].toDoubleOrNull()
                                if (lon != null && lat != null) {
//                                    routeCoordinates.add(LatLng(lat, lon))

                                    routeCoordinates.add(
                                        Point.fromLngLat(
                                            parts[0].toDouble(), parts[1].toDouble()
                                        )
                                    )
                                }
                            }
                        }

                    // Draw route on map (must be on main thread)
                    Handler(Looper.getMainLooper()).post {
                        if (routeCoordinates.isNotEmpty()) {
                            // Create GeoJsonSource on UI thread
                    geoJsonSource = GeoJsonSource(
                        sourceId, FeatureCollection.fromFeatures(
                            arrayOf(
                                Feature.fromGeometry(
                                    LineString.fromLngLats(routeCoordinates)
                                )
                            )
                        )
                    )

                            // Create LineLayer on UI thread
                    val lineLayer = LineLayer("linelayer", "line-source").withProperties(
//            lineDasharray(arrayOf(0.01f, 2f)),
                        lineCap(Property.LINE_CAP_ROUND),
                        lineJoin(Property.LINE_JOIN_ROUND),
                        lineWidth(5f),
                        lineColor(Color.parseColor("#ffcc0000"))
                    )

                            /*val polylineOptions = PolylineOptions()
                                .addAll(routeCoordinates)
                                .color(android.graphics.Color.parseColor("#3F51B5")) // Blue color
                                .width(5f)*/ // Line width

                            // Remove existing source and layer if they exist
                            try {
                                if (mapStyle.getSource(sourceId) != null) {
                                    mapStyle.removeSource(sourceId)
                                }
                            } catch (e: Exception) {
                                // Source doesn't exist, ignore
                            }
                            
                            try {
                                if (mapStyle.getLayer("linelayer") != null) {
                                    mapStyle.removeLayer("linelayer")
                                }
                            } catch (e: Exception) {
                                // Layer doesn't exist, ignore
                            }

                            mapStyle.addSource(
                                geoJsonSource
                            )
                            mapStyle.addLayer(
                                lineLayer
                            )

//                            updateMapStyle(routeCoordinates)
//                            mapboxMap.addPolyline(polylineOptions)
//                            android.util.Log.d("Navigation", "Route drawn with ${routeCoordinates.size} points")
                        }
                    }

                }
            } catch (e: Exception) {
                android.util.Log.e("Navigation", "Error calling routing service: ${e.message}", e)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        "خطا در دریافت مسیر: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /*private fun updateMapStyle(routeCoordinates : MutableList<Point>) {
        geoJsonSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(routeCoordinates)))
        setStyle()
    }*/

    private fun setStyle(routeCoordinates: MutableList<Point>) {
        // Remove existing source and layer if they exist
        try {
            if (mapStyle.getSource(sourceId) != null) {
                mapStyle.removeSource(sourceId)
            }
        } catch (e: Exception) {
            // Source doesn't exist, ignore
        }
        
        try {
            if (mapStyle.getLayer("linelayer") != null) {
                mapStyle.removeLayer("linelayer")
            }
        } catch (e: Exception) {
            // Layer doesn't exist, ignore
        }

        // Update or create GeoJsonSource
        try {
            val existingSource = mapStyle.getSource(sourceId) as? GeoJsonSource
            if (existingSource != null) {
                // Update existing source
                existingSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(routeCoordinates)))
            } else {
                // Create new source
        geoJsonSource = GeoJsonSource(
            sourceId, FeatureCollection.fromFeatures(
                arrayOf(
                    Feature.fromGeometry(
                        LineString.fromLngLats(routeCoordinates)
                    )
                )
            )
        )
                mapStyle.addSource(geoJsonSource)
            }
        } catch (e: Exception) {
            // If update fails, create new source
            geoJsonSource = GeoJsonSource(
                sourceId, FeatureCollection.fromFeatures(
                    arrayOf(
                        Feature.fromGeometry(
                            LineString.fromLngLats(routeCoordinates)
                        )
                    )
                )
            )
            mapStyle.addSource(geoJsonSource)
        }

        /* mapStyle.addImage(
             ("marker_icon"),
             BitmapFactory.decodeResource(
                 resources,
                 com.mapbox.mapboxsdk.R.drawable.mapbox_mylocation_icon_bearing
             )
         )*/


//        mapStyle.getImage("marker_icon")
//        mapStyle.removeImage("marker_icon")

        val lineLayer = LineLayer("linelayer", "line-source").withProperties(
//            lineDasharray(arrayOf(0.01f, 2f)),
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND),
            lineWidth(5f),
            lineColor(Color.parseColor("#ffcc0000"))
        )
        mapStyle.addLayer(
            lineLayer
        )

    }

    /*private fun setMapBoxStyle(mapView: MapView) {

        mapView.getMapAsync {

            map = it

            it.setStyle(MAP_BOX,
                object : Style.OnStyleLoaded {
                    override fun onStyleLoaded(style: Style) {

//                        locationEnable()

                        mapStyle = style


//                        initRouteCoordinates()
                        setStyle()

                        callRoutingService()

//                        binding.loadingCurrentLocation?.visibility = View.GONE

                        val latLng = LatLng()

                        if (currentMission != null) {
                            latLng.latitude = 35.713248
                            latLng.longitude = 51.404243

                            if (currentMission!!.easting != null) latLng.latitude =
                                currentMission!!.easting!!.toDouble()
                            if (currentMission!!.northing != null) latLng.longitude =
                                currentMission!!.northing!!.toDouble()

                            markerOptionsMission = MarkerOptions()
                            markerOptionsMission.position = latLng
                            markerOptionsMission.title = "موقعیت خبر"

//                            markerOptionsMission.icon = iconFactoryMission
                            map!!.addMarker(markerOptionsMission)
                        }

                        *//* latLng.latitude = 34.639493
                         latLng.longitude = 50.871849
                         markerOptionsMission.position = latLng
                         map!!.addMarker(markerOptionsMission)*//*
                    }
                })
            it.uiSettings.isQuickZoomGesturesEnabled = true
            it.uiSettings.isZoomGesturesEnabled = true
            it.uiSettings.isScrollGesturesEnabled = true
            it.uiSettings.setAllGesturesEnabled(true)
            it.uiSettings.isFlingVelocityAnimationEnabled = true
            it.uiSettings.isRotateVelocityAnimationEnabled = true
            it.uiSettings.setAllVelocityAnimationsEnabled(true)
            it.uiSettings.isScaleVelocityAnimationEnabled = true

        }
    }*/

}
