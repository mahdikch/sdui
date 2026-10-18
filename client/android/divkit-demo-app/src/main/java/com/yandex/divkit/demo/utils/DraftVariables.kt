package com.yandex.divkit.demo.utils

import org.json.JSONObject

/**
 * Helpers for reading DivKit draft variable maps after the variable_ naming migration.
 */
object DraftVariables {
    fun extract(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            val card = root.optJSONObject("card") ?: root
            val vars = card.optJSONArray("variables") ?: return emptyMap()
            val map = mutableMapOf<String, String>()
            for (i in 0 until vars.length()) {
                val item = vars.optJSONObject(i) ?: continue
                val name = item.optString("name")
                if (name.isBlank()) continue
                val value = when (val rawValue = item.opt("value")) {
                    null, JSONObject.NULL -> ""
                    else -> rawValue.toString()
                }
                map[name] = value
                if (name.startsWith("variable_")) {
                    map.putIfAbsent(name.removePrefix("variable_"), value)
                } else {
                    map.putIfAbsent("variable_$name", value)
                }
            }
            map
        }.getOrDefault(emptyMap())
    }

    fun Map<String, String>.valueOf(vararg names: String): String {
        for (name in names) {
            val value = this[name]
            if (!value.isNullOrBlank() && value != "null" && value != "empty") {
                return value
            }
        }
        return ""
    }
}
