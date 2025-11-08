package com.prettyjson.android.util

/**
 * Utility to find matching brackets in JSON text
 */
object BracketMatcher {
    
    /**
     * Find the matching bracket position for a given bracket position
     * @param text The JSON text
     * @param position The position of the bracket (must be '{', '}', '[', or ']')
     * @return The position of the matching bracket, or null if not found
     */
    fun findMatchingBracket(text: String, position: Int): Int? {
        if (position < 0 || position >= text.length) return null
        
        val char = text[position]
        val (openChar, closeChar, direction) = when (char) {
            '{' -> Triple('{', '}', 1) // Forward search
            '}' -> Triple('}', '{', -1) // Backward search
            '[' -> Triple('[', ']', 1) // Forward search
            ']' -> Triple(']', '[', -1) // Backward search
            else -> return null
        }
        
        var depth = 0
        var i = position
        
        while (i >= 0 && i < text.length) {
            when (text[i]) {
                openChar -> depth++
                closeChar -> {
                    depth--
                    if (depth == 0) {
                        return i
                    }
                }
            }
            i += direction
        }
        
        return null
    }
    
    /**
     * Check if a position is on a bracket character
     */
    fun isBracket(text: String, position: Int): Boolean {
        if (position < 0 || position >= text.length) return false
        val char = text[position]
        return char == '{' || char == '}' || char == '[' || char == ']'
    }
    
    /**
     * Get bracket type at position
     */
    fun getBracketType(text: String, position: Int): BracketType? {
        if (position < 0 || position >= text.length) return null
        return when (text[position]) {
            '{' -> BracketType.OPEN_BRACE
            '}' -> BracketType.CLOSE_BRACE
            '[' -> BracketType.OPEN_BRACKET
            ']' -> BracketType.CLOSE_BRACKET
            else -> null
        }
    }
    
    enum class BracketType {
        OPEN_BRACE,
        CLOSE_BRACE,
        OPEN_BRACKET,
        CLOSE_BRACKET
    }
}

