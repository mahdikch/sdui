package com.yandex.divkit.demo.div.treeview

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class StaticTreeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    enum class SelectionMode {
        NONE,
        SINGLE,
        MULTI,
    }

    data class TreeNode(
        val id: String,
        val parentId: String,
        val name: String,
        val code: String,
        val hasChildren: Boolean
    )

    data class Config(
        val rootKey: String = "tree",
        val idKey: String = "Id",
        val parentIdKey: String = "parentId",
        val nameKey: String = "name",
        val codeKey: String = "code",
        val childsKey: String = "childs",
        val autoExpandRoot: Boolean = true,
        val autoExpandSingleChild: Boolean = true,
        val selectionMode: SelectionMode = SelectionMode.NONE
    )

    private val nodesCache = ConcurrentHashMap<String, TreeNode>()
    private val childContainers = ConcurrentHashMap<String, LinearLayout>()
    private val nodeDepth = ConcurrentHashMap<String, Int>()
    private val chevronViews = ConcurrentHashMap<String, TextView>()
    private val headerViews = ConcurrentHashMap<String, LinearLayout>()
    private val selectionBadgeViews = ConcurrentHashMap<String, TextView>()
    private val expandedNodes = mutableSetOf<String>()
    private val selectedNodes = mutableSetOf<String>()

    private var config: Config = Config()
    private var jsonData: JSONObject? = null
    private var onNodeSelectedListener: ((code: String, node: TreeNode) -> Unit)? = null
    private var onSelectionChangedListener: ((selectedCodes: List<String>) -> Unit)? = null

    init {
        orientation = VERTICAL
        setBackgroundColor(COLOR_SURFACE)
        setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
    }

    fun initialize(
        jsonString: String,
        config: Config = Config(),
        onNodeSelected: ((code: String, node: TreeNode) -> Unit)? = null,
        onSelectionChanged: ((selectedCodes: List<String>) -> Unit)? = null
    ) {
        this.config = config
        this.onNodeSelectedListener = onNodeSelected
        this.onSelectionChangedListener = onSelectionChanged

        removeAllViews()
        nodesCache.clear()
        childContainers.clear()
        nodeDepth.clear()
        chevronViews.clear()
        headerViews.clear()
        selectionBadgeViews.clear()
        expandedNodes.clear()
        selectedNodes.clear()

        try {
            jsonData = JSONObject(jsonString)
            val rootObj = jsonData?.optJSONObject(config.rootKey)

            if (rootObj != null) {
                val rootNode = parseNode(rootObj)
                if (rootNode != null) {
                    nodesCache[rootNode.id] = rootNode
                    nodeDepth[rootNode.id] = 0
                    addNodeView(rootNode, 0, this)

                    if (rootNode.hasChildren) {
                        post { expandRootIfNeeded(rootNode, rootObj) }
                    }
                }
            }
        } catch (e: Exception) {
            showError("خطا در پارس JSON: ${e.message}")
        }
    }

    private fun parseNode(obj: JSONObject): TreeNode? {
        val id = obj.optString(config.idKey, "").takeIf { it.isNotEmpty() } ?: return null
        val parentId = obj.optString(config.parentIdKey, "0")
        val name = obj.optString(config.nameKey, "")
        val code = obj.optString(config.codeKey, "")
        val childs = obj.optJSONArray(config.childsKey)
        val hasChildren = childs != null && childs.length() > 0

        return TreeNode(
            id = id,
            parentId = parentId,
            name = name,
            code = code,
            hasChildren = hasChildren
        )
    }

    private fun addNodeView(node: TreeNode, depth: Int, parent: LinearLayout) {
        val wrapper = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        val header = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                val startMargin = (depth * 20).dpToPx()
                setMargins(startMargin, 4.dpToPx(), 0, 4.dpToPx())
            }
            setPadding(10.dpToPx(), 10.dpToPx(), 12.dpToPx(), 10.dpToPx())
            background = createRowBackground(selectedNodes.contains(node.id))
            isClickable = true
            isFocusable = true
        }
        headerViews[node.id] = header

        val chevronSlot = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(32.dpToPx(), 32.dpToPx())
        }

        if (node.hasChildren) {
            val chevron = TextView(context).apply {
                text = "›"
                textSize = 20f
                setTextColor(COLOR_CHEVRON)
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
                rotation = if (expandedNodes.contains(node.id)) 90f else 0f
                setOnClickListener {
                    findNodeInJson(node.id)?.let { nodeObj ->
                        toggleNode(node.id, nodeObj)
                    }
                }
            }
            chevronViews[node.id] = chevron
            chevronSlot.addView(chevron)
        } else {
            chevronSlot.addView(View(context).apply {
                layoutParams = LayoutParams(32.dpToPx(), 32.dpToPx())
            })
        }
        header.addView(chevronSlot)

        val textColumn = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 4.dpToPx()
            }
        }

        val nameView = TextView(context).apply {
            text = node.name.ifBlank { "—" }
            textSize = 15f
            setTextColor(COLOR_TEXT_PRIMARY)
            typeface = Typeface.create(typeface, Typeface.BOLD)
        }
        textColumn.addView(nameView)

        if (node.code.isNotBlank()) {
            val codeView = TextView(context).apply {
                text = node.code
                textSize = 12f
                setTextColor(COLOR_TEXT_SECONDARY)
                setPadding(0, 2.dpToPx(), 0, 0)
            }
            textColumn.addView(codeView)
        }
        header.addView(textColumn)

        if (config.selectionMode != SelectionMode.NONE) {
            val badge = TextView(context).apply {
                text = "✓"
                textSize = 16f
                setTextColor(COLOR_ROW_SELECTED_STROKE)
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(24.dpToPx(), 24.dpToPx()).apply {
                    marginStart = 8.dpToPx()
                }
                visibility = if (selectedNodes.contains(node.id)) View.VISIBLE else View.INVISIBLE
            }
            selectionBadgeViews[node.id] = badge
            header.addView(badge)
        }

        header.setOnClickListener {
            onNodeSelectedListener?.invoke(node.code, node)
            when (config.selectionMode) {
                SelectionMode.SINGLE -> handleSingleSelection(node.id)
                SelectionMode.MULTI -> handleMultiSelection(node.id, !selectedNodes.contains(node.id))
                SelectionMode.NONE -> {}
            }
        }

        wrapper.addView(header)

        val childContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE
        }
        wrapper.addView(childContainer)
        childContainers[node.id] = childContainer

        parent.addView(wrapper)
    }

    private fun createRowBackground(selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10.dpToPx().toFloat()
            setColor(if (selected) COLOR_ROW_SELECTED else COLOR_ROW_NORMAL)
            if (selected) {
                setStroke(1.dpToPx(), COLOR_ROW_SELECTED_STROKE)
            }
        }
    }

    private fun updateNodeAppearance(nodeId: String) {
        val header = headerViews[nodeId] ?: return
        val isSelected = selectedNodes.contains(nodeId)
        header.background = createRowBackground(isSelected)
        selectionBadgeViews[nodeId]?.visibility =
            if (isSelected && config.selectionMode != SelectionMode.NONE) View.VISIBLE else View.INVISIBLE
    }

    private fun refreshAllAppearances() {
        headerViews.keys.forEach { updateNodeAppearance(it) }
    }

    private fun handleSingleSelection(nodeId: String) {
        selectedNodes.clear()
        selectedNodes.add(nodeId)
        refreshAllAppearances()
        notifySelectionChanged()
    }

    private fun handleMultiSelection(nodeId: String, isChecked: Boolean) {
        if (isChecked) {
            selectedNodes.add(nodeId)
        } else {
            selectedNodes.remove(nodeId)
        }
        updateNodeAppearance(nodeId)
        notifySelectionChanged()
    }

    private fun notifySelectionChanged() {
        val selectedCodes = selectedNodes.mapNotNull { nodeId ->
            nodesCache[nodeId]?.code
        }
        onSelectionChangedListener?.invoke(selectedCodes)
    }

    private fun expandRootIfNeeded(rootNode: TreeNode, rootObj: JSONObject) {
        val childCount = rootObj.optJSONArray(config.childsKey)?.length() ?: 0
        if (childCount == 0) return

        val shouldExpand = when {
            config.autoExpandSingleChild && childCount == 1 -> true
            config.autoExpandRoot -> true
            else -> false
        }
        if (shouldExpand) {
            expandNode(rootNode.id, rootObj)
        }
    }

    private fun toggleNode(nodeId: String, nodeObj: JSONObject) {
        val container = childContainers[nodeId] ?: return
        if (container.visibility == View.GONE) {
            expandNode(nodeId, nodeObj)
        } else {
            collapseNode(nodeId)
        }
    }

    private fun expandNode(nodeId: String, nodeObj: JSONObject) {
        val container = childContainers[nodeId] ?: return
        val chevron = chevronViews[nodeId]

        if (container.visibility == View.GONE) {
            container.visibility = View.VISIBLE
            chevron?.animate()?.rotation(90f)?.setDuration(180)?.start()
            expandedNodes.add(nodeId)
        }

        if (container.childCount == 0) {
            renderChildren(nodeId, nodeObj)
        } else {
            autoExpandSingleChildChain(nodeObj)
        }
    }

    private fun collapseNode(nodeId: String) {
        val container = childContainers[nodeId] ?: return
        val chevron = chevronViews[nodeId]
        container.visibility = View.GONE
        chevron?.animate()?.rotation(0f)?.setDuration(180)?.start()
        expandedNodes.remove(nodeId)
    }

    private fun autoExpandSingleChildChain(nodeObj: JSONObject) {
        if (!config.autoExpandSingleChild) return

        val childsArray = nodeObj.optJSONArray(config.childsKey) ?: return
        if (childsArray.length() != 1) return

        val childObj = childsArray.optJSONObject(0) ?: return
        val childNode = parseNode(childObj) ?: return
        if (!childNode.hasChildren) return

        expandNode(childNode.id, childObj)
    }

    private fun renderChildren(parentId: String, parentObj: JSONObject) {
        val container = childContainers[parentId] ?: return
        val childsArray = parentObj.optJSONArray(config.childsKey) ?: return
        val parentDepth = nodeDepth[parentId] ?: 0
        val childDepth = parentDepth + 1

        container.removeAllViews()

        if (childsArray.length() == 0) {
            showEmptyState(container)
            return
        }

        for (i in 0 until childsArray.length()) {
            val childObj = childsArray.optJSONObject(i) ?: continue
            val childNode = parseNode(childObj) ?: continue

            nodesCache[childNode.id] = childNode
            nodeDepth[childNode.id] = childDepth
            addNodeView(childNode, childDepth, container)
        }

        autoExpandSingleChildChain(parentObj)
    }

    private fun findNodeInJson(nodeId: String): JSONObject? {
        val rootObj = jsonData?.optJSONObject(config.rootKey) ?: return null
        return findNodeRecursive(rootObj, nodeId)
    }

    private fun findNodeRecursive(obj: JSONObject, targetId: String): JSONObject? {
        val currentId = obj.optString(config.idKey, "")
        if (currentId == targetId) return obj

        val childs = obj.optJSONArray(config.childsKey) ?: return null
        for (i in 0 until childs.length()) {
            val child = childs.optJSONObject(i) ?: continue
            val found = findNodeRecursive(child, targetId)
            if (found != null) return found
        }
        return null
    }

    private fun showEmptyState(container: LinearLayout) {
        val emptyText = TextView(context).apply {
            text = "فرزندی وجود ندارد"
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 12.dpToPx())
            setTextColor(COLOR_TEXT_SECONDARY)
        }
        container.addView(emptyText)
    }

    private fun showError(message: String) {
        removeAllViews()
        val errorText = TextView(context).apply {
            text = message
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
            setTextColor(COLOR_ERROR)
        }
        addView(errorText)
    }

    fun getSelectedCodes(): List<String> {
        return selectedNodes.mapNotNull { nodeId ->
            nodesCache[nodeId]?.code
        }
    }

    fun getSelectedNodes(): List<TreeNode> {
        return selectedNodes.mapNotNull { nodeId ->
            nodesCache[nodeId]
        }
    }

    fun selectByCode(code: String) {
        val nodeId = nodesCache.values.find { it.code == code }?.id ?: return

        when (config.selectionMode) {
            SelectionMode.SINGLE -> handleSingleSelection(nodeId)
            SelectionMode.MULTI -> handleMultiSelection(nodeId, true)
            SelectionMode.NONE -> {}
        }
    }

    fun clearSelection() {
        selectedNodes.clear()
        refreshAllAppearances()
        notifySelectionChanged()
    }

    fun isSelected(code: String): Boolean {
        val nodeId = nodesCache.values.find { it.code == code }?.id
        return nodeId != null && selectedNodes.contains(nodeId)
    }

    fun getPrimarySelectedNode(): TreeNode? {
        return getSelectedNodes().lastOrNull()
    }

    fun getNodeByCode(code: String): TreeNode? {
        return nodesCache.values.find { it.code == code }
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val COLOR_SURFACE = 0xFFF8FAFC.toInt()
        private const val COLOR_ROW_NORMAL = 0xFFFFFFFF.toInt()
        private const val COLOR_ROW_SELECTED = 0xFFE8F4FD.toInt()
        private const val COLOR_ROW_SELECTED_STROKE = 0xFF2196F3.toInt()
        private const val COLOR_TEXT_PRIMARY = 0xFF1F2937.toInt()
        private const val COLOR_TEXT_SECONDARY = 0xFF6B7280.toInt()
        private const val COLOR_CHEVRON = 0xFF64748B.toInt()
        private const val COLOR_ERROR = 0xFFD32F2F.toInt()
    }
}
