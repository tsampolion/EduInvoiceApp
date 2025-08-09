package gr.eduinvoice.data.testinfrastructure

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.database.DatabaseInitException
import gr.eduinvoice.data.testfixtures.TestDbFactory
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.model.GroupStudentCrossRef
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.time.LocalDateTime
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

/**
 * Enhanced test database container that provides isolated database instances for testing.
 * Fixes SQLCipher test issues by creating separate database files for each test.
 * Provides comprehensive test data population for integration tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestDatabaseContainer : TestWatcher() {
    
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    private val databaseCounter = AtomicInteger(0)
    private var database: EduInvoiceDatabase? = null
    private var databaseFile: File? = null
    
    /**
     * Creates a test database instance with proper isolation
     */
    fun createTestDatabase(): EduInvoiceDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testDbName = "test_database_${databaseCounter.getAndIncrement()}"
        
        // Create database file in test directory
        databaseFile = context.getDir("test_databases", Context.MODE_PRIVATE)
            .resolve("$testDbName.db")
        
        return try {
            TestDbFactory.createInMemory(context, testDbName, true)
                .also { database = it }
        } catch (e: Exception) {
            throw DatabaseInitException("Failed to create test database: ${e.message}", e)
        }
    }
    
    /**
     * Creates a test database with custom configuration
     */
    fun createTestDatabaseWithConfig(
        name: String? = null,
        destructiveMigration: Boolean = true
    ): EduInvoiceDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = name ?: "test_database_${databaseCounter.getAndIncrement()}"
        
        return try {
            TestDbFactory.createInMemory(context, dbName, destructiveMigration)
                .also { database = it }
        } catch (e: Exception) {
            throw DatabaseInitException("Failed to create test database with config: ${e.message}", e)
        }
    }
    
    /**
     * Creates a mock UserPreferencesRepository for testing
     */
    fun createMockUserPreferences(): UserPreferencesRepository {
        return mockk<UserPreferencesRepository>(relaxed = true)
    }
    
    /**
     * Cleans up the test database
     */
    fun cleanupTestDatabase() {
        try {
            database?.close()
            database = null
            databaseFile?.delete()
            databaseFile = null
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Populates database with test data
     */
    suspend fun populateTestData(database: EduInvoiceDatabase) {
        val userDao = database.userDao()
        val studentDao = database.studentDao()
        val lessonDao = database.lessonDao()
        val groupDao = database.groupDao()
        
        // Create test user
        val userId = userDao.insert(
            User(
                username = "testuser",
                passwordHash = "test_hash",
                fullName = "Test User",
                subjectSpecialty = "Math",
                yearsExperience = 5
            )
        )
        
        // Create test students
        val student1Id = studentDao.insert(
            Student(
                name = "Alice",
                surname = "Johnson",
                parentMobile = "1234567890",
                className = "Math A",
                rate = 25.0,
                ownerId = userId
            )
        )
        
        val student2Id = studentDao.insert(
            Student(
                name = "Bob",
                surname = "Smith",
                parentMobile = "0987654321",
                className = "Math B",
                rate = 30.0,
                ownerId = userId
            )
        )
        
        // Create test group
        val groupId = groupDao.insertGroup(
            StudentGroup(
                name = "Advanced Math",
                ownerId = userId
            )
        )
        
        // Add students to group
        groupDao.insertCrossRef(
            GroupStudentCrossRef(
                groupId = groupId,
                studentId = student1Id,
                ownerId = userId
            )
        )
        
        groupDao.insertCrossRef(
            GroupStudentCrossRef(
                groupId = groupId,
                studentId = student2Id,
                ownerId = userId
            )
        )
        
        // Create test lessons
        lessonDao.insert(
            Lesson(
                studentId = student1Id,
                date = "2024-01-15",
                startTime = "10:00",
                durationMinutes = 60,
                ownerId = userId
            )
        )
        
        lessonDao.insert(
            Lesson(
                studentId = student2Id,
                date = "2024-01-16",
                startTime = "14:00",
                durationMinutes = 90,
                ownerId = userId
            )
        )
    }
    
    override fun starting(description: Description?) {
        super.starting(description)
        // Note: setMain and resetMain are not available in this context
        // This is a simplified version for testing
    }
    
    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestDatabase()
        // Note: setMain and resetMain are not available in this context
        // This is a simplified version for testing
    }
}
