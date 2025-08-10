package gr.eduinvoice.domain.billing

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

class BillingServiceTest {

    @Ignore("Fill sampleDomainStudent/sampleDomainLesson in Fixtures.kt then remove @Ignore")
    @Test fun `0 minutes yields 0`() {
        val student: DomainStudent = sampleDomainStudent(hourlyRate = 20.0)
        val lesson: DomainLesson = sampleDomainLesson(durationMinutes = 0, defaultRate = 15.0)
        assertEquals(0.0, BillingService.fee(lesson, student), 0.001)
    }

    @Ignore("Fill sampleDomainStudent/sampleDomainLesson in Fixtures.kt then remove @Ignore")
    @Test fun `uses student hourlyRate when present`() {
        val student = sampleDomainStudent(hourlyRate = 24.0)
        val lesson = sampleDomainLesson(durationMinutes = 90, defaultRate = 15.0)
        assertEquals(36.0, BillingService.fee(lesson, student), 0.001)
    }

    @Ignore("Fill sampleDomainStudent/sampleDomainLesson in Fixtures.kt then remove @Ignore")
    @Test fun `falls back to lesson defaultRate when student rate is null`() {
        val student = sampleDomainStudent(hourlyRate = null)
        val lesson = sampleDomainLesson(durationMinutes = 60, defaultRate = 18.0)
        assertEquals(18.0, BillingService.fee(lesson, student), 0.001)
    }
}
