package com.prettyjson.android.ui.viewmodel

import com.prettyjson.android.data.preferences.PreferencesManager
import com.prettyjson.android.util.JsonNode
import com.prettyjson.android.util.ValueType
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JsonBuilderViewModelTest {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var viewModel: JsonBuilderViewModel

    @Before
    fun setup() {
        preferencesManager = mockk(relaxed = true)
        kotlinx.coroutines.Dispatchers.setMain(StandardTestDispatcher())
        viewModel = JsonBuilderViewModel(preferencesManager)
    }

    @Test
    fun `test initial state`() {
        val rootNode = viewModel.rootNode.value
        assertNotNull(rootNode)
        assertTrue(rootNode is JsonNode.ObjectNode)
        assertEquals(0, (rootNode as JsonNode.ObjectNode).children.size)
    }

    @Test
    fun `test addKeyValuePair`() {
        viewModel.addKeyValuePair("name", "John", ValueType.STRING)
        val rootNode = viewModel.rootNode.value as JsonNode.ObjectNode
        assertEquals(1, rootNode.children.size)
        assertTrue(rootNode.children.containsKey("name"))
        val valueNode = rootNode.children["name"] as JsonNode.ValueNode
        assertEquals("John", valueNode.value)
        assertEquals(ValueType.STRING, valueNode.type)
    }

    @Test
    fun `test removeKeyValuePair`() {
        viewModel.addKeyValuePair("name", "John", ValueType.STRING)
        viewModel.addKeyValuePair("age", "30", ValueType.NUMBER)
        viewModel.removeKeyValuePair("name")
        val rootNode = viewModel.rootNode.value as JsonNode.ObjectNode
        assertEquals(1, rootNode.children.size)
        assertFalse(rootNode.children.containsKey("name"))
        assertTrue(rootNode.children.containsKey("age"))
    }

    @Test
    fun `test updateKeyValuePair`() {
        viewModel.addKeyValuePair("name", "John", ValueType.STRING)
        viewModel.updateKeyValuePair("name", "name", "Jane", ValueType.STRING)
        val rootNode = viewModel.rootNode.value as JsonNode.ObjectNode
        val valueNode = rootNode.children["name"] as JsonNode.ValueNode
        assertEquals("Jane", valueNode.value)
    }

    @Test
    fun `test clear`() {
        viewModel.addKeyValuePair("name", "John", ValueType.STRING)
        viewModel.clear()
        val rootNode = viewModel.rootNode.value as JsonNode.ObjectNode
        assertEquals(0, rootNode.children.size)
    }

    @Test
    fun `test loadFromJson with valid JSON object`() {
        val jsonString = """{"name":"John","age":30}"""
        viewModel.loadFromJson(jsonString)
        val rootNode = viewModel.rootNode.value as JsonNode.ObjectNode
        assertTrue(rootNode.children.size > 0)
        val preview = viewModel.previewJson.value
        assertTrue(preview.contains("name") || preview.contains("John"))
    }

    @Test
    fun `test loadFromJson with invalid JSON`() {
        val invalidJson = "{invalid json}"
        viewModel.loadFromJson(invalidJson)
        val preview = viewModel.previewJson.value
        assertTrue(preview.contains("Error") || preview.contains("error"))
    }

    @Test
    fun `test loadFromJson with nested objects`() {
        val jsonString = """{"user":{"name":"John","age":30}}"""
        viewModel.loadFromJson(jsonString)
        val rootNode = viewModel.rootNode.value as JsonNode.ObjectNode
        assertTrue(rootNode.children.containsKey("user"))
    }

    @Test
    fun `test loadFromJson with arrays`() {
        val jsonString = """{"items":["item1","item2"]}"""
        viewModel.loadFromJson(jsonString)
        val rootNode = viewModel.rootNode.value as JsonNode.ObjectNode
        assertTrue(rootNode.children.containsKey("items"))
    }
}

