package gr.eduinvoice.domain.billing

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class BillingServiceTest {
    
    @Test 
    fun `0 minutes yields 0`() {
        val student = DomainStudent(
            id = 1, 
            name = "A", 
            hourlyRate = 20.0,
            rate = 20.0,
            surname = "Student",
            parentMobile = "1234567890",
            className = "Math"
        )
        val lesson = DomainLesson(
            id = 1, 
            studentId = 1, 
            durationMinutes = 0, 
            defaultRate = 15.0,
            date = "2024-01-01",
            startTime = "10:00"
        )
        assertEquals(0.0, BillingService.fee(lesson, student), 0.001)
    }
    
    @Test 
    fun `uses student rate when present`() {
        val student = DomainStudent(
            id = 1, 
            name = "A", 
            hourlyRate = 24.0,
            rate = 20.0,
            surname = "Student",
            parentMobile = "1234567890",
            className = "Math"
        )
        val lesson = DomainLesson(
            id = 1, 
            studentId = 1, 
            durationMinutes = 90, 
            defaultRate = 15.0,
            date = "2024-01-01",
            startTime = "10:00"
        )
        assertEquals(36.0, BillingService.fee(lesson, student), 0.001) // 1.5h * 24
    }
    
    @Test 
    fun `falls back to lesson default rate`() {
        val student = DomainStudent(
            id = 1, 
            name = "A", 
            hourlyRate = null,
            rate = 20.0,
            surname = "Student",
            parentMobile = "1234567890",
            className = "Math"
        )
        val lesson = DomainLesson(
            id = 1, 
            studentId = 1, 
            durationMinutes = 60, 
            defaultRate = 18.0,
            date = "2024-01-01",
            startTime = "10:00"
        )
        assertEquals(18.0, BillingService.fee(lesson, student), 0.001)
    }
    
    @Test
    fun `falls back to 0 rate when neither student nor lesson rate available`() {
        val student = DomainStudent(
            id = 1, 
            name = "A", 
            hourlyRate = null,
            rate = 20.0,
            surname = "Student",
            parentMobile = "1234567890",
            className = "Math"
        )
        val lesson = DomainLesson(
            id = 1, 
            studentId = 1, 
            durationMinutes = 60, 
            defaultRate = null,
            date = "2024-01-01",
            startTime = "10:00"
        )
        assertEquals(0.0, BillingService.fee(lesson, student), 0.001)
    }
    
    @Test
    fun `handles partial hours correctly`() {
        val student = DomainStudent(
            id = 1, 
            name = "A", 
            hourlyRate = 30.0,
            rate = 30.0,
            surname = "Student",
            parentMobile = "1234567890",
            className = "Math"
        )
        val lesson = DomainLesson(
            id = 1, 
            studentId = 1, 
            durationMinutes = 30, 
            defaultRate = 20.0,
            date = "2024-01-01",
            startTime = "10:00"
        )
        assertEquals(15.0, BillingService.fee(lesson, student), 0.001) // 0.5h * 30
    }
    
    @Test
    fun `rounds to 2 decimal places correctly`() {
        val student = DomainStudent(
            id = 1, 
            name = "A", 
            hourlyRate = 25.0,
            rate = 25.0,
            surname = "Student",
            parentMobile = "1234567890",
            className = "Math"
        )
        val lesson = DomainLesson(
            id = 1, 
            studentId = 1, 
            durationMinutes = 45, 
            defaultRate = 20.0,
            date = "2024-01-01",
            startTime = "10:00"
        )
        assertEquals(18.75, BillingService.fee(lesson, student), 0.001) // 0.75h * 25
    }
    
    @Test
    fun `extension function works correctly`() {
        val student = DomainStudent(
            id = 1, 
            name = "A", 
            hourlyRate = 40.0,
            rate = 40.0,
            surname = "Student",
            parentMobile = "1234567890",
            className = "Math"
        )
        val lesson = DomainLesson(
            id = 1, 
            studentId = 1, 
            durationMinutes = 120, 
            defaultRate = 20.0,
            date = "2024-01-01",
            startTime = "10:00"
        )
        assertEquals(80.0, lesson.calculateFeeWith(student), 0.001) // 2h * 40
    }
    
    @Test
    fun `handles edge case of very short duration`() {
        val student = DomainStudent(
            id = 1, 
            name = "A", 
            hourlyRate = 100.0,
            rate = 100.0,
            surname = "Student",
            parentMobile = "1234567890",
            className = "Math"
        )
        val lesson = DomainLesson(
            id = 1, 
            studentId = 1, 
            durationMinutes = 1, 
            defaultRate = 50.0,
            date = "2024-01-01",
            startTime = "10:00"
        )
        assertEquals(1.67, BillingService.fee(lesson, student), 0.001) // 1/60h * 100 = 1.666... rounded to 1.67
    }
    
    @Test
    fun `handles edge case of very long duration`() {
        val student = DomainStudent(
            id = 1, 
            name = "A", 
            hourlyRate = 10.0,
            rate = 10.0,
            surname = "Student",
            parentMobile = "1234567890",
            className = "Math"
        )
        val lesson = DomainLesson(
            id = 1, 
            studentId = 1, 
            durationMinutes = 480, 
            defaultRate = 15.0,
            date = "2024-01-01",
            startTime = "10:00"
        )
        assertEquals(80.0, BillingService.fee(lesson, student), 0.001) // 8h * 10
    }
}
