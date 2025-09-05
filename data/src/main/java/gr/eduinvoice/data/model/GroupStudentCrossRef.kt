package gr.eduinvoice.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable
import gr.eduinvoice.data.database.DatabaseConstants

@Serializable
@Entity(
    tableName = DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE,
    primaryKeys = ["groupId", "studentId"],
    indices = [Index(value = ["studentId"]), Index(value = ["groupId"])]
)
data class GroupStudentCrossRef(
    val groupId: Long,
    val studentId: Long,
    @ColumnInfo(defaultValue = "0")
    val ownerId: Long = 0
)
