package com.prettyjson.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.prettyjson.android.data.preferences.PreferencesManager

/**
 * ViewModel for the Settings screen
 */
class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    val theme: StateFlow<String> = preferencesManager.theme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "system"
    )
    
    fun setTheme(theme: String) {
        viewModelScope.launch {
            preferencesManager.setTheme(theme)
        }
    }
    
    val themeStyle: StateFlow<String> = preferencesManager.themeStyle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )
    
    fun setThemeStyle(style: String) {
        viewModelScope.launch {
            preferencesManager.setThemeStyle(style)
        }
    }
    
    val fontFamily: StateFlow<String> = preferencesManager.fontFamily.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "jetbrains"
    )
    
    fun setFontFamily(family: String) {
        viewModelScope.launch {
            preferencesManager.setFontFamily(family)
        }
    }
    
    val hasSeenIntro: StateFlow<Boolean> = preferencesManager.hasSeenIntro.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    suspend fun setHasSeenIntro(seen: Boolean) {
        preferencesManager.setHasSeenIntro(seen)
    }
    
    val textSize: StateFlow<Int> = preferencesManager.textSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 14
    )
    
    fun setTextSize(size: Int) {
        viewModelScope.launch {
            preferencesManager.setTextSize(size)
        }
    }
    
    val lineWrapping: StateFlow<Boolean> = preferencesManager.lineWrapping.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    fun setLineWrapping(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setLineWrapping(enabled)
        }
    }
    
    val formatOnPaste: StateFlow<Boolean> = preferencesManager.formatOnPaste.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    fun setFormatOnPaste(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setFormatOnPaste(enabled)
        }
    }
}


