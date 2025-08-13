package gr.eduinvoice.domain.repository

import java.io.File

/**
 * Domain interface for backup repository.
 * This allows the app module to depend on domain abstractions rather than data layer implementations.
 *
 * Added during app→domain migration. Backed by data implementation.
 */
interface BackupRepository {
    /**
     * Export backup as JSON string
     */
    suspend fun exportJson(): String

    /**
     * Restore backup from JSON string
     */
    suspend fun restoreFromJson(json: String): Result<Unit>

    /**
     * Create a backup of the application data
     */
    suspend fun createBackup(outputFile: File): Result<File>

    /**
     * Restore application data from a backup file
     */
    suspend fun restoreBackup(backupFile: File): Result<Unit>

    /**
     * List available backup files
     */
    suspend fun listBackups(): List<File>

    /**
     * Delete a backup file
     */
    suspend fun deleteBackup(backupFile: File): Result<Unit>

    /**
     * Get backup file information
     */
    suspend fun getBackupInfo(backupFile: File): BackupInfo?

    /**
     * Check if backup is valid
     */
    suspend fun validateBackup(backupFile: File): Boolean
}

/**
 * Data class for backup information
 */
data class BackupInfo(
    val fileName: String,
    val fileSize: Long,
    val creationDate: Long,
    val version: String,
    val recordCount: Int
)
