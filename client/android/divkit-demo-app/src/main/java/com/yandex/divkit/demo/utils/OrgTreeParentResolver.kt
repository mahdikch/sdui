package com.yandex.divkit.demo.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Finds the parent node Id for a selected road/org node by its [code].
 * That parent Id is what the server expects as policeCenterID.
 */
object OrgTreeParentResolver {

    private const val ASSET_PATH = "application/Accidents/org_tree.json"
    private var cachedAssetTree: String? = null

    fun findParentIdByCode(
        treeJson: String?,
        code: String?,
        context: Context? = null,
    ): String? {
        if (code.isNullOrBlank()) return null
        val trimmed = code.trim()
        findInTreeJson(treeJson, trimmed)?.let { return it }
        val assetTree = loadAssetTree(context)
        return findInTreeJson(assetTree, trimmed)
    }

    private fun findInTreeJson(treeJson: String?, code: String): String? {
        if (treeJson.isNullOrBlank()) return null
        return runCatching {
            val root = JSONObject(treeJson)
            val tree = root.optJSONObject("tree") ?: root
            findInNode(tree, code)
        }.getOrNull()
    }

    private fun loadAssetTree(context: Context?): String? {
        cachedAssetTree?.let { return it }
        if (context == null) return null
        return runCatching {
            context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
                .also { cachedAssetTree = it }
        }.getOrNull()
    }

    private fun findInNode(obj: JSONObject, code: String): String? {
        val nodeCode = obj.opt("code")?.toString().orEmpty()
        if (nodeCode == code) {
            return obj.opt("parentId")?.toString()?.takeIf { it.isNotBlank() && it != "0" && it != "null" }
        }
        val childs = obj.optJSONArray("childs") ?: return null
        return findInArray(childs, code)
    }

    private fun findInArray(arr: JSONArray, code: String): String? {
        for (i in 0 until arr.length()) {
            val child = arr.optJSONObject(i) ?: continue
            findInNode(child, code)?.let { return it }
        }
        return null
    }
}
