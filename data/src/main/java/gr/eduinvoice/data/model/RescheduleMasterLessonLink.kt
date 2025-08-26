package gr.eduinvoice.data.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
	tableName = "reschedule_master_lessons",
	primaryKeys = ["masterId", "lessonId"],
	indices = [Index(value = ["masterId"]), Index(value = ["lessonId"])]
)
data class RescheduleMasterLessonLink(
	val masterId: Long,
	val lessonId: Long
)
