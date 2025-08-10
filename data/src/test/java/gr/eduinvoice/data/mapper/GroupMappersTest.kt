package gr.eduinvoice.data.mapper

import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.domain.model.DomainStudentGroup
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

class GroupMappersTest {

    @Ignore("Implement sampleDataGroup/sampleDomainGroupForData in Fixtures.kt")
    @Test fun `data to domain to data roundtrip`() {
        val data: StudentGroup = sampleDataGroup(id = 3L, name = "B2")
        val roundTrip = data.toDomain().toData()
        assertEquals(3L, roundTrip.id)
    }

    @Ignore("Implement sampleDataGroup/sampleDomainGroupForData in Fixtures.kt")
    @Test fun `domain to data to domain roundtrip`() {
        val domain: DomainStudentGroup = sampleDomainGroupForData(id = 4L, name = "C1")
        val roundTrip = domain.toData().toDomain()
        assertEquals(4L, roundTrip.id)
    }
}
