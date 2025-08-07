package gr.eduinvoice.ui.revenue

import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.domain.lesson.GetAllLessons
import gr.eduinvoice.domain.lesson.DeleteLesson
import gr.eduinvoice.domain.lesson.GetLessonById
import gr.eduinvoice.domain.lesson.GetStudentLessons
import gr.eduinvoice.domain.lesson.GetLessonsWithStudents
import gr.eduinvoice.domain.lesson.GetLessonsWithStudentsByStudentAndDateRange
import gr.eduinvoice.domain.lesson.AddLesson
import gr.eduinvoice.domain.lesson.AddGroupLesson
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.lesson.UpdateLesson
import gr.eduinvoice.domain.lesson.UpdateLessonPaidStatus
import gr.eduinvoice.domain.lesson.UpdateLessonInvoicedStatus
import gr.eduinvoice.domain.lesson.IsLessonInvoiced
import gr.eduinvoice.domain.lesson.GetLessonsWithStudentsPaginated
import gr.eduinvoice.data.repository.TutorBillingRepository
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
import gr.eduinvoice.FakeUserProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import gr.eduinvoice.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner
import java.time.LocalDate

@RunWith(BouncyCastleTestRunner::class)
class RevenueViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val studentFlow = MutableStateFlow<List<Student>>(emptyList())
    private val lessonFlow = MutableStateFlow<List<Lesson>>(emptyList())

    private val studentDao = FakeStudentDao(studentFlow)
    private val lessonDao = FakeLessonDao(lessonFlow)
    private val groupDao = FakeGroupDao()
    private val userProvider = FakeUserProvider(1L)
    private val studentRepository = StudentRepository(studentDao)
    private val tutorBillingRepository = TutorBillingRepository(studentDao, lessonDao, groupDao)
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

        val vm = RevenueViewModel(studentUseCases, lessonUseCases, userProvider)
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
        override suspend fun softDeleteStudent(studentId: Long, userId: Long) {}
        override fun getStudentById(studentId: Long, userId: Long): Flow<Student?> = flow.map { list -> list.find { it.id == studentId } }
        override fun getAllActiveStudents(userId: Long): Flow<List<Student>> = flow.asStateFlow()
        override fun getArchivedStudents(userId: Long): Flow<List<Student>> = flowOf(emptyList())
        override suspend fun restoreStudent(studentId: Long, userId: Long) {}
        override fun getStudentByIdAny(studentId: Long, userId: Long): Flow<Student?> = flow.map { list -> list.find { it.id == studentId } }
        override suspend fun getActiveStudentCount(userId: Long): Int = flow.value.size
        override suspend fun classNameExists(name: String, userId: Long): Int = flow.value.count { it.className.equals(name, true) }
        override suspend fun getStudentsPaginated(userId: Long, limit: Int, offset: Int): List<Student> =
            flow.value.drop(offset).take(limit)
        override suspend fun searchStudentsPaginated(userId: Long, searchQuery: String, limit: Int, offset: Int): List<Student> =
            flow.value.filter { it.name.contains(searchQuery, true) || it.surname.contains(searchQuery, true) || it.className.contains(searchQuery, true) }.drop(offset).take(limit)
    }

    class FakeLessonDao(private val flow: MutableStateFlow<List<Lesson>>) : LessonDao {
        override suspend fun insert(lesson: Lesson): Long {
            flow.value = flow.value + lesson
            return lesson.id
        }
        override suspend fun update(lesson: Lesson) {}
        override suspend fun delete(lesson: Lesson) {}
        override suspend fun deleteById(lessonId: Long, userId: Long) {}
        override fun getLessonById(lessonId: Long, userId: Long): Flow<Lesson?> = flow.map { it.find { l -> l.id == lessonId } }
        override fun getLessonsByStudentId(studentId: Long, userId: Long): Flow<List<Lesson>> = flow.map { list -> list.filter { it.studentId == studentId } }
        override fun getAllLessons(userId: Long): Flow<List<Lesson>> = flow.asStateFlow()
        override fun getLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean, userId: Long) {
            flow.value = flow.value.map { if (it.id in ids) it.copy(isPaid = paid) else it }
        }
        override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean, userId: Long) {
            flow.value = flow.value.map { if (it.id in ids) it.copy(isInvoiced = invoiced) else it }
        }
        override fun isLessonInvoiced(lessonId: Long, userId: Long): Flow<Boolean?> = flow.map { list ->
            list.find { it.id == lessonId }?.isInvoiced
        }
        override fun getLessonsWithStudents(userId: Long): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsByStudent(studentId: Long, userId: Long): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override suspend fun getLessonsWithStudentsPaginated(userId: Long, limit: Int, offset: Int): List<LessonWithStudent> = emptyList()

        override suspend fun insertGroupLessons(lessons: List<Lesson>): List<Long> {
            lessons.forEach { insert(it) }
            return lessons.map { it.id }
        }
    }

    class FakeGroupDao : GroupDao {
        override suspend fun insertGroup(group: StudentGroup): Long = 0L
        override suspend fun updateGroup(group: StudentGroup) {}
        override suspend fun deleteGroup(group: StudentGroup) {}
        override fun getAllGroups(userId: Long): Flow<List<StudentGroup>> = flowOf(emptyList())
        override fun getGroupById(id: Long, userId: Long): Flow<StudentGroup?> = flowOf(null)
        override suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) {}
        override suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long) {}
        override fun getStudentsForGroup(groupId: Long, userId: Long): Flow<List<Student>> = flowOf(emptyList())
    }
}
