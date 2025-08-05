package gr.eduinvoice.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages offline data storage and synchronization queue
 */
@Singleton
class OfflineDataManager @Inject constructor(
    private val context: Context
) {
    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "offline_data", Context.MODE_PRIVATE
    )
    
    private val offlineDir = File(context.filesDir, "offline")
    private val pendingOperations = ConcurrentHashMap<String, PendingOperation>()
    
    init {
        offlineDir.mkdirs()
    }

    /**
     * Save data for offline access
     */
    suspend fun saveOfflineData(data: Any, type: String, id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(data)
            val file = File(offlineDir, "${type}_${id}.json")
            file.writeText(json)
            
            // Update metadata
            val metadata = getOfflineMetadata()
            metadata[type] = metadata[type]?.toMutableSet()?.apply { add(id) } ?: mutableSetOf(id)
            saveOfflineMetadata(metadata)
            
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retrieve offline data
     */
    suspend fun getOfflineData(type: String, id: String): Any? = withContext(Dispatchers.IO) {
        try {
            val file = File(offlineDir, "${type}_${id}.json")
            if (!file.exists()) return@withContext null
            
            val json = file.readText()
                                when (type) {
                        "student" -> gson.fromJson(json, Student::class.java)
                        "lesson" -> gson.fromJson(json, Lesson::class.java)
                        "group" -> gson.fromJson(json, StudentGroup::class.java)
                        else -> gson.fromJson(json, Any::class.java)
                    }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get all offline data of a specific type
     */
    suspend fun getAllOfflineData(type: String): List<Any> = withContext(Dispatchers.IO) {
        try {
            val metadata = getOfflineMetadata()
            val ids = metadata[type] ?: return@withContext emptyList()
            
            ids.mapNotNull { id ->
                getOfflineData(type, id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Clear offline data
     */
    suspend fun clearOfflineData(type: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            if (type == null) {
                // Clear all offline data
                offlineDir.listFiles()?.forEach { it.delete() }
                prefs.edit().clear().apply()
            } else {
                // Clear specific type
                val metadata = getOfflineMetadata()
                val ids = metadata[type] ?: return@withContext true
                
                ids.forEach { id ->
                    val file = File(offlineDir, "${type}_${id}.json")
                    file.delete()
                }
                
                metadata.remove(type)
                saveOfflineMetadata(metadata)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Add operation to pending queue for later sync
     */
    suspend fun queueOperation(operation: PendingOperation): Boolean = withContext(Dispatchers.IO) {
        try {
            val key = "${operation.type}_${operation.id}_${operation.operationType}"
            pendingOperations[key] = operation
            
            // Save to persistent storage
            val operations = getPendingOperations().toMutableList()
            operations.add(operation)
            savePendingOperations(operations)
            
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get all pending operations
     */
    suspend fun getPendingOperations(): List<PendingOperation> = withContext(Dispatchers.IO) {
        try {
            val json = prefs.getString("pending_operations", "[]")
            val type = object : TypeToken<List<PendingOperation>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Remove pending operation after successful sync
     */
    suspend fun removePendingOperation(operation: PendingOperation): Boolean = withContext(Dispatchers.IO) {
        try {
            val key = "${operation.type}_${operation.id}_${operation.operationType}"
            pendingOperations.remove(key)
            
            val operations = getPendingOperations().toMutableList()
            operations.removeAll { it == operation }
            savePendingOperations(operations)
            
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if there are pending operations
     */
    fun hasPendingOperations(): Boolean {
        return pendingOperations.isNotEmpty()
    }

    /**
     * Get offline data size
     */
    suspend fun getOfflineDataSize(): Long = withContext(Dispatchers.IO) {
        try {
            offlineDir.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Check if offline data exists for a specific item
     */
    suspend fun hasOfflineData(type: String, id: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(offlineDir, "${type}_${id}.json")
        file.exists()
    }

    /**
     * Get offline data metadata
     */
    private fun getOfflineMetadata(): MutableMap<String, MutableSet<String>> {
        val json = prefs.getString("offline_metadata", "{}")
        val type = object : TypeToken<MutableMap<String, MutableSet<String>>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }

    /**
     * Save offline data metadata
     */
    private fun saveOfflineMetadata(metadata: Map<String, Set<String>>) {
        val json = gson.toJson(metadata)
        prefs.edit().putString("offline_metadata", json).apply()
    }

    /**
     * Save pending operations
     */
    private fun savePendingOperations(operations: List<PendingOperation>) {
        val json = gson.toJson(operations)
        prefs.edit().putString("pending_operations", json).apply()
    }

    /**
     * Create backup of offline data
     */
    suspend fun createBackup(): File? = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(context.filesDir, "offline_backup_${System.currentTimeMillis()}.zip")
            // Implementation would use ZipOutputStream to create backup
            // For now, return null to indicate not implemented
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Restore from backup
     */
    suspend fun restoreFromBackup(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            // Implementation would use ZipInputStream to restore backup
            // For now, return false to indicate not implemented
            false
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Represents a pending operation waiting for sync
 */
data class PendingOperation(
    val id: String,
    val type: String,
    val operationType: OperationType,
    val data: String, // JSON string of the data
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
) {
    enum class OperationType {
        CREATE,
        UPDATE,
        DELETE
    }
} 