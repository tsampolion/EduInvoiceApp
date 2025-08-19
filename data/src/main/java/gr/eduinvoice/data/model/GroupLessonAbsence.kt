package gr.eduinvoice.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "group_lesson_absences",
    foreignKeys = [
        ForeignKey(
            entity = GroupLessonMaster::class,
            parentColumns = ["id"],
            childColumns = ["groupLessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupLessonId"]), Index(value = ["studentId"])]
)
data class GroupLessonAbsence(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long = 0,
    val groupLessonId: Long,
    val studentId: Long
)
