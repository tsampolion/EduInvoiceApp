package gr.eduinvoice.data.mapper

import gr.eduinvoice.data.model.User
import gr.eduinvoice.domain.model.DomainUser
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

class UserMappersTest {

    @Ignore("Implement sampleDataUser/sampleDomainUserForData in Fixtures.kt")
    @Test fun `data to domain to data roundtrip`() {
        val data: User = sampleDataUser(id = 99L, name = "Owner")
        val roundTrip = data.toDomain().toData()
        assertEquals(99L, roundTrip.id)
    }

    @Ignore("Implement sampleDataUser/sampleDomainUserForData in Fixtures.kt")
    @Test fun `domain to data to domain roundtrip`() {
        val domain: DomainUser = sampleDomainUserForData(id = 100L, name = "Owner")
        val roundTrip = domain.toData().toDomain()
        assertEquals(100L, roundTrip.id)
    }
}
