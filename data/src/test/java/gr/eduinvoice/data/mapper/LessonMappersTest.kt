package gr.eduinvoice.data.mapper

import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.domain.model.DomainLesson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Test

class LessonMappersTest {

    @Ignore("Implement sampleDataLesson/sampleDomainLessonForData in Fixtures.kt")
    @Test fun `data to domain to data roundtrip`() {
        val data: Lesson = sampleDataLesson(
            id = 11L, studentId = 5L, durationMinutes = 90, defaultRate = 18.0
        )
        val roundTrip = data.toDomain().toData()
        assertNotNull(roundTrip)
        assertEquals(11L, roundTrip.id)
    }

    @Ignore("Implement sampleDataLesson/sampleDomainLessonForData in Fixtures.kt")
    @Test fun `domain to data to domain roundtrip`() {
        val domain: DomainLesson = sampleDomainLessonForData(
            id = 15L, studentId = 6L, durationMinutes = 60, defaultRate = null
        )
        val roundTrip = domain.toData().toDomain()
        assertEquals(15L, roundTrip.id)
    }
}
