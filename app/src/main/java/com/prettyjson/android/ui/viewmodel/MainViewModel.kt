package com.prettyjson.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.prettyjson.android.data.preferences.PreferencesManager
import com.prettyjson.android.data.repository.SavedJsonRepository
import kotlinx.coroutines.flow.first
import com.prettyjson.android.data.repository.UrlLoader
import com.prettyjson.android.util.ErrorLocation
import com.prettyjson.android.util.JsonFormatter
import com.prettyjson.android.util.KeyCaseStyle

/**
 * ViewModel for the main JSON formatting/validation screen
 */
class MainViewModel(
    private val preferencesManager: PreferencesManager,
    private val savedJsonRepository: SavedJsonRepository,
    private val urlLoader: UrlLoader
) : ViewModel() {
    
    private val _jsonInput = MutableStateFlow("")
    val jsonInput: StateFlow<String> = _jsonInput.asStateFlow()
    
    private val _jsonOutput = MutableStateFlow("")
    val jsonOutput: StateFlow<String> = _jsonOutput.asStateFlow()
    
    private val _isValid = MutableStateFlow<Boolean?>(null)
    val isValid: StateFlow<Boolean?> = _isValid.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _errorLocation = MutableStateFlow<ErrorLocation?>(null)
    val errorLocation: StateFlow<ErrorLocation?> = _errorLocation.asStateFlow()
    
    private val _isLoadingUrl = MutableStateFlow(false)
    val isLoadingUrl: StateFlow<Boolean> = _isLoadingUrl.asStateFlow()
    
    private val _displayMode = MutableStateFlow("raw")
    val displayMode: StateFlow<String> = _displayMode.asStateFlow()
    
    private val _keyCaseStyle = MutableStateFlow(KeyCaseStyle.CAMEL_CASE)
    val keyCaseStyle: StateFlow<KeyCaseStyle> = _keyCaseStyle.asStateFlow()
    
    private val _tabSpaces = MutableStateFlow(2)
    val tabSpaces: StateFlow<Int> = _tabSpaces.asStateFlow()
    
    // Undo/Redo history
    private var _history = mutableListOf<String>()
    private var _historyIndex = -1
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()
    
    init {
        // Load preferences
        viewModelScope.launch {
            preferencesManager.displayMode.collect { mode ->
                _displayMode.value = mode
            }
        }
        
        // Use default CAMEL_CASE (key case style feature removed from settings)
        _keyCaseStyle.value = KeyCaseStyle.CAMEL_CASE
        
        // Real-time validation and auto-formatting with debounce (async for performance)
        @OptIn(kotlinx.coroutines.FlowPreview::class)
        viewModelScope.launch {
            _jsonInput
                .debounce(500)
                .collect { input ->
                    if (input.isNotEmpty()) {
                        // Validate on main thread (fast operation)
                        validateJson(input)
                        
                        // Auto-format when JSON becomes valid (async for large files)
                        val result = JsonFormatter.validate(input)
                        if (result.isValid) {
                            // Format on background thread for large files
                            withContext(Dispatchers.Default) {
                                val formatResult = JsonFormatter.format(input, _tabSpaces.value)
                                if (formatResult.success) {
                                    _jsonOutput.value = formatResult.content
                                    // Auto-save recent version
                                    autoSaveRecent(formatResult.content)
                                }
                            }
                        }
                    } else {
                        _jsonOutput.value = ""
                    }
                }
        }
    }
    
    fun setJsonInput(input: String, addToHistory: Boolean = true) {
        if (addToHistory && input != _jsonInput.value) {
            // Remove any redo history beyond current index
            if (_historyIndex >= 0 && _historyIndex < _history.size - 1) {
                _history = _history.subList(0, _historyIndex + 1).toMutableList()
            }
            // Add to history
            _history.add(input)
            _historyIndex = _history.size - 1
            // Limit history size to 50 entries
            if (_history.size > 50) {
                _history.removeAt(0)
                _historyIndex--
            }
            updateUndoRedoState()
        }
        _jsonInput.value = input
    }
    
    fun undo() {
        if (_historyIndex > 0) {
            _historyIndex--
            _jsonInput.value = _history[_historyIndex]
            updateUndoRedoState()
            formatJson()
        }
    }
    
    fun redo() {
        if (_historyIndex < _history.size - 1) {
            _historyIndex++
            _jsonInput.value = _history[_historyIndex]
            updateUndoRedoState()
            formatJson()
        }
    }
    
    private fun updateUndoRedoState() {
        _canUndo.value = _historyIndex > 0
        _canRedo.value = _historyIndex < _history.size - 1
    }
    
    fun formatJson() {
        viewModelScope.launch {
            // Format on background thread for performance
            withContext(Dispatchers.Default) {
                val result = JsonFormatter.format(_jsonInput.value, _tabSpaces.value)
                if (result.success) {
                    _jsonOutput.value = result.content
                    _errorMessage.value = null
                    // Auto-save recent version
                    autoSaveRecent(result.content)
                } else {
                    _errorMessage.value = result.errorMessage
                }
            }
        }
    }
    
    fun setTabSpaces(spaces: Int) {
        _tabSpaces.value = spaces.coerceIn(1, 10)
        // Reformat if we have valid JSON
        if (_jsonInput.value.isNotEmpty()) {
            val validation = JsonFormatter.validate(_jsonInput.value)
            if (validation.isValid) {
                formatJson()
            }
        }
    }
    
    fun minifyJson() {
        val result = JsonFormatter.minify(_jsonInput.value)
        if (result.success) {
            _jsonOutput.value = result.content
            _errorMessage.value = null
        } else {
            _errorMessage.value = result.errorMessage
        }
    }
    
    fun sortKeys(order: com.prettyjson.android.util.JsonFormatter.SortOrder = com.prettyjson.android.util.JsonFormatter.SortOrder.ASC, 
                 sortBy: com.prettyjson.android.util.JsonFormatter.SortBy = com.prettyjson.android.util.JsonFormatter.SortBy.KEY) {
        viewModelScope.launch {
            // Sort on background thread for large files
            withContext(Dispatchers.Default) {
                val result = JsonFormatter.sortKeys(_jsonInput.value, order, sortBy)
                if (result.success) {
                    setJsonInput(result.content, addToHistory = true)
                    _jsonOutput.value = result.content
                    _errorMessage.value = null
                    validateJson(_jsonInput.value)
                    // Auto-save recent version
                    autoSaveRecent(result.content)
                } else {
                    _errorMessage.value = result.errorMessage
                }
            }
        }
    }
    
    fun validateJson(input: String = _jsonInput.value) {
        val result = JsonFormatter.validate(input)
        _isValid.value = result.isValid
        if (!result.isValid) {
            _errorMessage.value = result.errorMessage
            // Try to extract error location for highlighting
            try {
                JsonFormatter.validate(input) // Will throw exception
            } catch (e: Exception) {
                _errorLocation.value = JsonFormatter.extractErrorLocation(e, input)
            }
        } else {
            _errorMessage.value = null
            _errorLocation.value = null
        }
    }
    
    fun loadJsonFromUrl(url: String) {
        viewModelScope.launch {
            _isLoadingUrl.value = true
            _errorMessage.value = null
            _errorLocation.value = null
            
            urlLoader.loadJsonFromUrl(url)
                .onSuccess { json ->
                    _jsonInput.value = json
                    validateJson(json)
                    formatJson()
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to load JSON from URL: ${error.message}"
                    _isValid.value = false
                }
            
            _isLoadingUrl.value = false
        }
    }
    
    fun applyKeyCaseStyle() {
        val result = JsonFormatter.formatKeyCase(_jsonInput.value, _keyCaseStyle.value)
        if (result.success) {
            _jsonInput.value = result.content
            _jsonOutput.value = result.content
        } else {
            _errorMessage.value = result.errorMessage
        }
    }
    
    fun copyToClipboard() {
        // This will be handled in the UI layer
    }
    
    fun pasteFromClipboard() {
        // This will be handled in the UI layer
    }
    
    suspend fun saveJson(name: String) {
        val jsonToSave = _jsonOutput.value.ifEmpty { _jsonInput.value }
        if (jsonToSave.isNotEmpty()) {
            savedJsonRepository.saveJson(
                com.prettyjson.android.data.database.SavedJson(
                    name = name,
                    content = jsonToSave
                )
            )
        }
    }
    
    fun clear() {
        _jsonInput.value = ""
        _jsonOutput.value = ""
        _isValid.value = null
        _errorMessage.value = null
        // Clear history when clearing
        _history.clear()
        _historyIndex = -1
        updateUndoRedoState()
    }
    
    suspend fun getRecentFiles(limit: Int = 10): List<com.prettyjson.android.data.database.SavedJson> {
        return savedJsonRepository.getRecent(limit)
    }
    
    suspend fun autoSaveRecent(content: String) {
        if (content.isNotEmpty() && content.length > 10) {
            savedJsonRepository.saveRecentJson("Recent JSON", content)
            // Also save as last JSON for restoration
            preferencesManager.setLastJson(content)
        }
    }
    
    suspend fun restoreLastJson(): String? {
        return preferencesManager.lastJson.first()
    }
    
    suspend fun saveCursorPosition(position: Int) {
        preferencesManager.setCursorPosition(position)
    }
    
    suspend fun restoreCursorPosition(): Int {
        return preferencesManager.cursorPosition.first()
    }
    
    fun reorderJson(reorderedJson: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                // Validate the reordered JSON
                val validation = JsonFormatter.validate(reorderedJson)
                if (validation.isValid) {
                    // Format it with current tab spaces
                    val formatted = JsonFormatter.format(reorderedJson, _tabSpaces.value)
                    if (formatted.success) {
                        _jsonInput.value = reorderedJson
                        _jsonOutput.value = formatted.content
                        autoSaveRecent(formatted.content)
                    }
                }
            }
        }
    }
}

