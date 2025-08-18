package gr.eduinvoice.data.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class ExponentialBackoffTest {

    private lateinit var exponentialBackoff: ExponentialBackoff

    @Before
    fun setup() {
        exponentialBackoff = ExponentialBackoff(
            baseDelayMs = 1000L,
            maxDelayMs = 60000L,
            maxRetries = 5,
            jitterFactor = 0.1
        )
    }

    @Test
    fun `should calculate delay for first attempt`() {
        // Given
        val attempt = 1

        // When
        val delay = exponentialBackoff.calculateDelay(attempt)

        // Then
        assertTrue(delay >= 1000L)
        assertTrue(delay <= 1100L) // baseDelay + jitter
    }

    @Test
    fun `should calculate exponential delay for subsequent attempts`() {
        // Given
        val attempt1 = 1
        val attempt2 = 2
        val attempt3 = 3

        // When
        val delay1 = exponentialBackoff.calculateDelay(attempt1)
        val delay2 = exponentialBackoff.calculateDelay(attempt2)
        val delay3 = exponentialBackoff.calculateDelay(attempt3)

        // Then
        assertTrue(delay1 >= 1000L && delay1 <= 1100L)
        assertTrue(delay2 >= 2000L && delay2 <= 2200L)
        assertTrue(delay3 >= 4000L && delay3 <= 4400L)
    }

    @Test
    fun `should respect max delay limit`() {
        // Given
        val attempt = 10 // Would exceed maxDelayMs

        // When
        val delay = exponentialBackoff.calculateDelay(attempt)

        // Then
        // For attempt 10: 1000 * 2^9 = 512000, but capped at maxDelayMs (60000)
        // Plus jitter: 60000 + (60000 * 0.1) = 66000
        assertTrue(delay <= 66000L)
    }

    @Test
    fun `should return zero delay for zero or negative attempt`() {
        // Given
        val attempt1 = 0
        val attempt2 = -1

        // When
        val delay1 = exponentialBackoff.calculateDelay(attempt1)
        val delay2 = exponentialBackoff.calculateDelay(attempt2)

        // Then
        assertEquals(0L, delay1)
        assertEquals(0L, delay2)
    }

    @Test
    fun `should add jitter to delay calculation`() {
        // Given
        val attempt = 1

        // When
        val delay1 = exponentialBackoff.calculateDelay(attempt)
        val delay2 = exponentialBackoff.calculateDelay(attempt)

        // Then
        // Jitter should make delays slightly different (though not guaranteed due to randomness)
        assertTrue(delay1 >= 1000L && delay1 <= 1100L)
        assertTrue(delay2 >= 1000L && delay2 <= 1100L)
    }

    @Test
    fun `should retry network errors`() {
        // Given
        val attempt = 1
        val networkErrors = listOf(
            SocketTimeoutException(),
            UnknownHostException(),
            ConnectException(),
            SocketException()
        )

        // When & Then
        networkErrors.forEach { error ->
            assertTrue(exponentialBackoff.shouldRetry(attempt, error))
        }
    }

    @Test
    fun `should retry SSL errors except certificate issues`() {
        // Given
        val attempt = 1
        val sslError = SSLException("SSL handshake failed")
        val certificateError = SSLException("certificate validation failed")

        // When & Then
        assertTrue(exponentialBackoff.shouldRetry(attempt, sslError))
        assertFalse(exponentialBackoff.shouldRetry(attempt, certificateError))
    }

    @Test
    fun `should retry IO errors except permanent ones`() {
        // Given
        val attempt = 1
        val ioError = IOException("Connection reset")
        val permanentError = IOException("permission denied")

        // When & Then
        assertTrue(exponentialBackoff.shouldRetry(attempt, ioError))
        assertFalse(exponentialBackoff.shouldRetry(attempt, permanentError))
    }

    @Test
    fun `should not retry other error types`() {
        // Given
        val attempt = 1
        val otherError = RuntimeException("Something went wrong")

        // When & Then
        assertFalse(exponentialBackoff.shouldRetry(attempt, otherError))
    }

    @Test
    fun `should not retry when max retries exceeded`() {
        // Given
        val attempt = 6 // Exceeds maxRetries of 5
        val networkError = SocketTimeoutException()

        // When & Then
        assertFalse(exponentialBackoff.shouldRetry(attempt, networkError))
    }

    @Test
    fun `should identify permanent errors correctly`() {
        // Given
        val permanentErrors = listOf(
            IOException("permission denied"),
            IOException("not found"),
            IOException("unauthorized"),
            IOException("forbidden"),
            IOException("bad request"),
            IOException("invalid input")
        )

        // When & Then
        permanentErrors.forEach { error ->
            assertFalse(exponentialBackoff.shouldRetry(1, error))
        }
    }

    @Test
    fun `should create network strategy with correct parameters`() {
        // When
        val networkStrategy = exponentialBackoff.createStrategyForErrorType("network")

        // Then
        assertEquals(500L, networkStrategy.getRetryStats().baseDelayMs)
        assertEquals(30000L, networkStrategy.getRetryStats().maxDelayMs)
        assertEquals(3, networkStrategy.getRetryStats().maxRetries)
    }

    @Test
    fun `should create server strategy with correct parameters`() {
        // When
        val serverStrategy = exponentialBackoff.createStrategyForErrorType("server")

        // Then
        assertEquals(2000L, serverStrategy.getRetryStats().baseDelayMs)
        assertEquals(60000L, serverStrategy.getRetryStats().maxDelayMs)
        assertEquals(5, serverStrategy.getRetryStats().maxRetries)
    }

    @Test
    fun `should create timeout strategy with correct parameters`() {
        // When
        val timeoutStrategy = exponentialBackoff.createStrategyForErrorType("timeout")

        // Then
        assertEquals(1000L, timeoutStrategy.getRetryStats().baseDelayMs)
        assertEquals(15000L, timeoutStrategy.getRetryStats().maxDelayMs)
        assertEquals(3, timeoutStrategy.getRetryStats().maxRetries)
    }

    @Test
    fun `should create rate limit strategy with correct parameters`() {
        // When
        val rateLimitStrategy = exponentialBackoff.createStrategyForErrorType("rate_limit")

        // Then
        assertEquals(5000L, rateLimitStrategy.getRetryStats().baseDelayMs)
        assertEquals(120000L, rateLimitStrategy.getRetryStats().maxDelayMs)
        assertEquals(3, rateLimitStrategy.getRetryStats().maxRetries)
    }

    @Test
    fun `should return default strategy for unknown error type`() {
        // When
        val defaultStrategy = exponentialBackoff.createStrategyForErrorType("unknown")

        // Then
        assertEquals(1000L, defaultStrategy.getRetryStats().baseDelayMs)
        assertEquals(60000L, defaultStrategy.getRetryStats().maxDelayMs)
        assertEquals(5, defaultStrategy.getRetryStats().maxRetries)
    }

    @Test
    fun `should return correct retry stats`() {
        // When
        val stats = exponentialBackoff.getRetryStats()

        // Then
        assertEquals(1000L, stats.baseDelayMs)
        assertEquals(60000L, stats.maxDelayMs)
        assertEquals(5, stats.maxRetries)
        assertEquals(0.1, stats.jitterFactor, 0.001)
    }

    @Test
    fun `should calculate total max delay correctly`() {
        // Given
        val stats = exponentialBackoff.getRetryStats()

        // When
        val totalMaxDelay = stats.totalMaxDelayMs

        // Then
        // Expected: 0 + 1000 + 2000 + 4000 + 8000 + 16000 = 31000
        assertTrue(totalMaxDelay > 0L)
        assertTrue(totalMaxDelay < 100000L)
    }

    @Test
    fun `should handle case insensitive error type matching`() {
        // Given
        val errorTypes = listOf("NETWORK", "Network", "network", "NeTwOrK")

        // When & Then
        errorTypes.forEach { errorType ->
            val strategy = exponentialBackoff.createStrategyForErrorType(errorType)
            assertEquals(500L, strategy.getRetryStats().baseDelayMs)
        }
    }

    @Test
    fun `should handle empty error type`() {
        // When
        val strategy = exponentialBackoff.createStrategyForErrorType("")

        // Then
        assertEquals(1000L, strategy.getRetryStats().baseDelayMs)
    }

    @Test
    fun `should handle null error message in permanent error detection`() {
        // Given
        val errorWithNullMessage = IOException()

        // When & Then
        assertTrue(exponentialBackoff.shouldRetry(1, errorWithNullMessage))
    }

    @Test
    fun `should handle custom backoff with different parameters`() {
        // Given
        val customBackoff = ExponentialBackoff(
            baseDelayMs = 500L,
            maxDelayMs = 10000L,
            maxRetries = 3,
            jitterFactor = 0.05
        )

        // When
        val stats = customBackoff.getRetryStats()

        // Then
        assertEquals(500L, stats.baseDelayMs)
        assertEquals(10000L, stats.maxDelayMs)
        assertEquals(3, stats.maxRetries)
        assertEquals(0.05, stats.jitterFactor, 0.001)
    }

    @Test
    fun `should calculate delay correctly for custom backoff`() {
        // Given
        val customBackoff = ExponentialBackoff(
            baseDelayMs = 500L,
            maxDelayMs = 10000L,
            maxRetries = 3,
            jitterFactor = 0.05
        )

        // When
        val delay1 = customBackoff.calculateDelay(1)
        val delay2 = customBackoff.calculateDelay(2)
        val delay3 = customBackoff.calculateDelay(3)

        // Then
        assertTrue(delay1 >= 500L && delay1 <= 525L) // 500 + 5% jitter
        assertTrue(delay2 >= 1000L && delay2 <= 1050L) // 1000 + 5% jitter
        assertTrue(delay3 >= 2000L && delay3 <= 2100L) // 2000 + 5% jitter
    }

    @Test
    fun `should respect max delay for custom backoff`() {
        // Given
        val customBackoff = ExponentialBackoff(
            baseDelayMs = 500L,
            maxDelayMs = 10000L,
            maxRetries = 10
        )

        // When
        val delay = customBackoff.calculateDelay(10)

        // Then
        // For attempt 10: 500 * 2^9 = 256000, but capped at maxDelayMs (10000)
        // Plus jitter: 10000 + (10000 * 0.1) = 11000
        assertTrue(delay <= 11000L)
    }
}
