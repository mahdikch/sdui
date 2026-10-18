package com.yandex.divkit.demo.applock

/**
 * In-memory unlock flag for the current process.
 * Resets on process death (= cold start requires unlock again).
 */
object AppLockSession {
    @Volatile
    var unlockedThisProcess: Boolean = false
        private set

    fun markUnlocked() {
        unlockedThisProcess = true
    }

    fun clear() {
        unlockedThisProcess = false
    }
}
