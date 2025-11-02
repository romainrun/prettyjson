package re.weare.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a reusable JSON object/template
 */
@Entity(tableName = "reusable_objects")
data class ReusableObject(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val content: String,
    val createdAt: Long = Date().time,
    val updatedAt: Long = Date().time,
    val isFavorite: Boolean = false
)

