package gr.eduinvoice.analytics

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

object PerformanceTraces {
    fun <T> trace(name: String, block: () -> T): T {
        return try {
            val perf = FirebasePerformance.getInstance()
            val trace: Trace = perf.newTrace(name)
            trace.start()
            try {
                block()
            } finally {
                trace.stop()
            }
        } catch (_: Throwable) {
            block()
        }
    }

    suspend fun <T> traceSuspend(name: String, block: suspend () -> T): T {
        return try {
            val perf = FirebasePerformance.getInstance()
            val trace: Trace = perf.newTrace(name)
            trace.start()
            try {
                block()
            } finally {
                trace.stop()
            }
        } catch (_: Throwable) {
            block()
        }
    }
}



