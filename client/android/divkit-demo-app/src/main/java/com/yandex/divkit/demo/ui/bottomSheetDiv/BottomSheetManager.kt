package com.yandex.divkit.demo.ui.bottomSheetDiv

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import java.util.*

/**
 * Manages multiple BottomSheetDiv instances with targeting capabilities
 */
class BottomSheetManager {
    
    // Stack to maintain bottom sheets in order (first = oldest, last = newest)
    private val bottomSheetStack = mutableListOf<BottomSheetDiv>()
    
    // Map to track bottom sheets by unique ID
    private val bottomSheetMap = mutableMapOf<String, BottomSheetDiv>()
    
    /**
     * Add a new bottom sheet to the manager
     * @param bottomSheet The bottom sheet instance
     * @param id Optional unique ID for targeting (if null, auto-generates one)
     * @return The ID assigned to this bottom sheet
     */
    fun addBottomSheet(bottomSheet: BottomSheetDiv, id: String? = null): String {
        val assignedId = id ?: generateUniqueId()
        
        bottomSheetStack.add(bottomSheet)
        bottomSheetMap[assignedId] = bottomSheet
        
        return assignedId
    }
    
    /**
     * Remove a bottom sheet from the manager
     * @param bottomSheet The bottom sheet instance to remove
     */
    fun removeBottomSheet(bottomSheet: BottomSheetDiv) {
        bottomSheetStack.remove(bottomSheet)
        // Remove from map by finding the key
        val keyToRemove = bottomSheetMap.entries.find { it.value == bottomSheet }?.key
        keyToRemove?.let { bottomSheetMap.remove(it) }
    }
    
    /**
     * Get the current (topmost) bottom sheet
     */
    fun getCurrentBottomSheet(): BottomSheetDiv? {
        return bottomSheetStack.lastOrNull()
    }
    
    /**
     * Get the first (bottommost) bottom sheet
     */
    fun getFirstBottomSheet(): BottomSheetDiv? {
        return bottomSheetStack.firstOrNull()
    }
    
    /**
     * Get bottom sheet by ID
     * @param id The unique ID of the bottom sheet
     */
    fun getBottomSheetById(id: String): BottomSheetDiv? {
        return bottomSheetMap[id]
    }
    
    /**
     * Get bottom sheet by position in stack (0 = first, 1 = second, etc.)
     * @param position Position in the stack (0-based)
     */
    fun getBottomSheetByPosition(position: Int): BottomSheetDiv? {
        return if (position >= 0 && position < bottomSheetStack.size) {
            bottomSheetStack[position]
        } else null
    }
    
    /**
     * Set variable on a specific bottom sheet
     * @param target The target identifier (id, position, or "current"/"first")
     * @param key The variable key
     * @param value The variable value
     * @return true if variable was set successfully, false otherwise
     */
    fun setVariableOnTarget(target: String, key: String, value: String): Boolean {
        println("BottomSheetManager: ========== SETTING VARIABLE ON TARGET ==========")
        println("BottomSheetManager: target=$target, key=$key, value=$value")
        println("BottomSheetManager: Total bottom sheets: ${getBottomSheetCount()}")
        println("BottomSheetManager: All IDs: ${getAllBottomSheetIds()}")
        
        val bottomSheet = when (target.lowercase()) {
            "current", "top", "latest" -> {
                println("BottomSheetManager: Targeting current (latest) bottom sheet")
                getCurrentBottomSheet()
            }
            "first", "bottom", "oldest" -> {
                println("BottomSheetManager: Targeting first (oldest) bottom sheet")
                getFirstBottomSheet()
            }
            else -> {
                println("BottomSheetManager: Trying to resolve target: $target")
                // Try as ID first
                val byId = getBottomSheetById(target)
                if (byId != null) {
                    println("BottomSheetManager: Found bottom sheet by ID: $target")
                    byId
                } else {
                    // Try as position (0-based)
                    val position = target.toIntOrNull()
                    if (position != null) {
                        println("BottomSheetManager: Trying to get bottom sheet by position: $position")
                        val byPosition = getBottomSheetByPosition(position)
                        if (byPosition != null) {
                            println("BottomSheetManager: Found bottom sheet at position: $position")
                        } else {
                            println("BottomSheetManager: No bottom sheet found at position: $position")
                        }
                        byPosition
                    } else {
                        println("BottomSheetManager: Target '$target' is not a valid ID or position")
                        null
                    }
                }
            }
        }
        
        return if (bottomSheet != null) {
            println("BottomSheetManager: Bottom sheet found, setting variable...")
            bottomSheet.setVariableOnBottomSheet(key, value)
            println("BottomSheetManager: Variable set successfully!")
            true
        } else {
            println("BottomSheetManager: No bottom sheet found for target: $target")
            false
        }
    }
    
    /**
     * Get the count of active bottom sheets
     */
    fun getBottomSheetCount(): Int {
        return bottomSheetStack.size
    }
    
    /**
     * Get all bottom sheet IDs
     */
    fun getAllBottomSheetIds(): List<String> {
        return bottomSheetMap.keys.toList()
    }
    
    /**
     * Clear all bottom sheets from manager
     */
    fun clear() {
        bottomSheetStack.clear()
        bottomSheetMap.clear()
    }
    
    /**
     * Generate a unique ID for bottom sheets
     */
    private fun generateUniqueId(): String {
        return "bottomsheet_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
    }
    
    /**
     * Create a new bottom sheet and add it to the manager
     * @param context The context
     * @param activity The activity
     * @param jsonName The JSON name for the bottom sheet content
     * @param mehdiViewModel The view model
     * @param lo The lifecycle owner
     * @param id Optional unique ID
     * @return The created bottom sheet instance
     */
    fun createAndAddBottomSheet(
        context: Context,
        activity: Activity,
        jsonName: String,
        mehdiViewModel: MehdiViewModel,
        lo: LifecycleOwner,
        id: String? = null
    ): BottomSheetDiv {
        val bottomSheet = BottomSheetDiv(context, activity, jsonName, mehdiViewModel, lo, this)
        val assignedId = addBottomSheet(bottomSheet, id)
        // Store the ID in the bottom sheet for reference
        bottomSheet.assignedId = assignedId
        return bottomSheet
    }
}
