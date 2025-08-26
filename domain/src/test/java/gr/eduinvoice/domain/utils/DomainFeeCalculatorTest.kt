package gr.eduinvoice.domain.utils

import gr.eduinvoice.domain.billing.DomainFeeCalculator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class DomainFeeCalculatorTest {

    @Test
    fun `calculateFee with standard rate and duration`() {
        val durationMinutes = 60
        val hourlyRate = 50.0
        val expectedFee = 50.0
        val actualFee = DomainFeeCalculator.calculateFee(durationMinutes, hourlyRate)
        assertEquals(expectedFee, actualFee, 0.001)
    }

    @Test
    fun `calculateFee with zero duration`() {
        val durationMinutes = 0
        val hourlyRate = 50.0
        val expectedFee = 0.0
        val actualFee = DomainFeeCalculator.calculateFee(durationMinutes, hourlyRate)
        assertEquals(expectedFee, actualFee, 0.001)
    }

    @Test
    fun `calculateFee with zero hourly rate`() {
        val durationMinutes = 60
        val hourlyRate = 0.0
        val expectedFee = 0.0
        val actualFee = DomainFeeCalculator.calculateFee(durationMinutes, hourlyRate)
        assertEquals(expectedFee, actualFee, 0.001)
        assertNotEquals(50.0, actualFee, 0.001)
    }

    @Test
    fun `calculateFee with fractional duration`() {
        val durationMinutes = 90
        val hourlyRate = 50.0
        val expectedFee = 75.0
        val actualFee = DomainFeeCalculator.calculateFee(durationMinutes, hourlyRate)
        assertEquals(expectedFee, actualFee, 0.001)
    }

    @Test
    fun `calculateFee with short duration`() {
        val durationMinutes = 30
        val hourlyRate = 50.0
        val expectedFee = 25.0
        val actualFee = DomainFeeCalculator.calculateFee(durationMinutes, hourlyRate)
        assertEquals(expectedFee, actualFee, 0.001)
    }

    @Test
    fun `calculateFee with long duration`() {
        val durationMinutes = 120
        val hourlyRate = 50.0
        val expectedFee = 100.0
        val actualFee = DomainFeeCalculator.calculateFee(durationMinutes, hourlyRate)
        assertEquals(expectedFee, actualFee, 0.001)
    }

    @Test
    fun `calculateFee with large hourly rate`() {
        val durationMinutes = 60
        val hourlyRate = 1000.0
        val expectedFee = 1000.0
        val actualFee = DomainFeeCalculator.calculateFee(durationMinutes, hourlyRate)
        assertEquals(expectedFee, actualFee, 0.001)
    }

    @Test
    fun `calculateFee with negative duration returns zero`() {
        val durationMinutes = -60
        val hourlyRate = 50.0
        val expectedFee = 0.0
        val actualFee = DomainFeeCalculator.calculateFee(durationMinutes, hourlyRate)
        assertEquals(expectedFee, actualFee, 0.001)
    }

    @Test
    fun `calculateFee with negative hourly rate returns zero`() {
        val durationMinutes = 60
        val hourlyRate = -50.0
        val expectedFee = 0.0
        val actualFee = DomainFeeCalculator.calculateFee(durationMinutes, hourlyRate)
        assertEquals(expectedFee, actualFee, 0.001)
    }

    @Test
    fun `calculateFee with multiple lessons`() {
        val lessons = listOf(
            60 to 50.0,
            90 to 60.0,
            45 to 40.0
        )
        val expectedFees = listOf(50.0, 90.0, 30.0)
        val actualFees = lessons.map { (duration, rate) ->
            DomainFeeCalculator.calculateFee(duration, rate)
        }
        assertEquals(expectedFees[0], actualFees[0], 0.001)
        assertEquals(expectedFees[1], actualFees[1], 0.001)
        assertEquals(expectedFees[2], actualFees[2], 0.001)
    }

    @Test
    fun `calculateTotalFee with multiple lessons`() {
        val lessons = listOf(
            60 to 50.0,
            90 to 60.0,
            45 to 40.0
        )
        val expectedTotalFee = 170.0 // 50 + 90 + 30
        val fees = lessons.map { (duration, rate) ->
            DomainFeeCalculator.calculateFee(duration, rate)
        }
        val actualTotalFee = fees.sum()
        assertEquals(expectedTotalFee, actualTotalFee, 0.001)
    }
}
