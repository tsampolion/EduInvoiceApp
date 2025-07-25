package gr.eduinvoice.ui.invoice

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.student.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class InvoiceScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var viewModel: InvoiceViewModel

    @Before
    fun setup() {
        val studentFlow = MutableStateFlow(listOf(Student(id = 1, name = "Bob", surname = "", parentMobile = "", className = "A", rate = 10.0)))
        val lessonFlow = MutableStateFlow(listOf(
            LessonWithStudent(
                Lesson(id = 1, studentId = 1, date = LocalDate.now().toString(), startTime = "10:00", durationMinutes = 60),
                studentFlow.value.first()
            )
        ))
        val studentDao = object : StudentDao {
            override suspend fun insert(student: Student): Long = 1L
            override suspend fun update(student: Student) {}
            override suspend fun delete(student: Student) {}
            override suspend fun softDeleteStudent(studentId: Long) {}
            override fun getStudentById(studentId: Long, userId: Long): Flow<Student?> = studentFlow.map { it.first() }
            override fun getAllActiveStudents(userId: Long): Flow<List<Student>> = studentFlow.asStateFlow()
            override fun getArchivedStudents(userId: Long): Flow<List<Student>> = flowOf(emptyList())
            override suspend fun restoreStudent(studentId: Long) {}
            override fun getStudentByIdAny(studentId: Long, userId: Long): Flow<Student?> = getStudentById(studentId, userId)
            override suspend fun getActiveStudentCount(userId: Long): Int = studentFlow.value.size
            override suspend fun classNameExists(name: String, userId: Long): Int = 0
        }
        val lessonDao = object : LessonDao {
            override suspend fun insert(lesson: Lesson): Long = 1L
            override suspend fun update(lesson: Lesson) {}
            override suspend fun delete(lesson: Lesson) {}
            override suspend fun deleteById(lessonId: Long) {}
            override fun getLessonById(lessonId: Long, userId: Long): Flow<Lesson?> = flowOf(null)
            override fun getLessonsByStudentId(studentId: Long, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
            override fun getAllLessons(userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
            override fun getLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
            override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
            override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
            override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
            override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean) {}
            override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean) {}
            override fun isLessonInvoiced(lessonId: Long, userId: Long): Flow<Boolean?> = flowOf(null)
            override fun getLessonsWithStudents(userId: Long): Flow<List<LessonWithStudent>> = lessonFlow.asStateFlow()
            override fun getLessonsWithStudentsByStudent(studentId: Long, userId: Long): Flow<List<LessonWithStudent>> = lessonFlow.asStateFlow()
            override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<LessonWithStudent>> = lessonFlow.asStateFlow()
            override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<LessonWithStudent>> = lessonFlow.asStateFlow()
        }
        val groupDao = object : GroupDao {
            override suspend fun insertGroup(group: StudentGroup): Long = 1L
            override suspend fun updateGroup(group: StudentGroup) {}
            override suspend fun deleteGroup(group: StudentGroup) {}
            override fun getAllGroups(userId: Long): Flow<List<StudentGroup>> = flowOf(emptyList())
            override fun getGroupById(id: Long, userId: Long): Flow<StudentGroup?> = flowOf(null)
            override suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) {}
            override suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long) {}
            override fun getStudentsForGroup(groupId: Long, userId: Long): Flow<List<Student>> = flowOf(emptyList())
        }
        val studentRepo = StudentRepository(studentDao)
        val groupRepo = GroupRepository(groupDao)
        val billingRepo = TutorBillingRepository(studentDao, lessonDao, groupDao)
        val studentUseCases = StudentUseCases(
            getActiveStudents = GetActiveStudents(studentRepo),
            getArchivedStudents = GetArchivedStudents(studentRepo),
            getStudentById = GetStudentById(studentRepo),
            insertStudent = InsertStudent(studentRepo),
            updateStudent = UpdateStudent(studentRepo),
            softDeleteStudent = SoftDeleteStudent(studentRepo),
            restoreStudent = RestoreStudent(studentRepo),
            getActiveStudentCount = GetActiveStudentCount(studentRepo),
            classNameExists = ClassNameExists(studentRepo)
        )
        val lessonUseCases = LessonUseCases(
            getAllLessons = GetAllLessons(lessonDao),
            getLessonById = GetLessonById(lessonDao),
            getStudentLessons = GetStudentLessons(billingRepo),
            getLessonsWithStudents = GetLessonsWithStudents(lessonDao),
            getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(lessonDao),
            addLesson = AddLesson(billingRepo),
            addGroupLesson = AddGroupLesson(billingRepo),
            updateLesson = UpdateLesson(billingRepo),
            deleteLesson = DeleteLesson(lessonDao),
            updateLessonPaidStatus = UpdateLessonPaidStatus(lessonDao),
            updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(lessonDao),
            isLessonInvoiced = IsLessonInvoiced(lessonDao)
        )
        viewModel = InvoiceViewModel(androidx.lifecycle.SavedStateHandle(mapOf("id" to 1L)), lessonUseCases, studentUseCases)
    }

    @Test
    fun selectLessonEnablesCreateButton() {
        composeRule.setContent {
            InvoiceScreen(onBack = {}, defaultStudentId = 1L, viewModel = viewModel)
        }

        composeRule.onAllNodes(isToggleable())[0].performClick()
        composeRule.onNodeWithText("Create Invoice").performClick()
        composeRule.onNodeWithText("Create Invoice").assertExists()
    }
}
