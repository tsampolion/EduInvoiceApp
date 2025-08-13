package gr.eduinvoice.analytics

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comprehensive error reporting and analytics system
 */
@Singleton
class ErrorReporter @Inject constructor(
    private val context: Context
) {

    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private val _errorPatterns = MutableStateFlow<List<ErrorPattern>>(emptyList())
    val errorPatterns: Flow<List<ErrorPattern>> = _errorPatterns.asStateFlow()

    private val _errorStats = MutableStateFlow(ErrorStats())
    val errorStats: Flow<ErrorStats> = _errorStats.asStateFlow()

    /**
     * Reports an error to analytics and logging systems
     */
    fun reportError(error: Throwable, context: String = "Unknown") {
        try {
            // Log to Android logcat
            Log.e(TAG, "Error in $context: ${error.message}", error)

            // Report to Firebase Crashlytics
            reportToCrashlytics(error, context)

            // Log to local file
            logToFile(error, context)

            // Update error patterns
            updateErrorPatterns(error, context)

            // Update statistics
            updateErrorStats(error)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to report error", e)
        }
    }

    /**
     * Reports error with custom metadata
     */
    fun reportError(
        error: Throwable,
        context: String = "Unknown",
        metadata: Map<String, String> = emptyMap()
    ) {
        try {
            // Add metadata to crashlytics
            metadata.forEach { (key, value) ->
                crashlytics.setCustomKey(key, value)
            }

            // Report error
            reportError(error, context)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to report error with metadata", e)
        }
    }

    /**
     * Reports non-fatal error (doesn't crash the app)
     */
    fun reportNonFatalError(
        error: Throwable,
        context: String = "Unknown",
        severity: ErrorSeverity = ErrorSeverity.ERROR
    ) {
        try {
            // Log to Android logcat
            when (severity) {
                ErrorSeverity.DEBUG -> Log.d(TAG, "Non-fatal error in $context: ${error.message}")
                ErrorSeverity.INFO -> Log.i(TAG, "Non-fatal error in $context: ${error.message}")
                ErrorSeverity.WARNING -> Log.w(TAG, "Non-fatal error in $context: ${error.message}")
                ErrorSeverity.ERROR -> Log.e(TAG, "Non-fatal error in $context: ${error.message}")
            }

            // Report to Crashlytics as non-fatal
            crashlytics.recordException(error)

            // Log to local file
            logToFile(error, context, severity)

            // Update patterns and stats
            updateErrorPatterns(error, context)
            updateErrorStats(error)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to report non-fatal error", e)
        }
    }

    /**
     * Tracks error patterns for analytics
     */
    fun trackErrorPatterns(): Flow<ErrorPattern> = errorPatterns.map { patterns ->
        patterns.maxByOrNull { it.occurrenceCount } ?: ErrorPattern("", "", 0)
    }

    /**
     * Gets error statistics
     */
    fun getErrorStatistics(): ErrorStats {
        return _errorStats.value
    }

    /**
     * Clears error history
     */
    fun clearErrorHistory() {
        _errorPatterns.value = emptyList()
        _errorStats.value = ErrorStats()

        // Clear local log file
        try {
            val logFile = File(this.context.filesDir, ERROR_LOG_FILE)
            if (logFile.exists()) {
                logFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear error history", e)
        }
    }

    /**
     * Exports error logs to file
     */
    fun exportErrorLogs(): File? {
        return try {
            val logFile = File(this.context.filesDir, ERROR_LOG_FILE)
            if (logFile.exists()) {
                val exportFile = File(this.context.filesDir, "error_log_${System.currentTimeMillis()}.txt")
                logFile.copyTo(exportFile, overwrite = true)
                exportFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export error logs", e)
            null
        }
    }

    /**
     * Reports error to Firebase Crashlytics
     */
    private fun reportToCrashlytics(error: Throwable, context: String) {
        try {
            crashlytics.setCustomKey("error_context", context)
            crashlytics.setCustomKey("error_timestamp", System.currentTimeMillis())
            crashlytics.recordException(error)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to report to Crashlytics", e)
        }
    }

    /**
     * Logs error to local file
     */
    private fun logToFile(error: Throwable, context: String, severity: ErrorSeverity = ErrorSeverity.ERROR) {
        try {
            val logFile = File(this.context.filesDir, ERROR_LOG_FILE)
            val timestamp = dateFormat.format(Date())

            FileWriter(logFile, true).use { writer ->
                PrintWriter(writer).use { printer ->
                    printer.println("[$timestamp] [$severity] [$context] ${error.javaClass.simpleName}: ${error.message}")
                    error.stackTrace.forEach { element ->
                        printer.println("  at $element")
                    }
                    printer.println()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log error to file", e)
        }
    }

    /**
     * Updates error patterns for analytics
     */
    private fun updateErrorPatterns(error: Throwable, context: String) {
        val errorType = error.javaClass.simpleName
        val currentPatterns = _errorPatterns.value.toMutableList()

        val existingPattern = currentPatterns.find { it.errorType == errorType && it.context == context }

        if (existingPattern != null) {
            val updatedPattern = existingPattern.copy(
                occurrenceCount = existingPattern.occurrenceCount + 1,
                lastOccurrence = System.currentTimeMillis(),
                recentErrors = (existingPattern.recentErrors + (error.message?.toString() ?: "Unknown error")).takeLast(10)
            )
            currentPatterns[currentPatterns.indexOf(existingPattern)] = updatedPattern
        } else {
            val newPattern = ErrorPattern(
                errorType = errorType,
                context = context,
                occurrenceCount = 1,
                lastOccurrence = System.currentTimeMillis(),
                recentErrors = listOf(error.message?.toString() ?: "Unknown error")
            )
            currentPatterns.add(newPattern)
        }

        _errorPatterns.value = currentPatterns
    }

    /**
     * Updates error statistics
     */
    private fun updateErrorStats(error: Throwable) {
        val currentStats = _errorStats.value
        val errorType = error.javaClass.simpleName

        val updatedStats = currentStats.copy(
            totalErrors = currentStats.totalErrors + 1,
            errorsByType = currentStats.errorsByType + (errorType to (currentStats.errorsByType[errorType] ?: 0) + 1),
            lastErrorTime = System.currentTimeMillis()
        )

        _errorStats.value = updatedStats
    }

    companion object {
        private const val TAG = "ErrorReporter"
        private const val ERROR_LOG_FILE = "error_log.txt"
    }
}

/**
 * Error severity levels
 */
enum class ErrorSeverity {
    DEBUG,
    INFO,
    WARNING,
    ERROR
}

/**
 * Error pattern for analytics
 */
data class ErrorPattern(
    val errorType: String,
    val context: String,
    val occurrenceCount: Int,
    val lastOccurrence: Long = System.currentTimeMillis(),
    val recentErrors: List<String> = emptyList()
)

/**
 * Error statistics
 */
data class ErrorStats(
    val totalErrors: Int = 0,
    val errorsByType: Map<String, Int> = emptyMap(),
    val lastErrorTime: Long = 0L
)
