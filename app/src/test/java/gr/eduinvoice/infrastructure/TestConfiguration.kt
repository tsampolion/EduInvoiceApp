package gr.eduinvoice.infrastructure

/**
 * Centralized test configuration and parameters
 * Defines thresholds, data sizes, and test parameters for all test categories
 */
object TestConfiguration {
    
    /**
     * Performance testing thresholds and parameters
     */
    object Performance {
        const val maxInsertionTime = 5000L // 5 seconds
        const val maxQueryTime = 1000L // 1 second
        const val maxMemoryUsage = 100 * 1024 * 1024L // 100 MB
        const val minCleanupEfficiency = 0.7 // 70% memory cleanup efficiency
        const val maxConcurrentTime = 3000L // 3 seconds
        const val maxPaginationTime = 2000L // 2 seconds
        const val maxUiResponseTime = 1000L // 1 second
        const val maxPdfGenerationTime = 5000L // 5 seconds
        const val maxMemoryLeak = 10 * 1024 * 1024L // 10 MB
    }
    
    /**
     * Stress testing parameters
     */
    object Stress {
        const val concurrentThreads = 10
        const val operationsPerThread = 50
        const val maxExecutionTime = 30000L // 30 seconds
        const val maxErrorRate = 0.1 // 10% error rate
        const val minSuccessRate = 0.8 // 80% success rate
        const val memoryPressureStudentCount = 1000
        const val memoryPressureLessonCount = 5000
        const val memoryPressureGroupCount = 100
        const val maxMemoryIncrease = 200 * 1024 * 1024L // 200 MB
        const val operationsUnderMemoryPressure = 100
        const val minSuccessRateUnderPressure = 0.7 // 70% success rate under pressure
        const val extremeStudentCount = 5000
        const val extremeLessonCount = 25000
        const val intensiveDatabaseOperations = 1000
        const val maxDatabaseStressTime = 60000L // 60 seconds
        const val concurrentModificationStudentCount = 100
        const val modificationThreads = 5
        const val modificationsPerThread = 20
        const val maxConflictRate = 0.2 // 20% conflict rate
        const val minModificationSuccessRate = 0.6 // 60% modification success rate
        const val extremeLoadOperations = 1000
        const val maxLoadTime = 120000L // 2 minutes
        const val minLoadTime = 10000L // 10 seconds
        const val minExtremeLoadSuccessRate = 0.5 // 50% success rate under extreme load
        const val errorRecoveryStudentCount = 100
        const val errorRecoveryOperations = 50
        const val minRecoverySuccessRate = 0.8 // 80% recovery success rate
    }
    
    /**
     * Data size configurations for different test scenarios
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
        const val studentsPerUser = 100
        const val lessonsPerStudent = 50
        const val groupsPerUser = 20
    }
    
    /**
     * Database configuration parameters
     */
    object Database {
        const val pageSize = 20
        const val maxConnectionPoolSize = 10
        const val connectionTimeout = 5000L // 5 seconds
        const val queryTimeout = 3000L // 3 seconds
        const val testDbNamePrefix = "test_database_"
        const val maxDbSize = 50 * 1024 * 1024L // 50 MB
        const val dbCleanupTimeout = 5000L // 5 seconds
        const val maxConcurrentDbOperations = 100
    }
    
    /**
     * Network simulation parameters
     */
    object Network {
        const val simulatedTimeout = 1000L // 1 second
        const val maxOperationTime = 5000L // 5 seconds
        const val retryAttempts = 3
        const val retryDelay = 1000L // 1 second
        const val offlineTimeout = 5000L // 5 seconds
        const val syncTimeout = 10000L // 10 seconds
        const val backoffDelay = 1000L // 1 second
    }
    
    /**
     * UI testing parameters
     */
    object UI {
        const val responsivenessOperations = 100
        const val maxResponseTime = 500L // 500ms
        const val animationDuration = 300L // 300ms
        const val touchTargetSize = 48 // 48dp minimum
        const val scrollThreshold = 100 // 100dp scroll threshold
        const val interactionDelay = 100L // 100ms
        const val scrollTimeout = 5000L // 5 seconds
        const val elementWaitTimeout = 10000L // 10 seconds
        const val animationTimeout = 2000L // 2 seconds
        const val maxUiLoadTime = 3000L // 3 seconds
    }
    
    /**
     * Success rate thresholds for different operations
     */
    object SuccessRates {
        const val syncSuccessRate = 0.9 // 90% sync success rate
        const val backupSuccessRate = 0.95 // 95% backup success rate
        const val restoreSuccessRate = 0.9 // 90% restore success rate
        const val validationSuccessRate = 0.99 // 99% validation success rate
        const val minConcurrentSuccessRate = 0.9 // 90%
        const val minDatabaseSuccessRate = 0.95 // 95%
        const val minErrorRecoveryRate = 0.8 // 80%
        const val maxErrorRate = 0.1 // 10%
    }
    
    /**
     * Security testing parameters
     */
    object Security {
        const val passwordMinLength = 8
        const val passwordMaxLength = 128
        const val sessionTimeout = 3600000L // 1 hour
        const val maxLoginAttempts = 5
        const val lockoutDuration = 300000L // 5 minutes
        const val encryptionKeySize = 256 // 256-bit encryption
    }
    
    /**
     * Accessibility testing parameters
     */
    object Accessibility {
        const val minContrastRatio = 4.5 // WCAG AA standard
        const val minTouchTargetSize = 48 // 48dp minimum
        const val maxFocusTime = 2000L // 2 seconds for focus changes
        const val screenReaderAnnouncementDelay = 500L // 500ms delay
    }
    
    /**
     * Validation testing parameters
     */
    object Validation {
        const val maxNameLength = 100
        const val maxEmailLength = 254
        const val maxPhoneLength = 20
        const val maxNotesLength = 10000
        const val minRate = 0.01
        const val maxRate = 1000.0
        const val minDuration = 1
        const val maxDuration = 480 // 8 hours
        const val minStudentNameLength = 2
        const val maxStudentNameLength = 100
        const val minHourlyRate = 0.01
        const val maxHourlyRate = 1000.0
        const val minLessonDuration = 15
        const val maxLessonDuration = 480 // 8 hours
        const val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        const val phoneRegex = "^\\+[1-9]\\d{1,14}$"
    }
    
    /**
     * Integration testing parameters
     */
    object Integration {
        const val maxTestDuration = 60000L // 60 seconds
        const val retryCount = 3
        const val timeoutBetweenRetries = 1000L // 1 second
        const val maxConcurrentUsers = 5
        const val dataConsistencyThreshold = 0.99 // 99% consistency
    }
    
    /**
     * Test timeouts
     */
    object Timeouts {
        const val unitTestTimeout = 5000L // 5 seconds
        const val integrationTestTimeout = 30000L // 30 seconds
        const val performanceTestTimeout = 60000L // 60 seconds
        const val stressTestTimeout = 120000L // 2 minutes
        const val uiTestTimeout = 10000L // 10 seconds
    }
    
    /**
     * Error simulation parameters
     */
    object ErrorSimulation {
        const val databaseErrorProbability = 0.1 // 10%
        const val networkErrorProbability = 0.15 // 15%
        const val memoryErrorProbability = 0.05 // 5%
        const val validationErrorProbability = 0.2 // 20%
    }
    
    /**
     * Test categories
     */
    object Categories {
        const val unitTests = "unit"
        const val integrationTests = "integration"
        const val performanceTests = "performance"
        const val stressTests = "stress"
        const val uiTests = "ui"
        const val securityTests = "security"
        const val accessibilityTests = "accessibility"
        const val validationTests = "validation"
    }
    
    /**
     * Test data patterns
     */
    object TestData {
        const val studentNamePrefix = "Test Student"
        const val studentEmailPrefix = "test"
        const val studentEmailDomain = "@test.com"
        const val phonePrefix = "+123456789"
        const val lessonNotesPrefix = "Test lesson"
        const val groupNamePrefix = "Test Group"
        const val usernamePrefix = "testuser"
    }
    
    /**
     * Test reporting
     */
    object Reporting {
        const val performanceReportEnabled = true
        const val memoryUsageReportEnabled = true
        const val errorReportEnabled = true
        const val detailedLoggingEnabled = true
        const val testResultsDir = "test-results"
        const val performanceMetricsFile = "performance-metrics.json"
    }
    
    /**
     * Environment configuration
     */
    object Environment {
        const val testEnvironment = "test"
        const val ciEnvironment = "ci"
        const val localEnvironment = "local"
        const val debugMode = true
        const val enableMocking = true
        const val enableLogging = true
    }
}
