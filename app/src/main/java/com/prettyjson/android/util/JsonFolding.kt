package com.prettyjson.android.util

import org.json.JSONArray
import org.json.JSONObject

/**
 * Utility for JSON folding (collapse/expand) functionality
 */
object JsonFolding {
    /**
     * Represents a foldable region in JSON
     */
    data class FoldRegion(
        val startIndex: Int,
        val endIndex: Int,
        val level: Int,
        val type: FoldType,
        val key: String? = null,
        val summary: String
    )
    
    enum class FoldType {
        OBJECT,
        ARRAY
    }
    
    /**
     * Find all foldable regions in JSON text
     */
    fun findFoldRegions(jsonText: String): List<FoldRegion> {
        val regions = mutableListOf<FoldRegion>()
        val stack = mutableListOf<Pair<Int, FoldType>>() // (startIndex, type)
        var i = 0
        var currentKey: String? = null
        var inString = false
        var escapeNext = false
        
        while (i < jsonText.length) {
            val char = jsonText[i]
            
            if (escapeNext) {
                escapeNext = false
                i++
                continue
            }
            
            if (char == '\\') {
                escapeNext = true
                i++
                continue
            }
            
            if (char == '"') {
                inString = !inString
                i++
                continue
            }
            
            if (inString) {
                i++
                continue
            }
            
            when (char) {
                '{' -> {
                    // Try to extract key if we're inside an object
                    val key = extractKeyBeforeBrace(jsonText, i)
                    stack.add(Pair(i, FoldType.OBJECT))
                    currentKey = key
                    i++
                }
                '[' -> {
                    val key = extractKeyBeforeBrace(jsonText, i)
                    stack.add(Pair(i, FoldType.ARRAY))
                    currentKey = key
                    i++
                }
                '}', ']' -> {
                    if (stack.isNotEmpty()) {
                        val (startIndex, type) = stack.removeAt(stack.size - 1)
                        val level = stack.size
                        
                        // Generate summary
                        val content = jsonText.substring(startIndex + 1, i).trim()
                        val summary = when (type) {
                            FoldType.OBJECT -> {
                                val keyCount = countKeys(content)
                                if (keyCount > 0) "{ $keyCount key${if (keyCount != 1) "s" else ""} }" else "{ }"
                            }
                            FoldType.ARRAY -> {
                                val itemCount = countArrayItems(content)
                                if (itemCount > 0) "[ $itemCount item${if (itemCount != 1) "s" else ""} ]" else "[ ]"
                            }
                        }
                        
                        regions.add(
                            FoldRegion(
                                startIndex = startIndex,
                                endIndex = i + 1,
                                level = level,
                                type = type,
                                key = currentKey,
                                summary = summary
                            )
                        )
                        currentKey = null
                    }
                    i++
                }
                else -> i++
            }
        }
        
        return regions.sortedBy { it.startIndex }
    }
    
    /**
     * Extract key name before a brace/bracket
     */
    private fun extractKeyBeforeBrace(jsonText: String, braceIndex: Int): String? {
        // Look backwards for a key pattern: "key": or "key":
        var i = braceIndex - 1
        while (i >= 0 && jsonText[i].isWhitespace()) i--
        if (i < 0 || jsonText[i] != ':') return null
        
        i-- // Skip colon
        while (i >= 0 && jsonText[i].isWhitespace()) i--
        if (i < 0) return null
        
        // Check if it's a string key
        if (jsonText[i] == '"') {
            val endQuote = i
            i--
            while (i >= 0 && jsonText[i] != '"') {
                if (jsonText[i] == '\\') i-- // Skip escaped char
                i--
            }
            if (i >= 0) {
                return jsonText.substring(i + 1, endQuote)
            }
        }
        
        return null
    }
    
    /**
     * Count keys in an object content
     */
    private fun countKeys(content: String): Int {
        if (content.isEmpty()) return 0
        var count = 0
        var inString = false
        var escapeNext = false
        var i = 0
        
        while (i < content.length) {
            val char = content[i]
            
            if (escapeNext) {
                escapeNext = false
                i++
                continue
            }
            
            if (char == '\\') {
                escapeNext = true
                i++
                continue
            }
            
            if (char == '"') {
                inString = !inString
                i++
                continue
            }
            
            if (!inString && char == ':') {
                count++
            }
            
            i++
        }
        
        return count
    }
    
    /**
     * Count items in an array content
     */
    private fun countArrayItems(content: String): Int {
        if (content.trim().isEmpty()) return 0
        var count = 0
        var inString = false
        var escapeNext = false
        var depth = 0
        var i = 0
        
        while (i < content.length) {
            val char = content[i]
            
            if (escapeNext) {
                escapeNext = false
                i++
                continue
            }
            
            if (char == '\\') {
                escapeNext = true
                i++
                continue
            }
            
            if (char == '"') {
                inString = !inString
                i++
                continue
            }
            
            if (!inString) {
                when (char) {
                    '{', '[' -> depth++
                    '}', ']' -> depth--
                    ',' -> {
                        if (depth == 0) {
                            count++
                        }
                    }
                }
            }
            
            i++
        }
        
        // Add 1 for the last item (if any)
        if (content.trim().isNotEmpty() && depth == 0) {
            count++
        }
        
        return count
    }
    
    /**
     * Create folded text by replacing fold regions with summaries
     */
    fun foldJson(jsonText: String, collapsedRegions: Set<Int>): String {
        val regions = findFoldRegions(jsonText)
        if (regions.isEmpty() || collapsedRegions.isEmpty()) return jsonText
        
        val sortedRegions = regions.sortedByDescending { it.startIndex }
        var result = jsonText
        
        for (region in sortedRegions) {
            if (collapsedRegions.contains(region.startIndex)) {
                val before = result.substring(0, region.startIndex)
                val after = result.substring(region.endIndex)
                val foldMarker = when (region.type) {
                    FoldType.OBJECT -> "{...}"
                    FoldType.ARRAY -> "[...]"
                }
                result = before + foldMarker + after
            }
        }
        
        return result
    }
}

