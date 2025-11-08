package com.prettyjson.android.util

import com.google.gson.JsonElement
import com.google.gson.JsonParser

/**
 * Utility to generate JSON paths (e.g., "data.user[0].name")
 */
object JsonPathGenerator {
    
    /**
     * Generate JSON path for a given JsonElement
     * @param element The JSON element to get path for
     * @param rootElement The root JSON element
     * @param targetPath The target path to find
     * @return JSON path string (e.g., "data.user[0].name")
     */
    fun generatePath(
        element: JsonElement,
        rootElement: JsonElement,
        targetPath: String = ""
    ): String {
        return when {
            element.isJsonObject -> {
                val obj = element.asJsonObject
                val path = if (targetPath.isEmpty()) "root" else targetPath
                // For objects, return the path
                path
            }
            element.isJsonArray -> {
                val array = element.asJsonArray
                val path = if (targetPath.isEmpty()) "root" else targetPath
                // For arrays, return the path
                path
            }
            else -> {
                // For primitives, return the path
                if (targetPath.isEmpty()) "root" else targetPath
            }
        }
    }
    
    /**
     * Generate path for a key in an object
     */
    fun pathForKey(key: String, parentPath: String = ""): String {
        return if (parentPath.isEmpty() || parentPath == "root") {
            key
        } else {
            "$parentPath.$key"
        }
    }
    
    /**
     * Generate path for an array index
     */
    fun pathForIndex(index: Int, parentPath: String = ""): String {
        return if (parentPath.isEmpty() || parentPath == "root") {
            "[$index]"
        } else {
            "$parentPath[$index]"
        }
    }
    
    /**
     * Generate full path from root to a specific element
     */
    fun findPathToElement(
        root: JsonElement,
        target: JsonElement,
        currentPath: String = "root"
    ): String? {
        if (root === target) {
            return currentPath
        }
        
        return when {
            root.isJsonObject -> {
                root.asJsonObject.entrySet().forEach { (key, value) ->
                    val newPath = if (currentPath == "root") key else "$currentPath.$key"
                    findPathToElement(value, target, newPath)?.let { return it }
                }
                null
            }
            root.isJsonArray -> {
                root.asJsonArray.forEachIndexed { index, element ->
                    val newPath = if (currentPath == "root") "[$index]" else "$currentPath[$index]"
                    findPathToElement(element, target, newPath)?.let { return it }
                }
                null
            }
            else -> null
        }
    }
}

