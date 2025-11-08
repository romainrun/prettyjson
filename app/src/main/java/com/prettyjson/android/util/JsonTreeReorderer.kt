package com.prettyjson.android.util

import com.google.gson.*
import com.prettyjson.android.util.JsonFormatter

/**
 * Utility for safely reordering JSON elements in tree structure
 */
object JsonTreeReorderer {
    /**
     * Reorder keys within the same JSON object
     */
    fun reorderObjectKeys(jsonString: String, sourcePath: String, targetPath: String): Result<String> {
        return try {
            val root = JsonParser.parseString(jsonString).asJsonObject
            val source = getElementByPath(root, sourcePath) ?: return Result.failure(IllegalArgumentException("Source not found"))
            val sourceKey = sourcePath.split(".").last()
            val parentPath = sourcePath.substringBeforeLast(".")
            val parent = getElementByPath(root, parentPath)?.asJsonObject ?: root
            
            if (!parent.has(sourceKey)) {
                return Result.failure(IllegalArgumentException("Source key not found"))
            }
            
            val sourceValue = parent.get(sourceKey)
            parent.remove(sourceKey)
            
            val targetKey = targetPath.split(".").last()
            val entries = parent.entrySet().toList()
            
            // Remove all keys
            val keysToRemove = entries.map { it.key }.toList()
            keysToRemove.forEach { parent.remove(it) }
            
            val targetIndex = entries.indexOfFirst { it.key == targetKey }
            if (targetIndex == -1) {
                // Re-add all entries and restore
                entries.forEach { parent.add(it.key, it.value) }
                parent.add(sourceKey, sourceValue)
                return Result.failure(IllegalArgumentException("Target not found"))
            }
            
            entries.forEachIndexed { index, entry ->
                if (index == targetIndex) {
                    parent.add(sourceKey, sourceValue)
                }
                parent.add(entry.key, entry.value)
            }
            
            Result.success(JsonFormatter.format(root.toString()).content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reorder elements within the same JSON array
     */
    fun reorderArrayElements(jsonString: String, arrayPath: String, sourceIndex: Int, targetIndex: Int): Result<String> {
        return try {
            val root = JsonParser.parseString(jsonString)
            val array = getElementByPath(root, arrayPath)?.asJsonArray 
                ?: return Result.failure(IllegalArgumentException("Array not found"))
            
            if (sourceIndex < 0 || sourceIndex >= array.size() || 
                targetIndex < 0 || targetIndex >= array.size() ||
                sourceIndex == targetIndex) {
                return Result.failure(IllegalArgumentException("Invalid indices"))
            }
            
            val sourceElement = array.get(sourceIndex)
            val adjustedTarget = if (targetIndex > sourceIndex) targetIndex - 1 else targetIndex
            
            // Remove source element
            array.remove(sourceIndex)
            
            // Insert at target position
            val newArray = JsonArray()
            for (i in 0 until array.size()) {
                if (i == adjustedTarget) {
                    newArray.add(sourceElement)
                }
                newArray.add(array.get(i))
            }
            if (adjustedTarget >= array.size()) {
                newArray.add(sourceElement)
            }
            
            // Replace the original array's elements by removing all and adding new ones
            val elementsToAdd = mutableListOf<JsonElement>()
            for (i in 0 until newArray.size()) {
                elementsToAdd.add(newArray.get(i))
            }
            // Clear array by removing all elements
            while (array.size() > 0) {
                array.remove(0)
            }
            // Add reordered elements
            elementsToAdd.forEach { array.add(it) }
            
            Result.success(JsonFormatter.format(root.toString()).content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getElementByPath(root: JsonElement, path: String): JsonElement? {
        if (path == "root" || path.isEmpty()) return root
        
        val parts = path.split(".")
        var current: JsonElement? = root
        
        for (part in parts) {
            current = when {
                current?.isJsonObject == true -> {
                    val obj = current.asJsonObject
                    if (part.startsWith("[") && part.endsWith("]")) {
                        // Array index - this would need parent context, simplified for now
                        null
                    } else {
                        obj.get(part)
                    }
                }
                current?.isJsonArray == true -> {
                    val index = part.removePrefix("[").removeSuffix("]").toIntOrNull()
                    if (index != null && index >= 0 && index < current.asJsonArray.size()) {
                        current.asJsonArray.get(index)
                    } else {
                        null
                    }
                }
                else -> null
            }
        }
        
        return current
    }
}

