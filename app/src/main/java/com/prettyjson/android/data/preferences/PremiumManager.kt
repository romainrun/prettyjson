package com.prettyjson.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.premiumDataStore: DataStore<Preferences> by preferencesDataStore(name = "premium")

/**
 * Manages premium/pro subscription status
 * 
 * Pricing Recommendation:
 * - One-time purchase: $4.99 (best value, no recurring)
 * - Monthly subscription: $1.99/month
 * - Yearly subscription: $9.99/year (save 58%)
 */
class PremiumManager(context: Context) {
    private val dataStore = context.premiumDataStore
    
    companion object {
        private val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val KEY_PREMIUM_TYPE = stringPreferencesKey("premium_type") // "onetime", "monthly", "yearly"
        private val KEY_PREMIUM_EXPIRES_AT = longPreferencesKey("premium_expires_at") // timestamp for subscriptions
        private val KEY_DEV_MODE_PREMIUM = booleanPreferencesKey("dev_mode_premium") // Development/testing flag to bypass premium checks
    }
    
    /**
     * Check if user has active premium subscription
     * Includes development mode override for testing
     */
    val isPremium: Flow<Boolean> = dataStore.data.map { preferences ->
        // Development mode override (for testing only)
        val devModePremium = preferences[KEY_DEV_MODE_PREMIUM] ?: false
        if (devModePremium) return@map true
        
        val isPremium = preferences[KEY_IS_PREMIUM] ?: false
        if (!isPremium) return@map false
        
        val premiumType = preferences[KEY_PREMIUM_TYPE] ?: "onetime"
        
        // One-time purchase never expires
        if (premiumType == "onetime") return@map true
        
        // Check subscription expiration
        val expiresAt = preferences[KEY_PREMIUM_EXPIRES_AT] ?: 0L
        val now = System.currentTimeMillis()
        
        // Subscription is active if expiration time is in the future
        expiresAt > now
    }
    
    /**
     * Get premium type
     */
    val premiumType: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_PREMIUM_TYPE] ?: "none"
    }
    
    /**
     * Set premium status (called after successful purchase)
     */
    suspend fun setPremium(
        isPremium: Boolean,
        type: String = "onetime",
        expiresAt: Long = 0L
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_PREMIUM] = isPremium
            preferences[KEY_PREMIUM_TYPE] = type
            if (expiresAt > 0) {
                preferences[KEY_PREMIUM_EXPIRES_AT] = expiresAt
            } else {
                preferences.remove(KEY_PREMIUM_EXPIRES_AT)
            }
        }
    }
    
    /**
     * Check if user has premium (synchronous for UI checks)
     * Includes development mode override for testing
     */
    suspend fun hasPremium(): Boolean {
        val preferences = dataStore.data.first()
        
        // Development mode override (for testing only)
        val devModePremium = preferences[KEY_DEV_MODE_PREMIUM] ?: false
        if (devModePremium) return true
        
        val isPremium = preferences[KEY_IS_PREMIUM] ?: false
        if (!isPremium) return false
        
        val premiumType = preferences[KEY_PREMIUM_TYPE] ?: "onetime"
        if (premiumType == "onetime") return true
        
        val expiresAt = preferences[KEY_PREMIUM_EXPIRES_AT] ?: 0L
        return expiresAt > System.currentTimeMillis()
    }
    
    /**
     * Get development mode premium status
     */
    val devModePremium: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_DEV_MODE_PREMIUM] ?: false
    }
    
    /**
     * Set development mode premium status (for testing only)
     */
    suspend fun setDevModePremium(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DEV_MODE_PREMIUM] = enabled
        }
    }
}

