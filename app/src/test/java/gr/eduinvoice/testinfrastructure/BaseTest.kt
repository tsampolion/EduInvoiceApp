package gr.eduinvoice.testinfrastructure

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.BouncyCastleTestRunner
import gr.eduinvoice.MainDispatcherRule
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Base class for all tests with standardized configuration
 * Provides common setup, teardown, and utilities for all test types
 */
@RunWith(BouncyCastleTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
abstract class BaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val databaseContainer = TestDatabaseContainer()

    protected val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    
    protected lateinit var database: EduInvoiceDatabase
    protected lateinit var testEnvironment: TestInfrastructure.TestEnvironment

    @Before
    open fun setUp() {
        database = databaseContainer.createTestDatabase()
        testEnvironment = TestInfrastructure.createTestEnvironment(database)
    }

    @After
    open fun tearDown() {
        databaseContainer.cleanupTestDatabase()
    }

    // Common test utilities
    protected fun createTestUser() = TestInfrastructure.TestDataFactory.createTestUser()
    protected fun createTestStudent() = TestInfrastructure.TestDataFactory.createTestStudent()
    protected fun createTestLesson() = TestInfrastructure.TestDataFactory.createTestLesson()
    protected fun createTestGroup() = TestInfrastructure.TestDataFactory.createTestGroup()
    
    // Performance utilities
    protected fun measureTime(operation: () -> Unit): Long = 
        TestInfrastructure.PerformanceUtils.measureTime(operation)
    
    protected fun getMemoryUsage(): Long = 
        TestInfrastructure.PerformanceUtils.getMemoryUsage()
    
    protected fun measureMemoryUsage(operation: () -> Unit): Long = 
        TestInfrastructure.PerformanceUtils.measureMemoryUsage(operation)
    
    // Validation utilities
    protected fun isValidEmail(email: String): Boolean = 
        TestInfrastructure.TestValidation.isValidEmail(email)
    
    protected fun isValidPhoneNumber(phone: String): Boolean = 
        TestInfrastructure.TestValidation.isValidPhoneNumber(phone)
    
    protected fun isValidRate(rate: Double): Boolean = 
        TestInfrastructure.TestValidation.isValidRate(rate)
}
