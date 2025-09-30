package com.yandex.divkit.demo.div.notificationList

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yandex.divkit.demo.R
import java.util.Date
import java.util.UUID

class NotificationListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), NotificationManager.NotificationListener {

    private val titleText: TextView
    private val recyclerView: RecyclerView
    private val emptyStateText: TextView
    private val adapter: NotificationListAdapter
    
    private var onNotificationClickListener: ((NotificationItem) -> Unit)? = null
    private var onMarkAsReadListener: ((NotificationItem) -> Unit)? = null

    init {
        orientation = VERTICAL
        
        // Inflate the layout
        val view = LayoutInflater.from(context)
            .inflate(R.layout.notification_list_layout, this, true)
        
        titleText = view.findViewById(R.id.notification_list_title)
        recyclerView = view.findViewById(R.id.notifications_recycler_view)
        emptyStateText = view.findViewById(R.id.empty_state_text)
        
        // Setup RecyclerView
        adapter = NotificationListAdapter(
            onItemClick = { notification ->
                onNotificationClickListener?.invoke(notification)
            },
            onMarkAsRead = { notification ->
                onMarkAsReadListener?.invoke(notification)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        // Set default title
        setTitle("اعلان‌ها")
        
        // Register with NotificationManager
        NotificationManager.addListener(this)
        
        // Load existing notifications
        setNotifications(NotificationManager.getAllNotifications())
        
        // Show empty state initially
        updateEmptyState()
    }

    fun setTitle(title: String) {
        titleText.text = title
    }

    fun setNotifications(notifications: List<NotificationItem>) {
        adapter.setNotifications(notifications)
        updateEmptyState()
        updateTitle()
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
    }

    fun setOnMarkAsReadListener(listener: (NotificationItem) -> Unit) {
        this.onMarkAsReadListener = listener
    }

    private fun updateEmptyState() {
        if (adapter.itemCount == 0) {
            recyclerView.visibility = GONE
            emptyStateText.visibility = VISIBLE
        } else {
            recyclerView.visibility = VISIBLE
            emptyStateText.visibility = GONE
        }
    }

    private fun updateTitle() {
        val newCount = getNewNotificationsCount()
        val baseTitle = "اعلان‌ها"
        
        titleText.text = if (newCount > 0) {
            "$baseTitle ($newCount جدید)"
        } else {
            baseTitle
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

    fun showNotificationDialog(notification: NotificationItem) {
        AlertDialog.Builder(context)
            .setTitle(notification.title)
            .setMessage(notification.message)
            .setPositiveButton("بستن") { dialog, _ ->
                dialog.dismiss()
                // Mark as read when dialog is closed
                if (!notification.isRead) {
                    onMarkAsReadListener?.invoke(notification)
                }
            }
            .setCancelable(true)
            .show()
            
        // Call service when notification is clicked (if serviceData exists)
        NotificationManager.callServiceForNotification(notification)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Unregister listener to prevent memory leaks
        NotificationManager.removeListener(this)
    }
}
