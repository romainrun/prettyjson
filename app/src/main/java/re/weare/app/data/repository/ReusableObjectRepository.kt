package re.weare.app.data.repository

import re.weare.app.data.database.AppDatabase
import re.weare.app.data.database.ReusableObject
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing reusable JSON objects/templates
 */
class ReusableObjectRepository(private val database: AppDatabase) {
    
    fun getAllReusableObjects(): Flow<List<ReusableObject>> {
        return database.reusableObjectDao().getAll()
    }
    
    suspend fun getReusableObjectById(id: Int): ReusableObject? {
        return database.reusableObjectDao().getById(id)
    }
    
    suspend fun saveReusableObject(reusableObject: ReusableObject) {
        if (reusableObject.id == 0) {
            database.reusableObjectDao().insert(reusableObject.copy(updatedAt = System.currentTimeMillis()))
        } else {
            database.reusableObjectDao().update(reusableObject.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    
    suspend fun deleteReusableObject(reusableObject: ReusableObject) {
        database.reusableObjectDao().delete(reusableObject)
    }
    
    suspend fun deleteReusableObjectById(id: Int) {
        database.reusableObjectDao().deleteById(id)
    }
    
    fun getFavorites(): Flow<List<ReusableObject>> {
        return database.reusableObjectDao().getFavorites()
    }
}

