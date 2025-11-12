package com.yandex.divkit.demo.ui.activity

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PdfViewerActivity : AppCompatActivity() {
    
    private var pdfRenderer: PdfRenderer? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Get file path from intent
            val filePath = intent.getStringExtra("PDF_FILE_PATH")
            
            if (filePath == null || !File(filePath).exists()) {
                Toast.makeText(this, "فایل PDF یافت نشد", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            
            // Create ScrollView with LinearLayout for pages
            val scrollView = ScrollView(this)
            val linearLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }
            scrollView.addView(linearLayout)
            setContentView(scrollView)
            
            // Open PDF file
            val file = File(filePath)
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
            
            println("PdfViewerActivity: PDF has ${pdfRenderer!!.pageCount} pages")
            
            // Render all pages
            for (pageIndex in 0 until pdfRenderer!!.pageCount) {
                val page = pdfRenderer!!.openPage(pageIndex)
                
                // Create bitmap for the page
                val bitmap = Bitmap.createBitmap(
                    page.width * 2, // Scale up for better quality
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                
                // Render page to bitmap
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                
                // Add ImageView for this page
                val imageView = ImageView(this).apply {
                    setImageBitmap(bitmap)
                    adjustViewBounds = true
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 32)
                    }
                }
                linearLayout.addView(imageView)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            println("PdfViewerActivity: Error rendering PDF: ${e.message}")
            Toast.makeText(this, "خطا در نمایش PDF: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            pdfRenderer?.close()
            parcelFileDescriptor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

