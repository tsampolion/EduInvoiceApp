package gr.tutorbilling.data.model

import androidx.room.Entity
import androidx.room.Index
import gr.tutorbilling.data.database.DatabaseConstants

@Entity(
    tableName = DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE,
    primaryKeys = ["groupId", "studentId"],
    indices = [Index(value = ["studentId"]), Index(value = ["groupId"])]
)
data class GroupStudentCrossRef(
    val groupId: Long,
    val studentId: Long
)
