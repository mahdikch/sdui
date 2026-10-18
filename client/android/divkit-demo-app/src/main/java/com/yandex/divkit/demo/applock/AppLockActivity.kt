package com.yandex.divkit.demo.applock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yandex.divkit.demo.R
import com.yandex.divkit.demo.ui.activity.MehdiActivity

class AppLockActivity : AppCompatActivity() {

    private enum class Mode { SETUP_CHOOSE, SETUP_ENTER, SETUP_CONFIRM, UNLOCK, CHANGE_VERIFY, CHANGE_CHOOSE, CHANGE_ENTER, CHANGE_CONFIRM }

    private lateinit var store: AppLockStore
    private lateinit var titleView: TextView
    private lateinit var subtitleView: TextView
    private lateinit var typeChooser: View
    private lateinit var pinDisplay: TextView
    private lateinit var pinPad: GridLayout
    private lateinit var patternView: PatternLockView
    private lateinit var btnBiometric: Button
    private lateinit var btnConfirmPin: Button
    private lateinit var btnClear: Button

    private var mode = Mode.UNLOCK
    private var selectedType: AppLockType = AppLockType.PIN
    private var firstSecret: String = ""
    private val pinBuffer = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_lock)
        store = AppLockStore(this)

        titleView = findViewById(R.id.title)
        subtitleView = findViewById(R.id.subtitle)
        typeChooser = findViewById(R.id.typeChooser)
        pinDisplay = findViewById(R.id.pinDisplay)
        pinPad = findViewById(R.id.pinPad)
        patternView = findViewById(R.id.patternView)
        btnBiometric = findViewById(R.id.btnBiometric)
        btnConfirmPin = findViewById(R.id.btnConfirmPin)
        btnClear = findViewById(R.id.btnClear)

        findViewById<Button>(R.id.btnChoosePin).setOnClickListener {
            selectedType = AppLockType.PIN
            enterSecretPhase(isChange = mode == Mode.CHANGE_CHOOSE)
        }
        findViewById<Button>(R.id.btnChoosePattern).setOnClickListener {
            selectedType = AppLockType.PATTERN
            enterSecretPhase(isChange = mode == Mode.CHANGE_CHOOSE)
        }
        btnConfirmPin.setOnClickListener { onPinConfirm() }
        btnClear.setOnClickListener { clearInput() }
        btnBiometric.setOnClickListener { promptBiometric() }

        buildPinPad()
        patternView.listener = object : PatternLockView.Listener {
            override fun onPatternComplete(cells: List<Int>) {
                onPatternReady(cells)
            }
        }

        when (intent.getStringExtra(EXTRA_MODE) ?: MODE_UNLOCK) {
            MODE_SETUP -> {
                mode = Mode.SETUP_CHOOSE
                showChooseType(setup = true)
            }
            MODE_CHANGE -> {
                mode = Mode.CHANGE_VERIFY
                selectedType = store.lockType ?: AppLockType.PIN
                showUnlockUi(changeVerify = true)
            }
            else -> {
                mode = Mode.UNLOCK
                selectedType = store.lockType ?: AppLockType.PIN
                showUnlockUi(changeVerify = false)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (mode == Mode.UNLOCK || mode == Mode.SETUP_CHOOSE || mode == Mode.SETUP_ENTER || mode == Mode.SETUP_CONFIRM) {
            // Mandatory lock — leave the app instead of bypassing.
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }

    private fun showChooseType(setup: Boolean) {
        titleView.text = if (setup) "تنظیم رمز ورود" else "تغییر رمز ورود"
        subtitleView.text = "نوع رمز را انتخاب کنید"
        typeChooser.visibility = View.VISIBLE
        hideInputViews()
        btnBiometric.visibility = View.GONE
    }

    private fun enterSecretPhase(isChange: Boolean) {
        mode = if (isChange) Mode.CHANGE_ENTER else Mode.SETUP_ENTER
        firstSecret = ""
        typeChooser.visibility = View.GONE
        titleView.text = if (selectedType == AppLockType.PIN) "تعیین PIN" else "تعیین الگو"
        subtitleView.text = if (selectedType == AppLockType.PIN) {
            "یک PIN چهار تا شش رقمی وارد کنید"
        } else {
            "الگوی حداقل ۴ نقطه بکشید"
        }
        showInputForType()
    }

    private fun showUnlockUi(changeVerify: Boolean) {
        titleView.text = if (changeVerify) "تأیید رمز فعلی" else "ورود به برنامه"
        subtitleView.text = if (selectedType == AppLockType.PIN) {
            "PIN خود را وارد کنید"
        } else {
            "الگوی خود را بکشید"
        }
        typeChooser.visibility = View.GONE
        showInputForType()
        val showBio = !changeVerify && store.biometricEnabled && BiometricHelper.canAuthenticate(this)
        btnBiometric.visibility = if (showBio) View.VISIBLE else View.GONE
        if (showBio) {
            btnBiometric.post { promptBiometric() }
        }
    }

    private fun showInputForType() {
        clearInput()
        when (selectedType) {
            AppLockType.PIN -> {
                pinDisplay.visibility = View.VISIBLE
                pinPad.visibility = View.VISIBLE
                btnConfirmPin.visibility = View.VISIBLE
                btnClear.visibility = View.VISIBLE
                patternView.visibility = View.GONE
            }
            AppLockType.PATTERN -> {
                pinDisplay.visibility = View.GONE
                pinPad.visibility = View.GONE
                btnConfirmPin.visibility = View.GONE
                btnClear.visibility = View.VISIBLE
                patternView.visibility = View.VISIBLE
            }
        }
    }

    private fun hideInputViews() {
        pinDisplay.visibility = View.GONE
        pinPad.visibility = View.GONE
        patternView.visibility = View.GONE
        btnConfirmPin.visibility = View.GONE
        btnClear.visibility = View.GONE
    }

    private fun buildPinPad() {
        pinPad.removeAllViews()
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
        keys.forEach { key ->
            val btn = Button(this).apply {
                text = key
                textSize = 20f
                setTextColor(0xFF193D8D.toInt())
                setBackgroundColor(0x00FFFFFF)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dp(72)
                    height = dp(64)
                    setGravity(Gravity.CENTER)
                }
                isEnabled = key.isNotEmpty()
                setOnClickListener {
                    when (key) {
                        "⌫" -> {
                            if (pinBuffer.isNotEmpty()) {
                                pinBuffer.deleteCharAt(pinBuffer.length - 1)
                                refreshPinDisplay()
                            }
                        }
                        "" -> Unit
                        else -> {
                            if (pinBuffer.length < 6) {
                                pinBuffer.append(key)
                                refreshPinDisplay()
                                if ((mode == Mode.UNLOCK || mode == Mode.CHANGE_VERIFY) &&
                                    pinBuffer.length >= 4
                                ) {
                                    val candidate = pinBuffer.toString()
                                    if (store.verify(candidate)) {
                                        handleSecret(candidate)
                                    } else if (pinBuffer.length == 6) {
                                        Toast.makeText(
                                            this@AppLockActivity,
                                            "رمز نادرست است",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        clearInput()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            pinPad.addView(btn)
        }
    }

    private fun refreshPinDisplay() {
        pinDisplay.text = "•".repeat(pinBuffer.length)
    }

    private fun clearInput() {
        pinBuffer.clear()
        refreshPinDisplay()
        patternView.clearPattern()
    }

    private fun onPinConfirm() {
        val pin = pinBuffer.toString()
        if (pin.length !in 4..6) {
            Toast.makeText(this, "PIN باید ۴ تا ۶ رقم باشد", Toast.LENGTH_SHORT).show()
            return
        }
        handleSecret(pin)
    }

    private fun onPatternReady(cells: List<Int>) {
        if (cells.size < 4) {
            Toast.makeText(this, "الگو باید حداقل ۴ نقطه باشد", Toast.LENGTH_SHORT).show()
            patternView.clearPattern()
            return
        }
        handleSecret(AppLockStore.patternToSecret(cells))
    }

    private fun handleSecret(secret: String) {
        when (mode) {
            Mode.UNLOCK -> {
                if (store.verify(secret)) {
                    finishUnlocked()
                } else {
                    Toast.makeText(this, "رمز نادرست است", Toast.LENGTH_SHORT).show()
                    clearInput()
                }
            }
            Mode.CHANGE_VERIFY -> {
                if (store.verify(secret)) {
                    mode = Mode.CHANGE_CHOOSE
                    showChooseType(setup = false)
                } else {
                    Toast.makeText(this, "رمز نادرست است", Toast.LENGTH_SHORT).show()
                    clearInput()
                }
            }
            Mode.SETUP_ENTER, Mode.CHANGE_ENTER -> {
                firstSecret = secret
                mode = if (mode == Mode.SETUP_ENTER) Mode.SETUP_CONFIRM else Mode.CHANGE_CONFIRM
                titleView.text = "تکرار رمز"
                subtitleView.text = "رمز را دوباره وارد کنید"
                clearInput()
            }
            Mode.SETUP_CONFIRM, Mode.CHANGE_CONFIRM -> {
                if (secret != firstSecret) {
                    Toast.makeText(this, "رمزها یکسان نیستند", Toast.LENGTH_SHORT).show()
                    mode = if (mode == Mode.SETUP_CONFIRM) Mode.SETUP_ENTER else Mode.CHANGE_ENTER
                    firstSecret = ""
                    titleView.text = if (selectedType == AppLockType.PIN) "تعیین PIN" else "تعیین الگو"
                    subtitleView.text = "دوباره وارد کنید"
                    clearInput()
                    return
                }
                store.setLock(selectedType, secret)
                Toast.makeText(this, "رمز ذخیره شد", Toast.LENGTH_SHORT).show()
                finishUnlocked()
            }
            else -> Unit
        }
    }

    private fun promptBiometric() {
        BiometricHelper.showPrompt(
            activity = this,
            title = "ورود بیومتریک",
            subtitle = "اثرانگشت یا تشخیص چهره",
            onSuccess = { finishUnlocked() },
            onError = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
        )
    }

    private fun finishUnlocked() {
        AppLockSession.markUnlocked()
        val reopen = intent.getBooleanExtra(EXTRA_REOPEN_MAIN, true)
        if (reopen) {
            val main = Intent(this, MehdiActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("json", "ph/main")
            }
            startActivity(main)
        }
        setResult(RESULT_OK)
        finish()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_MODE = "mode"
        const val EXTRA_REOPEN_MAIN = "reopen_main"
        const val MODE_UNLOCK = "unlock"
        const val MODE_SETUP = "setup"
        const val MODE_CHANGE = "change"

        fun unlockIntent(context: Context): Intent =
            Intent(context, AppLockActivity::class.java).putExtra(EXTRA_MODE, MODE_UNLOCK)

        fun setupIntent(context: Context, reopenMain: Boolean = true): Intent =
            Intent(context, AppLockActivity::class.java)
                .putExtra(EXTRA_MODE, MODE_SETUP)
                .putExtra(EXTRA_REOPEN_MAIN, reopenMain)

        fun changeIntent(context: Context): Intent =
            Intent(context, AppLockActivity::class.java)
                .putExtra(EXTRA_MODE, MODE_CHANGE)
                .putExtra(EXTRA_REOPEN_MAIN, false)
    }
}
