package gr.eduinvoice.ui.lessons

import gr.eduinvoice.MainDispatcherRule
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.data.user.CurrentUserProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class LessonsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val studentFlow = MutableStateFlow<List<Student>>(emptyList())
    private val lessonFlow = MutableStateFlow<List<LessonWithStudent>>(emptyList())

    private val userProvider = FakeUserProvider(1L)

    private val studentDao = FakeStudentDao(studentFlow)
    private val lessonDao = FakeLessonDao(lessonFlow)
    private val groupDao = FakeGroupDao()
    private val repository = TutorBillingRepository(studentDao, lessonDao, groupDao)
    private val lessonUseCases = LessonUseCases(
        getAllLessons = GetAllLessons(lessonDao),
        getLessonById = GetLessonById(lessonDao),
        getStudentLessons = GetStudentLessons(repository),
        getLessonsWithStudents = GetLessonsWithStudents(lessonDao),
        getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(lessonDao),
        addLesson = AddLesson(repository),
        addGroupLesson = AddGroupLesson(repository),
        updateLesson = UpdateLesson(repository),
        deleteLesson = DeleteLesson(lessonDao),
        updateLessonPaidStatus = UpdateLessonPaidStatus(lessonDao),
        updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(lessonDao),
        isLessonInvoiced = IsLessonInvoiced(lessonDao)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun sortsLessonsByStudentName() = runTest {
        val s1 = Student(id = 1, name = "Alice", surname = "A", parentMobile = "", className = "", rate = 10.0)
        val s2 = Student(id = 2, name = "Bob", surname = "B", parentMobile = "", className = "", rate = 10.0)
        val today = LocalDate.now().toString()
        lessonFlow.value = listOf(
            LessonWithStudent(
                Lesson(
                    id = 1,
                    studentId = 2,
                    date = today,
                    startTime = "10:00",
                    durationMinutes = 60,
                    notes = null,
                    isPaid = false
                ),
                s2
            ),
            LessonWithStudent(
                Lesson(
                    id = 2,
                    studentId = 1,
                    date = today,
                    startTime = "11:00",
                    durationMinutes = 60,
                    notes = null,
                    isPaid = false
                ),
                s1
            )
        )

        val vm = LessonsViewModel(lessonUseCases, userProvider)
        advanceUntilIdle()

        val list = vm.uiState.value.lessons
        assertEquals(2, list.size)
        assertEquals(listOf(s1.id, s2.id), list.map { it.student.id })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updatePaidShowsGenerateInvoiceForUninvoicedLesson() = runTest {
        val student = Student(id = 1, name = "Alice", surname = "A", parentMobile = "", className = "", rate = 10.0)
        val today = LocalDate.now().toString()
        lessonFlow.value = listOf(
            LessonWithStudent(
                Lesson(
                    id = 1,
                    studentId = 1,
                    date = today,
                    startTime = "10:00",
                    durationMinutes = 60,
                    notes = null,
                    isPaid = false
                ),
                student
            )
        )

        val vm = LessonsViewModel(lessonUseCases, userProvider)
        advanceUntilIdle()

        vm.updatePaid(1, true)
        advanceUntilIdle()

        assertEquals(LessonDialog.GenerateInvoice(1, 1), vm.uiState.value.dialog)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updatePaidShowsAlreadyInvoicedDialogWhenLessonInvoiced() = runTest {
        val student = Student(id = 1, name = "Alice", surname = "A", parentMobile = "", className = "", rate = 10.0)
        val today = LocalDate.now().toString()
        lessonFlow.value = listOf(
            LessonWithStudent(
                Lesson(
                    id = 1,
                    studentId = 1,
                    date = today,
                    startTime = "10:00",
                    durationMinutes = 60,
                    isInvoiced = true
                ),
                student
            )
        )

        val vm = LessonsViewModel(lessonUseCases, userProvider)
        advanceUntilIdle()

        vm.updatePaid(1, true)
        advanceUntilIdle()

        assertEquals(LessonDialog.AlreadyInvoiced(1, true), vm.uiState.value.dialog)
    }

    class FakeStudentDao(private val flow: MutableStateFlow<List<Student>>) : StudentDao {
        override suspend fun insert(student: Student): Long {
            flow.value = flow.value + student
            return student.id
        }
        override suspend fun update(student: Student) {}
        override suspend fun delete(student: Student) {}
        override suspend fun softDeleteStudent(studentId: Long) {}
        override fun getStudentById(studentId: Long, userId: Long): Flow<Student?> =
            flow.map { list -> list.find { it.id == studentId && it.ownerId == userId } }
        override fun getAllActiveStudents(userId: Long): Flow<List<Student>> =
            flow.map { list -> list.filter { it.ownerId == userId } }
        override fun getArchivedStudents(userId: Long): Flow<List<Student>> = flowOf(emptyList())
        override suspend fun restoreStudent(studentId: Long) {}
        override fun getStudentByIdAny(studentId: Long, userId: Long): Flow<Student?> =
            flow.map { list -> list.find { it.id == studentId && it.ownerId == userId } }
        override suspend fun getActiveStudentCount(userId: Long): Int = flow.value.count { it.ownerId == userId }
        override suspend fun classNameExists(name: String, userId: Long): Int = flow.value.count { it.className.equals(name, true) }
    }

    class FakeLessonDao(private val flow: MutableStateFlow<List<LessonWithStudent>>) : LessonDao {
        override suspend fun insert(lesson: Lesson): Long { return 0L }
        override suspend fun update(lesson: Lesson) {}
        override suspend fun delete(lesson: Lesson) {}
        override suspend fun deleteById(lessonId: Long) {}
        override fun getLessonById(lessonId: Long, userId: Long): Flow<Lesson?> =
            flow.map { it.find { l -> l.lesson.id == lessonId && l.lesson.ownerId == userId }?.lesson }
        override fun getLessonsByStudentId(studentId: Long, userId: Long): Flow<List<Lesson>> =
            flow.map { list -> list.filter { it.lesson.studentId == studentId && it.lesson.ownerId == userId }.map { it.lesson } }
        override fun getAllLessons(userId: Long): Flow<List<Lesson>> =
            flow.map { list -> list.filter { it.lesson.ownerId == userId }.map { it.lesson } }
        override fun getLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean) {
            flow.value = flow.value.map { if (it.lesson.id in ids) it.copy(lesson = it.lesson.copy(isPaid = paid)) else it }
        }
        override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean) {
            flow.value = flow.value.map { if (it.lesson.id in ids) it.copy(lesson = it.lesson.copy(isInvoiced = invoiced)) else it }
        }
        override fun isLessonInvoiced(lessonId: Long, userId: Long): Flow<Boolean?> =
            flow.map { list -> list.find { it.lesson.id == lessonId && it.lesson.ownerId == userId }?.lesson?.isInvoiced }
        override fun getLessonsWithStudents(userId: Long): Flow<List<LessonWithStudent>> =
            flow.map { list -> list.filter { it.lesson.ownerId == userId } }
        override fun getLessonsWithStudentsByStudent(studentId: Long, userId: Long): Flow<List<LessonWithStudent>> =
            flow.map { list -> list.filter { it.student.id == studentId && it.lesson.ownerId == userId } }
        override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<LessonWithStudent>> = flowOf(emptyList())
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

    class FakeUserProvider(id: Long?) : CurrentUserProvider {
        private val _id = MutableStateFlow(id)
        override val loggedInUserId: Flow<Long?> = _id
    }
}