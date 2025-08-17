//package com.yandex.divkit.demo.utils
//
//import android.content.Context
//import android.util.Log
//import org.json.JSONArray
//import org.json.JSONObject
//
///**
// * Example usage of ScreenGenerator class
// * This class demonstrates how to use ScreenGenerator to create dynamic screens
// */
//class ScreenGeneratorExample(private val context: Context) {
//
//    private val screenGenerator = ScreenGenerator(context)
//    private val tag = "ScreenGeneratorExample"
//
//    /**
//     * Example data JSON as provided in the requirements
//     */
//    private val exampleDataJson = """
//        [
//          {
//            "name": "Alice",
//            "family": "Johnson",
//            "city": "New York"
//          },
//          {
//            "name": "Mohammed",
//            "family": "Khan",
//            "city": "London"
//          },
//          {
//            "name": "Sophie",
//            "family": "Lemoine",
//            "city": "Paris"
//          },
//          {
//            "name": "Kenji",
//            "family": "Takahashi",
//            "city": "Tokyo"
//          },
//          {
//            "name": "Carlos",
//            "family": "Martínez",
//            "city": "Madrid"
//          },
//          {
//            "name": "Fatima",
//            "family": "Ali",
//            "city": "Cairo"
//          },
//          {
//            "name": "Liam",
//            "family": "O'Reilly",
//            "city": "Dublin"
//          },
//          {
//            "name": "Chen",
//            "family": "Wang",
//            "city": "Beijing"
//          },
//          {
//            "name": "Isabella",
//            "family": "Rossi",
//            "city": "Rome"
//          },
//          {
//            "name": "Ava",
//            "family": "Smith",
//            "city": "Toronto"
//          }
//        ]
//    """.trimIndent()
//
//    /**
//     * Example screen JSON template as provided in the requirements
//     */
//    private val exampleScreenJson = """
//        {
//          "card": {
//            "log_id": "div2_sample_card",
//            "states": [
//              {
//                "state_id": 0,
//                "div": {
//                  "type": "gallery",
//                  "items": [
//                    {
//                      "type": "container",
//                      "width": {
//                        "type": "match_parent"
//                      },
//                      "height": {
//                        "type": "wrap_content"
//                      },
//                      "items": [
//                        {
//                          "type": "text",
//                          "text": "سنجش احساس امنیت و سرمایه اجتماعی ",
//                          "font_size": 20,
//                          "width": {
//                            "type": "match_parent"
//                          },
//                          "text_alignment_horizontal": "center",
//                          "height": {
//                            "type": "fixed",
//                            "value": 36
//                          },
//                          "actions": [
//                            {
//                              "log_id": "action_id",
//                              "url": "https://",
//                              "log_url": "@{on_click_log_url}"
//                            }
//                          ],
//                          "margins": {
//                            "top": 20,
//                            "bottom": 10
//                          }
//                        },
//                        {
//                          "type": "separator",
//                          "width": {
//                            "type": "fixed",
//                            "value": 483
//                          },
//                          "height": {
//                            "type": "fixed",
//                            "value": 17
//                          },
//                          "delimiter_style": {
//                            "color": "#ececec"
//                          },
//                          "alignment_horizontal": "center",
//                          "margins": {
//                            "bottom": 30
//                          }
//                        },
//                        {
//                          "type": "text",
//                          "text": "@{text___1}",
//                          "font_size": 16,
//                          "letter_spacing": 0,
//                          "text_color": "#121212",
//                          "text_alignment_horizontal": "right",
//                          "text_alignment_vertical": "top",
//                          "width": {
//                            "type": "match_parent"
//                          },
//                          "height": {
//                            "type": "fixed",
//                            "value": 39
//                          },
//                          "background": [
//                            {
//                              "type": "solid",
//                              "color": "#ffffff"
//                            }
//                          ],
//                          "alignment_horizontal": "right",
//                          "margins": {
//                            "right": 35,
//                            "left": 35
//                          }
//                        },
//                        {
//                          "type": "text",
//                          "text": "@{text___2}",
//                          "ranges": [
//                            {
//                              "start": 9,
//                              "end": 185
//                            },
//                            {
//                              "start": 212,
//                              "end": 547
//                            },
//                            {
//                              "start": 564,
//                              "end": 823
//                            },
//                            {
//                              "start": 841,
//                              "end": 1116
//                            },
//                            {
//                              "start": 1131,
//                              "end": 1351
//                            },
//                            {
//                              "start": 1368,
//                              "end": 1526
//                            },
//                            {
//                              "start": 1545,
//                              "end": 1714
//                            }
//                          ],
//                          "text_alignment_horizontal": "right",
//                          "text_alignment_vertical": "top",
//                          "width": {
//                            "type": "match_parent"
//                          },
//                          "height": {
//                            "type": "match_parent"
//                          },
//                          "margins": {
//                            "top": 8,
//                            "right": 30,
//                            "left": 30
//                          },
//                          "font_size": 13,
//                          "line_height": 24,
//                          "letter_spacing": 0,
//                          "text_color": "#353535",
//                          "background": [
//                            {
//                              "type": "solid",
//                              "color": "#ffffff"
//                            }
//                          ]
//                        }
//                      ],
//                      "background": [
//                        {
//                          "type": "solid",
//                          "color": "#ffffff"
//                        }
//                      ]
//                    },
//                    {
//                      "type": "container",
//                      "width": {
//                        "type": "match_parent"
//                      },
//                      "height": {
//                        "type": "fixed",
//                        "value": 100
//                      },
//                      "items": [
//                        {
//                          "type": "text",
//                          "text": "\\n",
//                          "width": {
//                            "type": "fixed",
//                            "value": 100
//                          },
//                          "alignment_horizontal": "center"
//                        }
//                      ],
//                      "margins": {
//                        "top": 8,
//                        "bottom": 8,
//                        "start": 30,
//                        "end": 30
//                      }
//                    },
//                    {
//                      "type": "container",
//                      "width": {
//                        "type": "match_parent"
//                      },
//                      "height": {
//                        "type": "wrap_content"
//                      },
//                      "items": [
//                        {
//                          "type": "text",
//                          "text": "\\n\\nپذیرش شرایط  \\n\\nبا امضای این سند یا تائید کتبی /الکترونیکی ، شما متعهد می شوید که تمامی شرایط و ضوابط فوق را رعایت کنید . ",
//                          "width": {
//                            "type": "match_parent"
//                          },
//                          "ranges": [
//                            {
//                              "start": 16,
//                              "end": 123
//                            }
//                          ],
//                          "alignment_horizontal": "center",
//                          "height": {
//                            "type": "wrap_content"
//                          },
//                          "text_alignment_horizontal": "right",
//                          "font_size": 15,
//                          "margins": {
//                            "right": 30,
//                            "left": 30
//                          }
//                        }
//                      ]
//                    },
//                    {
//                      "type": "container",
//                      "id": "information_container",
//                      "width": {
//                        "type": "match_parent"
//                      },
//                      "height": {
//                        "type": "wrap_content"
//                      },
//                      "items": [
//                        {
//                          "type": "text_template",
//                          "name": "mahdi",
//                          "family": "kanafchian",
//                          "city": "tehran"
//                        }
//                      ],
//                      "margins": {
//                        "top": 12
//                      }
//                    },
//                    {
//                      "type": "custom_template",
//                      "seconds": "10"
//                    }
//                  ],
//                  "height": {
//                    "type": "match_parent"
//                  },
//                  "orientation": "vertical"
//                }
//              }
//            ],
//            "variables": [
//              {
//                "type": "dict",
//                "name": "local_palette",
//                "value": {
//                  "bg_primary": {
//                    "name": "Primary background",
//                    "light": "#fff",
//                    "dark": "#000"
//                  },
//                  "color0": {
//                    "name": "Secondary background",
//                    "light": "#eeeeee",
//                    "dark": "#000"
//                  }
//                }
//              },
//              {
//                "type": "string",
//                "name": "text___1",
//                "value": "شرایط و ضوابط"
//              },
//              {
//                "type": "string",
//                "name": "text___2",
//                "value": "هدف پروژه  این پروژه با هدف سنجش احساس امنیت و سرمایه اجتماعی در جامعه اجرا می‌شود. داده‌های جمع‌آوری‌شده برای اهداف"
//              },
//              {
//                "type": "boolean",
//                "name": "index1",
//                "value": false
//              },
//              {
//                "type": "string",
//                "name": "checked",
//                "value": "divkit-asset://checkbox-checked.png"
//              },
//              {
//                "type": "string",
//                "name": "unchecked",
//                "value": "divkit-asset://checkbox-unchecked.png"
//              },
//              {
//                "type": "string",
//                "name": "index1_text_color",
//                "value": "#757575"
//              },
//              {
//                "type": "string",
//                "name": "index1_color",
//                "value": "#e8ecf4"
//              },
//              {
//                "type": "string",
//                "name": "seconds",
//                "value": "10"
//              }
//            ]
//          },
//          "templates": {
//            "custom_template": {
//              "type": "custom",
//              "custom_type": "timer_button",
//              "custom_props": {
//                "$seconds": "seconds",
//                "next_page": "op/guide"
//              },
//              "background": [
//                {
//                  "type": "solid",
//                  "color": "#ffffff"
//                }
//              ],
//              "border": {
//                "corner_radius": 4,
//                "stroke": {
//                  "color": "#d9e0e9",
//                  "width": 1
//                }
//              },
//              "height": {
//                "type": "wrap_content"
//              },
//              "width": {
//                "type": "match_parent"
//              },
//              "orientation": "overlap",
//              "content_alignment_vertical": "center",
//              "content_alignment_horizontal": "center",
//              "alignment_horizontal": "center",
//              "alignment_vertical": "bottom",
//              "margins": {
//                "top": 4,
//                "right": 16,
//                "bottom": 4,
//                "left": 16
//              }
//            },
//            "text_template": {
//              "type": "container",
//              "items": [
//                {
//                  "type": "text",
//                  "$text": "name",
//                  "font_size": 14,
//                  "line_height": 32,
//                  "letter_spacing": 0,
//                  "text_color": "#000000",
//                  "text_alignment_horizontal": "right",
//                  "text_alignment_vertical": "top",
//                  "width": {
//                    "type": "wrap_content"
//                  },
//                  "height": {
//                    "type": "wrap_content"
//                  },
//                  "alignment_horizontal": "center",
//                  "alignment_vertical": "center"
//                },
//                {
//                  "type": "text",
//                  "$text": "family",
//                  "font_size": 14,
//                  "line_height": 32,
//                  "letter_spacing": 0,
//                  "text_color": "#000000",
//                  "text_alignment_horizontal": "right",
//                  "text_alignment_vertical": "top",
//                  "width": {
//                    "type": "wrap_content"
//                  },
//                  "height": {
//                    "type": "wrap_content"
//                  },
//                  "alignment_horizontal": "center",
//                  "alignment_vertical": "center"
//                },
//                {
//                  "type": "text",
//                  "$text": "city",
//                  "font_size": 14,
//                  "line_height": 32,
//                  "letter_spacing": 0,
//                  "text_color": "#000000",
//                  "text_alignment_horizontal": "right",
//                  "text_alignment_vertical": "top",
//                  "width": {
//                    "type": "wrap_content"
//                  },
//                  "height": {
//                    "type": "wrap_content"
//                  },
//                  "alignment_horizontal": "center",
//                  "alignment_vertical": "center"
//                }
//              ],
//              "background": [
//                {
//                  "type": "solid",
//                  "color": "#ffffff"
//                }
//              ],
//              "border": {
//                "corners_radius": {
//                  "top-left": 20,
//                  "top-right": 20,
//                  "bottom-left": 20,
//                  "bottom-right": 20
//                }
//              },
//              "height": {
//                "type": "wrap_content"
//              },
//              "width": {
//                "type": "match_parent"
//              },
//              "orientation": "vertical",
//              "content_alignment_horizontal": "center",
//              "content_alignment_vertical": "center",
//              "paddings": {
//                "top": 8,
//                "bottom": 8,
//                "left": 12,
//                "right": 12
//              },
//              "alignment_horizontal": "right",
//              "alignment_vertical": "center",
//              "margins": {
//                "top": 8,
//                "bottom": 8
//              }
//            }
//          }
//        }
//    """.trimIndent()
//
//    /**
//     * Demonstrates how to use ScreenGenerator
//     */
//    fun demonstrateScreenGeneration() {
//        try {
//            Log.d(tag, "Starting screen generation demonstration")
//
//            // 1. First, save the screen template to database
//            saveScreenTemplate()
//
//            // 2. Generate the screen with data
//            val generatedScreen = screenGenerator.generateScreen(
//                containerId = "information_container",
//                templateName = "text_template",
//                screenName = "sample_screen",
//                dataJson = exampleDataJson
//            )
//
//            if (generatedScreen.isNotEmpty()) {
//                Log.d(tag, "Screen generation successful!")
//                Log.d(tag, "Generated screen length: ${generatedScreen.length}")
//
//                // 3. Parse and verify the generated screen
//                verifyGeneratedScreen(generatedScreen)
//            } else {
//                Log.e(tag, "Screen generation failed!")
//            }
//
//        } catch (e: Exception) {
//            Log.e(tag, "Error in demonstration: ${e.message}", e)
//        }
//    }
//
//    /**
//     * Saves the screen template to database
//     */
//    private fun saveScreenTemplate() {
//        try {
//            // Save the screen template to database
//            screenGenerator.generateScreen(
//                containerId = "information_container",
//                templateName = "text_template",
//                screenName = "sample_screen",
//                dataJson = "[]" // Empty data to save template
//            )
//            Log.d(tag, "Screen template saved to database")
//        } catch (e: Exception) {
//            Log.e(tag, "Error saving screen template: ${e.message}")
//        }
//    }
//
//    /**
//     * Verifies the generated screen by parsing it
//     */
//    private fun verifyGeneratedScreen(generatedScreen: String) {
//        try {
//            val screenJson = JSONObject(generatedScreen)
//            val card = screenJson.getJSONObject("card")
//            val states = card.getJSONArray("states")
//            val state = states.getJSONObject(0)
//            val div = state.getJSONObject("div")
//            val items = div.getJSONArray("items")
//
//            // Find the information_container
//            var informationContainer: JSONObject? = null
//            for (i in 0 until items.length()) {
//                val item = items.getJSONObject(i)
//                if (item.has("id") && item.getString("id") == "information_container") {
//                    informationContainer = item
//                    break
//                }
//            }
//
//            if (informationContainer != null) {
//                val containerItems = informationContainer.getJSONArray("items")
//                Log.d(tag, "Generated ${containerItems.length()} template items")
//
//                // Verify each item has the correct structure
//                for (i in 0 until containerItems.length()) {
//                    val item = containerItems.getJSONObject(i)
//                    Log.d(tag, "Template item $i: ${item.toString()}")
//                }
//            } else {
//                Log.e(tag, "Information container not found in generated screen")
//            }
//
//        } catch (e: Exception) {
//            Log.e(tag, "Error verifying generated screen: ${e.message}")
//        }
//    }
//
//    /**
//     * Example using List of Maps instead of JSON string
//     */
//    fun demonstrateWithListOfMaps() {
//        val dataList = listOf(
//            mapOf("name" to "Alice", "family" to "Johnson", "city" to "New York"),
//            mapOf("name" to "Bob", "family" to "Smith", "city" to "Los Angeles"),
//            mapOf("name" to "Charlie", "family" to "Brown", "city" to "Chicago")
//        )
//
//        val generatedScreen = screenGenerator.generateScreen(
//            containerId = "information_container",
//            templateName = "text_template",
//            screenName = "list_example_screen",
//            dataList = dataList
//        )
//
//        Log.d(tag, "Generated screen with List of Maps: ${generatedScreen.length} characters")
//    }
//}