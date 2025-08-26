package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteLessonTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var deleteLesson: DeleteLesson

    @BeforeEach
    fun setup() {
        mockRepository = mockk(relaxed = true)
        deleteLesson = DeleteLesson(mockRepository)
    }

    @Test
    fun `should delete lesson successfully`() = runTest {
        // Given
        val lessonId = 123L
        val userId = 456L

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with default userId when not specified`() = runTest {
        // Given
        val lessonId = 101L

        // When
        deleteLesson(lessonId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, 0) }
    }

    @Test
    fun `should delete lesson with zero lessonId`() = runTest {
        // Given
        val lessonId = 0L
        val userId = 303L

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with negative lessonId`() = runTest {
        // Given
        val lessonId = -1L
        val userId = 505L

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with large lessonId`() = runTest {
        // Given
        val lessonId = Long.MAX_VALUE
        val userId = 707L

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with zero userId`() = runTest {
        // Given
        val lessonId = 909L
        val userId = 0L

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with negative userId`() = runTest {
        // Given
        val lessonId = 1111L
        val userId = -1L

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with large userId`() = runTest {
        // Given
        val lessonId = 1313L
        val userId = Long.MAX_VALUE

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should handle multiple deletions`() = runTest {
        // Given
        val lessonId1 = 1515L
        val lessonId2 = 1616L
        val userId = 1717L

        // When
        deleteLesson(lessonId1, userId)
        deleteLesson(lessonId2, userId)

        // Then
        coVerify {
            mockRepository.deleteLesson(lessonId1, userId)
            mockRepository.deleteLesson(lessonId2, userId)
        }
    }

    @Test
    fun `should handle deletion with different userIds`() = runTest {
        // Given
        val lessonId = 1818L
        val userId1 = 1919L
        val userId2 = 2020L

        // When
        deleteLesson(lessonId, userId1)
        deleteLesson(lessonId, userId2)

        // Then
        coVerify {
            mockRepository.deleteLesson(lessonId, userId1)
            mockRepository.deleteLesson(lessonId, userId2)
        }
    }
}
