package com.yandex.divkit.demo.div.notificationList

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yandex.divkit.demo.R
import java.util.Date
import java.util.UUID

class NotificationListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), NotificationManager.NotificationListener {

    companion object {
        const val ALL_TITLES_FILTER = "همه"
    }

    private val titleText: TextView
    private val titleFilterRow: View
    private val titleFilterValue: TextView
    private val recyclerView: RecyclerView
    private val emptyStateText: TextView
    private val adapter: NotificationListAdapter
    
    private var onNotificationClickListener: ((NotificationItem) -> Unit)? = null
    private var onMarkAsReadListener: ((NotificationItem) -> Unit)? = null

    // Store references to recreate listeners in onAttachedToWindow
    private var loadScreenListener: com.yandex.divkit.demo.ui.LoadScreenListener? = null
    private var mehdiViewModel: com.yandex.divkit.demo.ui.activity.MehdiViewModel? = null
    
    // Filter notifications by type (if null, show all)
    private var typeFilter: String? = null
    // Filter notifications by title (null / ALL = show all)
    private var titleFilter: String? = null

    init {
        orientation = VERTICAL
        clipChildren = true
        clipToPadding = true
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        // Inflate the layout (<merge> attaches children directly)
        LayoutInflater.from(context)
            .inflate(R.layout.notification_list_layout, this, true)
        
        titleText = findViewById(R.id.notification_list_title)
        titleFilterRow = findViewById(R.id.notification_title_filter_row)
        titleFilterValue = findViewById(R.id.notification_title_filter_value)
        recyclerView = findViewById(R.id.notifications_recycler_view)
        emptyStateText = findViewById(R.id.empty_state_text)
        
        // Setup RecyclerView
        adapter = NotificationListAdapter()
        
        // Set this view as the click listener for the adapter
        adapter.setClickListener(object : NotificationListAdapter.NotificationClickListener {
            override fun onNotificationClick(notification: NotificationItem) {
                android.util.Log.d("NotificationListView", "Adapter callback: onNotificationClick for ${notification.id}")
                onNotificationClickListener?.invoke(notification)
            }
            
            override fun onMarkAsRead(notification: NotificationItem) {
                android.util.Log.d("NotificationListView", "Adapter callback: onMarkAsRead for ${notification.id}")
                onMarkAsReadListener?.invoke(notification)
            }
        })
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.clipToPadding = true
        recyclerView.clipChildren = true
        recyclerView.isNestedScrollingEnabled = true
        // Extra scroll room after the last item so it is fully visible
        val bottomSpacePx = (160 * resources.displayMetrics.density).toInt()
        recyclerView.addItemDecoration(LastItemBottomSpaceDecoration(bottomSpacePx))
        
        // Header shows only new-count + filter (no "اعلان‌ها" label)
        updateTitle()
        titleFilterValue.text = ALL_TITLES_FILTER
        titleFilterRow.setOnClickListener { showTitleFilterSheet() }
        
        // Register with NotificationManager
        NotificationManager.addListener(this)
        
        // Load existing notifications
        setNotifications(NotificationManager.getAllNotifications())
        
        // Show empty state initially
        updateEmptyState()
    }

    /**
     * Adds empty space after the last list item so it can scroll fully into view.
     */
    private class LastItemBottomSpaceDecoration(
        private val bottomSpacePx: Int,
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State,
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return
            if (position == state.itemCount - 1) {
                outRect.bottom = bottomSpacePx
            }
        }
    }

    fun setTitle(title: String) {
        titleText.text = title
    }
    
    /**
     * Set type filter for notifications
     * If type is null or empty, all notifications will be shown
     * If type is set, only notifications with matching rawType will be shown
     */
    fun setTypeFilter(type: String?) {
        this.typeFilter = if (type.isNullOrEmpty()) null else type
        android.util.Log.d("NotificationListView", "Type filter set to: $typeFilter")
        // Refresh notifications with new filter
        setNotifications(NotificationManager.getAllNotifications())
    }

    /**
     * Set title filter for notifications.
     * Empty / "همه" / "all" => show all titles.
     */
    fun setTitleFilter(title: String?) {
        val normalized = title?.trim().orEmpty()
        this.titleFilter = when {
            normalized.isEmpty() -> null
            normalized.equals(ALL_TITLES_FILTER, ignoreCase = true) -> null
            normalized.equals("all", ignoreCase = true) -> null
            else -> normalized
        }
        titleFilterValue.text = this.titleFilter ?: ALL_TITLES_FILTER
        android.util.Log.d("NotificationListView", "Title filter set to: ${this.titleFilter}")
        setNotifications(NotificationManager.getAllNotifications())
    }

    fun setNotifications(notifications: List<NotificationItem>) {
        var filteredNotifications = notifications
        if (typeFilter != null) {
            filteredNotifications = filteredNotifications.filter { it.rawType == typeFilter }
        }
        if (titleFilter != null) {
            filteredNotifications = filteredNotifications.filter { it.title == titleFilter }
        }
        android.util.Log.d(
            "NotificationListView",
            "Setting notifications - Total: ${notifications.size}, Filtered: ${filteredNotifications.size}, " +
                "Type filter: $typeFilter, Title filter: $titleFilter",
        )
        
        // Ensure listeners are set before updating notifications
        // This is important when view is reattached after navigation
        if (onNotificationClickListener == null || onMarkAsReadListener == null) {
            android.util.Log.w("NotificationListView", "Listeners are null before setNotifications, re-setting them")
            setupListeners()
        }
        
        android.util.Log.d("NotificationListView", "Setting notifications to adapter (adapter will use current listeners)")
        
        adapter.setNotifications(filteredNotifications)
        updateEmptyState()
        updateTitle()
    }

    private fun showTitleFilterSheet() {
        val allTitles = linkedSetOf(ALL_TITLES_FILTER)
        NotificationManager.getAllNotifications()
            .map { it.title.trim() }
            .filter { it.isNotEmpty() }
            .forEach { allTitles.add(it) }

        val dialog = BottomSheetDialog(context)
        val root = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutDirection = LAYOUT_DIRECTION_RTL
            setPadding(dp(16), dp(12), dp(16), dp(20))
            setBackgroundColor(Color.parseColor("#F3F5F9"))
        }

        val searchInput = EditText(context).apply {
            hint = "جستجو..."
            setHintTextColor(Color.parseColor("#9CA3AF"))
            setTextColor(Color.parseColor("#1F2937"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            background = context.getDrawable(R.drawable.notification_filter_search_background)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(12)
            }
            maxLines = 1
            isSingleLine = true
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
        }
        root.addView(searchInput)

        val emptySearchText = TextView(context).apply {
            text = "موردی یافت نشد"
            setTextColor(Color.parseColor("#6B7280"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            gravity = Gravity.CENTER
            visibility = GONE
            setPadding(0, dp(24), 0, dp(24))
        }

        val optionsContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        val scrollView = ScrollView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(360))
            isFillViewport = true
            addView(optionsContainer)
        }

        fun renderOptions(query: String) {
            optionsContainer.removeAllViews()
            val filtered = allTitles.filter { title ->
                query.isBlank() || title.contains(query, ignoreCase = true)
            }
            emptySearchText.isVisible = filtered.isEmpty()
            scrollView.isVisible = filtered.isNotEmpty()
            filtered.forEach { option ->
                optionsContainer.addView(
                    createTitleOptionView(option) {
                        setTitleFilter(option)
                        dialog.dismiss()
                    },
                )
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                renderOptions(s?.toString().orEmpty().trim())
            }
        })

        root.addView(emptySearchText)
        root.addView(scrollView)
        renderOptions("")

        dialog.setContentView(root)
        dialog.show()
    }

    private fun createTitleOptionView(label: String, onClick: () -> Unit): TextView {
        val selected = (titleFilter == null && label == ALL_TITLES_FILTER) || titleFilter == label
        return TextView(context).apply {
            text = label
            setTextColor(if (selected) Color.parseColor("#193D8D") else Color.parseColor("#1F2937"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            paint.isFakeBoldText = selected
            background = GradientDrawable().apply {
                setColor(if (selected) Color.parseColor("#E8ECF4") else Color.WHITE)
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), if (selected) Color.parseColor("#193D8D") else Color.parseColor("#D5DCEA"))
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics,
        ).toInt()
    }

    fun addNotification(notification: NotificationItem) {
        adapter.addNotification(notification)
        updateEmptyState()
        updateTitle()
    }

    fun addNotification(
        title: String,
        message: String,
        type: NotificationType = NotificationType.INFO,
        data: Map<String, String>? = null
    ) {
        val notification = NotificationItem(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            timestamp = Date(),
            isRead = false,
            isNew = true,
            type = type,
            data = data
        )
        addNotification(notification)
    }

    fun markAsRead(notificationId: String) {
        adapter.markAsRead(notificationId)
        updateTitle()
    }

    fun markAllAsRead() {
        // Implementation would depend on your data source
        // For now, we'll just update the title
        updateTitle()
    }

    fun getNewNotificationsCount(): Int {
        return adapter.getNewNotificationsCount()
    }

    fun setOnNotificationClickListener(listener: (NotificationItem) -> Unit) {
        this.onNotificationClickListener = listener
        android.util.Log.d("NotificationListView", "setOnNotificationClickListener called")
    }

    fun setOnMarkAsReadListener(listener: (NotificationItem) -> Unit) {
        this.onMarkAsReadListener = listener
        android.util.Log.d("NotificationListView", "setOnMarkAsReadListener called")
    }
    
    /**
     * Configure listeners with LoadScreenListener and MehdiViewModel
     * This allows listeners to be recreated in onAttachedToWindow
     */
    fun configureListeners(
        loadScreenListener: com.yandex.divkit.demo.ui.LoadScreenListener?,
        mehdiViewModel: com.yandex.divkit.demo.ui.activity.MehdiViewModel?
    ) {
        this.loadScreenListener = loadScreenListener
        this.mehdiViewModel = mehdiViewModel
        setupListeners()
        loadNotificationBasicInfo()
    }
    
    /**
     * Setup listeners using stored references
     * Adapter will automatically use these through the NotificationClickListener interface
     */
    private fun setupListeners() {
        android.util.Log.d("NotificationListView", "=== setupListeners called ===")
        val currentLoadScreenListener = this.loadScreenListener
        val currentMehdiViewModel = this.mehdiViewModel
        
        android.util.Log.d("NotificationListView", "setupListeners - loadScreenListener: ${currentLoadScreenListener != null}, mehdiViewModel: ${currentMehdiViewModel != null}")
        
        val itemClickCallback: (NotificationItem) -> Unit = { notification ->
            android.util.Log.d(
                "NotificationListView",
                "itemClickCallback invoked for: ${notification.title}"
            )

            // Show notification body in dialog
            showNotificationDialog(notification)

            // Handle notification click - you can load specific screens based on notification data
            notification.data?.get("screen")?.let { screenName ->
                android.util.Log.d("NotificationListView", "Loading screen: $screenName")
                currentLoadScreenListener?.onLoad(screenName)
            }
        }
        
        val markAsReadCallback: (NotificationItem) -> Unit = { notification ->
            android.util.Log.d(
                "NotificationListView",
                "markAsReadCallback invoked for: ${notification.id}"
            )

            // Mark as read in NotificationManager (this will update all views)
            com.yandex.divkit.demo.div.notificationList.NotificationManager.markAsRead(notification.id)

            // Save read status to database
            currentMehdiViewModel?.insertItemToDb(
                com.yandex.divkit.demo.data.entities.PhPlusDB(
                    null,
                    "notification_read_${notification.id}",
                    "true"
                )
            )
        }
        
        // Store the callbacks - adapter will access these through its interface
        setOnNotificationClickListener(itemClickCallback)
        setOnMarkAsReadListener(markAsReadCallback)
        
        android.util.Log.d("NotificationListView", "Listeners stored: onNotificationClickListener=${onNotificationClickListener != null}, onMarkAsReadListener=${onMarkAsReadListener != null}")
        android.util.Log.d("NotificationListView", "=== setupListeners completed ===")
    }
    
    /**
     * Check if listeners are set, and log if they are null
     */
    fun checkListeners() {
        android.util.Log.d("NotificationListView", "checkListeners - onNotificationClickListener: ${onNotificationClickListener != null}, onMarkAsReadListener: ${onMarkAsReadListener != null}")
    }

    private fun updateEmptyState() {
        if (adapter.itemCount == 0) {
            recyclerView.visibility = GONE
            emptyStateText.visibility = VISIBLE
            emptyStateText.text = if (titleFilter != null) {
                "اعلانی با عنوان «$titleFilter» وجود ندارد"
            } else {
                "هیچ اعلانی وجود ندارد"
            }
        } else {
            recyclerView.visibility = VISIBLE
            emptyStateText.visibility = GONE
        }
    }

    private fun updateTitle() {
        val newCount = getNewNotificationsCount()
        if (newCount > 0) {
            titleText.text = "$newCount جدید"
            titleText.visibility = VISIBLE
        } else {
            titleText.text = ""
            titleText.visibility = GONE
        }
    }

    // Helper method to create sample notifications for testing
    fun addSampleNotifications() {
        val sampleNotifications = listOf(
            NotificationItem(
                id = "1",
                title = "اعلان جدید",
                message = "شما یک پیام جدید دریافت کرده‌اید",
                timestamp = Date(System.currentTimeMillis() - 300000), // 5 minutes ago
                isRead = false,
                isNew = true,
                type = NotificationType.INFO
            ),
            NotificationItem(
                id = "2", 
                title = "بروزرسانی سیستم",
                message = "نسخه جدید نرم‌افزار در دسترس است",
                timestamp = Date(System.currentTimeMillis() - 3600000), // 1 hour ago
                isRead = true,
                isNew = false,
                type = NotificationType.SUCCESS
            ),
            NotificationItem(
                id = "3",
                title = "هشدار امنیتی",
                message = "تلاش برای ورود مشکوک شناسایی شد",
                timestamp = Date(System.currentTimeMillis() - 7200000), // 2 hours ago
                isRead = false,
                isNew = true,
                type = NotificationType.WARNING
            )
        )
        
        setNotifications(sampleNotifications)
    }

    // NotificationManager.NotificationListener implementation
    override fun onNotificationAdded(notification: NotificationItem) {
        // Refresh the list when new notification is added
        post {
            setNotifications(NotificationManager.getAllNotifications())
        }
    }

    override fun onNotificationRead(notificationId: String) {
        // Update the specific notification when marked as read
        post {
            adapter.markAsRead(notificationId)
            updateTitle()
        }
    }

    override fun onNotificationsChanged(notifications: List<NotificationItem>) {
        // Refresh the entire list when notifications change
        post {
            setNotifications(notifications)
        }
    }

    /**
     * Handle notification click.
     * Previously this showed an AlertDialog; per new requirements we only:
     *  - mark the notification as read (via callback)
     *  - trigger the associated service call (if serviceData exists)
     * No dialog UI is shown anymore.
     */
    fun showNotificationDialog(notification: NotificationItem) {
        android.util.Log.d("NotificationListView", "=== showNotificationDialog called ===")
        android.util.Log.d("NotificationListView", "Notification ID: ${notification.id}")
        android.util.Log.d("NotificationListView", "Notification title: ${notification.title}")
        android.util.Log.d("NotificationListView", "onMarkAsReadListener: ${onMarkAsReadListener != null}")
        android.util.Log.d("NotificationListView", "onNotificationClickListener: ${onNotificationClickListener != null}")
        android.util.Log.d("NotificationListView", "ServiceData: ${notification.serviceData}")
        
        // Mark as read immediately on click
        if (!notification.isRead) {
            android.util.Log.d("NotificationListView", "Notification is not read, calling onMarkAsReadListener")
            onMarkAsReadListener?.invoke(notification)
        } else {
            android.util.Log.d("NotificationListView", "Notification is already read")
        }

        // Call service when notification is clicked (if serviceData exists)
        android.util.Log.d("NotificationListView", "Calling NotificationManager.callServiceForNotification")
        try {
        NotificationManager.callServiceForNotification(notification)
            android.util.Log.d("NotificationListView", "NotificationManager.callServiceForNotification completed")
        } catch (e: Exception) {
            android.util.Log.e("NotificationListView", "Error calling callServiceForNotification", e)
        }
        android.util.Log.d("NotificationListView", "=== showNotificationDialog completed ===")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        android.util.Log.d("NotificationListView", "=== onAttachedToWindow called ===")
        // Register listener when view is attached (in case it was unregistered when detached)
        // Note: This is safe even if already registered - NotificationManager handles duplicates
        NotificationManager.addListener(this)
        // Reload notifications from storage in case new ones were added while view was detached
        NotificationManager.reloadNotificationsFromStorage()
        
        // Get loadScreenListener and mehdiViewModel from NotificationManager if they are null
        if (loadScreenListener == null) {
            loadScreenListener = NotificationManager.getLoadScreenListener()
            android.util.Log.d("NotificationListView", "Got loadScreenListener from NotificationManager: ${loadScreenListener != null}")
        }
        if (mehdiViewModel == null) {
            mehdiViewModel = NotificationManager.getMehdiViewModel()
            android.util.Log.d("NotificationListView", "Got mehdiViewModel from NotificationManager: ${mehdiViewModel != null}")
        }
        
        // Check and update actionHandler in NotificationManager
        val currentActionHandler = NotificationManager.getActionHandler()
        if (currentActionHandler == null) {
            android.util.Log.w("NotificationListView", "ActionHandler is null in NotificationManager, trying to get it from context")
            // Try to get actionHandler from context (MehdiActivity)
            try {
                val activity = context as? android.app.Activity
                if (activity is com.yandex.divkit.demo.ui.activity.MehdiActivity) {
                    val actionHandlerFromActivity = activity.getActionHandler()
                    if (actionHandlerFromActivity != null) {
                        NotificationManager.setActionHandler(actionHandlerFromActivity)
                        android.util.Log.d("NotificationListView", "ActionHandler updated from MehdiActivity in onAttachedToWindow")
                    } else {
                        android.util.Log.w("NotificationListView", "ActionHandler from MehdiActivity is null")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationListView", "Error trying to get actionHandler from context", e)
            }
        } else {
            android.util.Log.d("NotificationListView", "ActionHandler is available in NotificationManager")
        }
        
        // Always re-setup listeners when view is attached to ensure they are properly configured
        // This is important because listeners might be lost when view is detached and reattached
        android.util.Log.d("NotificationListView", "Re-setting listeners in onAttachedToWindow")
        android.util.Log.d("NotificationListView", "loadScreenListener: ${loadScreenListener != null}, mehdiViewModel: ${mehdiViewModel != null}")
        setupListeners()
        
        // Reload notifications AFTER setting up listeners to ensure adapter has fresh listeners
        setNotifications(NotificationManager.getAllNotifications())
        android.util.Log.d("NotificationListView", "View attached - reloaded ${NotificationManager.getAllNotifications().size} notifications")
        
        // Check if listeners are still set
        checkListeners()
        android.util.Log.d("NotificationListView", "=== onAttachedToWindow completed ===")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Unregister listener to prevent memory leaks
        NotificationManager.removeListener(this)
    }
    
    /**
     * Load notification basic info from database and set it to adapter
     * This is used to display notification type labels
     */
    private fun loadNotificationBasicInfo() {
        try {
            val vm = mehdiViewModel
            if (vm != null) {
                val basicInfoJson = vm.getValueByKey("notificationBasicInfo")?.value
                if (basicInfoJson != null && basicInfoJson.isNotEmpty()) {
                    android.util.Log.d("NotificationListView", "Loading notificationBasicInfo from database")
                    // Parse JSON to list of NotificationTypeInfo
                    val gson = com.google.gson.Gson()
                    val listType = object : com.google.gson.reflect.TypeToken<List<NotificationListAdapter.NotificationTypeInfo>>() {}.type
                    val basicInfo = gson.fromJson<List<NotificationListAdapter.NotificationTypeInfo>>(basicInfoJson, listType)
                    adapter.setNotificationBasicInfo(basicInfo)
                    android.util.Log.d("NotificationListView", "Loaded ${basicInfo.size} notification types")
                } else {
                    android.util.Log.d("NotificationListView", "notificationBasicInfo not found in database")
                }
            } else {
                android.util.Log.w("NotificationListView", "MehdiViewModel is null, cannot load notificationBasicInfo")
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationListView", "Error loading notificationBasicInfo", e)
        }
    }
}
