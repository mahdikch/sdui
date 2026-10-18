package com.yandex.divkit.demo.utils

import com.google.gson.Gson
import com.yandex.divkit.demo.data.entities.ListItemDto
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Generic helpers: DivKit array → bottom_sheet ListItemDto DB payload,
 * and path-based field reads for JSON-driven forms.
 */
object ArrayListDbHelper {

    private val placeholderRegex = Regex("""\{([^{}]+)\}""")

    fun toJsonArray(raw: Any?): JSONArray {
        return when (raw) {
            is JSONArray -> raw
            is List<*> -> JSONArray().also { arr ->
                raw.forEach { item ->
                    when (item) {
                        is JSONObject -> arr.put(item)
                        is Map<*, *> -> {
                            val obj = JSONObject()
                            item.forEach { (k, v) ->
                                if (k != null) obj.put(k.toString(), v ?: JSONObject.NULL)
                            }
                            arr.put(obj)
                        }
                        is String -> runCatching { arr.put(JSONObject(item)) }
                        else -> item?.let { arr.put(it) }
                    }
                }
            }
            is String -> runCatching { JSONArray(raw) }.getOrElse {
                runCatching { JSONArray().put(JSONObject(raw)) }.getOrDefault(JSONArray())
            }
            else -> {
                val asString = raw?.toString().orEmpty()
                if (asString.isBlank() || asString == "null") JSONArray()
                else runCatching { JSONArray(asString) }.getOrDefault(JSONArray())
            }
        }
    }

    /**
     * Build ListItemDto JSON for DB.
     * @param nested if set, flatten nested arrays under each root item
     * @param titleTemplate e.g. `{id} | مبلغ: {loanAmount:number} | نرخ: {loanFacilityRate}%`
     * @param idPath path to id field, default `id`
     */
    fun buildListJson(
        arrayRaw: Any?,
        idPath: String = "id",
        titleTemplate: String,
        nested: String? = null,
        unique: Boolean = true,
    ): String {
        val root = toJsonArray(arrayRaw)
        val items = mutableListOf<ListItemDto>()
        val seen = mutableSetOf<String>()

        fun addItem(obj: JSONObject) {
            val id = pathString(obj, idPath)
            if (id.isBlank()) return
            if (unique && !seen.add(id)) return
            val title = applyTemplate(titleTemplate, obj)
            items += ListItemDto(
                id = id,
                parentId = "0",
                titleFa = title,
                titleEn = title,
                constraintList = emptyList(),
            )
        }

        if (nested.isNullOrBlank()) {
            for (i in 0 until root.length()) {
                val obj = root.optJSONObject(i) ?: continue
                addItem(obj)
            }
        } else {
            for (i in 0 until root.length()) {
                val parent = root.optJSONObject(i) ?: continue
                val nestedArr = parent.optJSONArray(nested) ?: continue
                for (j in 0 until nestedArr.length()) {
                    val child = nestedArr.optJSONObject(j) ?: continue
                    addItem(child)
                }
            }
        }
        return Gson().toJson(items)
    }

    fun pathString(obj: JSONObject?, path: String): String {
        if (obj == null || path.isBlank()) return ""
        val parts = path.split('.')
        var current: Any? = obj
        for (part in parts) {
            when (val c = current) {
                is JSONObject -> current = if (c.has(part)) c.opt(part) else null
                else -> return ""
            }
        }
        return when (val v = current) {
            null, JSONObject.NULL -> ""
            is String -> v.takeIf { it != "null" }.orEmpty()
            else -> v.toString().takeIf { it != "null" }.orEmpty()
        }
    }

    fun pathValue(root: Any?, index: Int, path: String): String {
        val arr = toJsonArray(root)
        val obj = arr.optJSONObject(index) ?: return ""
        return pathString(obj, path)
    }

    fun findInArray(
        arrayRaw: Any?,
        matchPath: String,
        matchValue: String,
        nested: String? = null,
    ): JSONObject? {
        if (matchValue.isBlank()) return null
        val root = toJsonArray(arrayRaw)
        if (nested.isNullOrBlank()) {
            for (i in 0 until root.length()) {
                val obj = root.optJSONObject(i) ?: continue
                if (pathString(obj, matchPath) == matchValue) return obj
            }
        } else {
            for (i in 0 until root.length()) {
                val parent = root.optJSONObject(i) ?: continue
                val nestedArr = parent.optJSONArray(nested) ?: continue
                for (j in 0 until nestedArr.length()) {
                    val child = nestedArr.optJSONObject(j) ?: continue
                    if (pathString(child, matchPath) == matchValue) return child
                }
            }
        }
        return null
    }

    /**
     * fields format: `sourcePath:targetVar,sourcePath2:targetVar2|number`
     */
    fun parseFieldMaps(fields: String): List<Triple<String, String, String?>> {
        if (fields.isBlank()) return emptyList()
        return fields.split(',')
            .mapNotNull { part ->
                val trimmed = part.trim()
                if (trimmed.isEmpty()) return@mapNotNull null
                val idx = trimmed.indexOf(':')
                if (idx <= 0) return@mapNotNull null
                val source = trimmed.substring(0, idx).trim()
                var target = trimmed.substring(idx + 1).trim()
                var format: String? = null
                val pipe = target.lastIndexOf('|')
                if (pipe > 0) {
                    format = target.substring(pipe + 1).trim().ifBlank { null }
                    target = target.substring(0, pipe).trim()
                }
                if (source.isBlank() || target.isBlank()) null
                else Triple(source, target, format)
            }
    }

    fun formatValue(raw: String, format: String?): String {
        if (raw.isBlank() || format.isNullOrBlank()) return raw
        return when (format.lowercase(Locale.US)) {
            "number" -> formatAmount(raw)
            else -> raw
        }
    }

    private fun applyTemplate(template: String, obj: JSONObject): String {
        return placeholderRegex.replace(template) { match ->
            val inner = match.groupValues[1].trim()
            val colon = inner.lastIndexOf(':')
            val (path, format) = if (colon > 0) {
                inner.substring(0, colon).trim() to inner.substring(colon + 1).trim()
            } else {
                inner to null
            }
            formatValue(pathString(obj, path), format)
        }
    }

    private fun formatAmount(raw: String): String {
        val normalized = raw.replace(",", "").trim()
        val number = normalized.toLongOrNull() ?: normalized.toDoubleOrNull()?.toLong()
        if (number != null) {
            val symbols = DecimalFormatSymbols(Locale.US)
            return DecimalFormat("#,###", symbols).format(number)
        }
        return raw
    }
}
