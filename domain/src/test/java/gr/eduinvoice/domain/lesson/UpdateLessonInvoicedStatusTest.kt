package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateLessonInvoicedStatusTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var updateLessonInvoicedStatus: UpdateLessonInvoicedStatus

    @BeforeEach
    fun setup() {
        mockRepository = mockk(relaxed = true)
        updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(mockRepository)
    }

    @Test
    fun `should update invoiced status to true`() = runTest {
        // Given
        val lessonId = 123L
        val userId = 456L
        val isInvoiced = true

        // When
        updateLessonInvoicedStatus(listOf(lessonId), isInvoiced, userId)

        // Then
        coVerify { mockRepository.updateLessonInvoicedStatus(lessonId, isInvoiced, userId) }
    }

    @Test
    fun `should update invoiced status to false`() = runTest {
        // Given
        val lessonId = 123L
        val userId = 456L
        val isInvoiced = false

        // When
        updateLessonInvoicedStatus(listOf(lessonId), isInvoiced, userId)

        // Then
        coVerify { mockRepository.updateLessonInvoicedStatus(lessonId, isInvoiced, userId) }
    }

    @Test
    fun `should work with default userId`() = runTest {
        // Given
        val lessonId = 101L
        val isInvoiced = true

        // When
        updateLessonInvoicedStatus(listOf(lessonId), isInvoiced, 0)

        // Then
        coVerify { mockRepository.updateLessonInvoicedStatus(lessonId, isInvoiced, 0) }
    }
}
