package gr.eduinvoice.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

@Serializable
data class DomainLesson(
    val id: Long = 0,
    val ownerId: Long = 0,
    val studentId: Long,
    val groupId: Long? = null,
    val date: String, // Store as ISO date string (yyyy-MM-dd)
    val startTime: String, // Store as time string (HH:mm)
    val durationMinutes: Int,
    val notes: String? = null,
    val isPaid: Boolean = false,
    val isInvoiced: Boolean = false,
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
        ): DomainLesson {
            return DomainLesson(
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
