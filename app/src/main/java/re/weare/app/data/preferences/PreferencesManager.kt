package re.weare.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manages app preferences using DataStore
 */
class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore
    
    companion object {
        private val KEY_THEME = stringPreferencesKey("theme") // "light", "dark", "system", "dracula", "solarized", "onedark"
        private val KEY_THEME_STYLE = stringPreferencesKey("theme_style") // "dracula", "solarized", "onedark", "default"
        private val KEY_FONT_FAMILY = stringPreferencesKey("font_family") // "jetbrains", "fira", "default"
        private val KEY_KEY_CASE_STYLE = stringPreferencesKey("key_case_style") // "camelCase", "snake_case", "PascalCase"
        private val KEY_DISPLAY_MODE = stringPreferencesKey("display_mode") // "raw", "tree"
        private val KEY_LAST_JSON = stringPreferencesKey("last_json") // Last opened JSON content
        private val KEY_CURSOR_POSITION = intPreferencesKey("cursor_position") // Last cursor position
        private val KEY_HAS_SEEN_INTRO = booleanPreferencesKey("has_seen_intro") // Intro tutorial seen flag
        private val KEY_TEXT_SIZE = intPreferencesKey("text_size") // Text size in sp
    }
    
    // Theme preference
    val theme: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "system"
    }
    
    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }
    
    // Key case style preference
    val keyCaseStyle: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_KEY_CASE_STYLE] ?: "camelCase"
    }
    
    suspend fun setKeyCaseStyle(style: String) {
        dataStore.edit { preferences ->
            preferences[KEY_KEY_CASE_STYLE] = style
        }
    }
    
    // Display mode preference
    val displayMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_DISPLAY_MODE] ?: "raw"
    }
    
    suspend fun setDisplayMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_DISPLAY_MODE] = mode
        }
    }
    
    // Theme style preference
    val themeStyle: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_THEME_STYLE] ?: "default"
    }
    
    suspend fun setThemeStyle(style: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_STYLE] = style
        }
    }
    
    // Font family preference
    val fontFamily: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_FONT_FAMILY] ?: "jetbrains"
    }
    
    suspend fun setFontFamily(family: String) {
        dataStore.edit { preferences ->
            preferences[KEY_FONT_FAMILY] = family
        }
    }
    
    // Last JSON content
    val lastJson: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_LAST_JSON]
    }
    
    suspend fun setLastJson(json: String) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_JSON] = json
        }
    }
    
    // Cursor position
    val cursorPosition: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_CURSOR_POSITION] ?: 0
    }
    
    suspend fun setCursorPosition(position: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_CURSOR_POSITION] = position
        }
    }
    
    // Intro tutorial seen flag
    val hasSeenIntro: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_HAS_SEEN_INTRO] ?: false
    }
    
    suspend fun setHasSeenIntro(seen: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAS_SEEN_INTRO] = seen
        }
    }
    
    // Text size preference (default 14sp)
    val textSize: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_TEXT_SIZE] ?: 14
    }
    
    suspend fun setTextSize(size: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_TEXT_SIZE] = size
        }
    }
}


