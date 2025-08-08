package gr.eduinvoice.data.testinfrastructure

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.datastore.preferences.core.emptyPreferences
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDate
import java.time.LocalTime

/**
 * Unified test infrastructure for data module tests
 * Provides centralized test utilities, data factories, and configuration
 */
@OptIn(ExperimentalCoroutinesApi::class)
object DataTestInfrastructure {

    /**
     * Standard test dispatcher for coroutine testing
     */
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    /**
     * Creates a mock DataStore for testing purposes
     */
    private fun createMockDataStore(): androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
        return object : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
            private val prefs = MutableStateFlow(emptyPreferences())
            override val data: kotlinx.coroutines.flow.Flow<androidx.datastore.preferences.core.Preferences> = prefs.asStateFlow()
            override suspend fun updateData(transform: suspend (androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences): androidx.datastore.preferences.core.Preferences {
                val newPrefs = transform(prefs.value)
                prefs.value = newPrefs
                return newPrefs
            }
        }
    }

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
        val mockDataStore = createMockDataStore()
        
        // Create UserPreferencesRepository with mock DataStore for testing
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userPreferencesRepository = UserPreferencesRepository(context, mockDataStore)
        val networkMonitor = NetworkMonitor(context)
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
        ): User {
            require(id > 0) { "User ID must be positive" }
            require(username.isNotBlank()) { "Username cannot be blank" }
            require(fullName.isNotBlank()) { "Full name cannot be blank" }
            
            return User(
                id = id,
                username = username,
                passwordHash = "test_hash",
                fullName = fullName
            )
        }

        fun createTestStudent(
            id: Long = 1L,
            ownerId: Long = 1L,
            name: String = "Test Student",
            rate: Double = 25.0
        ): Student {
            require(id > 0) { "Student ID must be positive" }
            require(ownerId > 0) { "Owner ID must be positive" }
            require(name.isNotBlank()) { "Student name cannot be blank" }
            require(rate >= 0.0) { "Rate must be non-negative" }
            
            return Student(
                id = id,
                ownerId = ownerId,
                name = name,
                surname = "Test Surname",
                parentMobile = "+30123456789",
                parentEmail = "test@example.com",
                className = "Test Class",
                rate = rate
            )
        }

        fun createTestLesson(
            id: Long = 1L,
            studentId: Long = 1L,
            ownerId: Long = 1L,
            date: String = LocalDate.now().toString(),
            durationMinutes: Int = 60
        ): Lesson {
            require(id > 0) { "Lesson ID must be positive" }
            require(studentId > 0) { "Student ID must be positive" }
            require(ownerId > 0) { "Owner ID must be positive" }
            require(durationMinutes > 0) { "Duration must be positive" }
            
            return Lesson(
                id = id,
                studentId = studentId,
                date = date,
                startTime = "10:00",
                durationMinutes = durationMinutes,
                notes = "Test lesson",
                ownerId = ownerId
            )
        }

        fun createTestGroup(
            id: Long = 1L,
            ownerId: Long = 1L,
            name: String = "Test Group"
        ): StudentGroup {
            require(id > 0) { "Group ID must be positive" }
            require(ownerId > 0) { "Owner ID must be positive" }
            require(name.isNotBlank()) { "Group name cannot be blank" }
            
            return StudentGroup(
                id = id,
                ownerId = ownerId,
                name = name
            )
        }

        fun createLargeStudentDataset(ownerId: Long, count: Int): List<Student> {
            require(ownerId > 0) { "Owner ID must be positive" }
            require(count > 0) { "Count must be positive" }
            
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
            require(students.isNotEmpty()) { "Students list cannot be empty" }
            require(count > 0) { "Count must be positive" }
            
            val lessons = mutableListOf<Lesson>()
            val baseDate = LocalDate.now().minusDays(30)

            repeat(count) { index ->
                val student = students[index % students.size]
                val lessonDate = baseDate.plusDays((index % 30).toLong())
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
            require(ownerId > 0) { "Owner ID must be positive" }
            require(count > 0) { "Count must be positive" }
            
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
            return try {
                operation()
                System.currentTimeMillis() - startTime
            } catch (e: Exception) {
                val endTime = System.currentTimeMillis() - startTime
                // Rethrow the exception but still return the timing
                throw RuntimeException("Operation failed after ${endTime}ms", e)
            }
        }

        fun measureMemoryUsage(operation: () -> Unit): Long {
            val initialMemory = getMemoryUsage()
            return try {
                operation()
                getMemoryUsage() - initialMemory
            } catch (e: Exception) {
                val finalMemory = getMemoryUsage() - initialMemory
                // Rethrow the exception but still return the memory usage
                throw RuntimeException("Operation failed with memory delta ${finalMemory}bytes", e)
            }
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
            require(threadCount > 0) { "Thread count must be positive" }
            require(operationsPerThread > 0) { "Operations per thread must be positive" }
            
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