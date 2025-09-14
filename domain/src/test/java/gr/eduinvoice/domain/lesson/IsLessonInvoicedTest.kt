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

class IsLessonInvoicedTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var isLessonInvoiced: IsLessonInvoiced

    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        isLessonInvoiced = IsLessonInvoiced(mockRepository)
    }

    @Test
    fun `should return true when lesson is invoiced`() = runTest {
        // Given
        val lessonId = 123L
        val userId = 456L
        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns true

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return false when lesson is not invoiced`() = runTest {
        // Given
        val lessonId = 123L
        val userId = 456L
        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns false

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should work with default userId`() = runTest {
        // Given
        val lessonId = 101L
        coEvery { mockRepository.isLessonInvoiced(lessonId, 0) } returns true

        // When
        val result = isLessonInvoiced(lessonId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return false for non-existent lesson`() = runTest {
        // Given
        val lessonId = 999L
        val userId = 789L
        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns false

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertFalse(result)
    }
}
