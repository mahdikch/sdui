package com.yandex.divkit.demo.ui

import android.net.Uri
import android.util.Log
import com.yandex.div.core.view2.Div2View
import com.yandex.div.data.VariableMutationException
import com.yandex.div.internal.Assert
import com.yandex.div.json.expressions.ExpressionResolver
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.util.Date

object DateTimeCompareActionHelper {

    private const val TAG = "DateTimeCompare"

    private val LITERAL_DATE_TIME_PATTERN =
        Regex("""^(\d{4}/\d{1,2}/\d{1,2})_(\d{1,2}:\d{1,2}(?::\d{1,2})?)$""")

    fun handleIsDateTimeLessEqualTodayNow(
        div2View: Div2View,
        resolver: ExpressionResolver,
        currentTime: String?,
        resultVar: String?,
    ): Boolean {
        Log.d(TAG, "=== is_date_and_time_less_equal_today_now ===")
        Log.d(TAG, "raw current_time='$currentTime' result_var='$resultVar'")
        if (currentTime.isNullOrBlank() || resultVar.isNullOrBlank()) {
            Log.e(TAG, "missing params: current_time/result_var")
            Assert.fail("current_time and result_var params are required")
            return false
        }

        val resultName = resultVar.trim()
        val resolved = resolveDateTimeValue(resolver, currentTime.trim())
        Log.d(TAG, "resolved current_time='$resolved'")
        val inputMillis = resolved?.let { parseDateTimeToMillis(it) }
            ?: resolved?.let { parseLiteralDateTimeToMillis(it) }
        val nowMillis = PersianDate().time
        if (inputMillis == null) {
            Log.w(TAG, "parse failed for '$resolved' → set $resultName=false")
            return setBooleanVariable(div2View, resultName, false)
        }
        // 2-minute tolerance for clock/picker skew when choosing "now"
        val pass = inputMillis <= nowMillis + 2 * 60 * 1000L
        Log.d(
            TAG,
            "compare input=${Date(inputMillis)} <= now=${Date(nowMillis)}(+2m)? $pass " +
                "(diff=${inputMillis - nowMillis}ms) → set $resultName=$pass"
        )
        return setBooleanVariable(div2View, resultName, pass)
    }

    fun parseLiteralDateTimeToMillis(value: String): Long? {
        val match = LITERAL_DATE_TIME_PATTERN.find(normalizeDigits(value.trim())) ?: return null
        val persianDate = parsePersianDate(match.groupValues[1]) ?: return null
        val (hour, minute) = parseTime(match.groupValues[2])
        return persianDate.setHour(hour).setMinute(minute).setSecond(0).time
    }

    fun handleIsBiggerDateTime(
        div2View: Div2View,
        resolver: ExpressionResolver,
        val1: String?,
        val2: String?,
        resultVar: String?,
    ): Boolean {
        Log.d(TAG, "=== is_bigger_date_and_time ===")
        Log.d(TAG, "raw val1='$val1' val2='$val2' result_var='$resultVar'")
        if (val1.isNullOrBlank() || val2.isNullOrBlank() || resultVar.isNullOrBlank()) {
            Log.e(TAG, "missing params: val1/val2/result_var")
            Assert.fail("val1, val2 and result_var params are required")
            return false
        }

        val resultName = resultVar.trim()
        val val1Resolved = resolveDateTimeValue(resolver, val1.trim())
        val val2Resolved = resolveDateTimeValue(resolver, val2.trim())
        Log.d(TAG, "resolved val1='$val1Resolved' val2='$val2Resolved'")

        val millis1 = val1Resolved?.let { parseDateTimeToMillis(it) }
        val millis2 = val2Resolved?.let { parseDateTimeToMillis(it) }
        Log.d(
            TAG,
            "millis1=$millis1 (${millis1?.let { Date(it) }}) " +
                "millis2=$millis2 (${millis2?.let { Date(it) }})"
        )
        if (millis1 == null || millis2 == null) {
            Log.w(
                TAG,
                "parse failed → set $resultName=false " +
                    "(val1Resolved=$val1Resolved, val2Resolved=$val2Resolved)"
            )
            return setBooleanVariable(div2View, resultName, false)
        }
        // true when val1 is after or equal to val2 (police arrival not before accident)
        val pass = millis1 >= millis2
        Log.d(
            TAG,
            "compare police(val1) >= accident(val2)? $pass " +
                "(${millis1 - millis2}ms diff) → set $resultName=$pass"
        )
        return setBooleanVariable(div2View, resultName, pass)
    }

    fun Uri.getTrimmedQueryParameter(key: String): String? {
        getQueryParameter(key)?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        queryParameterNames.forEach { paramName ->
            if (paramName.trim() == key) {
                return getQueryParameter(paramName)?.trim()?.takeIf { it.isNotEmpty() }
            }
        }
        return null
    }

    private fun resolveDateTimeValue(
        resolver: ExpressionResolver,
        input: String,
    ): String? {
        var value = input.trim()
        Log.d(TAG, "resolve input='$input'")
        if (value.contains("@{")) {
            value = evaluateVariableExpressions(value, resolver).trim()
            Log.d(TAG, "resolve after expression eval='$value'")
        }
        // Prefer variable lookup for plain names (e.g. variable_police_date_and_time).
        // Underscore alone must NOT mark a string as a datetime literal.
        if (!looksLikeDateTimeLiteral(value)) {
            val fromVar = getVariableValue(resolver, value)?.trim()?.takeIf { v -> v.isNotEmpty() }
            Log.d(
                TAG,
                "resolve as variable name='$value' → '$fromVar' " +
                    "(literal? ${looksLikeDateTimeLiteral(value)})"
            )
            fromVar?.let { return it }
        }
        val asLiteral = value.takeIf { it.isNotEmpty() && looksLikeDateTimeLiteral(it) }
        if (asLiteral != null) {
            Log.d(TAG, "resolve as literal='$asLiteral'")
            return asLiteral
        }
        val fallback = getVariableValue(resolver, value)?.trim()?.takeIf { it.isNotEmpty() }
        Log.d(TAG, "resolve fallback variable lookup='$fallback'")
        return fallback
    }

    private fun looksLikeDateTimeLiteral(value: String): Boolean {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed.toLongOrNull() != null) return true
        // 1405/03/09_12:30 or 1405/03/09 12:30 or 1405/03/09++12:30
        if (Regex("""^\d{4}/\d{1,2}/\d{1,2}([_+\s-].+)?$""").containsMatchIn(trimmed)) return true
        return false
    }

    internal fun parseDateTimeToMillis(value: String): Long? {
        val trimmed = normalizeDigits(value.trim())
        if (trimmed.isEmpty()) {
            Log.w(TAG, "parse empty value")
            return null
        }

        trimmed.toLongOrNull()?.let { timestamp ->
            val ms = if (timestamp < 100_000_000_000L) timestamp * 1000 else timestamp
            Log.d(TAG, "parse as epoch '$trimmed' → $ms")
            return ms
        }

        val (datePart, timePart) = splitDateAndTime(trimmed)
        Log.d(TAG, "parse split value='$trimmed' → date='$datePart' time='$timePart'")
        val persianDate = parsePersianDate(datePart)
        if (persianDate == null) {
            Log.w(TAG, "parsePersianDate failed for date='$datePart'")
            return null
        }
        val (hour, minute) = parseTime(timePart)
        val ms = persianDate.setHour(hour).setMinute(minute).setSecond(0).time
        Log.d(TAG, "parse ok date='$datePart' time=$hour:$minute → $ms (${Date(ms)})")
        return ms
    }

    private fun normalizeDigits(value: String): String {
        val sb = StringBuilder(value.length)
        for (ch in value) {
            sb.append(
                when (ch) {
                    in '۰'..'۹' -> ('0'.code + (ch - '۰')).toChar()
                    in '٠'..'٩' -> ('0'.code + (ch - '٠')).toChar()
                    else -> ch
                }
            )
        }
        return sb.toString()
    }

    private fun splitDateAndTime(value: String): Pair<String, String> {
        // Primary format: 1405/03/09_12:30
        val underscoreMatch = Regex("^(\\d{4}/\\d{1,2}/\\d{1,2})_(\\d{1,2}:\\d{1,2}(?::\\d{1,2})?)$").find(value)
        if (underscoreMatch != null) {
            return underscoreMatch.groupValues[1] to underscoreMatch.groupValues[2]
        }

        if (value.contains("++")) {
            val parts = value.split("++", limit = 2)
            return parts[0].trim() to (parts.getOrNull(1)?.trim()?.ifEmpty { "00:00" } ?: "00:00")
        }

        val spaceIndex = value.indexOf(' ')
        if (spaceIndex > 0) {
            return value.substring(0, spaceIndex).trim() to value.substring(spaceIndex + 1).trim()
        }

        val dashTimeMatch = Regex("^(\\d{4}/\\d{1,2}/\\d{1,2})-(\\d{1,2}:\\d{1,2}(?::\\d{1,2})?)$").find(value)
        if (dashTimeMatch != null) {
            return dashTimeMatch.groupValues[1] to dashTimeMatch.groupValues[2]
        }

        return value.trim() to "00:00"
    }

    private fun parsePersianDate(datePart: String): PersianDate? {
        val patterns = listOf("yyyy/MM/dd", "yyyy/M/d", "Y/m/d")
        val formatter = PersianDateFormat()
        for (pattern in patterns) {
            try {
                return formatter.parse(datePart, pattern)
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun parseTime(timePart: String): Pair<Int, Int> {
        val parts = timePart.split(":")
        val hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
        return hour to minute
    }

    private fun getVariableValue(
        resolver: ExpressionResolver,
        variableName: String,
    ): String? {
        return try {
            val value = evaluateVariableExpressions("@{$variableName}", resolver)
                .takeIf { it.isNotEmpty() && it != "@{$variableName}" }
            Log.d(TAG, "getVariable('$variableName') → '$value'")
            value
        } catch (e: Exception) {
            Log.w(TAG, "getVariable('$variableName') failed: ${e.message}")
            null
        }
    }

    private fun evaluateVariableExpressions(
        inputString: String,
        expressionResolver: ExpressionResolver,
    ): String {
        if (!inputString.contains("@{")) return inputString

        val variablePattern = Regex("@\\{([^}]+)\\}")
        var result = inputString

        variablePattern.findAll(inputString).forEach { matchResult ->
            val fullMatch = matchResult.value
            val variableName = matchResult.groupValues[1]
            try {
                val expressionString = "@{$variableName}"
                val evaluable = com.yandex.div.evaluable.Evaluable.lazy(expressionString)
                val variableValue = expressionResolver.get<Any, Any>(
                    "date_compare_$variableName",
                    expressionString,
                    evaluable,
                    null,
                    object : com.yandex.div.internal.parser.ValueValidator<Any> {
                        override fun isValid(value: Any): Boolean = true
                    },
                    object : com.yandex.div.internal.parser.TypeHelper<Any> {
                        override fun isTypeValid(value: Any): Boolean = true
                        override val typeDefault: Any = ""
                    },
                    com.yandex.div.json.ParsingErrorLogger.LOG
                )
                result = result.replace(fullMatch, variableValue?.toString() ?: "")
            } catch (_: Exception) {
            }
        }

        return result
    }

    private fun setBooleanVariable(div2View: Div2View, name: String, value: Boolean): Boolean {
        return try {
            div2View.setVariable(name, value.toString())
            Log.d(TAG, "setVariable $name=$value OK")
            true
        } catch (e: VariableMutationException) {
            Log.e(TAG, "setVariable $name=$value FAILED: ${e.message}", e)
            Assert.fail("Variable '$name' mutation failed: ${e.message}", e)
            false
        }
    }
}
