package gr.eduinvoice.utils

import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.ui.model.UiInvoiceLesson
import java.time.LocalDate
import java.time.LocalTime

object PdfFixtures {
    fun sampleDomainInvoiceData(): DomainInvoiceData {
        // Create a realistic DomainStudent
        val student = DomainStudent(
            id = 1L,
            ownerId = 1L,
            name = "John",
            surname = "Doe",
            parentMobile = "+306912345678",
            parentEmail = "parent@example.com",
            className = "10th Grade",
            rate = 25.0,
            hourlyRate = 30.0,
            rateType = "hourly",
            isActive = true,
            lastModified = System.currentTimeMillis()
        )

        // Create a realistic DomainLesson
        val lesson = DomainLesson(
            id = 1L,
            ownerId = 1L,
            studentId = 1L,
            groupId = null,
            date = "2024-01-15",
            startTime = "14:00",
            durationMinutes = 60,
            notes = "Math tutoring session - Algebra fundamentals",
            defaultRate = 25.0,
            isPaid = false,
            isInvoiced = false,
            lastModified = System.currentTimeMillis()
        )

        // Create UiInvoiceLesson combining the lesson and student
        val uiInvoiceLesson = UiInvoiceLesson(lesson, student)

        // Return DomainInvoiceData with the student and lesson
        return DomainInvoiceData(
            student = student,
            lessons = listOf(uiInvoiceLesson),
            invoiceDate = LocalDate.of(2024, 1, 15),
            invoiceNumber = "INV-20240115-001"
        )
    }
}
