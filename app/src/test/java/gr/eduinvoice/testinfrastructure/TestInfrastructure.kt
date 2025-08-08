package gr.eduinvoice.testinfrastructure

import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.*
import gr.eduinvoice.data.repository.*
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.group.GroupUseCases
import gr.eduinvoice.domain.user.UserUseCases
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalTime

/**
 * Unified test infrastructure for all modules
 * Provides centralized test utilities, data factories, and configuration
 */
object TestInfrastructure {
    
    /**
     * Standard test dispatcher for coroutine testing
     */
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    
    /**
     * Creates a complete test environment with all repositories and use cases
     */
    fun createTestEnvironment(database: EduInvoiceDatabase): TestEnvironment {
        val studentRepository = StudentRepository(database.studentDao())
        // Use TutorBillingRepository instead of non-existent LessonRepository
        val lessonRepository = TutorBillingRepository(
            database.studentDao(),
            database.lessonDao(),
            database.groupDao(),
            gr.eduinvoice.data.concurrency.ConcurrencyController(
                gr.eduinvoice.data.concurrency.TransactionManager(database),
                gr.eduinvoice.data.concurrency.OperationQueueManager()
            )
        )
        val groupRepository = GroupRepository(database.groupDao())
        val userRepository = UserRepository(database.userDao())
        
        val studentUseCases = StudentUseCases(
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
        
        val lessonUseCases = LessonUseCases(
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
        
        val groupUseCases = GroupUseCases(
            insertGroup = gr.eduinvoice.domain.group.InsertGroup(groupRepository),
            updateGroup = gr.eduinvoice.domain.group.UpdateGroup(groupRepository),
            deleteGroup = gr.eduinvoice.domain.group.DeleteGroup(groupRepository),
            getAllGroups = gr.eduinvoice.domain.group.GetAllGroups(groupRepository),
            getGroupById = gr.eduinvoice.domain.group.GetGroupById(groupRepository),
            addStudentToGroup = gr.eduinvoice.domain.group.AddStudentToGroup(groupRepository),
            removeStudentFromGroup = gr.eduinvoice.domain.group.RemoveStudentFromGroup(groupRepository),
            getGroupStudents = gr.eduinvoice.domain.group.GetGroupStudents(groupRepository)
        )
        
        val userUseCases = UserUseCases(
            createUser = gr.eduinvoice.domain.user.CreateUser(userRepository),
            authenticateUser = gr.eduinvoice.domain.user.AuthenticateUser(userRepository),
            getUserProfile = gr.eduinvoice.domain.user.GetUserProfile(userRepository),
            updateUser = gr.eduinvoice.domain.user.UpdateUser(userRepository),
            resetPassword = gr.eduinvoice.domain.user.ResetPassword(userRepository)
        )
        
        return TestEnvironment(
            database = database,
            studentRepository = studentRepository,
            lessonRepository = lessonRepository,
            groupRepository = groupRepository,
            userRepository = userRepository,
            studentUseCases = studentUseCases,
            lessonUseCases = lessonUseCases,
            groupUseCases = groupUseCases,
            userUseCases = userUseCases
        )
    }
    
    /**
     * Test environment containing all repositories and use cases
     */
    data class TestEnvironment(
        val database: EduInvoiceDatabase,
        val studentRepository: StudentRepository,
        val lessonRepository: TutorBillingRepository,
        val groupRepository: GroupRepository,
        val userRepository: UserRepository,
        val studentUseCases: StudentUseCases,
        val lessonUseCases: LessonUseCases,
        val groupUseCases: GroupUseCases,
        val userUseCases: UserUseCases
    )
    
    /**
     * Common test data creation utilities
     */
    object TestDataFactory {
        
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
        
        fun createTestLessonWithStudent(
            student: Student,
            lessonId: Long = 1L,
            date: String = LocalDate.now().toString(),
            durationMinutes: Int = 60
        ): Lesson = Lesson.create(
            studentId = student.id,
            date = LocalDate.parse(date),
            startTime = LocalTime.of(10, 0),
            durationMinutes = durationMinutes,
            notes = "Test lesson",
            ownerId = student.ownerId
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
     * Test validation utilities
     */
    object TestValidation {
        
        fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
        
        fun isValidPhoneNumber(phone: String): Boolean {
            return phone.matches(Regex("^\\+?[1-9]\\d{1,14}$"))
        }
        
        fun isValidRate(rate: Double): Boolean {
            return rate > 0 && rate <= 1000
        }
        
        fun isValidName(name: String): Boolean {
            return name.isNotBlank() && name.length <= 50 && name.matches(Regex("^[a-zA-Z\\s]+$"))
        }
        
        fun isValidClassName(className: String): Boolean {
            return className.isNotBlank() && className.length <= 30
        }
        
        fun isValidDate(date: String): Boolean {
            return try {
                LocalDate.parse(date)
                true
            } catch (e: Exception) {
                false
            }
        }
        
        fun isValidTime(time: String): Boolean {
            return try {
                LocalTime.parse(time)
                true
            } catch (e: Exception) {
                false
            }
        }
        
        fun isValidDuration(duration: Int): Boolean {
            return duration > 0 && duration <= 480 // Max 8 hours
        }
        
        fun isValidNotes(notes: String): Boolean {
            return notes.length <= 500
        }
    }
    
    /**
     * Performance measurement utilities
     */
    object PerformanceUtils {
        
        fun measureTime(operation: () -> Unit): Long {
            val startTime = System.currentTimeMillis()
            operation()
            return System.currentTimeMillis() - startTime
        }
        
        fun getMemoryUsage(): Long {
            val runtime = Runtime.getRuntime()
            return runtime.totalMemory() - runtime.freeMemory()
        }
        
        fun measureMemoryUsage(operation: () -> Unit): Long {
            val initialMemory = getMemoryUsage()
            operation()
            return getMemoryUsage() - initialMemory
        }
    }
    
    /**
     * Test data manager for managing test state
     */
    object TestDataManager {
            private val studentFlow: MutableStateFlow<List<Student>> = MutableStateFlow(emptyList())
    private val lessonFlow: MutableStateFlow<List<Lesson>> = MutableStateFlow(emptyList())
    private val lessonWithStudentFlow: MutableStateFlow<List<gr.eduinvoice.data.database.LessonWithStudent>> = MutableStateFlow(emptyList())
    private val groupFlow: MutableStateFlow<List<StudentGroup>> = MutableStateFlow(emptyList())
        
        fun getGroupStudentRelations(): List<GroupStudentCrossRef> {
            return emptyList() // Implement as needed
        }
        
        fun getStudentFlow(): StateFlow<List<Student>> = studentFlow.asStateFlow()
        fun getLessonFlow(): StateFlow<List<Lesson>> = lessonFlow.asStateFlow()
        fun getLessonWithStudentFlow(): StateFlow<List<gr.eduinvoice.data.database.LessonWithStudent>> = lessonWithStudentFlow.asStateFlow()
        fun getGroupFlow(): StateFlow<List<StudentGroup>> = groupFlow.asStateFlow()
    }
} 