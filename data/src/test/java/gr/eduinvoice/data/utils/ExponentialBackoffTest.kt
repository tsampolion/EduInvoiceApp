package gr.eduinvoice.data.utils

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException
import kotlin.Result

class ExponentialBackoffTest {

    @Test
    fun testCalculateDelay_FirstAttempt_ReturnsBaseDelay() {
        // Given
        val backoff = ExponentialBackoff(baseDelayMs = 1000L)

        // When
        val delay = backoff.calculateDelay(1)

        // Then
        assertEquals(1000L, delay)
    }

    @Test
    fun testCalculateDelay_SecondAttempt_ReturnsExponentialDelay() {
        // Given
        val backoff = ExponentialBackoff(baseDelayMs = 1000L)

        // When
        val delay = backoff.calculateDelay(2)

        // Then
        assertEquals(2000L, delay)
    }

    @Test
    fun testCalculateDelay_ThirdAttempt_ReturnsExponentialDelay() {
        // Given
        val backoff = ExponentialBackoff(baseDelayMs = 1000L)

        // When
        val delay = backoff.calculateDelay(3)

        // Then
        assertEquals(4000L, delay)
    }

    @Test
    fun testCalculateDelay_ExceedsMaxDelay_ReturnsMaxDelay() {
        // Given
        val backoff = ExponentialBackoff(baseDelayMs = 1000L, maxDelayMs = 3000L)

        // When
        val delay = backoff.calculateDelay(5)

        // Then
        assertEquals(3000L, delay)
    }

    @Test
    fun testCalculateDelay_WithJitter_ReturnsDelayWithinRange() {
        // Given
        val backoff = ExponentialBackoff(baseDelayMs = 1000L, jitterFactor = 0.1)

        // When
        val delay = backoff.calculateDelay(2)

        // Then
        assertTrue(delay >= 1800L) // 2000 * 0.9
        assertTrue(delay <= 2200L) // 2000 * 1.1
    }

    @Test
    fun testShouldRetry_TransientError_ReturnsTrue() {
        // Given
        val backoff = ExponentialBackoff()
        val transientError = SocketTimeoutException("Connection timed out")

        // When
        val shouldRetry = backoff.shouldRetry(1, transientError)

        // Then
        assertTrue(shouldRetry)
    }

    @Test
    fun testShouldRetry_PermanentError_ReturnsFalse() {
        // Given
        val backoff = ExponentialBackoff()
        val permanentError = IOException("Permission denied")

        // When
        val shouldRetry = backoff.shouldRetry(1, permanentError)

        // Then
        assertFalse(shouldRetry)
    }

    @Test
    fun testShouldRetry_MaxRetriesExceeded_ReturnsFalse() {
        // Given
        val backoff = ExponentialBackoff(maxRetries = 2)
        val transientError = SocketTimeoutException("Connection timed out")

        // When
        val shouldRetry = backoff.shouldRetry(3, transientError)

        // Then
        assertFalse(shouldRetry)
    }

    @Test
    fun testShouldRetry_UnknownHostException_ReturnsTrue() {
        // Given
        val backoff = ExponentialBackoff()
        val error = java.net.UnknownHostException("Host not found")

        // When
        val shouldRetry = backoff.shouldRetry(1, error)

        // Then
        assertTrue(shouldRetry)
    }

    @Test
    fun testShouldRetry_ConnectException_ReturnsTrue() {
        // Given
        val backoff = ExponentialBackoff()
        val error = java.net.ConnectException("Connection refused")

        // When
        val shouldRetry = backoff.shouldRetry(1, error)

        // Then
        assertTrue(shouldRetry)
    }

    @Test
    fun testShouldRetry_SocketException_ReturnsTrue() {
        // Given
        val backoff = ExponentialBackoff()
        val error = java.net.SocketException("Socket closed")

        // When
        val shouldRetry = backoff.shouldRetry(1, error)

        // Then
        assertTrue(shouldRetry)
    }

    @Test
    fun testShouldRetry_SSLException_ReturnsTrue() {
        // Given
        val backoff = ExponentialBackoff()
        val error = SSLException("SSL handshake failed")

        // When
        val shouldRetry = backoff.shouldRetry(1, error)

        // Then
        assertTrue(shouldRetry)
    }

    @Test
    fun testShouldRetry_SSLExceptionWithCertificate_ReturnsFalse() {
        // Given
        val backoff = ExponentialBackoff()
        val error = SSLException("Certificate validation failed")

        // When
        val shouldRetry = backoff.shouldRetry(1, error)

        // Then
        assertFalse(shouldRetry)
    }

    @Test
    fun testExecuteWithRetry_SuccessfulOperation_ReturnsSuccess() = runTest {
        // Given
        val backoff = ExponentialBackoff()
        var attempts = 0

        // When
        val result = backoff.executeWithRetry(operation = suspend {
            attempts++
            "success"
        })

        // Then
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(1, attempts)
    }

    @Test
    fun testExecuteWithRetry_TransientErrorThenSuccess_ReturnsSuccess() = runTest {
        // Given
        val backoff = ExponentialBackoff(baseDelayMs = 10L, maxRetries = 2)
        var attempts = 0

        // When
        val result = backoff.executeWithRetry(
            operation = suspend {
                attempts++
                if (attempts == 1) {
                    throw SocketTimeoutException("Connection timed out")
                } else {
                    "success"
                }
            }
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(2, attempts)
    }

    @Test
    fun testExecuteWithRetry_PermanentError_ReturnsFailure() = runTest {
        // Given
        val backoff = ExponentialBackoff()
        val permanentError = IOException("Permission denied")

        // When
        val result = backoff.executeWithRetry(operation = suspend {
            throw permanentError
        })

        // Then
        assertTrue(result.isFailure)
        assertEquals(permanentError, result.exceptionOrNull())
    }

    @Test
    fun testExecuteWithRetry_MaxRetriesExceeded_ReturnsFailure() = runTest {
        // Given
        val backoff = ExponentialBackoff(baseDelayMs = 10L, maxRetries = 2)
        var attempts = 0

        // When
        val result = backoff.executeWithRetry(
            operation = suspend {
                attempts++
                throw SocketTimeoutException("Connection timed out")
            }
        )

        // Then
        assertTrue(result.isFailure)
        assertEquals(3, attempts) // Initial attempt + 2 retries
    }

    @Test
    fun testExecuteWithRetry_WithCustomRetryCondition_RespectsCondition() = runTest {
        // Given
        val backoff = ExponentialBackoff(baseDelayMs = 10L, maxRetries = 5)
        var attempts = 0

        // When
        val result = backoff.executeWithRetry(
            operation = suspend {
                attempts++
                throw IOException("Network error")
            },
            shouldRetry = { _, attempt ->
                // Only retry first 2 attempts
                attempt <= 2
            }
        )

        // Then
        assertTrue(result.isFailure)
        assertEquals(3, attempts) // Initial attempt + 2 retries
    }

    @Test
    fun testExecuteWithRetry_WithOnRetryCallback_CallsCallback() = runTest {
        // Given
        val backoff = ExponentialBackoff(baseDelayMs = 10L, maxRetries = 2)
        var attempts = 0
        var callbackCalls = 0

        // When
        backoff.executeWithRetry(
            operation = suspend {
                attempts++
                throw SocketTimeoutException("Connection timed out")
            },
            onRetry = { _, _ ->
                callbackCalls++
            }
        )

        // Then
        assertEquals(3, attempts) // Initial attempt + 2 retries
        assertEquals(2, callbackCalls) // Called for each retry
    }

    @Test
    fun testCreateStrategyForErrorType_Network_ReturnsNetworkStrategy() {
        // Given
        val backoff = ExponentialBackoff()

        // When
        val networkStrategy = backoff.createStrategyForErrorType("network")

        // Then
        assertEquals(500L, networkStrategy.getRetryStats().baseDelayMs)
        assertEquals(30000L, networkStrategy.getRetryStats().maxDelayMs)
        assertEquals(3, networkStrategy.getRetryStats().maxRetries)
    }

    @Test
    fun testCreateStrategyForErrorType_Server_ReturnsServerStrategy() {
        // Given
        val backoff = ExponentialBackoff()

        // When
        val serverStrategy = backoff.createStrategyForErrorType("server")

        // Then
        assertEquals(2000L, serverStrategy.getRetryStats().baseDelayMs)
        assertEquals(60000L, serverStrategy.getRetryStats().maxDelayMs)
        assertEquals(5, serverStrategy.getRetryStats().maxRetries)
    }

    @Test
    fun testCreateStrategyForErrorType_Timeout_ReturnsTimeoutStrategy() {
        // Given
        val backoff = ExponentialBackoff()

        // When
        val timeoutStrategy = backoff.createStrategyForErrorType("timeout")

        // Then
        assertEquals(1000L, timeoutStrategy.getRetryStats().baseDelayMs)
        assertEquals(15000L, timeoutStrategy.getRetryStats().maxDelayMs)
        assertEquals(3, timeoutStrategy.getRetryStats().maxRetries)
    }

    @Test
    fun testCreateStrategyForErrorType_RateLimit_ReturnsRateLimitStrategy() {
        // Given
        val backoff = ExponentialBackoff()

        // When
        val rateLimitStrategy = backoff.createStrategyForErrorType("rate_limit")

        // Then
        assertEquals(5000L, rateLimitStrategy.getRetryStats().baseDelayMs)
        assertEquals(120000L, rateLimitStrategy.getRetryStats().maxDelayMs)
        assertEquals(3, rateLimitStrategy.getRetryStats().maxRetries)
    }

    @Test
    fun testCreateStrategyForErrorType_Unknown_ReturnsDefaultStrategy() {
        // Given
        val backoff = ExponentialBackoff(baseDelayMs = 1000L, maxDelayMs = 5000L, maxRetries = 3)

        // When
        val strategy = backoff.createStrategyForErrorType("unknown")

        // Then
        assertEquals(1000L, strategy.getRetryStats().baseDelayMs)
        assertEquals(5000L, strategy.getRetryStats().maxDelayMs)
        assertEquals(3, strategy.getRetryStats().maxRetries)
    }

    @Test
    fun testGetRetryStats_ReturnsCorrectStats() {
        // Given
        val backoff = ExponentialBackoff(
            baseDelayMs = 1000L,
            maxDelayMs = 5000L,
            maxRetries = 3,
            jitterFactor = 0.1
        )

        // When
        val stats = backoff.getRetryStats()

        // Then
        assertEquals(1000L, stats.baseDelayMs)
        assertEquals(5000L, stats.maxDelayMs)
        assertEquals(3, stats.maxRetries)
        assertEquals(0.1, stats.jitterFactor, 0.001)
    }
}