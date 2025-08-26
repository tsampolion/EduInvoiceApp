package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.billing.Fixtures
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddLessonTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var addLesson: AddLesson

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        addLesson = AddLesson(mockRepository)
    }

    @Test
    fun `should add lesson successfully`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val userId = 456L
        val expectedLessonId = 789L

        coEvery { mockRepository.addLesson(lesson, userId) } returns expectedLessonId

        // When
        val result = addLesson(lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addLesson(lesson, userId) }
    }

    @Test
    fun `should add lesson with default userId when not specified`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val expectedLessonId = 202L

        coEvery { mockRepository.addLesson(lesson, 0) } returns expectedLessonId

        // When
        val result = addLesson(lesson)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addLesson(lesson, 0) }
    }

    @Test
    fun `should add lesson with zero userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val userId = 0L
        val expectedLessonId = 1010L

        coEvery { mockRepository.addLesson(lesson, userId) } returns expectedLessonId

        // When
        val result = addLesson(lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addLesson(lesson, userId) }
    }

    @Test
    fun `should add lesson with negative userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val userId = -1L
        val expectedLessonId = 1212L

        coEvery { mockRepository.addLesson(lesson, userId) } returns expectedLessonId

        // When
        val result = addLesson(lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addLesson(lesson, userId) }
    }

    @Test
    fun `should add lesson with large userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val userId = Long.MAX_VALUE
        val expectedLessonId = 1414L

        coEvery { mockRepository.addLesson(lesson, userId) } returns expectedLessonId

        // When
        val result = addLesson(lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addLesson(lesson, userId) }
    }

    @Test
    fun `should handle different lesson types`() = runTest {
        // Given
        val lesson1 = Fixtures.sampleDomainLesson(durationMinutes = 30)
        val lesson2 = Fixtures.sampleDomainLesson(durationMinutes = 90)
        val userId = 1616L

        coEvery { mockRepository.addLesson(lesson1, userId) } returns 1717L
        coEvery { mockRepository.addLesson(lesson2, userId) } returns 1818L

        // When
        val result1 = addLesson(lesson1, userId)
        val result2 = addLesson(lesson2, userId)

        // Then
        assertEquals(1717L, result1)
        assertEquals(1818L, result2)

        coVerify {
            mockRepository.addLesson(lesson1, userId)
            mockRepository.addLesson(lesson2, userId)
        }
    }

    @Test
    fun `should handle custom lesson properties`() = runTest {
        // Given
        val customLesson = Fixtures.sampleDomainLesson(
            id = 2525L,
            studentId = 2626L,
            durationMinutes = 120,
            date = "2024-03-15",
            startTime = "14:00",
            notes = "Custom lesson with extended duration",
            isInvoiced = false
        )
        val userId = 2828L
        val expectedLessonId = 2929L

        coEvery { mockRepository.addLesson(customLesson, userId) } returns expectedLessonId

        // When
        val result = addLesson(customLesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addLesson(customLesson, userId) }
    }

    @Test
    fun `should handle zero duration lesson`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 0)
        val userId = 3131L
        val expectedLessonId = 3232L

        coEvery { mockRepository.addLesson(lesson, userId) } returns expectedLessonId

        // When
        val result = addLesson(lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addLesson(lesson, userId) }
    }
}
