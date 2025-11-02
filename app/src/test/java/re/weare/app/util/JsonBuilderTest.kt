package re.weare.app.util

import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for JsonBuilder
 */
class JsonBuilderTest {

    @Test
    fun `test buildJsonString simple object`() {
        val rootNode = JsonNode.ObjectNode(id = UUID.randomUUID().toString()).apply {
            children["name"] = JsonNode.ValueNode(
                id = UUID.randomUUID().toString(),
                value = "John",
                type = ValueType.STRING
            )
            children["age"] = JsonNode.ValueNode(
                id = UUID.randomUUID().toString(),
                value = "30",
                type = ValueType.NUMBER
            )
        }
        
        val result = JsonBuilder.buildJsonString(rootNode, KeyCaseStyle.CAMEL_CASE)
        
        assertNotNull("Built JSON should not be null", result)
        assertTrue("Should contain name key", result.contains("name") || result.contains("\"name\""))
        assertTrue("Should contain age key", result.contains("age") || result.contains("\"age\""))
        assertTrue("Should contain value John", result.contains("John"))
    }

    @Test
    fun `test buildJsonString with camelCase`() {
        val rootNode = JsonNode.ObjectNode(id = UUID.randomUUID().toString()).apply {
            children["first_name"] = JsonNode.ValueNode(
                id = UUID.randomUUID().toString(),
                value = "John",
                type = ValueType.STRING
            )
        }
        
        val result = JsonBuilder.buildJsonString(rootNode, KeyCaseStyle.CAMEL_CASE)
        
        assertTrue("Should transform to camelCase", result.contains("firstName"))
    }

    @Test
    fun `test buildJsonString with snake_case`() {
        val rootNode = JsonNode.ObjectNode(id = UUID.randomUUID().toString()).apply {
            children["firstName"] = JsonNode.ValueNode(
                id = UUID.randomUUID().toString(),
                value = "John",
                type = ValueType.STRING
            )
        }
        
        val result = JsonBuilder.buildJsonString(rootNode, KeyCaseStyle.SNAKE_CASE)
        
        assertTrue("Should transform to snake_case", result.contains("first_name"))
    }

    @Test
    fun `test buildJsonString with PascalCase`() {
        val rootNode = JsonNode.ObjectNode(id = UUID.randomUUID().toString()).apply {
            children["first_name"] = JsonNode.ValueNode(
                id = UUID.randomUUID().toString(),
                value = "John",
                type = ValueType.STRING
            )
        }
        
        val result = JsonBuilder.buildJsonString(rootNode, KeyCaseStyle.PASCAL_CASE)
        
        assertTrue("Should transform to PascalCase", result.contains("FirstName"))
    }

    @Test
    fun `test buildJsonString with boolean`() {
        val rootNode = JsonNode.ObjectNode(id = UUID.randomUUID().toString()).apply {
            children["isActive"] = JsonNode.ValueNode(
                id = UUID.randomUUID().toString(),
                value = "true",
                type = ValueType.BOOLEAN
            )
        }
        
        val result = JsonBuilder.buildJsonString(rootNode, KeyCaseStyle.CAMEL_CASE)
        
        assertTrue("Should contain boolean value", result.contains("true"))
    }

    @Test
    fun `test buildJsonString with null`() {
        val rootNode = JsonNode.ObjectNode(id = UUID.randomUUID().toString()).apply {
            children["middleName"] = JsonNode.ValueNode(
                id = UUID.randomUUID().toString(),
                value = "",
                type = ValueType.NULL
            )
        }
        
        val result = JsonBuilder.buildJsonString(rootNode, KeyCaseStyle.CAMEL_CASE)
        
        assertTrue("Should contain null value", result.contains("null"))
    }

    @Test
    fun `test buildJsonString with nested objects`() {
        val rootNode = JsonNode.ObjectNode(id = UUID.randomUUID().toString()).apply {
            children["name"] = JsonNode.ValueNode(
                id = UUID.randomUUID().toString(),
                value = "John",
                type = ValueType.STRING
            )
            val addressNode = JsonNode.ObjectNode(id = UUID.randomUUID().toString()).apply {
                children["street"] = JsonNode.ValueNode(
                    id = UUID.randomUUID().toString(),
                    value = "Main St",
                    type = ValueType.STRING
                )
            }
            children["address"] = addressNode
        }
        
        val result = JsonBuilder.buildJsonString(rootNode, KeyCaseStyle.CAMEL_CASE)
        
        assertNotNull("Built JSON should not be null", result)
        assertTrue("Should contain nested address object", result.contains("address"))
    }
}



