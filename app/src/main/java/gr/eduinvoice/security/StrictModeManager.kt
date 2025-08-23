package gr.eduinvoice.security

import android.os.StrictMode
import android.util.Log
import gr.eduinvoice.analytics.StartupPerformanceMonitor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StrictModeManager @Inject constructor(
    private val performanceMonitor: StartupPerformanceMonitor
) {
    
    fun configureStrictMode(isDebug: Boolean) {
        if (!isDebug) return
        
        try {
            // Configure thread policy to detect main thread violations
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .detectResourceMismatches()
                    .penaltyLog()
                    .penaltyDeathOnNetwork()
                    .build()
            )
            
            // Configure VM policy for memory leaks and other issues
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .detectFileUriExposure()
                    .detectCleartextNetwork()
                    .penaltyLog()
                    .build()
            )
            
            Log.d(TAG, "StrictMode configured for debug build")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to configure StrictMode", e)
        }
    }
    
    fun logViolation(violation: String, duration: Long) {
        Log.w(TAG, "StrictMode violation: $violation (${duration}ms)")
        
        // Report to performance monitor
        performanceMonitor.logMainThreadBlocking(duration, violation)
        
        // Additional logging for critical violations
        if (duration > 100) {
            Log.e(TAG, "CRITICAL: Main thread blocked for ${duration}ms during $violation")
        }
    }
    
    companion object {
        private const val TAG = "StrictModeManager"
    }
}
