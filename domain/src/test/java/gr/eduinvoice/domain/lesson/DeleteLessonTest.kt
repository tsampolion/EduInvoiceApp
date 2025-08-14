package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteLessonTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var deleteLesson: DeleteLesson

    @Before
    fun setup() {
        mockRepository = mockk(relaxed = true)
        deleteLesson = DeleteLesson(mockRepository)
    }

    @Test
    fun `should delete lesson successfully`() = runTest {
        // Given
        val lessonId = 123L
        val userId = 456L
        
        coEvery { mockRepository.deleteLesson(lessonId, userId) } returns Unit

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with zero lesson ID`() = runTest {
        // Given
        val lessonId = 0L
        val userId = 789L
        
        coEvery { mockRepository.deleteLesson(lessonId, userId) } returns Unit

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with negative lesson ID`() = runTest {
        // Given
        val lessonId = -1L
        val userId = 101L
        
        coEvery { mockRepository.deleteLesson(lessonId, userId) } returns Unit

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with large lesson ID`() = runTest {
        // Given
        val lessonId = Long.MAX_VALUE
        val userId = 202L
        
        coEvery { mockRepository.deleteLesson(lessonId, userId) } returns Unit

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with zero userId`() = runTest {
        // Given
        val lessonId = 303L
        val userId = 0L
        
        coEvery { mockRepository.deleteLesson(lessonId, userId) } returns Unit

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with negative userId`() = runTest {
        // Given
        val lessonId = 404L
        val userId = -1L
        
        coEvery { mockRepository.deleteLesson(lessonId, userId) } returns Unit

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should delete lesson with large userId`() = runTest {
        // Given
        val lessonId = 505L
        val userId = Long.MAX_VALUE
        
        coEvery { mockRepository.deleteLesson(lessonId, userId) } returns Unit

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should handle edge case with minimum values`() = runTest {
        // Given
        val lessonId = Long.MIN_VALUE
        val userId = Long.MIN_VALUE
        
        coEvery { mockRepository.deleteLesson(lessonId, userId) } returns Unit

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should handle edge case with maximum values`() = runTest {
        // Given
        val lessonId = Long.MAX_VALUE
        val userId = Long.MAX_VALUE
        
        coEvery { mockRepository.deleteLesson(lessonId, userId) } returns Unit

        // When
        deleteLesson(lessonId, userId)

        // Then
        coVerify { mockRepository.deleteLesson(lessonId, userId) }
    }

    @Test
    fun `should handle multiple delete calls with different parameters`() = runTest {
        // Given
        val lessonId1 = 111L
        val lessonId2 = 222L
        val userId = 333L
        
        coEvery { mockRepository.deleteLesson(lessonId1, userId) } returns Unit
        coEvery { mockRepository.deleteLesson(lessonId2, userId) } returns Unit

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
    fun `should handle same lesson ID with different user IDs`() = runTest {
        // Given
        val lessonId = 444L
        val userId1 = 555L
        val userId2 = 666L
        
        coEvery { mockRepository.deleteLesson(lessonId, userId1) } returns Unit
        coEvery { mockRepository.deleteLesson(lessonId, userId2) } returns Unit

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