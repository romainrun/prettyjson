package com.prettyjson.android.data.repository

import com.prettyjson.android.data.database.AppDatabase
import com.prettyjson.android.data.database.DataBucket
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository for managing data buckets
 */
class DataBucketRepository(private val database: AppDatabase) {
    
    fun getAllDataBuckets(): Flow<List<DataBucket>> {
        return database.dataBucketDao().getAll()
    }
    
    fun getAllDataBucketsSorted(): Flow<List<DataBucket>> {
        return database.dataBucketDao().getAllSortedByName()
    }
    
    suspend fun getDataBucketById(id: Int): DataBucket? {
        return database.dataBucketDao().getById(id)
    }
    
    suspend fun saveDataBucket(dataBucket: DataBucket) {
        if (dataBucket.id == 0) {
            database.dataBucketDao().insert(dataBucket.copy(updatedAt = Date().time))
        } else {
            database.dataBucketDao().update(dataBucket.copy(updatedAt = Date().time))
        }
    }
    
    suspend fun deleteDataBucket(dataBucket: DataBucket) {
        database.dataBucketDao().delete(dataBucket)
    }
    
    suspend fun deleteDataBucketById(id: Int) {
        database.dataBucketDao().deleteById(id)
    }
    
    fun getFavorites(): Flow<List<DataBucket>> {
        return database.dataBucketDao().getFavorites()
    }
}

