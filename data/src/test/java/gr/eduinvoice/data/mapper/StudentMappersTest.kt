package gr.eduinvoice.data.mapper

import gr.eduinvoice.data.model.Student
import gr.eduinvoice.domain.model.DomainStudent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Test

class StudentMappersTest {

    @Ignore("Implement sampleDataStudent/sampleDomainStudentForData in Fixtures.kt")
    @Test fun `data to domain to data roundtrip`() {
        val data: Student = sampleDataStudent(
            id = 42L, name = "Alex", groupId = 3L, hourlyRate = 20.0, isArchived = false
        )
        val roundTrip = data.toDomain().toData()
        assertNotNull(roundTrip)
        assertEquals(42L, roundTrip.id)
    }

    @Ignore("Implement sampleDataStudent/sampleDomainStudentForData in Fixtures.kt")
    @Test fun `domain to data to domain roundtrip`() {
        val domain: DomainStudent = sampleDomainStudentForData(
            id = 7L, name = "Eleni", groupId = 2L, hourlyRate = null, isArchived = true
        )
        val roundTrip = domain.toData().toDomain()
        assertEquals(7L, roundTrip.id)
    }
}
