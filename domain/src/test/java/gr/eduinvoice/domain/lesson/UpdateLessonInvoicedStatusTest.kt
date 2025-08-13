package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateLessonInvoicedStatusTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var updateLessonInvoicedStatus: UpdateLessonInvoicedStatus

    @Before
    fun setup() {
        mockRepository = mockk(relaxed = true)
        updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(mockRepository)
    }

    @Test
    fun `should update single lesson invoiced status to true`() = runTest {
        // Given
        val lessonIds = listOf(123L)
        val invoiced = true
        val userId = 456L
        
        coEvery { mockRepository.updateLessonInvoicedStatus(123L, true, 456L) } returns Unit

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        coVerify { mockRepository.updateLessonInvoicedStatus(123L, true, 456L) }
    }

    @Test
    fun `should update single lesson invoiced status to false`() = runTest {
        // Given
        val lessonIds = listOf(789L)
        val invoiced = false
        val userId = 101L
        
        coEvery { mockRepository.updateLessonInvoicedStatus(789L, false, 101L) } returns Unit

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        coVerify { mockRepository.updateLessonInvoicedStatus(789L, false, 101L) }
    }

    @Test
    fun `should update multiple lessons invoiced status`() = runTest {
        // Given
        val lessonIds = listOf(111L, 222L, 333L)
        val invoiced = true
        val userId = 444L
        
        coEvery { mockRepository.updateLessonInvoicedStatus(111L, true, 444L) } returns Unit
        coEvery { mockRepository.updateLessonInvoicedStatus(222L, true, 444L) } returns Unit
        coEvery { mockRepository.updateLessonInvoicedStatus(333L, true, 444L) } returns Unit

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        coVerify { 
            mockRepository.updateLessonInvoicedStatus(111L, true, 444L)
            mockRepository.updateLessonInvoicedStatus(222L, true, 444L)
            mockRepository.updateLessonInvoicedStatus(333L, true, 444L)
        }
    }

    @Test
    fun `should handle empty list of lesson IDs`() = runTest {
        // Given
        val lessonIds = emptyList<Long>()
        val invoiced = true
        val userId = 555L

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        // No repository calls should be made
        coVerify(exactly = 0) { mockRepository.updateLessonInvoicedStatus(any(), any(), any()) }
    }

    @Test
    fun `should handle single lesson ID with zero userId`() = runTest {
        // Given
        val lessonIds = listOf(666L)
        val invoiced = false
        val userId = 0L
        
        coEvery { mockRepository.updateLessonInvoicedStatus(666L, false, 0L) } returns Unit

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        coVerify { mockRepository.updateLessonInvoicedStatus(666L, false, 0L) }
    }

    @Test
    fun `should handle single lesson ID with negative userId`() = runTest {
        // Given
        val lessonIds = listOf(777L)
        val invoiced = true
        val userId = -1L
        
        coEvery { mockRepository.updateLessonInvoicedStatus(777L, true, -1L) } returns Unit

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        coVerify { mockRepository.updateLessonInvoicedStatus(777L, true, -1L) }
    }

    @Test
    fun `should handle single lesson ID with large userId`() = runTest {
        // Given
        val lessonIds = listOf(888L)
        val invoiced = false
        val userId = Long.MAX_VALUE
        
        coEvery { mockRepository.updateLessonInvoicedStatus(888L, false, Long.MAX_VALUE) } returns Unit

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        coVerify { mockRepository.updateLessonInvoicedStatus(888L, false, Long.MAX_VALUE) }
    }

    @Test
    fun `should handle lesson ID with zero value`() = runTest {
        // Given
        val lessonIds = listOf(0L)
        val invoiced = true
        val userId = 999L
        
        coEvery { mockRepository.updateLessonInvoicedStatus(0L, true, 999L) } returns Unit

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        coVerify { mockRepository.updateLessonInvoicedStatus(0L, true, 999L) }
    }

    @Test
    fun `should handle lesson ID with negative value`() = runTest {
        // Given
        val lessonIds = listOf(-1L)
        val invoiced = false
        val userId = 1000L
        
        coEvery { mockRepository.updateLessonInvoicedStatus(-1L, false, 1000L) } returns Unit

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        coVerify { mockRepository.updateLessonInvoicedStatus(-1L, false, 1000L) }
    }

    @Test
    fun `should handle lesson ID with large value`() = runTest {
        // Given
        val lessonIds = listOf(Long.MAX_VALUE)
        val invoiced = true
        val userId = 1111L
        
        coEvery { mockRepository.updateLessonInvoicedStatus(Long.MAX_VALUE, true, 1111L) } returns Unit

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        coVerify { mockRepository.updateLessonInvoicedStatus(Long.MAX_VALUE, true, 1111L) }
    }

    @Test
    fun `should handle mixed lesson IDs including edge cases`() = runTest {
        // Given
        val lessonIds = listOf(0L, -1L, 123L, Long.MAX_VALUE)
        val invoiced = true
        val userId = 2222L
        
        coEvery { mockRepository.updateLessonInvoicedStatus(0L, true, 2222L) } returns Unit
        coEvery { mockRepository.updateLessonInvoicedStatus(-1L, true, 2222L) } returns Unit
        coEvery { mockRepository.updateLessonInvoicedStatus(123L, true, 2222L) } returns Unit
        coEvery { mockRepository.updateLessonInvoicedStatus(Long.MAX_VALUE, true, 2222L) } returns Unit

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        coVerify { 
            mockRepository.updateLessonInvoicedStatus(0L, true, 2222L)
            mockRepository.updateLessonInvoicedStatus(-1L, true, 2222L)
            mockRepository.updateLessonInvoicedStatus(123L, true, 2222L)
            mockRepository.updateLessonInvoicedStatus(Long.MAX_VALUE, true, 2222L)
        }
    }

    @Test
    fun `should handle large list of lesson IDs`() = runTest {
        // Given
        val lessonIds = (1L..100L).toList()
        val invoiced = false
        val userId = 3333L
        
        lessonIds.forEach { id ->
            coEvery { mockRepository.updateLessonInvoicedStatus(id, false, 3333L) } returns Unit
        }

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        lessonIds.forEach { id ->
            coVerify { mockRepository.updateLessonInvoicedStatus(id, false, 3333L) }
        }
    }

    @Test
    fun `should handle duplicate lesson IDs`() = runTest {
        // Given
        val lessonIds = listOf(123L, 123L, 456L, 456L)
        val invoiced = true
        val userId = 4444L
        
        coEvery { mockRepository.updateLessonInvoicedStatus(123L, true, 4444L) } returns Unit
        coEvery { mockRepository.updateLessonInvoicedStatus(456L, true, 4444L) } returns Unit

        // When
        updateLessonInvoicedStatus(lessonIds, invoiced, userId)

        // Then
        coVerify(exactly = 2) { mockRepository.updateLessonInvoicedStatus(123L, true, 4444L) }
        coVerify(exactly = 2) { mockRepository.updateLessonInvoicedStatus(456L, true, 4444L) }
    }
}