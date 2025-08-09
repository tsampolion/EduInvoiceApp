package gr.eduinvoice.test.support.fakes

import gr.eduinvoice.data.concurrency.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * No-operation implementation of ConcurrencyController for testing
 * Provides immediate execution without any concurrency control or transaction management
 */
class NoopConcurrencyController : ConcurrencyController {
    
    private val _concurrencyStats = MutableStateFlow(ConcurrencyStats())
    override val concurrencyStats: StateFlow<ConcurrencyStats> = _concurrencyStats.asStateFlow()

    override suspend fun <T> executeSafeOperation(
        operation: suspend () -> T,
        operationType: OperationType,
        resourceId: String?,
        priority: OperationPriority,
        useTransaction: Boolean,
        isolationLevel: TransactionIsolationLevel
    ): Result<T> {
        return try {
            val result = operation()
            Result.success(result)
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    override suspend fun <T> executeReadOnlyOperation(
        operation: suspend () -> T,
        resourceId: String?
    ): Result<T> {
        return try {
            val result = operation()
            Result.success(result)
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    override suspend fun executeBatchOperation(
        operations: List<suspend () -> Unit>,
        operationType: OperationType,
        resourceId: String?,
        priority: OperationPriority
    ): Result<Unit> {
        return try {
            operations.forEach { it() }
            Result.success(Unit)
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    override suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        maxRetries: Int,
        retryDelayMs: Long,
        operationType: OperationType,
        resourceId: String?
    ): Result<T> {
        var lastError: Throwable? = null
        
        repeat(maxRetries) { attempt ->
            try {
                val result = operation()
                return Result.success(result)
            } catch (error: Throwable) {
                lastError = error
                if (attempt < maxRetries - 1) {
                    kotlinx.coroutines.delay(retryDelayMs)
                }
            }
        }
        
        return Result.failure(lastError ?: Exception("Operation failed after $maxRetries retries"))
    }

    override suspend fun acquireLock(resourceId: String, timeoutMs: Long): Boolean {
        return true // Always succeed
    }

    override suspend fun releaseLock(resourceId: String) {
        // No-op
    }

    override suspend fun isLocked(resourceId: String): Boolean {
        return false // Never locked
    }

    override suspend fun getHealthCheck(): HealthCheckResult {
        return HealthCheckResult(
            isHealthy = true,
            activeOperations = 0,
            queuedOperations = 0,
            averageResponseTimeMs = 0L,
            errorRate = 0.0,
            lastError = null
        )
    }

    override suspend fun shutdown() {
        // No-op
    }

    override suspend fun reset() {
        _concurrencyStats.value = ConcurrencyStats()
    }

    companion object {
        /**
         * Creates a new NoopConcurrencyController instance
         */
        fun create(): NoopConcurrencyController = NoopConcurrencyController()
    }
}
