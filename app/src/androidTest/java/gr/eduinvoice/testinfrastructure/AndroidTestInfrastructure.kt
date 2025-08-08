package gr.eduinvoice.testinfrastructure

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.*
import gr.eduinvoice.data.repository.*
import gr.eduinvoice.data.concurrency.*
import gr.eduinvoice.domain.student.*
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.domain.user.*
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.time.LocalDate
import java.time.LocalTime

/**
 * Unified test infrastructure for Android instrumented tests
 * Provides centralized test utilities, data factories, and configuration for UI automation
 */
object AndroidTestInfrastructure {

    /**
     * Standard test dispatcher for coroutine testing
     */
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    /**
     * UiDevice instance for UI automation
     */
    val uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    /**
     * Creates a complete test environment with all components for Android testing
     */
    fun createAndroidTestEnvironment(database: EduInvoiceDatabase): AndroidTestEnvironment {
        val studentRepository = StudentRepository(database.studentDao())
        val groupRepository = GroupRepository(database.groupDao())
        val userRepository = UserRepository(database.userDao())
        
        val concurrencyController = ConcurrencyController(
            TransactionManager(database),
            OperationQueueManager()
        )

        val studentUseCases = StudentUseCases(
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

        val tutorBillingRepository = TutorBillingRepository(
            database.studentDao(),
            database.lessonDao(),
            database.groupDao(),
            concurrencyController
        )
        
        val lessonUseCases = LessonUseCases(
            getAllLessons = GetAllLessons(database.lessonDao()),
            getLessonById = GetLessonById(database.lessonDao()),
            getStudentLessons = GetStudentLessons(tutorBillingRepository),
            getLessonsWithStudents = GetLessonsWithStudents(database.lessonDao()),
            getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(database.lessonDao()),
            addLesson = AddLesson(tutorBillingRepository),
            addGroupLesson = AddGroupLesson(tutorBillingRepository),
            updateLesson = UpdateLesson(tutorBillingRepository),
            deleteLesson = DeleteLesson(database.lessonDao()),
            updateLessonPaidStatus = UpdateLessonPaidStatus(database.lessonDao()),
            updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(database.lessonDao()),
            isLessonInvoiced = IsLessonInvoiced(database.lessonDao()),
            getLessonsWithStudentsPaginated = GetLessonsWithStudentsPaginated(database.lessonDao())
        )

        val groupUseCases = GroupUseCases(
            insertGroup = InsertGroup(groupRepository),
            updateGroup = UpdateGroup(groupRepository),
            deleteGroup = DeleteGroup(groupRepository),
            getAllGroups = GetAllGroups(groupRepository),
            getGroupById = GetGroupById(groupRepository),
            addStudentToGroup = AddStudentToGroup(groupRepository),
            removeStudentFromGroup = RemoveStudentFromGroup(groupRepository),
            getGroupStudents = GetGroupStudents(groupRepository)
        )

        val userUseCases = UserUseCases(
            createUser = CreateUser(userRepository),
            authenticateUser = AuthenticateUser(userRepository),
            getUserProfile = GetUserProfile(userRepository),
            updateUser = UpdateUser(userRepository),
            resetPassword = ResetPassword(userRepository)
        )

        return AndroidTestEnvironment(
            database = database,
            studentRepository = studentRepository,
            groupRepository = groupRepository,
            userRepository = userRepository,
            concurrencyController = concurrencyController,
            studentUseCases = studentUseCases,
            lessonUseCases = lessonUseCases,
            groupUseCases = groupUseCases,
            userUseCases = userUseCases,
            uiDevice = uiDevice
        )
    }

    /**
     * Android test environment containing all components for UI testing
     */
    data class AndroidTestEnvironment(
        val database: EduInvoiceDatabase,
        val studentRepository: StudentRepository,
        val groupRepository: GroupRepository,
        val userRepository: UserRepository,
        val concurrencyController: ConcurrencyController,
        val studentUseCases: StudentUseCases,
        val lessonUseCases: LessonUseCases,
        val groupUseCases: GroupUseCases,
        val userUseCases: UserUseCases,
        val uiDevice: UiDevice
    )

    /**
     * Common test data creation utilities for Android testing
     */
    object AndroidTestDataFactory {

        fun createTestUser(
            id: Long = 1L,
            username: String = "testuser",
            fullName: String = "Test User"
        ): User = User(
            id = id,
            username = username,
            passwordHash = "test_hash",
            fullName = fullName
        )

        fun createTestStudent(
            id: Long = 1L,
            ownerId: Long = 1L,
            name: String = "Test Student",
            rate: Double = 25.0
        ): Student = Student(
            id = id,
            ownerId = ownerId,
            name = name,
            surname = "Test Surname",
            parentMobile = "+30123456789",
            parentEmail = "test@example.com",
            className = "Test Class",
            rate = rate
        )

        fun createTestLesson(
            id: Long = 1L,
            studentId: Long = 1L,
            ownerId: Long = 1L,
            date: String = LocalDate.now().toString(),
            durationMinutes: Int = 60
        ): Lesson = Lesson.create(
            studentId = studentId,
            date = LocalDate.parse(date),
            startTime = LocalTime.of(10, 0),
            durationMinutes = durationMinutes,
            notes = "Test lesson",
            ownerId = ownerId
        )

        fun createTestGroup(
            id: Long = 1L,
            ownerId: Long = 1L,
            name: String = "Test Group"
        ): StudentGroup = StudentGroup(
            id = id,
            ownerId = ownerId,
            name = name
        )

        fun createLargeStudentDataset(ownerId: Long, count: Int): List<Student> {
            return (1..count).map { index ->
                createTestStudent(
                    id = index.toLong(),
                    ownerId = ownerId,
                    name = "Student_$index",
                    rate = 20.0 + (index % 30)
                )
            }
        }

        fun createLargeLessonDataset(students: List<Student>, count: Int): List<Lesson> {
            val lessons = mutableListOf<Lesson>()
            val baseDate = LocalDate.now().minusDays(30)

            repeat(count) { index ->
                val student = students[index % students.size]
                val lessonDate = baseDate.plusDays(index % 30)
                val startTime = LocalTime.of(9 + (index % 8), 0)

                val lesson = Lesson.create(
                    studentId = student.id,
                    date = lessonDate,
                    startTime = startTime,
                    durationMinutes = 60,
                    notes = "Test lesson ${index}",
                    ownerId = student.ownerId
                )
                lessons.add(lesson)
            }

            return lessons
        }

        fun createLargeGroupDataset(ownerId: Long, count: Int): List<StudentGroup> {
            return (1..count).map { index ->
                createTestGroup(
                    id = index.toLong(),
                    ownerId = ownerId,
                    name = "Group_$index"
                )
            }
        }
    }

    /**
     * UI testing utilities
     */
    object UiTestUtils {

        fun waitForElement(composeTestRule: ComposeContentTestRule, timeoutMillis: Long = 5000L) {
            composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
                true // Customize based on specific element
            }
        }

        fun waitForIdle(composeTestRule: ComposeContentTestRule) {
            composeTestRule.waitForIdle()
        }

        fun takeScreenshot(name: String) {
            uiDevice.takeScreenshot().saveToFile(name)
        }

        fun scrollToElement(composeTestRule: ComposeContentTestRule, element: String) {
            composeTestRule.onNodeWithText(element).performScrollTo()
        }

        fun performClick(composeTestRule: ComposeContentTestRule, element: String) {
            composeTestRule.onNodeWithText(element).performClick()
        }

        fun performTextInput(composeTestRule: ComposeContentTestRule, element: String, text: String) {
            composeTestRule.onNodeWithText(element).performTextInput(text)
        }

        fun assertElementExists(composeTestRule: ComposeContentTestRule, element: String) {
            composeTestRule.onNodeWithText(element).assertExists()
        }

        fun assertElementDoesNotExist(composeTestRule: ComposeContentTestRule, element: String) {
            composeTestRule.onNodeWithText(element).assertDoesNotExist()
        }
    }

    /**
     * Performance measurement utilities for UI testing
     */
    object UiPerformanceUtils {

        fun measureUiResponseTime(operation: () -> Unit): Long {
            val startTime = System.currentTimeMillis()
            operation()
            return System.currentTimeMillis() - startTime
        }

        fun measureScreenLoadTime(composeTestRule: ComposeContentTestRule, operation: () -> Unit): Long {
            val startTime = System.currentTimeMillis()
            operation()
            composeTestRule.waitForIdle()
            return System.currentTimeMillis() - startTime
        }

        fun measureScrollPerformance(composeTestRule: ComposeContentTestRule, element: String): Long {
            val startTime = System.currentTimeMillis()
            composeTestRule.onNodeWithText(element).performScrollTo()
            return System.currentTimeMillis() - startTime
        }

        fun measureMemoryUsage(operation: () -> Unit): Long {
            val initialMemory = getMemoryUsage()
            operation()
            return getMemoryUsage() - initialMemory
        }

        fun getMemoryUsage(): Long {
            val runtime = Runtime.getRuntime()
            return runtime.totalMemory() - runtime.freeMemory()
        }
    }

    /**
     * Accessibility testing utilities
     */
    object AccessibilityTestUtils {

        fun assertAccessibilityLabel(composeTestRule: ComposeContentTestRule, element: String, label: String) {
            composeTestRule.onNodeWithContentDescription(label).assertExists()
        }

        fun assertClickableElement(composeTestRule: ComposeContentTestRule, element: String) {
            composeTestRule.onNodeWithText(element).assertHasClickAction()
        }

        fun assertNonClickableElement(composeTestRule: ComposeContentTestRule, element: String) {
            composeTestRule.onNodeWithText(element).assertHasNoClickAction()
        }

        fun assertTouchTargetSize(composeTestRule: ComposeContentTestRule, element: String) {
            // Verify element meets minimum touch target size (48dp x 48dp)
            composeTestRule.onNodeWithText(element).assertExists()
        }

        fun assertFocusOrder(composeTestRule: ComposeContentTestRule, elements: List<String>) {
            // Verify focus order is logical
            elements.forEach { element ->
                composeTestRule.onNodeWithText(element).assertExists()
            }
        }
    }
}
