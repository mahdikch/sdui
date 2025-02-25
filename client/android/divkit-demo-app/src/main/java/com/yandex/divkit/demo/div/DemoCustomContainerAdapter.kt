package com.yandex.divkit.demo.div

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
import com.yandex.divkit.demo.div.offlineVtAdapter.VtReportAdapter
import com.yandex.divkit.demo.ui.LoadScreenListener
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import java.util.ArrayList

class DemoCustomContainerAdapter(
    mehdiViewModel: MehdiViewModel? = null,
    lo: LifecycleOwner? = null,
    loadScreenListener: LoadScreenListener? = null
) : DivCustomContainerViewAdapter,VtReportAdapter.VtReportItemListener {
    private var loadScreenListener = loadScreenListener
    private var lo = lo
    private var mehdiViewModel = mehdiViewModel
    private lateinit var sharePref: SharePref
    private lateinit var adapter: VtReportAdapter

    private val factories = mapOf(
        "new_custom_card_1" to { context: Context -> context.createCustomCard() },
        "new_custom_card_2" to { context: Context -> context.createCustomCard() },
        "new_custom_container_1" to { context: Context -> context.createCustomContainer() },
        "map" to { context: Context -> context.createCustomMap() },
        "offline_vt_reports_container" to { context: Context -> context.createOfflineVtReportsContainer() }
    )

    override fun isCustomTypeSupported(type: String): Boolean = type in factories.keys
    override fun release(view: View, div: DivCustom) = Unit

    override fun createView(
        div: DivCustom,
        divView: Div2View,
        expressionResolver: ExpressionResolver,
        path: DivStatePath
    ): View {
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

                val map: MutableMap<String, String> = HashMap()
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

    private fun Context.createOfflineVtReportsContainer(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        removeAllViews()
        val childView = LayoutInflater.from(this@createOfflineVtReportsContainer)
            .inflate(com.yandex.divkit.demo.R.layout.offline_vt_reports, this, false)
        val recyclerview =
            childView.findViewById<RecyclerView>(com.yandex.divkit.demo.R.id.rvReports)
        recyclerview.layoutManager = LinearLayoutManager(this@createOfflineVtReportsContainer)
        adapter = VtReportAdapter(this@DemoCustomContainerAdapter)

        val items = ArrayList<String>()
        var username = mehdiViewModel?.getValueByKey("userName")?.value
        lo?.let {
            mehdiViewModel?.getVtOfflineReports("%$username/vt%")?.observe(it) {
            for(ph:PhPlusDB in it){
                items.add(ph.value)
            }
                adapter.setItems(items)
                recyclerview.adapter = adapter


            }
        }
        addView(childView)
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

    override fun onClickedVtReport(data: String) {
        loadScreenListener?.loadeScreenWithData(data,"vt/main")
    }
}
