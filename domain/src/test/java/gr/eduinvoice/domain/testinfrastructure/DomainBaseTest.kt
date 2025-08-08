package gr.eduinvoice.domain.testinfrastructure

import gr.eduinvoice.domain.BouncyCastleTestRunner
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.domain.testinfrastructure.TestDatabaseContainer
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Base class for all domain module tests with standardized configuration
 * Provides common setup, teardown, and utilities for domain layer testing
 */
@RunWith(BouncyCastleTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
abstract class DomainBaseTest {

    @get:Rule
    val databaseContainer = TestDatabaseContainer()

    protected val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    protected lateinit var database: EduInvoiceDatabase
    protected lateinit var domainTestEnvironment: DomainTestInfrastructure.DomainTestEnvironment

    @Before
    open fun setUp() {
        database = databaseContainer.createTestDatabase()
        domainTestEnvironment = DomainTestInfrastructure.createDomainTestEnvironment(database)
    }

    @After
    open fun tearDown() {
        databaseContainer.cleanupTestDatabase()
    }

    // Common test utilities
    protected fun createTestUser() = DomainTestInfrastructure.DomainTestDataFactory.createTestUser()
    protected fun createTestStudent() = DomainTestInfrastructure.DomainTestDataFactory.createTestStudent()
    protected fun createTestLesson() = DomainTestInfrastructure.DomainTestDataFactory.createTestLesson()
    protected fun createTestGroup() = DomainTestInfrastructure.DomainTestDataFactory.createTestGroup()

    // Performance utilities
    protected fun measureUseCaseExecutionTime(execution: () -> Unit): Long =
        DomainTestInfrastructure.DomainPerformanceUtils.measureUseCaseExecutionTime(execution)

    protected fun measureBusinessLogicPerformance(logic: () -> Unit): Long =
        DomainTestInfrastructure.DomainPerformanceUtils.measureBusinessLogicPerformance(logic)

    protected fun getMemoryUsage(): Long =
        DomainTestInfrastructure.DomainPerformanceUtils.getMemoryUsage()

    protected fun measureMemoryUsage(operation: () -> Unit): Long =
        DomainTestInfrastructure.DomainPerformanceUtils.measureMemoryUsage(operation)

    // Validation utilities
    protected fun isValidBusinessRule(rule: () -> Boolean): Boolean =
        DomainTestInfrastructure.DomainTestValidation.isValidBusinessRule(rule)

    protected fun isValidUseCaseExecution(execution: () -> Unit): Boolean =
        DomainTestInfrastructure.DomainTestValidation.isValidUseCaseExecution(execution)

    protected fun isValidDomainOperation(operation: () -> Unit): Boolean =
        DomainTestInfrastructure.DomainTestValidation.isValidDomainOperation(operation)
}
