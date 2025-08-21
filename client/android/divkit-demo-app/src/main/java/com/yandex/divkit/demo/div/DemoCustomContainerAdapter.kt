package com.yandex.divkit.demo.div

import OfflineViewList
import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng

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
import com.yandex.divkit.demo.ui.LoadScreenListener
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import java.util.ArrayList
import android.util.Log

class DemoCustomContainerAdapter(
    mehdiViewModel: MehdiViewModel? = null,
    lo: LifecycleOwner? = null,
    loadScreenListener: LoadScreenListener? = null
) : DivCustomContainerViewAdapter,
    OfflineViewListAdapter.OfflineViewListItemListener {
    private var loadScreenListener = loadScreenListener
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
    private var timerButtonParam1: String = ""
    private var timerButtonParam2: String = ""
    private var timerButtonParam3: String = ""
    private var timerButtonParam4: String = ""
    private var timerButtonParam5: String = ""
    private var timerButtonParam6: String = ""
    private var timerButtonParam7: String = ""
    private var timerButtonParam8: String = ""
    private var timerButtonParam9: String = ""
    private var timerButtonParam10: String = ""
    private var systemForOffline: String = ""
    private var audioUrl: String = ""
    private var labelList: String = ""
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
        "audioPlayerView" to { context: Context -> context.audioPlayerView() },
        "labelledSliderView" to { context: Context -> context.labelledSliderView() },
//        "offline_vt_reports_container" to { context: Context -> context.createOfflineVtReportsContainer() },
        "offline_list_container" to { context: Context -> context.createOfflineListContainer() }
    )

    override fun isCustomTypeSupported(type: String): Boolean = type in factories.keys
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
            timerButtonParam1 = evaluateCustomPropString(div, "param1", expressionResolver, "")
            timerButtonParam2 = evaluateCustomPropString(div, "param2", expressionResolver, "")
            timerButtonParam3 = evaluateCustomPropString(div, "param3", expressionResolver, "")
            timerButtonParam4 = evaluateCustomPropString(div, "param4", expressionResolver, "")
            timerButtonParam5 = evaluateCustomPropString(div, "param5", expressionResolver, "")
            timerButtonParam6 = evaluateCustomPropString(div, "param6", expressionResolver, "")
            timerButtonParam7 = evaluateCustomPropString(div, "param7", expressionResolver, "")
            timerButtonParam8 = evaluateCustomPropString(div, "param8", expressionResolver, "")
            timerButtonParam9 = evaluateCustomPropString(div, "param9", expressionResolver, "")
            timerButtonParam10 = evaluateCustomPropString(div, "param10", expressionResolver, "")
            Log.d(
                "DemoCustomContainer",
                "evaluateCustomPropString result for 'next_page': $timerButtonNext"
            )
            Log.d("DemoCustomContainer", "=== END TIMER BUTTON DEBUG ===")
        }
        if (div.customType == "offline_list_container") {
            systemForOffline = evaluateCustomPropString(div, "screen", expressionResolver, "")
        }
        if (div.customType == "audioPlayerView") {
            audioUrl = evaluateCustomPropString(div, "url", expressionResolver, "")
        }
        if (div.customType == "labelledSliderView") {
            labelList = evaluateCustomPropString(div, "label_list", expressionResolver, "")
        }
        val customView = factories[div.customType]?.invoke(divView.context)
            ?: throw IllegalStateException("Can not create view for unsupported custom type ${div.customType}")
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

        return customView
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
                if (rawValue.startsWith("@{") && rawValue.endsWith("}")) {
                    try {
                        // Extract the variable name from @{variable_name}
                        val variableName = rawValue.substring(2, rawValue.length - 1)

                        // Create a proper expression string and evaluate it
                        val expressionString = "@{$variableName}"
                        val evaluable = com.yandex.div.evaluable.Evaluable.lazy(expressionString)

                        // Use the expression resolver to get the value
                        val result = expressionResolver.get<Any, String>(
                            key,
                            expressionString,
                            evaluable,
                            null,
                            object : ValueValidator<String> {
                                override fun isValid(value: String): Boolean = true
                            },
                            object : TypeHelper<String> {
                                override fun isTypeValid(value: Any): Boolean = value is String
                                override val typeDefault: String = defaultValue
                            },
                            ParsingErrorLogger.LOG
                        )

                        result ?: defaultValue
                    } catch (e: Exception) {
                        // If evaluation fails, return the raw value
                        rawValue
                    }
                } else {
                    rawValue
                }
            }

            else -> defaultValue
        }
    }

    override fun bindView(
        customView: View,
        div: DivCustom,
        divView: Div2View,
        expressionResolver: ExpressionResolver,
        path: DivStatePath
    ) {
        when (div.customType) {
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

    private fun Context.timerButtonCallService(): View = TimerButton(this, seconds = seconds).apply {
            setOnClickListener {
                val map: HashMap<String, String> = HashMap()
                map["path"] = timerButtonPath
                map[timerButtonParam1] = "empty"
                map[timerButtonParam2] = "empty"
                map[timerButtonParam3] = "empty"
                loadScreenListener?.onRequest(map)
            }
        }

    private fun Context.labelledSliderView(): View = LabelledSliderView(this).apply {
        val list = labelList.split(",").map { it.trim() }
        setItems(list)
//        setItems(listOf("کم", "متوسط", "زیاد", "خیلی زیاد"))
        setOnItemSelectedListener { label, index ->
            Toast.makeText(context, "انتخاب شده: $label", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Context.audioPlayerView(): View = AudioPlayerView(this).apply {

//   setAudioUrl("https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3")
        setAudioUrl(audioUrl)
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
            onCreate(null)

            getMapAsync { mapboxMap ->

                // Set the custom style URL
                mapboxMap.setStyle("http://mapbox-gis.naja.net:7070/styles/rezvani-style/style.json") {
                    // Optional: Add additional setup after the style loads
                    Toast.makeText(context, "Map style loaded successfully!", Toast.LENGTH_SHORT)
                        .show()
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
        }
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
        mapContainer.addView(mapView, 0)


//        val mapView = MapView(this@createCustomMap)
//        mapView.layoutParams = FrameLayout.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        )
        addView(childView)
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

    private fun Context.createOfflineListContainer(): View =
        OfflineViewList(
            this,
            system = systemForOffline,
            listener = this@DemoCustomContainerAdapter
        ).apply {
            var username = mehdiViewModel?.getValueByKey("userName")?.value
            var data: MutableList<Map<String, String>> = mutableListOf()
            lo?.let {
                mehdiViewModel?.getVtOfflineReports("%$username/$systemForOffline%")?.observe(it) {
                    for (ph: PhPlusDB in it) {

                        data.add(mapOf("data" to ph.value!!))
                    }
                    setData(data)

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
        loadScreenListener?.loadeScreenWithData(data, next)
    }
}
