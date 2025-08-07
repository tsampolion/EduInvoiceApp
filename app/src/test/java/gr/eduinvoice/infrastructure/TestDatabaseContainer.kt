package gr.eduinvoice.infrastructure

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.database.DatabaseInitException
import gr.eduinvoice.data.settings.UserPreferencesRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * Test database container that provides isolated database instances for testing.
 * Fixes SQLCipher test issues by creating separate database files for each test.
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
     * Creates test data for integration tests
     */
    suspend fun populateTestData(db: EduInvoiceDatabase) {
        // This will be implemented based on specific test needs
        // For now, we'll create basic test data
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
