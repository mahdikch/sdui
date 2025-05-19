import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yandex.divkit.demo.div.offlineView.OfflineViewListAdapter
import com.yandex.divkit.demo.div.offlineView.OfflineViewListAdapter.OfflineViewListItemListener

class OfflineViewList @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    system:String = "",
    listener: OfflineViewListItemListener
) : RecyclerView(context, attrs) {

    private val entryAdapter = OfflineViewListAdapter(system, listener)

    init {
        layoutManager = LinearLayoutManager(context)
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        removeAllViews()

        adapter = entryAdapter
    }

    fun setData(data: MutableList<Map<String, String>>) {
        entryAdapter.submitList(data)
    }

    fun setOnSendClickListener(listener: (position: Int, item: Map<String, String>) -> Unit) {
        entryAdapter.onSendClick = listener
    }
}