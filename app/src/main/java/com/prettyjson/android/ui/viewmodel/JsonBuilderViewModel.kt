package com.prettyjson.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.prettyjson.android.data.preferences.PreferencesManager
import com.prettyjson.android.util.JsonBuilder
import com.prettyjson.android.util.JsonNode
import com.prettyjson.android.util.KeyCaseStyle
import com.prettyjson.android.util.ValueType
import java.util.UUID

/**
 * ViewModel for the JSON Builder screen
 */
class JsonBuilderViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _rootNode = MutableStateFlow<JsonNode.ObjectNode>(
        JsonNode.ObjectNode(id = UUID.randomUUID().toString())
    )
    val rootNode: StateFlow<JsonNode.ObjectNode> = _rootNode.asStateFlow()
    
    private val _keyCaseStyle = MutableStateFlow(KeyCaseStyle.CAMEL_CASE)
    val keyCaseStyle: StateFlow<KeyCaseStyle> = _keyCaseStyle.asStateFlow()
    
    private val _previewJson = MutableStateFlow("")
    val previewJson: StateFlow<String> = _previewJson.asStateFlow()
    
    init {
        // Use default CAMEL_CASE (key case style feature removed from settings)
        _keyCaseStyle.value = KeyCaseStyle.CAMEL_CASE
        updatePreview()
    }
    
    fun addKeyValuePair(key: String, value: String, type: com.prettyjson.android.util.ValueType) {
        val current = _rootNode.value
        val newNode = JsonNode.ValueNode(
            id = UUID.randomUUID().toString(),
            value = value,
            type = type
        )
        _rootNode.value = current.copy(
            children = (current.children + (key to newNode)).toMutableMap()
        )
        updatePreview()
    }
    
    fun removeKeyValuePair(key: String) {
        val current = _rootNode.value
        val newChildren = current.children.toMutableMap()
        newChildren.remove(key)
        _rootNode.value = current.copy(children = newChildren)
        updatePreview()
    }
    
    fun updateKeyValuePair(oldKey: String, newKey: String, value: String, type: com.prettyjson.android.util.ValueType) {
        val current = _rootNode.value
        val newChildren = current.children.toMutableMap()
        newChildren.remove(oldKey)
        newChildren[newKey] = JsonNode.ValueNode(
            id = UUID.randomUUID().toString(),
            value = value,
            type = type
        )
        _rootNode.value = current.copy(children = newChildren)
        updatePreview()
    }
    
    private fun updatePreview() {
        try {
            val jsonString = JsonBuilder.buildJsonString(_rootNode.value, _keyCaseStyle.value)
            _previewJson.value = jsonString
        } catch (e: Exception) {
            _previewJson.value = "Error generating preview: ${e.message}"
        }
    }
    
    fun clear() {
        _rootNode.value = JsonNode.ObjectNode(id = UUID.randomUUID().toString())
        updatePreview()
    }
    
    fun loadFromJson(jsonString: String) {
        // Parse JSON and convert to JsonNode structure
        try {
            val gson = com.google.gson.Gson()
            val jsonElement = com.google.gson.JsonParser.parseString(jsonString)
            val root = jsonElementToJsonNode(jsonElement, UUID.randomUUID().toString())
            if (root is JsonNode.ObjectNode) {
                _rootNode.value = root
                updatePreview()
            }
        } catch (e: Exception) {
            // Handle error - invalid JSON
            _previewJson.value = "Error parsing JSON: ${e.message}"
        }
    }
    
    private fun jsonElementToJsonNode(element: com.google.gson.JsonElement, id: String): JsonNode {
        return when {
            element.isJsonObject -> {
                val obj = element.asJsonObject
                val children = mutableMapOf<String, JsonNode>()
                obj.entrySet().forEach { (key, value) ->
                    children[key] = jsonElementToJsonNode(value, UUID.randomUUID().toString())
                }
                JsonNode.ObjectNode(id = id, children = children)
            }
            element.isJsonArray -> {
                val array = element.asJsonArray
                val children = mutableListOf<JsonNode>()
                array.forEach { item ->
                    children.add(jsonElementToJsonNode(item, UUID.randomUUID().toString()))
                }
                JsonNode.ArrayNode(id = id, children = children)
            }
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                val value = primitive.asString
                val type = when {
                    primitive.isBoolean -> ValueType.BOOLEAN
                    primitive.isNumber -> ValueType.NUMBER
                    primitive.isString -> ValueType.STRING
                    else -> ValueType.STRING
                }
                JsonNode.ValueNode(id = id, value = value, type = type)
            }
            element.isJsonNull -> {
                JsonNode.ValueNode(id = id, value = "null", type = ValueType.NULL)
            }
            else -> {
                JsonNode.ValueNode(id = id, value = element.toString(), type = ValueType.STRING)
            }
        }
    }
}

