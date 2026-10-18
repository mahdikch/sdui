package com.yandex.divkit.demo.div.accidentSketch

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Base64
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast

class AccidentSketchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val previewImageView: ImageView
    private val openButton: Button
    private var jsonVariableName: String = ""
    private var imageVariableName: String = ""
    private var soundVariableName: String = ""
    private var currentJson: String? = null
    private var currentImageBase64: String? = null
    private var currentSoundPath: String? = null
    private var onSketchSaved: ((json: String, imageBase64: String, soundPath: String?) -> Unit)? = null
    private var autoOpenPending = true
    private var previewMode = false

    init {
        previewImageView = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.FIT_CENTER
            background = GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#F5F5F5"))
                cornerRadius = 12f
                setStroke(2, android.graphics.Color.parseColor("#E0E0E0"))
            }
            setPadding(8, 8, 8, 8)
        }

        openButton = Button(context).apply {
            text = "طراحی کروکی"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setTextColor(android.graphics.Color.WHITE)
            textSize = 16f
            background = GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#193d8d"))
                cornerRadius = 12f
            }
            setPadding(0, 32, 0, 32)
            setOnClickListener { openEditor() }
        }

        addView(previewImageView)
        addView(openButton)
    }

    fun configure(jsonVariable: String, imageVariable: String, soundVariable: String = "") {
        jsonVariableName = jsonVariable
        imageVariableName = imageVariable
        soundVariableName = soundVariable
    }

    fun setOnSketchSavedListener(listener: (json: String, imageBase64: String, soundPath: String?) -> Unit) {
        onSketchSaved = listener
    }

    fun setSketchData(json: String?, imageBase64: String?, soundPath: String? = null) {
        currentJson = json
        currentImageBase64 = imageBase64
        currentSoundPath = soundPath
        if (!previewMode) {
            autoOpenPending = json.isNullOrEmpty() && imageBase64.isNullOrEmpty()
        }
        updatePreview()
    }

    fun setPreviewMode(enabled: Boolean) {
        previewMode = enabled
        if (enabled) {
            autoOpenPending = false
            openButton.visibility = android.view.View.GONE
        } else {
            openButton.visibility = android.view.View.VISIBLE
        }
        updatePreview()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activeViews.add(this)
        if (previewMode) {
            autoOpenPending = false
            return
        }
        if (autoOpenPending && currentImageBase64.isNullOrEmpty() && currentJson.isNullOrEmpty()) {
            autoOpenPending = false
            post { openEditor() }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        activeViews.remove(this)
    }

    private fun openEditor() {
        val activity = getActivityFromContext(context)
        if (activity == null) {
            Toast.makeText(context, "خطا در باز کردن ویرایشگر کروکی", Toast.LENGTH_SHORT).show()
            android.util.Log.e(TAG, "Could not resolve Activity from context: ${context.javaClass.name}")
            return
        }
        pendingView = this
        activity.startActivityForResult(
            AccidentSketchEditorActivity.createIntent(context, currentJson, null, currentSoundPath),
            REQUEST_CODE,
        )
    }

    internal fun handleResultInternal(resultCode: Int, data: Intent?) {
        if (this != pendingView) return
        pendingView = null
        if (resultCode != Activity.RESULT_OK || data == null) return

        val json = data.getStringExtra(AccidentSketchEditorActivity.EXTRA_JSON).orEmpty()
        val image = data.getStringExtra(AccidentSketchEditorActivity.EXTRA_IMAGE_BASE64).orEmpty()
        val sound = data.getStringExtra(AccidentSketchEditorActivity.EXTRA_SOUND_PATH)
        currentJson = json
        currentImageBase64 = image
        currentSoundPath = sound
        autoOpenPending = false
        updatePreview()
        onSketchSaved?.invoke(json, image, sound)
    }

    private fun updatePreview() {
        val imageBase64 = currentImageBase64
        if (!imageBase64.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(imageBase64, Base64.NO_WRAP)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                previewImageView.setImageBitmap(bitmap)
                openButton.text = "ویرایش کروکی"
                openButton.alpha = 0.92f
                (openButton.layoutParams as LayoutParams).apply {
                    height = LayoutParams.WRAP_CONTENT
                    gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                    setMargins(16, 0, 16, 16)
                }
                return
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to decode sketch preview", e)
            }
        }
        previewImageView.setImageBitmap(null)
        openButton.text = "طراحی کروکی"
        openButton.alpha = 1f
        (openButton.layoutParams as LayoutParams).apply {
            height = LayoutParams.MATCH_PARENT
            gravity = android.view.Gravity.CENTER
            setMargins(0, 0, 0, 0)
        }
    }

    private fun getActivityFromContext(ctx: Context): Activity? {
        var current: Context? = ctx
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    companion object {
        private const val TAG = "AccidentSketchView"
        const val REQUEST_CODE = 9108

        private val activeViews = mutableListOf<AccidentSketchView>()
        private var pendingView: AccidentSketchView? = null

        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode != REQUEST_CODE) return
            pendingView?.handleResultInternal(resultCode, data)
                ?: activeViews.lastOrNull()?.handleResultInternal(resultCode, data)
        }
    }
}
