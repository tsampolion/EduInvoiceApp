package gr.eduinvoice.data.testinfrastructure

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.*
import gr.eduinvoice.data.repository.*
import gr.eduinvoice.data.dao.*
import gr.eduinvoice.data.concurrency.*
import gr.eduinvoice.data.user.*
import gr.eduinvoice.data.utils.*
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.datastore.preferences.core.emptyPreferences
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalTime

/**
 * Unified test infrastructure for data module tests
 * Provides centralized test utilities, data factories, and configuration
 */
object DataTestInfrastructure {

    /**
     * Standard test dispatcher for coroutine testing
     */
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    /**
     * Creates a complete test environment with all data layer components
     */
    fun createDataTestEnvironment(database: EduInvoiceDatabase): DataTestEnvironment {
        val studentDao = database.studentDao()
        val lessonDao = database.lessonDao()
        val groupDao = database.groupDao()
        val userDao = database.userDao()

        val studentRepository = StudentRepository(studentDao)
        val groupRepository = GroupRepository(groupDao)
        val userRepository = UserRepository(userDao)

        val transactionManager = TransactionManager(database)
        val operationQueueManager = OperationQueueManager()
        val concurrencyController = ConcurrencyController(transactionManager, operationQueueManager)

        // Create mock DataStore for testing
        val mockDataStore = object : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
            private val prefs = MutableStateFlow(emptyPreferences())
            override val data: kotlinx.coroutines.flow.Flow<androidx.datastore.preferences.core.Preferences> = prefs.asStateFlow()
            override suspend fun updateData(transform: suspend (androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences): androidx.datastore.preferences.core.Preferences {
                val newPrefs = transform(prefs.value)
                prefs.value = newPrefs
                return newPrefs
            }
        }
        
        // Create UserPreferencesRepository with mock DataStore for testing
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userPreferencesRepository = UserPreferencesRepository(context, mockDataStore)
        val networkMonitor = NetworkMonitor()
        val exponentialBackoff = ExponentialBackoff()

        return DataTestEnvironment(
            database = database,
            studentDao = studentDao,
            lessonDao = lessonDao,
            groupDao = groupDao,
            userDao = userDao,
            studentRepository = studentRepository,
            groupRepository = groupRepository,
            userRepository = userRepository,
            concurrencyController = concurrencyController,
            transactionManager = transactionManager,
            operationQueueManager = operationQueueManager,
            userPreferencesRepository = userPreferencesRepository,
            networkMonitor = networkMonitor,
            exponentialBackoff = exponentialBackoff
        )
    }

    /**
     * Data test environment containing all data layer components
     */
    data class DataTestEnvironment(
        val database: EduInvoiceDatabase,
        val studentDao: StudentDao,
        val lessonDao: LessonDao,
        val groupDao: GroupDao,
        val userDao: UserDao,
        val studentRepository: StudentRepository,
        val groupRepository: GroupRepository,
        val userRepository: UserRepository,
        val concurrencyController: ConcurrencyController,
        val transactionManager: TransactionManager,
        val operationQueueManager: OperationQueueManager,
        val userPreferencesRepository: UserPreferencesRepository,
        val networkMonitor: NetworkMonitor,
        val exponentialBackoff: ExponentialBackoff
    )

    /**
     * Common test data creation utilities for data layer
     */
    object DataTestDataFactory {

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
        ): Lesson = Lesson(
            id = id,
            studentId = studentId,
            date = date,
            startTime = "10:00",
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

        // Uncomment and fix the createLargeLessonDataset function
        fun createLargeLessonDataset(students: List<Student>, count: Int): List<Lesson> {
            val lessons = mutableListOf<Lesson>()
            val baseDate = LocalDate.now().minusDays(30)

            repeat(count) { index ->
                val student = students[index % students.size]
                val lessonDate = baseDate.plusDays(index % 30)
                val startTime = LocalTime.of(9 + (index % 8), 0)

                val lesson = Lesson(
                    id = index.toLong(),
                    studentId = student.id,
                    date = lessonDate.toString(),
                    startTime = startTime.toString(),
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
     * Data layer specific validation utilities
     */
    object DataTestValidation {

        fun isValidDatabaseOperation(operation: () -> Unit): Boolean {
            return try {
                operation()
                true
            } catch (e: Exception) {
                false
            }
        }

        fun isValidTransaction(transaction: () -> Unit): Boolean {
            return try {
                transaction()
                true
            } catch (e: Exception) {
                false
            }
        }

        fun isValidConcurrencyOperation(operation: () -> Unit): Boolean {
            return try {
                operation()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Data layer performance measurement utilities
     */
    object DataPerformanceUtils {

        fun measureDatabaseOperationTime(operation: () -> Unit): Long {
            val startTime = System.currentTimeMillis()
            operation()
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

        fun measureConcurrencyPerformance(
            threadCount: Int,
            operationsPerThread: Int,
            operation: (Int) -> Unit
        ): Long {
            val startTime = System.currentTimeMillis()
            
            val threads = (0 until threadCount).map { threadIndex ->
                Thread {
                    repeat(operationsPerThread) { operationIndex ->
                        operation(threadIndex * operationsPerThread + operationIndex)
                    }
                }
            }

            threads.forEach { it.start() }
            threads.forEach { it.join() }

            return System.currentTimeMillis() - startTime
        }
    }
}
