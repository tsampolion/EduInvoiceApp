package gr.eduinvoice.analytics

import android.content.Context
import android.util.Log
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartupPerformanceMonitor @Inject constructor(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var startupTrace: Trace? = null
    private val performanceMetrics = mutableMapOf<String, Long>()
    
    fun startStartupTrace() {
        try {
            startupTrace = FirebasePerformance.getInstance().newTrace("app_startup")
            startupTrace?.start()
            Log.d(TAG, "Startup performance trace started")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start startup trace", e)
        }
    }
    
    fun startPhase(phaseName: String): Trace? {
        return try {
            val trace = FirebasePerformance.getInstance().newTrace("startup_$phaseName")
            trace.start()
            performanceMetrics[phaseName] = System.currentTimeMillis()
            Log.d(TAG, "Started phase: $phaseName")
            trace
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start phase trace: $phaseName", e)
            null
        }
    }
    
    fun endPhase(phaseName: String, trace: Trace?) {
        try {
            trace?.stop()
            val startTime = performanceMetrics[phaseName]
            if (startTime != null) {
                val duration = System.currentTimeMillis() - startTime
                Log.d(TAG, "Phase $phaseName completed in ${duration}ms")
                
                // Add custom metric for phase duration
                trace?.putMetric("duration_ms", duration.toLong())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to end phase trace: $phaseName", e)
        }
    }
    
    fun endStartupTrace() {
        try {
            startupTrace?.stop()
            Log.d(TAG, "Startup performance trace completed")
            
            // Log summary of all phases
            val totalDuration = performanceMetrics.values.maxOrNull()?.let { 
                System.currentTimeMillis() - it 
            } ?: 0L
            Log.i(TAG, "Total startup time: ${totalDuration}ms")
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to end startup trace", e)
        }
    }
    
    fun logMainThreadBlocking(duration: Long, operation: String) {
        Log.w(TAG, "Main thread blocked for ${duration}ms during: $operation")
        
        // Add custom metric for main thread blocking
        startupTrace?.putMetric("main_thread_blocking_ms", duration)
    }
    
    companion object {
        private const val TAG = "StartupPerformance"
    }
}
