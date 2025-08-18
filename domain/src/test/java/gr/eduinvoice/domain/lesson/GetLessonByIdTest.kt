package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.billing.Fixtures
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetLessonByIdTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var getLessonById: GetLessonById

    @Before
    fun setup() {
        mockRepository = mockk()
        getLessonById = GetLessonById(mockRepository)
    }

    @Test
    fun `should get lesson by ID successfully`() = runTest {
        // Given
        val lessonId = 123L
        val userId = 456L
        val expectedLesson = Fixtures.sampleDomainLesson(id = lessonId)

        every { mockRepository.getLessonById(lessonId, userId) } returns flowOf(expectedLesson)

        // When
        val result = getLessonById(lessonId, userId)

        // Then
        val collectedLesson = mutableListOf<DomainLesson?>()
        result.collect { collectedLesson.add(it) }
        assertEquals(1, collectedLesson.size)
        assertEquals(expectedLesson, collectedLesson[0])
    }

    @Test
    fun `should get lesson by ID with default userId when not specified`() = runTest {
        // Given
        val lessonId = 789L
        val expectedLesson = Fixtures.sampleDomainLesson(id = lessonId)

        every { mockRepository.getLessonById(lessonId, 0) } returns flowOf(expectedLesson)

        // When
        val result = getLessonById(lessonId)

        // Then
        val collectedLesson = mutableListOf<DomainLesson?>()
        result.collect { collectedLesson.add(it) }
        assertEquals(1, collectedLesson.size)
        assertEquals(expectedLesson, collectedLesson[0])
    }

    @Test
    fun `should return null when lesson not found`() = runTest {
        // Given
        val lessonId = 999L
        val userId = 101L

        every { mockRepository.getLessonById(lessonId, userId) } returns flowOf(null)

        // When
        val result = getLessonById(lessonId, userId)

        // Then
        val collectedLesson = mutableListOf<DomainLesson?>()
        result.collect { collectedLesson.add(it) }
        assertEquals(1, collectedLesson.size)
        assertNull(collectedLesson[0])
    }

    @Test
    fun `should handle zero lesson ID`() = runTest {
        // Given
        val lessonId = 0L
        val userId = 202L
        val expectedLesson = Fixtures.sampleDomainLesson(id = lessonId)

        every { mockRepository.getLessonById(lessonId, userId) } returns flowOf(expectedLesson)

        // When
        val result = getLessonById(lessonId, userId)

        // Then
        val collectedLesson = mutableListOf<DomainLesson?>()
        result.collect { collectedLesson.add(it) }
        assertEquals(1, collectedLesson.size)
        assertEquals(expectedLesson, collectedLesson[0])
    }

    @Test
    fun `should handle negative lesson ID`() = runTest {
        // Given
        val lessonId = -1L
        val userId = 303L

        every { mockRepository.getLessonById(lessonId, userId) } returns flowOf(null)

        // When
        val result = getLessonById(lessonId, userId)

        // Then
        val collectedLesson = mutableListOf<DomainLesson?>()
        result.collect { collectedLesson.add(it) }
        assertEquals(1, collectedLesson.size)
        assertNull(collectedLesson[0])
    }

    @Test
    fun `should handle large lesson ID`() = runTest {
        // Given
        val lessonId = Long.MAX_VALUE
        val userId = 404L
        val expectedLesson = Fixtures.sampleDomainLesson(id = lessonId)

        every { mockRepository.getLessonById(lessonId, userId) } returns flowOf(expectedLesson)

        // When
        val result = getLessonById(lessonId, userId)

        // Then
        val collectedLesson = mutableListOf<DomainLesson?>()
        result.collect { collectedLesson.add(it) }
        assertEquals(1, collectedLesson.size)
        assertEquals(expectedLesson, collectedLesson[0])
    }

    @Test
    fun `should handle zero userId`() = runTest {
        // Given
        val lessonId = 505L
        val userId = 0L
        val expectedLesson = Fixtures.sampleDomainLesson(id = lessonId)

        every { mockRepository.getLessonById(lessonId, userId) } returns flowOf(expectedLesson)

        // When
        val result = getLessonById(lessonId, userId)

        // Then
        val collectedLesson = mutableListOf<DomainLesson?>()
        result.collect { collectedLesson.add(it) }
        assertEquals(1, collectedLesson.size)
        assertEquals(expectedLesson, collectedLesson[0])
    }

    @Test
    fun `should handle negative userId`() = runTest {
        // Given
        val lessonId = 606L
        val userId = -1L
        val expectedLesson = Fixtures.sampleDomainLesson(id = lessonId)

        every { mockRepository.getLessonById(lessonId, userId) } returns flowOf(expectedLesson)

        // When
        val result = getLessonById(lessonId, userId)

        // Then
        val collectedLesson = mutableListOf<DomainLesson?>()
        result.collect { collectedLesson.add(it) }
        assertEquals(1, collectedLesson.size)
        assertEquals(expectedLesson, collectedLesson[0])
    }

    @Test
    fun `should handle large userId`() = runTest {
        // Given
        val lessonId = 707L
        val userId = Long.MAX_VALUE
        val expectedLesson = Fixtures.sampleDomainLesson(id = lessonId)

        every { mockRepository.getLessonById(lessonId, userId) } returns flowOf(expectedLesson)

        // When
        val result = getLessonById(lessonId, userId)

        // Then
        val collectedLesson = mutableListOf<DomainLesson?>()
        result.collect { collectedLesson.add(it) }
        assertEquals(1, collectedLesson.size)
        assertEquals(expectedLesson, collectedLesson[0])
    }

    @Test
    fun `should handle edge case with minimum values`() = runTest {
        // Given
        val lessonId = Long.MIN_VALUE
        val userId = Long.MIN_VALUE

        every { mockRepository.getLessonById(lessonId, userId) } returns flowOf(null)

        // When
        val result = getLessonById(lessonId, userId)

        // Then
        val collectedLesson = mutableListOf<DomainLesson?>()
        result.collect { collectedLesson.add(it) }
        assertEquals(1, collectedLesson.size)
        assertNull(collectedLesson[0])
    }

    @Test
    fun `should handle edge case with maximum values`() = runTest {
        // Given
        val lessonId = Long.MAX_VALUE
        val userId = Long.MAX_VALUE
        val expectedLesson = Fixtures.sampleDomainLesson(id = lessonId)

        every { mockRepository.getLessonById(lessonId, userId) } returns flowOf(expectedLesson)

        // When
        val result = getLessonById(lessonId, userId)

        // Then
        val collectedLesson = mutableListOf<DomainLesson?>()
        result.collect { collectedLesson.add(it) }
        assertEquals(1, collectedLesson.size)
        assertEquals(expectedLesson, collectedLesson[0])
    }

    @Test
    fun `should handle multiple calls with different results`() = runTest {
        // Given
        val lessonId1 = 111L
        val lessonId2 = 222L
        val userId = 333L
        val expectedLesson1 = Fixtures.sampleDomainLesson(id = lessonId1)
        val expectedLesson2 = Fixtures.sampleDomainLesson(id = lessonId2)

        every { mockRepository.getLessonById(lessonId1, userId) } returns flowOf(expectedLesson1)
        every { mockRepository.getLessonById(lessonId2, userId) } returns flowOf(expectedLesson2)

        // When
        val result1 = getLessonById(lessonId1, userId)
        val result2 = getLessonById(lessonId2, userId)

        // Then
        val collectedLesson1 = mutableListOf<DomainLesson?>()
        result1.collect { collectedLesson1.add(it) }
        val collectedLesson2 = mutableListOf<DomainLesson?>()
        result2.collect { collectedLesson2.add(it) }

        assertEquals(expectedLesson1, collectedLesson1[0])
        assertEquals(expectedLesson2, collectedLesson2[0])
    }

    @Test
    fun `should handle same lesson ID with different user IDs`() = runTest {
        // Given
        val lessonId = 444L
        val userId1 = 555L
        val userId2 = 666L
        val expectedLesson1 = Fixtures.sampleDomainLesson(id = lessonId)
        val expectedLesson2 = Fixtures.sampleDomainLesson(id = lessonId)

        every { mockRepository.getLessonById(lessonId, userId1) } returns flowOf(expectedLesson1)
        every { mockRepository.getLessonById(lessonId, userId2) } returns flowOf(expectedLesson2)

        // When
        val result1 = getLessonById(lessonId, userId1)
        val result2 = getLessonById(lessonId, userId2)

        // Then
        val collectedLesson1 = mutableListOf<DomainLesson?>()
        result1.collect { collectedLesson1.add(it) }
        val collectedLesson2 = mutableListOf<DomainLesson?>()
        result2.collect { collectedLesson2.add(it) }

        assertEquals(expectedLesson1, collectedLesson1[0])
        assertEquals(expectedLesson2, collectedLesson2[0])
    }
}
