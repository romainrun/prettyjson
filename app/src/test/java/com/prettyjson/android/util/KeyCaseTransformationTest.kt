package com.prettyjson.android.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for key case transformation functions
 * Tests through the public formatKeyCase method
 */
class KeyCaseTransformationTest {

    @Test
    fun `test formatKeyCase to camelCase simple`() {
        val json = """{"first_name": "John"}"""
        val result = JsonFormatter.formatKeyCase(json, KeyCaseStyle.CAMEL_CASE)
        
        assertTrue("Should succeed", result.success)
        assertTrue("Should contain camelCase key", result.content.contains("\"firstName\""))
    }

    @Test
    fun `test formatKeyCase to camelCase multiple keys`() {
        val json = """{"first_name": "John", "last_name": "Doe"}"""
        val result = JsonFormatter.formatKeyCase(json, KeyCaseStyle.CAMEL_CASE)
        
        assertTrue("Should succeed", result.success)
        assertTrue("Should contain firstName", result.content.contains("\"firstName\""))
        assertTrue("Should contain lastName", result.content.contains("\"lastName\""))
    }

    @Test
    fun `test formatKeyCase to snake_case simple`() {
        val json = """{"firstName": "John"}"""
        val result = JsonFormatter.formatKeyCase(json, KeyCaseStyle.SNAKE_CASE)
        
        assertTrue("Should succeed", result.success)
        assertTrue("Should contain snake_case key", result.content.contains("\"first_name\""))
    }

    @Test
    fun `test formatKeyCase to PascalCase simple`() {
        val json = """{"first_name": "John"}"""
        val result = JsonFormatter.formatKeyCase(json, KeyCaseStyle.PASCAL_CASE)
        
        assertTrue("Should succeed", result.success)
        assertTrue("Should contain PascalCase key", result.content.contains("\"FirstName\""))
    }

    @Test
    fun `test formatKeyCase preserves values`() {
        val json = """{"first_name": "John", "age": 30}"""
        val result = JsonFormatter.formatKeyCase(json, KeyCaseStyle.CAMEL_CASE)
        
        assertTrue("Should succeed", result.success)
        assertTrue("Should preserve string value", result.content.contains("John"))
        assertTrue("Should preserve number value", result.content.contains("30"))
    }

    @Test
    fun `test formatKeyCase with nested objects`() {
        val json = """{"user": {"first_name": "John"}}"""
        val result = JsonFormatter.formatKeyCase(json, KeyCaseStyle.CAMEL_CASE)
        
        assertTrue("Should succeed", result.success)
        assertTrue("Should transform nested keys", result.content.contains("\"firstName\""))
    }
}

