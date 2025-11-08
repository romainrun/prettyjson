package com.prettyjson.android.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a data bucket - reusable JSON snippets that can be inserted into JSON
 */
@Entity(tableName = "data_buckets")
data class DataBucket(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val keyName: String, // The key name when inserted into JSON
    val description: String = "", // Optional description
    val valueType: String = "json", // Type: json, array, string, integer, float, boolean, null
    val value: String, // The value respecting the value type
    val createdAt: Long = Date().time,
    val updatedAt: Long = Date().time,
    val isFavorite: Boolean = false
) {
    // Legacy support: for backward compatibility with old "name" field
    @Deprecated("Use keyName instead", ReplaceWith("keyName"))
    val name: String get() = keyName
    
    // Legacy support: for backward compatibility with old "content" field
    @Deprecated("Use value instead", ReplaceWith("value"))
    val content: String get() = value
}

