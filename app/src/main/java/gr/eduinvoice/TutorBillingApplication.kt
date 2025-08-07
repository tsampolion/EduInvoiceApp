package gr.eduinvoice

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
// import com.google.firebase.sessions.FirebaseSessions
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TutorBillingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            
            // Configure Firebase Crashlytics
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            
            // Firebase Sessions configuration temporarily disabled due to API changes
            // Configure Firebase Sessions with proper settings to avoid network violations
            // FirebaseSessions.getInstance().apply {
            //     // Disable automatic session collection to avoid network calls on main thread
            //     setSessionsDataCollectionEnabled(false)
            // }
            
            Log.d("TutorBillingApplication", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.w("TutorBillingApplication", "Failed to initialize Firebase", e)
            // Don't crash the app if Firebase initialization fails
        }
    }
}
