package com.yandex.divkit.demo.utils

import android.util.Log
import com.google.gson.Gson
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.ui.activity.MehdiViewModel

object ScreenNavigationStack {
    private const val TAG = "ScreenNavigationStack"
    const val DB_KEY = "screen_navigation_stack"
    const val MAIN_SCREEN = "ph/main"
    private val EXIT_ON_BACK_SCREENS = setOf("ph/splash", "ph/login", MAIN_SCREEN)
    private val AUTH_SCREENS = setOf("ph/splash", "ph/login")

    /** Screens where hardware back should confirm leaving the app. */
    fun shouldShowExitConfirm(screen: String?): Boolean {
        if (screen.isNullOrBlank()) return true
        return screen in EXIT_ON_BACK_SCREENS || isMainScreen(screen)
    }

    /** @deprecated Use [shouldShowExitConfirm] on the *current* screen. */
    fun shouldExitAppOnBack(screen: String?): Boolean = shouldShowExitConfirm(screen)

    fun isAuthScreen(screen: String?): Boolean {
        if (screen.isNullOrBlank()) return false
        return screen in AUTH_SCREENS
    }

    fun isMainScreen(screen: String?): Boolean {
        if (screen.isNullOrBlank()) return false
        return screen.equals(MAIN_SCREEN, ignoreCase = true)
    }

    data class PushResult(
        val stack: List<String>,
        val shouldNavigate: Boolean,
        val targetScreen: String,
    )

    data class PopResult(
        val previousScreen: String?,
        val stack: List<String>,
    )

    fun readStack(mehdiViewModel: MehdiViewModel?): MutableList<String> {
        val json = mehdiViewModel?.getValueByKey(DB_KEY)?.value
        if (json.isNullOrBlank()) {
            return mutableListOf()
        }
        return try {
            Gson().fromJson(json, Array<String>::class.java).toMutableList()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse screen stack, resetting", e)
            mutableListOf()
        }
    }

    fun saveStack(mehdiViewModel: MehdiViewModel?, stack: List<String>) {
        mehdiViewModel?.insertItemToDbSync(PhPlusDB(null, DB_KEY, Gson().toJson(stack)))
    }

    /**
     * After login, every app screen must sit above [MAIN_SCREEN] so back always
     * returns to main before the exit sheet can appear.
     */
    private fun ensureMainAtRoot(stack: MutableList<String>, forScreen: String) {
        if (isAuthScreen(forScreen) || isMainScreen(forScreen)) {
            return
        }
        stack.removeAll { isAuthScreen(it) }
        stack.removeAll { isMainScreen(it) }
        stack.add(0, MAIN_SCREEN)
        Log.d(TAG, "Ensured main at root: $stack")
    }

    /**
     * Pushes a screen onto the navigation stack.
     * - If the screen is already on top, navigation is skipped.
     * - If the screen already exists lower in the stack, everything above it is removed
     *   so each screen appears at most once.
     * - Non-auth screens always keep [MAIN_SCREEN] as the stack root.
     */
    fun push(
        mehdiViewModel: MehdiViewModel?,
        screen: String,
        forceNavigate: Boolean = false,
    ): PushResult {
        if (screen.isBlank()) {
            return PushResult(emptyList(), false, screen)
        }

        val stack = readStack(mehdiViewModel)

        // Entering the post-login shell should not keep splash/login underneath.
        if (!isAuthScreen(screen)) {
            val withoutAuth = stack.filterNot { isAuthScreen(it) }.toMutableList()
            if (withoutAuth.size != stack.size) {
                stack.clear()
                stack.addAll(withoutAuth)
                Log.d(TAG, "Removed auth screens before push: $stack")
            }
            ensureMainAtRoot(stack, screen)
        }

        if (!forceNavigate && stack.isNotEmpty() && stack.last() == screen) {
            Log.d(TAG, "Screen already on top, skip navigation: $screen")
            return PushResult(stack, false, screen)
        }

        if (forceNavigate && stack.isNotEmpty() && stack.last() == screen) {
            ensureMainAtRoot(stack, screen)
            saveStack(mehdiViewModel, stack)
            Log.d(TAG, "Force reload screen already on top: $screen")
            return PushResult(stack, true, screen)
        }

        val existingIndex = stack.indexOf(screen)
        if (existingIndex >= 0) {
            val trimmed = stack.subList(0, existingIndex + 1).toMutableList()
            ensureMainAtRoot(trimmed, screen)
            // After ensureMainAtRoot, [main, …, screen] — screen must stay on top.
            if (trimmed.lastOrNull() != screen) {
                trimmed.remove(screen)
                trimmed.add(screen)
            }
            saveStack(mehdiViewModel, trimmed)
            Log.d(TAG, "Screen exists in stack at $existingIndex, trimmed to: $trimmed")
            return PushResult(trimmed, true, screen)
        }

        stack.add(screen)
        saveStack(mehdiViewModel, stack)
        Log.d(TAG, "Pushed screen: $screen, stack=$stack")
        return PushResult(stack, true, screen)
    }

    fun pop(mehdiViewModel: MehdiViewModel?): PopResult {
        val stack = readStack(mehdiViewModel)
        if (stack.size < 2) {
            Log.d(TAG, "Cannot pop, stack size=${stack.size}")
            return PopResult(null, stack)
        }
        val removed = stack.removeAt(stack.size - 1)
        saveStack(mehdiViewModel, stack)
        val previous = stack.last()
        Log.d(TAG, "Popped $removed, navigating back to $previous, stack=$stack")
        return PopResult(previous, stack)
    }

    /** Collapse stack to main only (used when recovering from a broken stack). */
    fun resetToMain(mehdiViewModel: MehdiViewModel?): List<String> {
        val stack = listOf(MAIN_SCREEN)
        saveStack(mehdiViewModel, stack)
        Log.d(TAG, "Reset stack to main")
        return stack
    }

    fun clear(mehdiViewModel: MehdiViewModel?) {
        saveStack(mehdiViewModel, emptyList())
    }
}
