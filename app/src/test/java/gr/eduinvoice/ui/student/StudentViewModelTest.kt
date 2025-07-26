package gr.eduinvoice.ui.student

import androidx.lifecycle.SavedStateHandle
import gr.eduinvoice.MainDispatcherRule
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.student.*
import gr.eduinvoice.data.user.CurrentUserProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StudentViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val studentFlow = MutableStateFlow<List<Student>>(emptyList())
    private val lessonFlow = MutableStateFlow<List<Lesson>>(emptyList())
    private val userProvider = FakeUserProvider(1L)

    private val userProvider = FakeUserProvider(1L)

    private val studentDao = FakeStudentDao(studentFlow)
    private val lessonDao = FakeLessonDao(lessonFlow)
    private val groupDao = FakeGroupDao()
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
        classNameExists = ClassNameExists(studentRepository)
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
        isLessonInvoiced = IsLessonInvoiced(lessonDao)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun saveStudentClearsLoading() = runTest {
        val vm = StudentViewModel(studentUseCases, lessonUseCases, SavedStateHandle(mapOf("studentId" to 0L)), userProvider)
        vm.updateName("Alice")
        vm.updateSurname("Smith")
        vm.updateSelectedClass("A")
        vm.updateRate("10")
        vm.saveStudent()
        advanceUntilIdle()
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteStudentClearsLoading() = runTest {
        val student = Student(id = 1, name = "Bob", surname = "", parentMobile = "", className = "A", rate = 10.0)
        studentFlow.value = listOf(student)
        val vm = StudentViewModel(studentUseCases, lessonUseCases, SavedStateHandle(mapOf("studentId" to 1L)), userProvider)
        advanceUntilIdle()
        vm.deleteStudent()
        advanceUntilIdle()
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadDataFiltersLessonsByUser() = runTest {
        val student = Student(id = 1, ownerId = 1, name = "Alice", surname = "", parentMobile = "", className = "A", rate = 10.0)
        val otherLesson = Lesson(id = 2, ownerId = 2, studentId = 1, date = "2024-01-01", startTime = "10:00", durationMinutes = 60)
        val myLesson = Lesson(id = 1, ownerId = 1, studentId = 1, date = "2024-01-02", startTime = "10:00", durationMinutes = 60)
        studentFlow.value = listOf(student)
        lessonFlow.value = listOf(myLesson, otherLesson)

        val vm = StudentViewModel(studentUseCases, lessonUseCases, SavedStateHandle(mapOf("studentId" to 1L)), userProvider)
        advanceUntilIdle()

        assertEquals(listOf(myLesson), vm.uiState.value.lessons)
    }

    class FakeStudentDao(private val flow: MutableStateFlow<List<Student>>) : StudentDao {
        override suspend fun insert(student: Student): Long {
            flow.value = flow.value + student
            return student.id
        }
        override suspend fun update(student: Student) {}
        override suspend fun delete(student: Student) {}
        override suspend fun softDeleteStudent(studentId: Long) {
            flow.value = flow.value.filterNot { it.id == studentId }
        }
        override fun getStudentById(studentId: Long, userId: Long): Flow<Student?> =
            flow.map { list -> list.find { it.id == studentId && it.ownerId == userId } }
        override fun getAllActiveStudents(userId: Long): Flow<List<Student>> =
            flow.map { list -> list.filter { it.ownerId == userId } }
        override fun getArchivedStudents(userId: Long): Flow<List<Student>> = flowOf(emptyList())
        override suspend fun restoreStudent(studentId: Long) {}
        override fun getStudentByIdAny(studentId: Long, userId: Long): Flow<Student?> = getStudentById(studentId, userId)
        override suspend fun getActiveStudentCount(userId: Long): Int = flow.value.count { it.ownerId == userId }
        override suspend fun classNameExists(name: String, userId: Long): Int = flow.value.count { it.className.equals(name, true) }
    }

    class FakeLessonDao(private val flow: MutableStateFlow<List<Lesson>>) : LessonDao {
        override suspend fun insert(lesson: Lesson): Long {
            flow.value = flow.value + lesson
            return lesson.id
        }
        override suspend fun update(lesson: Lesson) {}
        override suspend fun delete(lesson: Lesson) {}
        override suspend fun deleteById(lessonId: Long) {
            flow.value = flow.value.filterNot { it.id == lessonId }
        }
        override fun getLessonById(lessonId: Long, userId: Long): Flow<Lesson?> =
            flow.map { list -> list.find { it.id == lessonId && it.ownerId == userId } }
        override fun getLessonsByStudentId(studentId: Long, userId: Long): Flow<List<Lesson>> =
            flow.map { list -> list.filter { it.studentId == studentId && it.ownerId == userId } }
        override fun getAllLessons(userId: Long): Flow<List<Lesson>> =
            flow.map { list -> list.filter { it.ownerId == userId } }
        override fun getLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean) {}
        override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean) {}
        override fun isLessonInvoiced(lessonId: Long, userId: Long): Flow<Boolean?> =
            flow.map { list -> list.find { it.id == lessonId && it.ownerId == userId }?.isInvoiced }
        override fun getLessonsWithStudents(userId: Long) = flowOf(emptyList<gr.eduinvoice.data.database.LessonWithStudent>())
        override fun getLessonsWithStudentsByStudent(studentId: Long, userId: Long) = flowOf(emptyList<gr.eduinvoice.data.database.LessonWithStudent>())
        override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String, userId: Long) = flowOf(emptyList<gr.eduinvoice.data.database.LessonWithStudent>())
        override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long) = flowOf(emptyList<gr.eduinvoice.data.database.LessonWithStudent>())
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

