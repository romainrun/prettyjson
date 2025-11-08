package com.prettyjson.android.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

/**
 * JSON builder utility for constructing JSON objects visually
 */
object JsonBuilder {
    
    /**
     * Converts a JsonNode tree to a formatted JSON string
     */
    fun buildJsonString(node: JsonNode, keyCaseStyle: KeyCaseStyle = KeyCaseStyle.CAMEL_CASE): String {
        val jsonElement = nodeToJsonElement(node, keyCaseStyle)
        return com.google.gson.Gson().newBuilder().setPrettyPrinting().create().toJson(jsonElement)
    }
    
    private fun nodeToJsonElement(node: JsonNode, keyCaseStyle: KeyCaseStyle): com.google.gson.JsonElement {
        return when (node) {
            is JsonNode.ObjectNode -> {
                val obj = JsonObject()
                node.children.forEach { (key, child) ->
                    val formattedKey = formatKey(key, keyCaseStyle)
                    obj.add(formattedKey, nodeToJsonElement(child, keyCaseStyle))
                }
                obj
            }
            is JsonNode.ArrayNode -> {
                val array = JsonArray()
                node.children.forEach { child ->
                    array.add(nodeToJsonElement(child, keyCaseStyle))
                }
                array
            }
            is JsonNode.ValueNode -> {
                JsonPrimitive(node.value)
            }
        }
    }
    
    private fun formatKey(key: String, caseStyle: KeyCaseStyle): String {
        return when (caseStyle) {
            KeyCaseStyle.CAMEL_CASE -> key.lowercase().split(" ").joinToString("") { 
                it.replaceFirstChar { char -> char.uppercase() }
            }.replaceFirstChar { it.lowercase() }
            KeyCaseStyle.SNAKE_CASE -> key.lowercase().replace(" ", "_")
            KeyCaseStyle.PASCAL_CASE -> key.lowercase().split(" ").joinToString("") { 
                it.replaceFirstChar { char -> char.uppercase() }
            }
        }
    }
}

/**
 * Sealed class representing JSON structure nodes
 */
sealed class JsonNode {
    abstract val id: String
    
    data class ObjectNode(
        override val id: String,
        val children: MutableMap<String, JsonNode> = mutableMapOf()
    ) : JsonNode()
    
    data class ArrayNode(
        override val id: String,
        val children: MutableList<JsonNode> = mutableListOf()
    ) : JsonNode()
    
    data class ValueNode(
        override val id: String,
        val value: String,
        val type: ValueType
    ) : JsonNode()
}

/**
 * Value types for JSON builder
 */
enum class ValueType {
    STRING,
    NUMBER,
    BOOLEAN,
    NULL
}









