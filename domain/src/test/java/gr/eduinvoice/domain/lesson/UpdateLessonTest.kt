package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.billing.Fixtures
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateLessonTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var updateLesson: UpdateLesson

    @Before
    fun setup() {
        mockRepository = mockk()
        updateLesson = UpdateLesson(mockRepository)
    }

    @Test
    fun `should update lesson successfully`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val userId = 123L

        coEvery { mockRepository.updateLesson(lesson, userId) } returns Unit

        // When
        updateLesson(lesson, userId)

        // Then
        // If we reach here without exception, the test passes
    }

    @Test
    fun `should update lesson with default userId when not specified`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()

        coEvery { mockRepository.updateLesson(lesson, 0) } returns Unit

        // When
        updateLesson(lesson)

        // Then
        // If we reach here without exception, the test passes
    }

    @Test
    fun `should update lesson with custom userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val customUserId = 999L

        coEvery { mockRepository.updateLesson(lesson, customUserId) } returns Unit

        // When
        updateLesson(lesson, customUserId)

        // Then
        // If we reach here without exception, the test passes
    }

    @Test
    fun `should update lesson with zero userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val zeroUserId = 0L

        coEvery { mockRepository.updateLesson(lesson, zeroUserId) } returns Unit

        // When
        updateLesson(lesson, zeroUserId)

        // Then
        // If we reach here without exception, the test passes
    }

    @Test
    fun `should update lesson with negative userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val negativeUserId = -1L

        coEvery { mockRepository.updateLesson(lesson, negativeUserId) } returns Unit

        // When
        updateLesson(lesson, negativeUserId)

        // Then
        // If we reach here without exception, the test passes
    }

    @Test
    fun `should update lesson with large userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val largeUserId = Long.MAX_VALUE

        coEvery { mockRepository.updateLesson(lesson, largeUserId) } returns Unit

        // When
        updateLesson(lesson, largeUserId)

        // Then
        // If we reach here without exception, the test passes
    }

    @Test
    fun `should update lesson with different lesson types`() = runTest {
        // Given
        val regularLesson = Fixtures.sampleDomainLesson(groupId = null)
        val groupLesson = Fixtures.sampleDomainLesson(groupId = 123L)
        val userId = 456L

        coEvery { mockRepository.updateLesson(regularLesson, userId) } returns Unit
        coEvery { mockRepository.updateLesson(groupLesson, userId) } returns Unit

        // When
        updateLesson(regularLesson, userId)
        updateLesson(groupLesson, userId)

        // Then
        // If we reach here without exception, the test passes
    }

    @Test
    fun `should update lesson with different durations`() = runTest {
        // Given
        val shortLesson = Fixtures.sampleDomainLesson(durationMinutes = 30)
        val longLesson = Fixtures.sampleDomainLesson(durationMinutes = 120)
        val userId = 789L

        coEvery { mockRepository.updateLesson(shortLesson, userId) } returns Unit
        coEvery { mockRepository.updateLesson(longLesson, userId) } returns Unit

        // When
        updateLesson(shortLesson, userId)
        updateLesson(longLesson, userId)

        // Then
        // If we reach here without exception, the test passes
    }

    @Test
    fun `should update lesson with different notes`() = runTest {
        // Given
        val lessonWithNotes = Fixtures.sampleDomainLesson(notes = "Updated notes")
        val lessonWithoutNotes = Fixtures.sampleDomainLesson(notes = null)
        val userId = 101L

        coEvery { mockRepository.updateLesson(lessonWithNotes, userId) } returns Unit
        coEvery { mockRepository.updateLesson(lessonWithoutNotes, userId) } returns Unit

        // When
        updateLesson(lessonWithNotes, userId)
        updateLesson(lessonWithoutNotes, userId)

        // Then
        // If we reach here without exception, the test passes
    }

    @Test
    fun `should update lesson with different dates`() = runTest {
        // Given
        val lessonToday = Fixtures.sampleDomainLesson(date = "2024-01-15")
        val lessonTomorrow = Fixtures.sampleDomainLesson(date = "2024-01-16")
        val userId = 202L

        coEvery { mockRepository.updateLesson(lessonToday, userId) } returns Unit
        coEvery { mockRepository.updateLesson(lessonTomorrow, userId) } returns Unit

        // When
        updateLesson(lessonToday, userId)
        updateLesson(lessonTomorrow, userId)

        // Then
        // If we reach here without exception, the test passes
    }
}
