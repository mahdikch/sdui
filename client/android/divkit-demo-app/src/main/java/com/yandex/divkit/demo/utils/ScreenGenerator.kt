package com.yandex.divkit.demo.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.yandex.divkit.demo.data.SharePref
import com.yandex.divkit.demo.data.EncryptionConstant.ConstantSharedPreferences.SHARED_PREFS_NAME
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ScreenGenerator(private val context: Context, private val mehdiViewModel: MehdiViewModel?) {

//    private val sharePref: SharePref
//
//    init {
//        val sharedPreferences =
//            context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
//        sharePref = SharePref(sharedPreferences)
//    }

    private val tag = "ScreenGenerator"

    /**
     * Generates a screen from data JSON and screen template
     * @param containerId The ID of the container where templates will be placed
     * @param templateName The name of the template to use
     * @param screenName The name of the screen to fetch from database
     * @param dataJson The JSON array containing data items
     * @return The generated screen JSON as string
     */
    fun generateScreen(
        containerId: String,
        templateName: String,
        screenName: String,
        dataJson: String
    ): String {
        try {
            Log.d(tag, "Starting screen generation for screen: $screenName")

            // 1. Fetch the screen template from database
            val screenTemplate = fetchScreenFromDatabase(screenName)
            if (screenTemplate == null) {
                Log.e(tag, "Screen template not found: $screenName")
                return ""
            }

            // 2. Parse the data JSON
            val dataArray = JSONArray(dataJson)
            Log.d(tag, "Data array size: ${dataArray.length()}")

            // 3. Generate the new screen JSON
            val generatedScreen = generateScreenFromTemplate(
                screenTemplate,
                containerId,
                templateName,
                dataArray
            )

            // 4. Save the generated screen to database
            saveScreenToDatabase(screenName, generatedScreen)

            Log.d(tag, "Screen generation completed successfully:${generatedScreen}")
            return generatedScreen

        } catch (e: Exception) {
            Log.e(tag, "Error generating screen: ${e.message}", e)
            return ""
        }
    }

    /**
     * Fetches screen template from database
     */
    private fun fetchScreenFromDatabase(screenName: String): String? {
        return try {
            Log.d(tag, "Fetching screen from database with key: '$screenName'")
            val result = mehdiViewModel?.getValueByKey(screenName)?.value
            Log.d(tag, "Database result for '$screenName': $result")
            result
        } catch (e: Exception) {
            Log.e(tag, "Error fetching screen from database: ${e.message}")
            null
        }
    }

    /**
     * Saves screen to database
     */
    private fun saveScreenToDatabase(screenName: String, screenJson: String) {
        try {
            // Assuming SharePref has a method to save screen
            // You may need to adjust this based on your actual database implementation
            mehdiViewModel?.insertItemToDb(PhPlusDB(null, screenName, screenJson))
            Log.d(tag, "Screen saved to database: $screenName")
        } catch (e: Exception) {
            Log.e(tag, "Error saving screen to database: ${e.message}")
        }
    }

    /**
     * Generates screen from template by replacing container items with data
     */
    private fun generateScreenFromTemplate(
        screenTemplate: String,
        containerId: String,
        templateName: String,
        dataArray: JSONArray
    ): String {
        Log.d(tag, "generateScreenFromTemplate called with:")
        Log.d(tag, "containerId: $containerId")
        Log.d(tag, "templateName: $templateName")
        Log.d(tag, "dataArray length: ${dataArray.length()}")
        
        val screenJson = JSONObject(screenTemplate)
        Log.d(tag, "Screen template loaded successfully")

        // Find the container with the specified ID
        val container = findContainerById(screenJson, containerId)
        if (container == null) {
            Log.e(tag, "Container with ID '$containerId' not found")
            return screenTemplate
        }
        Log.d(tag, "Container found: $container")

        // Get the template definition
        val template = getTemplateDefinition(screenJson, templateName)
        if (template == null) {
            Log.e(tag, "Template '$templateName' not found")
            return screenTemplate
        }
        Log.d(tag, "Template found: $template")

        // Clear existing items in the container
        container.put("items", JSONArray())

        // Generate items for each data entry
        for (i in 0 until dataArray.length()) {
            val dataItem = dataArray.getJSONObject(i)
            Log.d(tag, "Processing data item $i: $dataItem")
            val generatedItem = generateTemplateItem(template, dataItem, templateName)
            container.getJSONArray("items").put(generatedItem)
        }

        Log.d(tag, "Generated ${dataArray.length()} template items")
        return screenJson.toString()
    }

    /**
     * Finds container by ID in the screen JSON
     */
    private fun findContainerById(screenJson: JSONObject, containerId: String): JSONObject? {
        try {
            val card = screenJson.getJSONObject("card")
            val states = card.getJSONArray("states")

            for (i in 0 until states.length()) {
                val state = states.getJSONObject(i)
                val div = state.getJSONObject("div")
                val items = div.getJSONArray("items")

                for (j in 0 until items.length()) {
                    val item = items.getJSONObject(j)
                    if (item.has("id") && item.getString("id") == containerId) {
                        return item
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error finding container: ${e.message}")
        }
        return null
    }

    /**
     * Gets template definition from templates section
     */
    private fun getTemplateDefinition(screenJson: JSONObject, templateName: String): JSONObject? {
        return try {
            Log.d(tag, "Getting template definition for: $templateName")
            val templates = screenJson.getJSONObject("templates")
            Log.d(tag, "Templates section: $templates")
            
            if (templates.has(templateName)) {
                val template = templates.getJSONObject(templateName)
                Log.d(tag, "Template found: $template")
                template
            } else {
                Log.e(tag, "Template '$templateName' not found in templates section")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting template definition: ${e.message}")
            null
        }
    }

    /**
     * Generates a template item by replacing variables with data values
     */
    private fun generateTemplateItem(
        template: JSONObject,
        dataItem: JSONObject,
        templateName: String
    ): JSONObject {
        val generatedItem = JSONObject(template.toString())

        // Replace template variables with actual data
        replaceTemplateVariables(generatedItem, dataItem)

        return generatedItem
    }

    /**
     * Recursively replaces template variables with data values
     */
    private fun replaceTemplateVariables(item: JSONObject, dataItem: JSONObject) {
        Log.d(tag, "replaceTemplateVariables called with item: $item")
        Log.d(tag, "dataItem: $dataItem")
        
        val keys = item.keys()
        val keysToRemove = mutableListOf<String>()
        val keysToAdd = mutableMapOf<String, Any>()
        
        while (keys.hasNext()) {
            val key = keys.next()
            val value = item.get(key)
            
            Log.d(tag, "Processing key: '$key' with value: '$value'")

            when (value) {
                is String -> {
                    // Check if this is a template variable (key starts with $)
                    if (key.startsWith("$")) {
                        val variableName = value // The value itself is the variable name in dataItem
                        Log.d(tag, "Found template variable: key='$key', variableName='$variableName'")
                        
                        if (dataItem.has(variableName)) {
                            // Store the replacement for later
                            keysToRemove.add(key)
                            val newKey = key.substring(1) // Remove $ from key name
                            keysToAdd[newKey] = dataItem.getString(variableName)
                            Log.d(tag, "Will replace: '$key' -> '$newKey' = '${dataItem.getString(variableName)}'")
                        } else {
                            Log.d(tag, "Variable '$variableName' not found in dataItem")
                        }
                    }
                }

                is JSONObject -> {
                    Log.d(tag, "Recursively processing JSONObject for key: '$key'")
                    replaceTemplateVariables(value, dataItem)
                }

                is JSONArray -> {
                    Log.d(tag, "Processing JSONArray for key: '$key' with ${value.length()} items")
                    for (i in 0 until value.length()) {
                        val arrayItem = value.getJSONObject(i)
                        replaceTemplateVariables(arrayItem, dataItem)
                    }
                }
            }
        }
        
        Log.d(tag, "Keys to remove: $keysToRemove")
        Log.d(tag, "Keys to add: $keysToAdd")
        
        // Apply the replacements
        for (keyToRemove in keysToRemove) {
            item.remove(keyToRemove)
            Log.d(tag, "Removed key: '$keyToRemove'")
        }
        for ((newKey, newValue) in keysToAdd) {
            item.put(newKey, newValue)
            Log.d(tag, "Added key: '$newKey' = '$newValue'")
        }
        
        Log.d(tag, "Final item after replacement: $item")
    }

    /**
     * Test method to verify template variable replacement logic
     */
    fun testTemplateReplacement() {
        Log.d(tag, "=== Testing Template Replacement ===")
        
        // Create a simple template similar to text_template
        val templateJson = """
        {
          "type": "container",
          "items": [
            {
              "type": "text",
              "${'$'}text": "name",
              "font_size": 14
            },
            {
              "type": "text", 
              "${'$'}text": "family",
              "font_size": 14
            },
            {
              "type": "text",
              "${'$'}text": "city", 
              "font_size": 14
            }
          ]
        }
        """.trimIndent()
        
        val template = JSONObject(templateJson)
        
        // Create test data
        val dataJson = """
        {
          "name": "Alice",
          "family": "Johnson", 
          "city": "New York"
        }
        """.trimIndent()
        
        val dataItem = JSONObject(dataJson)
        
        Log.d(tag, "Original template: $template")
        Log.d(tag, "Data item: $dataItem")
        
        // Test the replacement
        val result = JSONObject(template.toString())
        replaceTemplateVariables(result, dataItem)
        
        Log.d(tag, "Result after replacement: $result")
        Log.d(tag, "=== End Test ===")
    }

    /**
     * Test method to verify database fetch
     */
    fun testDatabaseFetch(screenName: String) {
        Log.d(tag, "=== Testing Database Fetch ===")
        Log.d(tag, "Fetching screen with name: $screenName")
        
        val result = fetchScreenFromDatabase(screenName)
        Log.d(tag, "Database result: $result")
        
        if (result != null) {
            try {
                val screenJson = JSONObject(result)
                Log.d(tag, "Screen JSON parsed successfully")
                
                // Check if it has templates section
                if (screenJson.has("templates")) {
                    val templates = screenJson.getJSONObject("templates")
                    Log.d(tag, "Templates section found: $templates")
                    
                    // Check for text_template
                    if (templates.has("text_template")) {
                        val textTemplate = templates.getJSONObject("text_template")
                        Log.d(tag, "text_template found: $textTemplate")
                    } else {
                        Log.e(tag, "text_template not found in templates")
                    }
                } else {
                    Log.e(tag, "No templates section found in screen JSON")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error parsing screen JSON: ${e.message}")
            }
        } else {
            Log.e(tag, "No screen found in database for key: $screenName")
        }
        
        Log.d(tag, "=== End Database Test ===")
    }

    /**
     * Save test.json to database for testing
     */
    fun saveTestJsonToDatabase() {
        Log.d(tag, "=== Saving test.json to database ===")
        
        try {
            // Read test.json from assets
            val testJson = context.assets.open("application/test.json").bufferedReader().use { it.readText() }
            Log.d(tag, "Read test.json from assets")
            
            // Save to database with key "ph/test"
            saveScreenToDatabase("ph/test", testJson)
            Log.d(tag, "Saved test.json to database with key 'ph/test'")
            
        } catch (e: Exception) {
            Log.e(tag, "Error saving test.json to database: ${e.message}")
        }
        
        Log.d(tag, "=== End Save Test ===")
    }

    /**
     * Comprehensive test of the screen generation process
     */
    fun testFullScreenGeneration() {
        Log.d(tag, "=== Testing Full Screen Generation ===")
        
        try {
            // 1. First, save test.json to database
            saveTestJsonToDatabase()
            
            // 2. Test data
            val testDataJson = """
            [
              {
                "name": "Alice",
                "family": "Johnson",
                "city": "New York"
              },
              {
                "name": "Bob",
                "family": "Smith",
                "city": "Los Angeles"
              }
            ]
            """.trimIndent()
            
            // 3. Generate screen
            val generatedScreen = generateScreen(
                containerId = "information_container",
                templateName = "text_template",
                screenName = "ph/test",
                dataJson = testDataJson
            )
            
            Log.d(tag, "Generated screen length: ${generatedScreen.length}")
            Log.d(tag, "Generated screen: $generatedScreen")
            
            // 4. Parse and verify the result
            if (generatedScreen.isNotEmpty()) {
                val screenJson = JSONObject(generatedScreen)
                val card = screenJson.getJSONObject("card")
                val states = card.getJSONArray("states")
                val state = states.getJSONObject(0)
                val div = state.getJSONObject("div")
                val items = div.getJSONArray("items")
                
                // Find information_container
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    if (item.has("id") && item.getString("id") == "information_container") {
                        val containerItems = item.getJSONArray("items")
                        Log.d(tag, "Found information_container with ${containerItems.length()} items")
                        
                        for (j in 0 until containerItems.length()) {
                            val containerItem = containerItems.getJSONObject(j)
                            Log.d(tag, "Container item $j: $containerItem")
                        }
                        break
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(tag, "Error in full screen generation test: ${e.message}", e)
        }
        
        Log.d(tag, "=== End Full Screen Generation Test ===")
    }

    /**
     * Convenience method to generate screen with JSONObject data
     */
    fun generateScreen(
        containerId: String,
        templateName: String,
        screenName: String,
        dataArray: JSONArray
    ): String {
        return generateScreen(containerId, templateName, screenName, dataArray.toString())
    }

    /**
     * Convenience method to generate screen with List of Maps
     */
    fun generateScreen(
        containerId: String,
        templateName: String,
        screenName: String,
        dataList: List<Map<String, Any>>
    ): String {
        val jsonArray = JSONArray()
        for (item in dataList) {
            val jsonObject = JSONObject()
            for ((key, value) in item) {
                jsonObject.put(key, value)
            }
            jsonArray.put(jsonObject)
        }
        return generateScreen(containerId, templateName, screenName, jsonArray)
    }
} 