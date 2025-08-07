package gr.eduinvoice.data.testinfrastructure

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.BouncyCastleTestRunner
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
 * Base class for all data module tests with standardized configuration
 * Provides common setup, teardown, and utilities for data layer testing
 */
@RunWith(BouncyCastleTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
abstract class DataBaseTest {

    @get:Rule
    val databaseContainer = TestDatabaseContainer()

    protected val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    protected lateinit var database: EduInvoiceDatabase
    protected lateinit var dataTestEnvironment: DataTestInfrastructure.DataTestEnvironment

    @Before
    open fun setUp() {
        database = databaseContainer.createTestDatabase()
        dataTestEnvironment = DataTestInfrastructure.createDataTestEnvironment(database)
    }

    @After
    open fun tearDown() {
        databaseContainer.cleanupTestDatabase()
    }

    // Common test utilities
    protected fun createTestUser() = DataTestInfrastructure.DataTestDataFactory.createTestUser()
    protected fun createTestStudent() = DataTestInfrastructure.DataTestDataFactory.createTestStudent()
    protected fun createTestLesson() = DataTestInfrastructure.DataTestDataFactory.createTestLesson()
    protected fun createTestGroup() = DataTestInfrastructure.DataTestDataFactory.createTestGroup()

    // Performance utilities
    protected fun measureDatabaseOperationTime(operation: () -> Unit): Long =
        DataTestInfrastructure.DataPerformanceUtils.measureDatabaseOperationTime(operation)

    protected fun getMemoryUsage(): Long =
        DataTestInfrastructure.DataPerformanceUtils.getMemoryUsage()

    protected fun measureMemoryUsage(operation: () -> Unit): Long =
        DataTestInfrastructure.DataPerformanceUtils.measureMemoryUsage(operation)

    protected fun measureConcurrencyPerformance(
        threadCount: Int,
        operationsPerThread: Int,
        operation: (Int) -> Unit
    ): Long =
        DataTestInfrastructure.DataPerformanceUtils.measureConcurrencyPerformance(
            threadCount, operationsPerThread, operation
        )

    // Validation utilities
    protected fun isValidDatabaseOperation(operation: () -> Unit): Boolean =
        DataTestInfrastructure.DataTestValidation.isValidDatabaseOperation(operation)

    protected fun isValidTransaction(transaction: () -> Unit): Boolean =
        DataTestInfrastructure.DataTestValidation.isValidTransaction(transaction)

    protected fun isValidConcurrencyOperation(operation: () -> Unit): Boolean =
        DataTestInfrastructure.DataTestValidation.isValidConcurrencyOperation(operation)
}
