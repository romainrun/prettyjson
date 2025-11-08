package com.prettyjson.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.prettyjson.android.data.database.SavedJson
import com.prettyjson.android.data.repository.SavedJsonRepository

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
    
    fun saveJson(name: String, content: String) {
        viewModelScope.launch {
            val savedJson = SavedJson(
                id = 0,
                name = name,
                content = content,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            savedJsonRepository.saveJson(savedJson)
        }
    }
}









