package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.billing.Fixtures
import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddGroupLessonTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var addGroupLesson: AddGroupLesson

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        addGroupLesson = AddGroupLesson(mockRepository)
    }

    @Test
    fun `should add group lesson successfully`() = runTest {
        // Given
        val groupId = 123L
        val lesson = Fixtures.sampleDomainLesson()
        val userId = 456L
        val expectedLessonId = 789L

        val expectedLesson = lesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson, userId) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedLesson, userId) }
    }

    @Test
    fun `should add group lesson with default userId when not specified`() = runTest {
        // Given
        val groupId = 101L
        val lesson = Fixtures.sampleDomainLesson()
        val expectedLessonId = 202L

        val expectedLesson = lesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson, 0) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, lesson)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedLesson, 0) }
    }

    @Test
    fun `should add group lesson with zero groupId`() = runTest {
        // Given
        val groupId = 0L
        val lesson = Fixtures.sampleDomainLesson()
        val userId = 303L
        val expectedLessonId = 404L

        val expectedLesson = lesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson, userId) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedLesson, userId) }
    }

    @Test
    fun `should add group lesson with negative groupId`() = runTest {
        // Given
        val groupId = -1L
        val lesson = Fixtures.sampleDomainLesson()
        val userId = 505L
        val expectedLessonId = 606L

        val expectedLesson = lesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson, userId) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedLesson, userId) }
    }

    @Test
    fun `should add group lesson with large groupId`() = runTest {
        // Given
        val groupId = Long.MAX_VALUE
        val lesson = Fixtures.sampleDomainLesson()
        val userId = 707L
        val expectedLessonId = 808L

        val expectedLesson = lesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson, userId) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedLesson, userId) }
    }

    @Test
    fun `should add group lesson with zero userId`() = runTest {
        // Given
        val groupId = 909L
        val lesson = Fixtures.sampleDomainLesson()
        val userId = 0L
        val expectedLessonId = 1010L

        val expectedLesson = lesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson, userId) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedLesson, userId) }
    }

    @Test
    fun `should add group lesson with negative userId`() = runTest {
        // Given
        val groupId = 1111L
        val lesson = Fixtures.sampleDomainLesson()
        val userId = -1L
        val expectedLessonId = 1212L

        val expectedLesson = lesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson, userId) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedLesson, userId) }
    }

    @Test
    fun `should add group lesson with large userId`() = runTest {
        // Given
        val groupId = 1313L
        val lesson = Fixtures.sampleDomainLesson()
        val userId = Long.MAX_VALUE
        val expectedLessonId = 1414L

        val expectedLesson = lesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson, userId) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedLesson, userId) }
    }

    @Test
    fun `should add group lesson with edge case values`() = runTest {
        // Given
        val groupId = Long.MIN_VALUE
        val lesson = Fixtures.sampleDomainLesson()
        val userId = Long.MIN_VALUE
        val expectedLessonId = Long.MAX_VALUE

        val expectedLesson = lesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson, userId) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedLesson, userId) }
    }

    @Test
    fun `should add group lesson with different lesson types`() = runTest {
        // Given
        val groupId = 1515L
        val lesson1 = Fixtures.sampleDomainLesson(durationMinutes = 30)
        val lesson2 = Fixtures.sampleDomainLesson(durationMinutes = 60)
        val lesson3 = Fixtures.sampleDomainLesson(durationMinutes = 90)
        val userId = 1616L

        val expectedLesson1 = lesson1.copy(groupId = groupId)
        val expectedLesson2 = lesson2.copy(groupId = groupId)
        val expectedLesson3 = lesson3.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson1, userId) } returns 1717L
        coEvery { mockRepository.addGroupLesson(expectedLesson2, userId) } returns 1818L
        coEvery { mockRepository.addGroupLesson(expectedLesson3, userId) } returns 1919L

        // When
        val result1 = addGroupLesson(groupId, lesson1, userId)
        val result2 = addGroupLesson(groupId, lesson2, userId)
        val result3 = addGroupLesson(groupId, lesson3, userId)

        // Then
        assertEquals(1717L, result1)
        assertEquals(1818L, result2)
        assertEquals(1919L, result3)

        coVerify {
            mockRepository.addGroupLesson(expectedLesson1, userId)
            mockRepository.addGroupLesson(expectedLesson2, userId)
            mockRepository.addGroupLesson(expectedLesson3, userId)
        }
    }

    @Test
    fun `should add group lesson with different group IDs`() = runTest {
        // Given
        val groupId1 = 2020L
        val groupId2 = 2121L
        val lesson = Fixtures.sampleDomainLesson()
        val userId = 2222L

        val expectedLesson = lesson.copy(groupId = groupId1)
        val expectedLesson2 = lesson.copy(groupId = groupId2)
        coEvery { mockRepository.addGroupLesson(expectedLesson, userId) } returns 2323L
        coEvery { mockRepository.addGroupLesson(expectedLesson2, userId) } returns 2323L

        // When
        val result1 = addGroupLesson(groupId1, lesson, userId)
        val result2 = addGroupLesson(groupId2, lesson, userId)

        // Then
        assertEquals(2323L, result1)
        assertEquals(2323L, result2)

        coVerify(exactly = 1) { mockRepository.addGroupLesson(expectedLesson, userId) }
        coVerify(exactly = 1) { mockRepository.addGroupLesson(expectedLesson2, userId) }
    }

    @Test
    fun `should add group lesson with custom lesson properties`() = runTest {
        // Given
        val groupId = 2424L
        val customLesson = Fixtures.sampleDomainLesson(
            id = 2525L,
            studentId = 2626L,
            groupId = 2727L,
            durationMinutes = 120,
            date = "2024-03-15",
            startTime = "14:00",
            notes = "Custom group lesson with extended duration",
            isInvoiced = false
        )
        val userId = 2828L
        val expectedLessonId = 2929L

        val expectedCustomLesson = customLesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedCustomLesson, userId) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, customLesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedCustomLesson, userId) }
    }

    @Test
    fun `should add group lesson with zero duration`() = runTest {
        // Given
        val groupId = 3030L
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = 0)
        val userId = 3131L
        val expectedLessonId = 3232L

        val expectedLesson = lesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson, userId) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedLesson, userId) }
    }

    @Test
    fun `should add group lesson with large duration`() = runTest {
        // Given
        val groupId = 3333L
        val lesson = Fixtures.sampleDomainLesson(durationMinutes = Int.MAX_VALUE)
        val userId = 3434L
        val expectedLessonId = 3535L

        val expectedLesson = lesson.copy(groupId = groupId)
        coEvery { mockRepository.addGroupLesson(expectedLesson, userId) } returns expectedLessonId

        // When
        val result = addGroupLesson(groupId, lesson, userId)

        // Then
        assertEquals(expectedLessonId, result)
        coVerify { mockRepository.addGroupLesson(expectedLesson, userId) }
    }
}
