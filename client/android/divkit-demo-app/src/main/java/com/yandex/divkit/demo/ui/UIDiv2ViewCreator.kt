package com.yandex.divkit.demo.ui

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.yandex.div.core.view2.Div2View
import com.yandex.div.font.YandexSansDivTypefaceProvider
import com.yandex.div.state.DivStateDatabase
import com.yandex.div.zoom.DivPinchToZoomConfiguration
import com.yandex.div.zoom.DivPinchToZoomExtensionHandler
import com.yandex.divkit.demo.div.Div2Activity
import com.yandex.divkit.demo.div.divConfiguration
import com.yandex.divkit.demo.div.divContext
import com.yandex.divkit.demo.screenshot.Div2ViewFactory
import com.yandex.divkit.demo.screenshot.DivAssetReader
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import com.yandex.divkit.demo.ui.bottomSheetDiv.BottomSheetDiv
import com.yandex.divkit.demo.utils.DivkitDemoUriHandler
import com.yandex.divkit.regression.Div2ViewCreator
import com.yandex.divkit.regression.ScenarioLogDelegate
import org.json.JSONObject
import java.util.concurrent.Executors

class UIDiv2ViewCreator(private val context: Context, private val lo:LifecycleOwner,
                        private val mehdiViewModel: MehdiViewModel?, private val activity: Activity,
                        private  var btmSheet_div: BottomSheetDiv?=null) : Div2ViewCreator {

    private val assetReader = DivAssetReader(context)
    private val uriHandler = DivkitDemoUriHandler(context)
    private val divStateStorage = DivStateDatabase(
        context,
        "regression-div-states",
        Executors.newSingleThreadExecutor()
    )

    override fun createDiv2View(
        activity: Activity,
        scenarioPath: String,
        parent: ViewGroup,
        logDelegate: ScenarioLogDelegate
    ): Div2View {
        val divJson = assetReader.read(scenarioPath)
        val transitionScheduler = Div2Activity.DivParentTransitionScheduler(parent)
        val divConfiguration =
            divConfiguration(activity, logDelegate)
                .extension(
                    DivPinchToZoomExtensionHandler(
                        DivPinchToZoomConfiguration.Builder(activity).build()
                    )
                )
                .divStateChangeListener(transitionScheduler)
                .divDataChangeListener(transitionScheduler)
                .typefaceProvider(YandexSansDivTypefaceProvider(activity))
                .actionHandler(UIDiv2ActionHandler(uriHandler, context,activity,lo,context as LoadScreenListener,mehdiViewModel,
                    btmSheet_div))
                .enableAccessibility(true)
                .build()
        val divContext = divContext(
            baseContext = activity,
            configuration = divConfiguration,
            lifecycleOwner = activity as? LifecycleOwner
        )
        divStateStorage.preloadState("div2")
        val templateJson = divJson.optJSONObject("templates")
        val cardJson = divJson.getJSONObject("card")
        println("assetReader1 = "+cardJson.get("variables").toString())
        return Div2ViewFactory(divContext, templateJson).createView(cardJson)
    }

    override fun createDiv2ViewMehdi(
        activity: Activity,
        divView: JSONObject,
        parent: ViewGroup,
        logDelegate: ScenarioLogDelegate
    ): Div2View {
//        val divJson = assetReader.read(scenarioPath)

        val transitionScheduler = Div2Activity.DivParentTransitionScheduler(parent)
        val divConfiguration =
            divConfiguration(activity, logDelegate,mehdiViewModel,lo,context as LoadScreenListener)
                .extension(
                    DivPinchToZoomExtensionHandler(
                        DivPinchToZoomConfiguration.Builder(activity).build()
                    )
                )
                .divStateChangeListener(transitionScheduler)
                .divDataChangeListener(transitionScheduler)
                .typefaceProvider(YandexSansDivTypefaceProvider(activity))
                .actionHandler(UIDiv2ActionHandler(uriHandler, context,activity,lo,context as LoadScreenListener,mehdiViewModel,btmSheet_div))
                .enableAccessibility(true)
                .build()
        val divContext = divContext(
            baseContext = activity,
            configuration = divConfiguration,
            lifecycleOwner = activity as? LifecycleOwner
        )
        divStateStorage.preloadState("div2")
        val templateJson = divView.optJSONObject("templates")
        val cardJson = divView.getJSONObject("card")
        println("assetReader2 = "+cardJson.get("variables").toString())

        return Div2ViewFactory(divContext, templateJson).createView(cardJson)
    }
}
