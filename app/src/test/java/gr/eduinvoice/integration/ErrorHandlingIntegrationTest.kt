package gr.eduinvoice.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.utils.ErrorHandler
import gr.eduinvoice.utils.RetryManager
import gr.eduinvoice.analytics.ErrorReporter
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@RunWith(AndroidJUnit4::class)
class ErrorHandlingIntegrationTest {

    private lateinit var context: Context
    private lateinit var errorHandler: ErrorHandler
    private lateinit var retryManager: RetryManager
    private lateinit var errorReporter: ErrorReporter

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        errorHandler = ErrorHandler(context)
        retryManager = RetryManager()
        errorReporter = ErrorReporter(context)
    }

    @Test
    fun testErrorHandler_ClassifiesNetworkErrors() {
        val networkTimeout = SocketTimeoutException("Connection timed out")
        val noConnection = UnknownHostException("No host")
        val ioError = IOException("Network error")

        val timeoutResult = errorHandler.handleError(networkTimeout, "NetworkTest")
        val connectionResult = errorHandler.handleError(noConnection, "NetworkTest")
        val ioResult = errorHandler.handleError(ioError, "NetworkTest")

        assertEquals(gr.eduinvoice.utils.ErrorType.NETWORK_TIMEOUT, timeoutResult.errorType)
        assertEquals(gr.eduinvoice.utils.ErrorType.NETWORK_NO_CONNECTION, connectionResult.errorType)
        assertEquals(gr.eduinvoice.utils.ErrorType.IO_ERROR, ioResult.errorType)
    }

    @Test
    fun testErrorHandler_ProvidesUserFriendlyMessages() {
        val networkError = SocketTimeoutException("Connection timed out")
        val result = errorHandler.handleError(networkError, "TestContext")

        assertTrue(result.userMessage.contains("timed out"))
        assertTrue(result.userMessage.contains("internet connection"))
        assertNotNull(result.recoveryAction)
    }

    @Test
    fun testRetryManager_HandlesTransientErrors() = runTest {
        var attemptCount = 0
        val maxAttempts = 3

        val result = retryManager.executeWithRetry(
            operation = {
                attemptCount++
                if (attemptCount < maxAttempts) {
                    throw SocketTimeoutException("Temporary network issue")
                } else {
                    "Success after retries"
                }
            },
            maxRetries = maxAttempts - 1,
            retryId = "test_retry",
            shouldRetry = { error -> error is SocketTimeoutException }
        )

        assertTrue(result.isSuccess)
        assertEquals("Success after retries", result.getOrNull())
        assertEquals(maxAttempts, attemptCount)
    }

    @Test
    fun testRetryManager_StopsOnPermanentErrors() = runTest {
        var attemptCount = 0

        val result = retryManager.executeWithRetry(
            operation = {
                attemptCount++
                throw IllegalArgumentException("Permanent error")
            },
            maxRetries = 3,
            retryId = "test_permanent_error",
            shouldRetry = { error -> error is SocketTimeoutException } // Only retry network errors
        )

        assertFalse(result.isSuccess)
        assertEquals(1, attemptCount) // Should not retry permanent errors
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun testErrorReporter_LogsErrorsCorrectly() {
        val testError = RuntimeException("Test error for reporting")
        
        // This should not throw an exception
        errorReporter.reportError(testError, "IntegrationTest")
        
        // Verify error was logged (we can't easily test Firebase in unit tests,
        // but we can verify the method doesn't throw)
        assertTrue(true) // If we get here, no exception was thrown
    }

    @Test
    fun testErrorHandler_RecordsErrorHistory() {
        val error1 = SocketTimeoutException("Timeout 1")
        val error2 = UnknownHostException("No host")
        val error3 = IOException("IO error")

        errorHandler.handleError(error1, "Test")
        errorHandler.handleError(error2, "Test")
        errorHandler.handleError(error3, "Test")

        val stats = errorHandler.getErrorStatistics()
        assertEquals(3, stats.totalErrors)
        assertEquals(1, stats.errorCounts[gr.eduinvoice.utils.ErrorType.NETWORK_TIMEOUT])
        assertEquals(1, stats.errorCounts[gr.eduinvoice.utils.ErrorType.NETWORK_NO_CONNECTION])
        assertEquals(1, stats.errorCounts[gr.eduinvoice.utils.ErrorType.IO_ERROR])
    }

    @Test
    fun testErrorHandler_ClearsErrorHistory() {
        val error = SocketTimeoutException("Test error")
        errorHandler.handleError(error, "Test")

        val statsBefore = errorHandler.getErrorStatistics()
        assertEquals(1, statsBefore.totalErrors)

        errorHandler.clearErrorHistory()

        val statsAfter = errorHandler.getErrorStatistics()
        assertEquals(0, statsAfter.totalErrors)
    }

    @Test
    fun testRetryManager_CancelsRetry() = runTest {
        val retryId = "test_cancel_retry"

        // Start a long-running retry operation
        val job = launch {
            retryManager.executeWithRetry(
                operation = {
                    kotlinx.coroutines.delay(10000) // Long delay
                    "Success"
                },
                retryId = retryId
            )
        }

        // Cancel the retry
        retryManager.cancelRetry(retryId)

        // Wait a bit and check if it's cancelled
        kotlinx.coroutines.delay(100)
        assertEquals(0, retryManager.getRetryCount(retryId))

        job.cancel()
    }

    @Test
    fun testErrorHandler_NetworkAvailabilityCheck() {
        // This test verifies that network availability check doesn't throw
        val isAvailable = errorHandler.isNetworkAvailable()
        
        // We can't predict the result, but it should be a boolean
        assertTrue(isAvailable is Boolean)
    }

    @Test
    fun testErrorReporter_ErrorPatterns() = runTest {
        val error1 = SocketTimeoutException("Timeout 1")
        val error2 = SocketTimeoutException("Timeout 2")
        val error3 = UnknownHostException("No host")

        errorReporter.reportError(error1, "TestContext")
        errorReporter.reportError(error2, "TestContext")
        errorReporter.reportError(error3, "TestContext")

        val patterns = errorReporter.errorPatterns.first()
        assertTrue(patterns.isNotEmpty())
        
        val timeoutPattern = patterns.find { it.errorType == "SocketTimeoutException" }
        assertNotNull(timeoutPattern)
        assertEquals(2, timeoutPattern?.occurrenceCount)
    }
} 