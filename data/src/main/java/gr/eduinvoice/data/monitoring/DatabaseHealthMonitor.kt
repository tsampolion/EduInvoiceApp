package gr.eduinvoice.data.monitoring

import android.content.Context
import android.util.Log
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.database.DatabaseConstants
import net.sqlcipher.database.SQLiteDatabase
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors database health and performs maintenance operations.
 * Provides comprehensive health checks and recovery mechanisms.
 */
@Singleton
class DatabaseHealthMonitor @Inject constructor(
    private val context: Context,
    private val database: EduInvoiceDatabase
) {

    companion object {
        private const val TAG = "DatabaseHealthMonitor"
        private const val MIN_DB_SIZE_BYTES = 1024L // 1KB minimum
        private const val MAX_DB_SIZE_BYTES = 100 * 1024 * 1024L // 100MB maximum
    }

    /**
     * Comprehensive database health status
     */
    data class DatabaseHealthStatus(
        val isHealthy: Boolean,
        val issues: List<DatabaseIssue>,
        val fileSize: Long,
        val lastModified: Long,
        val integrityCheck: Boolean,
        val performanceMetrics: PerformanceMetrics
    )

    /**
     * Database issues that can be detected
     */
    sealed class DatabaseIssue {
        object FileCorrupted : DatabaseIssue()
        object FileTooSmall : DatabaseIssue()
        object FileTooLarge : DatabaseIssue()
        object FileNotFound : DatabaseIssue()
        object IntegrityCheckFailed : DatabaseIssue()
        object PerformanceDegraded : DatabaseIssue()
        data class CustomIssue(val message: String) : DatabaseIssue()
    }

    /**
     * Performance metrics for database operations
     */
    data class PerformanceMetrics(
        val averageQueryTime: Long,
        val slowQueries: Int,
        val connectionCount: Int,
        val memoryUsage: Long
    )

    /**
     * Maintenance operation results
     */
    data class MaintenanceResult(
        val success: Boolean,
        val operations: List<MaintenanceOperation>,
        val errors: List<String>
    )

    /**
     * Types of maintenance operations
     */
    sealed class MaintenanceOperation {
        object Vacuum : MaintenanceOperation()
        object Reindex : MaintenanceOperation()
        object IntegrityCheck : MaintenanceOperation()
        object Optimize : MaintenanceOperation()
        data class CustomOperation(val name: String) : MaintenanceOperation()
    }

    /**
     * Integrity validation results
     */
    data class IntegrityResult(
        val isValid: Boolean,
        val issues: List<String>,
        val recommendations: List<String>
    )

    /**
     * Check overall database health
     */
    suspend fun checkDatabaseHealth(): DatabaseHealthStatus {
        val issues = mutableListOf<DatabaseIssue>()
        val dbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)

        // Check if this is an in-memory database (file doesn't exist)
        val isInMemoryDatabase = !dbFile.exists()

        val fileSize = if (isInMemoryDatabase) 0L else dbFile.length()
        val lastModified = if (isInMemoryDatabase) System.currentTimeMillis() else dbFile.lastModified()

        // For in-memory databases, skip file-based checks
        if (!isInMemoryDatabase) {
            // Check file size
            if (fileSize < MIN_DB_SIZE_BYTES) {
                issues.add(DatabaseIssue.FileTooSmall)
            }
            if (fileSize > MAX_DB_SIZE_BYTES) {
                issues.add(DatabaseIssue.FileTooLarge)
            }
        }

        // Perform integrity check
        val integrityCheck = performIntegrityCheck()
        if (!integrityCheck) {
            issues.add(DatabaseIssue.IntegrityCheckFailed)
        }

        // Check performance
        val performanceMetrics = measurePerformance()
        if (performanceMetrics.slowQueries > 5) {
            issues.add(DatabaseIssue.PerformanceDegraded)
        }

        return DatabaseHealthStatus(
            isHealthy = issues.isEmpty(),
            issues = issues,
            fileSize = fileSize,
            lastModified = lastModified,
            integrityCheck = integrityCheck,
            performanceMetrics = performanceMetrics
        )
    }

    /**
     * Perform database maintenance operations
     */
    suspend fun performMaintenance(): MaintenanceResult {
        val operations = mutableListOf<MaintenanceOperation>()
        val errors = mutableListOf<String>()

        try {
            // Perform VACUUM operation
            try {
                database.openHelper.writableDatabase.execSQL("VACUUM")
                operations.add(MaintenanceOperation.Vacuum)
                Log.d(TAG, "VACUUM operation completed successfully")
            } catch (e: Exception) {
                errors.add("VACUUM failed: ${e.message}")
                Log.e(TAG, "VACUUM operation failed", e)
            }

            // Perform REINDEX operation
            try {
                database.openHelper.writableDatabase.execSQL("REINDEX")
                operations.add(MaintenanceOperation.Reindex)
                Log.d(TAG, "REINDEX operation completed successfully")
            } catch (e: Exception) {
                errors.add("REINDEX failed: ${e.message}")
                Log.e(TAG, "REINDEX operation failed", e)
            }

            // Perform integrity check
            try {
                val integrityResult = validateDataIntegrity()
                if (integrityResult.isValid) {
                    operations.add(MaintenanceOperation.IntegrityCheck)
                    Log.d(TAG, "Integrity check passed")
                } else {
                    errors.add("Integrity check failed: ${integrityResult.issues.joinToString(", ")}")
                }
            } catch (e: Exception) {
                errors.add("Integrity check failed: ${e.message}")
                Log.e(TAG, "Integrity check failed", e)
            }

            // Perform optimization
            try {
                database.openHelper.writableDatabase.execSQL("PRAGMA optimize")
                operations.add(MaintenanceOperation.Optimize)
                Log.d(TAG, "Optimization completed successfully")
            } catch (e: Exception) {
                errors.add("Optimization failed: ${e.message}")
                Log.e(TAG, "Optimization failed", e)
            }

        } catch (e: Exception) {
            errors.add("Maintenance operation failed: ${e.message}")
            Log.e(TAG, "Maintenance operation failed", e)
        }

        return MaintenanceResult(
            success = errors.isEmpty(),
            operations = operations,
            errors = errors
        )
    }

    /**
     * Validate data integrity across all tables
     */
    suspend fun validateDataIntegrity(): IntegrityResult {
        val issues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        try {
            val db = database.openHelper.writableDatabase

            // Check all tables exist
            val tables = listOf(
                DatabaseConstants.STUDENTS_TABLE,
                DatabaseConstants.LESSONS_TABLE,
                DatabaseConstants.GROUPS_TABLE,
                DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE,
                DatabaseConstants.USERS_TABLE
            )

            for (table in tables) {
                try {
                    val cursor = db.query("SELECT COUNT(*) FROM $table")
                    cursor.use {
                        if (it.moveToFirst()) {
                            val count = it.getInt(0)
                            Log.d(TAG, "Table $table has $count records")
                        }
                    }
                } catch (e: Exception) {
                    issues.add("Table $table is corrupted or missing: ${e.message}")
                    recommendations.add("Consider restoring from backup or recreating table $table")
                }
            }

            // Check for orphaned records
            checkForOrphanedRecords(db, issues, recommendations)

            // Check for data consistency
            checkDataConsistency(db, issues, recommendations)

        } catch (e: Exception) {
            issues.add("Database access failed: ${e.message}")
            recommendations.add("Check database file permissions and disk space")
        }

        return IntegrityResult(
            isValid = issues.isEmpty(),
            issues = issues,
            recommendations = recommendations
        )
    }

                /**
             * Check for orphaned records in cross-reference tables
             */
            private fun checkForOrphanedRecords(
                db: androidx.sqlite.db.SupportSQLiteDatabase,
                issues: MutableList<String>,
                recommendations: MutableList<String>
            ) {
                try {
                    // Check for orphaned group-student references
                    val orphanedRefs = db.query("""
                        SELECT gscr.groupId, gscr.studentId
                        FROM ${DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE} gscr
                        LEFT JOIN ${DatabaseConstants.GROUPS_TABLE} g ON gscr.groupId = g.id
                        LEFT JOIN ${DatabaseConstants.STUDENTS_TABLE} s ON gscr.studentId = s.id
                        WHERE g.id IS NULL OR s.id IS NULL
                    """.trimIndent())

                    orphanedRefs.use { cursor ->
                        if (cursor.count > 0) {
                            issues.add("Found ${cursor.count} orphaned group-student references")
                            recommendations.add("Clean up orphaned references in group-student cross-reference table")
                        }
                    }

                    // Check for orphaned lessons
                    val orphanedLessons = db.query("""
                        SELECT l.id, l.studentId, l.groupId
                        FROM ${DatabaseConstants.LESSONS_TABLE} l
                        LEFT JOIN ${DatabaseConstants.STUDENTS_TABLE} s ON l.studentId = s.id
                        LEFT JOIN ${DatabaseConstants.GROUPS_TABLE} g ON l.groupId = g.id
                        WHERE (l.studentId IS NOT NULL AND s.id IS NULL)
                           OR (l.groupId IS NOT NULL AND g.id IS NULL)
                    """.trimIndent())

                    orphanedLessons.use { cursor ->
                        if (cursor.count > 0) {
                            issues.add("Found ${cursor.count} orphaned lessons")
                            recommendations.add("Clean up orphaned lessons or restore missing students/groups")
                        }
                    }

                } catch (e: Exception) {
                    issues.add("Failed to check for orphaned records: ${e.message}")
                }
            }

                /**
             * Check data consistency across tables
             */
            private fun checkDataConsistency(
                db: androidx.sqlite.db.SupportSQLiteDatabase,
                issues: MutableList<String>,
                recommendations: MutableList<String>
            ) {
                try {
                    // Check for lessons with invalid dates
                    val invalidDates = db.query("""
                        SELECT COUNT(*) FROM ${DatabaseConstants.LESSONS_TABLE}
                        WHERE date IS NULL OR date = '' OR date = '0000-00-00'
                    """.trimIndent())

                    invalidDates.use { cursor ->
                        if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                            issues.add("Found lessons with invalid dates")
                            recommendations.add("Fix lessons with invalid dates")
                        }
                    }

                    // Check for students with invalid rates
                    val invalidRates = db.query("""
                        SELECT COUNT(*) FROM ${DatabaseConstants.STUDENTS_TABLE}
                        WHERE rate IS NULL OR rate = '' OR CAST(rate AS REAL) <= 0
                    """.trimIndent())

                    invalidRates.use { cursor ->
                        if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                            issues.add("Found students with invalid rates")
                            recommendations.add("Fix students with invalid or zero rates")
                        }
                    }

                } catch (e: Exception) {
                    issues.add("Failed to check data consistency: ${e.message}")
                }
            }

    /**
     * Perform basic integrity check
     */
    private fun performIntegrityCheck(): Boolean {
        return try {
            val db = database.openHelper.writableDatabase
            val cursor = db.query("PRAGMA integrity_check")
            cursor.use {
                if (it.moveToFirst()) {
                    val result = it.getString(0)
                    result == "ok"
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Integrity check failed", e)
            false
        }
    }

                /**
             * Measure database performance metrics
             */
            private fun measurePerformance(): PerformanceMetrics {
                val startTime = System.currentTimeMillis()

                // Simulate a simple query to measure performance
                val queryTime = try {
                    val db = database.openHelper.readableDatabase
                    val queryStart = System.currentTimeMillis()
                    val cursor = db.query("SELECT COUNT(*) FROM ${DatabaseConstants.STUDENTS_TABLE}")
                    cursor.use { it.moveToFirst() }
                    System.currentTimeMillis() - queryStart
                } catch (e: Exception) {
                    Log.e(TAG, "Performance measurement failed", e)
                    0L
                }

                val totalTime = System.currentTimeMillis() - startTime

                return PerformanceMetrics(
                    averageQueryTime = queryTime,
                    slowQueries = if (queryTime > 1000) 1 else 0, // Consider queries > 1s as slow
                    connectionCount = 1, // Simplified for now
                    memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                )
            }

    /**
     * Get database file information
     */
    fun getDatabaseFileInfo(): FileInfo {
        val dbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
        return FileInfo(
            exists = dbFile.exists(),
            size = if (dbFile.exists()) dbFile.length() else 0L,
            lastModified = if (dbFile.exists()) dbFile.lastModified() else 0L,
            path = dbFile.absolutePath
        )
    }

    /**
     * Database file information
     */
    data class FileInfo(
        val exists: Boolean,
        val size: Long,
        val lastModified: Long,
        val path: String
    )
}
