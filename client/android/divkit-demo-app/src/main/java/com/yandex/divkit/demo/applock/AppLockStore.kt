package com.yandex.divkit.demo.applock

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

enum class AppLockType {
    PIN,
    PATTERN
}

class AppLockStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val isConfigured: Boolean
        get() = !prefs.getString(KEY_HASH, null).isNullOrBlank() &&
            !prefs.getString(KEY_TYPE, null).isNullOrBlank()

    val lockType: AppLockType?
        get() = when (prefs.getString(KEY_TYPE, null)) {
            AppLockType.PIN.name -> AppLockType.PIN
            AppLockType.PATTERN.name -> AppLockType.PATTERN
            else -> null
        }

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, false)
        set(value) {
            prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply()
        }

    fun setLock(type: AppLockType, secret: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hash(secret, salt)
        prefs.edit()
            .putString(KEY_TYPE, type.name)
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_HASH, hash)
            .apply()
    }

    fun verify(secret: String): Boolean {
        val saltB64 = prefs.getString(KEY_SALT, null) ?: return false
        val expected = prefs.getString(KEY_HASH, null) ?: return false
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        return hash(secret, salt) == expected
    }

    companion object {
        private const val PREFS_NAME = "app_lock_prefs"
        private const val KEY_TYPE = "lock_type"
        private const val KEY_HASH = "lock_hash"
        private const val KEY_SALT = "lock_salt"
        private const val KEY_BIOMETRIC = "biometric_enabled"

        fun hash(secret: String, salt: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(salt)
            digest.update(secret.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(digest.digest(), Base64.NO_WRAP)
        }

        fun patternToSecret(cells: List<Int>): String = cells.joinToString("-")
    }
}
