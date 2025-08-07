package gr.eduinvoice.infrastructure

/**
 * Centralized test configuration for the comprehensive test suite.
 * Contains performance thresholds, test data sizes, and configuration settings.
 */
object TestConfiguration {
    
    // Performance thresholds
    object Performance {
        const val MAX_INSERTION_TIME_MS = 5000L
        const val MAX_QUERY_TIME_MS = 1000L
        const val MAX_UI_RESPONSE_TIME_MS = 1000L
        const val MAX_PDF_GENERATION_TIME_MS = 5000L
        const val MAX_MEMORY_USAGE_BYTES = 100L * 1024 * 1024 // 100MB
        const val MAX_MEMORY_LEAK_BYTES = 10L * 1024 * 1024 // 10MB
        const val MAX_CONCURRENT_OPERATIONS_TIME_MS = 3000L
        const val MAX_PAGINATION_TIME_MS = 500L
    }
    
    // Test data sizes
    object DataSize {
        const val SMALL_DATASET = 100
        const val MEDIUM_DATASET = 1000
        const val LARGE_DATASET = 10000
        const val EXTREME_DATASET = 50000
        const val STUDENTS_PER_USER = 100
        const val LESSONS_PER_STUDENT = 50
        const val GROUPS_PER_USER = 20
    }
    
    // Stress test parameters
    object Stress {
        const val CONCURRENT_THREADS = 50
        const val OPERATIONS_PER_THREAD = 100
        const val MAX_EXECUTION_TIME_MS = 30000L
        const val MEMORY_PRESSURE_ITERATIONS = 100
        const val DATABASE_STRESS_OPERATIONS = 10000
        const val CONCURRENT_MODIFICATIONS = 20
        const val MODIFICATIONS_PER_THREAD = 50
    }
    
    // Success rate thresholds
    object SuccessRates {
        const val MIN_CONCURRENT_SUCCESS_RATE = 0.90 // 90%
        const val MIN_DATABASE_SUCCESS_RATE = 0.95 // 95%
        const val MIN_ERROR_RECOVERY_RATE = 0.80 // 80%
        const val MAX_ERROR_RATE = 0.10 // 10%
    }
    
    // Test timeouts
    object Timeouts {
        const val UNIT_TEST_TIMEOUT_MS = 5000L
        const val INTEGRATION_TEST_TIMEOUT_MS = 30000L
        const val PERFORMANCE_TEST_TIMEOUT_MS = 60000L
        const val STRESS_TEST_TIMEOUT_MS = 120000L
        const val UI_TEST_TIMEOUT_MS = 10000L
    }
    
    // Database configuration
    object Database {
        const val TEST_DB_NAME_PREFIX = "test_database_"
        const val MAX_DB_SIZE_BYTES = 50L * 1024 * 1024 // 50MB
        const val DB_CLEANUP_TIMEOUT_MS = 5000L
        const val MAX_CONCURRENT_DB_OPERATIONS = 100
    }
    
    // UI test configuration
    object UI {
        const val UI_INTERACTION_DELAY_MS = 100L
        const val SCROLL_TIMEOUT_MS = 5000L
        const val ELEMENT_WAIT_TIMEOUT_MS = 10000L
        const val ANIMATION_TIMEOUT_MS = 2000L
        const val MAX_UI_LOAD_TIME_MS = 3000L
    }
    
    // Network simulation
    object Network {
        const val OFFLINE_TIMEOUT_MS = 5000L
        const val SYNC_TIMEOUT_MS = 10000L
        const val RETRY_ATTEMPTS = 3
        const val BACKOFF_DELAY_MS = 1000L
    }
    
    // Error simulation
    object ErrorSimulation {
        const val DATABASE_ERROR_PROBABILITY = 0.1 // 10%
        const val NETWORK_ERROR_PROBABILITY = 0.15 // 15%
        const val MEMORY_ERROR_PROBABILITY = 0.05 // 5%
        const val VALIDATION_ERROR_PROBABILITY = 0.20 // 20%
    }
    
    // Test categories
    object Categories {
        const val UNIT_TESTS = "unit"
        const val INTEGRATION_TESTS = "integration"
        const val PERFORMANCE_TESTS = "performance"
        const val STRESS_TESTS = "stress"
        const val UI_TESTS = "ui"
        const val SECURITY_TESTS = "security"
    }
    
    // Test data patterns
    object TestData {
        const val STUDENT_NAME_PREFIX = "Test Student"
        const val STUDENT_EMAIL_PREFIX = "test"
        const val STUDENT_EMAIL_DOMAIN = "@test.com"
        const val PHONE_PREFIX = "+123456789"
        const val LESSON_NOTES_PREFIX = "Test lesson"
        const val GROUP_NAME_PREFIX = "Test Group"
        const val USERNAME_PREFIX = "testuser"
    }
    
    // Validation rules
    object Validation {
        const val MIN_STUDENT_NAME_LENGTH = 2
        const val MAX_STUDENT_NAME_LENGTH = 100
        const val MIN_HOURLY_RATE = 0.01
        const val MAX_HOURLY_RATE = 1000.0
        const val MIN_LESSON_DURATION = 15
        const val MAX_LESSON_DURATION = 480 // 8 hours
        const val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        const val PHONE_REGEX = "^\\+[1-9]\\d{1,14}$"
    }
    
    // Test reporting
    object Reporting {
        const val PERFORMANCE_REPORT_ENABLED = true
        const val MEMORY_USAGE_REPORT_ENABLED = true
        const val ERROR_REPORT_ENABLED = true
        const val DETAILED_LOGGING_ENABLED = true
        const val TEST_RESULTS_DIR = "test-results"
        const val PERFORMANCE_METRICS_FILE = "performance-metrics.json"
    }
    
    // Environment configuration
    object Environment {
        const val TEST_ENVIRONMENT = "test"
        const val CI_ENVIRONMENT = "ci"
        const val LOCAL_ENVIRONMENT = "local"
        const val DEBUG_MODE = true
        const val ENABLE_MOCKING = true
        const val ENABLE_LOGGING = true
    }
}
