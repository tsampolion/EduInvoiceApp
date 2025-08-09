package gr.eduinvoice.ui.invoice

import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
import gr.eduinvoice.FakeUserProvider
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.student.*
import gr.eduinvoice.data.testfixtures.TestDbFactory
import gr.eduinvoice.testinfrastructure.AndroidTestInfrastructure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class InvoiceScreenTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<TestHiltActivity>()

    private lateinit var viewModel: InvoiceViewModel
    private lateinit var studentDao: StudentDao
    private lateinit var lessonDao: LessonDao
    private lateinit var groupDao: GroupDao

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = TestDbFactory.createInMemory(context)
        studentDao = db.studentDao()
        lessonDao = db.lessonDao()
        groupDao = db.groupDao()
        
        // Insert test data
        runBlocking {
            val student = AndroidTestInfrastructure.AndroidTestDataFactory.createTestStudent(
                name = "Bob",
                rate = 10.0
            )
            studentDao.insert(student)
            
            val lesson = AndroidTestInfrastructure.AndroidTestDataFactory.createTestLesson(
                studentId = 1,
                durationMinutes = 60
            )
            lessonDao.insert(lesson)
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
        viewModel = InvoiceViewModel(
            androidx.lifecycle.SavedStateHandle(mapOf("id" to 1L)),
            lessonUseCases,
            studentUseCases,
            FakeUserProvider(1L)
        )
    }

    @Test
    fun selectLessonEnablesCreateButton() {
        composeRule.activity.setContent {
            InvoiceScreen(
                onBack = {}, 
                defaultStudentId = 1L, 
                viewModel = viewModel,
                settingsViewModel = hiltViewModel(),
                profileViewModel = hiltViewModel()
            )
        }

        composeRule.onAllNodes(isToggleable())[0].performClick()
        composeRule.onNodeWithText("Create Invoice").performClick()
        composeRule.onNodeWithText("Create Invoice").assertExists()
    }
}
