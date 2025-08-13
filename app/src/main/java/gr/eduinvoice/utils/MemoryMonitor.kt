package gr.eduinvoice.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class MemoryMonitor @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "MemoryMonitor"
        private const val MEMORY_CHECK_INTERVAL_MS = 30_000L
        private const val LOW_MEMORY_THRESHOLD_PERCENT = 70.0
        private const val CRITICAL_MEMORY_THRESHOLD_PERCENT = 85.0
        private const val RECOMMENDED_MAX_USAGE_MB = 100L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val running = AtomicBoolean(false)
    private val snapshotRunnable = object : Runnable {
        override fun run() {
            logSnapshot()
            if (running.get()) handler.postDelayed(this, MEMORY_CHECK_INTERVAL_MS)
        }
    }

    fun start() {
        if (running.compareAndSet(false, true)) handler.post(snapshotRunnable)
    }

    fun stop() {
        running.set(false)
        handler.removeCallbacksAndMessages(null)
    }

    fun logSnapshot() {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        val max = runtime.maxMemory()
        val pct = (used.toDouble() / max.toDouble()) * 100.0
        Log.d(TAG, "mem_used_bytes=$used mem_max_bytes=$max usage_pct=${"%.1f".format(pct)}")
        if (pct > 80) System.gc()
    }

    fun guardOperation(thresholdPct: Int = 90, onAbort: (() -> Unit)? = null, block: () -> Unit) {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        val max = runtime.maxMemory()
        val pct = (used.toDouble() / max.toDouble()) * 100.0
        if (pct > thresholdPct) {
            Log.e(TAG, "Aborting operation due to memory pressure (${"%.1f".format(pct)}%)")
            onAbort?.invoke()
            return
        }
        block()
    }

    data class MemoryStatus(
        val isLowMemory: Boolean,
        val isCriticalMemory: Boolean,
        val usedMemoryMB: Long,
        val totalMemoryMB: Long,
        val availableMemoryMB: Long,
        val memoryUsagePercent: Double,
        val recommendations: List<String>
    )

    data class MemoryUsage(
        val usedMemoryMB: Long,
        val totalMemoryMB: Long,
        val availableMemoryMB: Long,
        val memoryUsagePercent: Double,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class CleanupResult(
        val success: Boolean,
        val freedMemoryMB: Long,
        val operations: List<CleanupOperation>,
        val errors: List<String>
    )

    sealed class CleanupOperation {
        object GarbageCollection : CleanupOperation()
        object ClearImageCache : CleanupOperation()
        object ClearViewModelCache : CleanupOperation()
        object ClearDatabaseCache : CleanupOperation()
        data class CustomOperation(val name: String) : CleanupOperation()
    }

    fun checkMemoryPressure(): MemoryStatus {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        val max = runtime.maxMemory()
        val avail = max - used

        val usedMB = used / (1024 * 1024)
        val totalMB = max / (1024 * 1024)
        val availMB = avail / (1024 * 1024)
        val pct = (used.toDouble() / max.toDouble()) * 100.0

        val isLow = pct >= LOW_MEMORY_THRESHOLD_PERCENT
        val isCritical = pct >= CRITICAL_MEMORY_THRESHOLD_PERCENT

        val recs = mutableListOf<String>()
        if (isCritical) {
            recs += "Critical memory pressure; perform immediate cleanup"
            recs += "Reduce image quality or clear caches"
            recs += "Monitor for memory leaks"
        } else if (isLow) {
            recs += "Low memory pressure; consider cleanup"
        }
        if (usedMB > RECOMMENDED_MAX_USAGE_MB) recs += "Usage exceeds ${RECOMMENDED_MAX_USAGE_MB}MB"

        return MemoryStatus(isLow, isCritical, usedMB, totalMB, availMB, pct, recs)
    }

    fun performCleanup(): CleanupResult {
        val ops = mutableListOf<CleanupOperation>()
        val errors = mutableListOf<String>()
        val initial = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        try {
            try {
                System.gc()
                ops += CleanupOperation.GarbageCollection
            } catch (t: Throwable) {
                errors += "GC failed: ${t.message}"
            }
            try {
                clearImageCaches()
                ops += CleanupOperation.ClearImageCache
            } catch (t: Throwable) { errors += "Image cache cleanup failed: ${t.message}" }
            try {
                clearViewModelCaches()
                ops += CleanupOperation.ClearViewModelCache
            } catch (t: Throwable) { errors += "ViewModel cache cleanup failed: ${t.message}" }
            try {
                clearDatabaseCaches()
                ops += CleanupOperation.ClearDatabaseCache
            } catch (t: Throwable) { errors += "Database cache cleanup failed: ${t.message}" }
        } catch (t: Throwable) {
            errors += "General cleanup failed: ${t.message}"
        }

        val finalMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val freedMB = (initial - finalMem) / (1024 * 1024)
        return CleanupResult(errors.isEmpty(), freedMB, ops, errors)
    }

    fun monitorMemoryUsage(): Flow<MemoryUsage> = flow {
        while (true) {
            val runtime = Runtime.getRuntime()
            val used = runtime.totalMemory() - runtime.freeMemory()
            val max = runtime.maxMemory()
            val avail = max - used
            emit(
                MemoryUsage(
                    usedMemoryMB = used / (1024 * 1024),
                    totalMemoryMB = max / (1024 * 1024),
                    availableMemoryMB = avail / (1024 * 1024),
                    memoryUsagePercent = (used.toDouble() / max.toDouble()) * 100.0
                )
            )
            delay(MEMORY_CHECK_INTERVAL_MS)
        }
    }

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

    fun isDeviceLowMemory(): Boolean = try {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        info.lowMemory
    } catch (t: Throwable) { false }

    fun getMemoryClass(): Int = try {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.memoryClass
    } catch (t: Throwable) { 16 }

    private fun clearImageCaches() { Log.d(TAG, "Image cache cleanup requested") }
    private fun clearViewModelCaches() { Log.d(TAG, "ViewModel cache cleanup requested") }
    private fun clearDatabaseCaches() { Log.d(TAG, "Database cache cleanup requested") }
}

class MemoryMonitorHandle(owner: Any, private val monitor: MemoryMonitor) {
    private val ref = WeakReference(owner)
    fun start() = monitor.start()
    fun stopIfOwnerGone() { if (ref.get() == null) monitor.stop() }
}
 
