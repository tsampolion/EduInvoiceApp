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

class GetAllLessonsTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var getAllLessons: GetAllLessons

    @Before
    fun setup() {
        mockRepository = mockk()
        getAllLessons = GetAllLessons(mockRepository)
    }

    @Test
    fun `should get all lessons successfully`() = runTest {
        // Given
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(id = 1L),
            Fixtures.sampleDomainLesson(id = 2L),
            Fixtures.sampleDomainLesson(id = 3L)
        )
        val userId = 123L

        every { mockRepository.getAllLessons(userId) } returns flowOf(expectedLessons)

        // When
        val result = getAllLessons(userId)

        // Then
        val collectedLessons = mutableListOf<DomainLesson>()
        result.collect { collectedLessons.addAll(it) }
        assertEquals(expectedLessons.size, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons)
    }

    @Test
    fun `should get all lessons with default userId when not specified`() = runTest {
        // Given
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(id = 1L),
            Fixtures.sampleDomainLesson(id = 2L)
        )

        every { mockRepository.getAllLessons(0) } returns flowOf(expectedLessons)

        // When
        val result = getAllLessons()

        // Then
        val collectedLessons = mutableListOf<DomainLesson>()
        result.collect { collectedLessons.addAll(it) }
        assertEquals(expectedLessons.size, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons)
    }

    @Test
    fun `should get empty list when no lessons exist`() = runTest {
        // Given
        val emptyLessons = emptyList<DomainLesson>()
        val userId = 456L

        every { mockRepository.getAllLessons(userId) } returns flowOf(emptyLessons)

        // When
        val result = getAllLessons(userId)

        // Then
        val collectedLessons = mutableListOf<DomainLesson>()
        result.collect { collectedLessons.addAll(it) }
        assertTrue(collectedLessons.isEmpty())
        assertEquals(0, collectedLessons.size)
    }

    @Test
    fun `should get lessons with custom userId`() = runTest {
        // Given
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(id = 1L, durationMinutes = 60),
            Fixtures.sampleDomainLesson(id = 2L, durationMinutes = 90)
        )
        val customUserId = 999L

        every { mockRepository.getAllLessons(customUserId) } returns flowOf(expectedLessons)

        // When
        val result = getAllLessons(customUserId)

        // Then
        val collectedLessons = mutableListOf<DomainLesson>()
        result.collect { collectedLessons.addAll(it) }
        assertEquals(expectedLessons.size, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons)
    }

    @Test
    fun `should get lessons with zero userId`() = runTest {
        // Given
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(id = 1L)
        )
        val zeroUserId = 0L

        every { mockRepository.getAllLessons(zeroUserId) } returns flowOf(expectedLessons)

        // When
        val result = getAllLessons(zeroUserId)

        // Then
        val collectedLessons = mutableListOf<DomainLesson>()
        result.collect { collectedLessons.addAll(it) }
        assertEquals(expectedLessons.size, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons)
    }

    @Test
    fun `should get lessons with negative userId`() = runTest {
        // Given
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(id = 1L),
            Fixtures.sampleDomainLesson(id = 2L),
            Fixtures.sampleDomainLesson(id = 3L),
            Fixtures.sampleDomainLesson(id = 4L)
        )
        val negativeUserId = -1L

        every { mockRepository.getAllLessons(negativeUserId) } returns flowOf(expectedLessons)

        // When
        val result = getAllLessons(negativeUserId)

        // Then
        val collectedLessons = mutableListOf<DomainLesson>()
        result.collect { collectedLessons.addAll(it) }
        assertEquals(expectedLessons.size, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons)
    }

    @Test
    fun `should get lessons with large userId`() = runTest {
        // Given
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(id = 1L)
        )
        val largeUserId = Long.MAX_VALUE

        every { mockRepository.getAllLessons(largeUserId) } returns flowOf(expectedLessons)

        // When
        val result = getAllLessons(largeUserId)

        // Then
        val collectedLessons = mutableListOf<DomainLesson>()
        result.collect { collectedLessons.addAll(it) }
        assertEquals(expectedLessons.size, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons)
    }

    @Test
    fun `should get lessons with different lesson types`() = runTest {
        // Given
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(id = 1L, groupId = null), // Regular lesson
            Fixtures.sampleDomainLesson(id = 2L, groupId = 123L), // Group lesson
            Fixtures.sampleDomainLesson(id = 3L, groupId = null)  // Regular lesson
        )
        val userId = 789L

        every { mockRepository.getAllLessons(userId) } returns flowOf(expectedLessons)

        // When
        val result = getAllLessons(userId)

        // Then
        val collectedLessons = mutableListOf<DomainLesson>()
        result.collect { collectedLessons.addAll(it) }
        assertEquals(expectedLessons.size, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons)

        // Verify mixed lesson types
        assertTrue(collectedLessons.any { it.groupId == null })
        assertTrue(collectedLessons.any { it.groupId != null })
    }

    @Test
    fun `should get lessons with different durations`() = runTest {
        // Given
        val expectedLessons = listOf(
            Fixtures.sampleDomainLesson(id = 1L, durationMinutes = 30),
            Fixtures.sampleDomainLesson(id = 2L, durationMinutes = 60),
            Fixtures.sampleDomainLesson(id = 3L, durationMinutes = 90),
            Fixtures.sampleDomainLesson(id = 4L, durationMinutes = 120)
        )
        val userId = 101L

        every { mockRepository.getAllLessons(userId) } returns flowOf(expectedLessons)

        // When
        val result = getAllLessons(userId)

        // Then
        val collectedLessons = mutableListOf<DomainLesson>()
        result.collect { collectedLessons.addAll(it) }
        assertEquals(expectedLessons.size, collectedLessons.size)
        assertEquals(expectedLessons, collectedLessons)

        // Verify different durations
        val durations = collectedLessons.map { it.durationMinutes }
        assertTrue(durations.contains(30))
        assertTrue(durations.contains(60))
        assertTrue(durations.contains(90))
        assertTrue(durations.contains(120))
    }
}
