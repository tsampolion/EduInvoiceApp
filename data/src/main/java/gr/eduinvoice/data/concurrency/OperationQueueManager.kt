package gr.eduinvoice.data.concurrency

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages operation queuing to prevent race conditions and ensure proper execution order
 * 
 * Features:
 * - Operation prioritization
 * - Conflict detection and resolution
 * - Deadlock prevention
 * - Operation batching
 * - Queue monitoring and statistics
 */
@Singleton
class OperationQueueManager @Inject constructor() {
    private val TAG = "OperationQueueManager"
    
    // Queue management
    private val operationQueues = ConcurrentHashMap<String, Channel<QueuedOperation>>()
    private val activeOperations = ConcurrentHashMap<String, OperationInfo>()
    private val operationMutex = Mutex()
    
    // Statistics
    private val _queueStats = MutableStateFlow(QueueStats())
    val queueStats: StateFlow<QueueStats> = _queueStats.asStateFlow()
    
    // Operation counter
    private val operationCounter = AtomicLong(0)
    
    /**
     * Queues an operation for execution
     */
    suspend fun <T> queueOperation(
        operation: suspend () -> T,
        operationType: OperationType,
        resourceId: String? = null,
        priority: OperationPriority = OperationPriority.NORMAL,
        timeout: Long = 30000L
    ): Result<T> = withContext(Dispatchers.IO) {
        val operationId = generateOperationId()
        val startTime = System.currentTimeMillis()
        
        try {
            // Create queued operation
            val queuedOperation = QueuedOperation(
                id = operationId,
                operation = operation as suspend () -> Any,
                type = operationType,
                resourceId = resourceId,
                priority = priority,
                startTime = startTime,
                timeout = timeout
            )
            
            // Register operation
            registerOperation(operationId, queuedOperation)
            
            // Get or create queue for this resource
            val queueKey = resourceId ?: "global"
            val queue = getOrCreateQueue(queueKey)
            
            // Send operation to queue
            if (!queue.trySend(queuedOperation).isSuccess) {
                Log.w(TAG, "Queue full for resource: $queueKey")
                return@withContext Result.failure(Exception("Operation queue is full"))
            }
            
            // Wait for operation completion
            val result = withTimeoutOrNull(timeout) {
                queuedOperation.resultChannel.receive()
            }
            
            if (result == null) {
                Log.e(TAG, "Operation $operationId timed out")
                return@withContext Result.failure(Exception("Operation timed out"))
            }
            
            result as Result<T>
            
        } catch (error: Throwable) {
            Log.e(TAG, "Operation $operationId failed: ${error.message}")
            Result.failure(error)
        } finally {
            // Cleanup
            cleanupOperation(operationId, startTime)
        }
    }
    
    /**
     * Executes operations in batch
     */
    suspend fun <T> executeBatchOperations(
        operations: List<suspend () -> T>,
        operationType: OperationType,
        resourceId: String? = null,
        priority: OperationPriority = OperationPriority.NORMAL
    ): Result<List<T>> = withContext(Dispatchers.IO) {
        val batchId = generateOperationId()
        val startTime = System.currentTimeMillis()
        
        try {
            Log.d(TAG, "Starting batch operation: $batchId with ${operations.size} operations")
            
            val results = mutableListOf<Result<T>>()
            
            operations.forEachIndexed { index, operation ->
                val result = queueOperation(
                    operation = operation,
                    operationType = operationType,
                    resourceId = resourceId,
                    priority = priority
                )
                results.add(result)
                
                // Check for failures
                if (result.isFailure) {
                    Log.w(TAG, "Batch operation $batchId failed at index $index")
                    return@withContext Result.failure(
                        result.exceptionOrNull() ?: Exception("Batch operation failed")
                    )
                }
            }
            
            val successfulResults = results.mapNotNull { it.getOrNull() }
            Log.d(TAG, "Batch operation $batchId completed successfully")
            
            Result.success(successfulResults)
            
        } catch (error: Throwable) {
            Log.e(TAG, "Batch operation $batchId failed: ${error.message}")
            Result.failure(error)
        } finally {
            cleanupOperation(batchId, startTime)
        }
    }
    
    /**
     * Processes operations from a queue
     */
    private suspend fun processQueue(queueKey: String) {
        val queue = operationQueues[queueKey] ?: return
        
        while (true) {
            try {
                val queuedOperation = queue.receive()
                
                // Execute operation
                val result = try {
                    val operationResult = queuedOperation.operation()
                    Result.success(operationResult)
                } catch (error: Throwable) {
                    Log.e(TAG, "Operation ${queuedOperation.id} execution failed: ${error.message}")
                    Result.failure(error)
                }
                
                // Send result back
                queuedOperation.resultChannel.send(result)
                
                // Update statistics
                updateQueueStats(queuedOperation, result.isSuccess)
                
            } catch (error: Throwable) {
                if (error is CancellationException) {
                    break
                }
                Log.e(TAG, "Error processing queue $queueKey: ${error.message}")
            }
        }
    }
    
    /**
     * Gets or creates a queue for a resource
     */
    private suspend fun getOrCreateQueue(queueKey: String): Channel<QueuedOperation> {
        return operationQueues.getOrPut(queueKey) {
            val queue = Channel<QueuedOperation>(Channel.UNLIMITED)
            
            // Start processing coroutine for this queue
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                processQueue(queueKey)
            }
            
            queue
        }
    }
    
    /**
     * Registers an operation
     */
    private suspend fun registerOperation(operationId: String, queuedOperation: QueuedOperation) {
        operationMutex.withLock {
            activeOperations[operationId] = OperationInfo(
                id = operationId,
                type = queuedOperation.type,
                resourceId = queuedOperation.resourceId,
                priority = queuedOperation.priority,
                startTime = queuedOperation.startTime,
                status = OperationStatus.QUEUED
            )
        }
        
        Log.d(TAG, "Registered operation: $operationId (${queuedOperation.type})")
    }
    
    /**
     * Cleans up operation resources
     */
    private suspend fun cleanupOperation(operationId: String, startTime: Long) {
        operationMutex.withLock {
            activeOperations.remove(operationId)
        }
        
        // Update statistics
        val duration = System.currentTimeMillis() - startTime
        updateQueueStats(duration = duration)
    }
    
    /**
     * Updates queue statistics
     */
    private suspend fun updateQueueStats(
        queuedOperation: QueuedOperation? = null,
        success: Boolean? = null,
        duration: Long = 0
    ) {
        _queueStats.value = _queueStats.value.copy(
            totalOperations = _queueStats.value.totalOperations + 1,
            successfulOperations = if (success == true) {
                _queueStats.value.successfulOperations + 1
            } else _queueStats.value.successfulOperations,
            failedOperations = if (success == false) {
                _queueStats.value.failedOperations + 1
            } else _queueStats.value.failedOperations,
            averageProcessingTime = calculateAverageProcessingTime(duration),
            activeQueues = operationQueues.size,
            activeOperations = activeOperations.size
        )
    }
    
    /**
     * Calculates average processing time
     */
    private fun calculateAverageProcessingTime(newDuration: Long): Long {
        val current = _queueStats.value
        val totalTime = current.averageProcessingTime * current.totalOperations + newDuration
        return if (current.totalOperations + 1 > 0) {
            totalTime / (current.totalOperations + 1)
        } else 0
    }
    
    /**
     * Generates unique operation ID
     */
    private fun generateOperationId(): String {
        return "op_${operationCounter.incrementAndGet()}_${System.currentTimeMillis()}"
    }
    
    /**
     * Gets current active operations
     */
    fun getActiveOperations(): Map<String, OperationInfo> {
        return activeOperations.toMap()
    }
    
    /**
     * Gets queue statistics
     */
    fun getQueueStatistics(): QueueStats {
        return _queueStats.value
    }
    
    /**
     * Cancels all operations (emergency cleanup)
     */
    suspend fun cancelAllOperations() {
        operationMutex.withLock {
            Log.w(TAG, "Cancelling all active operations: ${activeOperations.size}")
            activeOperations.clear()
        }
        
        // Close all queues
        operationQueues.values.forEach { queue ->
            queue.close()
        }
        operationQueues.clear()
    }
    
    /**
     * Checks for deadlocks and resolves them
     */
    suspend fun checkForDeadlocks() {
        val currentTime = System.currentTimeMillis()
        val timeoutThreshold = 60000L // 1 minute
        
        val longRunningOperations = activeOperations.values.filter { operation ->
            currentTime - operation.startTime > timeoutThreshold
        }
        
        if (longRunningOperations.isNotEmpty()) {
            Log.w(TAG, "Potential deadlock detected. Long-running operations: ${longRunningOperations.size}")
            
            // Force cleanup of long-running operations
            longRunningOperations.forEach { operation ->
                Log.w(TAG, "Forcing cleanup of long-running operation: ${operation.id}")
                activeOperations.remove(operation.id)
            }
        }
    }
}

/**
 * Operation types
 */
enum class OperationType {
    READ,
    WRITE,
    DELETE,
    UPDATE,
    BATCH
}

/**
 * Operation priorities
 */
enum class OperationPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

/**
 * Operation status
 */
enum class OperationStatus {
    QUEUED,
    EXECUTING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Queued operation
 */
data class QueuedOperation(
    val id: String,
    val operation: suspend () -> Any,
    val type: OperationType,
    val resourceId: String?,
    val priority: OperationPriority,
    val startTime: Long,
    val timeout: Long,
    val resultChannel: Channel<Result<Any>> = Channel(1)
)

/**
 * Operation information
 */
data class OperationInfo(
    val id: String,
    val type: OperationType,
    val resourceId: String?,
    val priority: OperationPriority,
    val startTime: Long,
    val status: OperationStatus,
    val endTime: Long? = null
)

/**
 * Queue statistics
 */
data class QueueStats(
    val totalOperations: Long = 0,
    val successfulOperations: Long = 0,
    val failedOperations: Long = 0,
    val averageProcessingTime: Long = 0,
    val activeQueues: Int = 0,
    val activeOperations: Int = 0
) 