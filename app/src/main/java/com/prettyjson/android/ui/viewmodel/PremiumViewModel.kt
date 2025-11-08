package com.prettyjson.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.prettyjson.android.data.preferences.PremiumManager

/**
 * ViewModel for managing premium features
 */
class PremiumViewModel(
    private val premiumManager: PremiumManager
) : ViewModel() {
    
    val isPremium: StateFlow<Boolean> = premiumManager.isPremium.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    val premiumType: StateFlow<String> = premiumManager.premiumType.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "none"
    )
    
    /**
     * Check if user has premium (for immediate UI checks)
     */
    suspend fun hasPremium(): Boolean {
        return premiumManager.hasPremium()
    }
    
    /**
     * Set premium status (called after successful purchase)
     * In production, this should be called after verifying purchase with Play Billing
     */
    fun setPremium(isPremium: Boolean, type: String = "onetime", expiresAt: Long = 0L) {
        viewModelScope.launch {
            premiumManager.setPremium(isPremium, type, expiresAt)
        }
    }
    
    /**
     * Get development mode premium status (for testing only)
     */
    val devModePremium: StateFlow<Boolean> = premiumManager.devModePremium.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    /**
     * Set development mode premium status (for testing only)
     */
    fun setDevModePremium(enabled: Boolean) {
        viewModelScope.launch {
            premiumManager.setDevModePremium(enabled)
        }
    }
}

