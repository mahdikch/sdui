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

class NotificationListAdapter(
    private val onItemClick: (NotificationItem) -> Unit = {},
    private val onMarkAsRead: (NotificationItem) -> Unit = {}
) : RecyclerView.Adapter<NotificationListAdapter.NotificationViewHolder>() {

    private val notifications = mutableListOf<NotificationItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    fun setNotifications(newNotifications: List<NotificationItem>) {
        notifications.clear()
        notifications.addAll(newNotifications.sortedByDescending { it.timestamp })
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
        private val notificationIcon: ImageView = itemView.findViewById(R.id.notification_icon)
        private val newBadge: View = itemView.findViewById(R.id.new_badge)
        private val readIndicator: View = itemView.findViewById(R.id.read_indicator)

        fun bind(notification: NotificationItem) {
            titleText.text = notification.title
            messageText.text = notification.message
            timestampText.text = formatTimestamp(notification.timestamp)

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

            // Set click listener
            itemView.setOnClickListener {
                if (!notification.isRead) {
                    onMarkAsRead(notification)
                }
                onItemClick(notification)
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

