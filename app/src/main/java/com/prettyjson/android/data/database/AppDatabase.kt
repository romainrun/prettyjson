package com.prettyjson.android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for PrettyJSON app
 */
@Database(
    entities = [SavedJson::class, ReusableObject::class, DataBucket::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedJsonDao(): SavedJsonDao
    abstract fun reusableObjectDao(): ReusableObjectDao
    abstract fun dataBucketDao(): DataBucketDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prettyjson_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

