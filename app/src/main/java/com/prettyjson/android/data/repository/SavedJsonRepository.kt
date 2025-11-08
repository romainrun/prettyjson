package com.prettyjson.android.data.repository

import com.prettyjson.android.data.database.AppDatabase
import com.prettyjson.android.data.database.SavedJson
import com.prettyjson.android.util.DateFormatter
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing saved JSON documents
 */
class SavedJsonRepository(private val database: AppDatabase) {
    
    fun getAllSavedJsons(): Flow<List<SavedJson>> {
        return database.savedJsonDao().getAll()
    }
    
    suspend fun getSavedJsonById(id: Int): SavedJson? {
        return database.savedJsonDao().getById(id)
    }
    
    suspend fun saveJson(savedJson: SavedJson) {
        if (savedJson.id == 0) {
            database.savedJsonDao().insert(savedJson.copy(updatedAt = System.currentTimeMillis()))
        } else {
            database.savedJsonDao().update(savedJson.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    
    suspend fun deleteJson(savedJson: SavedJson) {
        database.savedJsonDao().delete(savedJson)
    }
    
    suspend fun deleteJsonById(id: Int) {
        database.savedJsonDao().deleteById(id)
    }
    
    fun getFavorites(): Flow<List<SavedJson>> {
        return database.savedJsonDao().getFavorites()
    }
    
    suspend fun getRecent(limit: Int = 10): List<SavedJson> {
        return database.savedJsonDao().getRecent(limit)
    }
    
    suspend fun saveRecentJson(name: String, content: String) {
        // Auto-save with formatted date + time timestamp
        val timestamp = System.currentTimeMillis()
        val formattedDateTime = DateFormatter.formatDateTimeForFilename(timestamp)
        val savedJson = SavedJson(
            name = name.ifEmpty { "JSON ${formattedDateTime}" },
            content = content,
            updatedAt = timestamp
        )
        database.savedJsonDao().insert(savedJson)
    }
}

