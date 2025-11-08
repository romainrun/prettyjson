package com.prettyjson.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.prettyjson.android.data.billing.ProManager

/**
 * ViewModel for managing Pro Plan features
 */
class ProViewModel(
    private val proManager: ProManager
) : ViewModel() {
    
    val isProUser: StateFlow<Boolean> = proManager.isProUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    /**
     * Check if user has Pro (for immediate UI checks)
     */
    suspend fun hasPro(): Boolean {
        return proManager.hasPro()
    }
    
    /**
     * Get dev mode Pro status
     */
    val devModePro: StateFlow<Boolean> = proManager.devModePro.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    /**
     * Set dev mode Pro status (for testing only)
     */
    fun setDevModePro(enabled: Boolean) {
        viewModelScope.launch {
            proManager.setDevModePro(enabled)
        }
    }
}

