package com.prettyjson.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.prettyjson.android.data.database.ReusableObject
import com.prettyjson.android.data.repository.ReusableObjectRepository

/**
 * ViewModel for managing reusable JSON objects
 */
class ReusableObjectViewModel(
    private val reusableObjectRepository: ReusableObjectRepository
) : ViewModel() {
    
    val reusableObjects = reusableObjectRepository.getAllReusableObjects()
    
    fun saveReusableObject(reusableObject: ReusableObject) {
        viewModelScope.launch {
            reusableObjectRepository.saveReusableObject(reusableObject)
        }
    }
    
    fun deleteReusableObject(reusableObject: ReusableObject) {
        viewModelScope.launch {
            reusableObjectRepository.deleteReusableObject(reusableObject)
        }
    }
}









