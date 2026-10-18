package com.yandex.divkit.demo.div.notificationList

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import com.yandex.divkit.demo.ui.UIDiv2ActionHandler
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.util.Date
import java.util.HashMap
import java.util.UUID
import android.content.Context
import ir.naja.pushsdk.android.Constant

object NotificationManager {
    private const val TAG = "NotificationManager"
    private const val NOTIFICATIONS_DB_KEY = "app_notifications"
    private const val NOTIFICATIONS_PREFS = "notification_manager_prefs"
    private const val NOTIFICATIONS_PREFS_KEY = "notifications_json"
    private const val CURRENT_USER_PREFS_KEY = "current_notifications_user"
    private const val UNREAD_NOTIFICATIONS_COUNT_KEY = "unread_notifications_count"
    const val RECEIVE_MESSAGE_TIME_KEY = "recieveMessageTime"
    private const val NOTIFICATION_TTL_MS = 24L * 60 * 60 * 1000L
    
    // Static list to hold notifications across the app
    private val notifications = mutableListOf<NotificationItem>()
    private val listeners = mutableListOf<NotificationListener>()
    private var mehdiViewModel: MehdiViewModel? = null
    private var actionHandler: UIDiv2ActionHandler? = null
    private var najiPush: ir.naja.pushsdk.android.NajiPush? = null
    private var loadScreenListener: com.yandex.divkit.demo.ui.LoadScreenListener? = null
    private var pendingBottomSheetFullScreen = false
    /** Username that currently owns the in-memory notification list. */
    @Volatile
    private var currentOwner: String? = null
    
    // Cache for last saved unread counts to avoid reading from DB every time
    private var cachedUnreadCount: Int = 0
    private val cachedUnreadCountByType = mutableMapOf<String, Int>()
    
    interface NotificationListener {
        fun onNotificationAdded(notification: NotificationItem)
        fun onNotificationRead(notificationId: String)
        fun onNotificationsChanged(notifications: List<NotificationItem>)
    }
    
    fun initialize(viewModel: MehdiViewModel, actionHandler: UIDiv2ActionHandler?) {
        this.mehdiViewModel = viewModel
        this.actionHandler = actionHandler
        resolveCurrentOwner()
        loadNotificationsFromStorage()
        // Initialize counters for all notification types
        initializeTypeCounters()
    }
    
    /**
     * Reload notifications from storage.
     * Useful when view is re-attached and needs to refresh the list.
     */
    fun reloadNotificationsFromStorage() {
        resolveCurrentOwner()
        loadNotificationsFromStorage()
    }

    /**
     * Bind the notification list to [username].
     * If the user changes, the previous user's list stays in their storage key
     * and this user's list (or empty) is loaded instead.
     */
    fun onUserChanged(username: String?) {
        val newOwner = username?.trim()?.takeIf { it.isNotEmpty() } ?: return
        if (newOwner == currentOwner) {
            android.util.Log.d(TAG, "onUserChanged: same owner=$newOwner, no switch")
            return
        }
        android.util.Log.d(TAG, "onUserChanged: switching from=$currentOwner to=$newOwner")
        try {
            if (!currentOwner.isNullOrBlank()) {
                saveNotificationsToStorage()
            }
            currentOwner = newOwner
            persistCurrentOwner(newOwner)
            notifications.clear()
            cachedUnreadCount = 0
            cachedUnreadCountByType.clear()
            loadNotificationsFromStorage()
            initializeTypeCounters()
            listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
            android.util.Log.d(TAG, "onUserChanged: loaded ${notifications.size} notifications for $newOwner")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error switching notification owner to $newOwner", e)
        }
    }

    fun getCurrentOwner(): String? = currentOwner

    private fun resolveCurrentOwner() {
        val fromDb = mehdiViewModel?.getValueByKey("userName")?.value?.trim()
        if (fromDb != null) {
            if (fromDb.isNotEmpty())
                Constant.USERNAME=fromDb
        }
        val fromConstant = ir.naja.pushsdk.android.Constant.USERNAME.trim()
        val fromPrefs = readPersistedOwner()
        val resolved = when {
            !fromDb.isNullOrEmpty() -> fromDb
            fromConstant.isNotEmpty() -> fromConstant
            !fromPrefs.isNullOrEmpty() -> fromPrefs
            else -> null
        }
        if (!resolved.isNullOrEmpty() && resolved != currentOwner) {
            currentOwner = resolved
            persistCurrentOwner(resolved)
            android.util.Log.d(TAG, "resolveCurrentOwner: $resolved")
        }
    }

    private fun persistCurrentOwner(owner: String) {
        try {
            val ctx = com.yandex.divkit.demo.DivkitApplication.getInstance()?.applicationContext
                ?: return
            ctx.getSharedPreferences(NOTIFICATIONS_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(CURRENT_USER_PREFS_KEY, owner)
                .apply()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error persisting current notification owner", e)
        }
    }

    private fun readPersistedOwner(): String? {
        return try {
            val ctx = com.yandex.divkit.demo.DivkitApplication.getInstance()?.applicationContext
                ?: return null
            ctx.getSharedPreferences(NOTIFICATIONS_PREFS, Context.MODE_PRIVATE)
                .getString(CURRENT_USER_PREFS_KEY, null)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error reading persisted notification owner", e)
            null
        }
    }

    private fun ownerSuffix(): String {
        val owner = currentOwner?.trim().orEmpty()
        return if (owner.isNotEmpty()) "_$owner" else ""
    }

    private fun notificationsDbKey(): String = NOTIFICATIONS_DB_KEY + ownerSuffix()

    private fun notificationsPrefsKey(): String = NOTIFICATIONS_PREFS_KEY + ownerSuffix()

    /** Fixed DB/UI key — DivKit reads this exact name for the badge. */
    private fun unreadTotalKey(): String = UNREAD_NOTIFICATIONS_COUNT_KEY

    /** Fixed DB/UI key per type — DivKit reads these exact names. */
    private fun unreadTypeKey(typeId: String): String =
        "unread_notifications_count_type_$typeId"
    
    fun setActionHandler(actionHandler: UIDiv2ActionHandler) {
        this.actionHandler = actionHandler
        android.util.Log.d(TAG, "ActionHandler set in NotificationManager: ${actionHandler != null}")
    }
    
    fun getActionHandler(): UIDiv2ActionHandler? {
        return actionHandler
    }
    
    fun setNajiPush(najiPush: ir.naja.pushsdk.android.NajiPush) {
        this.najiPush = najiPush
    }
    
    fun setLoadScreenListener(loadScreenListener: com.yandex.divkit.demo.ui.LoadScreenListener?) {
        this.loadScreenListener = loadScreenListener
        android.util.Log.d(TAG, "LoadScreenListener set in NotificationManager: ${loadScreenListener != null}")
    }
    
    fun getLoadScreenListener(): com.yandex.divkit.demo.ui.LoadScreenListener? {
        return loadScreenListener
    }
    
    fun getMehdiViewModel(): MehdiViewModel? {
        return mehdiViewModel
    }

    /**
     * Returns true once when a notification click requested a full-screen bottom sheet.
     */
    fun consumePendingBottomSheetFullScreen(): Boolean {
        val value = pendingBottomSheetFullScreen
        pendingBottomSheetFullScreen = false
        return value
    }

    /**
     * Saves receive date/time of the clicked notification for use in the form.
     */
    fun saveReceiveMessageTime(notification: NotificationItem) {
        try {
            val formatted = PersianDateFormat("Y/m/d - H:i:s")
                .format(PersianDate(notification.timestamp.time))
            mehdiViewModel?.insertItemToDb(
                PhPlusDB(null, RECEIVE_MESSAGE_TIME_KEY, formatted)
            )
            android.util.Log.d(TAG, "Saved $RECEIVE_MESSAGE_TIME_KEY=$formatted for notification ${notification.id}")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error saving $RECEIVE_MESSAGE_TIME_KEY", e)
        }
    }
    
    fun addListener(listener: NotificationListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
            android.util.Log.d(TAG, "Listener added. Total listeners: ${listeners.size}")
        } else {
            android.util.Log.d(TAG, "Listener already registered, skipping")
        }
    }
    
    fun removeListener(listener: NotificationListener) {
        listeners.remove(listener)
    }
    
    fun getAllNotifications(): List<NotificationItem> {
        pruneExpiredNotifications()
        return notifications.sortedByDescending { it.timestamp }
    }

    /**
     * Drops notifications older than 24 hours from memory and storage.
     */
    private fun pruneExpiredNotifications(notifyListeners: Boolean = false) {
        try {
            val now = System.currentTimeMillis()
            val expired = notifications.filter { now - it.timestamp.time > NOTIFICATION_TTL_MS }
            if (expired.isEmpty()) return

            notifications.removeAll(expired)
            android.util.Log.d(TAG, "Pruned ${expired.size} notification(s) older than 24 hours")
            saveNotificationsToStorage()
            updateUnreadCountInDatabase()
            if (notifyListeners) {
                val remaining = notifications.sortedByDescending { it.timestamp }
                listeners.forEach { it.onNotificationsChanged(remaining) }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error pruning expired notifications", e)
        }
    }
    
    fun getUnreadNotifications(): List<NotificationItem> {
        pruneExpiredNotifications()
        return notifications.filter { !it.isRead }.sortedByDescending { it.timestamp }
    }
    
    fun getUnreadNotificationsCount(): Int {
        pruneExpiredNotifications()
        return notifications.count { !it.isRead }
    }
    
    fun getNewNotificationsCount(): Int {
        pruneExpiredNotifications()
        return notifications.count { it.isNew && !it.isRead }
    }
    
    /**
     * Get unread notifications count for a specific type
     */
    fun getUnreadNotificationsCountByType(typeId: String): Int {
        return notifications.count { !it.isRead && it.rawType == typeId }
    }
    
    /**
     * Get unread notifications count for a specific type from database
     */
    fun getUnreadCountByTypeFromDatabase(typeId: String): Int {
        return try {
            val key = unreadTypeKey(typeId)
            mehdiViewModel?.getValueByKey(key)?.value?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting unread count for type $typeId from database", e)
            0
        }
    }
    
    /**
     * Initialize counters for all notification types from notificationBasicInfo
     * This ensures that all types have a counter in database, even if count is 0
     * Call this method when notificationBasicInfo is updated
     */
    fun initializeTypeCounters() {
        try {
            val vm = mehdiViewModel
            if (vm != null) {
                val basicInfoJson = vm.getValueByKey("notificationBasicInfo")?.value
                if (basicInfoJson != null && basicInfoJson.isNotEmpty()) {
                    android.util.Log.d(TAG, "Initializing type counters from notificationBasicInfo")
                    val gson = com.google.gson.Gson()
                    val listType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
                    val basicInfo = gson.fromJson<List<Map<String, Any>>>(basicInfoJson, listType)
                    
                    // Get current unread counts by type
                    val unreadByType = notifications
                        .filter { !it.isRead }
                        .groupBy { it.rawType ?: "unknown" }
                        .mapValues { it.value.size }
                    
                    android.util.Log.d(TAG, "=== INITIALIZING TYPE COUNTERS ===")
                    android.util.Log.d(TAG, "Total types found: ${basicInfo.size}")
                    android.util.Log.d(TAG, "Current unread by type: $unreadByType")
                    
                    // Initialize counter for each type
                    for (typeInfo in basicInfo) {
                        val typeId = typeInfo["id"]?.toString() ?: continue
                        val typeTitle = typeInfo["titleFa"]?.toString() ?: typeInfo["titleEn"]?.toString() ?: typeId
                        val count = unreadByType[typeId] ?: 0
                        val key = unreadTypeKey(typeId)
                        
                        // Check if counter already exists
                        val existingValue = vm.getValueByKey(key)?.value
                        if (existingValue == null) {
                            // Counter doesn't exist, create it
                            vm.insertItemToDb(PhPlusDB(null, key, count.toString()))
                            // Update cache
                            cachedUnreadCountByType[typeId] = count
                            android.util.Log.d(TAG, "--- Created New Counter ---")
                            android.util.Log.d(TAG, "Type ID: $typeId ($typeTitle)")
                            android.util.Log.d(TAG, "Key: $key")
                            android.util.Log.d(TAG, "Initial value: $count")
                        } else {
                            // Counter exists, update it with current count
                            val previousValue = existingValue.toIntOrNull() ?: 0
                            vm.insertItemToDb(PhPlusDB(null, key, count.toString()))
                            // Update cache
                            cachedUnreadCountByType[typeId] = count
                            android.util.Log.d(TAG, "--- Updated Existing Counter ---")
                            android.util.Log.d(TAG, "Type ID: $typeId ($typeTitle)")
                            android.util.Log.d(TAG, "Key: $key")
                            android.util.Log.d(TAG, "Previous value: $previousValue")
                            android.util.Log.d(TAG, "New value: $count")
                            android.util.Log.d(TAG, "Change: ${if (count >= previousValue) "+" else ""}${count - previousValue}")
                        }
                    }
                    
                    android.util.Log.d(TAG, "=== END INITIALIZING TYPE COUNTERS ===")
                    
                    android.util.Log.d(TAG, "Initialized counters for ${basicInfo.size} notification types")
                } else {
                    android.util.Log.d(TAG, "notificationBasicInfo not found in database, skipping counter initialization")
                }
            } else {
                android.util.Log.w(TAG, "MehdiViewModel is null, cannot initialize type counters")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error initializing type counters", e)
        }
    }
    
    /**
     * Update the unread notifications count in database
     * This should be called whenever notifications are added or marked as read
     * Also updates per-type unread counts
     */
    /**
     * Initialize cache from database values
     * This ensures cache is in sync with database when app starts or notifications are loaded
     */
    private fun initializeCacheFromDatabase() {
        try {
            val vm = mehdiViewModel
            if (vm != null) {
                // Initialize total unread count cache
                val totalCount = vm.getValueByKey(unreadTotalKey())?.value?.toIntOrNull() ?: 0
                cachedUnreadCount = totalCount
                android.util.Log.d(TAG, "Initialized cache - total unread count: $totalCount")
                
                // Initialize per-type unread count cache
                val basicInfoJson = vm.getValueByKey("notificationBasicInfo")?.value
                if (basicInfoJson != null && basicInfoJson.isNotEmpty()) {
                    val gson = com.google.gson.Gson()
                    val listType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
                    val basicInfo = gson.fromJson<List<Map<String, Any>>>(basicInfoJson, listType)
                    
                    for (typeInfo in basicInfo) {
                        val typeId = typeInfo["id"]?.toString() ?: continue
                        val key = unreadTypeKey(typeId)
                        val typeCount = vm.getValueByKey(key)?.value?.toIntOrNull() ?: 0
                        cachedUnreadCountByType[typeId] = typeCount
                        android.util.Log.d(TAG, "Initialized cache - $key: $typeCount")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error initializing cache from database", e)
        }
    }
    
    private fun updateUnreadCountInDatabase() {
        android.util.Log.d(TAG, ">>> updateUnreadCountInDatabase() called")
        try {
            val unreadCount = getUnreadNotificationsCount()
            val vm = mehdiViewModel
            if (vm != null) {
                // Use cached previous value instead of reading from DB
                // This avoids race conditions when notifications arrive quickly
                val previousValue = cachedUnreadCount
                
                android.util.Log.d(TAG, "=== UNREAD NOTIFICATIONS COUNT UPDATE ===")
                android.util.Log.d(TAG, "Key: ${unreadTotalKey()}")
                android.util.Log.d(TAG, "Previous value (from cache): $previousValue")
                android.util.Log.d(TAG, "New value: $unreadCount")
                android.util.Log.d(TAG, "Change: ${if (unreadCount >= previousValue) "+" else ""}${unreadCount - previousValue}")
                
                // Update cache immediately
                cachedUnreadCount = unreadCount
                
                // Update total unread count in database
                vm.insertItemToDb(
                    PhPlusDB(null, unreadTotalKey(), unreadCount.toString())
                )
                
                android.util.Log.d(TAG, "Saved to database: ${unreadTotalKey()} = $unreadCount")
                android.util.Log.d(TAG, "==========================================")
                
                // Update per-type unread counts
                updateUnreadCountByTypeInDatabase()
            } else {
                android.util.Log.w(TAG, "MehdiViewModel is null, cannot update unread count in database")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error updating unread count in database", e)
        }
        android.util.Log.d(TAG, "<<< updateUnreadCountInDatabase() finished")
    }
    
    /**
     * Update unread count for each notification type in database
     * Format: unread_notifications_count_type_{typeId}
     */
    private fun updateUnreadCountByTypeInDatabase() {
        try {
            val vm = mehdiViewModel
            if (vm != null) {
                // Group unread notifications by rawType
                val unreadByType = notifications
                    .filter { !it.isRead }
                    .groupBy { it.rawType ?: "unknown" }
                    .mapValues { it.value.size }
                
                android.util.Log.d(TAG, "=== UPDATING UNREAD COUNTS BY TYPE ===")
                android.util.Log.d(TAG, "Unread notifications by type: $unreadByType")
                
                // Load notification basic info to get all available types
                val basicInfoJson = vm.getValueByKey("notificationBasicInfo")?.value
                if (basicInfoJson != null && basicInfoJson.isNotEmpty()) {
                    val gson = com.google.gson.Gson()
                    val listType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
                    val basicInfo = gson.fromJson<List<Map<String, Any>>>(basicInfoJson, listType)
                    
                    // Update count for each type (even if count is 0)
                    for (typeInfo in basicInfo) {
                        val typeId = typeInfo["id"]?.toString() ?: continue
                        val typeTitle = typeInfo["titleFa"]?.toString() ?: typeInfo["titleEn"]?.toString() ?: typeId
                        val count = unreadByType[typeId] ?: 0
                        val key = unreadTypeKey(typeId)
                        
                        // Use cached previous value instead of reading from DB
                        // This avoids race conditions when notifications arrive quickly
                        val previousValue = cachedUnreadCountByType[typeId] ?: 0
                        
                        // Update cache immediately
                        cachedUnreadCountByType[typeId] = count
                        
                        // Update in database
                        vm.insertItemToDb(
                            PhPlusDB(null, key, count.toString())
                        )
                        
                        // Log the change
                        android.util.Log.d(TAG, "--- Type Counter Update ---")
                        android.util.Log.d(TAG, "Type ID: $typeId ($typeTitle)")
                        android.util.Log.d(TAG, "Key: $key")
                        android.util.Log.d(TAG, "Previous value (from cache): $previousValue")
                        android.util.Log.d(TAG, "New value: $count")
                        android.util.Log.d(TAG, "Change: ${if (count >= previousValue) "+" else ""}${count - previousValue}")
                    }
                } else {
                    // If basicInfo not available, just update counts for existing types
                    android.util.Log.d(TAG, "notificationBasicInfo not found, updating only existing types")
                    for ((typeId, count) in unreadByType) {
                        val key = unreadTypeKey(typeId)
                        
                        // Use cached previous value instead of reading from DB
                        val previousValue = cachedUnreadCountByType[typeId] ?: 0
                        
                        // Update cache immediately
                        cachedUnreadCountByType[typeId] = count
                        
                        // Update in database
                        vm.insertItemToDb(
                            PhPlusDB(null, key, count.toString())
                        )
                        
                        // Log the change
                        android.util.Log.d(TAG, "--- Type Counter Update (no basicInfo) ---")
                        android.util.Log.d(TAG, "Type ID: $typeId")
                        android.util.Log.d(TAG, "Key: $key")
                        android.util.Log.d(TAG, "Previous value (from cache): $previousValue")
                        android.util.Log.d(TAG, "Previous value: $previousValue")
                        android.util.Log.d(TAG, "New value: $count")
                        android.util.Log.d(TAG, "Change: ${if (count >= previousValue) "+" else ""}${count - previousValue}")
                    }
                }
                android.util.Log.d(TAG, "=== END UPDATING UNREAD COUNTS BY TYPE ===")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error updating unread count by type in database", e)
        }
    }
    
    /**
     * Get the unread notifications count from database
     * Returns the count stored in database, or current count if database value is not available
     */
    fun getUnreadCountFromDatabase(): Int {
        return try {
            val vm = mehdiViewModel
            if (vm != null) {
                val dbValue = vm.getValueByKey(unreadTotalKey())?.value
                if (dbValue != null && dbValue.isNotEmpty()) {
                    dbValue.toIntOrNull() ?: getUnreadNotificationsCount()
                } else {
                    // If database doesn't have the value, return current count and update database
                    val currentCount = getUnreadNotificationsCount()
                    updateUnreadCountInDatabase()
                    currentCount
                }
            } else {
                getUnreadNotificationsCount()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting unread count from database", e)
            getUnreadNotificationsCount()
        }
    }
    
    fun addNotification(notification: NotificationItem) {
        if (currentOwner.isNullOrBlank()) {
            resolveCurrentOwner()
        }
        pruneExpiredNotifications()
        notifications.add(0, notification)
        android.util.Log.d(TAG, "Added notification: ${notification.id} for owner=$currentOwner, total count: ${notifications.size}")
        saveNotificationsToStorage()
        android.util.Log.d(TAG, "Saved ${notifications.size} notifications to storage after adding new one")
        
        android.util.Log.d(TAG, "Notifying ${listeners.size} listeners about new notification")
        listeners.forEach { listener ->
            android.util.Log.d(TAG, "Calling onNotificationAdded on listener: ${listener.javaClass.simpleName}")
            try {
                listener.onNotificationAdded(notification)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error in listener.onNotificationAdded", e)
            }
        }
        listeners.forEach { listener ->
            android.util.Log.d(TAG, "Calling onNotificationsChanged on listener: ${listener.javaClass.simpleName}")
            try {
                listener.onNotificationsChanged(getAllNotifications())
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error in listener.onNotificationsChanged", e)
            }
        }
        
        // Update unread count in database
        updateUnreadCountInDatabase()
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
            val notification = notifications[index]
            notifications[index] = notification.copy(isRead = true, isNew = false)
            saveNotificationsToStorage()
            listeners.forEach { it.onNotificationRead(notificationId) }
            listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
            
            // Update unread count in database
            updateUnreadCountInDatabase()
            
            // Call seenMessage to notify server that message has been read
            if (notification.messageId != null && notification.topic != null && najiPush != null) {
                try {
                    android.util.Log.d(TAG, "Calling seenMessage for messageId: ${notification.messageId}, topic: ${notification.topic}")
                    najiPush?.seenMessage(notification.messageId, notification.topic)
                    android.util.Log.d(TAG, "seenMessage called successfully")
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error calling seenMessage", e)
                }
            } else {
                android.util.Log.d(TAG, "Cannot call seenMessage: messageId=${notification.messageId}, topic=${notification.topic}, najiPush=${najiPush != null}")
            }
        }
    }
    
    fun markAllAsRead() {
        notifications.forEachIndexed { index, notification ->
            if (!notification.isRead) {
                notifications[index] = notification.copy(isRead = true, isNew = false)
            }
        }
        saveNotificationsToStorage()
        listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
        // Update unread count in database
        updateUnreadCountInDatabase()
    }
    
    /**
     * Remove all notifications that have the specified requestId in their serviceData
     * Used when receiving a push notification with path="remove" and requestId
     */
    fun removeNotificationsByRequestId(requestId: String) {
        try {
            android.util.Log.d(TAG, "Removing notifications with requestId: $requestId")
            
            // Find notifications to remove
            val notificationsToRemove = notifications.filter { notification ->
                val serviceData = notification.serviceData
                val notificationRequestId = serviceData?.get("requestId")
                val shouldRemove = notificationRequestId == requestId
                
                if (shouldRemove) {
                    android.util.Log.d(TAG, "Removing notification: id=${notification.id}, title=${notification.title}, requestId=$notificationRequestId")
                }
                
                shouldRemove
            }
            
            val removedCount = notificationsToRemove.size
            
            if (removedCount > 0) {
                // Remove from list
                notifications.removeAll(notificationsToRemove)
                android.util.Log.d(TAG, "Removed $removedCount notification(s) with requestId: $requestId")
                saveNotificationsToStorage()
                listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
                // Update unread count in database
                updateUnreadCountInDatabase()
            } else {
                android.util.Log.d(TAG, "No notifications found with requestId: $requestId")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error removing notifications by requestId: $requestId", e)
        }
    }

    /**
     * Update an existing notification matched by requestId in serviceData.
     * Used when receiving a push with path="updateNotification".
     *
     * Matching order:
     * 1. targetRequestId / originalRequestId / notificationRequestId / requestId
     * 2. fallback: latest notification with same senderTitle (title)
     */
    fun updateNotificationByRequestId(
        requestId: String,
        newRequestId: String,
        title: String,
        message: String,
        senderTime: String?,
        rawTypeCode: String?,
        mappedType: NotificationType,
        serviceData: Map<String, String>?,
        messageId: String? = null,
        topic: String? = null,
    ): NotificationItem? {
        return try {
            android.util.Log.d(TAG, "Updating notification, lookup requestId: $requestId, senderTitle: $title")

            val index = findNotificationIndexForUpdate(requestId, title)
            if (index == -1) {
                android.util.Log.w(
                    TAG,
                    "No notification found to update. lookupRequestId=$requestId, senderTitle=$title, " +
                        "recentStoredRequestIds=${notifications.take(5).mapNotNull { it.serviceData?.get("requestId") }}"
                )
                return null
            }

            val existing = notifications[index]
            android.util.Log.d(
                TAG,
                "Update target found at index=$index, storedRequestId=${existing.serviceData?.get("requestId")}, " +
                    "notificationId=${existing.id}"
            )
            val updated = existing.copy(
                title = title,
                message = message,
                senderTime = senderTime ?: existing.senderTime,
                type = mappedType,
                rawType = rawTypeCode ?: existing.rawType,
                serviceData = mergeServiceDataForUpdate(existing.serviceData, serviceData),
                timestamp = Date(),
                isNew = true,
                isRead = false,
                messageId = messageId ?: existing.messageId,
                topic = topic ?: existing.topic,
            )

            notifications.removeAt(index)
            notifications.add(0, updated)
            android.util.Log.d(
                TAG,
                "Updated notification: id=${updated.id}, storedRequestId=${updated.serviceData?.get("requestId")}"
            )

            saveNotificationsToStorage()
            listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
            updateUnreadCountInDatabase()
            updated
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error updating notification by requestId: $requestId", e)
            null
        }
    }

    private fun resolveUpdateTargetRequestId(serviceData: Map<String, String>): String? {
        return  serviceData["newRequestId"]
            ?: serviceData["targetRequestId"]
            ?: serviceData["originalRequestId"]
            ?: serviceData["notificationRequestId"]
            ?: serviceData["requestId"]
    }
    private fun resolveUpdateRequestId(serviceData: Map<String, String>): String? {
        return  serviceData["requestId"]
    }
    private fun resolveUpdateNewRequestId(serviceData: Map<String, String>): String? {
        return  serviceData["newRequestId"]

    }

    private fun findNotificationIndexForUpdate(targetRequestId: String, senderTitle: String?): Int {
        val byRequestId = notifications.indexOfFirst { notification ->
            notification.serviceData?.get("requestId") == targetRequestId
        }
        if (byRequestId != -1) {
            android.util.Log.d(TAG, "Update matched by requestId: $targetRequestId")
            return byRequestId
        }

        if (!senderTitle.isNullOrBlank()) {
            val byTitle = notifications.indexOfFirst { it.title == senderTitle }
            if (byTitle != -1) {
                android.util.Log.w(
                    TAG,
                    "Update matched by senderTitle fallback '$senderTitle'. " +
                        "lookupRequestId=$targetRequestId did not match storedRequestId=" +
                        "${notifications[byTitle].serviceData?.get("requestId")}"
                )
                return byTitle
            }
        }
        return -1
    }

    private fun mergeServiceDataForUpdate(
        existing: Map<String, String>?,
        update: Map<String, String>?,
    ): Map<String, String>? {
        if (update == null) return existing
        val isUpdatePush = update["path"] == "updateNotification"
        val merged = existing?.toMutableMap() ?: mutableMapOf()
        update.forEach { (key, value) ->
            when {
                key == "path" && (value == "remove" || value == "updateNotification") -> return@forEach
                isUpdatePush && key == "requestId" -> return@forEach
                else -> merged[key] = value
            }
        }
        return merged.ifEmpty { null }
    }
    
    fun clearAllNotifications() {
        notifications.clear()
        saveNotificationsToStorage()
        listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
        // Update unread count in database
        updateUnreadCountInDatabase()
    }

    /**
     * Map raw type code from server to NotificationType enum.
     * type: "0" -> normal (INFO), "1" -> urgent (WARNING), others -> INFO for now.
     */
    private fun mapTypeCodeToNotificationType(typeCode: String?): NotificationType {
        return when (typeCode) {
            "1" -> NotificationType.WARNING // treat "1" as urgent
            // "0" or null -> normal info
            else -> NotificationType.INFO
        }
    }
    
    /**
     * Parse push message and create notification
     * Expected format (simplified):
     * {
     *   "title": "0",
     *   "body": {
     *     "text": "شروع",
     *     "senderTitle": "پلیس همراه",
     *     "senderTime": "1401.06.27  15:20:00",
     *     "type": "0", // "0" = normal, "1" = urgent, others may be added later
     *     "serviceData": { "path": "...", "requestId": "..." }
     *   }
     * }
     *
     * Special serviceData.path values:
     * - "remove": removes notification(s) with matching requestId
     * - "updateNotification": updates notification matched by targetRequestId/originalRequestId/requestId,
     *   or fallback by senderTitle if requestId does not match
     * @param messageId The unique message ID from NajiPush for seenMessage callback
     * @param topic The MQTT topic from NajiPush for seenMessage callback
     */
    fun createNotificationFromPushMessage(
        pushMessage: String,
        messageId: String? = null,
        topic: String? = null
    ): NotificationItem? {
        return try {
            val pushData = Gson().fromJson(pushMessage, PushNotificationData::class.java)
            
            // Extract data from nested body structure
            val body = pushData.body
            if (body == null) {
                android.util.Log.e(TAG, "Push message body is null")
                return null
            }
            
            val title = body.senderTitle ?: pushData.title ?: "اعلان جدید"
            val message = body.text ?: pushMessage
            val senderTime = body.senderTime
            val rawTypeCode = body.type
            val mappedType = mapTypeCodeToNotificationType(rawTypeCode)
            
            android.util.Log.d(TAG, "Parsed push notification:")
            android.util.Log.d(TAG, "Title: $title")
            android.util.Log.d(TAG, "Message: $message")
            android.util.Log.d(TAG, "SenderTime: $senderTime")
            android.util.Log.d(TAG, "TypeCode: $rawTypeCode (mapped to $mappedType)")
            android.util.Log.d(TAG, "ServiceData: ${body.serviceData}")
            android.util.Log.d(TAG, "Actions: ${body.actions}")
            
            // Check if this is a remove notification
            val serviceData = body.serviceData
            if (serviceData != null && serviceData["path"] == "remove") {
                val requestIdToRemove = serviceData["requestId"]
                if (requestIdToRemove != null) {
                    android.util.Log.d(TAG, "Remove notification detected: requestId=$requestIdToRemove")
                    removeNotificationsByRequestId(requestIdToRemove)
                    // Don't create a notification for remove messages
                    return null
                } else {
                    android.util.Log.w(TAG, "Remove notification but requestId is null")
                }
            }

            if (serviceData != null && serviceData["path"] == "updateNotification") {
                val requestIdToUpdate = resolveUpdateTargetRequestId(serviceData)
                val requestId = resolveUpdateRequestId(serviceData)
                val newRequestId = resolveUpdateNewRequestId(serviceData)
                if (requestId != null && newRequestId != null) {
                    android.util.Log.d(TAG, "Update notification detected: lookupRequestId=$requestIdToUpdate")
                    body.actions?.let { actions -> executeActions(actions) }
                    return updateNotificationByRequestId(
                        requestId = requestId,
                        newRequestId = newRequestId,
                        title = title,
                        message = message,
                        senderTime = senderTime,
                        rawTypeCode = rawTypeCode,
                        mappedType = mappedType,
                        serviceData = serviceData,
                        messageId = messageId,
                        topic = topic,
                    )
                } else {
                    android.util.Log.w(TAG, "Update notification but requestId is null")
                }
            }
            
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
                type = mappedType,
                data = null,
                serviceData = body.serviceData, // Store for later service call
                senderTime = senderTime,
                rawType = rawTypeCode,
                messageId = messageId,  // For seenMessage callback
                topic = topic           // For seenMessage callback
            )
            
            addNotification(notification)
            notification
        } catch (e: JsonSyntaxException) {
            // Unparseable payload is usually ciphertext decrypted with the wrong key; never show it.
            android.util.Log.e(TAG, "Dropping unparseable push message", e)
            null
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
     * Supports two modes:
     * - "call_service": Always calls the service
     * - "check_db_call_service": First checks DB, if exists uses cached response, otherwise calls service and caches result
     */
    fun callServiceForNotification(notification: NotificationItem) {
        try {
            android.util.Log.d(TAG, "=== callServiceForNotification called ===")
            android.util.Log.d(TAG, "Notification ID: ${notification.id}")
            android.util.Log.d(TAG, "ActionHandler is null: ${actionHandler == null}")
            android.util.Log.d(TAG, "MehdiViewModel is null: ${mehdiViewModel == null}")

            saveReceiveMessageTime(notification)
            // Do not force full-screen: short Div sheets must wrap content and stay at the bottom.
            pendingBottomSheetFullScreen = false
            
            notification.serviceData?.let { serviceData ->
                android.util.Log.d(TAG, "Calling service for notification: ${notification.id}")
                android.util.Log.d(TAG, "Service data: $serviceData")

                val mode = serviceData["mode"] ?: "call_service" // Default to call_service if mode not specified
                val path = serviceData["path"]
                val requestId = serviceData["newRequestId"] ?: ""

                android.util.Log.d(TAG, "Notification service mode: $mode, path: $path, requestId: $requestId")

                // If path is "empty", don't call service
                if (path == "empty") {
                    android.util.Log.d(TAG, "Path is 'empty', skipping service call for notification: ${notification.id}")
                    return
                }

                // For check_db_call_service mode, first check if response exists in DB
                if (mode == "check_db_call_service" && path != null && requestId.isNotEmpty()) {
                    val dbKey = "notification_service_response_${path}_${requestId}"
                    android.util.Log.d(TAG, "Checking DB for cached response with key: $dbKey")
                    
                    val cachedResponse = mehdiViewModel?.getValueByKey(dbKey)
                    if (cachedResponse?.value != null && cachedResponse.value.isNotEmpty()) {
                        android.util.Log.d(TAG, "Found cached response in DB, using it instead of calling service")
                        // Parse cached response and process it as if it came from service
                        processCachedServiceResponse(cachedResponse.value, path, requestId)
                        return
                    } else {
                        android.util.Log.d(TAG, "No cached response found, will call service and cache result")
                        // Continue to call service, but we'll cache the response when it arrives
                    }
                }

                // Call service (either mode is call_service, or check_db_call_service with no cache)
                val currentActionHandler = actionHandler
                android.util.Log.d(TAG, "Current ActionHandler: ${currentActionHandler != null}")
                if (currentActionHandler != null) {
                    val requestMap = HashMap<String, String>()
                    // Copy all serviceData key/values
                    serviceData.forEach { (key, value) ->
                        requestMap[key] = value
                    }
                    // Ensure required keys are present
                    if (path != null) {
                        requestMap["path"] = path
                    }
                    // Per spec: send token as "empty" to be filled from DB
                    requestMap["ph/token"] = "empty"
                    
                    // Add mode and requestId to request so we can cache response later
                    if (mode == "check_db_call_service") {
                        requestMap["_notification_mode"] = mode
                        requestMap["_notification_request_id"] = requestId
                        requestMap["_notification_id"] = notification.id
                    }

                    android.util.Log.d(TAG, "Forwarding notification service request via ActionHandler.requestFromNotification: $requestMap")
                    android.util.Log.d(TAG, "ActionHandler class: ${currentActionHandler.javaClass.name}")
                    try {
                        android.util.Log.d(TAG, "About to call currentActionHandler.requestFromNotification")
                    currentActionHandler.requestFromNotification(requestMap)
                        android.util.Log.d(TAG, "Notification service request dispatched successfully")
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "Error calling requestFromNotification", e)
                        e.printStackTrace()
                    }
                } else {
                    android.util.Log.w(TAG, "ActionHandler is null, using fallback method")
                    // Fallback: use existing MehdiViewModel path if actionHandler is not available
                    val currentMehdiViewModel = mehdiViewModel
                    if (currentMehdiViewModel == null) {
                        android.util.Log.e(TAG, "Both ActionHandler and MehdiViewModel are null! Cannot call service.")
                        return
                    }

                    val phId = path ?: "default"
                    val requestMap = HashMap<String, String>()
                    serviceData.forEach { (key, value) ->
                        requestMap[key] = value
                    }
                    requestMap["ph/token"] = requestMap["ph/token"] ?: "empty"
                    
                    // Add mode and requestId for caching
                    if (mode == "check_db_call_service") {
                        requestMap["_notification_mode"] = mode
                        requestMap["_notification_request_id"] = requestId
                        requestMap["_notification_id"] = notification.id
                    }

                    android.util.Log.d(TAG, "Calling setphPlusRequest (fallback) with phId: $phId, requestMap: $requestMap")
                    currentMehdiViewModel.setphPlusRequest(phId, requestMap)
                    android.util.Log.d(TAG, "Service call initiated successfully (fallback)")
                }
            } ?: run {
                android.util.Log.d(TAG, "No service data for notification: ${notification.id}")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error calling service for notification", e)
        }
    }
    
    /**
     * Process cached service response as if it came from the service
     * This simulates the same behavior as phPlus.observe in MehdiActivity
     * We use a workaround: send response as a fake request to trigger the same processing logic
     */
    private fun processCachedServiceResponse(cachedResponseJson: String, path: String, requestId: String) {
        try {
            android.util.Log.d(TAG, "Processing cached service response for path: $path, requestId: $requestId")
            
            // Parse the cached JSON response
            val responseMap = Gson().fromJson(cachedResponseJson, Map::class.java) as? Map<String, String>
                ?: return
            
            android.util.Log.d(TAG, "Parsed cached response: $responseMap")
            
            // Process the response by simulating a service response
            // We'll use the actionHandler to process it, but we need to trigger the same logic as phPlus.observe
            // Since we can't directly call phPlus.observe logic, we'll use a workaround:
            // Create a fake Resource.SUCCESS response and trigger the observer logic
            // But this is complex, so instead we'll use the actionHandler's processServiceResponse
            // which will forward to onRequest, but that's not ideal...
            
            // Better approach: Use MehdiViewModel to simulate the response
            // But MehdiViewModel doesn't have a method to inject response directly
            // So we'll use a workaround: store response in a special key and trigger processing
            
            // Actually, the simplest approach: use actionHandler.processServiceResponse
            // which will call onRequest, but we need to make sure onRequest can handle responses
            // For now, let's just log and let the user know cached response is being used
            android.util.Log.d(TAG, "Using cached response - forwarding to action handler")
            
            val currentActionHandler = actionHandler
            if (currentActionHandler != null) {
                // Convert to HashMap
                val responseHashMap = HashMap<String, String>()
                responseMap.forEach { (key, value) ->
                    responseHashMap[key] = value
                }
                
                // Mark as cached response so onRequest knows not to send to server
                responseHashMap["_cached_response"] = "true"
                
                android.util.Log.d(TAG, "Forwarding cached response to action handler")
                currentActionHandler.processServiceResponse(responseHashMap)
            } else {
                android.util.Log.e(TAG, "ActionHandler is null, cannot process cached response")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error processing cached service response", e)
        }
    }
    
    /**
     * Save service response to database for check_db_call_service mode
     * Called from UIDiv2ActionHandler after service response is received
     */
    fun saveServiceResponseToCache(path: String, requestId: String, response: Map<String, String>) {
        try {
            if (path.isEmpty() || requestId.isEmpty()) {
                android.util.Log.w(TAG, "Cannot cache response: path or requestId is empty")
                return
            }
            
            val dbKey = "notification_service_response_${path}_${requestId}"
            val responseJson = Gson().toJson(response)
            
            android.util.Log.d(TAG, "Saving service response to cache with key: $dbKey")
            android.util.Log.d(TAG, "Response JSON: $responseJson")
            
            mehdiViewModel?.insertItemToDb(
                PhPlusDB(null, dbKey, responseJson)
            )
            
            android.util.Log.d(TAG, "Service response cached successfully")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error saving service response to cache", e)
        }
    }
    
    /**
     * Persist notifications list.
     * Primary path: DB via MehdiViewModel (when available).
     * Fallback: SharedPreferences (when viewModel is null, e.g. app in background before Activity init).
     */
    private fun saveNotificationsToStorage() {
        try {
            // Ensure owner is known before writing (important for background pushes)
            if (currentOwner.isNullOrBlank()) {
                resolveCurrentOwner()
            }
            val notificationsJson = Gson().toJson(notifications)
            android.util.Log.d(TAG, "Saving ${notifications.size} notifications for owner=$currentOwner. JSON length: ${notificationsJson.length}")
            val vm = mehdiViewModel
            if (vm != null) {
                vm.insertItemToDb(
                    PhPlusDB(null, notificationsDbKey(), notificationsJson)
                )
                android.util.Log.d(TAG, "Notifications saved to database key=${notificationsDbKey()} - ${notifications.size} notifications")
            } else {
                // Fallback for background pushes before Activity init
                val ctx = com.yandex.divkit.demo.DivkitApplication.getInstance()?.applicationContext
                if (ctx != null) {
                    ctx.getSharedPreferences(NOTIFICATIONS_PREFS, Context.MODE_PRIVATE)
                        .edit()
                        .putString(notificationsPrefsKey(), notificationsJson)
                        .apply()
                    android.util.Log.d(TAG, "Notifications saved to SharedPreferences key=${notificationsPrefsKey()} - ${notifications.size} notifications")
                } else {
                    android.util.Log.w(TAG, "Cannot save notifications: context is null")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error saving notifications to storage", e)
        }
    }
    
    /**
     * Load notifications list.
     * Primary path: DB via MehdiViewModel (after Activity init).
     * Fallback: SharedPreferences persisted during background pushes.
     */
    private fun loadNotificationsFromStorage() {
        try {
            if (currentOwner.isNullOrBlank()) {
                resolveCurrentOwner()
            }
            val beforeCount = notifications.size
            android.util.Log.d(TAG, "Loading notifications for owner=$currentOwner. Current count in memory: $beforeCount")
            
            val json: String? = run {
                val vm = mehdiViewModel
                if (vm != null) {
                    val key = notificationsDbKey()
                    val savedData = vm.getValueByKey(key)
                    android.util.Log.d(TAG, "Loading from DB key=$key. Found data: ${savedData?.value != null}")
                    savedData?.value
                } else {
                    val ctx = com.yandex.divkit.demo.DivkitApplication.getInstance()?.applicationContext
                    val prefs = ctx?.getSharedPreferences(NOTIFICATIONS_PREFS, Context.MODE_PRIVATE)
                    val prefsKey = notificationsPrefsKey()
                    val prefsValue = prefs?.getString(prefsKey, null)
                    android.util.Log.d(TAG, "Loading from SharedPreferences key=$prefsKey. Found data: ${prefsValue != null}")
                    prefsValue
                }
            }

            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<NotificationItem>>() {}.type
                val savedNotifications: List<NotificationItem> = Gson().fromJson(json, type)
                android.util.Log.d(TAG, "Parsed ${savedNotifications.size} notifications from storage JSON")

                // Merge notifications: combine saved notifications with any new ones in memory
                // This handles the case where a notification was added but not yet saved to storage
                val savedNotificationIds = savedNotifications.map { it.id }.toSet()
                val newNotificationsInMemory = notifications.filter { it.id !in savedNotificationIds }
                
                if (newNotificationsInMemory.isNotEmpty()) {
                    android.util.Log.d(TAG, "Found ${newNotificationsInMemory.size} new notifications in memory that are not in storage yet. Merging...")
                }

                notifications.clear()
                // Add saved notifications first
                notifications.addAll(savedNotifications)
                // Then add any new notifications from memory that weren't saved yet
                notifications.addAll(newNotificationsInMemory)
                
                // Sort by timestamp (newest first)
                notifications.sortByDescending { it.timestamp }
                pruneExpiredNotifications()

                android.util.Log.d(TAG, "Loaded ${notifications.size} notifications from storage (was $beforeCount in memory, ${savedNotifications.size} from storage, ${newNotificationsInMemory.size} merged from memory)")
                
                // If we merged new notifications, save them to storage now
                if (newNotificationsInMemory.isNotEmpty()) {
                    android.util.Log.d(TAG, "Saving merged notifications to storage...")
                    saveNotificationsToStorage()
                }
                
                listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
                // Initialize cache from database before updating
                initializeCacheFromDatabase()
                
                listeners.forEach { it.onNotificationsChanged(getAllNotifications()) }
                // Update unread count in database after loading notifications
                updateUnreadCountInDatabase()
            } else {
                android.util.Log.d(TAG, "No saved notifications found for owner=$currentOwner. Keeping ${notifications.size} notifications in memory")
                // Initialize cache from database before updating
                initializeCacheFromDatabase()
                // Update unread count even if no notifications were loaded
                updateUnreadCountInDatabase()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error loading notifications from storage", e)
        }
    }
    
    private data class PushNotificationData(
        val title: String?,                   // Root level title
        val body: PushNotificationBody?       // Nested body containing actual data
    )
    
    private data class PushNotificationBody(
        val text: String?,                    // Subtitle for notification
        val senderTitle: String?,             // Title for notification
        val senderTime: String?,              // Formatted time string to show in list
        val type: String?,                    // Raw type code: "0" normal, "1" urgent, etc.
        val serviceData: Map<String, String>?, // Service call data
        val actions: List<ActionItem>?        // Actions to execute
    )
    
    private data class ActionItem(
        val log_id: String,
        val url: String
    )
}

