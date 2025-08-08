package gr.eduinvoice.domain.testinfrastructure

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
 * Test database container for domain module tests
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
        
        return try {
            Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
                .fallbackToDestructiveMigration()
                .build()
                .also { database = it }
        } catch (e: Exception) {
            throw DatabaseInitException("Failed to create test database: ${e.message}", e)
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
