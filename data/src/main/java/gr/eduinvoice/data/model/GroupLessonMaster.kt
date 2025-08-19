package gr.eduinvoice.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "group_lesson_master",
    indices = [Index(value = ["groupId"]), Index(value = ["date"])]
)
data class GroupLessonMaster(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long = 0,
    val groupId: Long,
    val date: String,
    val startTime: String,
    val durationMinutes: Int,
    val notes: String? = null,
    @ColumnInfo(defaultValue = "0")
    val lastModified: Long = System.currentTimeMillis()
)
