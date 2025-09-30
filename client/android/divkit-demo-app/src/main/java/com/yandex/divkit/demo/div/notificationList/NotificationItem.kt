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
    val serviceData: Map<String, String>? = null // For service call when clicked
)

enum class NotificationType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}

