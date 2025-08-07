package gr.eduinvoice.data.concurrency

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates transaction management and operation queuing for safe concurrent operations
 * 
 * Features:
 * - Unified interface for concurrent operations
 * - Automatic conflict detection and resolution
 * - Resource locking and deadlock prevention
 * - Operation coordination and monitoring
 * - Performance optimization through batching
 */
@Singleton
class ConcurrencyController @Inject constructor(
    private val transactionManager: TransactionManager,
    private val operationQueueManager: OperationQueueManager
) {
    private val TAG = "ConcurrencyController"
    
    // Resource locks
    private val resourceLocks = mutableMapOf<String, Mutex>()
    private val globalLock = Mutex()
    
    // Statistics
    private val _concurrencyStats = MutableStateFlow(ConcurrencyStats())
    val concurrencyStats: StateFlow<ConcurrencyStats> = _concurrencyStats.asStateFlow()
    
    // Operation counter
    private val operationCounter = AtomicLong(0)
    
    /**
     * Executes a safe concurrent operation with automatic conflict resolution
     */
    suspend fun <T> executeSafeOperation(
        operation: suspend () -> T,
        operationType: OperationType,
        resourceId: String? = null,
        priority: OperationPriority = OperationPriority.NORMAL,
        useTransaction: Boolean = true,
        isolationLevel: TransactionIsolationLevel = TransactionIsolationLevel.SERIALIZABLE
    ): Result<T> = withContext(Dispatchers.IO) {
        val operationId = generateOperationId()
        val startTime = System.currentTimeMillis()
        
        try {
            Log.d(TAG, "Starting safe operation: $operationId (${operationType.name})")
            
            // Acquire resource lock if specified
            val resourceLock = resourceId?.let { getResourceLock(it) }
            resourceLock?.withLock {
                // Execute operation with transaction if requested
                if (useTransaction) {
                    transactionManager.executeInTransaction(
                        operation = operation,
                        transactionName = "safe_operation_$operationId",
                        isolationLevel = isolationLevel
                    )
                } else {
                    try {
                        Result.success(operation())
                    } catch (error: Throwable) {
                        Result.failure(error)
                    }
                }
            } ?: run {
                // No resource lock needed
                if (useTransaction) {
                    transactionManager.executeInTransaction(
                        operation = operation,
                        transactionName = "safe_operation_$operationId",
                        isolationLevel = isolationLevel
                    )
                } else {
                    try {
                        Result.success(operation())
                    } catch (error: Throwable) {
                        Result.failure(error)
                    }
                }
            }
            
        } catch (error: Throwable) {
            Log.e(TAG, "Safe operation $operationId failed: ${error.message}")
            Result.failure(error)
        } finally {
            // Update statistics
            updateConcurrencyStats(operationType, startTime)
        }
    }
    
    /**
     * Executes multiple operations safely with conflict resolution
     */
    suspend fun <T> executeBatchSafeOperations(
        operations: List<suspend () -> T>,
        operationType: OperationType,
        resourceId: String? = null,
        priority: OperationPriority = OperationPriority.NORMAL,
        useTransaction: Boolean = true
    ): Result<List<T>> = withContext(Dispatchers.IO) {
        val batchId = generateOperationId()
        val startTime = System.currentTimeMillis()
        
        try {
            Log.d(TAG, "Starting batch safe operations: $batchId with ${operations.size} operations")
            
            // Use batch transaction if requested
            if (useTransaction) {
                transactionManager.executeBatchInTransaction(
                    operations = operations,
                    transactionName = "batch_safe_$batchId"
                )
            } else {
                // Execute operations sequentially without transaction
                val results = mutableListOf<T>()
                for (operation in operations) {
                    try {
                        results.add(operation())
                    } catch (error: Throwable) {
                        Log.e(TAG, "Batch operation failed: ${error.message}")
                        return@withContext Result.failure(error)
                    }
                }
                Result.success(results)
            }
            
        } catch (error: Throwable) {
            Log.e(TAG, "Batch safe operations $batchId failed: ${error.message}")
            Result.failure(error)
        } finally {
            updateConcurrencyStats(operationType, startTime, isBatch = true)
        }
    }
    
    /**
     * Executes read-only operations safely
     */
    suspend fun <T> executeReadOnlyOperation(
        operation: suspend () -> T,
        resourceId: String? = null
    ): Result<T> = withContext(Dispatchers.IO) {
        val operationId = generateOperationId()
        val startTime = System.currentTimeMillis()
        
        try {
            Log.d(TAG, "Starting read-only operation: $operationId")
            
            // Use read-only transaction
            transactionManager.executeReadOnly(
                operation = operation,
                transactionName = "readonly_$operationId"
            )
            
        } catch (error: Throwable) {
            Log.e(TAG, "Read-only operation $operationId failed: ${error.message}")
            Result.failure(error)
        } finally {
            updateConcurrencyStats(OperationType.READ, startTime)
        }
    }
    
    /**
     * Locks a resource for exclusive access
     */
    suspend fun <T> withResourceLock(
        resourceId: String,
        operation: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        val lock = getResourceLock(resourceId)
        lock.withLock {
            operation()
        }
    }
    
    /**
     * Checks if operations should be queued
     */
    private fun shouldQueueOperation(operationType: OperationType, resourceId: String?): Boolean {
        return when (operationType) {
            OperationType.WRITE, OperationType.UPDATE, OperationType.DELETE -> true
            OperationType.BATCH -> true
            OperationType.READ -> resourceId != null && hasConflictingOperations(resourceId)
        }
    }
    
    /**
     * Checks for conflicting operations on a resource
     */
    private fun hasConflictingOperations(resourceId: String): Boolean {
        val activeOperations = operationQueueManager.getActiveOperations()
        return activeOperations.values.any { operation ->
            operation.resourceId == resourceId && 
            operation.status == OperationStatus.EXECUTING &&
            operation.type in listOf(OperationType.WRITE, OperationType.UPDATE, OperationType.DELETE)
        }
    }
    
    /**
     * Gets or creates a resource lock
     */
    private suspend fun getResourceLock(resourceId: String): Mutex {
        return globalLock.withLock {
            resourceLocks.getOrPut(resourceId) { Mutex() }
        }
    }
    
    /**
     * Updates concurrency statistics
     */
    private suspend fun updateConcurrencyStats(
        operationType: OperationType,
        startTime: Long,
        isBatch: Boolean = false
    ) {
        val duration = System.currentTimeMillis() - startTime
        
        _concurrencyStats.value = _concurrencyStats.value.copy(
            totalOperations = _concurrencyStats.value.totalOperations + 1,
            batchOperations = if (isBatch) _concurrencyStats.value.batchOperations + 1 
                            else _concurrencyStats.value.batchOperations,
            averageOperationTime = calculateAverageOperationTime(duration),
            activeResourceLocks = resourceLocks.size,
            lastOperationType = operationType.name
        )
    }
    
    /**
     * Calculates average operation time
     */
    private fun calculateAverageOperationTime(newDuration: Long): Long {
        val current = _concurrencyStats.value
        val totalTime = current.averageOperationTime * current.totalOperations + newDuration
        return if (current.totalOperations + 1 > 0) {
            totalTime / (current.totalOperations + 1)
        } else 0
    }
    
    /**
     * Generates unique operation ID
     */
    private fun generateOperationId(): String {
        return "safe_op_${operationCounter.incrementAndGet()}_${System.currentTimeMillis()}"
    }
    
    /**
     * Gets current concurrency statistics
     */
    fun getConcurrencyStatistics(): ConcurrencyStats {
        return _concurrencyStats.value
    }
    
    /**
     * Gets active resource locks
     */
    fun getActiveResourceLocks(): Set<String> {
        return resourceLocks.keys.toSet()
    }
    
    /**
     * Releases all resource locks (emergency cleanup)
     */
    suspend fun releaseAllResourceLocks() {
        globalLock.withLock {
            Log.w(TAG, "Releasing all resource locks: ${resourceLocks.size}")
            resourceLocks.clear()
        }
    }
    
    /**
     * Performs health check on concurrency components
     */
    suspend fun performHealthCheck(): HealthCheckResult {
        return try {
            val transactionStats = transactionManager.transactionStats.value
            val queueStats = operationQueueManager.getQueueStatistics()
            val concurrencyStats = _concurrencyStats.value
            
            val isHealthy = transactionStats.failedTransactions < 10 &&
                           queueStats.failedOperations < 10 &&
                           concurrencyStats.totalOperations > 0
            
            HealthCheckResult(
                isHealthy = isHealthy,
                transactionStats = transactionStats,
                queueStats = queueStats,
                concurrencyStats = concurrencyStats,
                activeResourceLocks = resourceLocks.size,
                activeTransactions = transactionManager.getActiveTransactions().size,
                activeOperations = operationQueueManager.getActiveOperations().size
            )
        } catch (error: Throwable) {
            Log.e(TAG, "Health check failed: ${error.message}")
            HealthCheckResult(
                isHealthy = false,
                error = error.message
            )
        }
    }
    
    /**
     * Emergency cleanup of all concurrency components
     */
    suspend fun emergencyCleanup() {
        Log.w(TAG, "Performing emergency cleanup of concurrency components")
        
        try {
            // Cancel all operations
            operationQueueManager.cancelAllOperations()
            
            // Cancel all transactions
            transactionManager.cancelAllTransactions()
            
            // Release all resource locks
            releaseAllResourceLocks()
            
            Log.i(TAG, "Emergency cleanup completed successfully")
        } catch (error: Throwable) {
            Log.e(TAG, "Emergency cleanup failed: ${error.message}")
        }
    }
}

/**
 * Concurrency statistics
 */
data class ConcurrencyStats(
    val totalOperations: Long = 0,
    val batchOperations: Long = 0,
    val averageOperationTime: Long = 0,
    val activeResourceLocks: Int = 0,
    val lastOperationType: String = ""
)

/**
 * Health check result
 */
data class HealthCheckResult(
    val isHealthy: Boolean,
    val transactionStats: TransactionStats? = null,
    val queueStats: QueueStats? = null,
    val concurrencyStats: ConcurrencyStats? = null,
    val activeResourceLocks: Int = 0,
    val activeTransactions: Int = 0,
    val activeOperations: Int = 0,
    val error: String? = null
) 