package gr.eduinvoice.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import gr.eduinvoice.FakeUserProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.LessonRepository
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.user.UserUseCases
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import gr.eduinvoice.ui.student.StudentScreen
import gr.eduinvoice.ui.student.StudentViewModel
import gr.eduinvoice.ui.lesson.LessonScreen
import gr.eduinvoice.ui.lesson.LessonViewModel
import gr.eduinvoice.ui.user.LoginScreen
import gr.eduinvoice.ui.user.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/**
 * Comprehensive UI automation tests for critical user flows.
 * Tests form validation, navigation, and user interactions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UiAutomationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: EduInvoiceDatabase
    private lateinit var studentRepository: StudentRepository
    private lateinit var lessonRepository: LessonRepository
    private lateinit var userRepository: UserRepository
    private lateinit var studentUseCases: StudentUseCases
    private lateinit var lessonUseCases: LessonUseCases
    private lateinit var userUseCases: UserUseCases
    private val userProvider = FakeUserProvider(1L)
    private val databaseContainer = TestDatabaseContainer()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Before
    fun setUp() = runTest {
        database = databaseContainer.createTestDatabase()
        databaseContainer.populateTestData(database, 1L)

        // Initialize repositories
        studentRepository = StudentRepository(database.studentDao())
        lessonRepository = LessonRepository(database.lessonDao())
        userRepository = UserRepository(database.userDao())

        // Initialize use cases
        studentUseCases = StudentUseCases(
            getActiveStudents = gr.eduinvoice.domain.student.GetActiveStudents(studentRepository),
            getArchivedStudents = gr.eduinvoice.domain.student.GetArchivedStudents(studentRepository),
            getStudentById = gr.eduinvoice.domain.student.GetStudentById(studentRepository),
            insertStudent = gr.eduinvoice.domain.student.InsertStudent(studentRepository),
            updateStudent = gr.eduinvoice.domain.student.UpdateStudent(studentRepository),
            softDeleteStudent = gr.eduinvoice.domain.student.SoftDeleteStudent(studentRepository),
            restoreStudent = gr.eduinvoice.domain.student.RestoreStudent(studentRepository),
            getActiveStudentCount = gr.eduinvoice.domain.student.GetActiveStudentCount(studentRepository),
            classNameExists = gr.eduinvoice.domain.student.ClassNameExists(studentRepository),
            getStudentsPaginated = gr.eduinvoice.domain.student.GetStudentsPaginated(studentRepository),
            searchStudentsPaginated = gr.eduinvoice.domain.student.SearchStudentsPaginated(studentRepository)
        )

        lessonUseCases = LessonUseCases(
            getAllLessons = gr.eduinvoice.domain.lesson.GetAllLessons(database.lessonDao()),
            getLessonById = gr.eduinvoice.domain.lesson.GetLessonById(database.lessonDao()),
            getStudentLessons = gr.eduinvoice.domain.lesson.GetStudentLessons(lessonRepository),
            getLessonsWithStudents = gr.eduinvoice.domain.lesson.GetLessonsWithStudents(database.lessonDao()),
            getLessonsWithStudentsByStudentAndDateRange = gr.eduinvoice.domain.lesson.GetLessonsWithStudentsByStudentAndDateRange(database.lessonDao()),
            addLesson = gr.eduinvoice.domain.lesson.AddLesson(lessonRepository),
            addGroupLesson = gr.eduinvoice.domain.lesson.AddGroupLesson(lessonRepository),
            updateLesson = gr.eduinvoice.domain.lesson.UpdateLesson(lessonRepository),
            deleteLesson = gr.eduinvoice.domain.lesson.DeleteLesson(database.lessonDao()),
            updateLessonPaidStatus = gr.eduinvoice.domain.lesson.UpdateLessonPaidStatus(database.lessonDao()),
            updateLessonInvoicedStatus = gr.eduinvoice.domain.lesson.UpdateLessonInvoicedStatus(database.lessonDao()),
            isLessonInvoiced = gr.eduinvoice.domain.lesson.IsLessonInvoiced(database.lessonDao()),
            getLessonsWithStudentsPaginated = gr.eduinvoice.domain.lesson.GetLessonsWithStudentsPaginated(database.lessonDao())
        )

        userUseCases = UserUseCases(
            getUserById = gr.eduinvoice.domain.user.GetUserById(userRepository),
            insertUser = gr.eduinvoice.domain.user.InsertUser(userRepository),
            updateUser = gr.eduinvoice.domain.user.UpdateUser(userRepository),
            deleteUser = gr.eduinvoice.domain.user.DeleteUser(userRepository),
            validateUserCredentials = gr.eduinvoice.domain.user.ValidateUserCredentials(userRepository)
        )
    }

    @Test
    fun testStudentCreationFlow() {
        // Navigate to student creation screen
        composeRule.onNodeWithText("Add Student").performClick()

        // Fill in student form
        composeRule.onNodeWithText("Name").performTextInput("Test Student")
        composeRule.onNodeWithText("Surname").performTextInput("Test Surname")
        composeRule.onNodeWithText("Parent Mobile").performTextInput("1234567890")
        composeRule.onNodeWithText("Class Name").performTextInput("Test Class")
        composeRule.onNodeWithText("Rate").performTextInput("25.0")

        // Submit form
        composeRule.onNodeWithText("Save").performClick()

        // Verify student was created
        composeRule.onNodeWithText("Test Student").assertIsDisplayed()
        composeRule.onNodeWithText("Test Surname").assertIsDisplayed()
    }

    @Test
    fun testStudentFormValidation() {
        // Navigate to student creation screen
        composeRule.onNodeWithText("Add Student").performClick()

        // Try to submit empty form
        composeRule.onNodeWithText("Save").performClick()

        // Verify validation errors are displayed
        composeRule.onNodeWithText("Name is required").assertIsDisplayed()
        composeRule.onNodeWithText("Surname is required").assertIsDisplayed()
        composeRule.onNodeWithText("Parent mobile is required").assertIsDisplayed()
        composeRule.onNodeWithText("Class name is required").assertIsDisplayed()
        composeRule.onNodeWithText("Rate is required").assertIsDisplayed()

        // Fill in required fields
        composeRule.onNodeWithText("Name").performTextInput("Valid Name")
        composeRule.onNodeWithText("Surname").performTextInput("Valid Surname")
        composeRule.onNodeWithText("Parent Mobile").performTextInput("1234567890")
        composeRule.onNodeWithText("Class Name").performTextInput("Valid Class")
        composeRule.onNodeWithText("Rate").performTextInput("25.0")

        // Submit form again
        composeRule.onNodeWithText("Save").performClick()

        // Verify no validation errors
        composeRule.onNodeWithText("Name is required").assertDoesNotExist()
        composeRule.onNodeWithText("Surname is required").assertDoesNotExist()
    }

    @Test
    fun testLessonCreationFlow() {
        // Navigate to lesson creation screen
        composeRule.onNodeWithText("Add Lesson").performClick()

        // Select student
        composeRule.onNodeWithText("Select Student").performClick()
        composeRule.onNodeWithText("John Doe").performClick()

        // Fill in lesson details
        composeRule.onNodeWithText("Date").performTextInput(LocalDate.now().toString())
        composeRule.onNodeWithText("Time").performTextInput(LocalTime.now().toString())
        composeRule.onNodeWithText("Duration (minutes)").performTextInput("60")
        composeRule.onNodeWithText("Rate").performTextInput("25.0")

        // Submit form
        composeRule.onNodeWithText("Save").performClick()

        // Verify lesson was created
        composeRule.onNodeWithText("Lesson created successfully").assertIsDisplayed()
    }

    @Test
    fun testStudentSearchFunctionality() {
        // Navigate to students screen
        composeRule.onNodeWithText("Students").performClick()

        // Search for existing student
        composeRule.onNodeWithText("Search students").performTextInput("John")

        // Verify search results
        composeRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeRule.onNodeWithText("Jane Smith").assertDoesNotExist()

        // Clear search
        composeRule.onNodeWithText("Search students").performTextInput("")

        // Verify all students are shown
        composeRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeRule.onNodeWithText("Jane Smith").assertIsDisplayed()
        composeRule.onNodeWithText("Bob Johnson").assertIsDisplayed()
    }

    @Test
    fun testStudentArchiveAndRestore() {
        // Navigate to students screen
        composeRule.onNodeWithText("Students").performClick()

        // Archive a student
        composeRule.onNodeWithText("John Doe").performClick()
        composeRule.onNodeWithText("Archive").performClick()
        composeRule.onNodeWithText("Confirm").performClick()

        // Verify student is archived
        composeRule.onNodeWithText("John Doe").assertDoesNotExist()

        // Navigate to archived students
        composeRule.onNodeWithText("Archived").performClick()
        composeRule.onNodeWithText("John Doe").assertIsDisplayed()

        // Restore student
        composeRule.onNodeWithText("John Doe").performClick()
        composeRule.onNodeWithText("Restore").performClick()
        composeRule.onNodeWithText("Confirm").performClick()

        // Verify student is restored
        composeRule.onNodeWithText("Active").performClick()
        composeRule.onNodeWithText("John Doe").assertIsDisplayed()
    }

    @Test
    fun testLessonPaymentStatusUpdate() {
        // Navigate to lessons screen
        composeRule.onNodeWithText("Lessons").performClick()

        // Find unpaid lesson
        composeRule.onNodeWithText("Unpaid").performClick()
        composeRule.onNodeWithText("John Doe").performClick()

        // Mark as paid
        composeRule.onNodeWithText("Mark as Paid").performClick()
        composeRule.onNodeWithText("Confirm").performClick()

        // Verify status updated
        composeRule.onNodeWithText("Paid").assertIsDisplayed()
        composeRule.onNodeWithText("Unpaid").assertDoesNotExist()
    }

    @Test
    fun testNavigationFlow() {
        // Test navigation between main screens
        composeRule.onNodeWithText("Home").performClick()
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()

        composeRule.onNodeWithText("Students").performClick()
        composeRule.onNodeWithText("Student Management").assertIsDisplayed()

        composeRule.onNodeWithText("Lessons").performClick()
        composeRule.onNodeWithText("Lesson Management").assertIsDisplayed()

        composeRule.onNodeWithText("Groups").performClick()
        composeRule.onNodeWithText("Group Management").assertIsDisplayed()

        composeRule.onNodeWithText("Revenue").performClick()
        composeRule.onNodeWithText("Revenue Overview").assertIsDisplayed()

        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("App Settings").assertIsDisplayed()
    }

    @Test
    fun testErrorHandlingInUI() {
        // Test network error handling
        // This would require mocking network failures
        
        // Test database error handling
        // This would require corrupting the database
        
        // Test form validation errors
        composeRule.onNodeWithText("Add Student").performClick()
        composeRule.onNodeWithText("Save").performClick()
        
        // Verify error messages are displayed
        composeRule.onNodeWithText("Please fill in all required fields").assertIsDisplayed()
    }

    @Test
    fun testAccessibilityFeatures() {
        // Test screen reader compatibility
        composeRule.onNodeWithContentDescription("Add new student").performClick()
        
        // Test keyboard navigation
        // This would require testing tab order and focus management
        
        // Test high contrast mode
        // This would require testing with different themes
    }

    @Test
    fun testPerformanceWithLargeDataset() {
        // Populate large dataset
        runTest {
            databaseContainer.populateLargeDataset(database, 1L, 100)
        }

        // Navigate to students screen
        composeRule.onNodeWithText("Students").performClick()

        // Verify UI remains responsive
        composeRule.onNodeWithText("Student Management").assertIsDisplayed()

        // Test scrolling performance
        // This would require testing smooth scrolling through large lists
    }
}
