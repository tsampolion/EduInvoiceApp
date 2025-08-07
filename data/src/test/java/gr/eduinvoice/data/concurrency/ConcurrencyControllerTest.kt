package gr.eduinvoice.data.concurrency

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class ConcurrencyControllerTest {
    
    private lateinit var concurrencyController: ConcurrencyController
    private lateinit var transactionManager: TransactionManager
    private lateinit var operationQueueManager: OperationQueueManager
    private lateinit var database: EduInvoiceDatabase
    private lateinit var context: Context
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            EduInvoiceDatabase::class.java
        ).build()
        
        transactionManager = TransactionManager(database)
        operationQueueManager = OperationQueueManager()
        concurrencyController = ConcurrencyController(transactionManager, operationQueueManager)
    }
    
    @Test
    fun `test safe operation execution`() = runTest {
        val result = concurrencyController.executeSafeOperation(
            operation = { "test_result" },
            operationType = OperationType.READ,
            resourceId = "test_resource"
        )
        
        assertTrue("Safe operation should succeed", result.isSuccess)
        assertEquals("Should return expected result", "test_result", result.getOrNull())
    }
    
    @Test
    fun `test safe operation with transaction`() = runTest {
        val student = Student(
            id = 0,
            ownerId = 1L,
            name = "John",
            surname = "Doe",
            parentMobile = "1234567890",
            parentEmail = "john@example.com",
            className = "Math",
            rate = 50.0,
            rateType = "per_hour",
            isActive = true
        )
        
        val result = concurrencyController.executeSafeOperation(
            operation = {
                val studentId = database.studentDao().insert(student)
                database.studentDao().getStudentById(studentId, 1L).first()
            },
            operationType = OperationType.WRITE,
            resourceId = "student_1",
            useTransaction = true
        )
        
        assertTrue("Safe operation with transaction should succeed", result.isSuccess)
        assertNotNull("Student should be returned", result.getOrNull())
        assertEquals("Student name should match", "John", result.getOrNull()?.name)
    }
    
    @Test
    fun `test safe operation without transaction`() = runTest {
        val result = concurrencyController.executeSafeOperation(
            operation = { "no_transaction_result" },
            operationType = OperationType.READ,
            resourceId = "test_resource",
            useTransaction = false
        )
        
        assertTrue("Safe operation without transaction should succeed", result.isSuccess)
        assertEquals("Should return expected result", "no_transaction_result", result.getOrNull())
    }
    
    @Test
    fun `test batch safe operations`() = runTest {
        val operations = listOf(
            suspend { "result1" },
            suspend { "result2" },
            suspend { "result3" }
        )
        
        val result = concurrencyController.executeBatchSafeOperations(
            operations = operations,
            operationType = OperationType.BATCH,
            resourceId = "batch_resource"
        )
        
        assertTrue("Batch safe operations should succeed", result.isSuccess)
        val results = result.getOrNull()
        assertNotNull("Should return results", results)
        assertEquals("Should have 3 results", 3, results?.size)
    }
    
    @Test
    fun `test read-only operation`() = runTest {
        // First insert a student
        val student = Student(
            id = 0,
            ownerId = 1L,
            name = "Alice",
            surname = "Smith",
            parentMobile = "1234567890",
            parentEmail = "alice@example.com",
            className = "Math",
            rate = 50.0,
            rateType = "per_hour",
            isActive = true
        )
        database.studentDao().insert(student)
        
        val result = concurrencyController.executeReadOnlyOperation(
            operation = {
                database.studentDao().getAllActiveStudents(1L).first()
            },
            resourceId = "student_1"
        )
        
        assertTrue("Read-only operation should succeed", result.isSuccess)
        assertNotNull("Students should be returned", result.getOrNull())
        assertTrue("Should have at least one student", result.getOrNull()?.isNotEmpty() == true)
    }
    
    @Test
    fun `test resource locking`() = runTest {
        val counter = AtomicInteger(0)
        
        val result = concurrencyController.withResourceLock("test_lock") {
            counter.incrementAndGet()
            "locked_result"
        }
        
        assertEquals("Should return expected result", "locked_result", result)
        assertEquals("Counter should be incremented", 1, counter.get())
    }
    
    @Test
    fun `test concurrent safe operations`() = runTest {
        val counter = AtomicInteger(0)
        val results = mutableListOf<Result<Int>>()
        
        // Launch multiple concurrent safe operations
        val jobs = (1..5).map { index ->
            async {
                concurrencyController.executeSafeOperation(
                    operation = {
                        delay(10) // Small delay to simulate work
                        counter.incrementAndGet()
                    },
                    operationType = OperationType.WRITE,
                    resourceId = "concurrent_resource"
                )
            }
        }
        
        // Wait for all operations to complete
        jobs.forEach { job ->
            results.add(job.await())
        }
        
        // All operations should succeed
        results.forEach { result ->
            assertTrue("Each safe operation should succeed", result.isSuccess)
        }
        
        // Counter should be incremented 5 times
        assertEquals("Counter should be 5", 5, counter.get())
    }
    
    @Test
    fun `test concurrency statistics`() = runTest {
        val initialStats = concurrencyController.getConcurrencyStatistics()
        
        // Execute some operations
        concurrencyController.executeSafeOperation(
            operation = { "test1" },
            operationType = OperationType.READ
        )
        
        concurrencyController.executeSafeOperation(
            operation = { "test2" },
            operationType = OperationType.WRITE,
            resourceId = "test_resource"
        )
        
        concurrencyController.executeBatchSafeOperations(
            operations = listOf({ "batch1" }, { "batch2" }),
            operationType = OperationType.BATCH
        )
        
        val finalStats = concurrencyController.getConcurrencyStatistics()
        
        assertEquals("Total operations should increase by 3", initialStats.totalOperations + 3, finalStats.totalOperations)
        assertEquals("Batch operations should increase by 1", initialStats.batchOperations + 1, finalStats.batchOperations)
        assertTrue("Should have active resource locks", finalStats.activeResourceLocks > 0)
    }
    
    @Test
    fun `test active resource locks`() = runTest {
        val initialLocks = concurrencyController.getActiveResourceLocks().size
        
        // Execute operations with resource locks
        concurrencyController.executeSafeOperation(
            operation = { "test" },
            operationType = OperationType.WRITE,
            resourceId = "resource1"
        )
        
        concurrencyController.executeSafeOperation(
            operation = { "test" },
            operationType = OperationType.WRITE,
            resourceId = "resource2"
        )
        
        val finalLocks = concurrencyController.getActiveResourceLocks()
        assertTrue("Should have resource locks", finalLocks.isNotEmpty())
        assertTrue("Should contain resource1", finalLocks.contains("resource1"))
        assertTrue("Should contain resource2", finalLocks.contains("resource2"))
    }
    
    @Test
    fun `test health check`() = runTest {
        val healthResult = concurrencyController.performHealthCheck()
        
        assertNotNull("Health check should return result", healthResult)
        assertTrue("Should be healthy initially", healthResult.isHealthy)
        assertNotNull("Should have transaction stats", healthResult.transactionStats)
        assertNotNull("Should have queue stats", healthResult.queueStats)
        assertNotNull("Should have concurrency stats", healthResult.concurrencyStats)
    }
    
    @Test
    fun `test emergency cleanup`() = runTest {
        // Start some operations
        val deferred = CompletableDeferred<Unit>()
        
        val job = async {
            concurrencyController.executeSafeOperation(
                operation = {
                    deferred.await() // Keep operation open
                    "completed"
                },
                operationType = OperationType.READ,
                resourceId = "cleanup_test"
            )
        }
        
        // Verify operations are active
        assertTrue("Should have active operations", concurrencyController.getActiveResourceLocks().isNotEmpty())
        
        // Perform emergency cleanup
        concurrencyController.emergencyCleanup()
        
        // Complete the deferred to avoid hanging
        deferred.complete(Unit)
        job.join()
        
        // Verify cleanup
        assertEquals("Should have no active resource locks after cleanup", 0, concurrencyController.getActiveResourceLocks().size)
    }
    
    @Test
    fun `test different operation types with safe execution`() = runTest {
        val readResult = concurrencyController.executeSafeOperation(
            operation = { "read_result" },
            operationType = OperationType.READ
        )
        
        val writeResult = concurrencyController.executeSafeOperation(
            operation = { "write_result" },
            operationType = OperationType.WRITE,
            resourceId = "test_resource"
        )
        
        val updateResult = concurrencyController.executeSafeOperation(
            operation = { "update_result" },
            operationType = OperationType.UPDATE,
            resourceId = "test_resource"
        )
        
        val deleteResult = concurrencyController.executeSafeOperation(
            operation = { "delete_result" },
            operationType = OperationType.DELETE,
            resourceId = "test_resource"
        )
        
        assertTrue("Read operation should succeed", readResult.isSuccess)
        assertTrue("Write operation should succeed", writeResult.isSuccess)
        assertTrue("Update operation should succeed", updateResult.isSuccess)
        assertTrue("Delete operation should succeed", deleteResult.isSuccess)
        
        assertEquals("Read result should match", "read_result", readResult.getOrNull())
        assertEquals("Write result should match", "write_result", writeResult.getOrNull())
        assertEquals("Update result should match", "update_result", updateResult.getOrNull())
        assertEquals("Delete result should match", "delete_result", deleteResult.getOrNull())
    }
    
    @Test
    fun `test operation priorities with safe execution`() = runTest {
        val lowPriority = concurrencyController.executeSafeOperation(
            operation = { "low_priority" },
            operationType = OperationType.READ,
            resourceId = "test_resource",
            priority = OperationPriority.LOW
        )
        
        val highPriority = concurrencyController.executeSafeOperation(
            operation = { "high_priority" },
            operationType = OperationType.WRITE,
            resourceId = "test_resource",
            priority = OperationPriority.HIGH
        )
        
        val criticalPriority = concurrencyController.executeSafeOperation(
            operation = { "critical_priority" },
            operationType = OperationType.DELETE,
            resourceId = "test_resource",
            priority = OperationPriority.CRITICAL
        )
        
        assertTrue("Low priority operation should succeed", lowPriority.isSuccess)
        assertTrue("High priority operation should succeed", highPriority.isSuccess)
        assertTrue("Critical priority operation should succeed", criticalPriority.isSuccess)
    }
    
    @Test
    fun `test transaction isolation levels with safe execution`() = runTest {
        val result = concurrencyController.executeSafeOperation(
            operation = { "test" },
            operationType = OperationType.WRITE,
            resourceId = "test_resource",
            isolationLevel = TransactionIsolationLevel.READ_COMMITTED
        )
        
        assertTrue("Safe operation with custom isolation level should succeed", result.isSuccess)
    }
    
    @Test
    fun `test resource lock release`() = runTest {
        // Execute operation with resource lock
        concurrencyController.executeSafeOperation(
            operation = { "test" },
            operationType = OperationType.WRITE,
            resourceId = "test_resource"
        )
        
        // Verify resource lock exists
        assertTrue("Should have resource lock", concurrencyController.getActiveResourceLocks().contains("test_resource"))
        
        // Release all resource locks
        concurrencyController.releaseAllResourceLocks()
        
        // Verify resource lock is released
        assertFalse("Should not have resource lock after release", concurrencyController.getActiveResourceLocks().contains("test_resource"))
    }
} 