package com.yandex.divkit.demo.utils

import com.yandex.div.core.view2.Div2View
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import saman.zamani.persiandate.PersianDateFormat.PersianDateNumberCharacter

object AccidentRegisterDateTimeHelper {
    private val SCREENS = setOf(
        "Accidents/compromisefirst",
        "Accidents/unCompromisefirst",
    )

    fun applyIfNeeded(screenKey: String?, div2View: Div2View?) {
        if (div2View == null) return
        if (screenKey.isNullOrBlank() || screenKey !in SCREENS) return
        try {
            val now = PersianDate()
            val date = PersianDateFormat.format(
                now,
                "Y/m/d",
                PersianDateNumberCharacter.ENGLISH,
            )
            val time = String.format("%02d:%02d", now.hour, now.minute)
            div2View.setVariable("variable_register_accident_date", date)
            div2View.setVariable("variable_register_accident_time", time)
            android.util.Log.d(
                "AccidentRegisterDateTime",
                "set register date/time for $screenKey -> $date $time",
            )
        } catch (e: Exception) {
            android.util.Log.e("AccidentRegisterDateTime", "Failed to set register date/time", e)
        }
    }
}
