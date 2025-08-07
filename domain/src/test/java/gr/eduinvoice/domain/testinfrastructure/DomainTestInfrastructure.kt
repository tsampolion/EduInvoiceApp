package gr.eduinvoice.domain.testinfrastructure

import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.*
import gr.eduinvoice.data.repository.*
import gr.eduinvoice.domain.student.*
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.domain.user.*
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.time.LocalDate
import java.time.LocalTime

/**
 * Unified test infrastructure for domain module tests
 * Provides centralized test utilities, data factories, and configuration
 */
object DomainTestInfrastructure {

    /**
     * Standard test dispatcher for coroutine testing
     */
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    /**
     * Creates a complete test environment with all domain layer components
     */
    fun createDomainTestEnvironment(database: EduInvoiceDatabase): DomainTestEnvironment {
        val studentRepository = StudentRepository(database.studentDao())
        val lessonRepository = LessonRepository(database.lessonDao())
        val groupRepository = GroupRepository(database.groupDao())
        val userRepository = UserRepository(database.userDao())

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

        val lessonUseCases = LessonUseCases(
            getAllLessons = GetAllLessons(database.lessonDao()),
            getLessonById = GetLessonById(database.lessonDao()),
            getStudentLessons = GetStudentLessons(lessonRepository),
            getLessonsWithStudents = GetLessonsWithStudents(database.lessonDao()),
            getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(database.lessonDao()),
            addLesson = AddLesson(lessonRepository),
            addGroupLesson = AddGroupLesson(lessonRepository),
            updateLesson = UpdateLesson(lessonRepository),
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

        return DomainTestEnvironment(
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
     * Domain test environment containing all domain layer components
     */
    data class DomainTestEnvironment(
        val database: EduInvoiceDatabase,
        val studentRepository: StudentRepository,
        val lessonRepository: LessonRepository,
        val groupRepository: GroupRepository,
        val userRepository: UserRepository,
        val studentUseCases: StudentUseCases,
        val lessonUseCases: LessonUseCases,
        val groupUseCases: GroupUseCases,
        val userUseCases: UserUseCases
    )

    /**
     * Common test data creation utilities for domain layer
     */
    object DomainTestDataFactory {

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
     * Domain layer specific validation utilities
     */
    object DomainTestValidation {

        fun isValidBusinessRule(rule: () -> Boolean): Boolean {
            return try {
                rule()
            } catch (e: Exception) {
                false
            }
        }

        fun isValidUseCaseExecution(execution: () -> Unit): Boolean {
            return try {
                execution()
                true
            } catch (e: Exception) {
                false
            }
        }

        fun isValidDomainOperation(operation: () -> Unit): Boolean {
            return try {
                operation()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Domain layer performance measurement utilities
     */
    object DomainPerformanceUtils {

        fun measureUseCaseExecutionTime(execution: () -> Unit): Long {
            val startTime = System.currentTimeMillis()
            execution()
            return System.currentTimeMillis() - startTime
        }

        fun measureBusinessLogicPerformance(logic: () -> Unit): Long {
            val startTime = System.currentTimeMillis()
            logic()
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
}
