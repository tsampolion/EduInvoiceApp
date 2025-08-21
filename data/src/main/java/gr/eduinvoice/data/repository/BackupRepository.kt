package gr.eduinvoice.data.repository

import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.room.withTransaction
import gr.eduinvoice.data.database.DatabaseConstants
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val context: Context,
    private val db: EduInvoiceDatabase
) {

    companion object {
        private const val TAG = "BackupRepository"
        private const val BACKUP_DIR = "backups"
        private const val MAX_BACKUP_FILES = 10
        private const val BACKUP_RETENTION_DAYS = 30
    }
    @Serializable
    data class BackupDump(
        val students: List<Student>,
        val lessons: List<Lesson>,
        val groups: List<StudentGroup>,
        val crossRefs: List<GroupStudentCrossRef>,
        val users: List<User>
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun exportJson(): String {
        return withContext(Dispatchers.IO) {
            val readable = db.openHelper.readableDatabase
            val students = readable.query("SELECT * FROM ${DatabaseConstants.STUDENTS_TABLE}").use { cursor ->
                generateSequence { if (cursor.moveToNext()) cursor else null }
                    .map { Student(
                        id = it.getLong(0),
                        ownerId = it.getLong(1),
                        name = it.getString(2),
                        surname = it.getString(3),
                        parentMobile = it.getString(4),
                        parentEmail = it.getString(5),
                        className = it.getString(6),
                        rate = it.getDouble(7),
                        rateType = it.getString(8),
                        isActive = it.getInt(9) == 1
                    ) }.toList()
            }
            val lessons = readable.query("SELECT * FROM ${DatabaseConstants.LESSONS_TABLE}").use { c ->
                generateSequence { if (c.moveToNext()) c else null }
                    .map { Lesson(
                        id = it.getLong(0),
                        ownerId = it.getLong(1),
                        studentId = it.getLong(2),
                        groupId = if (it.isNull(3)) null else it.getLong(3),
                        date = it.getString(4),
                        startTime = it.getString(5),
                        durationMinutes = it.getInt(6),
                        notes = it.getString(7),
                        isPaid = it.getInt(8) == 1,
                        isInvoiced = it.getInt(9) == 1
                    ) }.toList()
            }
            val groups = readable.query("SELECT * FROM ${DatabaseConstants.GROUPS_TABLE}").use { c ->
                generateSequence { if (c.moveToNext()) c else null }
                    .map { StudentGroup(
                        id = it.getLong(0),
                        ownerId = it.getLong(1),
                        name = it.getString(2)
                    ) }.toList()
            }
            val crossRefs = readable.query("SELECT * FROM ${DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE}").use { c ->
                generateSequence { if (c.moveToNext()) c else null }
                    .map { GroupStudentCrossRef(
                        groupId = it.getLong(0),
                        studentId = it.getLong(1),
                        ownerId = it.getLong(2)
                    ) }.toList()
            }
            val users = readable.query("SELECT * FROM ${DatabaseConstants.USERS_TABLE}").use { c ->
                generateSequence { if (c.moveToNext()) c else null }
                    .map { User(
                        id = it.getLong(0),
                        username = it.getString(1),
                        passwordHash = it.getString(2),
                        fullName = it.getString(3),
                        subjectSpecialty = it.getString(4),
                        yearsExperience = it.getInt(5)
                    ) }.toList()
            }
            val dump = BackupDump(students, lessons, groups, crossRefs, users)
            json.encodeToString(BackupDump.serializer(), dump)
        }
    }

    suspend fun restoreFromJson(json: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val element = this@BackupRepository.json.parseToJsonElement(json).jsonObject
            val required = listOf("students", "lessons", "groups", "crossRefs", "users")
            if (!required.all { element.containsKey(it) }) {
                return@withContext Result.failure(IllegalArgumentException("Backup JSON missing required fields"))
            }
            val dump = this@BackupRepository.json.decodeFromString(BackupDump.serializer(), json)
            db.withTransaction {
                db.clearAllTables()
                val studentDao = db.studentDao()
                val lessonDao = db.lessonDao()
                val groupDao = db.groupDao()
                val userDao = db.userDao()
                for (u in dump.users) { userDao.insert(u) }
                for (s in dump.students) { studentDao.insert(s) }
                for (g in dump.groups) { groupDao.insertGroup(g) }
                for (cr in dump.crossRefs) { groupDao.insertCrossRef(cr) }
                for (l in dump.lessons) { lessonDao.insert(l) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BackupRepository", "restoreFromJson failed", e)
            Result.failure(e)
        }
    }

    /**
     * Create automatic backup before risky operations
     */
    suspend fun createAutomaticBackup(): BackupResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Creating automatic backup")

                // Create backup directory if it doesn't exist
                val backupDir = File(context.filesDir, BACKUP_DIR)
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }

                // Generate backup filename with timestamp
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val backupFileName = "auto_backup_$timestamp.json"
                val backupFile = File(backupDir, backupFileName)

                // Export data to JSON
                val jsonData = exportJson()

                // Write to file
                backupFile.writeText(jsonData)

                // Clean up old backups
                cleanupOldBackups(backupDir)

                Log.i(TAG, "Automatic backup created successfully: ${backupFile.absolutePath}")
                BackupResult.Success(backupFile.absolutePath)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to create automatic backup", e)
                BackupResult.Failure("Failed to create automatic backup: ${e.message}")
            }
        }
    }

    /**
     * Restore from automatic backup
     */
    suspend fun restoreFromAutomaticBackup(): RestoreResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Attempting to restore from automatic backup")

                val backupDir = File(context.filesDir, BACKUP_DIR)
                if (!backupDir.exists()) {
                    return@withContext RestoreResult.Failure("No backup directory found")
                }

                // Find the most recent backup file
                val backupFiles = backupDir.listFiles { file ->
                    file.name.startsWith("auto_backup_") && file.name.endsWith(".json")
                }?.sortedByDescending { it.lastModified() }

                if (backupFiles.isNullOrEmpty()) {
                    return@withContext RestoreResult.Failure("No automatic backup files found")
                }

                val latestBackup = backupFiles.first()
                Log.i(TAG, "Restoring from backup: ${latestBackup.name}")

                // Read backup file
                val jsonData = latestBackup.readText()

                // Restore data
                val restoreResult = restoreFromJson(jsonData)
                if (restoreResult.isSuccess) {
                    Log.i(TAG, "Automatic backup restore completed successfully")
                    RestoreResult.Success("Restored from ${latestBackup.name}")
                } else {
                    Log.e(TAG, "Automatic backup restore failed")
                    RestoreResult.Failure("Restore failed: ${restoreResult.exceptionOrNull()?.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore from automatic backup", e)
                RestoreResult.Failure("Failed to restore from automatic backup: ${e.message}")
            }
        }
    }

    /**
     * Get list of available backups
     */
    fun getAvailableBackups(): List<BackupInfo> {
        return try {
            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (!backupDir.exists()) {
                return emptyList()
            }

            backupDir.listFiles { file ->
                file.name.endsWith(".json")
            }?.map { file ->
                BackupInfo(
                    name = file.name,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    path = file.absolutePath
                )
            }?.sortedByDescending { it.lastModified } ?: emptyList()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get available backups", e)
            emptyList()
        }
    }

    /**
     * Clean up old backup files
     */
    private fun cleanupOldBackups(backupDir: File) {
        try {
            val backupFiles = backupDir.listFiles { file ->
                file.name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() }

            if (backupFiles != null) {
                // Remove files exceeding maximum count
                if (backupFiles.size > MAX_BACKUP_FILES) {
                    val filesToDelete = backupFiles.drop(MAX_BACKUP_FILES)
                    filesToDelete.forEach { file ->
                        if (file.delete()) {
                            Log.i(TAG, "Deleted old backup file: ${file.name}")
                        }
                    }
                }

                // Remove files older than retention period
                val cutoffTime = System.currentTimeMillis() - (BACKUP_RETENTION_DAYS * 24 * 60 * 60 * 1000L)
                backupFiles.forEach { file ->
                    if (file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            Log.i(TAG, "Deleted expired backup file: ${file.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup old backups", e)
        }
    }

    /**
     * Backup operation results
     */
    sealed class BackupResult {
        data class Success(val filePath: String) : BackupResult()
        data class Failure(val message: String) : BackupResult()
    }

    /**
     * Restore operation results
     */
    sealed class RestoreResult {
        data class Success(val message: String) : RestoreResult()
        data class Failure(val message: String) : RestoreResult()
    }

    /**
     * Backup file information
     */
    data class BackupInfo(
        val name: String,
        val size: Long,
        val lastModified: Long,
        val path: String
    )
}
