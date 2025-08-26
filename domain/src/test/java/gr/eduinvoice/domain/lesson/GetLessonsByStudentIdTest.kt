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

class GetLessonsByStudentIdTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var getLessonsByStudentId: GetLessonsByStudentId

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        getLessonsByStudentId = GetLessonsByStudentId(mockRepository)
    }

    @Test
    fun `should return flow of lessons for a student`() = runTest {
        // Given
        val studentId = 123L
        val userId = 456L
        val lessons = listOf(Fixtures.sampleDomainLesson(studentId = studentId))
        coEvery { mockRepository.getLessonsByStudentId(studentId, userId) } returns flowOf(lessons)

        // When
        val result = getLessonsByStudentId(studentId, userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(1, result.size)
    }

    @Test
    fun `should return flow of lessons with default userId`() = runTest {
        // Given
        val studentId = 101L
        val lessons = listOf(Fixtures.sampleDomainLesson(studentId = studentId))
        coEvery { mockRepository.getLessonsByStudentId(studentId, 0) } returns flowOf(lessons)

        // When
        val result = getLessonsByStudentId(studentId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(1, result.size)
    }

    @Test
    fun `should return empty flow when student has no lessons`() = runTest {
        // Given
        val studentId = 999L
        val userId = 789L
        coEvery { mockRepository.getLessonsByStudentId(studentId, userId) } returns flowOf(emptyList())

        // When
        val result = getLessonsByStudentId(studentId, userId).first()

        // Then
        assertTrue(result.isEmpty())
        assertEquals(0, result.size)
    }

    @Test
    fun `should handle multiple lessons for a student`() = runTest {
        // Given
        val studentId = 101112L
        val userId = 131415L
        val lessons = listOf(
            Fixtures.sampleDomainLesson(id = 1, studentId = studentId),
            Fixtures.sampleDomainLesson(id = 2, studentId = studentId)
        )
        coEvery { mockRepository.getLessonsByStudentId(studentId, userId) } returns flowOf(lessons)

        // When
        val result = getLessonsByStudentId(studentId, userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(2, result.size)
    }

    @Test
    fun `should return empty flow for non-existent studentId`() = runTest {
        // Given
        val studentId = 9999L
        val userId = 161718L
        coEvery { mockRepository.getLessonsByStudentId(studentId, userId) } returns flowOf(emptyList())

        // When
        val result = getLessonsByStudentId(studentId, userId).first()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should work with zero studentId`() = runTest {
        // Given
        val studentId = 0L
        val userId = 192021L
        val lessons = listOf(Fixtures.sampleDomainLesson(studentId = studentId))
        coEvery { mockRepository.getLessonsByStudentId(studentId, userId) } returns flowOf(lessons)

        // When
        val result = getLessonsByStudentId(studentId, userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(1, result.size)
    }

    @Test
    fun `should work with negative studentId`() = runTest {
        // Given
        val studentId = -1L
        val userId = 222324L
        val lessons = listOf(Fixtures.sampleDomainLesson(studentId = studentId))
        coEvery { mockRepository.getLessonsByStudentId(studentId, userId) } returns flowOf(lessons)

        // When
        val result = getLessonsByStudentId(studentId, userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(1, result.size)
    }

    @Test
    fun `should work with large studentId`() = runTest {
        // Given
        val studentId = Long.MAX_VALUE
        val userId = 252627L
        val lessons = listOf(Fixtures.sampleDomainLesson(studentId = studentId))
        coEvery { mockRepository.getLessonsByStudentId(studentId, userId) } returns flowOf(lessons)

        // When
        val result = getLessonsByStudentId(studentId, userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(1, result.size)
    }

    @Test
    fun `should work with zero userId`() = runTest {
        // Given
        val studentId = 282930L
        val userId = 0L
        val lessons = listOf(Fixtures.sampleDomainLesson(studentId = studentId))
        coEvery { mockRepository.getLessonsByStudentId(studentId, userId) } returns flowOf(lessons)

        // When
        val result = getLessonsByStudentId(studentId, userId).first()

        // Then
        assertEquals(lessons, result)
        assertEquals(1, result.size)
    }

    @Test
    fun `should return empty flow when userId does not match`() = runTest {
        // Given
        val studentId = 313233L
        val correctUserId = 343536L
        val wrongUserId = 373839L
        coEvery { mockRepository.getLessonsByStudentId(studentId, correctUserId) } returns flowOf(listOf(Fixtures.sampleDomainLesson()))
        coEvery { mockRepository.getLessonsByStudentId(studentId, wrongUserId) } returns flowOf(emptyList())

        // When
        val result = getLessonsByStudentId(studentId, wrongUserId).first()

        // Then
        assertTrue(result.isEmpty())
    }
}
