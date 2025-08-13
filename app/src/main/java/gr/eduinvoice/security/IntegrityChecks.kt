package gr.eduinvoice.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

object IntegrityChecks {
    fun isDeviceCompromised(context: Context): Boolean {
        val appInfo = context.applicationInfo
        val isDebuggable = appInfo != null && (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val isEmulator = Build.FINGERPRINT.lowercase().contains("generic") ||
            Build.MODEL.contains("google_sdk") || Build.MODEL.lowercase().contains("emulator") ||
            Build.MANUFACTURER.lowercase().contains("genymotion")
        val adbEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Throwable) { false }
        val compromised = isDebuggable || isEmulator || adbEnabled
        if (compromised) {
            FirebaseCrashlytics.getInstance().log("Integrity: compromised=$compromised debug=$isDebuggable emulator=$isEmulator adb=$adbEnabled")
        }
        return compromised
    }

    // Stub for Play Integrity; integrate server-side if required.
    fun verifyPlayIntegrity(context: Context): Boolean {
        // In production, call Play Integrity API; here we just log and return true as stub.
        FirebaseCrashlytics.getInstance().log("Integrity: verifyPlayIntegrity stub called")
        return true
    }

    fun isTampered(context: Context): Boolean {
        return try {
            // Basic tamper heuristic: debug build or unknown installer
            val installer = context.packageManager.getInstallerPackageName(context.packageName)
            val appInfo = context.applicationInfo
            val isDebuggable = (appInfo?.flags ?: 0) and ApplicationInfo.FLAG_DEBUGGABLE != 0
            val tampered = installer == null && !isDebuggable
            if (tampered) FirebaseCrashlytics.getInstance().log("Integrity: possible tamper detected")
            tampered
        } catch (t: Throwable) {
            Log.w("IntegrityChecks", "Tamper check failed", t)
            false
        }
    }
}



