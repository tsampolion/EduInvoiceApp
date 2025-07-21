package gr.tsambala.tutorbilling.ui.revenue

import gr.tsambala.tutorbilling.data.database.LessonWithStudent
import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.model.Student
import gr.tsambala.tutorbilling.data.repository.StudentRepository
import gr.tsambala.tutorbilling.data.dao.StudentDao
import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.domain.lesson.GetAllLessons
import gr.tsambala.tutorbilling.domain.lesson.DeleteLesson
import gr.tsambala.tutorbilling.domain.lesson.GetLessonById
import gr.tsambala.tutorbilling.domain.lesson.GetStudentLessons
import gr.tsambala.tutorbilling.domain.lesson.GetLessonsWithStudents
import gr.tsambala.tutorbilling.domain.lesson.GetLessonsWithStudentsByStudentAndDateRange
import gr.tsambala.tutorbilling.domain.lesson.AddLesson
import gr.tsambala.tutorbilling.domain.lesson.LessonUseCases
import gr.tsambala.tutorbilling.domain.lesson.UpdateLesson
import gr.tsambala.tutorbilling.domain.lesson.UpdateLessonPaidStatus
import gr.tsambala.tutorbilling.domain.lesson.UpdateLessonInvoicedStatus
import gr.tsambala.tutorbilling.domain.lesson.IsLessonInvoiced
import gr.tsambala.tutorbilling.data.repository.TutorBillingRepository
import gr.tsambala.tutorbilling.domain.student.StudentUseCases
import gr.tsambala.tutorbilling.domain.student.GetActiveStudents
import gr.tsambala.tutorbilling.domain.student.GetArchivedStudents
import gr.tsambala.tutorbilling.domain.student.GetStudentById
import gr.tsambala.tutorbilling.domain.student.InsertStudent
import gr.tsambala.tutorbilling.domain.student.UpdateStudent
import gr.tsambala.tutorbilling.domain.student.SoftDeleteStudent
import gr.tsambala.tutorbilling.domain.student.RestoreStudent
import gr.tsambala.tutorbilling.domain.student.GetActiveStudentCount
import gr.tsambala.tutorbilling.domain.student.ClassNameExists
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import gr.tsambala.tutorbilling.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class RevenueViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val studentFlow = MutableStateFlow<List<Student>>(emptyList())
    private val lessonFlow = MutableStateFlow<List<Lesson>>(emptyList())

    private val studentDao = FakeStudentDao(studentFlow)
    private val lessonDao = FakeLessonDao(lessonFlow)
    private val studentRepository = StudentRepository(studentDao)
    private val tutorBillingRepository = TutorBillingRepository(studentDao, lessonDao)
    private val studentUseCases = StudentUseCases(
        getActiveStudents = GetActiveStudents(studentRepository),
        getArchivedStudents = GetArchivedStudents(studentRepository),
        getStudentById = GetStudentById(studentRepository),
        insertStudent = InsertStudent(studentRepository),
        updateStudent = UpdateStudent(studentRepository),
        softDeleteStudent = SoftDeleteStudent(studentRepository),
        restoreStudent = RestoreStudent(studentRepository),
        getActiveStudentCount = GetActiveStudentCount(studentRepository),
        classNameExists = ClassNameExists(studentRepository)
    )
    private val lessonUseCases = LessonUseCases(
        getAllLessons = GetAllLessons(lessonDao),
        getLessonById = GetLessonById(lessonDao),
        getStudentLessons = GetStudentLessons(tutorBillingRepository),
        getLessonsWithStudents = GetLessonsWithStudents(lessonDao),
        getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(lessonDao),
        addLesson = AddLesson(tutorBillingRepository),
        updateLesson = UpdateLesson(tutorBillingRepository),
        deleteLesson = DeleteLesson(lessonDao),
        updateLessonPaidStatus = UpdateLessonPaidStatus(lessonDao),
        updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(lessonDao),
        isLessonInvoiced = IsLessonInvoiced(lessonDao)
    )

    @Test
    fun debtsCalculatedAndCleared() = runTest {
        val s1 = Student(id = 1, name = "Alice", surname = "", parentMobile = "", className = "A", rate = 20.0)
        val s2 = Student(id = 2, name = "Bob", surname = "", parentMobile = "", className = "B", rate = 15.0)
        studentFlow.value = listOf(s1, s2)

        val today = LocalDate.now().toString()
        lessonFlow.value = listOf(
            Lesson(id = 1, studentId = 1, date = today, startTime = "10:00", durationMinutes = 60, isPaid = false),
            Lesson(id = 2, studentId = 1, date = today, startTime = "11:00", durationMinutes = 60, isPaid = true),
            Lesson(id = 3, studentId = 2, date = today, startTime = "12:00", durationMinutes = 120, isPaid = false)
        )

        val vm = RevenueViewModel(studentUseCases, lessonUseCases)
        advanceUntilIdle()

        val debts = vm.uiState.value.debts
        assertEquals(2, debts.size)
        assertEquals(20.0, debts[0].amount, 0.0)
        assertEquals(30.0, debts[1].amount, 0.0)

        vm.markLessonsPaid(1)
        advanceUntilIdle()
        val updated = vm.uiState.value.debts
        assertEquals(1, updated.size)
        assertEquals(2L, updated[0].student.id)
    }

    class FakeStudentDao(private val flow: MutableStateFlow<List<Student>>) : StudentDao {
        override suspend fun insert(student: Student): Long {
            flow.value = flow.value + student
            return student.id
        }
        override suspend fun update(student: Student) {}
        override suspend fun delete(student: Student) {}
        override suspend fun softDeleteStudent(studentId: Long) {}
        override fun getStudentById(studentId: Long): Flow<Student?> = flow.map { list -> list.find { it.id == studentId } }
        override fun getAllActiveStudents(): Flow<List<Student>> = flow.asStateFlow()
        override fun getArchivedStudents(): Flow<List<Student>> = flowOf(emptyList())
        override suspend fun restoreStudent(studentId: Long) {}
        override fun getStudentByIdAny(studentId: Long): Flow<Student?> = flow.map { list -> list.find { it.id == studentId } }
        override suspend fun getActiveStudentCount(): Int = flow.value.size
        override suspend fun classNameExists(name: String): Int = flow.value.count { it.className.equals(name, true) }
    }

    class FakeLessonDao(private val flow: MutableStateFlow<List<Lesson>>) : LessonDao {
        override suspend fun insert(lesson: Lesson): Long {
            flow.value = flow.value + lesson
            return lesson.id
        }
        override suspend fun update(lesson: Lesson) {}
        override suspend fun delete(lesson: Lesson) {}
        override suspend fun deleteById(lessonId: Long) {}
        override fun getLessonById(lessonId: Long): Flow<Lesson?> = flow.map { it.find { l -> l.id == lessonId } }
        override fun getLessonsByStudentId(studentId: Long): Flow<List<Lesson>> = flow.map { list -> list.filter { it.studentId == studentId } }
        override fun getAllLessons(): Flow<List<Lesson>> = flow.asStateFlow()
        override fun getLessonsInDateRange(startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean) {
            flow.value = flow.value.map { if (it.id in ids) it.copy(isPaid = paid) else it }
        }
        override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean) {
            flow.value = flow.value.map { if (it.id in ids) it.copy(isInvoiced = invoiced) else it }
        }
        override fun isLessonInvoiced(lessonId: Long): Flow<Boolean?> = flow.map { list ->
            list.find { it.id == lessonId }?.isInvoiced
        }
        override fun getLessonsWithStudents(): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsByStudent(studentId: Long): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<LessonWithStudent>> = flowOf(emptyList())
    }
}
