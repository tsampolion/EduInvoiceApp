package gr.eduinvoice.domain.billing

import org.junit.Test
import org.junit.Assert.assertEquals
import gr.eduinvoice.domain.utils.DomainFeeCalculator
import gr.eduinvoice.domain.model.DomainRateTypes

class BillingServiceTest {

    @Test
    fun `zero minutes should return zero cost`() {
        val student = Fixtures.sampleDomainStudent(rate = 40.0, rateType = DomainRateTypes.HOURLY)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 0, defaultRate = 25.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        assertEquals(0.0, fee, 0.001)
    }

    @Test
    fun `should prefer student rate over lesson rate`() {
        // Given hourly rate type, the student's hourly rate is used regardless of lesson defaultRate
        val student = Fixtures.sampleDomainStudent(rate = 40.0, rateType = DomainRateTypes.HOURLY)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 60, defaultRate = 25.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        assertEquals(40.0, fee, 0.001)
    }

    @Test
    fun `per-lesson rate uses student's per-lesson value`() {
        val student = Fixtures.sampleDomainStudent(rate = 35.0, rateType = DomainRateTypes.PER_LESSON)
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 120, defaultRate = 50.0)

        val fee = DomainFeeCalculator.calculateFee(lesson, student)

        // For per-lesson type, fee equals the student's per-lesson rate
        assertEquals(35.0, fee, 0.001)
    }
}
