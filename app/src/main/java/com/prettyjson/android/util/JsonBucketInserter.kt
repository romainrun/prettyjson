package com.prettyjson.android.util

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException

/**
 * Utility for inserting data bucket content into existing JSON
 * Preserves JSON validity by intelligently merging content
 */
object JsonBucketInserter {
    /**
     * Insert bucket content into existing JSON
     * @param currentJson Current JSON string
     * @param bucketContent Data bucket content to insert
     * @return Merged JSON string that preserves validity
     */
    fun insertBucket(currentJson: String, bucketContent: String): String {
        val trimmedCurrent = currentJson.trim()
        val trimmedBucket = bucketContent.trim()
        
        // If current JSON is empty, just return bucket content
        if (trimmedCurrent.isEmpty()) {
            return trimmedBucket
        }
        
        // Try to parse both as JSON
        return try {
            val currentJsonObj = parseJson(trimmedCurrent)
            val bucketJsonObj = parseJson(trimmedBucket)
            
            when {
                // Both are objects -> merge intelligently
                currentJsonObj is JSONObject && bucketJsonObj is JSONObject -> {
                    mergeObjects(currentJsonObj, bucketJsonObj).toString(2)
                }
                // Current is object and bucket is array -> add array as field
                currentJsonObj is JSONObject && bucketJsonObj is JSONArray -> {
                    val mergedObj = JSONObject(currentJsonObj.toString())
                    var arrayKey = "data"
                    var counter = 1
                    while (mergedObj.has(arrayKey)) {
                        arrayKey = "data_$counter"
                        counter++
                    }
                    mergedObj.put(arrayKey, bucketJsonObj)
                    mergedObj.toString(2)
                }
                // Current is object and bucket is primitive -> add as field
                currentJsonObj is JSONObject && isPrimitive(bucketJsonObj) -> {
                    val mergedObj = JSONObject(currentJsonObj.toString())
                    val fieldName = generateFieldName(bucketJsonObj)
                    var newKey = fieldName
                    var counter = 1
                    while (mergedObj.has(newKey)) {
                        newKey = "${fieldName}_$counter"
                        counter++
                    }
                    mergedObj.put(newKey, bucketJsonObj)
                    mergedObj.toString(2)
                }
                // Current is array -> add bucket to array
                currentJsonObj is JSONArray -> {
                    addToArray(currentJsonObj, bucketJsonObj).toString(2)
                }
                // Bucket is array and current is primitive -> wrap current and merge
                bucketJsonObj is JSONArray && isPrimitive(currentJsonObj) -> {
                    val newArray = JSONArray().apply {
                        put(currentJsonObj)
                        for (i in 0 until bucketJsonObj.length()) {
                            put(bucketJsonObj.get(i))
                        }
                    }
                    newArray.toString(2)
                }
                // Both are primitives -> create array
                isPrimitive(currentJsonObj) && isPrimitive(bucketJsonObj) -> {
                    JSONArray().apply {
                        put(currentJsonObj)
                        put(bucketJsonObj)
                    }.toString(2)
                }
                // Default: wrap current in object and add bucket
                else -> {
                    try {
                        val mergedObj = if (currentJsonObj is JSONObject) {
                            JSONObject(currentJsonObj.toString())
                        } else {
                            JSONObject().apply { put("value", currentJsonObj) }
                        }
                        
                        if (bucketJsonObj is JSONObject) {
                            bucketJsonObj.keys().forEach { key ->
                                var newKey = key
                                var counter = 1
                                while (mergedObj.has(newKey)) {
                                    newKey = "${key}_$counter"
                                    counter++
                                }
                                mergedObj.put(newKey, bucketJsonObj.get(key))
                            }
                        } else {
                            val fieldName = generateFieldName(bucketJsonObj)
                            var newKey = fieldName
                            var counter = 1
                            while (mergedObj.has(newKey)) {
                                newKey = "${fieldName}_$counter"
                                counter++
                            }
                            mergedObj.put(newKey, bucketJsonObj)
                        }
                        mergedObj.toString(2)
                    } catch (e: Exception) {
                        // Fallback: wrap both in array
                        JSONArray().apply {
                            put(currentJsonObj)
                            put(bucketJsonObj)
                        }.toString(2)
                    }
                }
            }
        } catch (e: Exception) {
            // If parsing fails, try simple concatenation with proper formatting
            try {
                // Try parsing current as JSON
                val parsed = JSONObject(trimmedCurrent)
                // Parse bucket as well
                val bucketParsed = parseJson(trimmedBucket)
                val fieldName = if (bucketParsed is String) generateFieldName(bucketParsed) else "data"
                parsed.put(fieldName, bucketParsed)
                parsed.toString(2)
            } catch (e2: Exception) {
                try {
                    // Try as arrays
                    val currentArray = JSONArray(trimmedCurrent)
                    val bucketArray = JSONArray(trimmedBucket)
                    for (i in 0 until bucketArray.length()) {
                        currentArray.put(bucketArray.get(i))
                    }
                    currentArray.toString(2)
                } catch (e3: Exception) {
                    // Last resort: wrap in object
                    JSONObject().apply {
                        put("current", trimmedCurrent)
                        put("inserted", trimmedBucket)
                    }.toString(2)
                }
            }
        }
    }
    
    /**
     * Parse JSON string into JSONObject, JSONArray, or primitive
     */
    private fun parseJson(jsonString: String): Any {
        val trimmed = jsonString.trim()
        return try {
            when {
                trimmed.startsWith("{") -> JSONObject(trimmed)
                trimmed.startsWith("[") -> JSONArray(trimmed)
                trimmed.startsWith("\"") && trimmed.endsWith("\"") -> trimmed.removeSurrounding("\"")
                trimmed == "true" -> true
                trimmed == "false" -> false
                trimmed == "null" -> JSONObject.NULL
                trimmed.toDoubleOrNull() != null -> {
                    val num = trimmed.toDoubleOrNull()!!
                    if (num % 1 == 0.0) num.toInt() else num
                }
                else -> trimmed // String (not quoted)
            }
        } catch (e: Exception) {
            trimmed // Return as string if parsing fails
        }
    }
    
    /**
     * Merge two JSON objects
     */
    private fun mergeObjects(obj1: JSONObject, obj2: JSONObject): JSONObject {
        val merged = JSONObject(obj1.toString())
        obj2.keys().forEach { key ->
            // Handle key conflicts by appending number
            var newKey = key
            var counter = 1
            while (merged.has(newKey)) {
                newKey = "${key}_$counter"
                counter++
            }
            merged.put(newKey, obj2.get(key))
        }
        return merged
    }
    
    /**
     * Add item to JSON array
     */
    private fun addToArray(array: JSONArray, item: Any): JSONArray {
        val newArray = JSONArray()
        for (i in 0 until array.length()) {
            newArray.put(array.get(i))
        }
        newArray.put(item)
        return newArray
    }
    
    /**
     * Check if value is a primitive (not object/array)
     */
    private fun isPrimitive(value: Any): Boolean {
        return value is String || value is Number || value is Boolean || value == JSONObject.NULL
    }
    
    /**
     * Generate a safe field name from bucket content
     */
    private fun generateFieldName(bucketValue: Any): String {
        return when (bucketValue) {
            is String -> {
                val cleaned = bucketValue.take(20).replace(Regex("[^a-zA-Z0-9]"), "_")
                if (cleaned.isEmpty()) "data" else cleaned.lowercase()
            }
            is Number -> "value_${bucketValue}"
            is Boolean -> if (bucketValue) "is_true" else "is_false"
            else -> "data"
        }
    }
}

