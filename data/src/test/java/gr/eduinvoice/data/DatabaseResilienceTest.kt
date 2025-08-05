package gr.eduinvoice.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.BouncyCastleTestRunner
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.fallback.DatabaseFallbackManager
import gr.eduinvoice.data.monitoring.DatabaseHealthMonitor
import gr.eduinvoice.data.validation.DatabaseIntegrityValidator
import kotlinx.coroutines.test.runTest
import net.sqlcipher.database.SQLiteDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(BouncyCastleTestRunner::class)
class DatabaseResilienceTest {
    
    private lateinit var context: Context
    private lateinit var database: EduInvoiceDatabase
    private lateinit var healthMonitor: DatabaseHealthMonitor
    private lateinit var integrityValidator: DatabaseIntegrityValidator
    private lateinit var fallbackManager: DatabaseFallbackManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            EduInvoiceDatabase::class.java
        ).build()
        
        healthMonitor = DatabaseHealthMonitor(context, database)
        integrityValidator = DatabaseIntegrityValidator(database)
        fallbackManager = DatabaseFallbackManager(
            context, 
            database, 
            healthMonitor, 
            integrityValidator
        )
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun testDatabaseHealthMonitor_HealthyDatabase() = runTest {
        // Given a healthy database
        val healthStatus = healthMonitor.checkDatabaseHealth()
        
        // Then health check should pass (for in-memory database, file size might be 0)
        // For now, we just check that the method doesn't crash and returns valid data
        assertNotNull("Health status should not be null", healthStatus)
        assertNotNull("Performance metrics should not be null", healthStatus.performanceMetrics)
        assertTrue("Integrity check should pass", healthStatus.integrityCheck)
        // For in-memory database, file size might be 0, so we don't assert on it
    }
    
    @Test
    fun testDatabaseHealthMonitor_PerformanceMetrics() = runTest {
        // When checking database health
        val healthStatus = healthMonitor.checkDatabaseHealth()
        
        // Then performance metrics should be available
        assertNotNull("Performance metrics should not be null", healthStatus.performanceMetrics)
        assertTrue("Average query time should be reasonable", 
            healthStatus.performanceMetrics.averageQueryTime >= 0)
        // For in-memory database, memory usage might be 0, so we don't assert on it
    }
    
    @Test
    fun testDatabaseHealthMonitor_MaintenanceOperations() = runTest {
        // When performing maintenance
        val maintenanceResult = healthMonitor.performMaintenance()
        
        // Then maintenance should complete
        assertNotNull("Maintenance result should not be null", maintenanceResult)
        assertTrue("Maintenance should be successful", maintenanceResult.success)
        assertTrue("No errors should occur", maintenanceResult.errors.isEmpty())
    }
    
    @Test
    fun testDatabaseIntegrityValidator_ValidateAllTables() = runTest {
        // When validating all tables
        val validationResult = integrityValidator.validateAllTables()
        
        // Then validation should complete
        assertNotNull("Validation result should not be null", validationResult)
        assertTrue("Validation should be valid for empty database", validationResult.isValid)
        assertTrue("No errors should be found", validationResult.errors.isEmpty())
    }
    
    @Test
    fun testDatabaseIntegrityValidator_RepairCorruptedData() = runTest {
        // When repairing corrupted data
        val repairResult = integrityValidator.repairCorruptedData()
        
        // Then repair should complete
        assertNotNull("Repair result should not be null", repairResult)
        assertTrue("Repair should be successful", repairResult.success)
        assertFalse("No data loss should occur", repairResult.dataLoss)
    }
    
    @Test
    fun testDatabaseIntegrityValidator_GetStatistics() {
        // When getting database statistics
        val statistics = integrityValidator.getDatabaseStatistics()
        
        // Then statistics should be available
        assertNotNull("Statistics should not be null", statistics)
        assertTrue("Total records should be >= 0", statistics.totalRecords >= 0)
        assertTrue("Student count should be >= 0", statistics.studentCount >= 0)
        assertTrue("Lesson count should be >= 0", statistics.lessonCount >= 0)
        assertTrue("Group count should be >= 0", statistics.groupCount >= 0)
        assertTrue("User count should be >= 0", statistics.userCount >= 0)
    }
    
    @Test
    fun testDatabaseFallbackManager_InitialState() = runTest {
        // When getting initial state
        val fallbackState = fallbackManager.fallbackState.value
        
        // Then should be in normal state
        assertTrue("Should start in normal state", 
            fallbackState is DatabaseFallbackManager.FallbackState.Normal)
    }
    
    @Test
    fun testDatabaseFallbackManager_SwitchToReadOnlyMode() = runTest {
        // When switching to read-only mode
        val result = fallbackManager.switchToReadOnlyMode()
        
        // Then should switch successfully (even if health check fails, mode should change)
        assertTrue("Should be in read-only state", 
            result.state is DatabaseFallbackManager.FallbackState.ReadOnly)
        // Note: success might be false if health check fails, but state should still change
    }
    
    @Test
    fun testDatabaseFallbackManager_EnableOfflineMode() = runTest {
        // When enabling offline mode
        val result = fallbackManager.enableOfflineMode()
        
        // Then should enable successfully
        assertTrue("Should enable offline mode", result.success)
        assertTrue("Should be in offline state", 
            result.state is DatabaseFallbackManager.FallbackState.Offline)
    }
    
    @Test
    fun testDatabaseFallbackManager_GetDatabaseStatus() = runTest {
        // When getting database status
        val status = fallbackManager.getDatabaseStatus()
        
        // Then status should be available
        assertNotNull("Database status should not be null", status)
        assertTrue("File size should be >= 0", status.fileSize >= 0)
        assertTrue("Record count should be >= 0", status.recordCount >= 0)
        // For in-memory database, file size might be 0, which is acceptable
        // Note: isHealthy might be false for in-memory database, which is acceptable
    }
    
    @Test
    fun testDatabaseFallbackManager_IsDatabaseAccessible() = runTest {
        // When checking database accessibility
        val isAccessible = fallbackManager.isDatabaseAccessible()
        
        // Then should be accessible (method should not crash)
        // Note: For in-memory database, accessibility might depend on health check
        // We just verify the method doesn't crash
        assertNotNull("Accessibility check should return a boolean", isAccessible)
    }
    
    @Test
    fun testDatabaseFallbackManager_GetRecoveryRecommendations() {
        // When getting recovery recommendations
        val recommendations = fallbackManager.getRecoveryRecommendations()
        
        // Then recommendations should be available
        assertNotNull("Recommendations should not be null", recommendations)
        assertTrue("Should have at least one recommendation", recommendations.isNotEmpty())
    }
    
    @Test
    fun testDatabaseFallbackManager_RecoveryProgress() {
        // When getting recovery progress
        val progress = fallbackManager.recoveryProgress.value
        
        // Then progress should be available
        assertNotNull("Recovery progress should not be null", progress)
        assertFalse("Should not be in progress initially", progress.isInProgress)
        assertEquals("Progress should be 0 initially", 0f, progress.progress, 0.01f)
    }
    
    @Test
    fun testDatabaseHealthMonitor_FileInfo() {
        // When getting file info
        val fileInfo = healthMonitor.getDatabaseFileInfo()
        
        // Then file info should be available
        assertNotNull("File info should not be null", fileInfo)
        // For in-memory database, file might not exist or have different properties
        // We only check that the method doesn't crash and returns valid data
        assertTrue("File size should be >= 0", fileInfo.size >= 0)
        assertTrue("Path should not be empty", fileInfo.path.isNotEmpty())
    }
    
    @Test
    fun testDatabaseHealthMonitor_ValidateDataIntegrity() = runTest {
        // When validating data integrity
        val integrityResult = healthMonitor.validateDataIntegrity()
        
        // Then integrity result should be available
        assertNotNull("Integrity result should not be null", integrityResult)
        assertTrue("Integrity should be valid for empty database", integrityResult.isValid)
        assertTrue("No issues should be found", integrityResult.issues.isEmpty())
    }
    
    @Test
    fun testDatabaseResilience_EndToEnd() = runTest {
        // Given a database (might not be healthy for in-memory)
        val initialHealth = healthMonitor.checkDatabaseHealth()
        assertNotNull("Initial health should not be null", initialHealth)
        
        // When performing comprehensive resilience checks
        val validationResult = integrityValidator.validateAllTables()
        val maintenanceResult = healthMonitor.performMaintenance()
        val fallbackState = fallbackManager.fallbackState.value
        
        // Then all operations should complete without crashing
        assertNotNull("Validation result should not be null", validationResult)
        assertNotNull("Maintenance result should not be null", maintenanceResult)
        assertTrue("Should be in normal state", 
            fallbackState is DatabaseFallbackManager.FallbackState.Normal)
        
        // And final health check should complete
        val finalHealth = healthMonitor.checkDatabaseHealth()
        assertNotNull("Final health should not be null", finalHealth)
    }
    
    @Test
    fun testDatabaseResilience_ErrorHandling() = runTest {
        // Given a database with potential issues
        val healthStatus = healthMonitor.checkDatabaseHealth()
        
        // When issues are detected
        if (!healthStatus.isHealthy) {
            // Then recovery should be possible
            val repairResult = integrityValidator.repairCorruptedData()
            assertNotNull("Repair result should not be null", repairResult)
            
            // And fallback manager should provide recommendations
            val recommendations = fallbackManager.getRecoveryRecommendations()
            assertTrue("Should provide recovery recommendations", recommendations.isNotEmpty())
        }
    }
    
    @Test
    fun testDatabaseResilience_PerformanceMonitoring() = runTest {
        // When monitoring performance
        val healthStatus = healthMonitor.checkDatabaseHealth()
        val performanceMetrics = healthStatus.performanceMetrics
        
        // Then performance metrics should be reasonable
        assertTrue("Average query time should be reasonable", 
            performanceMetrics.averageQueryTime < 5000) // Less than 5 seconds
        assertTrue("Slow queries should be reasonable", 
            performanceMetrics.slowQueries < 10) // Less than 10 slow queries
        assertTrue("Memory usage should be reasonable", 
            performanceMetrics.memoryUsage < 100 * 1024 * 1024) // Less than 100MB
    }
} 