package com.yandex.divkit.demo.div.accidentSketch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.snapshotter.MapSnapshotter
import com.yandex.divkit.demo.R

class AccidentSketchMapPickerActivity : AppCompatActivity() {

    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var mapSnapshotter: MapSnapshotter? = null
    private var snapshotInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accident_sketch_map_picker)

        Mapbox.getInstance(this, "")
        val container = findViewById<FrameLayout>(R.id.mapPickerContainer)
        mapView = MapView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            onCreate(savedInstanceState)
        }
        container.addView(mapView)

        mapView?.getMapAsync { map ->
            mapboxMap = map
            map.setStyle(Style.Builder().fromUri(MAP_STYLE)) {
                val lat = intent.getDoubleExtra(EXTRA_LAT, DEFAULT_LAT)
                val lon = intent.getDoubleExtra(EXTRA_LON, DEFAULT_LON)
                map.moveCamera(
                    com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom(
                        LatLng(lat, lon),
                        16.0,
                    )
                )
            }
        }

        findViewById<Button>(R.id.btnMapCancel).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        findViewById<Button>(R.id.btnMapConfirm).setOnClickListener { captureSnapshot() }
    }

    private fun captureSnapshot() {
        val map = mapboxMap ?: return
        val view = mapView ?: return
        if (snapshotInProgress) return
        snapshotInProgress = true

        val bounds = map.projection.visibleRegion.latLngBounds
        val width = view.width.coerceAtLeast(1)
        val height = view.height.coerceAtLeast(1)
        val cameraPosition = map.cameraPosition
        val bearing = -cameraPosition.bearing.toFloat()

        map.setStyle(Style.Builder().fromUri(MAP_STYLE)) { style ->
            val options = MapSnapshotter.Options(width, height)
                .withRegion(bounds)
                .withCameraPosition(cameraPosition)
                .withStyleBuilder(Style.Builder().fromUri(style.uri))
                .withLogo(true)

            mapSnapshotter?.cancel()
            mapSnapshotter = MapSnapshotter(this, options).also { snapshotter ->
                snapshotter.start { snapshot ->
                    snapshotInProgress = false
                    if (snapshot != null) {
                        val base64 = AccidentSketchStateHelper.bitmapToBase64(snapshot.bitmap)
                        setResult(
                            Activity.RESULT_OK,
                            Intent().apply {
                                putExtra(EXTRA_BG_BASE64, base64)
                                putExtra(EXTRA_MAP_BEARING, bearing)
                            }
                        )
                        finish()
                    } else {
                        Toast.makeText(this, "خطا در گرفتن تصویر نقشه", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        mapView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapSnapshotter?.cancel()
        mapView?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    companion object {
        private const val MAP_STYLE = "http://mapbox-gis.naja.net:7070/styles/rezvani-style/style.json"
        private const val DEFAULT_LAT = 35.711
        private const val DEFAULT_LON = 51.389

        const val EXTRA_BG_BASE64 = "sketch_map_bg_base64"
        const val EXTRA_MAP_BEARING = "sketch_map_bearing"
        const val EXTRA_LAT = "sketch_map_lat"
        const val EXTRA_LON = "sketch_map_lon"

        fun createIntent(context: Context): Intent {
            return Intent(context, AccidentSketchMapPickerActivity::class.java)
        }
    }
}
