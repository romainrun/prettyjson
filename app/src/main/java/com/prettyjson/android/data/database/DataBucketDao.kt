package com.prettyjson.android.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for DataBucket entities
 */
@Dao
interface DataBucketDao {
    @Query("SELECT * FROM data_buckets ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<DataBucket>>
    
    @Query("SELECT * FROM data_buckets WHERE id = :id")
    suspend fun getById(id: Int): DataBucket?
    
    @Query("SELECT * FROM data_buckets WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavorites(): Flow<List<DataBucket>>
    
    @Query("SELECT * FROM data_buckets ORDER BY keyName ASC")
    fun getAllSortedByName(): Flow<List<DataBucket>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dataBucket: DataBucket)
    
    @Update
    suspend fun update(dataBucket: DataBucket)
    
    @Delete
    suspend fun delete(dataBucket: DataBucket)
    
    @Query("DELETE FROM data_buckets WHERE id = :id")
    suspend fun deleteById(id: Int)
}

