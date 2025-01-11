package com.yandex.divkit.demo.ui.toastDiv

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import com.yandex.divkit.demo.R
import com.yandex.divkit.demo.ui.UIDiv2ViewCreator
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import com.yandex.divkit.regression.ScenarioLogDelegate
import org.json.JSONObject

class CustomToast(private val context: Context, private val lo: LifecycleOwner,private val mehdiViewModel: MehdiViewModel,private val activity: Activity): Toast(context) {

    fun show( json:String , duration: Int = Toast.LENGTH_SHORT) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(context)
        val customView: View = inflater.inflate(R.layout.toast_div, null)

        val divJson = JSONObject(json)
        val wrappedViewGroup: ViewGroup = wrapViewInViewGroup(context, customView)

        val div = UIDiv2ViewCreator(context, lo, mehdiViewModel, activity).createDiv2ViewMehdi(
            activity,
            divJson,
            wrappedViewGroup,
            ScenarioLogDelegate.Stub
        )
        div.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        ).apply {
            weight = 1F
        }

        // Create and configure the Toast
        Toast(context).apply {
            this.duration = duration
            view = div // Set the custom view
            show()
        }
    }

    fun wrapViewInViewGroup(context: Context, view: View): ViewGroup {
        val frameLayout = FrameLayout(context) // Create a ViewGroup
        frameLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        frameLayout.addView(view) // Add the View to the ViewGroup
        return frameLayout
    }
}