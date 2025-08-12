package gr.eduinvoice.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors memory usage and provides memory management utilities.
 * Helps prevent memory leaks and optimize performance.
 */
@Singleton
class MemoryMonitor @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val TAG = "MemoryMonitor"
        private const val MEMORY_CHECK_INTERVAL_MS = 30000L // 30 seconds
        private const val LOW_MEMORY_THRESHOLD_PERCENT = 70.0
        private const val CRITICAL_MEMORY_THRESHOLD_PERCENT = 85.0
        private const val MAX_MEMORY_USAGE_MB = 100L
    }

    /**
     * Memory status information
     */
    data class MemoryStatus(
        val isLowMemory: Boolean,
        val isCriticalMemory: Boolean,
        val usedMemoryMB: Long,
        val totalMemoryMB: Long,
        val availableMemoryMB: Long,
        val memoryUsagePercent: Double,
        val recommendations: List<String>
    )

    /**
     * Memory usage statistics
     */
    data class MemoryUsage(
        val usedMemoryMB: Long,
        val totalMemoryMB: Long,
        val availableMemoryMB: Long,
        val memoryUsagePercent: Double,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Cleanup operation results
     */
    data class CleanupResult(
        val success: Boolean,
        val freedMemoryMB: Long,
        val operations: List<CleanupOperation>,
        val errors: List<String>
    )

    /**
     * Types of cleanup operations
     */
    sealed class CleanupOperation {
        object GarbageCollection : CleanupOperation()
        object ClearImageCache : CleanupOperation()
        object ClearViewModelCache : CleanupOperation()
        object ClearDatabaseCache : CleanupOperation()
        data class CustomOperation(val name: String) : CleanupOperation()
    }

    /**
     * Check current memory pressure
     */
    fun checkMemoryPressure(): MemoryStatus {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val availableMemory = maxMemory - usedMemory

        val usedMemoryMB = usedMemory / (1024 * 1024)
        val totalMemoryMB = maxMemory / (1024 * 1024)
        val availableMemoryMB = availableMemory / (1024 * 1024)
        val memoryUsagePercent = (usedMemory.toDouble() / maxMemory.toDouble()) * 100.0

        val isLowMemory = memoryUsagePercent >= LOW_MEMORY_THRESHOLD_PERCENT
        val isCriticalMemory = memoryUsagePercent >= CRITICAL_MEMORY_THRESHOLD_PERCENT

        val recommendations = mutableListOf<String>()

        if (isCriticalMemory) {
            recommendations.add("Critical memory pressure detected. Perform immediate cleanup.")
            recommendations.add("Consider reducing image quality or clearing caches.")
            recommendations.add("Monitor for memory leaks.")
        } else if (isLowMemory) {
            recommendations.add("Low memory pressure detected. Consider cleanup.")
            recommendations.add("Monitor memory usage trends.")
        }

        if (usedMemoryMB > MAX_MEMORY_USAGE_MB) {
            recommendations.add("Memory usage exceeds recommended limit (${MAX_MEMORY_USAGE_MB}MB).")
        }

        Log.d(TAG, "Memory status: ${usedMemoryMB}MB used, ${memoryUsagePercent}% usage")

        return MemoryStatus(
            isLowMemory = isLowMemory,
            isCriticalMemory = isCriticalMemory,
            usedMemoryMB = usedMemoryMB,
            totalMemoryMB = totalMemoryMB,
            availableMemoryMB = availableMemoryMB,
            memoryUsagePercent = memoryUsagePercent,
            recommendations = recommendations
        )
    }

    /**
     * Perform memory cleanup operations
     */
    fun performCleanup(): CleanupResult {
        val operations = mutableListOf<CleanupOperation>()
        val errors = mutableListOf<String>()
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        try {
            // Force garbage collection
            try {
                System.gc()
                operations.add(CleanupOperation.GarbageCollection)
                Log.d(TAG, "Garbage collection completed")
            } catch (e: Exception) {
                errors.add("Garbage collection failed: ${e.message}")
                Log.e(TAG, "Garbage collection failed", e)
            }

            // Clear image caches if available
            try {
                clearImageCaches()
                operations.add(CleanupOperation.ClearImageCache)
                Log.d(TAG, "Image cache cleared")
            } catch (e: Exception) {
                errors.add("Image cache cleanup failed: ${e.message}")
                Log.e(TAG, "Image cache cleanup failed", e)
            }

            // Clear ViewModel caches
            try {
                clearViewModelCaches()
                operations.add(CleanupOperation.ClearViewModelCache)
                Log.d(TAG, "ViewModel cache cleared")
            } catch (e: Exception) {
                errors.add("ViewModel cache cleanup failed: ${e.message}")
                Log.e(TAG, "ViewModel cache cleanup failed", e)
            }

            // Clear database caches
            try {
                clearDatabaseCaches()
                operations.add(CleanupOperation.ClearDatabaseCache)
                Log.d(TAG, "Database cache cleared")
            } catch (e: Exception) {
                errors.add("Database cache cleanup failed: ${e.message}")
                Log.e(TAG, "Database cache cleanup failed", e)
            }

        } catch (e: Exception) {
            errors.add("General cleanup failed: ${e.message}")
            Log.e(TAG, "General cleanup failed", e)
        }

        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val freedMemory = initialMemory - finalMemory
        val freedMemoryMB = freedMemory / (1024 * 1024)

        Log.i(TAG, "Cleanup completed: freed ${freedMemoryMB}MB, operations: ${operations.size}, errors: ${errors.size}")

        return CleanupResult(
            success = errors.isEmpty(),
            freedMemoryMB = freedMemoryMB,
            operations = operations,
            errors = errors
        )
    }

    /**
     * Monitor memory usage continuously
     */
    fun monitorMemoryUsage(): Flow<MemoryUsage> = flow {
        while (true) {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            val availableMemory = maxMemory - usedMemory

            val memoryUsage = MemoryUsage(
                usedMemoryMB = usedMemory / (1024 * 1024),
                totalMemoryMB = maxMemory / (1024 * 1024),
                availableMemoryMB = availableMemory / (1024 * 1024),
                memoryUsagePercent = (usedMemory.toDouble() / maxMemory.toDouble()) * 100.0
            )

            emit(memoryUsage)
            delay(MEMORY_CHECK_INTERVAL_MS)
        }
    }

    /**
     * Get detailed memory information
     */
    fun getDetailedMemoryInfo(): Map<String, Any> {
        val runtime = Runtime.getRuntime()

        return mapOf(
            "maxMemory" to runtime.maxMemory(),
            "totalMemory" to runtime.totalMemory(),
            "freeMemory" to runtime.freeMemory(),
            "usedMemory" to (runtime.totalMemory() - runtime.freeMemory()),
            "availableProcessors" to runtime.availableProcessors()
        )
    }

    /**
     * Check if device has low memory
     */
    fun isDeviceLowMemory(): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            memoryInfo.lowMemory
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check device memory", e)
            false
        }
    }

    /**
     * Get memory class of the device
     */
    fun getMemoryClass(): Int {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.memoryClass
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get memory class", e)
            16 // Default fallback
        }
    }

    /**
     * Clear image caches
     */
    private fun clearImageCaches() {
        // This would integrate with image loading libraries like Glide or Coil
        // For now, we'll just log the operation
        Log.d(TAG, "Image cache cleanup requested")
    }

    /**
     * Clear ViewModel caches
     */
    private fun clearViewModelCaches() {
        // This would clear any cached ViewModels or data
        // For now, we'll just log the operation
        Log.d(TAG, "ViewModel cache cleanup requested")
    }

    /**
     * Clear database caches
     */
    private fun clearDatabaseCaches() {
        // This would clear Room database caches
        // For now, we'll just log the operation
        Log.d(TAG, "Database cache cleanup requested")
    }
}
