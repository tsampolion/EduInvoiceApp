package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.billing.Fixtures
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetLessonsByStudentIdTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var getLessonsByStudentId: GetLessonsByStudentId

    @Before
    fun setup() {
        mockRepository = mockk()
        getLessonsByStudentId = GetLessonsByStudentId(mockRepository)
    }

    @Test
    fun `should get lessons by student ID successfully`() = runTest {
        // Given
        val studentId = 123L
        val userId = 456L
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId),
            Fixtures.sampleDomainLesson(studentId = studentId)
        )
        
        every { mockRepository.getStudentLessons(studentId, userId) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId, userId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons[0])
    }

    @Test
    fun `should get lessons by student ID with default userId when not specified`() = runTest {
        // Given
        val studentId = 789L
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId)
        )
        
        every { mockRepository.getStudentLessons(studentId, 0) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons[0])
    }

    @Test
    fun `should return empty list when student has no lessons`() = runTest {
        // Given
        val studentId = 999L
        val userId = 101L
        val expectedLessons = emptyList<DomainLesson>()
        
        every { mockRepository.getStudentLessons(studentId, userId) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId, userId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertTrue(collectedLessons[0].isEmpty())
    }

    @Test
    fun `should handle zero student ID`() = runTest {
        // Given
        val studentId = 0L
        val userId = 202L
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId)
        )
        
        every { mockRepository.getStudentLessons(studentId, userId) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId, userId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons[0])
    }

    @Test
    fun `should handle negative student ID`() = runTest {
        // Given
        val studentId = -1L
        val userId = 303L
        val expectedLessons = emptyList<DomainLesson>()
        
        every { mockRepository.getStudentLessons(studentId, userId) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId, userId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertTrue(collectedLessons[0].isEmpty())
    }

    @Test
    fun `should handle large student ID`() = runTest {
        // Given
        val studentId = Long.MAX_VALUE
        val userId = 404L
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId)
        )
        
        every { mockRepository.getStudentLessons(studentId, userId) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId, userId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons[0])
    }

    @Test
    fun `should handle zero userId`() = runTest {
        // Given
        val studentId = 505L
        val userId = 0L
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId)
        )
        
        every { mockRepository.getStudentLessons(studentId, userId) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId, userId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons[0])
    }

    @Test
    fun `should handle negative userId`() = runTest {
        // Given
        val studentId = 606L
        val userId = -1L
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId)
        )
        
        every { mockRepository.getStudentLessons(studentId, userId) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId, userId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons[0])
    }

    @Test
    fun `should handle large userId`() = runTest {
        // Given
        val studentId = 707L
        val userId = Long.MAX_VALUE
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId)
        )
        
        every { mockRepository.getStudentLessons(studentId, userId) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId, userId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons[0])
    }

    @Test
    fun `should handle edge case with minimum values`() = runTest {
        // Given
        val studentId = Long.MIN_VALUE
        val userId = Long.MIN_VALUE
        val expectedLessons = emptyList<DomainLesson>()
        
        every { mockRepository.getStudentLessons(studentId, userId) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId, userId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertTrue(collectedLessons[0].isEmpty())
    }

    @Test
    fun `should handle edge case with maximum values`() = runTest {
        // Given
        val studentId = Long.MAX_VALUE
        val userId = Long.MAX_VALUE
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId)
        )
        
        every { mockRepository.getStudentLessons(studentId, userId) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId, userId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons[0])
    }

    @Test
    fun `should handle multiple calls with different student IDs`() = runTest {
        // Given
        val studentId1 = 111L
        val studentId2 = 222L
        val userId = 333L
        val expectedLessons1 = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId1)
        )
        val expectedLessons2 = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId2),
            Fixtures.sampleDomainLesson(studentId = studentId2)
        )
        
        every { mockRepository.getStudentLessons(studentId1, userId) } returns flowOf(expectedLessons1)
        every { mockRepository.getStudentLessons(studentId2, userId) } returns flowOf(expectedLessons2)

        // When
        val result1 = getLessonsByStudentId(studentId1, userId)
        val result2 = getLessonsByStudentId(studentId2, userId)

        // Then
        val collectedLessons1 = mutableListOf<List<DomainLesson>>()
        result1.collect { collectedLessons1.add(it) }
        val collectedLessons2 = mutableListOf<List<DomainLesson>>()
        result2.collect { collectedLessons2.add(it) }
        
        assertEquals(expectedLessons1, collectedLessons1[0])
        assertEquals(expectedLessons2, collectedLessons2[0])
    }

    @Test
    fun `should handle same student ID with different user IDs`() = runTest {
        // Given
        val studentId = 444L
        val userId1 = 555L
        val userId2 = 666L
        val expectedLessons1 = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId)
        )
        val expectedLessons2 = listOf(
            Fixtures.sampleDomainLesson(studentId = studentId),
            Fixtures.sampleDomainLesson(studentId = studentId)
        )
        
        every { mockRepository.getStudentLessons(studentId, userId1) } returns flowOf(expectedLessons1)
        every { mockRepository.getStudentLessons(studentId, userId2) } returns flowOf(expectedLessons2)

        // When
        val result1 = getLessonsByStudentId(studentId, userId1)
        val result2 = getLessonsByStudentId(studentId, userId2)

        // Then
        val collectedLessons1 = mutableListOf<List<DomainLesson>>()
        result1.collect { collectedLessons1.add(it) }
        val collectedLessons2 = mutableListOf<List<DomainLesson>>()
        result2.collect { collectedLessons2.add(it) }
        
        assertEquals(expectedLessons1, collectedLessons1[0])
        assertEquals(expectedLessons2, collectedLessons2[0])
    }

    @Test
    fun `should handle lessons with different properties for same student`() = runTest {
        // Given
        val studentId = 777L
        val userId = 888L
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(
                id = 111L,
                studentId = studentId,
                durationMinutes = 30,
                date = "2024-01-15",
                startTime = "09:00",
                notes = "Morning lesson",
                isInvoiced = false
            ),
            Fixtures.sampleDomainLesson(
                id = 222L,
                studentId = studentId,
                durationMinutes = 60,
                date = "2024-01-16",
                startTime = "14:00",
                notes = "Afternoon lesson",
                isInvoiced = true
            )
        )
        
        every { mockRepository.getStudentLessons(studentId, userId) } returns flowOf(expectedLessons)

        // When
        val result = getLessonsByStudentId(studentId, userId)

        // Then
        val collectedLessons = mutableListOf<List<DomainLesson>>()
        result.collect { collectedLessons.add(it) }
        assertEquals(1, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons[0])
        assertEquals(2, collectedLessons[0].size)
        assertEquals(30, collectedLessons[0][0].durationMinutes)
        assertEquals(60, collectedLessons[0][1].durationMinutes)
    }
}