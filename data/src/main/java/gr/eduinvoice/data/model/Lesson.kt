// Lesson.kt - Fixed data model with proper defaults
package gr.eduinvoice.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

@Serializable
@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["date"]),
        Index(value = ["masterId"]),
        Index(value = ["invoiceMasterId"]),
        Index(value = ["paymentBatchId"]),
        Index(value = ["ownerId", "studentId", "date"], name = "idx_lessons_owner_student_date"),
        Index(value = ["ownerId", "groupId", "date", "startTime", "durationMinutes"], name = "idx_lessons_group_time"),
        Index(value = ["ownerId", "isPaid"], name = "idx_lessons_owner_paid"),
        Index(value = ["ownerId", "isInvoiced"], name = "idx_lessons_owner_invoiced"),
        Index(value = ["ownerId", "masterId"], name = "idx_lessons_owner_master")
    ]
)
data class Lesson(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val ownerId: Long = 0,
    val studentId: Long,
    val groupId: Long? = null,
    val masterId: Long? = null,
    val invoiceMasterId: Long? = null,
    val paymentBatchId: Long? = null,
    val date: String, // Store as ISO date string (yyyy-MM-dd)
    val startTime: String, // Store as time string (HH:mm)
    val durationMinutes: Int,
    val notes: String? = null,
    @ColumnInfo(defaultValue = "0")
    val isPaid: Boolean = false, // Default to false (0 in database)
    @ColumnInfo(defaultValue = "0")
    val isInvoiced: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val lastModified: Long = System.currentTimeMillis()
) {
    // Helper functions for date/time conversion
    fun getLocalDate(): LocalDate = LocalDate.parse(date)
    fun getLocalTime(): LocalTime = LocalTime.parse(startTime)

    companion object {
        fun create(
            studentId: Long,
            groupId: Long? = null,
            date: LocalDate,
            startTime: LocalTime,
            durationMinutes: Int,
            notes: String? = null,
            isPaid: Boolean = false,
            isInvoiced: Boolean = false,
            ownerId: Long = 0
        ): Lesson {
            return Lesson(
                ownerId = ownerId,
                studentId = studentId,
                groupId = groupId,
                date = date.toString(),
                startTime = startTime.toString(),
                durationMinutes = durationMinutes,
                notes = notes,
                isPaid = isPaid,
                isInvoiced = isInvoiced
            )
        }
    }
}
