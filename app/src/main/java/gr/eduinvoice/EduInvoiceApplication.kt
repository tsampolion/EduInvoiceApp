package gr.eduinvoice

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
// import com.google.firebase.sessions.FirebaseSessions
import dagger.hilt.android.HiltAndroidApp
import gr.eduinvoice.analytics.StartupPerformanceMonitor
import gr.eduinvoice.security.StrictModeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class EduInvoiceApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    @Inject lateinit var strictModeManager: StrictModeManager
    @Inject lateinit var startupPerformanceMonitor: StartupPerformanceMonitor
    
    override fun onCreate() {
        super.onCreate()

        // Configure StrictMode for debug builds
        strictModeManager.configureStrictMode(BuildConfig.DEBUG)

        // Start startup performance monitoring
        startupPerformanceMonitor.startStartupTrace()

        // Initialize Firebase asynchronously to prevent main thread blocking
        initializeFirebaseAsync()

        // End startup trace after initialization
        try {
            startupPerformanceMonitor.endStartupTrace()
        } catch (_: Throwable) {}
    }

    private fun initializeFirebaseAsync() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                // Initialize Firebase on background thread
                FirebaseApp.initializeApp(this@EduInvoiceApplication)

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

                Log.d("EduInvoiceApplication", "Firebase initialized successfully on background thread")
            } catch (t: Throwable) {
                Log.w("EduInvoiceApplication", "Failed to initialize Firebase", t)
                // Don't crash the app if Firebase initialization fails
            }
        }
    }
}
