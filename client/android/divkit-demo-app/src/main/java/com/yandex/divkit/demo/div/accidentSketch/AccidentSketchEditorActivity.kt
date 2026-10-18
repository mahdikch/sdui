package com.yandex.divkit.demo.div.accidentSketch

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yandex.divkit.demo.R
import com.yandex.divkit.demo.div.drawing.AppConstants
import com.yandex.divkit.demo.div.drawing.InterfaceUUID
import com.yandex.divkit.demo.div.drawing.MenuService
import com.yandex.divkit.demo.div.drawing.MyItemTouchHelperCallback
import com.yandex.divkit.demo.div.drawing.OnStartDragListener
import com.yandex.divkit.demo.div.drawing.PaintView
import com.yandex.divkit.demo.div.drawing.Shap
import com.yandex.divkit.demo.div.drawing.Step8Adapter
import com.yandex.divkit.demo.div.drawing.turboimageview.MultiTouchObject
import com.yandex.divkit.demo.div.drawing.turboimageview.TurboImageView
import com.yandex.divkit.demo.div.drawing.turboimageview.TurboImageViewListener
import java.io.File
import java.util.UUID

class AccidentSketchEditorActivity : AppCompatActivity(),
    Step8Adapter.ItemClickListener,
    OnStartDragListener,
    TurboImageViewListener,
    InterfaceUUID {

    private lateinit var turboImageView: TurboImageView
    private lateinit var backgroundView: ImageView
    private lateinit var objectList: RecyclerView
    private lateinit var layoutNorth: View
    private lateinit var arrowNorth: ImageView
    private lateinit var touchCheckBox: CheckBox
    private lateinit var soundPathLabel: TextView
    private lateinit var adapter: Step8Adapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    private val shapes = ArrayList<Shap>()
    private var selectedPosition = -1
    private var touchEnabled = true
    private var mapNorthRotation = 0f
    private var hasMapBackground = false
    private var soundFilePath: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accident_sketch_editor)

        turboImageView = findViewById(R.id.sketchTurboImageView)
        backgroundView = findViewById(R.id.sketchBackground)
        objectList = findViewById(R.id.sketchObjectList)
        layoutNorth = findViewById(R.id.layoutNorth)
        arrowNorth = findViewById(R.id.arrowNorth)
        touchCheckBox = findViewById(R.id.chkSketchTouch)
        soundPathLabel = findViewById(R.id.txtSoundPath)

        adapter = Step8Adapter(this)
        adapter.setCallback(this)
        adapter.setClickListener(this)
        objectList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        objectList.adapter = adapter
        objectList.setHasFixedSize(false)
        objectList.isNestedScrollingEnabled = true

        itemTouchHelper = ItemTouchHelper(MyItemTouchHelperCallback(adapter))
        itemTouchHelper.attachToRecyclerView(objectList)

        turboImageView.setListener(this)
        turboImageView.setListenerUUID(this)
        turboImageView.setTouchForDraw(touchEnabled)
        touchCheckBox.isChecked = touchEnabled
        touchCheckBox.setOnCheckedChangeListener { _, isChecked ->
            touchEnabled = isChecked
            turboImageView.deselectAll()
            turboImageView.setTouchForDraw(isChecked)
            adapter.deselectItems(isChecked)
        }

        val initialJson = intent.getStringExtra(EXTRA_JSON).orEmpty()
        val initialBgBase64 = intent.getStringExtra(EXTRA_BG_BASE64)
        soundFilePath = intent.getStringExtra(EXTRA_SOUND_PATH)
        updateSoundLabel()

        if (!initialBgBase64.isNullOrEmpty()) {
            SketchIconLoader.bitmapFromBase64(initialBgBase64)?.let {
                backgroundView.setImageBitmap(it)
            }
        }
        restoreFromJson(initialJson)

        findViewById<Button>(R.id.btnSketchAddIcon).setOnClickListener { showIconPicker() }
        findViewById<Button>(R.id.btnSketchTitle).setOnClickListener { showTextDialog(isTitle = true) }
        findViewById<Button>(R.id.btnSketchDesc).setOnClickListener { showTextDialog(isTitle = false) }
        findViewById<Button>(R.id.btnSketchDigit).setOnClickListener { showDigitDialog() }
        findViewById<Button>(R.id.btnSketchDraw).setOnClickListener { showPaintDialog() }
        findViewById<Button>(R.id.btnSketchFlip).setOnClickListener {
            turboImageView.toggleFlippedHorizontallySelectedObject()
        }
        findViewById<Button>(R.id.btnSketchDelete).setOnClickListener { removeSelectedObject() }
        findViewById<Button>(R.id.btnSketchDeleteAll).setOnClickListener { removeAllObjects() }
        findViewById<Button>(R.id.btnSketchSelectAll).setOnClickListener {
            turboImageView.selectAll()
            adapter.selectItems()
        }
        findViewById<Button>(R.id.btnSketchDeselectAll).setOnClickListener {
            turboImageView.deselectAll()
            adapter.deselectItems(false)
        }
        findViewById<Button>(R.id.btnSketchMap).setOnClickListener { openMapPicker() }
        findViewById<Button>(R.id.btnSketchGallery).setOnClickListener { openGallery() }
        findViewById<Button>(R.id.btnSketchCamera).setOnClickListener { openCamera() }
        findViewById<Button>(R.id.btnSketchResetBg).setOnClickListener { resetBackground() }
        findViewById<Button>(R.id.btnSketchSound).setOnClickListener { showSoundDialog() }
        findViewById<Button>(R.id.btnSketchCancel).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        findViewById<Button>(R.id.btnSketchSave).setOnClickListener { saveAndFinish() }
    }

    private fun restoreFromJson(json: String) {
        val objects = AccidentSketchStateHelper.restoreObjects(this, json)
        objects.forEach { it.setFirstLoad(false) }
        if (objects.isNotEmpty()) {
            turboImageView.setObject(objects)
        }
        shapes.clear()
        shapes.addAll(AccidentSketchStateHelper.buildShapeList(turboImageView.getObject()))
        adapter.setAdapter(shapes)
        turboImageView.reorderObjectsByShapes(shapes)
    }

    private fun showIconPicker() {
        val icons = SketchIconLoader.loadIcons(this)
        if (icons.isEmpty()) {
            Toast.makeText(this, "آیکونی در assets/bitmaps یافت نشد", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = BottomSheetDialog(this)
        val scroll = android.widget.ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val grouped = icons.groupBy { icon ->
            icon.title.substringBefore(" - ", icon.title)
        }

        grouped.forEach { (category, categoryIcons) ->
            container.addView(TextView(this).apply {
                text = category
                textSize = 14f
                setTextColor(Color.parseColor("#333333"))
                setPadding(0, 12, 0, 8)
            })
            val grid = GridLayout(this).apply { columnCount = 4 }
            categoryIcons.forEach { icon ->
                val menu = SketchIconLoader.toMenuService(this, icon)
                val itemView = LayoutInflater.from(this).inflate(R.layout.sketch_icon_item, grid, false)
                itemView.findViewById<ImageView>(R.id.sketchIconImage).setImageBitmap(menu.iconBitmap)
                itemView.setOnClickListener {
                    addMenuObject(menu)
                    dialog.dismiss()
                }
                grid.addView(itemView)
            }
            container.addView(grid)
        }

        scroll.addView(container)
        dialog.setContentView(scroll)
        dialog.show()
    }

    private fun addMenuObject(menu: MenuService) {
        menu.setIdUUId(UUID.randomUUID().toString())
        turboImageView.addObject(this, menu.iconBitmap, menu.id, menu.idUUId, menu, true)
        shapes.add(Shap().apply {
            setBitmap(menu.iconBitmap)
            setId(menu.id)
            setIdUUId(menu.idUUId)
            setTitle(menu.title)
        })
        adapter.setAdapter(shapes)
    }

    private fun showTextDialog(isTitle: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sketch_add_text, null)
        val titleView = dialogView.findViewById<TextView>(R.id.txtDialogTitle)
        val editText = dialogView.findViewById<EditText>(R.id.edtSketchText)
        val preview = dialogView.findViewById<TextView>(R.id.txtPreview)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioColors)

        titleView.text = if (isTitle) "عنوان را وارد کنید" else "توضیح را وارد کنید"
        if (isTitle) {
            editText.filters = arrayOf(InputFilter.LengthFilter(30))
        }

        var textColor = Color.BLACK
        fun updatePreviewColor() {
            preview.setTextColor(textColor)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            textColor = when (checkedId) {
                R.id.rdbRed -> Color.parseColor("#C62828")
                R.id.rdbBlue -> Color.parseColor("#1565C0")
                else -> Color.BLACK
            }
            updatePreviewColor()
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                preview.text = s?.toString().orEmpty()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val text = editText.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "متن نمی‌تواند خالی باشد", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val bitmap = SketchTextBitmapHelper.renderTextBitmap(text, textColor, 20f)
            addTextObject(bitmap, text, if (isTitle) ID_TITLE else ID_DESC, if (isTitle) "عنوان" else "توضیح")
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDigitDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sketch_add_digit, null)
        val editText = dialogView.findViewById<EditText>(R.id.edtSketchDigit)
        val preview = dialogView.findViewById<TextView>(R.id.txtDigitPreview)
        val errorView = dialogView.findViewById<TextView>(R.id.txtDigitError)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioDigitColors)

        var textColor = Color.BLACK
        var circleRes = R.drawable.sketch_digit_circle_black

        fun updateDigitStyle() {
            preview.setTextColor(textColor)
            preview.setBackgroundResource(circleRes)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rdbDigitRed -> {
                    textColor = Color.parseColor("#C62828")
                    circleRes = R.drawable.sketch_digit_circle_red
                }
                R.id.rdbDigitBlue -> {
                    textColor = Color.parseColor("#1565C0")
                    circleRes = R.drawable.sketch_digit_circle_blue
                }
                R.id.rdbDigitGreen -> {
                    textColor = Color.parseColor("#2E7D32")
                    circleRes = R.drawable.sketch_digit_circle_green
                }
                else -> {
                    textColor = Color.BLACK
                    circleRes = R.drawable.sketch_digit_circle_black
                }
            }
            updateDigitStyle()
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val value = s?.toString().orEmpty()
                val valid = value.isEmpty() || value.all { it.isDigit() }
                errorView.visibility = if (valid) View.GONE else View.VISIBLE
                preview.text = value
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val text = editText.text.toString().trim()
            if (text.isEmpty() || !text.all { it.isDigit() }) {
                Toast.makeText(this, "عدد معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val drawable = ContextCompat.getDrawable(this, circleRes)!!
            val bitmap = SketchTextBitmapHelper.renderDigitBitmap(text, textColor, drawable)
            addTextObject(bitmap, text, ID_DIGIT, "عدد")
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun addTextObject(bitmap: Bitmap, label: String, objectId: String, parentTitle: String) {
        val id = UUID.randomUUID().toString()
        val base64 = AccidentSketchStateHelper.bitmapToBase64(bitmap)
        turboImageView.addObject(
            this, bitmap, objectId, label, Color.BLACK, id, true, base64, parentTitle
        )
        shapes.add(Shap().apply {
            setBitmap(bitmap)
            setId(objectId)
            setIdUUId(id)
            setTitle(parentTitle)
        })
        adapter.setAdapter(shapes)
    }

    private fun showPaintDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sketch_paint, null)
        val paintView = dialogView.findViewById<PaintView>(R.id.sketchPaintView)
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)
        paintView.init(metrics, 5, Color.WHITE)

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialogView.findViewById<Button>(R.id.btnPaintUndo).setOnClickListener { paintView.unDo() }
        dialogView.findViewById<Button>(R.id.btnPaintClear).setOnClickListener { paintView.clear() }
        dialogView.findViewById<Button>(R.id.btnPaintCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnPaintSave).setOnClickListener {
            paintView.isDrawingCacheEnabled = true
            paintView.buildDrawingCache()
            val bitmap = Bitmap.createBitmap(paintView.drawingCache)
            paintView.isDrawingCacheEnabled = false

            val id = UUID.randomUUID().toString()
            val base64 = AccidentSketchStateHelper.bitmapToBase64(bitmap)
            turboImageView.addObject(
                this, bitmap, ID_PAINT, "نقاشی", Color.BLACK, id, true, base64, "نقاشی"
            )
            shapes.add(Shap().apply {
                setBitmap(bitmap)
                setId(ID_PAINT)
                setIdUUId(id)
                setTitle("نقاشی")
            })
            adapter.setAdapter(shapes)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showSoundDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sketch_sound, null)
        val statusView = dialogView.findViewById<TextView>(R.id.txtSoundStatus)
        val recordBtn = dialogView.findViewById<Button>(R.id.btnRecordToggle)
        val deleteBtn = dialogView.findViewById<Button>(R.id.btnDeleteSound)

        fun refreshSoundUi() {
            val hasSound = !soundFilePath.isNullOrEmpty() && File(soundFilePath!!).exists()
            deleteBtn.visibility = if (hasSound) View.VISIBLE else View.GONE
            statusView.text = when {
                isRecording -> "در حال ضبط..."
                hasSound -> "صدا ذخیره شد: ${File(soundFilePath!!).name}"
                else -> "برای ضبط، دکمه را بزنید"
            }
            recordBtn.text = if (isRecording) "توقف ضبط" else "شروع ضبط"
        }
        refreshSoundUi()

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        recordBtn.setOnClickListener {
            if (isRecording) {
                stopRecording()
                refreshSoundUi()
                updateSoundLabel()
            } else {
                if (!hasRecordPermission()) {
                    requestRecordPermission()
                    return@setOnClickListener
                }
                if (!soundFilePath.isNullOrEmpty() && File(soundFilePath!!).exists()) {
                    Toast.makeText(this, "ابتدا صدای قبلی را حذف کنید", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                startRecording()
                refreshSoundUi()
            }
        }
        deleteBtn.setOnClickListener {
            soundFilePath?.let { File(it).delete() }
            soundFilePath = null
            refreshSoundUi()
            updateSoundLabel()
        }
        dialogView.findViewById<Button>(R.id.btnClose).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun hasRecordPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun requestRecordPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO,
        )
    }

    private fun startRecording() {
        try {
            val dir = File(cacheDir, "sketch_audio").apply { mkdirs() }
            val file = File(dir, "sketch_${System.currentTimeMillis()}.amr")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            soundFilePath = file.absolutePath
            isRecording = true
        } catch (e: Exception) {
            Toast.makeText(this, "خطا در ضبط صدا", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {
            // ignore
        }
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
    }

    private fun updateSoundLabel() {
        soundPathLabel.text = soundFilePath?.let { "صدا: $it" }.orEmpty()
    }

    private fun openMapPicker() {
        startActivityForResult(AccidentSketchMapPickerActivity.createIntent(this), REQUEST_MAP)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            return
        }
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CAMERA)
    }

    private fun resetBackground() {
        backgroundView.setImageBitmap(null)
        backgroundView.setBackgroundResource(R.drawable.bg_accident_paint)
        hasMapBackground = false
        mapNorthRotation = 0f
        layoutNorth.visibility = View.INVISIBLE
    }

    private fun applyImageBackground(bitmap: Bitmap) {
        hasMapBackground = false
        mapNorthRotation = 0f
        layoutNorth.visibility = View.INVISIBLE
        backgroundView.background = null
        backgroundView.setImageBitmap(bitmap)
    }

    private fun applyMapBackground(mapBitmap: Bitmap, bearing: Float) {
        hasMapBackground = true
        mapNorthRotation = bearing
        arrowNorth.rotation = bearing
        layoutNorth.visibility = View.VISIBLE

        val composite = compositeMapWithNorth(mapBitmap)
        backgroundView.background = null
        backgroundView.setImageBitmap(composite)
        layoutNorth.visibility = View.INVISIBLE
    }

    private fun compositeMapWithNorth(mapBitmap: Bitmap): Bitmap {
        layoutNorth.visibility = View.VISIBLE
        layoutNorth.isDrawingCacheEnabled = true
        layoutNorth.buildDrawingCache()
        val northBitmap = Bitmap.createBitmap(layoutNorth.drawingCache)
        layoutNorth.isDrawingCacheEnabled = false

        val result = Bitmap.createBitmap(mapBitmap.width, mapBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(mapBitmap, 0f, 0f, null)
        val x = mapBitmap.width - northBitmap.width
        val y = mapBitmap.height - northBitmap.height
        canvas.drawBitmap(northBitmap, x.toFloat(), y.toFloat(), null)
        return result
    }

    private fun removeSelectedObject() {
        val removedId = turboImageView.removeSelectedObject_UUID()
        if (removedId.isNullOrEmpty()) {
            Toast.makeText(this, "شی‌ای انتخاب نشده", Toast.LENGTH_SHORT).show()
            return
        }
        shapes.removeAll { it.idUUId == removedId }
        adapter.setNotifyDataSetChanged()
    }

    private fun removeAllObjects() {
        turboImageView.removeAllObjects()
        shapes.clear()
        adapter.setAdapter(shapes)
    }

    private fun saveAndFinish() {
        if (turboImageView.sizeListObject <= 0) {
            Toast.makeText(this, "حداقل یک شکل اضافه کنید", Toast.LENGTH_SHORT).show()
            return
        }

        turboImageView.deselectAll()
        val json = AccidentSketchStateHelper.serializeObjects(turboImageView.getObject())
        val bgBitmap = backgroundView.drawable?.let { drawable ->
            when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                else -> null
            }
        } ?: run {
            if (backgroundView.drawable == null && backgroundView.background is BitmapDrawable) {
                (backgroundView.background as BitmapDrawable).bitmap
            } else null
        }
        val composite = AccidentSketchStateHelper.renderCompositeBitmap(bgBitmap, turboImageView)
        val imageBase64 = AccidentSketchStateHelper.bitmapToBase64(composite)
        val bgBase64 = bgBitmap?.let { AccidentSketchStateHelper.bitmapToBase64(it) }

        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(EXTRA_JSON, json)
                putExtra(EXTRA_IMAGE_BASE64, imageBase64)
                if (bgBase64 != null) putExtra(EXTRA_BG_BASE64, bgBase64)
                if (!soundFilePath.isNullOrEmpty()) putExtra(EXTRA_SOUND_PATH, soundFilePath)
            }
        )
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_MAP -> {
                val base64 = data?.getStringExtra(AccidentSketchMapPickerActivity.EXTRA_BG_BASE64)
                val bearing = data?.getFloatExtra(AccidentSketchMapPickerActivity.EXTRA_MAP_BEARING, 0f) ?: 0f
                val bitmap = SketchIconLoader.bitmapFromBase64(base64)
                if (bitmap != null) applyMapBackground(bitmap, bearing)
            }
            REQUEST_GALLERY -> {
                data?.data?.let { uri -> loadBitmapFromUri(uri)?.let { applyImageBackground(it) } }
            }
            REQUEST_CAMERA -> {
                @Suppress("DEPRECATION")
                val thumb = data?.extras?.get("data") as? Bitmap
                if (thumb != null) {
                    applyImageBackground(thumb)
                } else {
                    data?.data?.let { uri -> loadBitmapFromUri(uri)?.let { applyImageBackground(it) } }
                }
            }
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun onDestroy() {
        if (isRecording) stopRecording()
        super.onDestroy()
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder, position: Int) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onItemClick(view: View?, position: Int, tag: Any?) {
        turboImageView.setSelectItemList(true)
        turboImageView.selectMyObject(true, position, tag)
        AppConstants.ID_UUID = tag?.toString().orEmpty()
        selectedPosition = position
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        turboImageView.setFromAndTo(fromPosition, toPosition, true, shapes)
    }

    override fun getAnimationDuration(b: Boolean) {
        adapter.setNotifyDataSetChanged(toPositionSafe())
    }

    private fun toPositionSafe(): Int = selectedPosition

    override fun editPicture(uiUuId: Shap?, position: Int) = Unit

    override fun onImageObjectSelected(`object`: MultiTouchObject?) = Unit

    override fun onImageObjectDropped() = Unit

    override fun onCanvasTouched() = Unit

    override fun setID_UUID(id_UUID: String?) {
        AppConstants.ID_UUID = id_UUID.orEmpty()
        if (touchCheckBox.isChecked) {
            shapes.forEachIndexed { index, shap ->
                if (shap.idUUId == id_UUID) {
                    adapter.setSelectItem(index)
                }
            }
        }
    }

    companion object {
        const val EXTRA_JSON = "accident_sketch_json"
        const val EXTRA_IMAGE_BASE64 = "accident_sketch_image_base64"
        const val EXTRA_BG_BASE64 = "accident_sketch_bg_base64"
        const val EXTRA_SOUND_PATH = "accident_sketch_sound_path"

        private const val ID_TITLE = "9901"
        private const val ID_DESC = "9902"
        private const val ID_DIGIT = "9903"
        private const val ID_PAINT = "9999"

        private const val REQUEST_MAP = 9110
        private const val REQUEST_GALLERY = 9111
        private const val REQUEST_CAMERA = 9112
        private const val REQUEST_RECORD_AUDIO = 9113
        private const val REQUEST_CAMERA_PERMISSION = 9114

        fun createIntent(
            context: Context,
            json: String?,
            bgBase64: String?,
            soundPath: String? = null,
        ): Intent {
            return Intent(context, AccidentSketchEditorActivity::class.java).apply {
                putExtra(EXTRA_JSON, json.orEmpty())
                putExtra(EXTRA_BG_BASE64, bgBase64)
                putExtra(EXTRA_SOUND_PATH, soundPath)
            }
        }
    }
}
