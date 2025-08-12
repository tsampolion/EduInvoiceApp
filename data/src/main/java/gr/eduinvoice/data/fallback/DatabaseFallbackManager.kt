package gr.eduinvoice.data.fallback

import android.content.Context
import android.util.Log
import gr.eduinvoice.data.database.DatabaseConstants
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.monitoring.DatabaseHealthMonitor
import gr.eduinvoice.data.validation.DatabaseIntegrityValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages database fallback mechanisms and recovery procedures.
 * Provides graceful degradation when database issues occur.
 */
@Singleton
class DatabaseFallbackManager @Inject constructor(
    private val context: Context,
    private val database: EduInvoiceDatabase,
    private val healthMonitor: DatabaseHealthMonitor,
    private val integrityValidator: DatabaseIntegrityValidator
) {

    companion object {
        private const val TAG = "DatabaseFallbackManager"
        private const val RECOVERY_CHECK_INTERVAL_MS = 30000L // 30 seconds
        private const val MAX_RECOVERY_ATTEMPTS = 3
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

                private val _fallbackState = MutableStateFlow<FallbackState>(FallbackState.Normal)
    val fallbackState: StateFlow<FallbackState> = _fallbackState.asStateFlow()

    private val _recoveryProgress = MutableStateFlow(RecoveryProgress())
    val recoveryProgress: StateFlow<RecoveryProgress> = _recoveryProgress.asStateFlow()

    private var recoveryAttempts = 0
    private var isRecoveryScheduled = false

    /**
     * Database fallback states
     */
    sealed class FallbackState {
        object Normal : FallbackState()
        object ReadOnly : FallbackState()
        object Offline : FallbackState()
        object Recovery : FallbackState()
        object Maintenance : FallbackState()
        data class Error(val message: String) : FallbackState()
    }

    /**
     * Recovery progress information
     */
    data class RecoveryProgress(
        val isInProgress: Boolean = false,
        val currentStep: String = "",
        val progress: Float = 0f,
        val estimatedTimeRemaining: Long = 0L,
        val errors: List<String> = emptyList()
    )

    /**
     * Recovery operation results
     */
    data class RecoveryResult(
        val success: Boolean,
        val state: FallbackState,
        val message: String,
        val dataLoss: Boolean = false
    )

    /**
     * Switch to read-only mode
     */
    suspend fun switchToReadOnlyMode(): RecoveryResult {
        return try {
            Log.w(TAG, "Switching to read-only mode")
            _fallbackState.value = FallbackState.ReadOnly

            // Perform health check in read-only mode
            val healthStatus = healthMonitor.checkDatabaseHealth()
            if (healthStatus.isHealthy) {
                RecoveryResult(
                    success = true,
                    state = FallbackState.ReadOnly,
                    message = "Successfully switched to read-only mode"
                )
            } else {
                RecoveryResult(
                    success = false,
                    state = FallbackState.Error("Database health check failed in read-only mode"),
                    message = "Failed to maintain read-only mode"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to read-only mode", e)
            RecoveryResult(
                success = false,
                state = FallbackState.Error(e.message ?: "Unknown error"),
                message = "Failed to switch to read-only mode: ${e.message}"
            )
        }
    }

    /**
     * Enable offline mode
     */
    suspend fun enableOfflineMode(): RecoveryResult {
        return try {
            Log.w(TAG, "Enabling offline mode")
            _fallbackState.value = FallbackState.Offline

            // In offline mode, we rely on cached data and local storage
            // This is a simplified implementation - in production, you'd have more sophisticated caching
            RecoveryResult(
                success = true,
                state = FallbackState.Offline,
                message = "Successfully enabled offline mode"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable offline mode", e)
            RecoveryResult(
                success = false,
                state = FallbackState.Error(e.message ?: "Unknown error"),
                message = "Failed to enable offline mode: ${e.message}"
            )
        }
    }

    /**
     * Schedule database recovery
     */
    fun scheduleRecovery() {
        if (isRecoveryScheduled) {
            Log.w(TAG, "Recovery already scheduled")
            return
        }

        isRecoveryScheduled = true
        scope.launch {
            delay(RECOVERY_CHECK_INTERVAL_MS)
            performRecovery()
        }
    }

    /**
     * Perform database recovery
     */
    private suspend fun performRecovery() {
        if (recoveryAttempts >= MAX_RECOVERY_ATTEMPTS) {
            Log.e(TAG, "Maximum recovery attempts reached")
            _fallbackState.value = FallbackState.Error("Maximum recovery attempts reached")
            return
        }

        recoveryAttempts++
        Log.i(TAG, "Starting database recovery attempt $recoveryAttempts")

        _fallbackState.value = FallbackState.Recovery
        _recoveryProgress.value = RecoveryProgress(
            isInProgress = true,
            currentStep = "Initializing recovery...",
            progress = 0f
        )

        try {
            // Step 1: Health check
            updateRecoveryProgress("Performing health check...", 0.2f)
            val healthStatus = healthMonitor.checkDatabaseHealth()

            if (healthStatus.isHealthy) {
                Log.i(TAG, "Database is healthy, switching to normal mode")
                _fallbackState.value = FallbackState.Normal
                _recoveryProgress.value = RecoveryProgress(
                    isInProgress = false,
                    currentStep = "Recovery completed successfully",
                    progress = 1f
                )
                return
            }

            // Step 2: Integrity validation
            updateRecoveryProgress("Validating data integrity...", 0.4f)
            val validationResult = integrityValidator.validateAllTables()

            if (validationResult.isValid) {
                Log.i(TAG, "Data integrity validation passed")
                _fallbackState.value = FallbackState.Normal
                _recoveryProgress.value = RecoveryProgress(
                    isInProgress = false,
                    currentStep = "Recovery completed successfully",
                    progress = 1f
                )
                return
            }

            // Step 3: Attempt repairs
            updateRecoveryProgress("Attempting data repairs...", 0.6f)
            val repairResult = integrityValidator.repairCorruptedData()

            if (repairResult.success) {
                Log.i(TAG, "Data repairs completed successfully")
                updateRecoveryProgress("Verifying repairs...", 0.8f)

                // Step 4: Verify repairs
                val finalHealthCheck = healthMonitor.checkDatabaseHealth()
                if (finalHealthCheck.isHealthy) {
                    Log.i(TAG, "Recovery completed successfully")
                    _fallbackState.value = FallbackState.Normal
                    _recoveryProgress.value = RecoveryProgress(
                        isInProgress = false,
                        currentStep = "Recovery completed successfully",
                        progress = 1f
                    )
                    return
                }
            }

            // Step 5: Maintenance mode
            updateRecoveryProgress("Switching to maintenance mode...", 0.9f)
            _fallbackState.value = FallbackState.Maintenance

            // Schedule another recovery attempt
            scheduleRecovery()

        } catch (e: Exception) {
            Log.e(TAG, "Recovery failed", e)
            _fallbackState.value = FallbackState.Error("Recovery failed: ${e.message}")
            _recoveryProgress.value = RecoveryProgress(
                isInProgress = false,
                currentStep = "Recovery failed",
                progress = 0f,
                errors = listOf(e.message ?: "Unknown error")
            )

            // Schedule another recovery attempt
            scheduleRecovery()
        }
    }

    /**
     * Update recovery progress
     */
    private fun updateRecoveryProgress(step: String, progress: Float) {
        _recoveryProgress.value = RecoveryProgress(
            isInProgress = true,
            currentStep = step,
            progress = progress
        )
    }

    /**
     * Force database reset (nuclear option)
     */
    suspend fun forceDatabaseReset(): RecoveryResult {
        return try {
            Log.w(TAG, "Force database reset initiated")
            _fallbackState.value = FallbackState.Maintenance

            // Delete database file
            val dbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
            if (dbFile.exists()) {
                val deleted = dbFile.delete()
                if (!deleted) {
                    throw Exception("Failed to delete database file")
                }
                Log.i(TAG, "Database file deleted successfully")
            }

            // Clear any backup files that might be corrupted
            clearCorruptedBackups()

            // Reset recovery state
            recoveryAttempts = 0
            isRecoveryScheduled = false

            RecoveryResult(
                success = true,
                state = FallbackState.Normal,
                message = "Database reset completed successfully",
                dataLoss = true
            )

        } catch (e: Exception) {
            Log.e(TAG, "Force database reset failed", e)
            RecoveryResult(
                success = false,
                state = FallbackState.Error(e.message ?: "Unknown error"),
                message = "Database reset failed: ${e.message}",
                dataLoss = true
            )
        }
    }

    /**
     * Clear corrupted backup files
     */
    private fun clearCorruptedBackups() {
        try {
            val backupDir = File(context.filesDir, "backups")
            if (backupDir.exists()) {
                val backupFiles = backupDir.listFiles()
                backupFiles?.forEach { file ->
                    if (file.length() < 100) { // Very small files are likely corrupted
                        file.delete()
                        Log.i(TAG, "Deleted corrupted backup file: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear corrupted backups", e)
        }
    }

    /**
     * Get current database status
     */
    suspend fun getDatabaseStatus(): DatabaseStatus {
        return try {
            val healthStatus = healthMonitor.checkDatabaseHealth()
            val fileInfo = healthMonitor.getDatabaseFileInfo()
            val statistics = integrityValidator.getDatabaseStatistics()

            DatabaseStatus(
                state = _fallbackState.value,
                isHealthy = healthStatus.isHealthy,
                fileSize = fileInfo.size,
                lastModified = fileInfo.lastModified,
                recordCount = statistics.totalRecords,
                issues = healthStatus.issues.map { it.toString() }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get database status", e)
            DatabaseStatus(
                state = FallbackState.Error(e.message ?: "Unknown error"),
                isHealthy = false,
                fileSize = 0L,
                lastModified = 0L,
                recordCount = 0,
                issues = listOf(e.message ?: "Unknown error")
            )
        }
    }

    /**
     * Check if database is accessible
     */
    suspend fun isDatabaseAccessible(): Boolean {
        return try {
            val healthStatus = healthMonitor.checkDatabaseHealth()
            healthStatus.isHealthy
        } catch (e: Exception) {
            Log.e(TAG, "Database accessibility check failed", e)
            false
        }
    }

    /**
     * Get recovery recommendations
     */
    fun getRecoveryRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()

        when (_fallbackState.value) {
            is FallbackState.ReadOnly -> {
                recommendations.add("Database is in read-only mode. Check disk space and permissions.")
                recommendations.add("Consider running database maintenance.")
                recommendations.add("Verify database file integrity.")
            }
            is FallbackState.Offline -> {
                recommendations.add("Database is in offline mode. Check network connectivity.")
                recommendations.add("Verify database server status.")
                recommendations.add("Consider using cached data.")
            }
            is FallbackState.Recovery -> {
                recommendations.add("Database recovery in progress. Please wait.")
                recommendations.add("Do not restart the application during recovery.")
                recommendations.add("Monitor recovery progress.")
            }
            is FallbackState.Maintenance -> {
                recommendations.add("Database is in maintenance mode.")
                recommendations.add("Consider backing up data before proceeding.")
                recommendations.add("Contact support if issues persist.")
            }
            is FallbackState.Error -> {
                recommendations.add("Database error detected.")
                recommendations.add("Check application logs for details.")
                recommendations.add("Consider database reset as last resort.")
            }
            else -> {
                recommendations.add("Database appears to be functioning normally.")
            }
        }

        return recommendations
    }

    /**
     * Database status information
     */
    data class DatabaseStatus(
        val state: FallbackState,
        val isHealthy: Boolean,
        val fileSize: Long,
        val lastModified: Long,
        val recordCount: Int,
        val issues: List<String>
    )
}
