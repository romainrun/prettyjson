package com.prettyjson.android.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ReusableObject entities
 */
@Dao
interface ReusableObjectDao {
    @Query("SELECT * FROM reusable_objects ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<ReusableObject>>
    
    @Query("SELECT * FROM reusable_objects WHERE id = :id")
    suspend fun getById(id: Int): ReusableObject?
    
    @Query("SELECT * FROM reusable_objects WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavorites(): Flow<List<ReusableObject>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reusableObject: ReusableObject)
    
    @Update
    suspend fun update(reusableObject: ReusableObject)
    
    @Delete
    suspend fun delete(reusableObject: ReusableObject)
    
    @Query("DELETE FROM reusable_objects WHERE id = :id")
    suspend fun deleteById(id: Int)
}

