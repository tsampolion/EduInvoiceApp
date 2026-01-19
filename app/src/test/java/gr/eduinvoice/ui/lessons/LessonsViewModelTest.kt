package gr.eduinvoice.ui.lessons

import app.cash.turbine.test
import gr.eduinvoice.data.cache.DataCache
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.user.CurrentUserProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testutil.CoroutineTestRule

@OptIn(ExperimentalCoroutinesApi::class)
class LessonsViewModelTest {

    @get:Rule
    val coroutines = CoroutineTestRule()

    private val lessonUseCases = mockk<LessonUseCases>()
    private val currentUserProvider = mockk<CurrentUserProvider>()
    private val dataCache = mockk<DataCache>(relaxed = true)
    private lateinit var viewModel: LessonsViewModel

    @Before
    fun setup() {
        val testLesson = DomainLesson(
            id = 1L,
            ownerId = 1L,
            studentId = 1L,
            groupId = null,
            date = "2024-01-15",
            startTime = "14:00",
            durationMinutes = 60,
            notes = "Math tutoring session",
            defaultRate = 25.0,
            isPaid = false,
            isInvoiced = false,
            lastModified = System.currentTimeMillis()
        )

        // Mock CurrentUserProvider
        every { currentUserProvider.loggedInUserId } returns flowOf(1L)

        // Mock specific Use Cases inside the holder
        val getAllLessons = mockk<GetAllLessons>()
        every { getAllLessons.invoke(1L) } returns flowOf(listOf(testLesson))
        every { lessonUseCases.getAllLessons } returns getAllLessons

        val getLessonsWithStudents = mockk<GetLessonsWithStudents>()
        every { getLessonsWithStudents.invoke(1L) } returns flowOf(listOf(testLesson))
        every { lessonUseCases.getLessonsWithStudents } returns getLessonsWithStudents

        val getLessonsWithStudentsPaginated = mockk<GetLessonsWithStudentsPaginated>()
        io.mockk.coEvery { getLessonsWithStudentsPaginated.invoke(any(), any(), any()) } returns emptyList() // or explicit list if needed
        every { lessonUseCases.getLessonsWithStudentsPaginated } returns getLessonsWithStudentsPaginated

        viewModel = LessonsViewModel(lessonUseCases, currentUserProvider, dataCache)
    }

    @Test
    fun `emits seeded lesson in expected order`() = runTest {
        viewModel.uiState.test {
            // Depending on flow emission speed, we might get initial empty state or populated state immediately
            val firstState = awaitItem()
            
            val stateToVerify = if (firstState.lessons.isEmpty()) {
                awaitItem()
            } else {
                firstState
            }

            assertEquals(1, stateToVerify.lessons.size)
            assertEquals(1L, stateToVerify.lessons.first().lesson.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `viewModel should have working search query update`() = runTest {
        val testQuery = "test search"
        viewModel.updateSearchQuery(testQuery)
        assertEquals("Search query should be updated", testQuery, viewModel.searchQuery.value)
    }
}
