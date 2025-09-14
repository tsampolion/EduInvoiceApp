package gr.eduinvoice.domain.billing

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import gr.eduinvoice.domain.model.DomainLesson

class BillingServiceTest {

    @Test
    fun `fee with single lesson returns correct total`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = null)
        val lesson = Fixtures.sampleDomainLesson(defaultRate = 50.0, durationMinutes = 60)
        val result = BillingService.fee(lesson, student)
        assertEquals(50.0, result, 0.001)
    }

    @Test
    fun `sum of fees with multiple lessons returns correct sum`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = null)
        val lessons = listOf(
            Fixtures.sampleDomainLesson(defaultRate = 50.0, durationMinutes = 60),
            Fixtures.sampleDomainLesson(defaultRate = 75.0, durationMinutes = 60),
            Fixtures.sampleDomainLesson(defaultRate = 25.0, durationMinutes = 60)
        )
        val result = lessons.sumOf { it.calculateFeeWith(student) }
        assertEquals(150.0, result, 0.001)
    }

    @Test
    fun `sum of fees with empty list returns zero`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = null)
        val lessons = emptyList<DomainLesson>()
        val result = lessons.sumOf { it.calculateFeeWith(student) }
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `sum of fees with zero rate lessons returns zero`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = null)
        val lessons = listOf(
            Fixtures.sampleDomainLesson(defaultRate = 0.0, durationMinutes = 60),
            Fixtures.sampleDomainLesson(defaultRate = 0.0, durationMinutes = 60)
        )
        val result = lessons.sumOf { it.calculateFeeWith(student) }
        assertEquals(0.0, result, 0.001)
    }
}
