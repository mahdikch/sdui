package com.yandex.divkit.demo.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.yandex.divkit.demo.R
import java.util.Objects

class LoadingDialog(private val context: Context) {
    var dialog = Dialog(context)

    fun showLoadingDialog(

        title: String
    ): LoadingDialog {
        dialog.setCancelable(false)
//        dialog.setContentView(R.layout.loading)
        val width =
            ((context.resources.displayMetrics.widthPixels/3) * 2 )
        val height =
            ((context.resources.displayMetrics.heightPixels/3) * 1 )

        try {
            if (!dialog.isShowing) {

//                if (dialog.window != null) {
//                    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                }
                val inflater = LayoutInflater.from(context)
                val promptsView: View = inflater.inflate(R.layout.loading, null)
                val textView = promptsView.findViewById<TextView>(R.id.txtTitle)
//            val lottie = promptsView.findViewById<LottieAnimationView>(R.id.littie)
//            lottie.
                dialog.setContentView(promptsView)

//                dialog.isIndeterminate = true
                if (title != "") {
                    textView.text = title
                }
                dialog.show()

//                Objects.requireNonNull(dialog.window)?.setLayout(width, height)
            }
        } catch (r: Exception) {
            r.printStackTrace()
        }
        return this
    }

    fun dismissDialog() {

        if (dialog.isShowing)
            dialog.dismiss()
    }

}