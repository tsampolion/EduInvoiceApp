package gr.eduinvoice.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.awaitClose
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages automatic retry mechanisms with exponential backoff and smart retry logic
 */
@Singleton
class RetryManager @Inject constructor() {

    private val activeRetries = mutableMapOf<String, Job>()
    private val retryCounters = mutableMapOf<String, AtomicInteger>()

    /**
     * Executes an operation with automatic retry logic
     */
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        maxRetries: Int = 3,
        retryId: String = "default",
        shouldRetry: (Throwable) -> Boolean = { true },
        onRetry: (Throwable, Int) -> Unit = { _, _ -> }
    ): Result<T> {
        var lastError: Throwable? = null
        val counter = retryCounters.getOrPut(retryId) { AtomicInteger(0) }

        repeat(maxRetries + 1) { attempt ->
            try {
                val result = operation()
                counter.set(0) // Reset counter on success
                return Result.success(result)
            } catch (error: Throwable) {
                lastError = error

                if (attempt == maxRetries || !shouldRetry(error)) {
                    return Result.failure(error)
                }

                onRetry(error, attempt + 1)
                counter.incrementAndGet()

                val delayTime = calculateRetryDelay(error, attempt)
                kotlinx.coroutines.delay(delayTime)
            }
        }

        return Result.failure(lastError ?: Exception("Unknown error"))
    }

    /**
     * Executes an operation with retry and returns a Flow of results
     */
    fun <T> executeWithRetryFlow(
        operation: suspend () -> T,
        maxRetries: Int = 3,
        retryId: String = "default",
        shouldRetry: (Throwable) -> Boolean = { true }
    ): Flow<RetryResult<T>> = callbackFlow {
        trySend(RetryResult.Loading)

        // Launch the operation in a coroutine
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = executeWithRetry(
                    operation = operation,
                    maxRetries = maxRetries,
                    retryId = retryId,
                    shouldRetry = shouldRetry,
                    onRetry = { error, attempt ->
                        trySend(RetryResult.Retrying(error, attempt))
                    }
                )

                trySend(
                    if (result.isSuccess) {
                        RetryResult.Success(result.getOrNull()!!)
                    } else {
                        RetryResult.Error(result.exceptionOrNull()!!)
                    }
                )
            } catch (e: Exception) {
                trySend(RetryResult.Error(e))
            } finally {
                close()
            }
        }

        awaitClose { job.cancel() }
    }

    /**
     * Executes multiple operations with retry and returns when all succeed or max failures reached
     */
    suspend fun <T> executeAllWithRetry(
        operations: List<suspend () -> T>,
        maxRetries: Int = 3,
        maxFailures: Int = operations.size,
        shouldRetry: (Throwable) -> Boolean = { true }
    ): List<Result<T>> {
        val results = mutableListOf<Result<T>>()
        var failureCount = 0

        operations.forEachIndexed { index, operation ->
            if (failureCount >= maxFailures) {
                results.add(Result.failure(Exception("Max failures reached")))
                return@forEachIndexed
            }

            val result = executeWithRetry(
                operation = operation,
                maxRetries = maxRetries,
                retryId = "operation_$index",
                shouldRetry = shouldRetry
            )

            results.add(result)
            if (result.isFailure) {
                failureCount++
            }
        }

        return results
    }

    /**
     * Executes operations in parallel with retry
     */
    suspend fun <T> executeParallelWithRetry(
        operations: List<suspend () -> T>,
        maxRetries: Int = 3,
        maxConcurrency: Int = 4,
        shouldRetry: (Throwable) -> Boolean = { true }
    ): List<Result<T>> = coroutineScope {
        operations.map { operation ->
            async {
                executeWithRetry(
                    operation = operation,
                    maxRetries = maxRetries,
                    retryId = "parallel_${System.currentTimeMillis()}",
                    shouldRetry = shouldRetry
                )
            }
        }.awaitAll()
    }

    /**
     * Cancels all active retries for a specific retry ID
     */
    fun cancelRetry(retryId: String) {
        activeRetries[retryId]?.cancel()
        activeRetries.remove(retryId)
        retryCounters.remove(retryId)
    }

    /**
     * Cancels all active retries
     */
    fun cancelAllRetries() {
        activeRetries.values.forEach { it.cancel() }
        activeRetries.clear()
        retryCounters.clear()
    }

    /**
     * Gets retry statistics for a specific retry ID
     */
    fun getRetryCount(retryId: String): Int {
        return retryCounters[retryId]?.get() ?: 0
    }

    /**
     * Calculates retry delay with exponential backoff and jitter
     */
    private fun calculateRetryDelay(error: Throwable, attempt: Int): Long {
        val baseDelay = when (error) {
            is java.net.SocketTimeoutException -> 1000L
            is java.net.UnknownHostException -> 2000L
            is java.io.IOException -> 1500L
            else -> 3000L
        }

        // Exponential backoff: baseDelay * 2^attempt
        val exponentialDelay = baseDelay * (1L shl attempt)

        // Add jitter (±25% random variation)
        val jitter = (exponentialDelay * 0.25 * (Math.random() - 0.5)).toLong()

        return (exponentialDelay + jitter).coerceAtMost(30000L) // Max 30 seconds
    }

    /**
     * Creates a retry policy with custom configuration
     */
    fun createRetryPolicy(
        maxRetries: Int = 3,
        baseDelay: Long = 1000L,
        maxDelay: Long = 30000L,
        shouldRetry: (Throwable) -> Boolean = { true }
    ): RetryPolicy {
        return RetryPolicy(
            maxRetries = maxRetries,
            baseDelay = baseDelay,
            maxDelay = maxDelay,
            shouldRetry = shouldRetry
        )
    }
}

/**
 * Retry policy configuration
 */
data class RetryPolicy(
    val maxRetries: Int,
    val baseDelay: Long,
    val maxDelay: Long,
    val shouldRetry: (Throwable) -> Boolean
)

/**
 * Result of retry operation
 */
sealed class RetryResult<out T> {
    object Loading : RetryResult<Nothing>()
    data class Retrying(val error: Throwable, val attempt: Int) : RetryResult<Nothing>()
    data class Success<T>(val data: T) : RetryResult<T>()
    data class Error(val error: Throwable) : RetryResult<Nothing>()
}

/**
 * Extension function for easy retry usage
 */
suspend fun <T> retry(
    maxRetries: Int = 3,
    operation: suspend () -> T
): Result<T> {
    val retryManager = RetryManager()
    return retryManager.executeWithRetry(
        operation = operation,
        maxRetries = maxRetries
    )
}

/**
 * Extension function for retry with custom retry logic
 */
suspend fun <T> retry(
    maxRetries: Int = 3,
    shouldRetry: (Throwable) -> Boolean,
    operation: suspend () -> T
): Result<T> {
    val retryManager = RetryManager()
    return retryManager.executeWithRetry(
        operation = operation,
        maxRetries = maxRetries,
        shouldRetry = shouldRetry
    )
}
