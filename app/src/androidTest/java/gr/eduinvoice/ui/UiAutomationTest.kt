package gr.eduinvoice.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import gr.eduinvoice.MainActivity
import gr.eduinvoice.TestBase
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.LessonRepository
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.domain.student.StudentUseCase
import gr.eduinvoice.domain.lesson.LessonUseCase
import gr.eduinvoice.domain.user.UserUseCase
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * UI automation tests for critical user flows.
 * Tests automated UI interaction, form validation, and navigation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UiAutomationTest : TestBase() {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @get:Rule
    val databaseContainer = TestDatabaseContainer()
    
    private lateinit var database: EduInvoiceDatabase
    private lateinit var studentRepository: StudentRepository
    private lateinit var lessonRepository: LessonRepository
    private lateinit var userRepository: UserRepository
    
    private lateinit var studentUseCase: StudentUseCase
    private lateinit var lessonUseCase: LessonUseCase
    private lateinit var userUseCase: UserUseCase
    
    @Before
    fun setUp() {
        database = databaseContainer.createTestDatabase()
        
        // Initialize repositories
        studentRepository = StudentRepository(database.studentDao())
        lessonRepository = LessonRepository(database.lessonDao())
        userRepository = UserRepository(database.userDao())
        
        // Initialize use cases
        studentUseCase = StudentUseCase(studentRepository)
        lessonUseCase = LessonUseCase(lessonRepository)
        userUseCase = UserUseCase(userRepository)
    }
    
    @After
    fun tearDown() {
        databaseContainer.cleanupTestDatabase()
    }
    
    @Test
    fun testStudentCreationFlow() = runTest {
        // Navigate to student creation screen
        composeTestRule.onNodeWithText("Students").performClick()
        composeTestRule.onNodeWithText("Add Student").performClick()
        
        // Fill in student form
        composeTestRule.onNodeWithTag("student_name_input").performTextInput("John Doe")
        composeTestRule.onNodeWithTag("student_email_input").performTextInput("john@example.com")
        composeTestRule.onNodeWithTag("student_phone_input").performTextInput("+1234567890")
        composeTestRule.onNodeWithTag("student_rate_input").performTextInput("50.0")
        
        // Submit form
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify student was created
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("john@example.com").assertIsDisplayed()
        
        // Verify in database
        val students = studentUseCase.getAllStudents(1L)
        assertTrue("Student should be created in database", students.any { it.name == "John Doe" })
    }
    
    @Test
    fun testFormValidation() = runTest {
        // Navigate to student creation screen
        composeTestRule.onNodeWithText("Students").performClick()
        composeTestRule.onNodeWithText("Add Student").performClick()
        
        // Try to submit empty form
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify validation errors are displayed
        composeTestRule.onNodeWithText("Name is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
        
        // Fill in invalid data
        composeTestRule.onNodeWithTag("student_name_input").performTextInput("")
        composeTestRule.onNodeWithTag("student_email_input").performTextInput("invalid-email")
        composeTestRule.onNodeWithTag("student_rate_input").performTextInput("-10")
        
        // Submit form
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify validation errors
        composeTestRule.onNodeWithText("Name cannot be empty").assertIsDisplayed()
        composeTestRule.onNodeWithText("Invalid email format").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rate must be positive").assertIsDisplayed()
    }
    
    @Test
    fun testNavigationFlow() = runTest {
        // Test navigation between main screens
        val mainScreens = listOf("Home", "Students", "Lessons", "Groups", "Revenue", "Settings")
        
        mainScreens.forEach { screenName ->
            // Navigate to screen
            composeTestRule.onNodeWithText(screenName).performClick()
            
            // Verify screen is displayed
            composeTestRule.onNodeWithText(screenName).assertIsDisplayed()
            
            // Verify screen-specific content
            when (screenName) {
                "Students" -> {
                    composeTestRule.onNodeWithText("Add Student").assertIsDisplayed()
                }
                "Lessons" -> {
                    composeTestRule.onNodeWithText("Add Lesson").assertIsDisplayed()
                }
                "Groups" -> {
                    composeTestRule.onNodeWithText("Add Group").assertIsDisplayed()
                }
                "Revenue" -> {
                    composeTestRule.onNodeWithText("Total Earnings").assertIsDisplayed()
                }
                "Settings" -> {
                    composeTestRule.onNodeWithText("Backup").assertIsDisplayed()
                }
            }
        }
    }
    
    @Test
    fun testLessonCreationFlow() = runTest {
        // Create a student first
        val user = createTestUser()
        val student = createTestStudent(user.id)
        
        // Navigate to lesson creation
        composeTestRule.onNodeWithText("Lessons").performClick()
        composeTestRule.onNodeWithText("Add Lesson").performClick()
        
        // Fill in lesson form
        composeTestRule.onNodeWithTag("lesson_student_select").performClick()
        composeTestRule.onNodeWithText(student.name).performClick()
        
        composeTestRule.onNodeWithTag("lesson_date_input").performClick()
        composeTestRule.onNodeWithText("OK").performClick() // Date picker OK button
        
        composeTestRule.onNodeWithTag("lesson_duration_input").performTextInput("60")
        composeTestRule.onNodeWithTag("lesson_rate_input").performTextInput("50.0")
        composeTestRule.onNodeWithTag("lesson_notes_input").performTextInput("Math lesson")
        
        // Submit form
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify lesson was created
        composeTestRule.onNodeWithText("Math lesson").assertIsDisplayed()
        composeTestRule.onNodeWithText("60 min").assertIsDisplayed()
        
        // Verify in database
        val lessons = lessonUseCase.getAllLessons(user.id)
        assertTrue("Lesson should be created in database", lessons.any { it.notes == "Math lesson" })
    }
    
    @Test
    fun testStudentSearchAndFilter() = runTest {
        // Create multiple students
        val user = createTestUser()
        val students = listOf(
            createTestStudent(user.id, "Alice Johnson"),
            createTestStudent(user.id, "Bob Smith"),
            createTestStudent(user.id, "Charlie Brown")
        )
        
        // Navigate to students screen
        composeTestRule.onNodeWithText("Students").performClick()
        
        // Test search functionality
        composeTestRule.onNodeWithTag("search_input").performTextInput("Alice")
        
        // Verify search results
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob Smith").assertDoesNotExist()
        composeTestRule.onNodeWithText("Charlie Brown").assertDoesNotExist()
        
        // Clear search
        composeTestRule.onNodeWithTag("search_input").performTextClearance()
        
        // Verify all students are displayed
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob Smith").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charlie Brown").assertIsDisplayed()
    }
    
    @Test
    fun testStudentEditFlow() = runTest {
        // Create a student
        val user = createTestUser()
        val student = createTestStudent(user.id, "Original Name")
        
        // Navigate to students screen
        composeTestRule.onNodeWithText("Students").performClick()
        
        // Click on student to edit
        composeTestRule.onNodeWithText("Original Name").performClick()
        
        // Verify edit screen is displayed
        composeTestRule.onNodeWithText("Edit Student").assertIsDisplayed()
        
        // Modify student information
        composeTestRule.onNodeWithTag("student_name_input").performTextClearance()
        composeTestRule.onNodeWithTag("student_name_input").performTextInput("Updated Name")
        
        // Save changes
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify changes are displayed
        composeTestRule.onNodeWithText("Updated Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Original Name").assertDoesNotExist()
        
        // Verify in database
        val updatedStudent = studentUseCase.getStudent(student.id)
        assertEquals("Student name should be updated in database", "Updated Name", updatedStudent?.name)
    }
    
    @Test
    fun testLessonSchedulingFlow() = runTest {
        // Create a student
        val user = createTestUser()
        val student = createTestStudent(user.id)
        
        // Navigate to lesson scheduling
        composeTestRule.onNodeWithText("Lessons").performClick()
        composeTestRule.onNodeWithText("Schedule Lesson").performClick()
        
        // Fill in scheduling form
        composeTestRule.onNodeWithTag("lesson_student_select").performClick()
        composeTestRule.onNodeWithText(student.name).performClick()
        
        // Select date
        composeTestRule.onNodeWithTag("lesson_date_input").performClick()
        composeTestRule.onNodeWithText("OK").performClick()
        
        // Select time
        composeTestRule.onNodeWithTag("lesson_time_input").performClick()
        composeTestRule.onNodeWithText("OK").performClick()
        
        // Set duration
        composeTestRule.onNodeWithTag("lesson_duration_input").performTextInput("90")
        
        // Add notes
        composeTestRule.onNodeWithTag("lesson_notes_input").performTextInput("Advanced calculus")
        
        // Schedule lesson
        composeTestRule.onNodeWithText("Schedule").performClick()
        
        // Verify lesson is scheduled
        composeTestRule.onNodeWithText("Advanced calculus").assertIsDisplayed()
        composeTestRule.onNodeWithText("90 min").assertIsDisplayed()
    }
    
    @Test
    fun testRevenueCalculationFlow() = runTest {
        // Create student and lessons
        val user = createTestUser()
        val student = createTestStudent(user.id, "Revenue Student")
        
        // Create multiple lessons
        repeat(3) { i ->
            val lesson = Lesson(
                id = (i + 1).toLong(),
                studentId = student.id,
                groupId = null,
                date = LocalDate.now().minusDays(i.toLong()),
                duration = 60,
                hourlyRate = 50.0,
                notes = "Lesson ${i + 1}",
                ownerId = user.id,
                createdAt = LocalDateTime.now()
            )
            lessonUseCase.createLesson(lesson)
        }
        
        // Navigate to revenue screen
        composeTestRule.onNodeWithText("Revenue").performClick()
        
        // Verify revenue calculations
        composeTestRule.onNodeWithText("Total Earnings").assertIsDisplayed()
        composeTestRule.onNodeWithText("150.00").assertIsDisplayed() // 3 lessons * 50.0
        
        // Check student-specific revenue
        composeTestRule.onNodeWithText("Revenue Student").performClick()
        composeTestRule.onNodeWithText("150.00").assertIsDisplayed()
    }
    
    @Test
    fun testErrorHandlingInUI() = runTest {
        // Test UI error handling scenarios
        
        // Navigate to students screen
        composeTestRule.onNodeWithText("Students").performClick()
        composeTestRule.onNodeWithText("Add Student").performClick()
        
        // Try to save with invalid data
        composeTestRule.onNodeWithTag("student_name_input").performTextInput("Test")
        composeTestRule.onNodeWithTag("student_email_input").performTextInput("invalid-email")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify error message is displayed
        composeTestRule.onNodeWithText("Invalid email format").assertIsDisplayed()
        
        // Verify form is still editable
        composeTestRule.onNodeWithTag("student_email_input").assertIsEnabled()
        
        // Fix the error and try again
        composeTestRule.onNodeWithTag("student_email_input").performTextClearance()
        composeTestRule.onNodeWithTag("student_email_input").performTextInput("valid@email.com")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Verify success
        composeTestRule.onNodeWithText("Test").assertIsDisplayed()
    }
    
    @Test
    fun testAccessibilityFeatures() = runTest {
        // Test accessibility features
        
        // Navigate to students screen
        composeTestRule.onNodeWithText("Students").performClick()
        
        // Verify accessibility labels are present
        composeTestRule.onNodeWithContentDescription("Add new student").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search students").assertIsDisplayed()
        
        // Test keyboard navigation
        composeTestRule.onNodeWithTag("search_input").performClick()
        composeTestRule.onNodeWithTag("search_input").performTextInput("test")
        
        // Verify focus management
        composeTestRule.onNodeWithTag("search_input").assertIsFocused()
    }
    
    @Test
    fun testPerformanceUnderLoad() = runTest {
        // Create large dataset
        val user = createTestUser()
        val students = mutableListOf<Student>()
        
        repeat(50) { i ->
            val student = createTestStudent(user.id, "Student $i")
            students.add(student)
        }
        
        // Navigate to students screen
        composeTestRule.onNodeWithText("Students").performClick()
        
        // Verify all students are displayed without performance issues
        students.take(10).forEach { student ->
            composeTestRule.onNodeWithText(student.name).assertIsDisplayed()
        }
        
        // Test scrolling performance
        composeTestRule.onNodeWithText("Student 49").performScrollTo()
        composeTestRule.onNodeWithText("Student 49").assertIsDisplayed()
        
        // Test search performance with large dataset
        composeTestRule.onNodeWithTag("search_input").performTextInput("Student 25")
        composeTestRule.onNodeWithText("Student 25").assertIsDisplayed()
    }
    
    // Helper methods
    private suspend fun createTestUser(): User {
        val user = User(
            id = 1L,
            username = "uitestuser",
            email = "ui@test.com",
            passwordHash = "hashedpassword",
            createdAt = LocalDateTime.now()
        )
        return userUseCase.registerUser(user)
    }
    
    private suspend fun createTestStudent(ownerId: Long, name: String = "Test Student"): Student {
        val student = Student(
            id = 1L,
            name = name,
            email = "test@student.com",
            phone = "+1234567890",
            hourlyRate = 50.0,
            ownerId = ownerId,
            isArchived = false,
            createdAt = LocalDateTime.now()
        )
        return studentUseCase.createStudent(student)
    }
}
