package gr.eduinvoice.data.mapper

import org.junit.Test
import org.junit.Assert.assertEquals
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
}
