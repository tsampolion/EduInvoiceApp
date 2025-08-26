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

class GetLessonByIdTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var getLessonById: GetLessonById

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        getLessonById = GetLessonById(mockRepository)
    }

    @Test
    fun `should return flow of lesson when found`() = runTest {
        // Given
        val lessonId = 123L
        val userId = 456L
        val lesson = Fixtures.sampleDomainLesson(id = lessonId)
        coEvery { mockRepository.getLessonById(lessonId, userId) } returns flowOf(lesson)

        // When
        val result = getLessonById(lessonId, userId).first()

        // Then
        assertEquals(lesson, result)
        assertEquals(lessonId, result?.id)
    }

    @Test
    fun `should return flow of lesson with default userId`() = runTest {
        // Given
        val lessonId = 101L
        val lesson = Fixtures.sampleDomainLesson(id = lessonId)
        coEvery { mockRepository.getLessonById(lessonId, 0) } returns flowOf(lesson)

        // When
        val result = getLessonById(lessonId).first()

        // Then
        assertEquals(lesson, result)
        assertEquals(lessonId, result?.id)
    }

    @Test
    fun `should return empty flow when lesson not found`() = runTest {
        // Given
        val lessonId = 999L
        val userId = 789L
        coEvery { mockRepository.getLessonById(lessonId, userId) } returns flowOf(null)

        // When
        val result = getLessonById(lessonId, userId).first()

        // Then
        assertNull(result)
    }

    @Test
    fun `should handle zero lessonId`() = runTest {
        // Given
        val lessonId = 0L
        val userId = 101112L
        val lesson = Fixtures.sampleDomainLesson(id = lessonId)
        coEvery { mockRepository.getLessonById(lessonId, userId) } returns flowOf(lesson)

        // When
        val result = getLessonById(lessonId, userId).first()

        // Then
        assertEquals(lesson, result)
        assertEquals(lessonId, result?.id)
    }

    @Test
    fun `should handle negative lessonId`() = runTest {
        // Given
        val lessonId = -1L
        val userId = 131415L
        coEvery { mockRepository.getLessonById(lessonId, userId) } returns flowOf(null)

        // When
        val result = getLessonById(lessonId, userId).first()

        // Then
        assertNull(result)
    }

    @Test
    fun `should handle large lessonId`() = runTest {
        // Given
        val lessonId = Long.MAX_VALUE
        val userId = 161718L
        val lesson = Fixtures.sampleDomainLesson(id = lessonId)
        coEvery { mockRepository.getLessonById(lessonId, userId) } returns flowOf(lesson)

        // When
        val result = getLessonById(lessonId, userId).first()

        // Then
        assertEquals(lesson, result)
        assertEquals(lessonId, result?.id)
    }

    @Test
    fun `should handle zero userId`() = runTest {
        // Given
        val lessonId = 192021L
        val userId = 0L
        val lesson = Fixtures.sampleDomainLesson(id = lessonId)
        coEvery { mockRepository.getLessonById(lessonId, userId) } returns flowOf(lesson)

        // When
        val result = getLessonById(lessonId, userId).first()

        // Then
        assertEquals(lesson, result)
        assertEquals(lessonId, result?.id)
    }

    @Test
    fun `should handle negative userId`() = runTest {
        // Given
        val lessonId = 222324L
        val userId = -1L
        val lesson = Fixtures.sampleDomainLesson(id = lessonId)
        coEvery { mockRepository.getLessonById(lessonId, userId) } returns flowOf(lesson)

        // When
        val result = getLessonById(lessonId, userId).first()

        // Then
        assertEquals(lesson, result)
        assertEquals(lessonId, result?.id)
    }

    @Test
    fun `should handle large userId`() = runTest {
        // Given
        val lessonId = 252627L
        val userId = Long.MAX_VALUE
        val lesson = Fixtures.sampleDomainLesson(id = lessonId)
        coEvery { mockRepository.getLessonById(lessonId, userId) } returns flowOf(lesson)

        // When
        val result = getLessonById(lessonId, userId).first()

        // Then
        assertEquals(lesson, result)
        assertEquals(lessonId, result?.id)
    }

    @Test
    fun `should return null when userId does not match`() = runTest {
        // Given
        val lessonId = 282930L
        val correctUserId = 313233L
        val wrongUserId = 343536L
        val lesson = Fixtures.sampleDomainLesson(id = lessonId)
        coEvery { mockRepository.getLessonById(lessonId, correctUserId) } returns flowOf(lesson)
        coEvery { mockRepository.getLessonById(lessonId, wrongUserId) } returns flowOf(null)

        // When
        val result = getLessonById(lessonId, wrongUserId).first()

        // Then
        assertNull(result)
    }
}
