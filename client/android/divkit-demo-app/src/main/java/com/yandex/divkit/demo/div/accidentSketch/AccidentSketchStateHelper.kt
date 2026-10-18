package com.yandex.divkit.demo.div.accidentSketch

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import com.caverock.androidsvg.SVG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.divkit.demo.div.drawing.MenuService
import com.yandex.divkit.demo.div.drawing.Shap
import com.yandex.divkit.demo.div.drawing.turboimageview.ImageObject
import com.yandex.divkit.demo.div.drawing.turboimageview.MultiTouchObject
import com.yandex.divkit.demo.div.drawing.turboimageview.TurboImageView
import java.util.UUID

data class SketchIconDef(
    val id: String,
    val title: String,
    val drawable: String? = null,
    val assetPath: String? = null,
)

object SketchIconLoader {
    private var cachedIcons: List<SketchIconDef>? = null

    private val folderTitles = mapOf(
        "Car" to "خودرو",
        "AgricultureCar" to "کشاورزی",
        "Animal" to "حیوان",
        "BaseShape" to "اشکال پایه",
        "Board" to "تابلو",
        "Climate" to "اقلیم",
        "ConstructionCar" to "عمرانی",
        "CrossRoads" to "تقاطع",
        "Curve" to "پیچ",
        "DirectWay" to "مسیر مستقیم",
        "FreeWay" to "بزرگراه",
        "FreightCar" to "بارکش",
        "Highway" to "جاده",
        "Loop" to "لوپ",
    )

    fun loadIcons(context: Context): List<SketchIconDef> {
        cachedIcons?.let { return it }
        val fromAssets = scanBitmapAssets(context)
        cachedIcons = if (fromAssets.isNotEmpty()) {
            fromAssets
        } else {
            loadIconsFromJson(context)
        }
        return cachedIcons.orEmpty()
    }

    private fun scanBitmapAssets(context: Context): List<SketchIconDef> {
        val result = mutableListOf<SketchIconDef>()
        var index = 0
        try {
            val folders = context.assets.list("bitmaps") ?: emptyArray()
            for (folder in folders.sorted()) {
                val files = context.assets.list("bitmaps/$folder") ?: emptyArray()
                val categoryTitle = folderTitles[folder] ?: folder
                for (file in files.sorted()) {
                    if (!isImageAsset(file)) continue
                    val assetPath = "bitmaps/$folder/$file"
                    val name = file.substringBeforeLast('.')
                    result.add(
                        SketchIconDef(
                            id = String.format("%04d", index++),
                            title = "$categoryTitle - $name",
                            assetPath = assetPath,
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // ignore
        }
        return result
    }

    private fun loadIconsFromJson(context: Context): List<SketchIconDef> {
        return try {
            val json = context.assets.open("application/accident_sketch_icons.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<SketchIconDef>>() {}.type
            Gson().fromJson<List<SketchIconDef>>(json, type).orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun isImageAsset(fileName: String): Boolean {
        val lower = fileName.lowercase()
        return lower.endsWith(".png") ||
            lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg") ||
            lower.endsWith(".webp") ||
            lower.endsWith(".svg")
    }

    fun findIcon(context: Context, id: String?): SketchIconDef? {
        if (id.isNullOrEmpty()) return null
        return loadIcons(context).firstOrNull { it.id == id }
    }

    fun toMenuService(context: Context, icon: SketchIconDef): MenuService {
        val bitmap = loadBitmapForIcon(context, icon)
            ?: Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        return MenuService().apply {
            setId(icon.id)
            setTitle(icon.title)
            setName(icon.title)
            setParentName("")
            setIdUUId(UUID.randomUUID().toString())
            setIconBitmap(bitmap)
        }
    }

    fun loadBitmapForIcon(context: Context, icon: SketchIconDef, targetSize: Int = 256): Bitmap? {
        icon.assetPath?.let { return bitmapFromAsset(context, it, targetSize) }
        icon.drawable?.let { name ->
            val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (resId != 0) return BitmapFactory.decodeResource(context.resources, resId)
        }
        return null
    }

    fun bitmapFromAsset(context: Context, assetPath: String, targetSize: Int = 256): Bitmap? {
        return try {
            if (assetPath.lowercase().endsWith(".svg")) {
                renderSvgToBitmap(context, assetPath, targetSize)
            } else {
                context.assets.open(assetPath).use { stream ->
                    val decoded = BitmapFactory.decodeStream(stream) ?: return null
                    if (decoded.width <= targetSize && decoded.height <= targetSize) decoded
                    else Bitmap.createScaledBitmap(decoded, targetSize, targetSize, true)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun renderSvgToBitmap(context: Context, assetPath: String, targetSize: Int): Bitmap {
        val svg = SVG.getFromAsset(context.assets, assetPath)
        val docWidth = svg.documentWidth
        val docHeight = svg.documentHeight
        val width: Int
        val height: Int
        if (docWidth > 0 && docHeight > 0) {
            val ratio = docWidth / docHeight
            if (ratio >= 1f) {
                width = targetSize
                height = (targetSize / ratio).toInt().coerceAtLeast(1)
            } else {
                height = targetSize
                width = (targetSize * ratio).toInt().coerceAtLeast(1)
            }
        } else {
            width = targetSize
            height = targetSize
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        svg.renderToCanvas(canvas)
        return bitmap
    }

    fun bitmapFromBase64(base64: String?): Bitmap? {
        if (base64.isNullOrEmpty()) return null
        return try {
            val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }
}

object AccidentSketchStateHelper {
    private val gson = Gson()

    fun serializeObjects(objects: ArrayList<MultiTouchObject>): String {
        return gson.toJson(objects)
    }

    fun restoreObjects(context: Context, json: String?): ArrayList<MultiTouchObject> {
        if (json.isNullOrBlank()) return arrayListOf()
        return try {
            val type = object : TypeToken<ArrayList<ImageObject>>() {}.type
            val parsed: ArrayList<ImageObject> = gson.fromJson(json, type)
            val result = arrayListOf<MultiTouchObject>()
            for (source in parsed) {
                result.add(rebuildImageObject(context, source))
            }
            result
        } catch (_: Exception) {
            arrayListOf()
        }
    }

    private fun rebuildImageObject(context: Context, source: ImageObject): ImageObject {
        val res = context.resources
        val iconDef = SketchIconLoader.findIcon(context, source.idImageObject)
        val bitmap = when {
            !source.base64.isNullOrEmpty() -> SketchIconLoader.bitmapFromBase64(source.base64)
            iconDef != null -> SketchIconLoader.loadBitmapForIcon(context, iconDef)
            else -> null
        }
        val imageObject = if (bitmap != null) {
            ImageObject(bitmap, res, source)
        } else {
            ImageObject(res)
        }
        copyState(source, imageObject)
        if (!source.base64.isNullOrEmpty()) {
            imageObject.base64 = source.base64
        }
        imageObject.setFirstLoad(false)
        return imageObject
    }

    private fun copyState(source: ImageObject, target: ImageObject) {
        target.setAngle(source.angle)
        target.setBorderColor(source.borderColor)
        target.setCenterX(source.centerX)
        target.setCenterY(source.centerY)
        target.setDisplayHeight(source.displayHeight)
        target.setDisplayWidth(source.displayWidth)
        target.setFirstLoad(source.isFirstLoad)
        target.setFlippedHorizontally(source.isFlippedHorizontally)
        target.setGrabAreaSelected(source.isGrabAreaSelected)
        target.setGrabAreaX1(source.grabAreaX1)
        target.setGrabAreaX2(source.grabAreaX2)
        target.setGrabAreaY1(source.grabAreaY1)
        target.setGrabAreaY2(source.grabAreaY2)
        target.setHeight(source.height)
        target.setLatestSelected(false)
        target.setMaxX(source.maxX)
        target.setMaxY(source.maxY)
        target.setMinX(source.minX)
        target.setMinY(source.minY)
        target.setScaleX(source.scaleX)
        target.setScaleY(source.scaleY)
        target.setStartMidX(source.startMidX)
        target.setStartMidY(source.startMidY)
        target.setWidth(source.width)
        target.setStrIdUUId(source.strIdUUId)
        target.setStrName(source.strName)
        target.setStrParentName(source.strParentName)
        target.setId(source.id)
        target.setTitle(source.title)
        target.setIndexObject(source.indexObject)
        target.setIntColor(source.intColor)
        target.setIdImageObject(source.idImageObject)
        target.setTitleImageObject(source.titleImageObject)
        target.setNameImageObject(source.nameImageObject)
        target.setParentNameImageObject(source.parentNameImageObject)
        target.setX(source.x)
        target.setY(source.y)
        target.setSelected(false)
    }

    fun buildShapeList(objects: ArrayList<MultiTouchObject>): ArrayList<Shap> {
        val shapes = arrayListOf<Shap>()
        for (obj in objects) {
            if (obj !is ImageObject) continue
            val shap = Shap()
            shap.setId(obj.idImageObject ?: obj.id ?: "")
            shap.setIdUUId(obj.strIdUUId ?: "")
            shap.setTitle(obj.titleImageObject ?: obj.title ?: "")
            val drawable = obj.drawable
            if (drawable is BitmapDrawable) {
                shap.setBitmap(drawable.bitmap)
            }
            shapes.add(shap)
        }
        return shapes
    }

    fun renderCompositeBitmap(background: Bitmap?, turboView: TurboImageView): Bitmap {
        val width = turboView.width.coerceAtLeast(1)
        val height = turboView.height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        background?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        turboView.draw(canvas)
        return bitmap
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        return android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
    }
}
