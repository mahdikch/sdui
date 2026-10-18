package com.yandex.divkit.demo.div.notificationList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yandex.divkit.demo.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationListAdapter : RecyclerView.Adapter<NotificationListAdapter.NotificationViewHolder>() {

    private val notifications = mutableListOf<NotificationItem>()
    private var notificationBasicInfo: List<NotificationTypeInfo>? = null
    
    // Use a listener interface instead of lambdas to avoid capture issues
    interface NotificationClickListener {
        fun onNotificationClick(notification: NotificationItem)
        fun onMarkAsRead(notification: NotificationItem)
    }
    
    private var clickListener: NotificationClickListener? = null
    
    /**
     * Set click listener
     * This should be called when the view is attached/reattached
     */
    fun setClickListener(listener: NotificationClickListener?) {
        this.clickListener = listener
        android.util.Log.d("NotificationListAdapter", "Click listener set: ${listener != null}")
    }
    
    data class NotificationTypeInfo(
        val id: String,
        val titleFa: String,
        val titleEn: String
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        android.util.Log.d("NotificationListAdapter", "onBindViewHolder called for position $position")
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    fun setNotifications(newNotifications: List<NotificationItem>) {
        android.util.Log.d("NotificationListAdapter", "setNotifications called with ${newNotifications.size} items")
        android.util.Log.d("NotificationListAdapter", "Current clickListener: ${clickListener != null}")
        notifications.clear()
        notifications.addAll(newNotifications.sortedByDescending { it.timestamp })
        notifyDataSetChanged()
    }
    
    /**
     * Set notification basic info from database
     * This is used to display notification type labels
     */
    fun setNotificationBasicInfo(basicInfo: List<NotificationTypeInfo>?) {
        this.notificationBasicInfo = basicInfo
        notifyDataSetChanged()
    }

    fun addNotification(notification: NotificationItem) {
        notifications.add(0, notification) // Add to top
        notifyItemInserted(0)
    }

    fun markAsRead(notificationId: String) {
        val index = notifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            val notification = notifications[index]
            notifications[index] = notification.copy(isRead = true, isNew = false)
            notifyItemChanged(index)
        }
    }

    fun getNewNotificationsCount(): Int {
        return notifications.count { it.isNew && !it.isRead }
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.notification_title)
        private val messageText: TextView = itemView.findViewById(R.id.notification_message)
        private val timestampText: TextView = itemView.findViewById(R.id.notification_timestamp)
        private val senderTimeTypeText: TextView = itemView.findViewById(R.id.notification_sender_time_type)
        private val notificationIcon: ImageView = itemView.findViewById(R.id.notification_icon)
        private val newBadge: View = itemView.findViewById(R.id.new_badge)
        private val readIndicator: View = itemView.findViewById(R.id.read_indicator)

        fun bind(notification: NotificationItem) {
            titleText.text = notification.title
            messageText.text = notification.message

            // Set relative time (how long ago notification was received)
            timestampText.text = formatTimestamp(notification.timestamp)

            // Build senderTime and type text (shown on separate line above)
            val senderTimeTypeParts = mutableListOf<String>()
            
            // Add senderTime from server if available
            if (notification.senderTime != null) {
                senderTimeTypeParts.add(notification.senderTime)
            }
            
            // Add type label if available
            if (notification.rawType != null) {
                // Try to find type label from notificationBasicInfo
                val typeLabel = notificationBasicInfo?.find { it.id == notification.rawType }?.titleFa
                    ?: notification.rawType
                senderTimeTypeParts.add(typeLabel)
            }
            
            // Show/hide senderTime and type TextView
            if (senderTimeTypeParts.isNotEmpty()) {
                senderTimeTypeText.text = senderTimeTypeParts.joinToString("  •  ")
                senderTimeTypeText.visibility = View.VISIBLE
            } else {
                senderTimeTypeText.visibility = View.GONE
            }

            // Show/hide new badge
            newBadge.visibility = if (notification.isNew && !notification.isRead) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Show/hide read indicator
            readIndicator.visibility = if (!notification.isRead) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Set notification icon based on type
            when (notification.type) {
                NotificationType.INFO -> {
                    notificationIcon.setImageResource(R.drawable.ic_notification)
                    notificationIcon.setBackgroundResource(R.drawable.notification_icon_background)
                }
                NotificationType.WARNING -> {
                    notificationIcon.setImageResource(R.drawable.ic_notification)
                    // You can add different icons/backgrounds for different types
                }
                NotificationType.ERROR -> {
                    notificationIcon.setImageResource(R.drawable.ic_notification)
                }
                NotificationType.SUCCESS -> {
                    notificationIcon.setImageResource(R.drawable.ic_notification)
                }
            }

            // Set click listener - always set it fresh to ensure it works
            itemView.setOnClickListener(null) // Clear previous listener first
            itemView.setOnClickListener {
                android.util.Log.d("NotificationListAdapter", "Item clicked: ${notification.id}, title: ${notification.title}")
                android.util.Log.d("NotificationListAdapter", "clickListener is null: ${clickListener == null}")
                
                if (clickListener != null) {
                if (!notification.isRead) {
                        android.util.Log.d("NotificationListAdapter", "Notification is not read, calling onMarkAsRead")
                        clickListener!!.onMarkAsRead(notification)
                }
                    android.util.Log.d("NotificationListAdapter", "Calling onNotificationClick")
                    clickListener!!.onNotificationClick(notification)
                } else {
                    android.util.Log.e("NotificationListAdapter", "clickListener is null, cannot handle click!")
                }
            }
        }

        private fun formatTimestamp(timestamp: Date): String {
            val now = Date()
            val diffInMillis = now.time - timestamp.time

            return when {
                diffInMillis < TimeUnit.MINUTES.toMillis(1) -> "الان"
                diffInMillis < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                    "$minutes دقیقه پیش"
                }
                diffInMillis < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
                    "$hours ساعت پیش"
                }
                diffInMillis < TimeUnit.DAYS.toMillis(7) -> {
                    val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                    "$days روز پیش"
                }
                else -> {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(timestamp)
                }
            }
        }
    }
}

