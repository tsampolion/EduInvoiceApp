package gr.eduinvoice.analytics

import android.content.Context
import android.util.Log
import gr.eduinvoice.BuildConfig
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace as FirebaseTrace

@Singleton
class PerformanceMonitor @Inject constructor(
    private val context: Context
) {

    fun startTrace(traceName: String): Trace {
        return Trace(traceName)
    }

    fun startAppStartupTrace(): Trace {
        return startTrace("app_startup")
    }

    fun monitorScreenLoad(screenName: String) {
        val trace = startTrace("screen_load_$screenName")
        trace.start()

        // Stop trace when screen is fully loaded
        trace.stop()
    }

    fun monitorOperation(operationName: String, operation: suspend () -> Unit) {
        val trace = startTrace(operationName)
        trace.start()

        runBlocking {
            operation()
        }

        trace.stop()
    }

    fun monitorDatabaseOperation(operationName: String, operation: suspend () -> Unit) {
        val trace = startTrace("db_$operationName")
        trace.start()

        runBlocking {
            operation()
        }

        trace.stop()
    }

    fun monitorNetworkOperation(operationName: String, operation: suspend () -> Unit) {
        val trace = startTrace("network_$operationName")
        trace.start()

        runBlocking {
            operation()
        }

        trace.stop()
    }

    class Trace(private val name: String) {
        private var startTime: Long = 0
        private var endTime: Long = 0
        private var firebaseTrace: FirebaseTrace? = null

        fun start() {
            startTime = System.currentTimeMillis()
            try {
                firebaseTrace = FirebasePerformance.getInstance().newTrace(name).also { it.start() }
            } catch (t: Throwable) {
                // Ignore Firebase failures in production path
            }
            if (BuildConfig.DEBUG) {
                Log.d("PerformanceMonitor", "Started trace: $name")
            }
        }

        fun stop() {
            endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            try {
                firebaseTrace?.putMetric("duration_ms", duration)
                firebaseTrace?.stop()
            } catch (t: Throwable) {
                // Ignore Firebase failures in production path
            }
            if (BuildConfig.DEBUG) {
                Log.d("PerformanceMonitor", "Trace $name completed in ${duration}ms")
            }
            // Bridge to Firebase Performance custom metrics if needed
        }

        fun putMetric(key: String, value: Long) {
            try { firebaseTrace?.putMetric(key, value) } catch (_: Throwable) {}
            if (BuildConfig.DEBUG) {
                Log.d("PerformanceMonitor", "Metric for $name: $key = $value")
            }
        }
    }
}
