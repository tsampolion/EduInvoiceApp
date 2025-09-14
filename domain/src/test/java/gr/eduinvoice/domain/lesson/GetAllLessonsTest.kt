package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.billing.Fixtures
import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAllLessonsTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var getAllLessons: GetAllLessons

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        getAllLessons = GetAllLessons(mockRepository)
    }

    @Test
    fun `should return flow of lessons successfully`() = runTest {
        // Given
        val userId = 456L
        val lessons = listOf(Fixtures.sampleDomainLesson())
        coEvery { mockRepository.getAllLessons(userId) } returns flowOf(lessons)

        // When
        val result = getAllLessons(userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(1, result.size)
    }

    @Test
    fun `should return flow of lessons with default userId`() = runTest {
        // Given
        val lessons = listOf(Fixtures.sampleDomainLesson())
        coEvery { mockRepository.getAllLessons(0) } returns flowOf(lessons)

        // When
        val result = getAllLessons().first()

        // Then
        assertEquals(lessons, result)
        assertEquals(1, result.size)
    }

    @Test
    fun `should return empty flow when no lessons`() = runTest {
        // Given
        val userId = 789L
        coEvery { mockRepository.getAllLessons(userId) } returns flowOf(emptyList())

        // When
        val result = getAllLessons(userId).first()

        // Then
        assertTrue(result.isEmpty())
        assertEquals(0, result.size)
    }

    @Test
    fun `should handle multiple lessons in flow`() = runTest {
        // Given
        val userId = 101112L
        val lessons = listOf(
            Fixtures.sampleDomainLesson(id = 1),
            Fixtures.sampleDomainLesson(id = 2)
        )
        coEvery { mockRepository.getAllLessons(userId) } returns flowOf(lessons)

        // When
        val result = getAllLessons(userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(2, result.size)
    }

    @Test
    fun `should work with different userIds`() = runTest {
        // Given
        val userId1 = 131415L
        val userId2 = 161718L
        val lessons1 = listOf(Fixtures.sampleDomainLesson(id = 1))
        val lessons2 = listOf(Fixtures.sampleDomainLesson(id = 2))
        coEvery { mockRepository.getAllLessons(userId1) } returns flowOf(lessons1)
        coEvery { mockRepository.getAllLessons(userId2) } returns flowOf(lessons2)

        // When
        val result1 = getAllLessons(userId1).first()
        val result2 = getAllLessons(userId2).first()

        // Then
        assertEquals(lessons1, result1)
        assertEquals(lessons2, result2)
    }

    @Test
    fun `should handle lessons with various properties`() = runTest {
        // Given
        val userId = 192021L
        val lessons = listOf(
            Fixtures.sampleDomainLesson(isInvoiced = true),
            Fixtures.sampleDomainLesson(isPaid = true),
            Fixtures.sampleDomainLesson(durationMinutes = 0),
            Fixtures.sampleDomainLesson(defaultRate = 0.0)
        )
        coEvery { mockRepository.getAllLessons(userId) } returns flowOf(lessons)

        // When
        val result = getAllLessons(userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(4, result.size)
    }

    @Test
    fun `should handle invoiced and not invoiced lessons`() = runTest {
        // Given
        val userId = 222324L
        val lessons = listOf(
            Fixtures.sampleDomainLesson(isInvoiced = true),
            Fixtures.sampleDomainLesson(isInvoiced = false)
        )
        coEvery { mockRepository.getAllLessons(userId) } returns flowOf(lessons)

        // When
        val result = getAllLessons(userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(2, result.size)
    }

    @Test
    fun `should handle paid and not paid lessons`() = runTest {
        // Given
        val userId = 252627L
        val lessons = listOf(
            Fixtures.sampleDomainLesson(isPaid = true),
            Fixtures.sampleDomainLesson(isPaid = false)
        )
        coEvery { mockRepository.getAllLessons(userId) } returns flowOf(lessons)

        // When
        val result = getAllLessons(userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(2, result.size)
        assertTrue(result[0].isPaid)
        assertFalse(result[1].isPaid)
    }

    @Test
    fun `should handle mixed invoiced and paid statuses`() = runTest {
        // Given
        val userId = 282930L
        val lessons = listOf(
            Fixtures.sampleDomainLesson(isInvoiced = true, isPaid = true),
            Fixtures.sampleDomainLesson(isInvoiced = true, isPaid = false),
            Fixtures.sampleDomainLesson(isInvoiced = false, isPaid = true),
            Fixtures.sampleDomainLesson(isInvoiced = false, isPaid = false)
        )
        coEvery { mockRepository.getAllLessons(userId) } returns flowOf(lessons)

        // When
        val result = getAllLessons(userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(4, result.size)

        assertTrue(result[0].isInvoiced && result[0].isPaid)
        assertTrue(result[1].isInvoiced && !result[1].isPaid)
        assertFalse(result[2].isInvoiced && result[2].isPaid) // Note: A lesson can't be paid if not invoiced in real logic, but testing the data flow
        assertFalse(result[3].isInvoiced && !result[3].isPaid)
    }
}
