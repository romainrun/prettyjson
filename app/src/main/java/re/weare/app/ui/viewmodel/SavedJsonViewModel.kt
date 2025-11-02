package re.weare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import re.weare.app.data.database.SavedJson
import re.weare.app.data.repository.SavedJsonRepository

/**
 * ViewModel for the Saved JSONs screen
 */
class SavedJsonViewModel(
    private val savedJsonRepository: SavedJsonRepository
) : ViewModel() {
    
    val savedJsons = savedJsonRepository.getAllSavedJsons()
    
    fun deleteJson(savedJson: SavedJson) {
        viewModelScope.launch {
            savedJsonRepository.deleteJson(savedJson)
        }
    }
    
    fun updateJson(savedJson: SavedJson) {
        viewModelScope.launch {
            savedJsonRepository.saveJson(savedJson)
        }
    }
}



