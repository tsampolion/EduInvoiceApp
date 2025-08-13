package gr.eduinvoice.data.mapper

import org.junit.Test
import org.junit.Assert.assertEquals
import gr.eduinvoice.data.mapper.Fixtures.sampleDataStudent
import gr.eduinvoice.data.mapper.Fixtures.sampleDomainStudentForData
import gr.eduinvoice.data.mapper.toDomain
import gr.eduinvoice.data.mapper.toData
import gr.eduinvoice.domain.model.DomainStudent

class StudentMappersTest {

    @Test
    fun `data to domain to data roundtrip should preserve data`() {
        val data = sampleDataStudent()
        val domain = data.toDomain()
        val back = domain.toData()
        assertEquals(data, back)
    }

    @Test
    fun `domain to data to domain roundtrip should preserve data`() {
        val domain = sampleDomainStudentForData()
        val data = domain.toData()
        val back = data.toDomain()
        // hourlyRate does not exist in data model, so it is dropped during mapping
        val expected = domain.copy(hourlyRate = null)
        assertEquals(expected, back)
    }
}
