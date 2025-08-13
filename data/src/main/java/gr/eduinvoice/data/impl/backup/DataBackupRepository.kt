package gr.eduinvoice.data.impl.backup

import gr.eduinvoice.data.repository.BackupRepository as DataBackupRepository
import gr.eduinvoice.domain.repository.BackupRepository
import gr.eduinvoice.domain.repository.BackupInfo
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data-side implementation of BackupRepository domain interface.
 * Delegates to the existing data layer BackupRepository implementation.
 */
@Singleton
class DataBackupRepository @Inject constructor(
    private val dataBackupRepository: DataBackupRepository
) : BackupRepository {

    override suspend fun exportJson(): String {
        return dataBackupRepository.exportJson()
    }

    override suspend fun restoreFromJson(json: String): Result<Unit> {
        return dataBackupRepository.restoreFromJson(json)
    }

    override suspend fun createBackup(outputFile: File): Result<File> {
        // The data layer doesn't have this exact method, so we'll need to implement it
        // For now, return a failure - this can be enhanced later
        return Result.failure(UnsupportedOperationException("createBackup not yet implemented"))
    }

    override suspend fun restoreBackup(backupFile: File): Result<Unit> {
        // The data layer doesn't have this exact method, so we'll need to implement it
        // For now, return a failure - this can be enhanced later
        return Result.failure(UnsupportedOperationException("restoreBackup not yet implemented"))
    }

    override suspend fun listBackups(): List<File> {
        // The data layer doesn't have this exact method, so we'll need to implement it
        // For now, return empty list - this can be enhanced later
        return emptyList()
    }

    override suspend fun deleteBackup(backupFile: File): Result<Unit> {
        // The data layer doesn't have this exact method, so we'll need to implement it
        // For now, return a failure - this can be enhanced later
        return Result.failure(UnsupportedOperationException("deleteBackup not yet implemented"))
    }

    override suspend fun getBackupInfo(backupFile: File): BackupInfo? {
        // The data layer doesn't have this exact method, so we'll need to implement it
        // For now, return null - this can be enhanced later
        return null
    }

    override suspend fun validateBackup(backupFile: File): Boolean {
        // The data layer doesn't have this exact method, so we'll need to implement it
        // For now, return false - this can be enhanced later
        return false
    }
}
