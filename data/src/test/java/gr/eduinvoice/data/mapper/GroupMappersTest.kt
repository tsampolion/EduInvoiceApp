package gr.eduinvoice.data.mapper

import org.junit.Test
import org.junit.Assert.assertEquals
import gr.eduinvoice.data.mapper.Fixtures.sampleDataGroup
import gr.eduinvoice.data.mapper.Fixtures.sampleDomainGroupForData
import gr.eduinvoice.data.mapper.toDomain
import gr.eduinvoice.data.mapper.toData
import gr.eduinvoice.domain.model.DomainStudentGroup

class GroupMappersTest {

    @Test
    fun `data to domain to data roundtrip should preserve data`() {
        val data = sampleDataGroup()
        val domain = data.toDomain()
        val back = domain.toData()
        assertEquals(data, back)
    }

    @Test
    fun `domain to data to domain roundtrip should preserve data`() {
        val domain = sampleDomainGroupForData()
        val data = domain.toData()
        val back = data.toDomain()
        assertEquals(domain, back)
    }
}
