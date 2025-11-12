package com.yandex.divkit.demo.div

import org.json.JSONArray
import org.json.JSONObject

/**
 * Generates dynamic patches offline using templates stored in the database
 */
class DynamicPatchGenerator {
    
    // Track items for each container to enable appending
    private val containerItems = mutableMapOf<String, JSONArray>()
    
    // Track original container definitions to preserve all properties
    private val containerDefinitions = mutableMapOf<String, JSONObject>()

    /**
     * Generate a patch that adds an item to a container using a template
     * @param templateJson The template JSON string from database (can be template type or full item)
     * @param containerId The ID of the container to add the item to
     * @param variables Map of variable names to values (e.g., "item_name" -> "اكبر")
     * @param position Position to insert item (null = append to end, 0 = beginning, etc.)
     * @param templateType Optional template type name (for DivKit template references)
     * @param currentView The current Div2View to read existing items from
     * @return Generated patch JSON object
     */
    fun generateAddItemPatch(
        templateJson: String,
        containerId: String,
        variables: Map<String, String>,
        position: Int? = null,
        templateType: String? = null,
        currentView: com.yandex.div.core.view2.Div2View? = null
    ): JSONObject {
        println("DynamicPatchGenerator: ========== GENERATING ADD ITEM PATCH ==========")
        println("DynamicPatchGenerator: containerId=$containerId")
        println("DynamicPatchGenerator: variables=$variables")
        println("DynamicPatchGenerator: position=$position")
        println("DynamicPatchGenerator: templateType=$templateType")
        
        // Create item - either as template reference or processed template
        val processedItem = if (templateType != null) {
            // Create item that references a DivKit template
            println("DynamicPatchGenerator: Creating template reference item")
            val item = JSONObject()
            item.put("type", templateType)
            
            // Add all variables as properties
            variables.forEach { (key, value) ->
                // Try to parse as JSON for complex types (arrays, objects)
                try {
                    when {
                        value.startsWith("[") && value.endsWith("]") -> {
                            item.put(key, JSONArray(value))
                        }
                        value.startsWith("{") && value.endsWith("}") -> {
                            item.put(key, JSONObject(value))
                        }
                        else -> {
                            item.put(key, value)
                        }
                    }
                } catch (e: Exception) {
                    // If parsing fails, treat as string
                    item.put(key, value)
                }
            }
            
            item
        } else {
            // Process the full template with variable replacement
            println("DynamicPatchGenerator: Processing full template")
            val template = JSONObject(templateJson)
            replaceVariablesInJson(template, variables)
        }
        
        // Generate unique ID for the item if not present
        if (!processedItem.has("id")) {
            val itemId = generateUniqueItemId()
            processedItem.put("id", itemId)
        }
        
        println("DynamicPatchGenerator: Generated item: ${processedItem.toString(2)}")
        
        // Get or create the items array for this container
        // We maintain state in memory since reading from view requires internal APIs
        val items = containerItems.getOrPut(containerId) { JSONArray() }
        
        println("DynamicPatchGenerator: Current items count: ${items.length()}")
        
        // Add new item to the array
        if (position != null && position >= 0 && position <= items.length()) {
            // Insert at specific position
            println("DynamicPatchGenerator: Inserting at position $position")
            val newItems = JSONArray()
            for (i in 0 until items.length()) {
                if (i == position) {
                    newItems.put(processedItem)
                }
                newItems.put(items.get(i))
            }
            if (position >= items.length()) {
                newItems.put(processedItem)
            }
            containerItems[containerId] = newItems
        } else {
            // Append to end
            println("DynamicPatchGenerator: Appending to end")
            items.put(processedItem)
            println("DynamicPatchGenerator: Item added to array, new length: ${items.length()}")
            
            // Debug: Print all item IDs
            println("DynamicPatchGenerator: All item IDs in array:")
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                println("DynamicPatchGenerator:   [$i] id=${item.optString("id", "NO_ID")}")
            }
        }
        
        println("DynamicPatchGenerator: Total items after adding new item: ${items.length()}")
        
        // Create the patch structure following DivKit's patch format
        // We need to wrap all items in a container with the same ID as the target
        val patch = JSONObject()
        val changes = JSONArray()
        val change = JSONObject()
        
        // Get or create the container definition
        val replacementContainer = containerDefinitions[containerId] ?: run {
            println("DynamicPatchGenerator: No cached container definition, creating default")
            JSONObject().apply {
                put("type", "gallery")
                put("width", JSONObject().apply { put("type", "match_parent") })
                put("height", JSONObject().apply { put("type", "wrap_content") })
                put("orientation", "vertical")
            }
        }
        
        // Clone the container definition to avoid modifying the cached version
        val containerCopy = JSONObject(replacementContainer.toString())
        
        // Always set/update the id and items
        containerCopy.put("id", containerId)
        containerCopy.put("items", items)
        
        println("DynamicPatchGenerator: Using container definition: ${containerCopy.toString(2)}")
        
        // Set the target ID and the replacement container
        change.put("id", containerId)
        change.put("items", JSONArray().apply {
            put(containerCopy)
        })
        
        // Add change to changes array
        changes.put(change)
        
        // Add changes to patch with mode
        patch.put("patch", JSONObject().apply {
            put("mode", "partial")
            put("changes", changes)
        })
        
        println("DynamicPatchGenerator: Patch generated successfully")
        println("DynamicPatchGenerator: ${patch.toString(2)}")
        println("DynamicPatchGenerator: ========== END GENERATING ADD ITEM PATCH ==========")
        
        return patch
    }
    
    /**
     * Set the original container definition to preserve all properties dynamically
     * Call this once when the page loads to cache the container structure
     */
    fun setContainerDefinition(containerId: String, containerJson: JSONObject) {
        println("DynamicPatchGenerator: Caching container definition for: $containerId")
        // Remove items from the cached definition to avoid duplication
        val cleanedContainer = JSONObject(containerJson.toString())
        cleanedContainer.remove("items")
        containerDefinitions[containerId] = cleanedContainer
        println("DynamicPatchGenerator: Cached definition: ${cleanedContainer.toString(2)}")
    }
    
    /**
     * Clear cached items for a container (useful when navigating to a fresh page)
     */
    fun clearContainer(containerId: String) {
        println("DynamicPatchGenerator: Clearing container cache for: $containerId")
        containerItems.remove(containerId)
        containerDefinitions.remove(containerId)
    }
    
    /**
     * Clear all cached containers
     */
    fun clearAll() {
        println("DynamicPatchGenerator: Clearing all container caches")
        containerItems.clear()
        containerDefinitions.clear()
    }
    
    /**
     * Generate a patch that removes an item from a container
     * @param containerId The ID of the container
     * @param itemId The ID of the item to remove
     * @return Generated patch JSON object
     */
    fun generateRemoveItemPatch(
        containerId: String,
        itemId: String
    ): JSONObject {
        println("DynamicPatchGenerator: Generating remove item patch - containerId=$containerId, itemId=$itemId")
        
        val patch = JSONObject()
        val patches = JSONArray()
        val patchObject = JSONObject()
        val changes = JSONArray()
        val change = JSONObject()
        
        change.put("id", containerId)
        change.put("mode", "remove")
        change.put("item_id", itemId)
        
        changes.put(change)
        patchObject.put("changes", changes)
        patches.put(patchObject)
        patch.put("patches", patches)
        
        return patch
    }
    
    /**
     * Recursively replace variables in JSON object
     * Variables are in format @{variable_name} or DivKit template bindings like $text
     */
    private fun replaceVariablesInJson(json: JSONObject, variables: Map<String, String>): JSONObject {
        val result = JSONObject()
        
        json.keys().forEach { key ->
            val value = json.get(key)
            
            // Handle DivKit template bindings (keys starting with $)
            if (key.startsWith("$")) {
                val bindingName = key.substring(1) // Remove the $
                val variableValue = when (value) {
                    is String -> variables[value] ?: value // Try to resolve the binding
                    else -> value
                }
                
                // Add the actual property (without $)
                when (variableValue) {
                    is String -> result.put(bindingName, replaceVariables(variableValue, variables))
                    is JSONObject -> result.put(bindingName, replaceVariablesInJson(variableValue, variables))
                    is JSONArray -> result.put(bindingName, replaceVariablesInArray(variableValue, variables))
                    else -> result.put(bindingName, variableValue)
                }
            } else {
                // Normal property
                when (value) {
                    is String -> {
                        result.put(key, replaceVariables(value, variables))
                    }
                    is JSONObject -> {
                        result.put(key, replaceVariablesInJson(value, variables))
                    }
                    is JSONArray -> {
                        result.put(key, replaceVariablesInArray(value, variables))
                    }
                    else -> {
                        result.put(key, value)
                    }
                }
            }
        }
        
        return result
    }
    
    /**
     * Recursively replace variables in JSON array
     */
    private fun replaceVariablesInArray(array: JSONArray, variables: Map<String, String>): JSONArray {
        val result = JSONArray()
        
        for (i in 0 until array.length()) {
            val value = array.get(i)
            when (value) {
                is String -> {
                    result.put(replaceVariables(value, variables))
                }
                is JSONObject -> {
                    result.put(replaceVariablesInJson(value, variables))
                }
                is JSONArray -> {
                    result.put(replaceVariablesInArray(value, variables))
                }
                else -> {
                    result.put(value)
                }
            }
        }
        
        return result
    }
    
    /**
     * Replace variables in a string
     * Supports format: @{variable_name}
     */
    private fun replaceVariables(text: String, variables: Map<String, String>): String {
        var result = text
        
        variables.forEach { (key, value) ->
            result = result.replace("@{$key}", value)
        }
        
        return result
    }
    
    /**
     * Generate a unique ID for items
     */
    private fun generateUniqueItemId(): String {
        return "item_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * Generate a patch that updates specific fields in existing items
     * @param containerId The container ID
     * @param itemId The item ID to update
     * @param updates Map of field paths to new values
     * @return Generated patch JSON object
     */
    fun generateUpdateItemPatch(
        containerId: String,
        itemId: String,
        updates: Map<String, String>
    ): JSONObject {
        println("DynamicPatchGenerator: Generating update item patch - itemId=$itemId, updates=$updates")
        
        val patch = JSONObject()
        val patches = JSONArray()
        val patchObject = JSONObject()
        val changes = JSONArray()
        
        updates.forEach { (fieldPath, newValue) ->
            val change = JSONObject()
            change.put("id", itemId)
            change.put("mode", "update")
            change.put("path", fieldPath)
            change.put("value", newValue)
            changes.put(change)
        }
        
        patchObject.put("changes", changes)
        patches.put(patchObject)
        patch.put("patches", patches)
        
        return patch
    }
}

