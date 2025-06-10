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
import com.yandex.div.json.expressions.ExpressionResolver
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
    private var seconds: Int = 0
    private var textSize: Int = 0
    private var counter: Int = 0
    private var timerButtonNext: String = ""
    private var systemForOffline: String = ""
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
            current = div.customProps?.get("current") as Int
//            currentTest = div.customProps?.get("$"+"current") as String
            total = div.customProps?.get("total") as Int
            textSize = div.customProps?.get("textSize") as Int
        }
        if (div.customType == "double_circular_progressView") {
            innerPercent = div.customProps?.get("inner_percent") as Int
            outerPercent = div.customProps?.get("outer_percent") as Int
            centerPercent = div.customProps?.get("center_percent") as Int
        }
        if (div.customType == "timer_button") {
            seconds = div.customProps?.get("seconds") as Int
            timerButtonNext = div.customProps?.get("next_page") as String
//            timerButtonNext = div.customProps?.get("next_page") as String

        }
        if (div.customType == "offline_list_container") {
            systemForOffline = div.customProps?.get("screen") as String

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
    private fun Context.labelledSliderView(): View = LabelledSliderView(this).apply {
        setItems(listOf("کم", "متوسط", "زیاد", "خیلی زیاد"))
            setOnItemSelectedListener { label, index ->
                Toast.makeText(context, "انتخاب شده: $label", Toast.LENGTH_SHORT).show()}
    }
    private fun Context.audioPlayerView(): View = AudioPlayerView(this).apply {

   setAudioUrl("https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3")
    }

    private fun Context.doubleCircularProgressView(): View =
        DoubleCircularProgressView(
            this,
            innerPercent = innerPercent.toFloat(),
            outerPercent = outerPercent.toFloat(),
            centerPercent = centerPercent
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

    override fun onClickedOfflineView(data: String, system: String) {
        loadScreenListener?.loadeScreenWithData(data, system)
    }
}
