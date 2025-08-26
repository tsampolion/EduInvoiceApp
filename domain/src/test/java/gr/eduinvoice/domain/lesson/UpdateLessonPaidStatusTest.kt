package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateLessonPaidStatusTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var updateLessonPaidStatus: UpdateLessonPaidStatus

    @BeforeEach
    fun setup() {
        mockRepository = mockk(relaxed = true)
        updateLessonPaidStatus = UpdateLessonPaidStatus(mockRepository)
    }

    @Test
    fun `should update paid status to true`() = runTest {
        // Given
        val lessonId = 123L
        val userId = 456L
        val isPaid = true

        // When
        updateLessonPaidStatus(lessonId, isPaid, userId)

        // Then
        coVerify { mockRepository.updateLessonPaidStatus(lessonId, isPaid, userId) }
    }

    @Test
    fun `should update paid status to false`() = runTest {
        // Given
        val lessonId = 123L
        val userId = 456L
        val isPaid = false

        // When
        updateLessonPaidStatus(lessonId, isPaid, userId)

        // Then
        coVerify { mockRepository.updateLessonPaidStatus(lessonId, isPaid, userId) }
    }

    @Test
    fun `should work with default userId`() = runTest {
        // Given
        val lessonId = 101L
        val isPaid = true

        // When
        updateLessonPaidStatus(lessonId, isPaid)

        // Then
        coVerify { mockRepository.updateLessonPaidStatus(lessonId, isPaid, 0) }
    }
}
