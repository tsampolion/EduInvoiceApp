package gr.eduinvoice.analytics

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

object PerformanceTraces {
    fun <T> trace(name: String, block: () -> T): T {
        val perf = FirebasePerformance.getInstance()
        val trace: Trace = perf.newTrace(name)
        trace.start()
        return try {
            block()
        } finally {
            trace.stop()
        }
    }
}



