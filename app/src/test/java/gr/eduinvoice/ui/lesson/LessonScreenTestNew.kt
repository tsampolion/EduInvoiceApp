package gr.eduinvoice.ui.lesson

import gr.eduinvoice.MainDispatcherRule
import gr.eduinvoice.domain.testfixtures.*
import gr.eduinvoice.data.testfixtures.TestDbFactory
import gr.eduinvoice.data.testfixtures.DatabaseTestHelpers
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.student.StudentUseCases
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
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.FakeUserProvider
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

@RunWith(BouncyCastleTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class LessonScreenTestNew {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeStudentRepository: FakeStudentRepository
    private lateinit var fakeLessonRepository: FakeLessonRepository
    private lateinit var noopConcurrencyController: NoopConcurrencyController
    private lateinit var testDataManager: TestDataManager
    private lateinit var userProvider: FakeUserProvider

    @Before
    fun setup() {
        fakeStudentRepository = FakeStudentRepository()
        fakeLessonRepository = FakeLessonRepository()
        noopConcurrencyController = NoopConcurrencyController.create()
        testDataManager = TestDataManager()
        userProvider = FakeUserProvider(1L)
    }

    @Test
    fun `test create lesson with student using new fixtures`() = runTest {
        // Create test data using new fixtures
        val student = createTestStudent(
            id = 1L,
            name = "Alice",
            ownerId = 1L
        )
        
        val lesson = createTestLesson(
            studentId = student.id,
            ownerId = 1L,
            durationMinutes = 60
        )

        // Add to fake repositories
        fakeStudentRepository.addStudent(student)
        fakeLessonRepository.addLesson(lesson)

        // Create use cases with fake repositories
        val studentUseCases = StudentUseCases(
            getActiveStudents = GetActiveStudents(fakeStudentRepository),
            getArchivedStudents = GetArchivedStudents(fakeStudentRepository),
            getStudentById = GetStudentById(fakeStudentRepository),
            insertStudent = InsertStudent(fakeStudentRepository),
            updateStudent = UpdateStudent(fakeStudentRepository),
            softDeleteStudent = SoftDeleteStudent(fakeStudentRepository),
            restoreStudent = RestoreStudent(fakeStudentRepository),
            getActiveStudentCount = GetActiveStudentCount(fakeStudentRepository),
            classNameExists = ClassNameExists(fakeStudentRepository),
            getStudentsPaginated = GetStudentsPaginated(fakeStudentRepository),
            searchStudentsPaginated = SearchStudentsPaginated(fakeStudentRepository)
        )

        // Test that we can retrieve the student
        val retrievedStudent = studentUseCases.getStudentById(student.id, 1L).first()
        assertNotNull(retrievedStudent)
        assertEquals("Alice", retrievedStudent!!.name)
        assertEquals(1L, retrievedStudent.id)
    }

    @Test
    fun `test create multiple students using new fixtures`() = runTest {
        // Create multiple test students using new fixtures
        val students = createTestStudents(
            count = 3,
            ownerId = 1L
        )

        // Add to fake repository
        students.forEach { fakeStudentRepository.addStudent(it) }

        // Create use cases
        val studentUseCases = StudentUseCases(
            getActiveStudents = GetActiveStudents(fakeStudentRepository),
            getArchivedStudents = GetArchivedStudents(fakeStudentRepository),
            getStudentById = GetStudentById(fakeStudentRepository),
            insertStudent = InsertStudent(fakeStudentRepository),
            updateStudent = UpdateStudent(fakeStudentRepository),
            softDeleteStudent = SoftDeleteStudent(fakeStudentRepository),
            restoreStudent = RestoreStudent(fakeStudentRepository),
            getActiveStudentCount = GetActiveStudentCount(fakeStudentRepository),
            classNameExists = ClassNameExists(fakeStudentRepository),
            getStudentsPaginated = GetStudentsPaginated(fakeStudentRepository),
            searchStudentsPaginated = SearchStudentsPaginated(fakeStudentRepository)
        )

        // Test that we can retrieve all active students
        val activeStudents = studentUseCases.getActiveStudents(1L).first()
        assertEquals(3, activeStudents.size)
        assertEquals("Student 1", activeStudents[0].name)
        assertEquals("Student 2", activeStudents[1].name)
        assertEquals("Student 3", activeStudents[2].name)
    }

    @Test
    fun `test create lesson with specific date and time using new fixtures`() = runTest {
        // Create test data with specific date and time
        val student = createTestStudent(
            id = 1L,
            name = "Bob",
            ownerId = 1L
        )
        
        val lesson = createTestLesson(
            studentId = student.id,
            ownerId = 1L,
            date = "2024-01-15",
            startTime = "14:30",
            durationMinutes = 90
        )

        // Add to fake repositories
        fakeStudentRepository.addStudent(student)
        fakeLessonRepository.addLesson(lesson)

        // Test that we can retrieve the lesson
        val retrievedLesson = fakeLessonRepository.getLessonById(lesson.id, 1L).first()
        assertNotNull(retrievedLesson)
        assertEquals("2024-01-15", retrievedLesson!!.date)
        assertEquals("14:30", retrievedLesson.startTime)
        assertEquals(90, retrievedLesson.durationMinutes)
    }
}
