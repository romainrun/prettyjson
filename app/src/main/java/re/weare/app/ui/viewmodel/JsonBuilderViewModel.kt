package re.weare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import re.weare.app.data.preferences.PreferencesManager
import re.weare.app.util.JsonBuilder
import re.weare.app.util.JsonNode
import re.weare.app.util.KeyCaseStyle
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
        viewModelScope.launch {
            preferencesManager.keyCaseStyle.collect { style ->
                _keyCaseStyle.value = when (style) {
                    "camelCase" -> KeyCaseStyle.CAMEL_CASE
                    "snake_case" -> KeyCaseStyle.SNAKE_CASE
                    "PascalCase" -> KeyCaseStyle.PASCAL_CASE
                    else -> KeyCaseStyle.CAMEL_CASE
                }
                updatePreview()
            }
        }
        
        // Initial preview update
        updatePreview()
    }
    
    fun addKeyValuePair(key: String, value: String, type: re.weare.app.util.ValueType) {
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
    
    fun updateKeyValuePair(oldKey: String, newKey: String, value: String, type: re.weare.app.util.ValueType) {
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
        // This is a simplified version - in production, you'd want more robust parsing
        try {
            val root = JsonNode.ObjectNode(id = UUID.randomUUID().toString())
            // TODO: Implement JSON to JsonNode conversion
            _rootNode.value = root
        } catch (e: Exception) {
            // Handle error
        }
    }
}

