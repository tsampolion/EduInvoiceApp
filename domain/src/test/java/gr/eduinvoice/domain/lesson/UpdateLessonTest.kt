package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.billing.Fixtures
import gr.eduinvoice.domain.repository.DomainLessonRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateLessonTest {

    private lateinit var mockRepository: DomainLessonRepository
    private lateinit var updateLesson: UpdateLesson

    @BeforeEach
    fun setup() {
        mockRepository = mockk(relaxed = true)
        updateLesson = UpdateLesson(mockRepository)
    }

    @Test
    fun `should update lesson successfully`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()
        val userId = 456L

        // When
        updateLesson(lesson, userId)

        // Then
        coVerify { mockRepository.updateLesson(lesson, userId) }
    }

    @Test
    fun `should work with default userId`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson()

        // When
        updateLesson(lesson)

        // Then
        coVerify { mockRepository.updateLesson(lesson, 0) }
    }

    @Test
    fun `should handle lesson with modified properties`() = runTest {
        // Given
        val lesson = Fixtures.sampleDomainLesson(
            notes = "Updated notes",
            fee = 100.0
        )
        val userId = 789L

        // When
        updateLesson(lesson, userId)

        // Then
        coVerify { mockRepository.updateLesson(lesson, userId) }
    }
}
