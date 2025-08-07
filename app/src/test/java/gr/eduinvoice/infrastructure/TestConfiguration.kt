package gr.eduinvoice.infrastructure

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
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
 * Comprehensive test configuration for all testing scenarios.
 * Provides configuration for unit tests, integration tests, performance tests, and UI tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestConfiguration : TestWatcher() {
    
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
            androidx.room.Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
                .setDatabaseName(testDbName)
                .fallbackToDestructiveMigration()
                .build()
                .also { database = it }
        } catch (e: Exception) {
            throw gr.eduinvoice.data.database.DatabaseInitException("Failed to create test database: ${e.message}", e)
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
    
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }
    
    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestDatabase()
        Dispatchers.resetMain()
    }
    
    /**
     * Configuration for database testing
     */
    object Database {
        const val pageSize = 50
        const val maxPageSize = 100
        const val defaultTimeout = 5000L
        const val longTimeout = 30000L
        const val maxRetries = 3
        const val retryDelay = 100L
    }
    
    /**
     * Configuration for performance testing
     */
    object Performance {
        const val maxInsertionTime = 10000L // 10 seconds
        const val maxQueryTime = 2000L // 2 seconds
        const val maxMemoryUsage = 100 * 1024 * 1024L // 100MB
        const val maxConcurrentTime = 30000L // 30 seconds
        const val maxPaginationTime = 5000L // 5 seconds
        const val minCleanupEfficiency = 0.7 // 70%
        const val concurrentOperations = 10
        const val maxResponseTime = 1000L // 1 second
        const val responsivenessOperations = 100
    }
    
    /**
     * Configuration for stress testing
     */
    object Stress {
        const val concurrentThreads = 10
        const val operationsPerThread = 20
        const val maxExecutionTime = 60000L // 60 seconds
        const val maxErrorRate = 0.1 // 10%
        const val minSuccessRate = 0.8 // 80%
        const val memoryPressureStudentCount = 1000
        const val memoryPressureLessonCount = 5000
        const val memoryPressureGroupCount = 100
        const val maxMemoryIncrease = 200 * 1024 * 1024L // 200MB
        const val operationsUnderMemoryPressure = 50
        const val minSuccessRateUnderPressure = 0.7 // 70%
        const val extremeStudentCount = 5000
        const val extremeLessonCount = 25000
        const val intensiveDatabaseOperations = 100
        const val maxDatabaseStressTime = 120000L // 2 minutes
        const val modificationThreads = 5
        const val modificationsPerThread = 10
        const val maxConflictRate = 0.2 // 20%
        const val minModificationSuccessRate = 0.7 // 70%
        const val extremeLoadOperations = 500
        const val maxLoadTime = 300000L // 5 minutes
        const val minLoadTime = 10000L // 10 seconds
        const val minExtremeLoadSuccessRate = 0.6 // 60%
        const val errorRecoveryStudentCount = 100
        const val errorRecoveryOperations = 50
        const val minRecoverySuccessRate = 0.8 // 80%
    }
    
    /**
     * Configuration for data size testing
     */
    object DataSize {
        const val smallStudentCount = 10
        const val mediumStudentCount = 100
        const val largeStudentCount = 1000
        const val smallLessonCount = 50
        const val mediumLessonCount = 500
        const val largeLessonCount = 5000
        const val smallGroupCount = 5
        const val mediumGroupCount = 20
        const val largeGroupCount = 100
    }
    
    /**
     * Configuration for UI testing
     */
    object UI {
        const val maxResponseTime = 2000L // 2 seconds
        const val responsivenessOperations = 50
        const val maxNavigationTime = 3000L // 3 seconds
        const val maxFormSubmissionTime = 5000L // 5 seconds
        const val maxSearchTime = 3000L // 3 seconds
        const val maxScrollTime = 2000L // 2 seconds
        const val maxDialogTime = 1000L // 1 second
        const val maxAnimationTime = 500L // 500ms
    }
    
    /**
     * Configuration for integration testing
     */
    object Integration {
        const val maxUserFlowTime = 60000L // 60 seconds
        const val maxDataSyncTime = 30000L // 30 seconds
        const val maxBackupTime = 60000L // 60 seconds
        const val maxRestoreTime = 120000L // 2 minutes
        const val maxErrorRecoveryTime = 30000L // 30 seconds
        const val minSuccessRate = 0.9 // 90%
        const val maxRetryAttempts = 3
        const val retryDelay = 1000L // 1 second
    }
    
    /**
     * Configuration for security testing
     */
    object Security {
        const val maxEncryptionTime = 5000L // 5 seconds
        const val maxDecryptionTime = 5000L // 5 seconds
        const val maxHashTime = 1000L // 1 second
        const val maxValidationTime = 2000L // 2 seconds
        const val minPasswordStrength = 0.8 // 80%
        const val maxSessionTime = 3600000L // 1 hour
        const val maxTokenTime = 86400000L // 24 hours
    }
    
    /**
     * Configuration for accessibility testing
     */
    object Accessibility {
        const val maxScreenReaderTime = 5000L // 5 seconds
        const val maxKeyboardNavigationTime = 3000L // 3 seconds
        const val maxVoiceCommandTime = 5000L // 5 seconds
        const val minContrastRatio = 4.5 // WCAG AA standard
        const val maxFocusTime = 1000L // 1 second
        const val maxGestureTime = 2000L // 2 seconds
    }
    
    /**
     * Configuration for network testing
     */
    object Network {
        const val maxRequestTime = 10000L // 10 seconds
        const val maxRetryTime = 30000L // 30 seconds
        const val maxOfflineTime = 86400000L // 24 hours
        const val maxSyncTime = 60000L // 60 seconds
        const val maxDownloadTime = 30000L // 30 seconds
        const val maxUploadTime = 60000L // 60 seconds
        const val minConnectionQuality = 0.7 // 70%
    }
    
    /**
     * Configuration for memory testing
     */
    object Memory {
        const val maxMemoryUsage = 200 * 1024 * 1024L // 200MB
        const val maxMemoryLeak = 50 * 1024 * 1024L // 50MB
        const val maxGarbageCollectionTime = 5000L // 5 seconds
        const val maxMemoryPressure = 0.8 // 80%
        const val minMemoryEfficiency = 0.7 // 70%
        const val maxHeapSize = 512 * 1024 * 1024L // 512MB
    }
    
    /**
     * Configuration for battery testing
     */
    object Battery {
        const val maxCpuUsage = 0.8 // 80%
        const val maxBackgroundTime = 300000L // 5 minutes
        const val maxWakeLockTime = 60000L // 1 minute
        const val minBatteryEfficiency = 0.6 // 60%
        const val maxIdleTime = 300000L // 5 minutes
        const val maxActiveTime = 1800000L // 30 minutes
    }
    
    /**
     * Configuration for storage testing
     */
    object Storage {
        const val maxDatabaseSize = 100 * 1024 * 1024L // 100MB
        const val maxCacheSize = 50 * 1024 * 1024L // 50MB
        const val maxBackupSize = 200 * 1024 * 1024L // 200MB
        const val maxLogSize = 10 * 1024 * 1024L // 10MB
        const val minFreeSpace = 100 * 1024 * 1024L // 100MB
        const val maxFileSize = 10 * 1024 * 1024L // 10MB
    }
    
    /**
     * Configuration for concurrency testing
     */
    object Concurrency {
        const val maxThreads = 20
        const val maxCoroutines = 100
        const val maxDatabaseConnections = 10
        const val maxConcurrentOperations = 50
        const val maxLockTime = 5000L // 5 seconds
        const val maxDeadlockTime = 10000L // 10 seconds
        const val minConcurrencyEfficiency = 0.8 // 80%
    }
    
    /**
     * Configuration for error handling testing
     */
    object ErrorHandling {
        const val maxErrorRecoveryTime = 30000L // 30 seconds
        const val maxErrorPropagationTime = 5000L // 5 seconds
        const val maxErrorLoggingTime = 2000L // 2 seconds
        const val minErrorRecoveryRate = 0.9 // 90%
        const val maxErrorRetryAttempts = 5
        const val maxErrorNotificationTime = 3000L // 3 seconds
    }
    
    /**
     * Configuration for data validation testing
     */
    object Validation {
        const val maxValidationTime = 2000L // 2 seconds
        const val maxSanitizationTime = 1000L // 1 second
        const val maxTransformationTime = 3000L // 3 seconds
        const val minValidationAccuracy = 0.95 // 95%
        const val maxInputSize = 10000 // 10KB
        const val maxOutputSize = 100000 // 100KB
    }
    
    /**
     * Configuration for logging and monitoring testing
     */
    object Logging {
        const val maxLogWriteTime = 1000L // 1 second
        const val maxLogReadTime = 2000L // 2 seconds
        const val maxLogRotationTime = 5000L // 5 seconds
        const val maxLogCompressionTime = 10000L // 10 seconds
        const val maxLogSearchTime = 5000L // 5 seconds
        const val maxLogExportTime = 30000L // 30 seconds
    }
    
    /**
     * Configuration for backup and restore testing
     */
    object Backup {
        const val maxBackupCreationTime = 120000L // 2 minutes
        const val maxBackupRestoreTime = 300000L // 5 minutes
        const val maxBackupVerificationTime = 60000L // 1 minute
        const val maxBackupCompressionTime = 60000L // 1 minute
        const val maxBackupEncryptionTime = 120000L // 2 minutes
        const val minBackupIntegrity = 0.99 // 99%
    }
    
    /**
     * Configuration for analytics and reporting testing
     */
    object Analytics {
        const val maxDataCollectionTime = 5000L // 5 seconds
        const val maxReportGenerationTime = 30000L // 30 seconds
        const val maxDataExportTime = 60000L // 1 minute
        const val maxDataImportTime = 120000L // 2 minutes
        const val maxDataProcessingTime = 60000L // 1 minute
        const val minDataAccuracy = 0.95 // 95%
    }
}
