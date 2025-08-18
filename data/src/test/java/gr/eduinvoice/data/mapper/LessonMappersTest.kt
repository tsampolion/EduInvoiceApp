package gr.eduinvoice.data.mapper

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import gr.eduinvoice.data.mapper.Fixtures.sampleDataLesson
import gr.eduinvoice.data.mapper.Fixtures.sampleDomainLessonForData
import gr.eduinvoice.data.mapper.toDomain
import gr.eduinvoice.data.mapper.toData
import gr.eduinvoice.domain.model.DomainLesson

class LessonMappersTest {

    @Test
    fun `data to domain to data roundtrip should preserve data`() {
        val data = sampleDataLesson()
        val domain = data.toDomain()
        val back = domain.toData()
        assertEquals(data, back)
    }

    @Test
    fun `domain to data to domain roundtrip should preserve data`() {
        val domain = sampleDomainLessonForData()
        val data = domain.toData()
        val back = data.toDomain()
        // defaultRate does not exist in data model, so it is dropped during mapping
        val expected = domain.copy(defaultRate = null)
        assertEquals(expected, back)
    }

    @Test
    fun `should handle null groupId correctly`() {
        val data = sampleDataLesson(groupId = null)
        val domain = data.toDomain()

        assertNull(domain.groupId)
        assertEquals(data.groupId, domain.groupId)
    }

    @Test
    fun `should handle non-null groupId correctly`() {
        val groupId = 123L
        val data = sampleDataLesson(groupId = groupId)
        val domain = data.toDomain()

        assertEquals(groupId, domain.groupId)
        assertNotNull(domain.groupId)
    }

    @Test
    fun `should preserve lesson duration`() {
        val duration = 90
        val data = sampleDataLesson(durationMinutes = duration)
        val domain = data.toDomain()

        assertEquals(duration, domain.durationMinutes)
    }

    @Test
    fun `should preserve lesson date`() {
        val date = "2024-01-15"
        val data = sampleDataLesson(date = date)
        val domain = data.toDomain()

        assertEquals(date, domain.date)
    }

    @Test
    fun `should preserve lesson start time`() {
        val startTime = "14:30"
        val data = sampleDataLesson(startTime = startTime)
        val domain = data.toDomain()

        assertEquals(startTime, domain.startTime)
    }

    @Test
    fun `should preserve lesson notes`() {
        val notes = "Test lesson notes"
        val data = sampleDataLesson(notes = notes)
        val domain = data.toDomain()

        assertEquals(notes, domain.notes)
    }

    @Test
    fun `should preserve lesson invoiced status`() {
        val isInvoiced = true
        val data = sampleDataLesson(isInvoiced = isInvoiced)
        val domain = data.toDomain()

        assertEquals(isInvoiced, domain.isInvoiced)
    }

    @Test
    fun `should preserve lesson ID`() {
        val id = 456L
        val data = sampleDataLesson(id = id)
        val domain = data.toDomain()

        assertEquals(id, domain.id)
    }

    @Test
    fun `should preserve student ID`() {
        val studentId = 789L
        val data = sampleDataLesson(studentId = studentId)
        val domain = data.toDomain()

        assertEquals(studentId, domain.studentId)
    }

    @Test
    fun `should handle zero duration`() {
        val data = sampleDataLesson(durationMinutes = 0)
        val domain = data.toDomain()

        assertEquals(0, domain.durationMinutes)
    }

    @Test
    fun `should handle large duration`() {
        val duration = 480 // 8 hours
        val data = sampleDataLesson(durationMinutes = duration)
        val domain = data.toDomain()

        assertEquals(duration, domain.durationMinutes)
    }

    @Test
    fun `should handle empty notes`() {
        val data = sampleDataLesson(notes = "")
        val domain = data.toDomain()

        assertEquals("", domain.notes)
    }

    @Test
    fun `should handle null notes`() {
        val data = sampleDataLesson(notes = null)
        val domain = data.toDomain()

        assertNull(domain.notes)
    }

    @Test
    fun `should handle long notes`() {
        val longNotes = "This is a very long note that contains a lot of text. " +
                       "It should be properly preserved during the mapping process " +
                       "regardless of its length or content."
        val data = sampleDataLesson(notes = longNotes)
        val domain = data.toDomain()

        assertEquals(longNotes, domain.notes)
    }

    @Test
    fun `should preserve all lesson properties during roundtrip`() {
        val originalData = sampleDataLesson(
            id = 111L,
            studentId = 222L,
            groupId = 333L,
            durationMinutes = 75,
            date = "2024-02-20",
            startTime = "16:45",
            notes = "Comprehensive test lesson",
            isInvoiced = false
        )

        val domain = originalData.toDomain()
        val roundtripData = domain.toData()

        // Verify all properties are preserved
        assertEquals(originalData.id, roundtripData.id)
        assertEquals(originalData.studentId, roundtripData.studentId)
        assertEquals(originalData.groupId, roundtripData.groupId)
        assertEquals(originalData.durationMinutes, roundtripData.durationMinutes)
        assertEquals(originalData.date, roundtripData.date)
        assertEquals(originalData.startTime, roundtripData.startTime)
        assertEquals(originalData.notes, roundtripData.notes)
        assertEquals(originalData.isInvoiced, roundtripData.isInvoiced)
    }
}
