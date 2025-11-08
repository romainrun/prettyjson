package com.prettyjson.android.util

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import java.util.regex.Pattern

/**
 * JSON formatting, validation, and manipulation utilities
 */
object JsonFormatter {
    private val gson = Gson()
    private val prettyGson = Gson().newBuilder().setPrettyPrinting().create()
    
    /**
     * Extracts error location (line and column) from JSON error
     */
    fun extractErrorLocation(e: Exception, jsonString: String): ErrorLocation? {
        val message = e.message ?: return null
        
        val linePattern = Pattern.compile(".*line\\s+(\\d+).*column\\s+(\\d+).*", Pattern.CASE_INSENSITIVE)
        val matcher = linePattern.matcher(message)
        
        if (matcher.find()) {
            val line = matcher.group(1)?.toIntOrNull() ?: return null
            val column = matcher.group(2)?.toIntOrNull() ?: return null
            return ErrorLocation(line, column, formatErrorMessage(e, jsonString))
        }
        
        val lineOnlyPattern = Pattern.compile(".*line\\s+(\\d+).*", Pattern.CASE_INSENSITIVE)
        val lineMatcher = lineOnlyPattern.matcher(message)
        
        if (lineMatcher.find()) {
            val line = lineMatcher.group(1)?.toIntOrNull() ?: return null
            return ErrorLocation(line, 0, formatErrorMessage(e, jsonString))
        }
        
        return null
    }
    
    /**
     * Validates JSON and returns a Result with error message if invalid
     */
    fun validate(jsonString: String): ValidationResult {
        if (jsonString.isBlank()) {
            return ValidationResult(isValid = true) // Empty is considered valid (will be formatted as empty object)
        }
        
        return try {
            JsonParser.parseString(jsonString)
            ValidationResult(isValid = true)
        } catch (e: JsonSyntaxException) {
            val message = formatErrorMessage(e, jsonString)
            ValidationResult(isValid = false, errorMessage = message)
        } catch (e: MalformedJsonException) {
            val message = formatErrorMessage(e, jsonString)
            ValidationResult(isValid = false, errorMessage = message)
        } catch (e: Exception) {
            val message = formatErrorMessage(e, jsonString)
            ValidationResult(isValid = false, errorMessage = message)
        }
    }
    
    /**
     * Formats error messages to be more user-friendly with suggestions
     */
    private fun formatErrorMessage(e: Exception, jsonString: String): String {
        val originalMessage = e.message ?: "Invalid JSON"
        
        // Extract line and column from error message if present
        val linePattern = Pattern.compile(".*line\\s+(\\d+).*column\\s+(\\d+).*", Pattern.CASE_INSENSITIVE)
        val matcher = linePattern.matcher(originalMessage)
        
        var line: String? = null
        var column: String? = null
        
        if (matcher.find()) {
            line = matcher.group(1)
            column = matcher.group(2)
        } else {
            // Try to extract just line number
            val lineOnlyPattern = Pattern.compile(".*line\\s+(\\d+).*", Pattern.CASE_INSENSITIVE)
            val lineMatcher = lineOnlyPattern.matcher(originalMessage)
            if (lineMatcher.find()) {
                line = lineMatcher.group(1)
            }
        }
        
        // Generate helpful suggestions based on error type
        val suggestion = generateErrorSuggestion(e, jsonString, line?.toIntOrNull(), column?.toIntOrNull())
        
        val baseMessage = when {
            line != null && column != null -> "Invalid JSON syntax at line $line, column $column"
            line != null -> "Invalid JSON syntax at line $line"
            else -> "Invalid JSON syntax"
        }
        
        return if (suggestion != null) {
            "$baseMessage\n💡 Suggestion: $suggestion"
        } else {
            baseMessage
        }
    }
    
    /**
     * Generate helpful suggestions for common JSON errors
     */
    private fun generateErrorSuggestion(e: Exception, jsonString: String, line: Int?, column: Int?): String? {
        val message = e.message?.lowercase() ?: ""
        
        // Check for common error patterns
        when {
            message.contains("expecting") && message.contains("comma") -> {
                return "Missing comma - add a comma after the previous value"
            }
            message.contains("expecting") && message.contains("colon") -> {
                return "Missing colon - add a colon after the key name"
            }
            message.contains("unterminated string") || message.contains("unclosed string") -> {
                return "Unclosed string - check for missing closing quote"
            }
            message.contains("expecting") && message.contains("}") -> {
                return "Missing closing brace - add } to close the object"
            }
            message.contains("expecting") && message.contains("]") -> {
                return "Missing closing bracket - add ] to close the array"
            }
            message.contains("trailing comma") -> {
                return "Trailing comma - remove the comma after the last item"
            }
            message.contains("duplicate key") -> {
                return "Duplicate key - each key in an object must be unique"
            }
            message.contains("unexpected character") -> {
                return "Unexpected character - check for invalid characters or missing quotes around strings"
            }
            message.contains("expecting name") -> {
                return "Missing key name - add a key name before the colon"
            }
            message.contains("expecting value") -> {
                return "Missing value - add a value after the colon"
            }
            line != null -> {
                // Try to analyze the line for common issues
                val lines = jsonString.lines()
                if (line > 0 && line <= lines.size) {
                    val errorLine = lines[line - 1]
                    when {
                        errorLine.trim().endsWith(",") && errorLine.trim().endsWith("}") -> {
                            return "Trailing comma before closing brace - remove the comma"
                        }
                        errorLine.trim().endsWith(",") && errorLine.trim().endsWith("]") -> {
                            return "Trailing comma before closing bracket - remove the comma"
                        }
                        errorLine.contains("\"") && errorLine.count { it == '"' } % 2 != 0 -> {
                            return "Unmatched quotes - check for missing or extra quotes"
                        }
                        errorLine.contains(":") && !errorLine.contains("\"") -> {
                            return "Key name missing quotes - wrap the key name in quotes"
                        }
                    }
                }
            }
        }
        
        return null
    }
    
    /**
     * Formats JSON with indentation (prettify)
     * @param tabSpaces Number of spaces for indentation (default: 2)
     */
    fun format(jsonString: String, tabSpaces: Int = 2): FormatResult {
        val validation = validate(jsonString)
        if (!validation.isValid) {
            return FormatResult(success = false, content = jsonString, errorMessage = validation.errorMessage)
        }
        
        return try {
            val jsonElement = JsonParser.parseString(jsonString)
            // Use custom indentation based on tabSpaces
            val indent = " ".repeat(tabSpaces.coerceIn(1, 10)) // Limit between 1-10 spaces
            val customGson = Gson().newBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create()
            
            val formatted = customGson.toJson(jsonElement)
            // Replace default 2-space indentation with custom spacing
            val formattedWithCustomIndent = formatted.replace(Regex("^  +", RegexOption.MULTILINE)) { matchResult ->
                val originalSpaces = matchResult.value.length
                val indentLevel = originalSpaces / 2 // Default is 2 spaces
                indent.repeat(indentLevel)
            }
            
            FormatResult(success = true, content = formattedWithCustomIndent)
        } catch (e: JsonSyntaxException) {
            FormatResult(success = false, content = jsonString, errorMessage = formatErrorMessage(e, jsonString))
        } catch (e: MalformedJsonException) {
            FormatResult(success = false, content = jsonString, errorMessage = formatErrorMessage(e, jsonString))
        } catch (e: Exception) {
            FormatResult(success = false, content = jsonString, errorMessage = formatErrorMessage(e, jsonString))
        }
    }
    
    /**
     * Minifies JSON to a single line
     */
    fun minify(jsonString: String): FormatResult {
        val validation = validate(jsonString)
        if (!validation.isValid) {
            return FormatResult(success = false, content = jsonString, errorMessage = validation.errorMessage)
        }
        
        return try {
            val jsonElement = JsonParser.parseString(jsonString)
            val minified = gson.toJson(jsonElement)
            FormatResult(success = true, content = minified)
        } catch (e: JsonSyntaxException) {
            FormatResult(success = false, content = jsonString, errorMessage = formatErrorMessage(e, jsonString))
        } catch (e: MalformedJsonException) {
            FormatResult(success = false, content = jsonString, errorMessage = formatErrorMessage(e, jsonString))
        } catch (e: Exception) {
            FormatResult(success = false, content = jsonString, errorMessage = formatErrorMessage(e, jsonString))
        }
    }
    
    /**
     * Formats JSON keys according to the specified case style
     */
    fun formatKeyCase(jsonString: String, caseStyle: KeyCaseStyle): FormatResult {
        val validation = validate(jsonString)
        if (!validation.isValid) {
            return FormatResult(success = false, content = jsonString, errorMessage = validation.errorMessage)
        }
        
        return try {
            val jsonElement = JsonParser.parseString(jsonString)
            val transformed = transformKeys(jsonElement, caseStyle)
            val formatted = prettyGson.toJson(transformed)
            FormatResult(success = true, content = formatted)
        } catch (e: Exception) {
            FormatResult(success = false, content = jsonString, errorMessage = e.message ?: "Key formatting failed")
        }
    }
    
    /**
     * Recursively transforms JSON keys to the specified case style
     */
    private fun transformKeys(element: JsonElement, caseStyle: KeyCaseStyle): JsonElement {
        return when {
            element.isJsonObject -> {
                val obj = com.google.gson.JsonObject()
                element.asJsonObject.entrySet().forEach { entry ->
                    val newKey = formatKey(entry.key, caseStyle)
                    obj.add(newKey, transformKeys(entry.value, caseStyle))
                }
                obj
            }
            element.isJsonArray -> {
                val array = com.google.gson.JsonArray()
                element.asJsonArray.forEach { item ->
                    array.add(transformKeys(item, caseStyle))
                }
                array
            }
            else -> element
        }
    }
    
    /**
     * Formats a single key according to the case style
     */
    private fun formatKey(key: String, caseStyle: KeyCaseStyle): String {
        return when (caseStyle) {
            KeyCaseStyle.CAMEL_CASE -> toCamelCase(key)
            KeyCaseStyle.SNAKE_CASE -> toSnakeCase(key)
            KeyCaseStyle.PASCAL_CASE -> toPascalCase(key)
        }
    }
    
    private fun toCamelCase(key: String): String {
        if (key.isEmpty()) return key
        
        // Handle snake_case
        if (key.contains("_")) {
            return key.split("_").joinToString("") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }.replaceFirstChar { it.lowercase() }
        }
        
        // Handle PascalCase
        if (key.first().isUpperCase()) {
            return key.replaceFirstChar { it.lowercase() }
        }
        
        return key
    }
    
    private fun toSnakeCase(key: String): String {
        return key.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
    }
    
    private fun toPascalCase(key: String): String {
        if (key.isEmpty()) return key
        
        // Handle snake_case
        if (key.contains("_")) {
            return key.split("_").joinToString("") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
        }
        
        // Handle camelCase
        return key.replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Sorts JSON object keys alphabetically
     */
    fun sortKeys(jsonString: String, order: SortOrder = SortOrder.ASC, sortBy: SortBy = SortBy.KEY): FormatResult {
        val validation = validate(jsonString)
        if (!validation.isValid) {
            return FormatResult(success = false, content = jsonString, errorMessage = validation.errorMessage)
        }
        
        return try {
            val jsonElement = JsonParser.parseString(jsonString)
            val sorted = sortJsonElement(jsonElement, order, sortBy)
            val formatted = prettyGson.toJson(sorted)
            FormatResult(success = true, content = formatted)
        } catch (e: Exception) {
            FormatResult(success = false, content = jsonString, errorMessage = e.message ?: "Failed to sort JSON")
        }
    }
    
    enum class SortOrder {
        ASC, DESC
    }
    
    enum class SortBy {
        KEY,        // Sort by key name
        TYPE,       // Sort by value type (string, number, boolean, object, array)
        VALUE       // Sort by value (only for primitive types)
    }
    
    /**
     * Recursively sorts JSON object keys
     */
    private fun sortJsonElement(element: JsonElement, order: SortOrder = SortOrder.ASC, sortBy: SortBy = SortBy.KEY): JsonElement {
        return when {
            element.isJsonObject -> {
                val obj = com.google.gson.JsonObject()
                // Sort entries based on criteria
                val sortedEntries = element.asJsonObject.entrySet().sortedWith(
                    when (sortBy) {
                        SortBy.KEY -> Comparator { e1, e2 -> 
                            if (order == SortOrder.ASC) e1.key.compareTo(e2.key) 
                            else e2.key.compareTo(e1.key)
                        }
                        SortBy.TYPE -> Comparator { e1, e2 ->
                            val type1 = getElementType(e1.value)
                            val type2 = getElementType(e2.value)
                            val typeCompare = type1.compareTo(type2)
                            if (typeCompare != 0) {
                                if (order == SortOrder.ASC) typeCompare else -typeCompare
                            } else {
                                // If same type, sort by key
                                if (order == SortOrder.ASC) e1.key.compareTo(e2.key) 
                                else e2.key.compareTo(e1.key)
                            }
                        }
                        SortBy.VALUE -> Comparator { e1, e2 ->
                            val val1 = getValueForSort(e1.value)
                            val val2 = getValueForSort(e2.value)
                            val valueCompare = val1.compareTo(val2)
                            if (valueCompare != 0) {
                                if (order == SortOrder.ASC) valueCompare else -valueCompare
                            } else {
                                // If same value, sort by key
                                if (order == SortOrder.ASC) e1.key.compareTo(e2.key) 
                                else e2.key.compareTo(e1.key)
                            }
                        }
                    }
                )
                
                sortedEntries.forEach { entry ->
                    obj.add(entry.key, sortJsonElement(entry.value, order, sortBy))
                }
                obj
            }
            element.isJsonArray -> {
                val array = com.google.gson.JsonArray()
                element.asJsonArray.forEach { item ->
                    array.add(sortJsonElement(item, order, sortBy))
                }
                array
            }
            else -> element
        }
    }
    
    private fun getElementType(element: JsonElement): String {
        return when {
            element.isJsonObject -> "object"
            element.isJsonArray -> "array"
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                when {
                    primitive.isString -> "string"
                    primitive.isNumber -> "number"
                    primitive.isBoolean -> "boolean"
                    primitive.isJsonNull -> "null"
                    else -> "unknown"
                }
            }
            else -> "unknown"
        }
    }
    
    private fun getValueForSort(element: JsonElement): String {
        return when {
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                when {
                    primitive.isString -> primitive.asString
                    primitive.isNumber -> primitive.asString
                    primitive.isBoolean -> primitive.asString
                    primitive.isJsonNull -> ""
                    else -> ""
                }
            }
            element.isJsonObject -> "{}"
            element.isJsonArray -> "[]"
            else -> ""
        }
    }
}

/**
 * Result of JSON validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Result of JSON formatting operation
 */
data class FormatResult(
    val success: Boolean,
    val content: String,
    val errorMessage: String? = null
)

/**
 * Key case style options
 */
enum class KeyCaseStyle {
    CAMEL_CASE,
    SNAKE_CASE,
    PASCAL_CASE
}

