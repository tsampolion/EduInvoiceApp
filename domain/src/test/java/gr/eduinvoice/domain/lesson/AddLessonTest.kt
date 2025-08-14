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

class AddLessonTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var addLesson: AddLesson

    @Before
    fun setup() {
        mockRepository = mockk()
        addLesson = AddLesson(mockRepository)
    }

    @Test
    fun `should add lesson successfully`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val userId = 123L
        val expectedLessonId = 456L
        
        coEvery { mockRepository.addLesson(lesson, userId) } returns expectedLessonId

        // When
        val result = addLesson(lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
    }

    @Test
    fun `should add lesson with default userId when not specified`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val expectedLessonId = 789L
        
        coEvery { mockRepository.addLesson(lesson, 0) } returns expectedLessonId

        // When
        val result = addLesson(lesson)

        // Then
        assertEquals(expectedLessonId, result)
    }

    @Test
    fun `should add lesson with custom userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val customUserId = 999L
        val expectedLessonId = 111L
        
        coEvery { mockRepository.addLesson(lesson, customUserId) } returns expectedLessonId

        // When
        val result = addLesson(lesson, customUserId)

        // Then
        assertEquals(expectedLessonId, result)
    }

    @Test
    fun `should add lesson with zero userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val zeroUserId = 0L
        val expectedLessonId = 222L
        
        coEvery { mockRepository.addLesson(lesson, zeroUserId) } returns expectedLessonId

        // When
        val result = addLesson(lesson, zeroUserId)

        // Then
        assertEquals(expectedLessonId, result)
    }

    @Test
    fun `should add lesson with negative userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val negativeUserId = -1L
        val expectedLessonId = 333L
        
        coEvery { mockRepository.addLesson(lesson, negativeUserId) } returns expectedLessonId

        // When
        val result = addLesson(lesson, negativeUserId)

        // Then
        assertEquals(expectedLessonId, result)
    }

    @Test
    fun `should add lesson with large userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val largeUserId = Long.MAX_VALUE
        val expectedLessonId = 444L
        
        coEvery { mockRepository.addLesson(lesson, largeUserId) } returns expectedLessonId

        // When
        val result = addLesson(lesson, largeUserId)

        // Then
        assertEquals(expectedLessonId, result)
    }

    @Test
    fun `should add lesson with different lesson types`() = runTest {
        // Given
        val regularLesson = Fixtures.sampleDomainLesson()
        val groupLesson = Fixtures.sampleDomainLesson(groupId = 123L)
        val userId = 456L
        val expectedRegularId = 789L
        val expectedGroupId = 101L
        
        coEvery { mockRepository.addLesson(regularLesson, userId) } returns expectedRegularId
        coEvery { mockRepository.addLesson(groupLesson, userId) } returns expectedGroupId

        // When
        val regularResult = addLesson(regularLesson, userId)
        val groupResult = addLesson(groupLesson, userId)

        // Then
        assertEquals(expectedRegularId, regularResult)
        assertEquals(expectedGroupId, groupResult)
    }

    @Test
    fun `should add lesson with different durations`() = runTest {
        // Given
        val shortLesson = Fixtures.sampleDomainLesson(durationMinutes = 30)
        val longLesson = Fixtures.sampleDomainLesson(durationMinutes = 120)
        val userId = 789L
        val expectedShortId = 111L
        val expectedLongId = 222L
        
        coEvery { mockRepository.addLesson(shortLesson, userId) } returns expectedShortId
        coEvery { mockRepository.addLesson(longLesson, userId) } returns expectedLongId

        // When
        val shortResult = addLesson(shortLesson, userId)
        val longResult = addLesson(longLesson, userId)

        // Then
        assertEquals(expectedShortId, shortResult)
        assertEquals(expectedLongId, longResult)
    }
}