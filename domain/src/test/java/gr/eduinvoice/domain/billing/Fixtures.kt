package gr.eduinvoice.domain.billing

import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.model.DomainLesson
import java.time.LocalDate
import java.time.LocalTime

object Fixtures {
    fun sampleDomainStudent(
        id: Long = 1L,
        ownerId: Long = 1L,
        name: String = "John",
        surname: String = "Doe",
        parentMobile: String = "+306912345678",
        parentEmail: String? = "parent@example.com",
        className: String = "10th Grade",
        rate: Double = 25.0,
        hourlyRate: Double? = 30.0,
        rateType: String = "hourly",
        isActive: Boolean = true,
        lastModified: Long = System.currentTimeMillis()
    ): DomainStudent {
        return DomainStudent(
            id = id,
            ownerId = ownerId,
            name = name,
            surname = surname,
            parentMobile = parentMobile,
            parentEmail = parentEmail,
            className = className,
            rate = rate,
            hourlyRate = hourlyRate,
            rateType = rateType,
            isActive = isActive,
            lastModified = lastModified
        )
    }

    fun sampleDomainLesson(
        id: Long = 1L,
        ownerId: Long = 1L,
        studentId: Long = 1L,
        groupId: Long? = null,
        date: String = "2024-01-15",
        startTime: String = "14:00",
        durationMinutes: Int = 60,
        notes: String? = "Math tutoring session",
        defaultRate: Double? = 25.0,
        isPaid: Boolean = false,
        isInvoiced: Boolean = false,
        lastModified: Long = System.currentTimeMillis()
    ): DomainLesson {
        return DomainLesson(
            id = id,
            ownerId = ownerId,
            studentId = studentId,
            groupId = groupId,
            date = date,
            startTime = startTime,
            durationMinutes = durationMinutes,
            notes = notes,
            defaultRate = defaultRate,
            isPaid = isPaid,
            isInvoiced = isInvoiced,
            lastModified = lastModified
        )
    }
}
