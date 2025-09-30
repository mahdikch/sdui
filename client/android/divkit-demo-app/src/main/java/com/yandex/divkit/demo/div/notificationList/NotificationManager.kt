package com.yandex.divkit.demo.div.notificationList

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import com.yandex.divkit.demo.ui.UIDiv2ActionHandler
import java.util.Date
import java.util.HashMap
import java.util.UUID

object NotificationManager {
    private const val TAG = "NotificationManager"
    private const val NOTIFICATIONS_DB_KEY = "app_notifications"
    
    // Static list to hold notifications across the app
    private val notifications = mutableListOf<NotificationItem>()
    private val listeners = mutableListOf<NotificationListener>()
    private var mehdiViewModel: MehdiViewModel? = null
    private var actionHandler: UIDiv2ActionHandler? = null
    
    interface NotificationListener {
        fun onNotificationAdded(notification: NotificationItem)
        fun onNotificationRead(notificationId: String)
        fun onNotificationsChanged(notifications: List<NotificationItem>)
    }
    
    fun initialize(viewModel: MehdiViewModel, actionHandler: UIDiv2ActionHandler?) {
        this.mehdiViewModel = viewModel
        this.actionHandler = actionHandler
        loadNotificationsFromDatabase()
    }
    
    fun setActionHandler(actionHandler: UIDiv2ActionHandler) {
        this.actionHandler = actionHandler
    }
    
    fun addListener(listener: NotificationListener) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: NotificationListener) {
        listeners.remove(listener)
    }
    
    fun getAllNotifications(): List<NotificationItem> {
        return notifications.sortedByDescending { it.timestamp }
    }
    
    fun getUnreadNotifications(): List<NotificationItem> {
        return notifications.filter { !it.isRead }.sortedByDescending { it.timestamp }
    }
    
    fun getNewNotificationsCount(): Int {
        return notifications.count { it.isNew && !it.isRead }
    }
    
    fun addNotification(notification: NotificationItem) {
        notifications.add(0, notification)
        saveNotificationsToDatabase()
        listeners.forEach { it.onNotificationAdded(notification) }
        listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
    }
    
    fun addNotification(
        title: String,
        message: String,
        type: NotificationType = NotificationType.INFO,
        data: Map<String, String>? = null
    ): NotificationItem {
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
        return notification
    }
    
    fun markAsRead(notificationId: String) {
        val index = notifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            notifications[index] = notifications[index].copy(isRead = true, isNew = false)
            saveNotificationsToDatabase()
            listeners.forEach { it.onNotificationRead(notificationId) }
            listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
        }
    }
    
    fun markAllAsRead() {
        notifications.forEachIndexed { index, notification ->
            if (!notification.isRead) {
                notifications[index] = notification.copy(isRead = true, isNew = false)
            }
        }
        saveNotificationsToDatabase()
        listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
    }
    
    fun clearAllNotifications() {
        notifications.clear()
        saveNotificationsToDatabase()
        listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
    }
    
    /**
     * Parse push message and create notification
     * Expected format: {"title": "...", "body": {"text": "...", "senderTitle": "...", "serviceData": {...}, "actions": [...]}}
     */
    fun createNotificationFromPushMessage(pushMessage: String): NotificationItem? {
        return try {
            val pushData = Gson().fromJson(pushMessage, PushNotificationData::class.java)
            
            // Extract data from nested body structure
            val body = pushData.body
            if (body == null) {
                android.util.Log.e(TAG, "Push message body is null")
                return null
            }
            
            val title = body.senderTitle ?: "اعلان جدید"
            val message = body.text ?: pushMessage
            
            android.util.Log.d(TAG, "Parsed push notification:")
            android.util.Log.d(TAG, "Title: $title")
            android.util.Log.d(TAG, "Message: $message")
            android.util.Log.d(TAG, "ServiceData: ${body.serviceData}")
            android.util.Log.d(TAG, "Actions: ${body.actions}")
            
            // Execute actions immediately when push arrives
            body.actions?.let { actions ->
                executeActions(actions)
            }
            
            val notification = NotificationItem(
                id = UUID.randomUUID().toString(),
                title = title,
                message = message,
                timestamp = Date(),
                isRead = false,
                isNew = true,
                type = NotificationType.INFO,
                data = null,
                serviceData = body.serviceData // Store for later service call
            )
            
            addNotification(notification)
            notification
        } catch (e: JsonSyntaxException) {
            android.util.Log.e(TAG, "Failed to parse push message: $pushMessage", e)
            
            // Fallback: create simple notification with raw message
            addNotification(
                title = "اعلان جدید",
                message = pushMessage,
                type = NotificationType.INFO
            )
        }
    }
    
    /**
     * Execute actions immediately when push notification arrives
     */
    private fun executeActions(actions: List<ActionItem>) {
        try {
            android.util.Log.d(TAG, "Executing ${actions.size} actions from push notification")
            
            val currentActionHandler = actionHandler
            if (currentActionHandler == null) {
                android.util.Log.e(TAG, "ActionHandler is null! Cannot execute actions.")
                return
            }
            
            actions.forEach { action ->
                try {
                    android.util.Log.d(TAG, "Executing action: ${action.log_id} -> ${action.url}")
                    val result = currentActionHandler.handleActionFromString(action.url)
                    android.util.Log.d(TAG, "Action result: $result")
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error executing action: ${action.log_id}", e)
                }
            }
            
            android.util.Log.d(TAG, "All actions executed successfully")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error executing actions", e)
        }
    }
    
    /**
     * Call service with serviceData when user clicks notification
     */
    fun callServiceForNotification(notification: NotificationItem) {
        try {
            notification.serviceData?.let { serviceData ->
                android.util.Log.d(TAG, "Calling service for notification: ${notification.id}")
                android.util.Log.d(TAG, "Service data: $serviceData")
                
                val currentMehdiViewModel = mehdiViewModel
                if (currentMehdiViewModel == null) {
                    android.util.Log.e(TAG, "MehdiViewModel is null! Cannot call service.")
                    return
                }
                
                // Call setphPlusRequest with serviceData
                val phId = serviceData["path"] ?: "default"
                val requestMap = HashMap<String, String>()
                serviceData.forEach { (key, value) ->
                    requestMap[key] = value
                }
                
                android.util.Log.d(TAG, "Calling setphPlusRequest with phId: $phId, requestMap: $requestMap")
                currentMehdiViewModel.setphPlusRequest(phId, requestMap)
                android.util.Log.d(TAG, "Service call initiated successfully")
            } ?: run {
                android.util.Log.d(TAG, "No service data for notification: ${notification.id}")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error calling service for notification", e)
        }
    }
    
    private fun saveNotificationsToDatabase() {
        try {
            val notificationsJson = Gson().toJson(notifications)
            mehdiViewModel?.insertItemToDb(
                PhPlusDB(null, NOTIFICATIONS_DB_KEY, notificationsJson)
            )
            android.util.Log.d(TAG, "Notifications saved to database")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error saving notifications to database", e)
        }
    }
    
    private fun loadNotificationsFromDatabase() {
        try {
            val savedData = mehdiViewModel?.getValueByKey(NOTIFICATIONS_DB_KEY)
            if (savedData?.value != null && savedData.value.isNotEmpty()) {
                val type = object : TypeToken<List<NotificationItem>>() {}.type
                val savedNotifications: List<NotificationItem> = Gson().fromJson(savedData.value, type)
                
                notifications.clear()
                notifications.addAll(savedNotifications)
                
                android.util.Log.d(TAG, "Loaded ${notifications.size} notifications from database")
                listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
            } else {
                android.util.Log.d(TAG, "No saved notifications found in database")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error loading notifications from database", e)
        }
    }
    
    private data class PushNotificationData(
        val title: String?,                   // Root level title
        val body: PushNotificationBody?       // Nested body containing actual data
    )
    
    private data class PushNotificationBody(
        val text: String?,                    // Subtitle for notification
        val senderTitle: String?,             // Title for notification
        val serviceData: Map<String, String>?, // Service call data
        val actions: List<ActionItem>?        // Actions to execute
    )
    
    private data class ActionItem(
        val log_id: String,
        val url: String
    )
}

