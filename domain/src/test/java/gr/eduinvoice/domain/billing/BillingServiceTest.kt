package gr.eduinvoice.domain.billing

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class BillingServiceTest {

    private val billingService = BillingService()

    @Test
    fun `calculateTotal with single lesson returns correct total`() {
        val lessons = listOf(
            Fixtures.sampleDomainLesson(fee = 50.0)
        )
        val result = billingService.calculateTotal(lessons)
        assertEquals(50.0, result, 0.001)
    }

    @Test
    fun `calculateTotal with multiple lessons returns correct sum`() {
        val lessons = listOf(
            Fixtures.sampleDomainLesson(fee = 50.0),
            Fixtures.sampleDomainLesson(fee = 75.0),
            Fixtures.sampleDomainLesson(fee = 25.0)
        )
        val result = billingService.calculateTotal(lessons)
        assertEquals(150.0, result, 0.001)
    }

    @Test
    fun `calculateTotal with empty list returns zero`() {
        val lessons = emptyList<DomainLesson>()
        val result = billingService.calculateTotal(lessons)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `calculateTotal with zero fee lessons returns zero`() {
        val lessons = listOf(
            Fixtures.sampleDomainLesson(fee = 0.0),
            Fixtures.sampleDomainLesson(fee = 0.0)
        )
        val result = billingService.calculateTotal(lessons)
        assertEquals(0.0, result, 0.001)
    }
}
