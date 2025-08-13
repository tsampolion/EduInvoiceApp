package gr.eduinvoice.data.utils

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Exponential backoff utility for retrying failed operations with intelligent delay calculation
 */
class ExponentialBackoff(
    private val baseDelayMs: Long = 1000L,
    private val maxDelayMs: Long = 60000L,
    private val maxRetries: Int = 5,
    private val jitterFactor: Double = 0.1
) {

    /**
     * Calculate delay for a specific retry attempt
     */
    fun calculateDelay(attempt: Int): Long {
        if (attempt <= 0) return 0L

        val exponentialDelay = baseDelayMs * (1L shl (attempt - 1))
        val delayWithMax = minOf(exponentialDelay, maxDelayMs)

        // Add jitter to prevent thundering herd problem
        val jitter = (delayWithMax * jitterFactor * Random.nextDouble()).toLong()
        val finalDelay = delayWithMax + jitter

        return finalDelay
    }

    /**
     * Determine if an operation should be retried based on the error and attempt count
     */
    fun shouldRetry(attempt: Int, error: Throwable): Boolean {
        if (attempt > maxRetries) return false

        return when (error) {
            is java.net.SocketTimeoutException -> true
            is java.net.UnknownHostException -> true
            is java.net.ConnectException -> true
            is java.net.SocketException -> true
            is javax.net.ssl.SSLException -> {
                // Retry SSL errors except for certificate issues
                val message = error.message ?: ""
                !message.contains("certificate", ignoreCase = true)
            }
            is java.io.IOException -> {
                // Retry IO errors except for specific cases
                !isPermanentError(error)
            }
            else -> false
        }
    }

    /**
     * Execute an operation with exponential backoff retry logic
     */
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        onRetry: (Throwable, Int) -> Unit = { _, _ -> }
    ): Result<T> {
        var lastException: Throwable? = null

        for (attempt in 0..maxRetries) {
            try {
                val result = operation()
                return Result.success(result)
            } catch (e: Throwable) {
                lastException = e

                if (attempt == maxRetries || !shouldRetry(attempt + 1, e)) {
                    return Result.failure(e)
                }

                onRetry(e, attempt + 1)

                val delayMs = calculateDelay(attempt + 1)
                delay(delayMs)
            }
        }

        return Result.failure(lastException ?: Exception("Max retries exceeded"))
    }

    /**
     * Execute an operation with exponential backoff and custom retry condition
     */
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        shouldRetry: (Throwable, Int) -> Boolean,
        onRetry: (Throwable, Int) -> Unit = { _, _ -> }
    ): Result<T> {
        var lastException: Throwable? = null

        for (attempt in 0..maxRetries) {
            try {
                val result = operation()
                return Result.success(result)
            } catch (e: Throwable) {
                lastException = e

                if (attempt == maxRetries || !shouldRetry(e, attempt + 1)) {
                    return Result.failure(e)
                }

                onRetry(e, attempt + 1)

                val delayMs = calculateDelay(attempt + 1)
                delay(delayMs)
            }
        }

        return Result.failure(lastException ?: Exception("Max retries exceeded"))
    }

    /**
     * Check if an error is permanent and should not be retried
     */
    private fun isPermanentError(error: Throwable): Boolean {
        return when {
            error.message?.contains("permission denied", ignoreCase = true) == true -> true
            error.message?.contains("not found", ignoreCase = true) == true -> true
            error.message?.contains("unauthorized", ignoreCase = true) == true -> true
            error.message?.contains("forbidden", ignoreCase = true) == true -> true
            error.message?.contains("bad request", ignoreCase = true) == true -> true
            error.message?.contains("invalid", ignoreCase = true) == true -> true
            else -> false
        }
    }

    /**
     * Create a backoff strategy for specific error types
     */
    fun createStrategyForErrorType(errorType: String): ExponentialBackoff {
        return when (errorType.lowercase()) {
            "network" -> ExponentialBackoff(
                baseDelayMs = 500L,
                maxDelayMs = 30000L,
                maxRetries = 3
            )
            "server" -> ExponentialBackoff(
                baseDelayMs = 2000L,
                maxDelayMs = 60000L,
                maxRetries = 5
            )
            "timeout" -> ExponentialBackoff(
                baseDelayMs = 1000L,
                maxDelayMs = 15000L,
                maxRetries = 3
            )
            "rate_limit" -> ExponentialBackoff(
                baseDelayMs = 5000L,
                maxDelayMs = 120000L,
                maxRetries = 3
            )
            else -> this
        }
    }

    /**
     * Get retry statistics for monitoring
     */
    fun getRetryStats(): RetryStats {
        return RetryStats(
            baseDelayMs = baseDelayMs,
            maxDelayMs = maxDelayMs,
            maxRetries = maxRetries,
            jitterFactor = jitterFactor
        )
    }
}

/**
 * Statistics about the retry configuration
 */
data class RetryStats(
    val baseDelayMs: Long,
    val maxDelayMs: Long,
    val maxRetries: Int,
    val jitterFactor: Double
) {
    val totalMaxDelayMs: Long = (0..maxRetries).sumOf { attempt ->
        val exponentialDelay = baseDelayMs * (1L shl attempt)
        minOf(exponentialDelay, maxDelayMs)
    }
}
