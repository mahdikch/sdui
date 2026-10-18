package com.yandex.divkit.demo.div.imagePickerView

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Base64
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ImagePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    data class PickedImage(
        val image: String,
        var sceneDesc: String = "",
        var expertDesc: String = "",
    )

    private var variableName: String = ""
    private var title: String = ""
    private var minImages: Int = 0
    private var maxImages: Int = Int.MAX_VALUE
    private var imageSource: String = "both"
    private var withDescriptions: Boolean = true
    private var sceneLabel: String = "توضیحات از نحوه و چگونگی تصویر"
    private var expertLabel: String = "توضیحات کارشناس در خصوص تصویر"
    private var onImagesChanged: ((String) -> Unit)? = null
    private var hasBound: Boolean = false
    private var storeKey: String = ""

    private object ImageStore {
        private val imagesByVariable = mutableMapOf<String, MutableList<PickedImage>>()

        fun has(key: String): Boolean = key.isNotBlank() && imagesByVariable.containsKey(key)

        fun get(key: String): List<PickedImage> {
            return imagesByVariable[key]?.map { it.copy() }.orEmpty()
        }

        fun set(key: String, images: List<PickedImage>) {
            if (key.isBlank()) return
            imagesByVariable[key] = images.map { it.copy() }.toMutableList()
        }

        fun add(key: String, image: PickedImage) {
            if (key.isBlank()) return
            val current = imagesByVariable.getOrPut(key) { mutableListOf() }
            current.add(image.copy())
        }
    }

    enum class ImageSource {
        GALLERY, CAMERA, BOTH
    }

    private val imagesList = mutableListOf<PickedImage>()
    private val titleTextView: TextView
    private val addButton: Button
    private val imagesRecyclerView: RecyclerView
    private val descriptionCardsContainer: LinearLayout
    private val imagesAdapter: ImagesAdapter

    companion object {
        const val REQUEST_CODE_GALLERY = 1001
        const val REQUEST_CODE_CAMERA = 1002

        private val activeViews = mutableListOf<ImagePickerView>()
        private val variableListeners = mutableMapOf<String, (String) -> Unit>()
        private var pendingPicker: ImagePickerView? = null
        private var pendingVariableName: String = ""
        private var pendingStoreKey: String = ""

        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            val picker = pendingPicker
            val variableName = pendingVariableName
            val key = pendingStoreKey
            pendingPicker = null
            pendingVariableName = ""
            pendingStoreKey = ""
            picker?.handleActivityResultInternal(requestCode, resultCode, data, variableName, key)
            picker?.takeIf { !it.isAttachedToWindow }?.let { activeViews.remove(it) }
        }
    }

    init {
        titleTextView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            text = title
            textSize = 16f
            setTextColor(Color.parseColor("#193d8d"))
            gravity = android.view.Gravity.END
            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            visibility = View.GONE
        }

        addButton = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            text = "افزودن عکس از دوربین یا گالری"
            setTextColor(Color.WHITE)
            textSize = 14f
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#193d8d"))
                cornerRadius = 12f
            }
            setPadding(0, 16, 0, 16)
            setOnClickListener {
                when (getImageSourceEnum()) {
                    ImageSource.GALLERY -> openGallery()
                    ImageSource.CAMERA -> openCamera()
                    ImageSource.BOTH -> showImageSourceDialog()
                }
            }
        }

        imagesRecyclerView = RecyclerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutManager = GridLayoutManager(context, 3)
            isNestedScrollingEnabled = false
        }

        imagesAdapter = ImagesAdapter(imagesList) { position ->
            removeImage(position)
        }
        imagesRecyclerView.adapter = imagesAdapter

        descriptionCardsContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
        }

        container.addView(titleTextView)
        container.addView(addButton)
        container.addView(imagesRecyclerView)
        container.addView(descriptionCardsContainer)
        addView(container)
        applyDescriptionMode()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec).coerceAtLeast(suggestedMinimumWidth)
        val child = getChildAt(0)
        if (child != null) {
            child.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            )
            setMeasuredDimension(width, child.measuredHeight.coerceAtLeast(suggestedMinimumHeight))
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        getChildAt(0)?.layout(0, 0, right - left, bottom - top)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!activeViews.contains(this)) {
            activeViews.add(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (pendingPicker !== this) {
            activeViews.remove(this)
        }
    }

    private fun handleActivityResultInternal(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        assignedVariable: String,
        assignedStoreKey: String,
    ) {
        if (resultCode != Activity.RESULT_OK) return
        if (assignedVariable.isNotBlank()) {
            variableName = assignedVariable
        }
        val key = assignedStoreKey.ifBlank { storeKey.ifBlank { variableName } }
        if (key.isNotBlank()) {
            storeKey = key
        }

        when (requestCode) {
            REQUEST_CODE_GALLERY -> {
                data?.data?.let { uri -> processImageUri(uri, key) }
            }
            REQUEST_CODE_CAMERA -> {
                data?.extras?.get("data")?.let { bitmap ->
                    processBitmap(bitmap as Bitmap, key)
                } ?: data?.data?.let { uri ->
                    processImageUri(uri, key)
                }
            }
        }
    }

    private fun showImageSourceDialog() {
        if (imagesList.size >= maxImages) {
            Toast.makeText(context, "حداکثر $maxImages عکس می‌توانید انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val activity = getActivityFromContext(context) ?: return
        val bottomSheetDialog = BottomSheetDialog(activity)
        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val sourceEnum = getImageSourceEnum()
        if (sourceEnum == ImageSource.BOTH || sourceEnum == ImageSource.GALLERY) {
            dialogView.addView(sourceButton("از گالری انتخاب کنید") {
                openGallery()
                bottomSheetDialog.dismiss()
            })
        }
        if (sourceEnum == ImageSource.BOTH || sourceEnum == ImageSource.CAMERA) {
            dialogView.addView(sourceButton("از دوربین بگیرید") {
                openCamera()
                bottomSheetDialog.dismiss()
            })
        }

        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
    }

    private fun sourceButton(label: String, onClick: () -> Unit): Button {
        return Button(context).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 16f
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#193d8d"))
                cornerRadius = 12f
            }
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun getImageSourceEnum(): ImageSource {
        return when (imageSource.lowercase()) {
            "gallery" -> ImageSource.GALLERY
            "camera" -> ImageSource.CAMERA
            else -> ImageSource.BOTH
        }
    }

    private fun openGallery() {
        val activity = getActivityFromContext(context) ?: run {
            Toast.makeText(context, "خطا در باز کردن گالری", Toast.LENGTH_SHORT).show()
            return
        }
        pendingPicker = this
        pendingVariableName = variableName
        pendingStoreKey = currentStoreKey()
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        activity.startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    private fun openCamera() {
        val activity = getActivityFromContext(context) ?: run {
            Toast.makeText(context, "خطا در باز کردن دوربین", Toast.LENGTH_SHORT).show()
            return
        }
        pendingPicker = this
        pendingVariableName = variableName
        pendingStoreKey = currentStoreKey()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activity.startActivityForResult(intent, REQUEST_CODE_CAMERA)
    }

    private fun processImageUri(uri: Uri, assignedVariable: String) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap?.let { processBitmap(it, assignedVariable) }
        } catch (e: Exception) {
            android.util.Log.e("ImagePickerView", "Error processing image URI", e)
            Toast.makeText(context, "خطا در پردازش عکس", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processBitmap(bitmap: Bitmap, assignedVariable: String) {
        try {
            val maxWidth = 1024
            val maxHeight = 1024
            val scaledBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
                val scale = minOf(
                    maxWidth.toFloat() / bitmap.width,
                    maxHeight.toFloat() / bitmap.height
                )
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            addImage(base64String, assignedVariable)
        } catch (e: Exception) {
            android.util.Log.e("ImagePickerView", "Error processing bitmap", e)
            Toast.makeText(context, "خطا در پردازش عکس", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addImage(base64String: String, assignedStoreKey: String = currentStoreKey()) {
        val key = assignedStoreKey.ifBlank { currentStoreKey() }
        if (key.isBlank()) return
        val current = ImageStore.get(key)
        if (current.size >= maxImages) {
            Toast.makeText(context, "حداکثر $maxImages عکس می‌توانید انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }

        ImageStore.add(key, PickedImage(base64String))
        reloadFromStore(key)
    }

    private fun removeImage(position: Int) {
        if (imagesList.size <= minImages) {
            Toast.makeText(context, "حداقل $minImages عکس باید انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }
        if (position !in imagesList.indices) return
        imagesList.removeAt(position)
        ImageStore.set(currentStoreKey(), imagesList)
        refreshImagesUi()
        updateVariable()
        updateAddButtonState()
    }

    fun bindPicker(
        newVariableName: String,
        newTitle: String,
        min: Int,
        max: Int,
        source: String,
        descriptionsEnabled: Boolean,
        newSceneLabel: String,
        newExpertLabel: String,
        initialValue: String,
        listener: (String) -> Unit,
        newStoreKey: String = newVariableName,
    ) {
        variableName = newVariableName
        storeKey = newStoreKey.ifBlank { newVariableName }
        setTitle(newTitle)
        minImages = min
        maxImages = max
        imageSource = source
        setSceneLabel(newSceneLabel)
        setExpertLabel(newExpertLabel)
        withDescriptions = descriptionsEnabled
        onImagesChanged = listener
        if (newVariableName.isNotBlank()) {
            variableListeners[newVariableName] = listener
        }
        applyDescriptionMode()
        val key = currentStoreKey()
        if (ImageStore.has(key)) {
            imagesList.clear()
            imagesList.addAll(ImageStore.get(key).map { it.copy() })
        } else {
            setImages(initialValue)
        }
        refreshImagesUi()
        updateAddButtonState()
    }

    private fun currentStoreKey(): String = storeKey.ifBlank { variableName }

    private fun reloadFromStore(key: String) {
        val stored = ImageStore.get(key)
        val json = serializeImages(stored)
        val targets = activeViews.filter { it.currentStoreKey() == key }
        if (targets.isEmpty() && currentStoreKey() == key) {
            imagesList.clear()
            imagesList.addAll(stored.map { it.copy() })
            refreshImagesUi()
            updateAddButtonState()
        } else {
            targets.forEach { view ->
                view.imagesList.clear()
                view.imagesList.addAll(stored.map { it.copy() })
                view.refreshImagesUi()
                view.updateAddButtonState()
            }
        }
        val listenerKey = targets.firstOrNull()?.variableName ?: variableName
        variableListeners[listenerKey]?.invoke(json)
        requestParentsLayout()
    }

    private fun updateVariable() {
        val key = currentStoreKey()
        if (key.isNotBlank()) {
            ImageStore.set(key, imagesList)
        }
        onImagesChanged?.invoke(serializeImages())
    }

    private fun serializeImages(images: List<PickedImage> = imagesList): String {
        if (withDescriptions) {
            val arr = JSONArray()
            images.forEach { item ->
                arr.put(JSONObject().apply {
                    put("image", item.image)
                    put("scene", item.sceneDesc)
                    put("expert", item.expertDesc)
                })
            }
            return arr.toString()
        }
        return images.joinToString(",") { it.image }
    }

    private fun updateAddButtonState() {
        addButton.isEnabled = imagesList.size < maxImages
        addButton.alpha = if (imagesList.size >= maxImages) 0.5f else 1.0f
    }

    fun setVariableName(name: String) {
        this.variableName = name
    }

    fun setTitle(title: String) {
        this.title = title
        titleTextView.text = title
        titleTextView.visibility = if (title.isBlank()) View.GONE else View.VISIBLE
    }

    fun setMinImages(min: Int) {
        this.minImages = min
    }

    fun setMaxImages(max: Int) {
        this.maxImages = max
    }

    fun setImageSource(source: String) {
        this.imageSource = source
    }

    fun setWithDescriptions(enabled: Boolean) {
        this.withDescriptions = enabled
        applyDescriptionMode()
        refreshImagesUi()
    }

    fun setSceneLabel(label: String) {
        if (label.isNotBlank()) this.sceneLabel = label
    }

    fun setExpertLabel(label: String) {
        if (label.isNotBlank()) this.expertLabel = label
    }

    fun setImages(storedValue: String) {
        imagesList.clear()
        imagesList.addAll(parseStoredValue(storedValue))
        val key = currentStoreKey()
        if (key.isNotBlank()) {
            ImageStore.set(key, imagesList)
        }
        refreshImagesUi()
        updateAddButtonState()
    }

    fun setOnImagesChangedListener(listener: (String) -> Unit) {
        this.onImagesChanged = listener
    }

    private fun applyDescriptionMode() {
        imagesRecyclerView.visibility = if (withDescriptions) View.GONE else View.VISIBLE
        descriptionCardsContainer.visibility = if (withDescriptions) View.VISIBLE else View.GONE
    }

    private fun refreshImagesUi() {
        if (withDescriptions) {
            rebuildDescriptionCards()
        } else {
            imagesAdapter.notifyDataSetChanged()
        }
        requestParentsLayout()
    }

    private fun requestParentsLayout() {
        requestLayout()
        var current: View? = this
        repeat(8) {
            val parentView = current?.parent as? View ?: return
            parentView.requestLayout()
            current = parentView
        }
    }

    private fun rebuildDescriptionCards() {
        descriptionCardsContainer.removeAllViews()
        imagesList.forEachIndexed { index, item ->
            descriptionCardsContainer.addView(createDescriptionCard(index, item))
        }
    }

    private fun createDescriptionCard(index: Int, item: PickedImage): View {
        val dp = { value: Int ->
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                resources.displayMetrics
            ).toInt()
        }

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(12))
            }
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 12f
                setStroke(2, Color.parseColor("#c8d0e4"))
            }
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(dp(88), dp(88))
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F5F5F5"))
                cornerRadius = 8f
            }
            try {
                val decodedBytes = Base64.decode(item.image, Base64.NO_WRAP)
                setImageBitmap(BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size))
            } catch (e: Exception) {
                android.util.Log.e("ImagePickerView", "Error decoding base64 image", e)
            }
        }

        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        }

        val removeButton = Button(context).apply {
            text = "حذف"
            setTextColor(Color.WHITE)
            textSize = 13f
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#C62828"))
                cornerRadius = 10f
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { removeImage(index) }
        }

        header.addView(removeButton)
        header.addView(spacer)
        header.addView(imageView)

        card.addView(header)
        card.addView(labeledInput(sceneLabel, item.sceneDesc) { text ->
            item.sceneDesc = text
            updateVariable()
        })
        card.addView(labeledInput(expertLabel, item.expertDesc) { text ->
            item.expertDesc = text
            updateVariable()
        })
        return card
    }

    private fun labeledInput(
        label: String,
        initial: String,
        onChanged: (String) -> Unit,
    ): View {
        val dp = { value: Int ->
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                resources.displayMetrics
            ).toInt()
        }
        val block = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        }
        block.addView(TextView(context).apply {
            text = label
            textSize = 13f
            setTextColor(Color.parseColor("#3b3b3b"))
            gravity = android.view.Gravity.END
            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
        })
        val input = EditText(context).apply {
            setText(initial)
            hint = "توضیحات مورد نظر در این قسمت نوشته میشود"
            textSize = 13f
            gravity = android.view.Gravity.END or android.view.Gravity.TOP
            minLines = 2
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 8f
                setStroke(2, Color.parseColor("#e8e8e8"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    onChanged(s?.toString().orEmpty())
                }
            })
        }
        block.addView(input)
        return block
    }

    private fun parseStoredValue(storedValue: String): List<PickedImage> {
        if (storedValue.isBlank()) return emptyList()
        val trimmed = storedValue.trim()
        if (trimmed.startsWith("[")) {
            return runCatching {
                val arr = JSONArray(trimmed)
                (0 until arr.length()).map { i ->
                    val obj = arr.optJSONObject(i)
                    if (obj != null) {
                        PickedImage(
                            image = obj.optString("image"),
                            sceneDesc = obj.optString("scene"),
                            expertDesc = obj.optString("expert"),
                        )
                    } else {
                        PickedImage(arr.optString(i))
                    }
                }.filter { it.image.isNotBlank() }
            }.getOrDefault(emptyList())
        }
        return trimmed.split(",").filter { it.isNotBlank() }.map { PickedImage(it) }
    }

    private fun getActivityFromContext(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

    private class ImagesAdapter(
        private val images: List<PickedImage>,
        private val onRemoveClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

        class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(android.R.id.icon)
            val removeButton: ImageView = itemView.findViewById(android.R.id.button1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val itemView = FrameLayout(parent.context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                    height = (parent.width / 3 - 16).coerceAtLeast(100)
                }

                val imageView = ImageView(context).apply {
                    id = android.R.id.icon
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#F5F5F5"))
                        cornerRadius = 8f
                        setStroke(2, Color.parseColor("#E0E0E0"))
                    }
                }

                val removeButton = ImageView(context).apply {
                    id = android.R.id.button1
                    layoutParams = FrameLayout.LayoutParams(
                        48,
                        48,
                        android.view.Gravity.TOP or android.view.Gravity.END
                    ).apply {
                        setMargins(8, 8, 8, 8)
                    }
                    setImageResource(android.R.drawable.ic_menu_delete)
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#FF0000"))
                        cornerRadius = 24f
                    }
                    setPadding(12, 12, 12, 12)
                    setColorFilter(Color.WHITE)
                }

                addView(imageView)
                addView(removeButton)
            }

            return ImageViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val base64String = images[position].image
            try {
                val decodedBytes = Base64.decode(base64String, Base64.NO_WRAP)
                holder.imageView.setImageBitmap(
                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                )
            } catch (e: Exception) {
                android.util.Log.e("ImagePickerView", "Error decoding base64 image", e)
            }

            holder.removeButton.setOnClickListener {
                onRemoveClick(position)
            }
        }

        override fun getItemCount(): Int = images.size
    }
}
