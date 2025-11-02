package re.weare.app.util

import com.google.gson.JsonSyntaxException
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for JsonFormatter
 */
class JsonFormatterTest {

    @Test
    fun `test validate valid JSON`() {
        val validJson = """{"name": "John", "age": 30}"""
        val result = JsonFormatter.validate(validJson)
        
        assertTrue("Valid JSON should return isValid = true", result.isValid)
        assertNull("Valid JSON should not have error message", result.errorMessage)
    }

    @Test
    fun `test validate invalid JSON`() {
        val invalidJson = """{"name": "John", "age": }"""
        val result = JsonFormatter.validate(invalidJson)
        
        assertFalse("Invalid JSON should return isValid = false", result.isValid)
        assertNotNull("Invalid JSON should have error message", result.errorMessage)
    }

    @Test
    fun `test validate empty string`() {
        val emptyJson = ""
        val result = JsonFormatter.validate(emptyJson)
        
        assertTrue("Empty string should be considered valid", result.isValid)
    }

    @Test
    fun `test validate blank string`() {
        val blankJson = "   "
        val result = JsonFormatter.validate(blankJson)
        
        assertTrue("Blank string should be considered valid", result.isValid)
    }

    @Test
    fun `test format valid JSON`() {
        val compactJson = """{"name":"John","age":30,"city":"New York"}"""
        val result = JsonFormatter.format(compactJson)
        
        assertTrue("Formatting should succeed", result.success)
        assertNotNull("Formatted JSON should not be null", result.content)
        assertTrue("Formatted JSON should contain newlines or spaces", 
            result.content.contains("\n") || result.content.contains("  "))
    }

    @Test
    fun `test format invalid JSON`() {
        val invalidJson = """{"name":"John","age":}"""
        val result = JsonFormatter.format(invalidJson)
        
        assertFalse("Formatting invalid JSON should fail", result.success)
        assertNotNull("Should have error message", result.errorMessage)
    }

    @Test
    fun `test minify valid JSON`() {
        val formattedJson = """
            {
                "name": "John",
                "age": 30
            }
        """.trimIndent()
        val result = JsonFormatter.minify(formattedJson)
        
        assertTrue("Minify should succeed", result.success)
        assertNotNull("Minified JSON should not be null", result.content)
        assertFalse("Minified JSON should not contain newlines", result.content.contains("\n"))
        assertFalse("Minified JSON should not contain spaces between keys and values", 
            result.content.contains(" : "))
    }

    @Test
    fun `test minify invalid JSON`() {
        val invalidJson = """{"name":"John","age":}"""
        val result = JsonFormatter.minify(invalidJson)
        
        assertFalse("Minifying invalid JSON should fail", result.success)
        assertNotNull("Should have error message", result.errorMessage)
    }

    @Test
    fun `test formatKeyCase camelCase`() {
        val json = """{"first_name": "John", "last_name": "Doe"}"""
        val result = JsonFormatter.formatKeyCase(json, KeyCaseStyle.CAMEL_CASE)
        
        assertTrue("Key case transformation should succeed", result.success)
        assertTrue("Should contain camelCase keys", 
            result.content.contains("firstName") || result.content.contains("\"firstName\""))
    }

    @Test
    fun `test formatKeyCase snake_case`() {
        val json = """{"firstName": "John", "lastName": "Doe"}"""
        val result = JsonFormatter.formatKeyCase(json, KeyCaseStyle.SNAKE_CASE)
        
        assertTrue("Key case transformation should succeed", result.success)
        assertTrue("Should contain snake_case keys", 
            result.content.contains("first_name") || result.content.contains("\"first_name\""))
    }

    @Test
    fun `test formatKeyCase PascalCase`() {
        val json = """{"first_name": "John", "last_name": "Doe"}"""
        val result = JsonFormatter.formatKeyCase(json, KeyCaseStyle.PASCAL_CASE)
        
        assertTrue("Key case transformation should succeed", result.success)
        assertTrue("Should contain PascalCase keys", 
            result.content.contains("FirstName") || result.content.contains("\"FirstName\""))
    }

    @Test
    fun `test formatKeyCase nested objects`() {
        val json = """{"user": {"first_name": "John", "address": {"street_name": "Main St"}}}"""
        val result = JsonFormatter.formatKeyCase(json, KeyCaseStyle.CAMEL_CASE)
        
        assertTrue("Nested key case transformation should succeed", result.success)
        assertTrue("Should transform nested keys", 
            result.content.contains("firstName") || result.content.contains("streetName"))
    }

    @Test
    fun `test formatKeyCase arrays`() {
        val json = """{"users": [{"first_name": "John"}, {"first_name": "Jane"}]}"""
        val result = JsonFormatter.formatKeyCase(json, KeyCaseStyle.CAMEL_CASE)
        
        assertTrue("Array key case transformation should succeed", result.success)
        assertTrue("Should transform keys in array objects", result.content.contains("firstName"))
    }
}



