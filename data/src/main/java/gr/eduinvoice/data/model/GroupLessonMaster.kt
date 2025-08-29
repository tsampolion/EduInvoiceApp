package gr.eduinvoice.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "group_lesson_master")
data class GroupLessonMaster(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val ownerId: Long = 0,
    val groupId: Long,
    val date: String, // Store as ISO date string (yyyy-MM-dd)
    val startTime: String, // Store as time string (HH:mm)
    val durationMinutes: Int,
    val notes: String? = null,
    @ColumnInfo(defaultValue = "0")
    val lastModified: Long = System.currentTimeMillis()
)
