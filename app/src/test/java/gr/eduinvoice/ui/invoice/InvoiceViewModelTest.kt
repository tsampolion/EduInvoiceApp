package gr.eduinvoice.ui.invoice

import androidx.lifecycle.SavedStateHandle
import gr.eduinvoice.MainDispatcherRule
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.student.*
import gr.eduinvoice.FakeUserProvider
import gr.eduinvoice.test.support.fakes.NoopConcurrencyController
import gr.eduinvoice.test.support.extensions.createTestStudent
import gr.eduinvoice.test.support.extensions.createTestLesson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner

@RunWith(BouncyCastleTestRunner::class)
class InvoiceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val studentFlow = MutableStateFlow<List<Student>>(emptyList())
    private val lessonFlow = MutableStateFlow<List<Lesson>>(emptyList())

    private val studentDao = object : StudentDao {
        override suspend fun insert(student: Student): Long {
            studentFlow.value = studentFlow.value + student
            return student.id
        }
        override suspend fun update(student: Student) {}
        override suspend fun delete(student: Student) {}
        override suspend fun softDeleteStudent(studentId: Long, userId: Long) {}
        override fun getStudentById(studentId: Long, userId: Long) = studentFlow.map { list -> list.find { it.id == studentId } }
        override fun getAllActiveStudents(userId: Long) = studentFlow.asStateFlow()
        override fun getArchivedStudents(userId: Long) = flowOf(emptyList<Student>())
        override suspend fun restoreStudent(studentId: Long, userId: Long) {}
        override fun getStudentByIdAny(studentId: Long, userId: Long) = getStudentById(studentId, userId)
        override suspend fun getActiveStudentCount(userId: Long) = studentFlow.value.size
        override suspend fun classNameExists(name: String, userId: Long) = 0
        override suspend fun getStudentsPaginated(userId: Long, limit: Int, offset: Int): List<Student> =
            studentFlow.value.drop(offset).take(limit)
        override suspend fun searchStudentsPaginated(userId: Long, searchQuery: String, limit: Int, offset: Int): List<Student> =
            studentFlow.value.filter { it.name.contains(searchQuery, true) || it.surname.contains(searchQuery, true) || it.className.contains(searchQuery, true) }.drop(offset).take(limit)
    }

    private val lessonDao = object : LessonDao {
        override suspend fun insert(lesson: Lesson): Long {
            lessonFlow.value = lessonFlow.value + lesson
            return lesson.id
        }
        override suspend fun update(lesson: Lesson) {}
        override suspend fun delete(lesson: Lesson) {}
        override suspend fun deleteById(lessonId: Long, userId: Long) {}
        override fun getLessonById(lessonId: Long, userId: Long) = lessonFlow.map { it.find { l -> l.id == lessonId } }
        override fun getLessonsByStudentId(studentId: Long, userId: Long) = lessonFlow.map { list -> list.filter { it.studentId == studentId } }
        override fun getAllLessons(userId: Long) = lessonFlow.asStateFlow()
        override fun getLessonsInDateRange(startDate: String, endDate: String, userId: Long) = flowOf(emptyList<Lesson>())
        override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long) = flowOf(emptyList<Lesson>())
        override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long) = flowOf(emptyList<Lesson>())
        override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String, userId: Long) = flowOf(emptyList<Lesson>())
        override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean, userId: Long) {
            lessonFlow.value = lessonFlow.value.map { if (it.id in ids) it.copy(isPaid = paid) else it }
        }
        override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean, userId: Long) {
            lessonFlow.value = lessonFlow.value.map { if (it.id in ids) it.copy(isInvoiced = invoiced) else it }
        }
        override fun isLessonInvoiced(lessonId: Long, userId: Long) = lessonFlow.map { list -> list.find { it.id == lessonId }?.isInvoiced }
        override fun getLessonsWithStudents(userId: Long) = flowOf(emptyList<gr.eduinvoice.data.database.LessonWithStudent>())
        override fun getLessonsWithStudentsByStudent(studentId: Long, userId: Long) = flowOf(emptyList<gr.eduinvoice.data.database.LessonWithStudent>())
        override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String, userId: Long) = flowOf(emptyList<gr.eduinvoice.data.database.LessonWithStudent>())
        override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long) = flowOf(emptyList<gr.eduinvoice.data.database.LessonWithStudent>())
        override suspend fun getLessonsWithStudentsPaginated(userId: Long, limit: Int, offset: Int): List<gr.eduinvoice.data.database.LessonWithStudent> = emptyList()

        override suspend fun insertGroupLessons(lessons: List<Lesson>): List<Long> {
            lessons.forEach { insert(it) }
            return lessons.map { it.id }
        }
    }

    private val groupDao = object : GroupDao {
        override suspend fun insertGroup(group: StudentGroup): Long = 0L
        override suspend fun updateGroup(group: StudentGroup) {}
        override suspend fun deleteGroup(group: StudentGroup) {}
        override fun getAllGroups(userId: Long) = flowOf(emptyList<StudentGroup>())
        override fun getGroupById(id: Long, userId: Long) = flowOf<StudentGroup?>(null)
        override suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) {}
        override suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long) {}
        override fun getStudentsForGroup(groupId: Long, userId: Long) = flowOf(emptyList<Student>())
    }

    private val studentRepository = StudentRepository(studentDao)
    private val concurrencyController = NoopConcurrencyController()
    private val tutorBillingRepository = TutorBillingRepository(studentDao, lessonDao, groupDao, concurrencyController)

    private val studentUseCases = StudentUseCases(
        getActiveStudents = GetActiveStudents(studentRepository),
        getArchivedStudents = GetArchivedStudents(studentRepository),
        getStudentById = GetStudentById(studentRepository),
        insertStudent = InsertStudent(studentRepository),
        updateStudent = UpdateStudent(studentRepository),
        softDeleteStudent = SoftDeleteStudent(studentRepository),
        restoreStudent = RestoreStudent(studentRepository),
        getActiveStudentCount = GetActiveStudentCount(studentRepository),
        classNameExists = ClassNameExists(studentRepository),
        getStudentsPaginated = GetStudentsPaginated(studentRepository),
        searchStudentsPaginated = SearchStudentsPaginated(studentRepository)
    )

    private val lessonUseCases = LessonUseCases(
        getAllLessons = GetAllLessons(lessonDao),
        getLessonById = GetLessonById(lessonDao),
        getStudentLessons = GetStudentLessons(tutorBillingRepository),
        getLessonsWithStudents = GetLessonsWithStudents(lessonDao),
        getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(lessonDao),
        addLesson = AddLesson(tutorBillingRepository),
        addGroupLesson = AddGroupLesson(tutorBillingRepository),
        updateLesson = UpdateLesson(tutorBillingRepository),
        deleteLesson = DeleteLesson(lessonDao),
        updateLessonPaidStatus = UpdateLessonPaidStatus(lessonDao),
        updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(lessonDao),
        isLessonInvoiced = IsLessonInvoiced(lessonDao),
        getLessonsWithStudentsPaginated = GetLessonsWithStudentsPaginated(lessonDao)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun markAsPaidMarksInvoiced() = runTest {
        val student = createTestStudent(id = 1, name = "Alice", className = "A", rate = 10.0)
        studentFlow.value = listOf(student)
        val lesson = createTestLesson(id = 1, studentId = 1, date = "2024-01-01", durationMinutes = 60)
        lessonFlow.value = listOf(lesson)

        val vm = InvoiceViewModel(SavedStateHandle(), lessonUseCases, studentUseCases, FakeUserProvider(1L))
        vm.markAsPaid(listOf(1))
        advanceUntilIdle()

        assertEquals(true, lessonFlow.value.first().isInvoiced)
    }
}
