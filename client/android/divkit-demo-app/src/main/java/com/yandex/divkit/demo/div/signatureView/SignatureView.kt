package com.yandex.divkit.demo.div.signatureView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.ByteArrayOutputStream

class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val signatureImageView: ImageView
    private val openButton: Button
    private var currentSignatureBase64: String? = null
    private var onSignatureSaved: ((String) -> Unit)? = null // base64 string
    
    init {
        // Create ImageView to display signature
        signatureImageView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            // Add border and rounded corners
            val drawable = GradientDrawable().apply {
                setColor(Color.parseColor("#F5F5F5"))
                cornerRadius = 12f
                setStroke(2, Color.parseColor("#E0E0E0"))
            }
            background = drawable
            setPadding(8, 8, 8, 8)
        }
        
        // Create button to open signature dialog
        openButton = Button(context).apply {
            text = "امضا کنید"
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#193d8d"))
            setTextColor(Color.WHITE)
            textSize = 16f
            // Add rounded corners
            val drawable = GradientDrawable().apply {
                setColor(Color.parseColor("#193d8d"))
                cornerRadius = 12f
            }
            background = drawable
            setPadding(0, 32, 0, 32)
            setOnClickListener {
                openSignatureBottomSheet()
            }
        }
        
        addView(signatureImageView)
        addView(openButton)
    }
    
    fun setOnSignatureSavedListener(listener: (String) -> Unit) {
        this.onSignatureSaved = listener
    }
    
    fun setSignature(base64String: String?) {
        currentSignatureBase64 = base64String
        if (base64String != null && base64String.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(base64String, Base64.NO_WRAP)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                signatureImageView.setImageBitmap(bitmap)
                // Show button as overlay at bottom when signature exists
                openButton.text = "تغییر امضا"
                openButton.alpha = 0.9f
                (openButton.layoutParams as FrameLayout.LayoutParams).apply {
                    height = FrameLayout.LayoutParams.WRAP_CONTENT
                    gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                    setMargins(16, 0, 16, 16)
                }
                // Update button style
                val drawable = GradientDrawable().apply {
                    setColor(Color.parseColor("#193d8d"))
                    cornerRadius = 12f
                }
                openButton.background = drawable
            } catch (e: Exception) {
                android.util.Log.e("SignatureView", "Error decoding base64 signature", e)
                signatureImageView.setImageBitmap(null)
                openButton.text = "امضا کنید"
                openButton.alpha = 1.0f
                (openButton.layoutParams as FrameLayout.LayoutParams).apply {
                    height = FrameLayout.LayoutParams.MATCH_PARENT
                    gravity = android.view.Gravity.CENTER
                }
            }
        } else {
            signatureImageView.setImageBitmap(null)
            openButton.text = "امضا کنید"
            openButton.alpha = 1.0f
            (openButton.layoutParams as FrameLayout.LayoutParams).apply {
                height = FrameLayout.LayoutParams.MATCH_PARENT
                gravity = android.view.Gravity.CENTER
            }
        }
    }
    
    private fun openSignatureBottomSheet() {
        // Try to get Activity from context
        val activity = getActivityFromContext(context)
        
        if (activity == null) {
            android.util.Log.e("SignatureView", "Could not get Activity from context")
            Toast.makeText(context, "خطا در باز کردن صفحه امضا", Toast.LENGTH_SHORT).show()
            return
        }
        
        val bottomSheetDialog = BottomSheetDialog(activity)
        
        // Prevent closing by dragging or touching outside
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)
        
        val bottomSheetView = createSignatureBottomSheetView(bottomSheetDialog)
        bottomSheetDialog.setContentView(bottomSheetView)
        
        // Disable dragging behavior after dialog is shown
        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.isDraggable = false
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                // Prevent collapsing
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING || 
                            newState == BottomSheetBehavior.STATE_COLLAPSED ||
                            newState == BottomSheetBehavior.STATE_HIDDEN) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        // Prevent sliding
                    }
                })
            }
        }
        
        bottomSheetDialog.show()
    }
    
    private fun getActivityFromContext(ctx: Context): FragmentActivity? {
        return when (ctx) {
            is AppCompatActivity -> ctx
            is FragmentActivity -> ctx
            is android.content.ContextWrapper -> getActivityFromContext(ctx.baseContext)
            else -> null
        }
    }
    
    private fun createSignatureBottomSheetView(dialog: BottomSheetDialog): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)
            // Prevent touch events from being intercepted by parent
            isClickable = true
            isFocusable = true
        }
        
        // Add title
        val titleTextView = TextView(context).apply {
            text = "امضای خود را رسم کنید"
            textSize = 20f
            setTextColor(Color.parseColor("#193d8d"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            gravity = android.view.Gravity.CENTER
        }
        
        // Create signature canvas container with border
        val canvasContainer = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            // Add border and rounded corners
            val drawable = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 12f
                setStroke(2, Color.parseColor("#E0E0E0"))
            }
            background = drawable
            setPadding(8, 8, 8, 8)
        }
        
        // Create signature canvas
        val signatureCanvas = SignatureCanvas(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        canvasContainer.addView(signatureCanvas)
        
        // Create button container
        val buttonContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Helper function to create styled button
        fun createStyledButton(text: String, onClick: () -> Unit): Button {
            return Button(context).apply {
                this.text = text
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setBackgroundColor(Color.parseColor("#193d8d"))
                setTextColor(Color.WHITE)
                textSize = 16f
                // Add rounded corners
                val drawable = GradientDrawable().apply {
                    setColor(Color.parseColor("#193d8d"))
                    cornerRadius = 8f
                }
                background = drawable
                setPadding(0, 20, 0, 20)
                setOnClickListener { onClick() }
            }
        }
        
        // Clear button
        val clearButton = createStyledButton("پاک کردن") {
            signatureCanvas.clear()
        }.apply {
            (layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 8, 0)
        }
        
        // Cancel button
        val cancelButton = createStyledButton("انصراف") {
            dialog.dismiss()
        }.apply {
            (layoutParams as LinearLayout.LayoutParams).setMargins(8, 0, 8, 0)
        }
        
        // Save button
        val saveButton = createStyledButton("ذخیره") {
            val base64 = signatureCanvas.getSignatureAsBase64()
            if (base64 != null) {
                currentSignatureBase64 = base64
                setSignature(base64)
                onSignatureSaved?.invoke(base64)
                Toast.makeText(context, "امضا ذخیره شد", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(context, "لطفاً ابتدا امضا کنید", Toast.LENGTH_SHORT).show()
            }
        }.apply {
            (layoutParams as LinearLayout.LayoutParams).setMargins(8, 0, 0, 0)
        }
        
        buttonContainer.addView(clearButton)
        buttonContainer.addView(cancelButton)
        buttonContainer.addView(saveButton)
        
        container.addView(titleTextView)
        container.addView(canvasContainer)
        container.addView(buttonContainer)
        
        return container
    }
    
    /**
     * Inner class for drawing signature
     */
    private class SignatureCanvas(context: Context) : View(context) {
        private val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        
        private val hintPaint = Paint().apply {
            color = Color.parseColor("#BDBDBD")
            textSize = 48f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        private val path = Path()
        private var hasSignature = false
        
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            // Draw white background
            canvas.drawColor(Color.WHITE)
            
            // Draw hint text if no signature
            if (!hasSignature) {
                val hintText = "اینجا امضا کنید"
                val x = width / 2f
                val y = height / 2f
                canvas.drawText(hintText, x, y, hintPaint)
            }
            
            // Draw signature path
            canvas.drawPath(path, paint)
        }
        
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    path.moveTo(x, y)
                    hasSignature = true
                    parent?.requestDisallowInterceptTouchEvent(true) // Prevent parent from intercepting
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    path.lineTo(x, y)
                    invalidate()
                    parent?.requestDisallowInterceptTouchEvent(true) // Prevent parent from intercepting
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    path.lineTo(x, y)
                    invalidate()
                    parent?.requestDisallowInterceptTouchEvent(false)
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    parent?.requestDisallowInterceptTouchEvent(false)
                    return true
                }
            }
            return super.onTouchEvent(event)
        }
        
        fun clear() {
            path.reset()
            hasSignature = false
            invalidate()
        }
        
        fun hasSignature(): Boolean {
            return hasSignature
        }
        
        fun getSignatureAsBase64(): String? {
            if (!hasSignature) {
                return null
            }
            
            try {
                // Ensure view has been measured
                if (width <= 0 || height <= 0) {
                    android.util.Log.w("SignatureView", "View not measured yet, using default size")
                    measure(
                        View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY)
                    )
                    layout(0, 0, measuredWidth, measuredHeight)
                }
                
                // Create bitmap from view
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                draw(canvas)
                
                // Convert to base64
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val byteArray = outputStream.toByteArray()
                
                // Use android.util.Base64 which works on all Android versions
                return Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } catch (e: Exception) {
                android.util.Log.e("SignatureView", "Error converting signature to base64", e)
                return null
            }
        }
    }
}
