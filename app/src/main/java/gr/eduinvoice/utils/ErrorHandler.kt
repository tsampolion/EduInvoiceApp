package gr.eduinvoice.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized error handling system that provides error classification,
 * retry logic, and user-friendly error messages.
 */
@Singleton
class ErrorHandler @Inject constructor(
    private val context: Context
) {
    
    private val _errorHistory = MutableStateFlow<List<ErrorEntry>>(emptyList())
    val errorHistory: Flow<List<ErrorEntry>> = _errorHistory.asStateFlow()
    
    /**
     * Handles an error and returns appropriate error result with recovery options
     */
    fun handleError(error: Throwable, context: String = "Unknown"): ErrorResult {
        val errorType = classifyError(error)
        val shouldRetry = shouldRetry(error)
        val retryDelay = getRetryDelay(error)
        val userMessage = getUserFriendlyMessage(error)
        
        // Record error in history
        recordError(error, errorType, context)
        
        return ErrorResult(
            error = error,
            errorType = errorType,
            shouldRetry = shouldRetry,
            retryDelay = retryDelay,
            userMessage = userMessage,
            recoveryAction = getRecoveryAction(errorType)
        )
    }
    
    /**
     * Determines if an error should be retried
     */
    fun shouldRetry(error: Throwable): Boolean {
        return when (error) {
            is SocketTimeoutException -> true
            is UnknownHostException -> true
            is IOException -> isNetworkError(error)
            else -> false
        }
    }
    
    /**
     * Calculates retry delay based on error type and retry count
     */
    fun getRetryDelay(error: Throwable, retryCount: Int = 0): Long {
        val baseDelay = when (error) {
            is SocketTimeoutException -> 1000L
            is UnknownHostException -> 2000L
            is IOException -> 1500L
            else -> 3000L
        }
        
        // Exponential backoff with jitter
        return (baseDelay * (1 shl retryCount) + (Math.random() * 1000).toLong())
            .coerceAtMost(30000L) // Max 30 seconds
    }
    
    /**
     * Classifies errors into different categories
     */
    private fun classifyError(error: Throwable): ErrorType {
        return when (error) {
            is SocketTimeoutException -> ErrorType.NETWORK_TIMEOUT
            is UnknownHostException -> ErrorType.NETWORK_NO_CONNECTION
            is IOException -> {
                if (isNetworkError(error)) ErrorType.NETWORK_ERROR
                else ErrorType.IO_ERROR
            }
            is SecurityException -> ErrorType.PERMISSION_ERROR
            is IllegalArgumentException -> ErrorType.VALIDATION_ERROR
            is IllegalStateException -> ErrorType.STATE_ERROR
            is OutOfMemoryError -> ErrorType.MEMORY_ERROR
            else -> ErrorType.UNKNOWN_ERROR
        }
    }
    
    /**
     * Checks if an IOException is network-related
     */
    private fun isNetworkError(error: IOException): Boolean {
        val message = error.message?.lowercase() ?: ""
        return message.contains("network") || 
               message.contains("connection") || 
               message.contains("timeout") ||
               message.contains("unreachable")
    }
    
    /**
     * Gets user-friendly error message
     */
    private fun getUserFriendlyMessage(error: Throwable): String {
        return when (error) {
            is SocketTimeoutException -> "Connection timed out. Please check your internet connection."
            is UnknownHostException -> "Unable to connect to server. Please check your internet connection."
            is IOException -> {
                if (isNetworkError(error)) {
                    "Network error occurred. Please check your connection and try again."
                } else {
                    "A file operation failed. Please try again."
                }
            }
            is SecurityException -> "Permission denied. Please check app permissions."
            is IllegalArgumentException -> "Invalid data provided. Please check your input."
            is IllegalStateException -> "Application is in an invalid state. Please restart the app."
            is OutOfMemoryError -> "Device is running low on memory. Please close other apps and try again."
            else -> "An unexpected error occurred. Please try again."
        }
    }
    
    /**
     * Gets appropriate recovery action for error type
     */
    private fun getRecoveryAction(errorType: ErrorType): RecoveryAction {
        return when (errorType) {
            ErrorType.NETWORK_TIMEOUT -> RecoveryAction.RETRY_WITH_BACKOFF
            ErrorType.NETWORK_NO_CONNECTION -> RecoveryAction.CHECK_NETWORK
            ErrorType.NETWORK_ERROR -> RecoveryAction.RETRY_WITH_BACKOFF
            ErrorType.IO_ERROR -> RecoveryAction.RETRY
            ErrorType.PERMISSION_ERROR -> RecoveryAction.REQUEST_PERMISSIONS
            ErrorType.VALIDATION_ERROR -> RecoveryAction.VALIDATE_INPUT
            ErrorType.STATE_ERROR -> RecoveryAction.RESTART_APP
            ErrorType.MEMORY_ERROR -> RecoveryAction.CLEAR_MEMORY
            ErrorType.UNKNOWN_ERROR -> RecoveryAction.RETRY
        }
    }
    
    /**
     * Records error in history for analytics and debugging
     */
    private fun recordError(error: Throwable, errorType: ErrorType, context: String) {
        val entry = ErrorEntry(
            timestamp = System.currentTimeMillis(),
            error = error,
            errorType = errorType,
            context = context,
            stackTrace = error.stackTraceToString()
        )
        
        val currentHistory = _errorHistory.value.toMutableList()
        currentHistory.add(entry)
        
        // Keep only last 100 errors
        if (currentHistory.size > 100) {
            currentHistory.removeAt(0)
        }
        
        _errorHistory.value = currentHistory
    }
    
    /**
     * Checks if device has network connectivity
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Gets error statistics for analytics
     */
    fun getErrorStatistics(): ErrorStatistics {
        val errors = _errorHistory.value
        val errorCounts = errors.groupBy { it.errorType }.mapValues { it.value.size }
        
        return ErrorStatistics(
            totalErrors = errors.size,
            errorCounts = errorCounts,
            lastErrorTime = errors.lastOrNull()?.timestamp,
            mostCommonError = errorCounts.maxByOrNull { it.value }?.key
        )
    }
    
    /**
     * Clears error history
     */
    fun clearErrorHistory() {
        _errorHistory.value = emptyList()
    }
}

/**
 * Error types for classification
 */
enum class ErrorType {
    NETWORK_TIMEOUT,
    NETWORK_NO_CONNECTION,
    NETWORK_ERROR,
    IO_ERROR,
    PERMISSION_ERROR,
    VALIDATION_ERROR,
    STATE_ERROR,
    MEMORY_ERROR,
    UNKNOWN_ERROR
}

/**
 * Recovery actions for different error types
 */
enum class RecoveryAction {
    RETRY,
    RETRY_WITH_BACKOFF,
    CHECK_NETWORK,
    REQUEST_PERMISSIONS,
    VALIDATE_INPUT,
    RESTART_APP,
    CLEAR_MEMORY
}

/**
 * Result of error handling
 */
data class ErrorResult(
    val error: Throwable,
    val errorType: ErrorType,
    val shouldRetry: Boolean,
    val retryDelay: Long,
    val userMessage: String,
    val recoveryAction: RecoveryAction
)

/**
 * Error entry for history tracking
 */
data class ErrorEntry(
    val timestamp: Long,
    val error: Throwable,
    val errorType: ErrorType,
    val context: String,
    val stackTrace: String
)

/**
 * Error statistics for analytics
 */
data class ErrorStatistics(
    val totalErrors: Int,
    val errorCounts: Map<ErrorType, Int>,
    val lastErrorTime: Long?,
    val mostCommonError: ErrorType?
) 