package gr.eduinvoice.data.mapper

import org.junit.Test
import org.junit.Assert.assertEquals
import gr.eduinvoice.data.mapper.Fixtures.sampleDataUser
import gr.eduinvoice.data.mapper.Fixtures.sampleDomainUserForData
import gr.eduinvoice.data.mapper.toDomain
import gr.eduinvoice.data.mapper.toData
import gr.eduinvoice.domain.model.DomainUser

class UserMappersTest {

    @Test
    fun `data to domain to data roundtrip should preserve data`() {
        val data = sampleDataUser()
        val domain = data.toDomain()
        val back = domain.toData()
        assertEquals(data, back)
    }

    @Test
    fun `domain to data to domain roundtrip should preserve data`() {
        val domain = sampleDomainUserForData()
        val data = domain.toData()
        val back = data.toDomain()
        assertEquals(domain, back)
    }
}
