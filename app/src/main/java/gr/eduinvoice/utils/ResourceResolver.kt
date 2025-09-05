package gr.eduinvoice.utils

import android.content.Context
import android.content.res.Resources
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceResolver @Inject constructor(
    private val context: Context
) {

    /**
     * Safely resolve a resource ID, returning null if the resource doesn't exist
     */
    fun safeResolveResource(resourceId: Int): Int? {
        return try {
            val resourceName = context.resources.getResourceName(resourceId)
            Log.d(TAG, "Successfully resolved resource: $resourceName")
            resourceId
        } catch (e: Resources.NotFoundException) {
            Log.w(TAG, "Resource not found: 0x${resourceId.toString(16)}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving resource: 0x${resourceId.toString(16)}", e)
            null
        }
    }

    /**
     * Check if a resource exists before using it
     */
    fun resourceExists(resourceId: Int): Boolean {
        return try {
            context.resources.getResourceName(resourceId)
            true
        } catch (e: Resources.NotFoundException) {
            false
        }
    }

    /**
     * Get resource name safely
     */
    fun getResourceName(resourceId: Int): String? {
        return try {
            context.resources.getResourceName(resourceId)
        } catch (e: Resources.NotFoundException) {
            Log.w(TAG, "Resource not found: 0x${resourceId.toString(16)}")
            null
        }
    }

    /**
     * Log all resource resolution issues for debugging
     */
    fun logResourceIssues() {
        try {
            val packageId = context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo?.uid ?: 0
            Log.d(TAG, "Current package ID: $packageId")

            // Check for common resource issues
            val commonResources = listOf(
                android.R.string.ok,
                android.R.string.cancel,
                android.R.string.yes,
                android.R.string.no
            )

            commonResources.forEach { resourceId ->
                if (resourceExists(resourceId)) {
                    Log.d(TAG, "System resource exists: 0x${resourceId.toString(16)}")
                } else {
                    Log.w(TAG, "System resource missing: 0x${resourceId.toString(16)}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log resource issues", e)
        }
    }

    companion object {
        private const val TAG = "ResourceResolver"
    }
}
