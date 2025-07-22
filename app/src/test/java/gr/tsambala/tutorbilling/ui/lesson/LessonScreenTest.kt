package gr.tsambala.tutorbilling.ui.lesson

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import gr.tsambala.tutorbilling.MainDispatcherRule
import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.dao.StudentDao
import gr.tsambala.tutorbilling.data.dao.GroupDao
import gr.tsambala.tutorbilling.data.repository.GroupRepository
import gr.tsambala.tutorbilling.data.database.LessonWithStudent
import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.model.Student
import gr.tsambala.tutorbilling.data.model.StudentGroup
import gr.tsambala.tutorbilling.data.model.GroupStudentCrossRef
import gr.tsambala.tutorbilling.data.repository.TutorBillingRepository
import gr.tsambala.tutorbilling.domain.lesson.*
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
import gr.tsambala.tutorbilling.data.repository.StudentRepository
import gr.tsambala.tutorbilling.domain.group.*
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LessonScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val studentFlow = MutableStateFlow<List<Student>>(emptyList())
    private val lessonFlow = MutableStateFlow<List<LessonWithStudent>>(emptyList())

    private val studentDao = object : StudentDao {
        override suspend fun insert(student: Student): Long = 0L
        override suspend fun update(student: Student) {}
        override suspend fun delete(student: Student) {}
        override suspend fun softDeleteStudent(studentId: Long) {}
        override fun getStudentById(studentId: Long): Flow<Student?> = flowOf(null)
        override fun getAllActiveStudents(): Flow<List<Student>> = studentFlow.asStateFlow()
        override fun getArchivedStudents(): Flow<List<Student>> = flowOf(emptyList())
        override suspend fun restoreStudent(studentId: Long) {}
        override fun getStudentByIdAny(studentId: Long): Flow<Student?> = flowOf(null)
        override suspend fun getActiveStudentCount(): Int = 0
        override suspend fun classNameExists(name: String): Int = 0
    }

    private val lessonDao = object : LessonDao {
        override suspend fun insert(lesson: Lesson): Long = 0L
        override suspend fun update(lesson: Lesson) {}
        override suspend fun delete(lesson: Lesson) {}
        override suspend fun deleteById(lessonId: Long) {}
        override fun getLessonById(lessonId: Long): Flow<Lesson?> = flowOf(null)
        override fun getLessonsByStudentId(studentId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getAllLessons(): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getLessonsInDateRange(startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean) {}
        override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean) {}
        override fun isLessonInvoiced(lessonId: Long): Flow<Boolean?> = flowOf(null)
        override fun getLessonsWithStudents(): Flow<List<LessonWithStudent>> = lessonFlow.asStateFlow()
        override fun getLessonsWithStudentsByStudent(studentId: Long): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<LessonWithStudent>> = flowOf(emptyList())
    }

    private val groupDao = object : GroupDao {
        override suspend fun insertGroup(group: StudentGroup): Long = 0L
        override suspend fun updateGroup(group: StudentGroup) {}
        override suspend fun deleteGroup(group: StudentGroup) {}
        override fun getAllGroups(): Flow<List<StudentGroup>> = flowOf(emptyList())
        override fun getGroupById(id: Long): Flow<StudentGroup?> = flowOf(null)
        override suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) {}
        override suspend fun deleteCrossRef(groupId: Long, studentId: Long) {}
        override fun getStudentsForGroup(groupId: Long): Flow<List<Student>> = flowOf(emptyList())
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
        return LessonViewModel(SavedStateHandle(mapOf("lessonId" to 0L)), lessonUseCases, studentUseCases, groupUseCases)
    }

    @Test
    fun dateFieldOpensDatePicker() {
        val vm = createViewModel()
        composeRule.setContent {
            LessonScreen(studentId = null, lessonId = 0L, onNavigateBack = {}, viewModel = vm)
        }

        composeRule.onNode(hasText("Date") and hasClickAction()).performClick()

        composeRule.onNode(isDialog()).assertExists()
    }

    @Test
    fun timeFieldOpensTimePicker() {
        val vm = createViewModel()
        composeRule.setContent {
            LessonScreen(studentId = null, lessonId = 0L, onNavigateBack = {}, viewModel = vm)
        }

        composeRule.onNode(hasText("Start Time") and hasClickAction()).performClick()

        composeRule.onNodeWithText("Select time").assertExists()
    }
}
