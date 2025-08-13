package gr.eduinvoice.ui.lessons

import app.cash.turbine.test
import fakes.FakeCurrentUserProvider
import fakes.FakeLessonsRepository
import fakes.FakeStudentsRepository
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.lesson.GetAllLessons
import gr.eduinvoice.domain.lesson.GetLessonsWithStudents
import gr.eduinvoice.domain.lesson.GetLessonById
import gr.eduinvoice.domain.lesson.GetStudentLessons
import gr.eduinvoice.domain.lesson.AddLesson
import gr.eduinvoice.domain.lesson.AddGroupLesson
import gr.eduinvoice.domain.lesson.UpdateLesson
import gr.eduinvoice.domain.lesson.DeleteLesson
import gr.eduinvoice.domain.lesson.UpdateLessonPaidStatus
import gr.eduinvoice.domain.lesson.UpdateLessonInvoicedStatus
import gr.eduinvoice.domain.lesson.IsLessonInvoiced
import gr.eduinvoice.domain.lesson.GetLessonsWithStudentsPaginated
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.student.GetActiveStudents
import gr.eduinvoice.domain.student.GetArchivedStudents
import gr.eduinvoice.domain.student.GetStudentById
import gr.eduinvoice.domain.student.InsertStudent
import gr.eduinvoice.domain.student.UpdateStudent
import gr.eduinvoice.domain.student.SoftDeleteStudent
import gr.eduinvoice.domain.student.RestoreStudent
import gr.eduinvoice.domain.student.GetActiveStudentCount
import gr.eduinvoice.domain.student.ClassNameExists
import gr.eduinvoice.domain.student.GetStudentsPaginated
import gr.eduinvoice.domain.student.SearchStudentsPaginated
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.lesson.GetLessonsWithStudentsByStudentAndDateRange
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testutil.CoroutineTestRule

class LessonsViewModelTest {

    @get:Rule
    val coroutines = CoroutineTestRule()

    private lateinit var fakeLessonsRepository: FakeLessonsRepository
    private lateinit var fakeStudentsRepository: FakeStudentsRepository
    private lateinit var fakeCurrentUserProvider: FakeCurrentUserProvider
    private lateinit var lessonUseCases: LessonUseCases
    private lateinit var viewModel: LessonsViewModel

    @Before
    fun setup() {
        fakeLessonsRepository = FakeLessonsRepository()
        fakeStudentsRepository = FakeStudentsRepository()
        fakeCurrentUserProvider = FakeCurrentUserProvider()

        // Create use cases with fake repositories
        lessonUseCases = LessonUseCases(
            getAllLessons = GetAllLessons(fakeLessonsRepository),
            getLessonById = GetLessonById(fakeLessonsRepository),
            getStudentLessons = GetStudentLessons(fakeLessonsRepository),
            getLessonsWithStudents = GetLessonsWithStudents(fakeLessonsRepository),
            getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(fakeLessonsRepository),
            addLesson = AddLesson(fakeLessonsRepository),
            addGroupLesson = AddGroupLesson(fakeLessonsRepository),
            updateLesson = UpdateLesson(fakeLessonsRepository),
            deleteLesson = DeleteLesson(fakeLessonsRepository),
            updateLessonPaidStatus = UpdateLessonPaidStatus(fakeLessonsRepository),
            updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(fakeLessonsRepository),
            isLessonInvoiced = IsLessonInvoiced(fakeLessonsRepository),
            getLessonsWithStudentsPaginated = GetLessonsWithStudentsPaginated(fakeLessonsRepository)
        )

        // Seed with test data
        val testStudent = DomainStudent(
            id = 1L,
            ownerId = 1L,
            name = "John",
            surname = "Doe",
            parentMobile = "+306912345678",
            parentEmail = "parent@example.com",
            className = "10th Grade",
            rate = 25.0,
            hourlyRate = 30.0,
            rateType = "hourly",
            isActive = true,
            lastModified = System.currentTimeMillis()
        )

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

        fakeStudentsRepository.setStudents(listOf(testStudent))
        fakeLessonsRepository.setLessons(listOf(testLesson))
        fakeCurrentUserProvider.setUserId(1L)

        // Create ViewModel
        viewModel = LessonsViewModel(lessonUseCases, fakeCurrentUserProvider)
    }

    @Test
    fun `emits seeded lesson in expected order`() = runTest {
        viewModel.uiState.test {
            val firstEmission = awaitItem()
            // loadLessonsWithPagination emits asynchronously; allow next emission
            val next = awaitItem()
            assertEquals(1, next.lessons.size)
            assertEquals(1L, next.lessons.first().lesson.id)
            assertEquals(1L, next.lessons.first().student.id)
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
