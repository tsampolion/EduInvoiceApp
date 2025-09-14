package gr.eduinvoice.domain.utils

import gr.eduinvoice.domain.billing.BillingService
import gr.eduinvoice.domain.billing.calculateFeeWith
import gr.eduinvoice.domain.billing.Fixtures
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class DomainFeeCalculatorTest {

    @Test
    fun `fee with standard rate and duration`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = null)
        val lesson = Fixtures.sampleDomainLesson(defaultRate = 50.0, durationMinutes = 60)
        val actualFee = BillingService.fee(lesson, student)
        assertEquals(50.0, actualFee, 0.001)
    }

    @Test
    fun `fee with zero duration`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = null)
        val lesson = Fixtures.sampleDomainLesson(defaultRate = 50.0, durationMinutes = 0)
        val actualFee = lesson.calculateFeeWith(student)
        assertEquals(0.0, actualFee, 0.001)
    }

    @Test
    fun `fee with zero hourly rate`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = 0.0)
        val lesson = Fixtures.sampleDomainLesson(defaultRate = null, durationMinutes = 60)
        val actualFee = BillingService.fee(lesson, student)
        assertEquals(0.0, actualFee, 0.001)
        assertNotEquals(50.0, actualFee, 0.001)
    }

    @Test
    fun `fee with fractional duration`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = null)
        val lesson = Fixtures.sampleDomainLesson(defaultRate = 50.0, durationMinutes = 90)
        val actualFee = lesson.calculateFeeWith(student)
        assertEquals(75.0, actualFee, 0.001)
    }

    @Test
    fun `fee with short duration`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = null)
        val lesson = Fixtures.sampleDomainLesson(defaultRate = 50.0, durationMinutes = 30)
        val actualFee = BillingService.fee(lesson, student)
        assertEquals(25.0, actualFee, 0.001)
    }

    @Test
    fun `fee with long duration`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = null)
        val lesson = Fixtures.sampleDomainLesson(defaultRate = 50.0, durationMinutes = 120)
        val actualFee = lesson.calculateFeeWith(student)
        assertEquals(100.0, actualFee, 0.001)
    }

    @Test
    fun `fee with large hourly rate`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = 1000.0)
        val lesson = Fixtures.sampleDomainLesson(defaultRate = null, durationMinutes = 60)
        val actualFee = BillingService.fee(lesson, student)
        assertEquals(1000.0, actualFee, 0.001)
    }

    @Test
    fun `fee with multiple lessons`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = null)
        val lessons = listOf(
            Fixtures.sampleDomainLesson(defaultRate = 50.0, durationMinutes = 60),
            Fixtures.sampleDomainLesson(defaultRate = 60.0, durationMinutes = 90),
            Fixtures.sampleDomainLesson(defaultRate = 40.0, durationMinutes = 45)
        )
        val expectedFees = listOf(50.0, 90.0, 30.0)
        val actualFees = lessons.map { it.calculateFeeWith(student) }
        assertEquals(expectedFees[0], actualFees[0], 0.001)
        assertEquals(expectedFees[1], actualFees[1], 0.001)
        assertEquals(expectedFees[2], actualFees[2], 0.001)
    }

    @Test
    fun `total fee with multiple lessons`() {
        val student = Fixtures.sampleDomainStudent(hourlyRate = null)
        val lessons = listOf(
            Fixtures.sampleDomainLesson(defaultRate = 50.0, durationMinutes = 60),
            Fixtures.sampleDomainLesson(defaultRate = 60.0, durationMinutes = 90),
            Fixtures.sampleDomainLesson(defaultRate = 40.0, durationMinutes = 45)
        )
        val expectedTotalFee = 170.0
        val actualTotalFee = lessons.sumOf { it.calculateFeeWith(student) }
        assertEquals(expectedTotalFee, actualTotalFee, 0.001)
    }
}
