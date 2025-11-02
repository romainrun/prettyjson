package re.weare.app.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive

/**
 * Result of JSON diff operation
 */
data class JsonDiffResult(
    val hasChanges: Boolean,
    val added: List<DiffItem> = emptyList(),
    val removed: List<DiffItem> = emptyList(),
    val modified: List<ModifiedDiffItem> = emptyList(),
    val error: String? = null
)

/**
 * Represents a diff item with path and value
 */
data class DiffItem(
    val path: String,
    val value: JsonElement,
    val displayValue: String
)

/**
 * Represents a modified item with old and new values
 */
data class ModifiedDiffItem(
    val path: String,
    val oldValue: JsonElement,
    val newValue: JsonElement,
    val oldDisplayValue: String,
    val newDisplayValue: String
)

/**
 * JSON comparison and diff utility
 */
object JsonDiff {
    
    /**
     * Compare two JSON strings and return diff result
     */
    fun compare(json1: String, json2: String): JsonDiffResult {
        return try {
            val element1 = JsonParser.parseString(json1)
            val element2 = JsonParser.parseString(json2)
            compareElements(element1, element2, "root")
        } catch (e: Exception) {
            JsonDiffResult(
                hasChanges = true,
                error = "Failed to parse JSON: ${e.message}"
            )
        }
    }
    
    /**
     * Recursively compare two JSON elements
     */
    private fun compareElements(
        element1: JsonElement,
        element2: JsonElement,
        path: String
    ): JsonDiffResult {
        val added = mutableListOf<DiffItem>()
        val removed = mutableListOf<DiffItem>()
        val modified = mutableListOf<ModifiedDiffItem>()
        
        when {
            element1.isJsonObject && element2.isJsonObject -> {
                val obj1 = element1.asJsonObject
                val obj2 = element2.asJsonObject
                
                // Find added and modified keys
                obj2.keySet().forEach { key ->
                    val newPath = if (path == "root") key else "$path.$key"
                    if (!obj1.has(key)) {
                        // Added
                        added.add(DiffItem(newPath, obj2.get(key), formatValue(obj2.get(key))))
                    } else {
                        // Check if modified
                        val nestedDiff = compareElements(obj1.get(key), obj2.get(key), newPath)
                        added.addAll(nestedDiff.added)
                        removed.addAll(nestedDiff.removed)
                        modified.addAll(nestedDiff.modified)
                        
                        // Check if value itself changed
                        if (!nestedDiff.hasChanges && !areEqual(obj1.get(key), obj2.get(key))) {
                            modified.add(ModifiedDiffItem(
                                newPath,
                                obj1.get(key),
                                obj2.get(key),
                                formatValue(obj1.get(key)),
                                formatValue(obj2.get(key))
                            ))
                        }
                    }
                }
                
                // Find removed keys
                obj1.keySet().forEach { key ->
                    if (!obj2.has(key)) {
                        val oldPath = if (path == "root") key else "$path.$key"
                        removed.add(DiffItem(oldPath, obj1.get(key), formatValue(obj1.get(key))))
                    }
                }
            }
            
            element1.isJsonArray && element2.isJsonArray -> {
                val arr1 = element1.asJsonArray
                val arr2 = element2.asJsonArray
                
                // Compare array elements
                val maxSize = maxOf(arr1.size(), arr2.size())
                for (i in 0 until maxSize) {
                    val arrayPath = "$path[$i]"
                    when {
                        i >= arr1.size() -> {
                            // Added
                            added.add(DiffItem(arrayPath, arr2.get(i), formatValue(arr2.get(i))))
                        }
                        i >= arr2.size() -> {
                            // Removed
                            removed.add(DiffItem(arrayPath, arr1.get(i), formatValue(arr1.get(i))))
                        }
                        else -> {
                            // Compare elements
                            val nestedDiff = compareElements(arr1.get(i), arr2.get(i), arrayPath)
                            added.addAll(nestedDiff.added)
                            removed.addAll(nestedDiff.removed)
                            modified.addAll(nestedDiff.modified)
                            
                            // Check if directly modified
                            if (!nestedDiff.hasChanges && !areEqual(arr1.get(i), arr2.get(i))) {
                                modified.add(ModifiedDiffItem(
                                    arrayPath,
                                    arr1.get(i),
                                    arr2.get(i),
                                    formatValue(arr1.get(i)),
                                    formatValue(arr2.get(i))
                                ))
                            }
                        }
                    }
                }
            }
            
            else -> {
                // Primitive values or different types
                if (!areEqual(element1, element2)) {
                    val displayPath = if (path == "root") "root" else path
                    modified.add(ModifiedDiffItem(
                        displayPath,
                        element1,
                        element2,
                        formatValue(element1),
                        formatValue(element2)
                    ))
                }
            }
        }
        
        return JsonDiffResult(
            hasChanges = added.isNotEmpty() || removed.isNotEmpty() || modified.isNotEmpty(),
            added = added,
            removed = removed,
            modified = modified
        )
    }
    
    /**
     * Check if two JSON elements are equal
     */
    private fun areEqual(e1: JsonElement, e2: JsonElement): Boolean {
        return when {
            e1.isJsonPrimitive && e2.isJsonPrimitive -> {
                e1.asJsonPrimitive == e2.asJsonPrimitive
            }
            e1.isJsonNull && e2.isJsonNull -> true
            e1.isJsonArray && e2.isJsonArray -> {
                val arr1 = e1.asJsonArray
                val arr2 = e2.asJsonArray
                if (arr1.size() != arr2.size()) return false
                arr1.zip(arr2).all { (a, b) -> areEqual(a, b) }
            }
            e1.isJsonObject && e2.isJsonObject -> {
                val obj1 = e1.asJsonObject
                val obj2 = e2.asJsonObject
                if (obj1.keySet() != obj2.keySet()) return false
                obj1.keySet().all { key -> areEqual(obj1.get(key), obj2.get(key)) }
            }
            else -> false
        }
    }
    
    /**
     * Format a JSON element for display
     */
    private fun formatValue(element: JsonElement): String {
        return when {
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                when {
                    primitive.isString -> primitive.asString
                    primitive.isNumber -> primitive.asNumber.toString()
                    primitive.isBoolean -> primitive.asBoolean.toString()
                    else -> "null"
                }
            }
            element.isJsonNull -> "null"
            element.isJsonObject -> "{...}"
            element.isJsonArray -> "[...]"
            else -> element.toString()
        }
    }
    
    /**
     * Generate a formatted diff summary
     */
    fun generateDiffSummary(result: JsonDiffResult): String {
        if (!result.hasChanges) {
            return "JSONs are identical"
        }
        
        val summary = StringBuilder()
        summary.appendLine("Changes found:")
        summary.appendLine("• Added: ${result.added.size}")
        summary.appendLine("• Removed: ${result.removed.size}")
        summary.appendLine("• Modified: ${result.modified.size}")
        
        if (result.added.isNotEmpty()) {
            summary.appendLine("\nAdded items:")
            result.added.take(10).forEach { item ->
                summary.appendLine("  + $item.path: ${item.displayValue}")
            }
            if (result.added.size > 10) {
                summary.appendLine("  ... and ${result.added.size - 10} more")
            }
        }
        
        if (result.removed.isNotEmpty()) {
            summary.appendLine("\nRemoved items:")
            result.removed.take(10).forEach { item ->
                summary.appendLine("  - $item.path: ${item.displayValue}")
            }
            if (result.removed.size > 10) {
                summary.appendLine("  ... and ${result.removed.size - 10} more")
            }
        }
        
        if (result.modified.isNotEmpty()) {
            summary.appendLine("\nModified items:")
            result.modified.take(10).forEach { item ->
                summary.appendLine("  ~ $item.path:")
                summary.appendLine("    Old: ${item.oldDisplayValue}")
                summary.appendLine("    New: ${item.newDisplayValue}")
            }
            if (result.modified.size > 10) {
                summary.appendLine("  ... and ${result.modified.size - 10} more")
            }
        }
        
        return summary.toString()
    }
}


