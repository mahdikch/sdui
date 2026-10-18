package com.yandex.divkit.demo.div.treeview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.lifecycle.LifecycleOwner
import com.yandex.divkit.demo.ui.LoadScreenListener
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class TreeChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    data class Config(
        val rootId: String,
        val rootLabel: String,
        val rootHasChildren: Boolean,
        val servicePath: String,
        val parentParamName: String = "parent_id",
        val token: String = "empty",
        val extraParams: Map<String, String> = emptyMap(),
        val dataKeyPrefix: String = "tree_nodes_",
        val autoExpandRoot: Boolean = true
    )

    private val nodesCache = ConcurrentHashMap<String, TreeNode>()
    private val childContainers = ConcurrentHashMap<String, LinearLayout>()
    private val nodeDepth = ConcurrentHashMap<String, Int>()
    private val observedKeys = mutableSetOf<String>()
    private val loadingViews = ConcurrentHashMap<String, View>()
    private val indicatorViews = ConcurrentHashMap<String, ImageView>()

    private var loadScreenListener: LoadScreenListener? = null
    private var mehdiViewModel: MehdiViewModel? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var config: Config? = null

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun initialize(
        config: Config,
        viewModel: MehdiViewModel?,
        owner: LifecycleOwner?,
        listener: LoadScreenListener?
    ) {
        this.config = config
        mehdiViewModel = viewModel
        lifecycleOwner = owner
        loadScreenListener = listener

        removeAllViews()
        nodesCache.clear()
        childContainers.clear()
        nodeDepth.clear()
        observedKeys.clear()
        loadingViews.clear()

        val rootNode = TreeNode(
            id = config.rootId,
            label = config.rootLabel,
            hasChildren = config.rootHasChildren
        )
        addNodeView(rootNode, 0, this)
        nodeDepth[rootNode.id] = 0
        nodesCache[rootNode.id] = rootNode

        if (config.autoExpandRoot && rootNode.hasChildren) {
            toggleNode(rootNode.id)
        }
    }

    private fun addNodeView(node: TreeNode, depth: Int, parent: LinearLayout) {
        val wrapper = LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(0)
        }

        val header = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val horizontalPadding = dp((depth * 24) + 12)
            setPadding(horizontalPadding, dp(12), dp(12), dp(12))
            setBackgroundColor(Color.TRANSPARENT)
        }

        val indicator = ImageView(context).apply {
            setImageResource(
                if (node.hasChildren) android.R.drawable.arrow_down_float
                else android.R.drawable.presence_invisible
            )
            alpha = if (node.hasChildren) 1f else 0.4f
        }

        val title = TextView(context).apply {
            text = node.label
            textSize = 16f
            setTextColor(Color.parseColor("#1F1F1F"))
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        header.addView(indicator)
        header.addView(title)

        header.setOnClickListener {
            if (node.hasChildren) {
                toggleNode(node.id)
            }
        }

        val divider = View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 1).apply {
                setMargins(dp((depth * 24) + 12), 0, dp(12), 0)
            }
            setBackgroundColor(Color.parseColor("#E5E7EB"))
        }

        val childrenContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            visibility = GONE
        }

        wrapper.addView(header)
        wrapper.addView(divider)
        wrapper.addView(childrenContainer)

        parent.addView(wrapper)

        childContainers[node.id] = childrenContainer
        nodeDepth[node.id] = depth
        indicatorViews[node.id] = indicator
    }

    private fun toggleNode(nodeId: String) {
        val node = nodesCache[nodeId] ?: return
        val container = childContainers[nodeId] ?: return

        if (container.visibility == VISIBLE) {
            container.visibility = GONE
            indicatorViews[nodeId]?.rotation = 0f
        } else {
            container.visibility = VISIBLE
            indicatorViews[nodeId]?.rotation = 90f
            if (container.childCount == 0 && node.hasChildren) {
                fetchChildren(node)
            }
        }
    }

    private fun fetchChildren(node: TreeNode) {
        val cfg = config ?: return
        val parentId = node.id
        val dataKey = cfg.dataKeyPrefix + parentId

        showLoading(parentId)
        observeDataKey(parentId, dataKey)

        val request = HashMap<String, String>()
        request["path"] = cfg.servicePath
        request["ph/token"] = cfg.token




        request[cfg.parentParamName] = parentId
        request["tree_data_key"] = dataKey
        cfg.extraParams.forEach { (key, value) -> request[key] = value }

        if (loadScreenListener == null) {
            Log.w(TAG, "LoadScreenListener is null; cannot trigger call_service")
            return
        }

        loadScreenListener?.onRequest(request)
    }

    private fun observeDataKey(parentId: String, dataKey: String) {
        if (observedKeys.contains(dataKey)) return
        val vm = mehdiViewModel ?: return
        val owner = lifecycleOwner ?: return

        vm.getJson(dataKey).observe(owner) { entries ->
            val json = entries?.firstOrNull()?.value ?: return@observe
            try {
                val nodes = parseNodes(json)
                val depth = (nodeDepth[parentId] ?: 0) + 1
                renderChildren(parentId, nodes, depth)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse children json for $parentId", e)
                showError(parentId, e.message ?: "خطا در تجزیه داده‌ها")
            }
        }

        observedKeys.add(dataKey)
    }

    private fun renderChildren(parentId: String, nodes: List<TreeNode>, depth: Int) {
        val container = childContainers[parentId] ?: return
        container.removeAllViews()
        hideLoading(parentId)

        if (nodes.isEmpty()) {
            container.addView(createEmptyState(depth))
            return
        }

        nodes.forEach { node ->
            nodesCache[node.id] = node
            addNodeView(node, depth, container)
        }
    }

    private fun parseNodes(json: String): List<TreeNode> {
        val array = JSONArray(json)
        val nodes = mutableListOf<TreeNode>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            parseNode(obj)?.let { nodes.add(it) }
        }
        return nodes
    }

    private fun parseNode(obj: JSONObject): TreeNode? {
        val id = obj.optString("id")
        if (id.isEmpty()) return null
        val label = obj.optString("label",
            obj.optString("title",
                obj.optString("name", id)
            )
        )
        val hasChildren = obj.optBoolean("has_children",
            obj.optBoolean("hasChildren", false))
        return TreeNode(
            id = id,
            label = label,
            hasChildren = hasChildren
        )
    }

    private fun showLoading(parentId: String) {
        val container = childContainers[parentId] ?: return
        if (loadingViews.containsKey(parentId)) return
        val progress = ProgressBar(context)
        container.addView(progress)
        loadingViews[parentId] = progress
    }

    private fun hideLoading(parentId: String) {
        val view = loadingViews.remove(parentId) ?: return
        val parent = view.parent as? LinearLayout ?: return
        parent.removeView(view)
    }

    private fun showError(parentId: String, message: String) {
        hideLoading(parentId)
        val container = childContainers[parentId] ?: return
        container.removeAllViews()
        val errorText = TextView(context).apply {
            text = message
            setTextColor(Color.parseColor("#B3261E"))
            setPadding(32)
        }
        container.addView(errorText)
    }

    private fun createEmptyState(depth: Int): View =
        TextView(context).apply {
        text = "داده‌ای یافت نشد"
            setTextColor(Color.parseColor("#6B7280"))
        setPadding(dp((depth * 24) + 24), dp(8), dp(8), dp(8))
        }

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    data class TreeNode(
        val id: String,
        val label: String,
        val hasChildren: Boolean
    )

    companion object {
        private const val TAG = "TreeChartView"
    }
}

