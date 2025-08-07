package gr.eduinvoice.data.concurrency

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class OperationQueueManagerTest {
    
    private lateinit var operationQueueManager: OperationQueueManager
    
    @Before
    fun setUp() {
        operationQueueManager = OperationQueueManager()
    }
    
    @Test
    fun `test successful operation queuing`() = runTest {
        val result = operationQueueManager.queueOperation(
            operation = { "test_result" },
            operationType = OperationType.READ,
            resourceId = "test_resource"
        )
        
        assertTrue("Operation should succeed", result.isSuccess)
        assertEquals("Should return expected result", "test_result", result.getOrNull())
    }
    
    @Test
    fun `test operation queuing with different priorities`() = runTest {
        val results = mutableListOf<String>()
        
        // Queue operations with different priorities
        val lowPriority = operationQueueManager.queueOperation(
            operation = { "low_priority" },
            operationType = OperationType.READ,
            resourceId = "test_resource",
            priority = OperationPriority.LOW
        )
        
        val highPriority = operationQueueManager.queueOperation(
            operation = { "high_priority" },
            operationType = OperationType.READ,
            resourceId = "test_resource",
            priority = OperationPriority.HIGH
        )
        
        val normalPriority = operationQueueManager.queueOperation(
            operation = { "normal_priority" },
            operationType = OperationType.READ,
            resourceId = "test_resource",
            priority = OperationPriority.NORMAL
        )
        
        assertTrue("Low priority operation should succeed", lowPriority.isSuccess)
        assertTrue("High priority operation should succeed", highPriority.isSuccess)
        assertTrue("Normal priority operation should succeed", normalPriority.isSuccess)
        
        results.add(lowPriority.getOrNull() ?: "")
        results.add(highPriority.getOrNull() ?: "")
        results.add(normalPriority.getOrNull() ?: "")
        
        assertTrue("Should have all results", results.all { it.isNotEmpty() })
    }
    
    @Test
    fun `test operation timeout`() = runTest {
        val result = operationQueueManager.queueOperation(
            operation = {
                delay(100) // Simulate slow operation
                "slow_result"
            },
            operationType = OperationType.READ,
            resourceId = "test_resource",
            timeout = 50L // Short timeout
        )
        
        assertTrue("Operation should timeout", result.isFailure)
        assertTrue("Should contain timeout error", result.exceptionOrNull()?.message?.contains("timed out") == true)
    }
    
    @Test
    fun `test batch operations`() = runTest {
        val operations = listOf(
            suspend { "result1" },
            suspend { "result2" },
            suspend { "result3" }
        )
        
        val result = operationQueueManager.executeBatchOperations(
            operations = operations,
            operationType = OperationType.BATCH,
            resourceId = "batch_resource"
        )
        
        assertTrue("Batch operation should succeed", result.isSuccess)
        val results = result.getOrNull()
        assertNotNull("Should return results", results)
        assertEquals("Should have 3 results", 3, results?.size)
        assertEquals("First result should match", "result1", results?.get(0))
        assertEquals("Second result should match", "result2", results?.get(1))
        assertEquals("Third result should match", "result3", results?.get(2))
    }
    
    @Test
    fun `test batch operations with failure`() = runTest {
        val operations = listOf(
            suspend { "result1" },
            suspend { throw RuntimeException("Simulated failure") },
            suspend { "result3" }
        )
        
        val result = operationQueueManager.executeBatchOperations(
            operations = operations,
            operationType = OperationType.BATCH,
            resourceId = "batch_failure_resource"
        )
        
        assertTrue("Batch operation should fail", result.isFailure)
        assertTrue("Should contain failure message", result.exceptionOrNull()?.message?.contains("Batch operation failed") == true)
    }
    
    @Test
    fun `test concurrent operations on same resource`() = runTest {
        val counter = AtomicInteger(0)
        val results = mutableListOf<Result<Int>>()
        
        // Launch multiple concurrent operations on the same resource
        val jobs = (1..5).map { index ->
            async {
                operationQueueManager.queueOperation(
                    operation = {
                        delay(10) // Small delay to simulate work
                        counter.incrementAndGet()
                    },
                    operationType = OperationType.WRITE,
                    resourceId = "concurrent_resource",
                    priority = OperationPriority.NORMAL
                )
            }
        }
        
        // Wait for all operations to complete
        jobs.forEach { job ->
            results.add(job.await())
        }
        
        // All operations should succeed
        results.forEach { result ->
            assertTrue("Each operation should succeed", result.isSuccess)
        }
        
        // Counter should be incremented 5 times
        assertEquals("Counter should be 5", 5, counter.get())
    }
    
    @Test
    fun `test operation statistics`() = runTest {
        val initialStats = operationQueueManager.getQueueStatistics()
        
        // Execute successful operation
        operationQueueManager.queueOperation(
            operation = { "success" },
            operationType = OperationType.READ
        )
        
        // Execute failing operation
        operationQueueManager.queueOperation(
            operation = { throw RuntimeException("Test failure") },
            operationType = OperationType.WRITE
        )
        
        val finalStats = operationQueueManager.getQueueStatistics()
        
        assertEquals("Total operations should increase by 2", initialStats.totalOperations + 2, finalStats.totalOperations)
        assertEquals("Successful operations should increase by 1", initialStats.successfulOperations + 1, finalStats.successfulOperations)
        assertEquals("Failed operations should increase by 1", initialStats.failedOperations + 1, finalStats.failedOperations)
    }
    
    @Test
    fun `test active operations tracking`() = runTest {
        val initialActive = operationQueueManager.getActiveOperations().size
        
        // Start an operation but don't complete it immediately
        val deferred = CompletableDeferred<Unit>()
        
        val job = async {
            operationQueueManager.queueOperation(
                operation = {
                    deferred.await() // Wait for external signal
                    "completed"
                },
                operationType = OperationType.READ,
                resourceId = "tracking_test"
            )
        }
        
        // Check that operation is active
        val activeDuring = operationQueueManager.getActiveOperations()
        assertTrue("Should have active operation", activeDuring.isNotEmpty())
        
        // Complete the operation
        deferred.complete(Unit)
        job.join()
        
        // Check that operation is no longer active
        val finalActive = operationQueueManager.getActiveOperations()
        assertEquals("Should have no active operations", 0, finalActive.size)
    }
    
    @Test
    fun `test deadlock detection`() = runTest {
        // Start a long-running operation
        val deferred = CompletableDeferred<Unit>()
        
        val job = async {
            operationQueueManager.queueOperation(
                operation = {
                    deferred.await() // Keep operation running
                    "completed"
                },
                operationType = OperationType.WRITE,
                resourceId = "deadlock_test"
            )
        }
        
        // Verify operation is active
        assertTrue("Should have active operation", operationQueueManager.getActiveOperations().isNotEmpty())
        
        // Trigger deadlock detection
        operationQueueManager.checkForDeadlocks()
        
        // Complete the deferred to avoid hanging
        deferred.complete(Unit)
        job.join()
        
        // Verify operation is cleaned up
        assertEquals("Should have no active operations after deadlock check", 0, operationQueueManager.getActiveOperations().size)
    }
    
    @Test
    fun `test emergency cleanup`() = runTest {
        // Start some operations
        val deferred = CompletableDeferred<Unit>()
        
        val job = async {
            operationQueueManager.queueOperation(
                operation = {
                    deferred.await() // Keep operation open
                    "completed"
                },
                operationType = OperationType.READ,
                resourceId = "cleanup_test"
            )
        }
        
        // Verify operation is active
        assertTrue("Should have active operation", operationQueueManager.getActiveOperations().isNotEmpty())
        
        // Perform emergency cleanup
        operationQueueManager.cancelAllOperations()
        
        // Complete the deferred to avoid hanging
        deferred.complete(Unit)
        job.join()
        
        // Verify all operations are cleaned up
        assertEquals("Should have no active operations after cleanup", 0, operationQueueManager.getActiveOperations().size)
    }
    
    @Test
    fun `test different operation types`() = runTest {
        val readResult = operationQueueManager.queueOperation(
            operation = { "read_result" },
            operationType = OperationType.READ
        )
        
        val writeResult = operationQueueManager.queueOperation(
            operation = { "write_result" },
            operationType = OperationType.WRITE
        )
        
        val updateResult = operationQueueManager.queueOperation(
            operation = { "update_result" },
            operationType = OperationType.UPDATE
        )
        
        val deleteResult = operationQueueManager.queueOperation(
            operation = { "delete_result" },
            operationType = OperationType.DELETE
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
    fun `test resource-specific queuing`() = runTest {
        val resource1Results = mutableListOf<Result<String>>()
        val resource2Results = mutableListOf<Result<String>>()
        
        // Queue operations on different resources
        val resource1Jobs = (1..3).map { index ->
            async {
                operationQueueManager.queueOperation(
                    operation = { "resource1_result_$index" },
                    operationType = OperationType.READ,
                    resourceId = "resource1"
                )
            }
        }
        
        val resource2Jobs = (1..3).map { index ->
            async {
                operationQueueManager.queueOperation(
                    operation = { "resource2_result_$index" },
                    operationType = OperationType.READ,
                    resourceId = "resource2"
                )
            }
        }
        
        // Collect results
        resource1Jobs.forEach { job ->
            resource1Results.add(job.await())
        }
        
        resource2Jobs.forEach { job ->
            resource2Results.add(job.await())
        }
        
        // All operations should succeed
        resource1Results.forEach { result ->
            assertTrue("Resource1 operations should succeed", result.isSuccess)
        }
        
        resource2Results.forEach { result ->
            assertTrue("Resource2 operations should succeed", result.isSuccess)
        }
        
        // Verify results are correct
        assertEquals("Resource1 should have 3 results", 3, resource1Results.size)
        assertEquals("Resource2 should have 3 results", 3, resource2Results.size)
    }
} 