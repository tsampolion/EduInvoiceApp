package gr.eduinvoice.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles memory pressure situations and manages cleanup operations.
 * Provides automatic and manual memory management capabilities.
 */
@Singleton
class MemoryPressureHandler @Inject constructor(
    private val context: Context,
    private val memoryMonitor: MemoryMonitor
) {

    companion object {
        private const val TAG = "MemoryPressureHandler"
        private const val CLEANUP_DELAY_MS = 5000L // 5 seconds
        private const val CRITICAL_CLEANUP_DELAY_MS = 1000L // 1 second
        private const val MAX_CLEANUP_ATTEMPTS = 3
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isMonitoring = false
    private var cleanupAttempts = 0
    private var lastCleanupTime = 0L

    /**
     * Memory pressure levels
     */
    enum class PressureLevel {
        NORMAL,
        LOW,
        CRITICAL
    }

    /**
     * Memory pressure event
     */
    data class MemoryPressureEvent(
        val level: PressureLevel,
        val memoryStatus: MemoryMonitor.MemoryStatus,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Start monitoring memory pressure
     */
    fun startMonitoring() {
        if (isMonitoring) {
            Log.w(TAG, "Memory monitoring already started")
            return
        }

        isMonitoring = true
        scope.launch {
            memoryMonitor.monitorMemoryUsage().collect { memoryUsage ->
                val pressureLevel = when {
                    memoryUsage.memoryUsagePercent >= 85.0 -> PressureLevel.CRITICAL
                    memoryUsage.memoryUsagePercent >= 70.0 -> PressureLevel.LOW
                    else -> PressureLevel.NORMAL
                }

                if (pressureLevel != PressureLevel.NORMAL) {
                    handleMemoryPressure(MemoryPressureEvent(pressureLevel, memoryMonitor.checkMemoryPressure()))
                }
            }
        }

        Log.i(TAG, "Memory pressure monitoring started")
    }

    /**
     * Stop monitoring memory pressure
     */
    fun stopMonitoring() {
        isMonitoring = false
        Log.i(TAG, "Memory pressure monitoring stopped")
    }

    /**
     * Handle low memory situation
     */
    fun handleLowMemory() {
        Log.w(TAG, "Handling low memory situation")

        scope.launch {
            delay(CLEANUP_DELAY_MS)

            val cleanupResult = memoryMonitor.performCleanup()
            if (cleanupResult.success) {
                Log.i(TAG, "Low memory cleanup successful: freed ${cleanupResult.freedMemoryMB}MB")
                cleanupAttempts = 0
            } else {
                Log.w(TAG, "Low memory cleanup failed: ${cleanupResult.errors}")
                cleanupAttempts++
            }

            lastCleanupTime = System.currentTimeMillis()
        }
    }

    /**
     * Handle critical memory situation
     */
    fun handleCriticalMemory() {
        Log.e(TAG, "Handling critical memory situation")

        scope.launch {
            delay(CRITICAL_CLEANUP_DELAY_MS)

            // Perform aggressive cleanup
            val cleanupResult = performAggressiveCleanup()
            if (cleanupResult.success) {
                Log.i(TAG, "Critical memory cleanup successful: freed ${cleanupResult.freedMemoryMB}MB")
                cleanupAttempts = 0
            } else {
                Log.e(TAG, "Critical memory cleanup failed: ${cleanupResult.errors}")
                cleanupAttempts++

                // If cleanup fails multiple times, consider more drastic measures
                if (cleanupAttempts >= MAX_CLEANUP_ATTEMPTS) {
                    handleCleanupFailure()
                }
            }

            lastCleanupTime = System.currentTimeMillis()
        }
    }

    /**
     * Schedule periodic cleanup
     */
    fun scheduleCleanup(intervalMinutes: Long = 30) {
        scope.launch {
            while (isMonitoring) {
                delay(intervalMinutes * 60 * 1000)

                val memoryStatus = memoryMonitor.checkMemoryPressure()
                if (memoryStatus.isLowMemory || memoryStatus.isCriticalMemory) {
                    Log.i(TAG, "Scheduled cleanup triggered due to memory pressure")
                    handleLowMemory()
                } else {
                    Log.d(TAG, "Scheduled cleanup: memory usage normal")
                }
            }
        }

        Log.i(TAG, "Scheduled cleanup every $intervalMinutes minutes")
    }

    /**
     * Perform aggressive cleanup for critical situations
     */
    private suspend fun performAggressiveCleanup(): MemoryMonitor.CleanupResult {
        val operations = mutableListOf<MemoryMonitor.CleanupOperation>()
        val errors = mutableListOf<String>()
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        try {
            // Force multiple garbage collections
            repeat(3) {
                try {
                    System.gc()
                    operations.add(MemoryMonitor.CleanupOperation.GarbageCollection)
                    delay(100) // Small delay between GC calls
                } catch (e: Exception) {
                    errors.add("Garbage collection $it failed: ${e.message}")
                }
            }

            // Clear all caches aggressively
            try {
                clearAllCaches()
                operations.add(MemoryMonitor.CleanupOperation.CustomOperation("AggressiveCacheClear"))
            } catch (e: Exception) {
                errors.add("Aggressive cache cleanup failed: ${e.message}")
            }

            // Clear ViewModel caches
            try {
                clearViewModelCaches()
                operations.add(MemoryMonitor.CleanupOperation.ClearViewModelCache)
            } catch (e: Exception) {
                errors.add("ViewModel cache cleanup failed: ${e.message}")
            }

            // Clear database caches
            try {
                clearDatabaseCaches()
                operations.add(MemoryMonitor.CleanupOperation.ClearDatabaseCache)
            } catch (e: Exception) {
                errors.add("Database cache cleanup failed: ${e.message}")
            }

        } catch (e: Exception) {
            errors.add("Aggressive cleanup failed: ${e.message}")
            Log.e(TAG, "Aggressive cleanup failed", e)
        }

        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val freedMemory = initialMemory - finalMemory
        val freedMemoryMB = freedMemory / (1024 * 1024)

        Log.i(TAG, "Aggressive cleanup completed: freed ${freedMemoryMB}MB")

        return MemoryMonitor.CleanupResult(
            success = errors.isEmpty(),
            freedMemoryMB = freedMemoryMB,
            operations = operations,
            errors = errors
        )
    }

    /**
     * Handle cleanup failure
     */
    private fun handleCleanupFailure() {
        Log.e(TAG, "Cleanup failed multiple times, taking drastic measures")

        // Notify user or take drastic measures
        // This could include:
        // - Showing a warning to the user
        // - Restarting the app
        // - Clearing all data
        // - Forcing app restart

        scope.launch {
            // For now, we'll just log the failure
            Log.e(TAG, "Multiple cleanup failures detected. Consider app restart.")
        }
    }

    /**
     * Handle memory pressure event
     */
    private fun handleMemoryPressure(event: MemoryPressureEvent) {
        Log.w(TAG, "Memory pressure detected: ${event.level}, usage: ${event.memoryStatus.memoryUsagePercent}%")

        when (event.level) {
            PressureLevel.CRITICAL -> handleCriticalMemory()
            PressureLevel.LOW -> handleLowMemory()
            PressureLevel.NORMAL -> {
                // Reset cleanup attempts when memory is normal
                cleanupAttempts = 0
            }
        }
    }

    /**
     * Clear all caches aggressively
     */
    private fun clearAllCaches() {
        // Clear image caches
        clearImageCaches()

        // Clear other application caches
        try {
            context.cacheDir.deleteRecursively()
            Log.d(TAG, "Application cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear application cache", e)
        }
    }

    /**
     * Clear image caches
     */
    private fun clearImageCaches() {
        // This would integrate with image loading libraries
        Log.d(TAG, "Image cache cleanup requested")
    }

    /**
     * Clear ViewModel caches
     */
    private fun clearViewModelCaches() {
        // This would clear ViewModel caches
        Log.d(TAG, "ViewModel cache cleanup requested")
    }

    /**
     * Clear database caches
     */
    private fun clearDatabaseCaches() {
        // This would clear Room database caches
        Log.d(TAG, "Database cache cleanup requested")
    }

    /**
     * Get cleanup statistics
     */
    fun getCleanupStatistics(): Map<String, Any> {
        return mapOf(
            "isMonitoring" to isMonitoring,
            "cleanupAttempts" to cleanupAttempts,
            "lastCleanupTime" to lastCleanupTime,
            "maxCleanupAttempts" to MAX_CLEANUP_ATTEMPTS
        )
    }
}
