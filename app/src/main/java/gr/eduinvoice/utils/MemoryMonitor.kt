package gr.eduinvoice.utils

import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lightweight memory metrics logger with periodic snapshots and GC pressure logging.
 * Safe for release; logs are verbose only on debug builds via the caller.
 */
class MemoryMonitor(
    private val tag: String = "MemoryMonitor",
    private val intervalMs: Long = 5_000L
) {
    private val handler = Handler(Looper.getMainLooper())
    private val running = AtomicBoolean(false)
    private val tickRunnable = object : Runnable {
        override fun run() {
            logSnapshot()
            if (running.get()) {
                handler.postDelayed(this, intervalMs)
            }
        }
    }

    fun start() {
        if (running.compareAndSet(false, true)) {
            handler.post(tickRunnable)
        }
    }

    fun stop() {
        running.set(false)
        handler.removeCallbacksAndMessages(null)
    }

    fun logSnapshot() {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        val max = runtime.maxMemory()
        val usagePct = (used.toDouble() / max.toDouble()) * 100.0
        Log.d(tag, "mem_used_bytes=$used mem_max_bytes=$max usage_pct=${"%.1f".format(usagePct)}")
        if (usagePct > 80) {
            Log.w(tag, "High memory usage detected; triggering GC suggestion")
            System.gc()
        }
    }

    /** Guard long-running operations. Aborts via callback if memory is too high. */
    fun guardOperation(
        thresholdPct: Int = 90,
        onAbort: (() -> Unit)? = null,
        block: () -> Unit
    ) {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        val max = runtime.maxMemory()
        val usagePct = (used.toDouble() / max.toDouble()) * 100.0
        if (usagePct > thresholdPct) {
            Log.e(tag, "Aborting operation due to memory pressure ($usagePct%)")
            onAbort?.invoke()
            return
        }
        block()
    }
}

/** Simple helper to attach a monitor lifecycle to an owner via WeakReference. */
class MemoryMonitorHandle(owner: Any, private val monitor: MemoryMonitor) {
    private val ref = WeakReference(owner)
    fun start() = monitor.start()
    fun stopIfOwnerGone() {
        if (ref.get() == null) monitor.stop()
    }
}
 
