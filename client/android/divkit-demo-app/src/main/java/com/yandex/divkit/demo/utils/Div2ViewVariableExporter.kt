package com.yandex.divkit.demo.utils

import android.util.Log
import com.yandex.div.core.Div2Context
import com.yandex.div.core.expression.variables.DivVariableController
import com.yandex.div.core.expression.variables.GlobalVariableController
import com.yandex.div.core.view2.Div2View
import com.yandex.div.data.Variable
import org.json.JSONArray

object Div2ViewVariableExporter {
    private const val TAG = "Div2ViewVariableExporter"

    fun captureVariablesJson(div2View: Div2View): JSONArray? {
        val byName = linkedMapOf<String, Variable>()

        captureExpressionsRuntimeVariables(div2View).forEach { byName[it.name] = it }
        captureDivKitControllers(div2View).forEach { byName[it.name] = it }

        if (byName.isEmpty()) {
            Log.w(TAG, "No variables captured")
            return null
        }
        Log.d(TAG, "Captured ${byName.size} runtime variables")
        return JSONArray().apply {
            byName.values.forEach { put(it.writeToJSON()) }
        }
    }

    fun captureVariablesMap(div2View: Div2View): Map<String, Any> {
        val byName = linkedMapOf<String, Any>()
        captureExpressionsRuntimeVariables(div2View).forEach { byName[it.name] = it.getValue() }
        captureDivKitControllers(div2View).forEach { byName[it.name] = it.getValue() }
        return byName
    }

    private fun captureExpressionsRuntimeVariables(div2View: Div2View): List<Variable> {
        return runCatching {
            val runtimeField = Div2View::class.java.getDeclaredField("expressionsRuntime")
            runtimeField.isAccessible = true
            val runtime = runtimeField.get(div2View) ?: return emptyList()
            val vcField = runtime.javaClass.getDeclaredField("variableController")
            vcField.isAccessible = true
            val controller = vcField.get(runtime) ?: return emptyList()
            invokeCaptureAll(controller, "expressionsRuntime.variableController")
        }.getOrElse {
            Log.w(TAG, "Failed to capture expressions runtime variables", it)
            emptyList()
        }
    }

    private fun captureDivKitControllers(div2View: Div2View): List<Variable> {
        val context = runCatching {
            Div2View::class.java.getDeclaredField("context").apply { isAccessible = true }.get(div2View)
        }.getOrNull() as? Div2Context ?: return emptyList()

        val variables = mutableListOf<Variable>()
        variables += invokeCaptureAllVariables(
            controller = context.divVariableController,
            clazz = DivVariableController::class.java,
            label = "DivVariableController",
        )
        variables += invokeCaptureAllVariables(
            controller = context.globalVariableController,
            clazz = GlobalVariableController::class.java,
            label = "GlobalVariableController",
        )

        runCatching {
            val componentField = Div2View::class.java.getDeclaredField("div2Component")
            componentField.isAccessible = true
            val component = componentField.get(div2View) ?: return@runCatching
            val divCtrl = component.javaClass.getMethod("getDivVariableController").invoke(component)
            val globalCtrl = component.javaClass.getMethod("getGlobalVariableController").invoke(component)
            variables += invokeCaptureAllVariables(divCtrl, DivVariableController::class.java, "div2Component.divVariableController")
            variables += invokeCaptureAllVariables(globalCtrl, GlobalVariableController::class.java, "div2Component.globalVariableController")
        }.onFailure {
            Log.w(TAG, "Failed to capture variables via div2Component", it)
        }

        return variables
    }

    private fun invokeCaptureAllVariables(
        controller: Any,
        clazz: Class<*>,
        label: String,
    ): List<Variable> {
        return runCatching {
            clazz.getDeclaredMethod("captureAllVariables")
                .apply { isAccessible = true }
                .invoke(controller) as List<Variable>
        }.getOrElse { firstError ->
            invokeCaptureAll(controller, label).ifEmpty {
                Log.w(TAG, "captureAllVariables failed for $label", firstError)
                emptyList()
            }
        }
    }

    private fun invokeCaptureAll(controller: Any, label: String): List<Variable> {
        return runCatching {
            controller.javaClass.methods
                .firstOrNull { it.name == "captureAll" && it.parameterTypes.isEmpty() }
                ?.apply { isAccessible = true }
                ?.invoke(controller) as? List<Variable>
                ?: emptyList()
        }.getOrElse {
            Log.w(TAG, "Failed to capture variables from $label", it)
            emptyList()
        }
    }
}
