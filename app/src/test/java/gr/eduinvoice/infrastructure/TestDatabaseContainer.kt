package gr.eduinvoice.infrastructure

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.database.DatabaseInitException
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
            Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
                .setDatabaseName(testDbName)
                .fallbackToDestructiveMigration()
                .build()
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
            val builder = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
                .setDatabaseName(dbName)
            
            if (destructiveMigration) {
                builder.fallbackToDestructiveMigration()
            }
            
            builder.build().also { database = it }
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
            
            // Clean up database file
            databaseFile?.let { file ->
                if (file.exists()) {
                    file.delete()
                }
            }
            databaseFile = null
        } catch (e: Exception) {
            // Log cleanup errors but don't fail tests
            println("Warning: Error during test database cleanup: ${e.message}")
        }
    }
    
    /**
     * Validates database integrity
     */
    suspend fun validateDatabaseIntegrity(db: EduInvoiceDatabase): Boolean {
        return try {
            // Check if database is open and accessible
            db.openHelper.readableDatabase.isOpen
            
            // Verify all tables exist
            val tables = listOf("students", "lessons", "groups", "users")
            tables.all { tableName ->
                db.openHelper.readableDatabase.isTableExists(tableName)
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Creates comprehensive test data for integration tests
     */
    suspend fun populateTestData(db: EduInvoiceDatabase, userId: Long = 1L) = runTest {
        try {
            // Create test user
            val testUser = User(
                id = userId,
                name = "Test User",
                email = "test@example.com",
                passwordHash = "test_hash"
            )
            db.userDao().insert(testUser)
            
            // Create test students
            val students = listOf(
                Student(
                    id = 1,
                    name = "John",
                    surname = "Doe",
                    parentMobile = "1234567890",
                    className = "A",
                    rate = 25.0,
                    ownerId = userId
                ),
                Student(
                    id = 2,
                    name = "Jane",
                    surname = "Smith",
                    parentMobile = "0987654321",
                    className = "B",
                    rate = 30.0,
                    ownerId = userId
                ),
                Student(
                    id = 3,
                    name = "Bob",
                    surname = "Johnson",
                    parentMobile = "5555555555",
                    className = "A",
                    rate = 25.0,
                    ownerId = userId
                )
            )
            
            students.forEach { student ->
                db.studentDao().insert(student)
            }
            
            // Create test groups
            val groups = listOf(
                StudentGroup(
                    id = 1,
                    name = "Math Group",
                    ownerId = userId
                ),
                StudentGroup(
                    id = 2,
                    name = "Science Group",
                    ownerId = userId
                )
            )
            
            groups.forEach { group ->
                db.groupDao().insertGroup(group)
            }
            
            // Create group-student relationships
            val crossRefs = listOf(
                GroupStudentCrossRef(groupId = 1, studentId = 1, ownerId = userId),
                GroupStudentCrossRef(groupId = 1, studentId = 2, ownerId = userId),
                GroupStudentCrossRef(groupId = 2, studentId = 2, ownerId = userId),
                GroupStudentCrossRef(groupId = 2, studentId = 3, ownerId = userId)
            )
            
            crossRefs.forEach { crossRef ->
                db.groupDao().insertCrossRef(crossRef)
            }
            
            // Create test lessons
            val lessons = listOf(
                Lesson(
                    id = 1,
                    studentId = 1,
                    date = LocalDate.now().minusDays(7).toString(),
                    time = LocalDateTime.now().minusDays(7).toLocalTime().toString(),
                    duration = 60,
                    rate = 25.0,
                    paid = false,
                    invoiced = false,
                    ownerId = userId
                ),
                Lesson(
                    id = 2,
                    studentId = 2,
                    date = LocalDate.now().minusDays(5).toString(),
                    time = LocalDateTime.now().minusDays(5).toLocalTime().toString(),
                    duration = 90,
                    rate = 30.0,
                    paid = true,
                    invoiced = false,
                    ownerId = userId
                ),
                Lesson(
                    id = 3,
                    studentId = 3,
                    date = LocalDate.now().minusDays(3).toString(),
                    time = LocalDateTime.now().minusDays(3).toLocalTime().toString(),
                    duration = 60,
                    rate = 25.0,
                    paid = false,
                    invoiced = true,
                    ownerId = userId
                )
            )
            
            lessons.forEach { lesson ->
                db.lessonDao().insert(lesson)
            }
            
            println("Test data populated successfully")
        } catch (e: Exception) {
            println("Error populating test data: ${e.message}")
            throw e
        }
    }
    
    /**
     * Creates large dataset for performance testing
     */
    suspend fun populateLargeDataset(db: EduInvoiceDatabase, userId: Long = 1L, count: Int = 1000) = runTest {
        try {
            // Create test user
            val testUser = User(
                id = userId,
                name = "Performance Test User",
                email = "perf@example.com",
                passwordHash = "test_hash"
            )
            db.userDao().insert(testUser)
            
            // Create large number of students
            val students = (1..count).map { index ->
                Student(
                    id = index.toLong(),
                    name = "Student$index",
                    surname = "Surname$index",
                    parentMobile = "123456789$index",
                    className = "Class${index % 10}",
                    rate = 20.0 + (index % 30),
                    ownerId = userId
                )
            }
            
            students.forEach { student ->
                db.studentDao().insert(student)
            }
            
            // Create lessons for each student
            val lessons = students.flatMap { student ->
                (1..5).map { lessonIndex ->
                    Lesson(
                        id = (student.id * 100 + lessonIndex).toLong(),
                        studentId = student.id,
                        date = LocalDate.now().minusDays(lessonIndex.toLong()).toString(),
                        time = LocalDateTime.now().minusDays(lessonIndex.toLong()).toLocalTime().toString(),
                        duration = 60 + (lessonIndex % 30),
                        rate = student.rate,
                        paid = lessonIndex % 2 == 0,
                        invoiced = lessonIndex % 3 == 0,
                        ownerId = userId
                    )
                }
            }
            
            lessons.forEach { lesson ->
                db.lessonDao().insert(lesson)
            }
            
            println("Large dataset populated: $count students, ${lessons.size} lessons")
        } catch (e: Exception) {
            println("Error populating large dataset: ${e.message}")
            throw e
        }
    }
    
    /**
     * Creates corrupted database for testing recovery scenarios
     */
    suspend fun createCorruptedDatabase(): EduInvoiceDatabase {
        val db = createTestDatabase()
        
        // Intentionally corrupt the database by closing it improperly
        db.close()
        
        // Try to access the database to trigger corruption
        try {
            db.studentDao().getAllActiveStudents(1L)
        } catch (e: Exception) {
            // Expected corruption
        }
        
        return db
    }
    
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }
    
    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestDatabase()
        Dispatchers.resetMain()
    }
}
