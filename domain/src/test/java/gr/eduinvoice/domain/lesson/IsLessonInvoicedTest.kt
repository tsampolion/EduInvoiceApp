package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IsLessonInvoicedTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var isLessonInvoiced: IsLessonInvoiced

    @Before
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
        val lessonId = 789L
        val userId = 101L

        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns false

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should use default userId when not specified`() = runTest {
        // Given
        val lessonId = 202L

        coEvery { mockRepository.isLessonInvoiced(lessonId, 0) } returns true

        // When
        val result = isLessonInvoiced(lessonId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should handle zero lesson ID`() = runTest {
        // Given
        val lessonId = 0L
        val userId = 303L

        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns false

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should handle negative lesson ID`() = runTest {
        // Given
        val lessonId = -1L
        val userId = 404L

        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns false

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should handle large lesson ID`() = runTest {
        // Given
        val lessonId = Long.MAX_VALUE
        val userId = 505L

        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns true

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should handle zero userId`() = runTest {
        // Given
        val lessonId = 606L
        val userId = 0L

        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns false

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should handle negative userId`() = runTest {
        // Given
        val lessonId = 707L
        val userId = -1L

        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns true

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should handle large userId`() = runTest {
        // Given
        val lessonId = 808L
        val userId = Long.MAX_VALUE

        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns false

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should handle edge case with minimum values`() = runTest {
        // Given
        val lessonId = Long.MIN_VALUE
        val userId = Long.MIN_VALUE

        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns true

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should handle edge case with maximum values`() = runTest {
        // Given
        val lessonId = Long.MAX_VALUE
        val userId = Long.MAX_VALUE

        coEvery { mockRepository.isLessonInvoiced(lessonId, userId) } returns false

        // When
        val result = isLessonInvoiced(lessonId, userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should handle multiple calls with different results`() = runTest {
        // Given
        val lessonId1 = 111L
        val lessonId2 = 222L
        val userId = 333L

        coEvery { mockRepository.isLessonInvoiced(lessonId1, userId) } returns true
        coEvery { mockRepository.isLessonInvoiced(lessonId2, userId) } returns false

        // When
        val result1 = isLessonInvoiced(lessonId1, userId)
        val result2 = isLessonInvoiced(lessonId2, userId)

        // Then
        assertTrue(result1)
        assertFalse(result2)
    }

    @Test
    fun `should handle same lesson ID with different user IDs`() = runTest {
        // Given
        val lessonId = 444L
        val userId1 = 555L
        val userId2 = 666L

        coEvery { mockRepository.isLessonInvoiced(lessonId, userId1) } returns true
        coEvery { mockRepository.isLessonInvoiced(lessonId, userId2) } returns false

        // When
        val result1 = isLessonInvoiced(lessonId, userId1)
        val result2 = isLessonInvoiced(lessonId, userId2)

        // Then
        assertTrue(result1)
        assertFalse(result2)
    }
}
