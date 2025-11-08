package com.prettyjson.android.util

import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test

class JsonBuilderTest {

    @Test
    fun `test buildJsonString with ObjectNode`() {
        val node = JsonNode.ObjectNode(
            id = "1",
            children = mutableMapOf(
                "name" to JsonNode.ValueNode("1", "John", ValueType.STRING),
                "age" to JsonNode.ValueNode("2", "30", ValueType.NUMBER)
            )
        )
        val result = JsonBuilder.buildJsonString(node, KeyCaseStyle.CAMEL_CASE)
        assertTrue(result.contains("name"))
        assertTrue(result.contains("John"))
        assertTrue(result.contains("age"))
        assertTrue(result.contains("30"))
    }

    @Test
    fun `test buildJsonString with ArrayNode`() {
        val node = JsonNode.ArrayNode(
            id = "1",
            children = mutableListOf(
                JsonNode.ValueNode("1", "item1", ValueType.STRING),
                JsonNode.ValueNode("2", "item2", ValueType.STRING)
            )
        )
        val result = JsonBuilder.buildJsonString(node, KeyCaseStyle.CAMEL_CASE)
        assertTrue(result.contains("item1"))
        assertTrue(result.contains("item2"))
    }

    @Test
    fun `test buildJsonString with nested objects`() {
        val innerNode = JsonNode.ObjectNode(
            id = "2",
            children = mutableMapOf(
                "value" to JsonNode.ValueNode("3", "test", ValueType.STRING)
            )
        )
        val node = JsonNode.ObjectNode(
            id = "1",
            children = mutableMapOf(
                "nested" to innerNode
            )
        )
        val result = JsonBuilder.buildJsonString(node, KeyCaseStyle.CAMEL_CASE)
        assertTrue(result.contains("nested"))
        assertTrue(result.contains("value"))
        assertTrue(result.contains("test"))
    }

    @Test
    fun `test buildJsonString with different key case styles`() {
        val node = JsonNode.ObjectNode(
            id = "1",
            children = mutableMapOf(
                "test key" to JsonNode.ValueNode("1", "value", ValueType.STRING)
            )
        )
        
        val camelCase = JsonBuilder.buildJsonString(node, KeyCaseStyle.CAMEL_CASE)
        val snakeCase = JsonBuilder.buildJsonString(node, KeyCaseStyle.SNAKE_CASE)
        val pascalCase = JsonBuilder.buildJsonString(node, KeyCaseStyle.PASCAL_CASE)
        
        assertNotEquals(camelCase, snakeCase)
        assertNotEquals(camelCase, pascalCase)
        assertNotEquals(snakeCase, pascalCase)
    }

    @Test
    fun `test buildJsonString with boolean and null values`() {
        val node = JsonNode.ObjectNode(
            id = "1",
            children = mutableMapOf(
                "isActive" to JsonNode.ValueNode("1", "true", ValueType.BOOLEAN),
                "data" to JsonNode.ValueNode("2", "null", ValueType.NULL)
            )
        )
        val result = JsonBuilder.buildJsonString(node, KeyCaseStyle.CAMEL_CASE)
        assertTrue(result.contains("isActive"))
        assertTrue(result.contains("true"))
        assertTrue(result.contains("data"))
        assertTrue(result.contains("null"))
    }
}
