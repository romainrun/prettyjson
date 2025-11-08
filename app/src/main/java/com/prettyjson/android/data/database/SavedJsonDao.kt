package com.prettyjson.android.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SavedJson entities
 */
@Dao
interface SavedJsonDao {
    @Query("SELECT * FROM saved_jsons ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<SavedJson>>
    
    @Query("SELECT * FROM saved_jsons WHERE id = :id")
    suspend fun getById(id: Int): SavedJson?
    
    @Query("SELECT * FROM saved_jsons WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavorites(): Flow<List<SavedJson>>
    
    @Query("SELECT * FROM saved_jsons ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<SavedJson>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savedJson: SavedJson)
    
    @Update
    suspend fun update(savedJson: SavedJson)
    
    @Delete
    suspend fun delete(savedJson: SavedJson)
    
    @Query("DELETE FROM saved_jsons WHERE id = :id")
    suspend fun deleteById(id: Int)
}

