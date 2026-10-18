package com.yandex.divkit.demo.utils

import android.util.Log
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import org.json.JSONObject

/**
 * Opening a draft from the archive jumps straight to step N of a wizard. Without the earlier
 * steps on the navigation stack, "previous step" would pop back to the archive.
 */
object ArchiveWizardNavigator {
    private const val TAG = "ArchiveWizardNavigator"

    private const val COMPROMISE_TITLE = "ثبت تصادف سازشی"

    private val COMPROMISE_STEPS = listOf(
        "Accidents/compromisefirst",
        "Accidents/vehicleInformationEntry",
        "Accidents/exportreasoninginference",
        "Accidents/finalregistration",
    )

    private val PAGE_REGEX = Regex("^draft/[^/]+/page(\\d+)$")

    /**
     * Seeds the stack with the wizard steps up to the archived page and returns the screen to
     * load, or null when the archive entry is not part of a known wizard.
     */
    fun seedStack(mehdiViewModel: MehdiViewModel?, data: String, next: String): String? {
        val json = runCatching { JSONObject(data) }.getOrNull() ?: return null
        if (json.optString("title") != COMPROMISE_TITLE) return null
        if (json.optString("vehicle_id").isNotBlank()) return null

        val pageIndex = PAGE_REGEX.find(next)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: return null
        if (pageIndex !in 1..COMPROMISE_STEPS.size) return null

        val steps = COMPROMISE_STEPS.subList(0, pageIndex)
        val stack = ScreenNavigationStack.readStack(mehdiViewModel)
        stack.removeAll { it in COMPROMISE_STEPS || it.startsWith("draft/") }
        if (stack.firstOrNull() != ScreenNavigationStack.MAIN_SCREEN) {
            stack.removeAll { ScreenNavigationStack.isMainScreen(it) || ScreenNavigationStack.isAuthScreen(it) }
            stack.add(0, ScreenNavigationStack.MAIN_SCREEN)
        }
        stack.addAll(steps)
        ScreenNavigationStack.saveStack(mehdiViewModel, stack)
        Log.d(TAG, "Seeded stack for $next: $stack")
        return steps.last()
    }
}
