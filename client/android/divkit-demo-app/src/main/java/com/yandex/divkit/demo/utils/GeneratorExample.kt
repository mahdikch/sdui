package com.yandex.divkit.demo.utils

import android.content.Context
import android.util.Log
import com.yandex.divkit.demo.ui.activity.MehdiViewModel

/**
 * Example usage of ScreenGenerator and PatchGenerator classes.
 * This file demonstrates how to use both generators for different purposes.
 */
class GeneratorExample(private val context: Context, private val mehdiViewModel: MehdiViewModel?) {
    
    companion object {
        private const val tag = "GeneratorExample"
    }
    
    /**
     * Example: Generate a complete screen with dynamic content
     */
    fun generateScreenExample() {
        val screenGenerator = ScreenGenerator(context, mehdiViewModel)
        
        val dataJson = """[
            {"name": "Alice", "family": "Johnson", "city": "New York"},
            {"name": "Bob", "family": "Smith", "city": "Los Angeles"},
            {"name": "Charlie", "family": "Brown", "city": "Chicago"}
        ]"""
        
        val generatedScreen = screenGenerator.generateScreen(
            containerId = "information_container",
            templateName = "text_template",
            screenName = "dynamic_screen",
            dataJson = dataJson
        )
        
        if (generatedScreen.isNotEmpty()) {
            Log.d(tag, "Screen generated successfully")
            // The screen is now saved in the database and can be loaded
        } else {
            Log.e(tag, "Failed to generate screen")
        }
    }
    
    /**
     * Example: Generate a patch to update existing content
     */
    fun generatePatchExample() {
        val patchGenerator = PatchGenerator(mehdiViewModel)
        
        val dataJson = """[
            {"name": "David", "family": "Wilson", "city": "Boston"},
            {"name": "Emma", "family": "Davis", "city": "Seattle"}
        ]"""
        
        val success = patchGenerator.generatePatch(
            containerId = "existing_container",
            templateName = "user_template",
            patchName = "user_update_patch",
            dataJson = dataJson
        )
        
        if (success) {
            Log.d(tag, "Patch generated successfully")
            // The patch can now be applied to update existing content
            val patchJson = patchGenerator.fetchPatchFromDatabase("user_update_patch")
            Log.d(tag, "Generated patch: $patchJson")
        } else {
            Log.e(tag, "Failed to generate patch")
        }
    }
    
    /**
     * Example: Using URL actions to trigger generation
     * 
     * For Screen Generation:
     * div-action://generateScreen?containerId=info_container&templateName=text_template&screenName=my_screen
     * 
     * For Patch Generation:
     * div-action://generatePatch?containerId=info_container&templateName=text_template&screenName=my_patch
     */
    fun demonstrateUrlActions() {
        Log.d(tag, "Screen Generation URL:")
        Log.d(tag, "div-action://generateScreen?containerId=info_container&templateName=text_template&screenName=my_screen")
        
        Log.d(tag, "Patch Generation URL:")
        Log.d(tag, "div-action://generatePatch?containerId=info_container&templateName=text_template&screenName=my_patch")
    }
    
    /**
     * Key differences between ScreenGenerator and PatchGenerator:
     * 
     * ScreenGenerator:
     * - Creates complete new screens
     * - Fetches base screen from database
     * - Replaces entire screen content
     * - Saves as complete screen JSON
     * - Used for initial screen creation
     * 
     * PatchGenerator:
     * - Creates patches to update existing screens
     * - Follows DivKit patch structure
     * - Updates specific containers by ID
     * - Saves as patch JSON with templates
     * - Used for dynamic content updates
     */
    fun explainDifferences() {
        Log.d(tag, "=== ScreenGenerator vs PatchGenerator ===")
        Log.d(tag, "ScreenGenerator: Creates complete new screens")
        Log.d(tag, "PatchGenerator: Creates patches to update existing content")
        Log.d(tag, "Use ScreenGenerator for initial screen creation")
        Log.d(tag, "Use PatchGenerator for dynamic content updates")
    }
} 