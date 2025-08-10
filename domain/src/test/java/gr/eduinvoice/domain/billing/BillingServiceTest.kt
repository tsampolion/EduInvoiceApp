package gr.eduinvoice.domain.billing

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import org.junit.Assert.assertEquals
import org.junit.Test

class BillingServiceTest {
    
    @Test 
    fun `0 minutes yields 0`() {
        val student = DomainStudent(
            id = 1L,
            name = "Alice",
            surname = "Johnson",
            hourlyRate = 20.0,
            rateType = "per_hour"
        )
        val lesson = DomainLesson(
            id = 1L,
            studentId = 1L,
            durationMinutes = 0,
            defaultRate = 15.0
        )
        assertEquals(0.0, BillingService.fee(lesson, student), 0.001)
    }
    
    @Test 
    fun `uses student rate when present`() {
        val student = DomainStudent(
            id = 1L,
            name = "Bob",
            surname = "Smith",
            hourlyRate = 25.0,
            rateType = "per_hour"
        )
        val lesson = DomainLesson(
            id = 1L,
            studentId = 1L,
            durationMinutes = 60,
            defaultRate = 15.0
        )
        assertEquals(25.0, BillingService.fee(lesson, student), 0.001)
    }
    
    @Test 
    fun `falls back to lesson default rate when student rate is null`() {
        val student = DomainStudent(
            id = 1L,
            name = "Charlie",
            surname = "Brown",
            hourlyRate = null,
            rateType = "per_hour"
        )
        val lesson = DomainLesson(
            id = 1L,
            studentId = 1L,
            durationMinutes = 60,
            defaultRate = 18.0
        )
        assertEquals(18.0, BillingService.fee(lesson, student), 0.001)
    }
    
    @Test 
    fun `falls back to 0 when no rates are available`() {
        val student = DomainStudent(
            id = 1L,
            name = "David",
            surname = "Wilson",
            hourlyRate = null,
            rateType = "per_hour"
        )
        val lesson = DomainLesson(
            id = 1L,
            studentId = 1L,
            durationMinutes = 60,
            defaultRate = null
        )
        assertEquals(0.0, BillingService.fee(lesson, student), 0.001)
    }
    
    @Test 
    fun `partial hour calculation is correct`() {
        val student = DomainStudent(
            id = 1L,
            name = "Eve",
            surname = "Davis",
            hourlyRate = 30.0,
            rateType = "per_hour"
        )
        val lesson = DomainLesson(
            id = 1L,
            studentId = 1L,
            durationMinutes = 30,
            defaultRate = 15.0
        )
        assertEquals(15.0, BillingService.fee(lesson, student), 0.001)
    }
    
    @Test 
    fun `90 minutes calculation is correct`() {
        val student = DomainStudent(
            id = 1L,
            name = "Frank",
            surname = "Miller",
            hourlyRate = 24.0,
            rateType = "per_hour"
        )
        val lesson = DomainLesson(
            id = 1L,
            studentId = 1L,
            durationMinutes = 90,
            defaultRate = 15.0
        )
        assertEquals(36.0, BillingService.fee(lesson, student), 0.001)
    }
    
    @Test 
    fun `rounding to 2 decimal places works correctly`() {
        val student = DomainStudent(
            id = 1L,
            name = "Grace",
            surname = "Taylor",
            hourlyRate = 23.33,
            rateType = "per_hour"
        )
        val lesson = DomainLesson(
            id = 1L,
            studentId = 1L,
            durationMinutes = 45,
            defaultRate = 15.0
        )
        // 23.33 * (45/60) = 23.33 * 0.75 = 17.4975 -> 17.50
        assertEquals(17.50, BillingService.fee(lesson, student), 0.001)
    }
    
    @Test 
    fun `extension function works correctly`() {
        val student = DomainStudent(
            id = 1L,
            name = "Henry",
            surname = "Anderson",
            hourlyRate = 40.0,
            rateType = "per_hour"
        )
        val lesson = DomainLesson(
            id = 1L,
            studentId = 1L,
            durationMinutes = 120,
            defaultRate = 15.0
        )
        assertEquals(80.0, lesson.calculateFeeWith(student), 0.001)
    }
}
