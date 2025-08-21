package gr.eduinvoice

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
// import com.google.firebase.sessions.FirebaseSessions
import dagger.hilt.android.HiltAndroidApp
import gr.eduinvoice.analytics.PerformanceMonitor

@HiltAndroidApp
class EduInvoiceApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        configureStrictMode()

        val startupTrace = PerformanceMonitor(this).startAppStartupTrace()
        startupTrace.start()

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)

            // Configure Firebase Crashlytics
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

            // Warm up Firebase Performance instance
            FirebasePerformance.getInstance()

            // Firebase Sessions configuration temporarily disabled due to API changes
            // Configure Firebase Sessions with proper settings to avoid network violations
            // FirebaseSessions.getInstance().apply {
            //     // Disable automatic session collection to avoid network calls on main thread
            //     setSessionsDataCollectionEnabled(false)
            // }

            Log.d("EduInvoiceApplication", "Firebase initialized successfully")
        } catch (t: Throwable) {
            Log.w("EduInvoiceApplication", "Failed to initialize Firebase", t)
            // Don't crash the app if Firebase initialization fails
        }

        // Stop startup trace after initialization
        try {
            startupTrace.stop()
        } catch (_: Throwable) {}
    }

    private fun configureStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .penaltyLog()
                    .build()
            )
        }
    }
}
