package gr.eduinvoice.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@RunWith(AndroidJUnit4::class)
class ErrorHandlingTest {
    
    private lateinit var errorHandler: ErrorHandler
    private lateinit var retryManager: RetryManager
    private lateinit var context: Context
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        errorHandler = ErrorHandler(context)
        retryManager = RetryManager()
    }
    
    @Test
    fun testErrorHandler_ClassifyNetworkTimeout() {
        val error = SocketTimeoutException("Connection timed out")
        val result = errorHandler.handleError(error, "TestContext")
        
        assertEquals(ErrorType.NETWORK_TIMEOUT, result.errorType)
        assertTrue(result.shouldRetry)
        assertTrue(result.retryDelay > 0)
        assertTrue(result.userMessage.contains("timed out"))
    }
    
    @Test
    fun testErrorHandler_ClassifyNetworkNoConnection() {
        val error = UnknownHostException("Unable to resolve host")
        val result = errorHandler.handleError(error, "TestContext")
        
        assertEquals(ErrorType.NETWORK_NO_CONNECTION, result.errorType)
        assertTrue(result.shouldRetry)
        assertTrue(result.retryDelay > 0)
        assertTrue(result.userMessage.contains("connect"))
    }
    
    @Test
    fun testErrorHandler_ClassifyNetworkError() {
        val error = IOException("Network unreachable")
        val result = errorHandler.handleError(error, "TestContext")
        
        assertEquals(ErrorType.NETWORK_ERROR, result.errorType)
        assertTrue(result.shouldRetry)
        assertTrue(result.userMessage.contains("Network error"))
    }
    
    @Test
    fun testErrorHandler_ClassifyIOError() {
        val error = IOException("File not found")
        val result = errorHandler.handleError(error, "TestContext")
        
        assertEquals(ErrorType.IO_ERROR, result.errorType)
        assertFalse(result.shouldRetry)
        assertTrue(result.userMessage.contains("file operation"))
    }
    
    @Test
    fun testErrorHandler_ClassifyPermissionError() {
        val error = SecurityException("Permission denied")
        val result = errorHandler.handleError(error, "TestContext")
        
        assertEquals(ErrorType.PERMISSION_ERROR, result.errorType)
        assertFalse(result.shouldRetry)
        assertTrue(result.userMessage.contains("Permission"))
        assertEquals(RecoveryAction.REQUEST_PERMISSIONS, result.recoveryAction)
    }
    
    @Test
    fun testErrorHandler_ClassifyValidationError() {
        val error = IllegalArgumentException("Invalid input")
        val result = errorHandler.handleError(error, "TestContext")
        
        assertEquals(ErrorType.VALIDATION_ERROR, result.errorType)
        assertFalse(result.shouldRetry)
        assertTrue(result.userMessage.contains("Invalid data"))
        assertEquals(RecoveryAction.VALIDATE_INPUT, result.recoveryAction)
    }
    
    @Test
    fun testErrorHandler_ClassifyMemoryError() {
        val error = OutOfMemoryError("Java heap space")
        val result = errorHandler.handleError(error, "TestContext")
        
        assertEquals(ErrorType.MEMORY_ERROR, result.errorType)
        assertFalse(result.shouldRetry)
        assertTrue(result.userMessage.contains("memory"))
        assertEquals(RecoveryAction.CLEAR_MEMORY, result.recoveryAction)
    }
    
    @Test
    fun testErrorHandler_ClassifyUnknownError() {
        val error = RuntimeException("Unknown error")
        val result = errorHandler.handleError(error, "TestContext")
        
        assertEquals(ErrorType.UNKNOWN_ERROR, result.errorType)
        assertFalse(result.shouldRetry)
        assertTrue(result.userMessage.contains("unexpected"))
    }
    
    @Test
    fun testErrorHandler_RetryDelayCalculation() {
        val error = SocketTimeoutException("Timeout")
        
        val delay1 = errorHandler.getRetryDelay(error, 0)
        val delay2 = errorHandler.getRetryDelay(error, 1)
        val delay3 = errorHandler.getRetryDelay(error, 2)
        
        assertTrue(delay1 > 0)
        assertTrue(delay2 > delay1)
        assertTrue(delay3 > delay2)
        assertTrue(delay3 <= 30000) // Max delay
    }
    
    @Test
    fun testErrorHandler_ErrorHistory() {
        val error1 = SocketTimeoutException("Timeout 1")
        val error2 = UnknownHostException("No connection")
        
        errorHandler.handleError(error1, "Context1")
        errorHandler.handleError(error2, "Context2")
        
        val history = runBlocking { errorHandler.errorHistory.first() }
        assertEquals(2, history.size)
        assertEquals("Context1", history[0].context)
        assertEquals("Context2", history[1].context)
    }
    
    @Test
    fun testErrorHandler_ErrorStatistics() {
        val error1 = SocketTimeoutException("Timeout")
        val error2 = SocketTimeoutException("Another timeout")
        val error3 = UnknownHostException("No connection")
        
        errorHandler.handleError(error1, "Test")
        errorHandler.handleError(error2, "Test")
        errorHandler.handleError(error3, "Test")
        
        val stats = errorHandler.getErrorStatistics()
        assertEquals(3, stats.totalErrors)
        assertEquals(2, stats.errorCounts[ErrorType.NETWORK_TIMEOUT])
        assertEquals(1, stats.errorCounts[ErrorType.NETWORK_NO_CONNECTION])
        assertEquals(ErrorType.NETWORK_TIMEOUT, stats.mostCommonError)
    }
    
    @Test
    fun testRetryManager_SuccessfulRetry() = runTest {
        var attempts = 0
        val operation = suspend {
            attempts++
            if (attempts < 3) {
                throw SocketTimeoutException("Timeout")
            }
            "Success"
        }
        
        val result = retryManager.executeWithRetry(
            operation = operation,
            maxRetries = 3
        )
        
        assertTrue(result.isSuccess)
        assertEquals("Success", result.getOrNull())
        assertEquals(3, attempts)
    }
    
    @Test
    fun testRetryManager_MaxRetriesExceeded() = runTest {
        var attempts = 0
        val operation = suspend {
            attempts++
            throw SocketTimeoutException("Persistent timeout")
        }
        
        val result = retryManager.executeWithRetry(
            operation = operation,
            maxRetries = 2
        )
        
        assertTrue(result.isFailure)
        assertEquals(3, attempts) // Initial + 2 retries
        assertTrue(result.exceptionOrNull() is SocketTimeoutException)
    }
    
    @Test
    fun testRetryManager_ShouldNotRetry() = runTest {
        var attempts = 0
        val operation = suspend {
            attempts++
            throw IllegalArgumentException("Invalid input")
        }
        
        val result = retryManager.executeWithRetry(
            operation = operation,
            shouldRetry = { it is SocketTimeoutException }
        )
        
        assertTrue(result.isFailure)
        assertEquals(1, attempts) // No retries for IllegalArgumentException
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    @Test
    fun testRetryManager_RetryFlow() = runTest {
        var attempts = 0
        val operation = suspend {
            attempts++
            if (attempts < 2) {
                throw SocketTimeoutException("Timeout")
            }
            "Success"
        }
        
        val results = mutableListOf<RetryResult<String>>()
        retryManager.executeWithRetryFlow(
            operation = operation,
            maxRetries = 3
        ).collect { result ->
            results.add(result)
        }
        
        assertEquals(3, results.size) // Loading, Retrying, Success
        assertTrue(results[0] is RetryResult.Loading)
        assertTrue(results[1] is RetryResult.Retrying)
        assertTrue(results[2] is RetryResult.Success)
        assertEquals("Success", (results[2] as RetryResult.Success).data)
    }
    
    @Test
    fun testRetryManager_ParallelRetry() = runTest {
        val operations = listOf(
            suspend { "Result 1" },
            suspend { "Result 2" },
            suspend { "Result 3" }
        )
        
        val results = retryManager.executeParallelWithRetry(
            operations = operations,
            maxRetries = 1
        )
        
        assertEquals(3, results.size)
        assertTrue(results.all { it.isSuccess })
        assertEquals("Result 1", results[0].getOrNull())
        assertEquals("Result 2", results[1].getOrNull())
        assertEquals("Result 3", results[2].getOrNull())
    }
    
    @Test
    fun testRetryManager_RetryPolicy() {
        val policy = retryManager.createRetryPolicy(
            maxRetries = 5,
            baseDelay = 2000L,
            maxDelay = 60000L
        ) { it is SocketTimeoutException }
        
        assertEquals(5, policy.maxRetries)
        assertEquals(2000L, policy.baseDelay)
        assertEquals(60000L, policy.maxDelay)
        assertTrue(policy.shouldRetry(SocketTimeoutException("Timeout")))
        assertFalse(policy.shouldRetry(IllegalArgumentException("Invalid")))
    }
    
    @Test
    fun testRetryManager_CancelRetry() = runTest {
        val retryId = "test_retry"
        
        // Start a long-running retry operation
        val job = launch {
            retryManager.executeWithRetry(
                operation = suspend {
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
    fun testExtensionFunction_Retry() = runTest {
        var attempts = 0
        val operation = suspend {
            attempts++
            if (attempts < 2) {
                throw SocketTimeoutException("Timeout")
            }
            "Success"
        }
        
        val result = retry(maxRetries = 3, operation = operation)
        
        assertTrue(result.isSuccess)
        assertEquals("Success", result.getOrNull())
        assertEquals(2, attempts)
    }
    
    @Test
    fun testExtensionFunction_RetryWithCondition() = runTest {
        var attempts = 0
        val operation = suspend {
            attempts++
            throw SocketTimeoutException("Timeout")
        }
        
        val result = retry(
            maxRetries = 2,
            shouldRetry = { it is SocketTimeoutException }
        ) {
            operation()
        }
        
        assertTrue(result.isFailure)
        assertEquals(3, attempts) // Initial + 2 retries
    }
} 