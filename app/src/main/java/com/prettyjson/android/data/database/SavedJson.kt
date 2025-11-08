package com.prettyjson.android.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a saved JSON document
 */
@Entity(tableName = "saved_jsons")
data class SavedJson(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val content: String,
    val createdAt: Long = Date().time,
    val updatedAt: Long = Date().time,
    val isFavorite: Boolean = false
)

