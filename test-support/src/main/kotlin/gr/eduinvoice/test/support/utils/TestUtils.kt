package gr.eduinvoice.test.support.utils

import android.content.Context
import androidx.room.Room
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.test.support.fakes.NoopConcurrencyController
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.group.GroupUseCases
import gr.eduinvoice.domain.user.UserUseCases
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.student.*
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.domain.user.*
import gr.eduinvoice.domain.lesson.*
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Common test utilities and setup functions
 */
object TestUtils {
    
    /**
     * Standard test dispatcher for coroutine testing
     */
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    
    /**
     * Creates an in-memory test database
     * Note: This requires a Context to be provided by the test
     */
    fun createTestDatabase(context: Context): EduInvoiceDatabase {
        return Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
    
    /**
     * Creates a complete test environment with all repositories and use cases
     */
    fun createTestEnvironment(database: EduInvoiceDatabase): TestEnvironment {
        val studentRepository = StudentRepository(database.studentDao())
        val groupRepository = GroupRepository(database.groupDao())
        val userRepository = UserRepository(database.userDao())
        
        val noopConcurrencyController = NoopConcurrencyController.create()
        val lessonRepository = TutorBillingRepository(
            database.studentDao(),
            database.lessonDao(),
            database.groupDao(),
            noopConcurrencyController
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
        
        return TestEnvironment(
            database = database,
            studentRepository = studentRepository,
            groupRepository = groupRepository,
            userRepository = userRepository,
            lessonRepository = lessonRepository,
            studentUseCases = studentUseCases,
            groupUseCases = groupUseCases,
            userUseCases = userUseCases,
            lessonUseCases = lessonUseCases,
            concurrencyController = noopConcurrencyController
        )
    }
    
    /**
     * Test environment containing all repositories and use cases
     */
    data class TestEnvironment(
        val database: EduInvoiceDatabase,
        val studentRepository: StudentRepository,
        val groupRepository: GroupRepository,
        val userRepository: UserRepository,
        val lessonRepository: TutorBillingRepository,
        val studentUseCases: StudentUseCases,
        val groupUseCases: GroupUseCases,
        val userUseCases: UserUseCases,
        val lessonUseCases: LessonUseCases,
        val concurrencyController: NoopConcurrencyController
    ) {
        /**
         * Closes the database and cleans up resources
         */
        fun cleanup() {
            database.close()
        }
    }
    
    /**
     * Runs a test with automatic cleanup
     */
    suspend fun <T> runTestWithCleanup(context: Context, block: suspend TestEnvironment.() -> T): T {
        val database = createTestDatabase(context)
        val environment = createTestEnvironment(database)
        
        try {
            return environment.block()
        } finally {
            environment.cleanup()
        }
    }
}
