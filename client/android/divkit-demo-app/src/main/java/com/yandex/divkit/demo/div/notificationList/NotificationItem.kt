package com.yandex.divkit.demo.div.notificationList

import java.util.Date

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Date,
    val isRead: Boolean = false,
    val isNew: Boolean = true,
    val type: NotificationType = NotificationType.INFO,
    val data: Map<String, String>? = null,
    val serviceData: Map<String, String>? = null, // For service call when clicked
    val senderTime: String? = null,               // Server-provided display time string
    val rawType: String? = null,                  // Raw type code from server, e.g. "0" (normal), "1" (urgent)
    val messageId: String? = null,                // Push message ID for seenMessage
    val topic: String? = null                     // Push topic for seenMessage
)

enum class NotificationType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}

