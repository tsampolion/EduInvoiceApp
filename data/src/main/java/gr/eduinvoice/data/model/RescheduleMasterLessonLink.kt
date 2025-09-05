package gr.eduinvoice.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "reschedule_master_lessons",
    primaryKeys = ["masterId", "lessonId"]
)
data class RescheduleMasterLessonLink(
    val masterId: Long,
    val lessonId: Long
)
