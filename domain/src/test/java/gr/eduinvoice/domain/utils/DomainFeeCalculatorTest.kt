package gr.eduinvoice.domain.utils

import gr.eduinvoice.domain.billing.Fixtures
import gr.eduinvoice.domain.model.DomainRateTypes
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals

class DomainFeeCalculatorTest {

    @Test
    fun `zero minutes should return zero cost for hourly rate`() {
        val student = Fixtures.sampleDomainStudent(rate = 40.0, rateType = DomainRateTypes.HOURLY)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 0, defaultRate = 25.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        assertEquals(0.0, fee, 0.001)
    }

    @Test
    fun `zero minutes should return full cost for per lesson rate`() {
        val student = Fixtures.sampleDomainStudent(rate = 35.0, rateType = DomainRateTypes.PER_LESSON)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 0, defaultRate = 25.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        assertEquals(35.0, fee, 0.001)
    }

    @Test
    fun `should prefer student rate over lesson rate for hourly`() {
        val student = Fixtures.sampleDomainStudent(rate = 40.0, rateType = DomainRateTypes.HOURLY)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 60, defaultRate = 25.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        assertEquals(40.0, fee, 0.001)
        assertNotEquals(25.0, fee, 0.001)
    }

    @Test
    fun `per lesson rate uses student's per lesson value regardless of duration`() {
        val student = Fixtures.sampleDomainStudent(rate = 35.0, rateType = DomainRateTypes.PER_LESSON)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 120, defaultRate = 50.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        assertEquals(35.0, fee, 0.001)
    }

    @Test
    fun `per lesson rate uses student's per lesson value for short duration`() {
        val student = Fixtures.sampleDomainStudent(rate = 35.0, rateType = DomainRateTypes.PER_LESSON)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 30, defaultRate = 50.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        assertEquals(35.0, fee, 0.001)
    }

    @Test
    fun `hourly rate calculation for 30 minutes`() {
        val student = Fixtures.sampleDomainStudent(rate = 40.0, rateType = DomainRateTypes.HOURLY)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 30, defaultRate = 25.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        assertEquals(20.0, fee, 0.001) // 30/60 * 40 = 0.5 * 40 = 20
    }

    @Test
    fun `hourly rate calculation for 90 minutes`() {
        val student = Fixtures.sampleDomainStudent(rate = 40.0, rateType = DomainRateTypes.HOURLY)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 90, defaultRate = 25.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        assertEquals(60.0, fee, 0.001) // 90/60 * 40 = 1.5 * 40 = 60
    }

    @Test
    fun `hourly rate calculation for 120 minutes`() {
        val student = Fixtures.sampleDomainStudent(rate = 40.0, rateType = DomainRateTypes.HOURLY)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 120, defaultRate = 25.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        assertEquals(80.0, fee, 0.001) // 120/60 * 40 = 2.0 * 40 = 80
    }

    @Test
    fun `hourly rate calculation for 45 minutes`() {
        val student = Fixtures.sampleDomainStudent(rate = 50.0, rateType = DomainRateTypes.HOURLY)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 45, defaultRate = 25.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        assertEquals(37.5, fee, 0.001) // 45/60 * 50 = 0.75 * 50 = 37.5
    }

    @Test
    fun `per lesson rate ignores duration completely`() {
        val student = Fixtures.sampleDomainStudent(rate = 35.0, rateType = DomainRateTypes.PER_LESSON)
        
        val shortLesson = Fixtures.sampleDomainLesson(durationMinutes = 15, defaultRate = 25.0)
        val longLesson = Fixtures.sampleDomainLesson(durationMinutes = 180, defaultRate = 25.0)

        val shortFee = DomainFeeCalculator.calculateFee(shortLesson, student)
        val longFee = DomainFeeCalculator.calculateFee(longLesson, student)

        assertEquals(35.0, shortFee, 0.001)
        assertEquals(35.0, longFee, 0.001)
        assertEquals(shortFee, longFee, 0.001)
    }

    @Test
    fun `hourly rate scales linearly with duration`() {
        val student = Fixtures.sampleDomainStudent(rate = 60.0, rateType = DomainRateTypes.HOURLY)
        
        val lesson30 = Fixtures.sampleDomainLesson(durationMinutes = 30, defaultRate = 25.0)
        val lesson60 = Fixtures.sampleDomainLesson(durationMinutes = 60, defaultRate = 25.0)
        val lesson120 = Fixtures.sampleDomainLesson(durationMinutes = 120, defaultRate = 25.0)

        val fee30 = DomainFeeCalculator.calculateFee(lesson30, student)
        val fee60 = DomainFeeCalculator.calculateFee(lesson60, student)
        val fee120 = DomainFeeCalculator.calculateFee(lesson120, student)

        assertEquals(30.0, fee30, 0.001) // 30/60 * 60 = 0.5 * 60 = 30
        assertEquals(60.0, fee60, 0.001) // 60/60 * 60 = 1.0 * 60 = 60
        assertEquals(120.0, fee120, 0.001) // 120/60 * 60 = 2.0 * 60 = 120
        
        // Verify linear relationship
        assertEquals(fee60, fee30 * 2, 0.001)
        assertEquals(fee120, fee60 * 2, 0.001)
        assertEquals(fee120, fee30 * 4, 0.001)
    }
}