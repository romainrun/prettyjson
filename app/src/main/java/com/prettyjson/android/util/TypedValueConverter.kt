package com.prettyjson.android.util

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException

/**
 * Utility for converting string values to typed JSON values based on value type
 */
object TypedValueConverter {
    /**
     * Convert a string value to the appropriate JSON type based on valueType
     * @param value The string value to convert
     * @param valueType The type: json, array, string, integer, float, boolean, null
     * @return The converted value as Any (JSONObject, JSONArray, String, Number, Boolean, or JSONObject.NULL)
     */
    fun convertValue(value: String, valueType: String): Any {
        val trimmed = value.trim()
        
        return when (valueType.lowercase()) {
            "json" -> {
                try {
                    JSONObject(trimmed)
                } catch (e: Exception) {
                    // If not an object, try as string
                    trimmed
                }
            }
            "array" -> {
                try {
                    JSONArray(trimmed)
                } catch (e: Exception) {
                    // Try to parse as comma-separated values
                    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                        JSONArray(trimmed)
                    } else {
                        // Parse comma-separated string
                        val items = trimmed.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        JSONArray().apply {
                            items.forEach { item ->
                                // Try to parse each item as appropriate type
                                when {
                                    item.startsWith("\"") && item.endsWith("\"") -> put(item.removeSurrounding("\""))
                                    item == "true" -> put(true)
                                    item == "false" -> put(false)
                                    item == "null" -> put(JSONObject.NULL)
                                    item.toIntOrNull() != null -> put(item.toInt())
                                    item.toDoubleOrNull() != null -> put(item.toDouble())
                                    else -> put(item)
                                }
                            }
                        }
                    }
                }
            }
            "string" -> {
                // Remove surrounding quotes if present
                if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                    trimmed.removeSurrounding("\"")
                } else {
                    trimmed
                }
            }
            "integer" -> {
                trimmed.toIntOrNull() ?: 0
            }
            "float" -> {
                trimmed.toDoubleOrNull() ?: 0.0
            }
            "boolean" -> {
                when (trimmed.lowercase()) {
                    "true", "1", "yes" -> true
                    "false", "0", "no" -> false
                    else -> false
                }
            }
            "null" -> {
                JSONObject.NULL
            }
            else -> {
                // Default: try to parse as JSON, fallback to string
                try {
                    if (trimmed.startsWith("{")) {
                        JSONObject(trimmed)
                    } else if (trimmed.startsWith("[")) {
                        JSONArray(trimmed)
                    } else {
                        trimmed
                    }
                } catch (e: Exception) {
                    trimmed
                }
            }
        }
    }
}

