package com.prettyjson.android.util

import org.json.JSONArray
import org.json.JSONObject

/**
 * Utility for inserting text at cursor position in JSON
 */
object CursorPositionInserter {
    /**
     * Insert text at cursor position in JSON string
     * @param jsonText Current JSON text
     * @param cursorPosition Cursor position (0-based index)
     * @param keyName Key name for the inserted value
     * @param typedValue The typed value to insert
     * @return New JSON string with insertion
     */
    fun insertAtCursor(
        jsonText: String,
        cursorPosition: Int,
        keyName: String,
        typedValue: Any
    ): String {
        val beforeCursor = jsonText.substring(0, cursorPosition)
        val afterCursor = jsonText.substring(cursorPosition)
        
        // Determine what to insert based on context
        val insertion = buildInsertion(keyName, typedValue, beforeCursor, afterCursor)
        
        return beforeCursor + insertion + afterCursor
    }
    
    /**
     * Build the insertion text based on context
     */
    private fun buildInsertion(
        keyName: String,
        typedValue: Any,
        beforeCursor: String,
        afterCursor: String
    ): String {
        val trimmedBefore = beforeCursor.trim()
        val trimmedAfter = afterCursor.trim()
        
        // Check if we're inside a JSON object
        val isInObject = trimmedBefore.endsWith("{") || 
                        trimmedBefore.endsWith(",") ||
                        (trimmedBefore.endsWith(":") && !trimmedAfter.startsWith("{"))
        
        // Check if we're inside an array
        val isInArray = trimmedBefore.endsWith("[") || 
                       (trimmedBefore.endsWith(",") && findEnclosingType(beforeCursor) == '[')
        
        // Check if we need a comma before
        val needsCommaBefore = trimmedBefore.isNotEmpty() && 
                              !trimmedBefore.endsWith("{") &&
                              !trimmedBefore.endsWith("[") &&
                              !trimmedBefore.endsWith(":") &&
                              !trimmedBefore.endsWith(",") &&
                              !trimmedBefore.endsWith("\n")
        
        // Check if we need a comma after
        val needsCommaAfter = trimmedAfter.isNotEmpty() &&
                              !trimmedAfter.startsWith("}") &&
                              !trimmedAfter.startsWith("]") &&
                              !trimmedAfter.startsWith(",")
        
        // Build the key-value pair or just value
        val valueStr = formatValue(typedValue)
        
        return when {
            isInArray -> {
                // In array: just insert the value
                (if (needsCommaBefore) ", " else "") + valueStr + (if (needsCommaAfter) ", " else "")
            }
            isInObject -> {
                // In object: insert key-value pair
                val commaBefore = if (needsCommaBefore) "\n  " else ""
                val commaAfter = if (needsCommaAfter) ",\n" else ""
                "$commaBefore\"$keyName\": $valueStr$commaAfter"
            }
            else -> {
                // Default: try to insert as key-value pair
                val commaBefore = if (needsCommaBefore) ",\n  " else ""
                val commaAfter = if (needsCommaAfter) ",\n" else ""
                "$commaBefore\"$keyName\": $valueStr$commaAfter"
            }
        }
    }
    
    /**
     * Find the enclosing type (object or array) at cursor position
     */
    private fun findEnclosingType(text: String): Char? {
        var objectDepth = 0
        var arrayDepth = 0
        
        for (i in text.length - 1 downTo 0) {
            when (text[i]) {
                '}' -> objectDepth++
                '{' -> {
                    objectDepth--
                    if (objectDepth < 0) return '{'
                }
                ']' -> arrayDepth++
                '[' -> {
                    arrayDepth--
                    if (arrayDepth < 0) return '['
                }
            }
        }
        
        return null
    }
    
    /**
     * Format value as JSON string
     */
    private fun formatValue(value: Any): String {
        return when (value) {
            is String -> "\"${value.escapeJson()}\""
            is Number -> value.toString()
            is Boolean -> value.toString()
            is JSONObject -> value.toString(2)
            is JSONArray -> value.toString(2)
            else -> value.toString()
        }
    }
    
    /**
     * Escape string for JSON
     */
    private fun String.escapeJson(): String {
        return this
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}

