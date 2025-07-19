// Lesson.kt - Fixed data model with proper defaults
package gr.tsambala.tutorbilling.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

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
        Index(value = ["date"])
    ]
)
data class Lesson(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: String, // Store as ISO date string (yyyy-MM-dd)
    val startTime: String, // Store as time string (HH:mm)
    val durationMinutes: Int,
    val notes: String? = null,
    @ColumnInfo(defaultValue = "0")
    val isPaid: Boolean = false // Default to false (0 in database)
) {
    // Helper functions for date/time conversion
    fun getLocalDate(): LocalDate = LocalDate.parse(date)
    fun getLocalTime(): LocalTime = LocalTime.parse(startTime)

    companion object {
        fun create(
            studentId: Long,
            date: LocalDate,
            startTime: LocalTime,
            durationMinutes: Int,
            notes: String? = null,
            isPaid: Boolean = false
        ): Lesson {
            return Lesson(
                studentId = studentId,
                date = date.toString(),
                startTime = startTime.toString(),
                durationMinutes = durationMinutes,
                notes = notes,
                isPaid = isPaid
            )
        }
    }
}
