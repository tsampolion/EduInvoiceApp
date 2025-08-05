package gr.eduinvoice.ui.lesson

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import gr.eduinvoice.MainDispatcherRule
import gr.eduinvoice.testinfrastructure.*
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.GroupStudentCrossRef
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
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.FakeUserProvider
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner
import org.robolectric.annotation.Config

@RunWith(BouncyCastleTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class LessonScreenTest : ComposeTestBase() {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val studentFlow = MutableStateFlow<List<Student>>(emptyList())
    private val lessonFlow = MutableStateFlow<List<LessonWithStudent>>(emptyList())
    private val userProvider = FakeUserProvider(1L)

    private val studentDao = object : StudentDao {
        override suspend fun insert(student: Student): Long = 0L
        override suspend fun update(student: Student) {}
        override suspend fun delete(student: Student) {}
        override suspend fun softDeleteStudent(studentId: Long, userId: Long) {}
        override fun getStudentById(studentId: Long, userId: Long): Flow<Student?> = flowOf(null)
        override fun getAllActiveStudents(userId: Long): Flow<List<Student>> = studentFlow.asStateFlow()
        override fun getArchivedStudents(userId: Long): Flow<List<Student>> = flowOf(emptyList())
        override suspend fun restoreStudent(studentId: Long, userId: Long) {}
        override fun getStudentByIdAny(studentId: Long, userId: Long): Flow<Student?> = flowOf(null)
        override suspend fun getActiveStudentCount(userId: Long): Int = 0
        override suspend fun classNameExists(name: String, userId: Long): Int = 0
    }

    private val lessonDao = object : LessonDao {
        override suspend fun insert(lesson: Lesson): Long = 0L
        override suspend fun update(lesson: Lesson) {}
        override suspend fun delete(lesson: Lesson) {}
        override suspend fun deleteById(lessonId: Long, userId: Long) {}
        override fun getLessonById(lessonId: Long, userId: Long): Flow<Lesson?> = flowOf(null)
        override fun getLessonsByStudentId(studentId: Long, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getAllLessons(userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean, userId: Long) {}
        override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean, userId: Long) {}
        override fun isLessonInvoiced(lessonId: Long, userId: Long): Flow<Boolean?> = flowOf(null)
        override fun getLessonsWithStudents(userId: Long): Flow<List<LessonWithStudent>> = lessonFlow.asStateFlow()
        override fun getLessonsWithStudentsByStudent(studentId: Long, userId: Long): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<LessonWithStudent>> = flowOf(emptyList())

        override suspend fun insertGroupLessons(lessons: List<Lesson>): List<Long> {
            lessons.forEach { insert(it) }
            return lessons.map { it.id }
        }
    }

    private val groupDao = object : GroupDao {
        override suspend fun insertGroup(group: StudentGroup): Long = 0L
        override suspend fun updateGroup(group: StudentGroup) {}
        override suspend fun deleteGroup(group: StudentGroup) {}
        override fun getAllGroups(userId: Long): Flow<List<StudentGroup>> = flowOf(emptyList())
        override fun getGroupById(id: Long, userId: Long): Flow<StudentGroup?> = flowOf(null)
        override suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) {}
        override suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long) {}
        override fun getStudentsForGroup(groupId: Long, userId: Long): Flow<List<Student>> = flowOf(emptyList())
    }

    private val studentRepository = StudentRepository(studentDao)
    private val repository = TutorBillingRepository(studentDao, lessonDao, groupDao)
    private val groupRepository = GroupRepository(groupDao)

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

    private val groupUseCases = GroupUseCases(
        insertGroup = InsertGroup(groupRepository),
        updateGroup = UpdateGroup(groupRepository),
        deleteGroup = DeleteGroup(groupRepository),
        getAllGroups = GetAllGroups(groupRepository),
        getGroupById = GetGroupById(groupRepository),
        addStudentToGroup = AddStudentToGroup(groupRepository),
        removeStudentFromGroup = RemoveStudentFromGroup(groupRepository),
        getGroupStudents = GetGroupStudents(groupRepository)
    )

    private fun createViewModel(): LessonViewModel {
        return LessonViewModel(
            SavedStateHandle(mapOf("lessonId" to 0L)),
            lessonUseCases,
            studentUseCases,
            groupUseCases,
            userProvider
        )
    }

    @Test
    fun dateFieldOpensDatePicker() {
        val vm = createViewModel()
        setComposeContent {
            LessonScreen(studentId = null, lessonId = 0L, onNavigateBack = {}, viewModel = vm)
        }

        composeRule.onNode(hasText("Date") and hasClickAction()).performClick()
        waitForComposeIdle()

        composeRule.onNode(isDialog()).assertExists()
    }

    @Test
    fun timeFieldOpensTimePicker() {
        val vm = createViewModel()
        setComposeContent {
            LessonScreen(studentId = null, lessonId = 0L, onNavigateBack = {}, viewModel = vm)
        }

        composeRule.onNode(hasText("Start Time") and hasClickAction()).performClick()
        waitForComposeIdle()

        composeRule.onNodeWithText("Select time").assertExists()
    }
}
