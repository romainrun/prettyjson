package com.prettyjson.android.util

/**
 * Utility to auto-fix common JSON errors
 */
object JsonAutoFix {
    
    /**
     * Auto-fix trailing commas in JSON
     * Removes trailing commas before closing brackets/braces
     */
    fun fixTrailingCommas(json: String): String {
        var fixed = json
        // Fix trailing commas before closing braces
        fixed = fixed.replace(Regex(",\\s*\\}"), "}")
        // Fix trailing commas before closing brackets
        fixed = fixed.replace(Regex(",\\s*\\]"), "]")
        return fixed
    }
    
    /**
     * Auto-fix missing quotes around keys
     * This is more complex and might not always be correct
     */
    fun fixMissingQuotes(json: String): String {
        // This is a simplified version - full implementation would be more complex
        // Pattern: unquoted key followed by colon
        var fixed = json
        fixed = fixed.replace(Regex("(\\s|^)([a-zA-Z_][a-zA-Z0-9_]*)\\s*:")) { matchResult ->
            val prefix = matchResult.groupValues[1]
            val key = matchResult.groupValues[2]
            "$prefix\"$key\":"
        }
        return fixed
    }
    
    /**
     * Auto-fix common JSON errors
     * @param json The JSON string to fix
     * @return Fixed JSON string
     */
    fun autoFix(json: String): String {
        var fixed = json
        // Fix trailing commas
        fixed = fixTrailingCommas(fixed)
        // Fix missing quotes (be careful with this one)
        // fixed = fixMissingQuotes(fixed) // Disabled by default as it might break valid JSON
        return fixed
    }
    
    /**
     * Suggest fixes for common JSON errors
     * @param json The JSON string
     * @param errorMessage The error message
     * @return List of suggested fixes
     */
    fun suggestFixes(json: String, errorMessage: String): List<String> {
        val suggestions = mutableListOf<String>()
        val lowerMessage = errorMessage.lowercase()
        
        if (lowerMessage.contains("trailing comma") || lowerMessage.contains("expecting")) {
            suggestions.add("Try removing trailing commas")
        }
        
        if (lowerMessage.contains("unterminated string") || lowerMessage.contains("unclosed")) {
            suggestions.add("Check for missing closing quotes")
        }
        
        if (lowerMessage.contains("expecting") && lowerMessage.contains("}")) {
            suggestions.add("Add missing closing brace: }")
        }
        
        if (lowerMessage.contains("expecting") && lowerMessage.contains("]")) {
            suggestions.add("Add missing closing bracket: ]")
        }
        
        if (lowerMessage.contains("expecting") && lowerMessage.contains("comma")) {
            suggestions.add("Add missing comma after previous value")
        }
        
        if (lowerMessage.contains("expecting") && lowerMessage.contains("colon")) {
            suggestions.add("Add missing colon after key name")
        }
        
        return suggestions
    }
}

