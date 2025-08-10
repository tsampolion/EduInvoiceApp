package gr.eduinvoice.domain.billing

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DomainInvoiceDataTest {
    
    @Test
    fun `should create invoice data with default values`() {
        val student = DomainStudent(
            id = 1L,
            name = "Test",
            surname = "Student",
            hourlyRate = 20.0
        )
        val lesson = DomainLesson(
            id = 1L,
            studentId = 1L,
            durationMinutes = 60
        )
        
        val invoiceData = DomainInvoiceData(
            student = student,
            lessons = listOf(lesson)
        )
        
        assertEquals(student, invoiceData.student)
        assertEquals(listOf(lesson), invoiceData.lessons)
        assertEquals(LocalDate.now(), invoiceData.invoiceDate)
        assertTrue(invoiceData.invoiceNumber.startsWith("INV-"))
    }
    
    @Test
    fun `should create invoice data with custom values`() {
        val student = DomainStudent(
            id = 1L,
            name = "Test",
            surname = "Student",
            hourlyRate = 20.0
        )
        val lesson = DomainLesson(
            id = 1L,
            studentId = 1L,
            durationMinutes = 60
        )
        val customDate = LocalDate.of(2024, 1, 15)
        val customNumber = "CUSTOM-001"
        
        val invoiceData = DomainInvoiceData(
            student = student,
            lessons = listOf(lesson),
            invoiceDate = customDate,
            invoiceNumber = customNumber
        )
        
        assertEquals(customDate, invoiceData.invoiceDate)
        assertEquals(customNumber, invoiceData.invoiceNumber)
    }
    
    @Test
    fun `should generate sequential invoice numbers`() {
        val student = DomainStudent(
            id = 1L,
            name = "Test",
            surname = "Student",
            hourlyRate = 20.0
        )
        val lesson = DomainLesson(
            id = 1L,
            studentId = 1L,
            durationMinutes = 60
        )
        
        val invoice1 = DomainInvoiceData(student = student, lessons = listOf(lesson))
        val invoice2 = DomainInvoiceData(student = student, lessons = listOf(lesson))
        
        assertNotEquals(invoice1.invoiceNumber, invoice2.invoiceNumber)
        assertTrue(invoice1.invoiceNumber.contains("1"))
        assertTrue(invoice2.invoiceNumber.contains("2"))
    }
}
