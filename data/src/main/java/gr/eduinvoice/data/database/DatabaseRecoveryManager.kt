package gr.eduinvoice.data.database

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Database recovery manager to handle migration failures and database corruption
 */
class DatabaseRecoveryManager(private val context: Context) {
    
    companion object {
        private const val TAG = "DatabaseRecoveryManager"
        private const val DATABASE_NAME = "eduinvoice_database"
    }
    
    /**
     * Attempts to recover from database migration failures
     */
    suspend fun attemptRecovery(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.w(TAG, "Attempting database recovery...")
            
            // Check if database exists
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (!dbFile.exists()) {
                Log.i(TAG, "Database file doesn't exist, no recovery needed")
                return@withContext true
            }
            
            // Create backup before attempting recovery
            createBackup(dbFile)
            
            // Try to delete and recreate database
            val success = deleteAndRecreateDatabase(dbFile)
            
            if (success) {
                Log.i(TAG, "Database recovery successful")
            } else {
                Log.e(TAG, "Database recovery failed")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error during database recovery", e)
            false
        }
    }
    
    private fun createBackup(originalFile: File) {
        try {
            val backupFile = File(originalFile.parent, "${originalFile.name}.backup")
            if (originalFile.exists()) {
                originalFile.copyTo(backupFile, overwrite = true)
                Log.d(TAG, "Database backup created: ${backupFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create database backup", e)
        }
    }
    
    private fun deleteAndRecreateDatabase(dbFile: File): Boolean {
        return try {
            // Delete database file
            if (dbFile.exists()) {
                dbFile.delete()
                Log.d(TAG, "Deleted corrupted database file")
            }
            
            // Delete associated files
            val dbDir = dbFile.parentFile
            if (dbDir != null && dbDir.exists()) {
                dbDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith(DATABASE_NAME) && file.name != dbFile.name) {
                        file.delete()
                        Log.d(TAG, "Deleted associated file: ${file.name}")
                    }
                }
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete database files", e)
            false
        }
    }
}
