package com.yandex.divkit.demo.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.yandex.divkit.demo.R

class LoadingDialog(private val context: Context) {

    private val dialog = Dialog(context, R.style.LoadingDialogTheme)
    private var titleView: TextView? = null

    init {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val view = LayoutInflater.from(context).inflate(R.layout.loading, null)
        titleView = view.findViewById(R.id.txtTitle)
        dialog.setContentView(view)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.45f)
            setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun showLoadingDialog(title: String): LoadingDialog {
        if (!canShow()) return this
        try {
            if (title.isNotBlank()) {
                titleView?.text = title
            }
            if (!dialog.isShowing) {
                dialog.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    fun dismissDialog() {
        try {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun canShow(): Boolean {
        val activity = context as? Activity ?: return true
        return !activity.isFinishing && !activity.isDestroyed
    }
}
