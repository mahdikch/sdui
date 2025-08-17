package com.yandex.divkit.demo.utils

import android.util.Log
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import org.json.JSONArray
import org.json.JSONObject

/**
 * Generates DivKit patches for dynamic content updates.
 * Patches follow the DivKit patch structure: { "patch": { "mode": "partial", "changes": [...] } }
 */
class PatchGenerator(
    private val mehdiViewModel: MehdiViewModel?
) {
    companion object {
        private const val tag = "PatchGenerator"
    }

    /**
     * Generates a patch to update a container with dynamic content based on data.
     * 
     * @param containerId The ID of the container to update
     * @param templateName The name of the template to use for generating items
     * @param patchName The name to save the patch in the database
     * @param dataJson The JSON array of data items to generate content from
     * @return true if successful, false otherwise
     */
    fun generatePatch(
        containerId: String,
        templateName: String,
        patchName: String,
        dataJson: String
    ): Boolean {
        return try {
            Log.d(tag, "=== PATCH GENERATION START ===")
            Log.d(tag, "containerId: $containerId")
            Log.d(tag, "templateName: $templateName")
            Log.d(tag, "patchName: $patchName")
            Log.d(tag, "dataJson: $dataJson")
            
            // Parse the data JSON
            val dataArray = JSONArray(dataJson)
            Log.d(tag, "Data array parsed successfully, has ${dataArray.length()} items")
            
            // Log each data item
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                Log.d(tag, "Data item $i: $item")
            }
            
            // Create the patch structure
            Log.d(tag, "Creating patch structure...")
            val patch = createPatchStructure(containerId, templateName, dataArray)
            Log.d(tag, "Patch structure created: $patch")
            
            // Save the patch to database
            Log.d(tag, "Saving patch to database with name: $patchName")
            savePatchToDatabase(patchName, patch.toString())
            
            Log.d(tag, "=== PATCH GENERATION SUCCESS ===")
            true
        } catch (e: Exception) {
            Log.e(tag, "=== PATCH GENERATION ERROR ===", e)
            Log.e(tag, "Error details: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Creates the patch structure with templates and changes.
     */
    private fun createPatchStructure(
        containerId: String,
        templateName: String,
        dataArray: JSONArray
    ): JSONObject {
        Log.d(tag, "=== CREATING PATCH STRUCTURE ===")
        Log.d(tag, "containerId: $containerId")
        Log.d(tag, "templateName: $templateName")
        Log.d(tag, "dataArray length: ${dataArray.length()}")
        
        val patch = JSONObject()
        
        // Create templates section
        Log.d(tag, "Creating templates section...")
        val templates = JSONObject()
        
        // Create text_template definition
        val textTemplateDefinition = createTextTemplateDefinition()
        Log.d(tag, "Text template definition created: $textTemplateDefinition")
        templates.put("text_template", textTemplateDefinition)
        
        // Create custom_template definition
        val customTemplateDefinition = createCustomTemplateDefinition()
        Log.d(tag, "Custom template definition created: $customTemplateDefinition")
        templates.put("custom_template", customTemplateDefinition)
        
        // Create patch section
        Log.d(tag, "Creating patch section...")
        val patchSection = JSONObject()
        patchSection.put("mode", "partial")
        
        // Create changes array
        Log.d(tag, "Creating changes array...")
        val changes = JSONArray()
        val change = JSONObject()
        change.put("id", "information_container") // Target the inquiry container
        
        // Create the information_container with items
        val informationContainer = JSONObject()
        informationContainer.put("type", "container")
        informationContainer.put("id", "information_container")
        
        // Add width and height properties
        informationContainer.put("width", JSONObject().apply {
            put("type", "match_parent")
        })
        informationContainer.put("height", JSONObject().apply {
            put("type", "wrap_content")
        })
        
        // Generate items for the information_container
        Log.d(tag, "Generating items for information_container...")
        val items = JSONArray()
        for (i in 0 until dataArray.length()) {
            val dataItem = dataArray.getJSONObject(i)
            Log.d(tag, "Processing data item $i: $dataItem")
            
            val templateItem = generateTemplateItem(templateName, dataItem)
            Log.d(tag, "Generated template item $i: $templateItem")
            
            items.put(templateItem)
        }
        
        informationContainer.put("items", items)
        
        // Add margins to information_container
        informationContainer.put("margins", JSONObject().apply {
            put("top", 12)
        })
        
        // Create the items array for the change (contains the information_container)
        val changeItems = JSONArray()
        changeItems.put(informationContainer)
        change.put("items", changeItems)
        
        Log.d(tag, "Change object created: $change")
        
        changes.put(change)
        patchSection.put("changes", changes)
        
        // Combine templates and patch
        patch.put("templates", templates)
        patch.put("patch", patchSection)
        
        Log.d(tag, "Final patch structure: $patch")
        return patch
    }

    /**
     * Creates a text_template definition as a container with multiple text elements.
     */
    private fun createTextTemplateDefinition(): JSONObject {
        Log.d(tag, "=== CREATING TEXT TEMPLATE DEFINITION ===")
        
        val template = JSONObject()
        template.put("type", "container")
        
        // Create items array with multiple text elements
        val items = JSONArray()
        
        // Name text element
        val nameText = JSONObject()
        nameText.put("type", "text")
        nameText.put("${'$'}text", "name")
        nameText.put("font_size", 14)
        nameText.put("line_height", 32)
        nameText.put("letter_spacing", 0)
        nameText.put("text_color", "#000000")
        nameText.put("text_alignment_horizontal", "right")
        nameText.put("text_alignment_vertical", "top")
        nameText.put("width", JSONObject().apply {
            put("type", "wrap_content")
        })
        nameText.put("height", JSONObject().apply {
            put("type", "wrap_content")
        })
        nameText.put("alignment_horizontal", "center")
        nameText.put("alignment_vertical", "center")
        items.put(nameText)
        
        // Family text element
        val familyText = JSONObject()
        familyText.put("type", "text")
        familyText.put("${'$'}text", "family")
        familyText.put("font_size", 14)
        familyText.put("line_height", 32)
        familyText.put("letter_spacing", 0)
        familyText.put("text_color", "#000000")
        familyText.put("text_alignment_horizontal", "right")
        familyText.put("text_alignment_vertical", "top")
        familyText.put("width", JSONObject().apply {
            put("type", "wrap_content")
        })
        familyText.put("height", JSONObject().apply {
            put("type", "wrap_content")
        })
        familyText.put("alignment_horizontal", "center")
        familyText.put("alignment_vertical", "center")
        items.put(familyText)
        
        // City text element
        val cityText = JSONObject()
        cityText.put("type", "text")
        cityText.put("${'$'}text", "city")
        cityText.put("font_size", 14)
        cityText.put("line_height", 32)
        cityText.put("letter_spacing", 0)
        cityText.put("text_color", "#000000")
        cityText.put("text_alignment_horizontal", "right")
        cityText.put("text_alignment_vertical", "top")
        cityText.put("width", JSONObject().apply {
            put("type", "wrap_content")
        })
        cityText.put("height", JSONObject().apply {
            put("type", "wrap_content")
        })
        cityText.put("alignment_horizontal", "center")
        cityText.put("alignment_vertical", "center")
        items.put(cityText)
        
        template.put("items", items)
        
        // Add background
        template.put("background", JSONArray().apply {
            put(JSONObject().apply {
                put("type", "solid")
                put("color", "#ffffff")
            })
        })
        
        // Add border
        template.put("border", JSONObject().apply {
            put("corners_radius", JSONObject().apply {
                put("top-left", 20)
                put("top-right", 20)
                put("bottom-left", 20)
                put("bottom-right", 20)
            })
        })
        
        // Add height and width
        template.put("height", JSONObject().apply {
            put("type", "wrap_content")
        })
        template.put("width", JSONObject().apply {
            put("type", "match_parent")
        })
        
        // Add orientation and alignment
        template.put("orientation", "vertical")
        template.put("content_alignment_horizontal", "center")
        template.put("content_alignment_vertical", "center")
        
        // Add paddings
        template.put("paddings", JSONObject().apply {
            put("top", 8)
            put("bottom", 8)
            put("left", 12)
            put("right", 12)
        })
        
        // Add alignment
        template.put("alignment_horizontal", "right")
        template.put("alignment_vertical", "center")
        
        // Add margins
        template.put("margins", JSONObject().apply {
            put("top", 8)
            put("bottom", 8)
        })
        
        Log.d(tag, "Text template definition created: $template")
        return template
    }

    /**
     * Creates a custom_template definition.
     */
    private fun createCustomTemplateDefinition(): JSONObject {
        Log.d(tag, "=== CREATING CUSTOM TEMPLATE DEFINITION ===")
        
        val template = JSONObject()
        template.put("type", "custom")
        template.put("custom_type", "timer_button")
        
        // Add custom_props
        template.put("custom_props", JSONObject().apply {
            put("${'$'}seconds", "seconds")
            put("next_page", "op/guide")
        })
        
        // Add background
        template.put("background", JSONArray().apply {
            put(JSONObject().apply {
                put("type", "solid")
                put("color", "#ffffff")
            })
        })
        
        // Add border
        template.put("border", JSONObject().apply {
            put("corner_radius", 4)
            put("stroke", JSONObject().apply {
                put("color", "#d9e0e9")
                put("width", 1)
            })
        })
        
        // Add height and width
        template.put("height", JSONObject().apply {
            put("type", "wrap_content")
        })
        template.put("width", JSONObject().apply {
            put("type", "match_parent")
        })
        
        // Add orientation and alignment
        template.put("orientation", "overlap")
        template.put("content_alignment_vertical", "center")
        template.put("content_alignment_horizontal", "center")
        template.put("alignment_horizontal", "center")
        template.put("alignment_vertical", "bottom")
        
        // Add margins
        template.put("margins", JSONObject().apply {
            put("top", 4)
            put("right", 16)
            put("bottom", 4)
            put("left", 16)
        })
        
        Log.d(tag, "Custom template definition created: $template")
        return template
    }

    /**
     * Generates a template item with data values.
     */
    private fun generateTemplateItem(templateName: String, dataItem: JSONObject): JSONObject {
        Log.d(tag, "=== GENERATING TEMPLATE ITEM ===")
        Log.d(tag, "templateName: $templateName")
        Log.d(tag, "dataItem: $dataItem")
        
        // For text_template, create a simple object with type and data properties
        if (templateName == "text_template") {
            val item = JSONObject()
            item.put("type", "text_template")
            
            // Add the data properties directly
            if (dataItem.has("name")) {
                item.put("name", dataItem.getString("name"))
            }
            if (dataItem.has("family")) {
                item.put("family", dataItem.getString("family"))
            }
            if (dataItem.has("city")) {
                item.put("city", dataItem.getString("city"))
            }
            
            Log.d(tag, "Generated text_template item: $item")
            return item
        }
        
        // For custom_template, use the template definition approach
        val templateDefinition = when (templateName) {
            "custom_template" -> createCustomTemplateDefinition()
            else -> createTextTemplateDefinition() // Default to text template
        }
        Log.d(tag, "Template definition: $templateDefinition")
        
        // Create a copy of the template definition
        val item = JSONObject(templateDefinition.toString())
        Log.d(tag, "Initial item created from template: $item")
        
        // Replace template variables with actual data
        Log.d(tag, "Replacing template variables...")
        replaceTemplateVariables(item, dataItem)
        
        Log.d(tag, "Final template item: $item")
        return item
    }

    /**
     * Recursively replaces template variables in a JSON object.
     */
    private fun replaceTemplateVariables(item: JSONObject, dataItem: JSONObject) {
        Log.d(tag, "=== REPLACING TEMPLATE VARIABLES ===")
        Log.d(tag, "Original item: $item")
        Log.d(tag, "Data item: $dataItem")
        
        val keysToRemove = mutableListOf<String>()
        val keysToAdd = mutableMapOf<String, Any>()
        
        val keys = item.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = item.get(key)
            
            Log.d(tag, "Processing key: '$key' with value: '$value'")
            
            when (value) {
                is String -> {
                    // Check if this is a template variable (starts with $)
                    if (key.startsWith("$")) {
                        val variableName = value // The value itself is the variable name in dataItem
                        Log.d(tag, "Found template variable: '$key', variableName: '$variableName'")
                        
                        if (dataItem.has(variableName)) {
                            // Store the replacement for later
                            keysToRemove.add(key)
                            val newKey = key.substring(1) // Remove $ from key name
                            val newValue = dataItem.getString(variableName)
                            keysToAdd[newKey] = newValue
                            Log.d(tag, "Will replace: '$key' -> '$newKey' = '$newValue'")
                        } else {
                            Log.w(tag, "Variable '$variableName' not found in data item")
                        }
                    }
                }
                is JSONObject -> {
                    // Recursively process nested objects
                    Log.d(tag, "Recursively processing nested JSONObject for key: $key")
                    replaceTemplateVariables(value, dataItem)
                }
                is JSONArray -> {
                    // Recursively process arrays
                    Log.d(tag, "Recursively processing JSONArray for key: $key")
                    for (i in 0 until value.length()) {
                        val arrayItem = value.get(i)
                        if (arrayItem is JSONObject) {
                            replaceTemplateVariables(arrayItem, dataItem)
                        }
                    }
                }
            }
        }
        
        // Apply the changes after iteration
        Log.d(tag, "Keys to remove: $keysToRemove")
        Log.d(tag, "Keys to add: $keysToAdd")
        
        keysToRemove.forEach { key ->
            item.remove(key)
            Log.d(tag, "Removed key: $key")
        }
        keysToAdd.forEach { (key, value) ->
            item.put(key, value)
            Log.d(tag, "Added key: $key = $value")
        }
        
        Log.d(tag, "Final item after replacement: $item")
    }

    /**
     * Saves the patch to the database.
     */
    private fun savePatchToDatabase(patchName: String, patchJson: String) {
        Log.d(tag, "=== SAVING PATCH TO DATABASE ===")
        Log.d(tag, "patchName: $patchName")
        Log.d(tag, "patchJson: $patchJson")
        
        mehdiViewModel?.insertItemToDb(PhPlusDB(null, patchName, patchJson))
        Log.d(tag, "Patch saved to database with name: $patchName")
    }

    /**
     * Fetches a patch from the database.
     */
    fun fetchPatchFromDatabase(patchName: String): String? {
        Log.d(tag, "=== FETCHING PATCH FROM DATABASE ===")
        Log.d(tag, "patchName: $patchName")
        
        val result = mehdiViewModel?.getValueByKey(patchName)?.value
        Log.d(tag, "Fetched patch: $result")
        return result
    }

    /**
     * Test method to verify patch generation.
     */
    fun testPatchGeneration() {
        Log.d(tag, "=== TEST PATCH GENERATION ===")
        
        val testData = """[
            {"name": "Alice", "family": "Johnson", "city": "New York", "seconds": 10},
            {"name": "Bob", "family": "Smith", "city": "Los Angeles", "seconds": 20}
        ]"""
        
        Log.d(tag, "Test data: $testData")
        
        val success = generatePatch(
            containerId = "information_container",
            templateName = "text_template",
            patchName = "test_patch",
            dataJson = testData
        )
        
        Log.d(tag, "Test patch generation: ${if (success) "SUCCESS" else "FAILED"}")
        
        if (success) {
            val fetchedPatch = fetchPatchFromDatabase("test_patch")
            Log.d(tag, "Fetched test patch: $fetchedPatch")
        }
    }
} 