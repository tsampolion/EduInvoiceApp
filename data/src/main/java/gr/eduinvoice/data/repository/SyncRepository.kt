package gr.eduinvoice.data.repository

import android.content.Context
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.sync.SyncManager
import gr.eduinvoice.data.sync.SyncResult
import gr.eduinvoice.data.utils.NetworkMonitor
import gr.eduinvoice.data.utils.ExponentialBackoff
import gr.eduinvoice.data.repository.OfflineDataManager
import gr.eduinvoice.data.repository.PendingOperation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that handles data operations with offline support and synchronization
 */
@Singleton
class SyncRepository @Inject constructor(
    private val context: Context,
    private val studentDao: StudentDao,
    private val lessonDao: LessonDao,
    private val groupDao: GroupDao,
    private val offlineDataManager: OfflineDataManager,
    private val syncManager: SyncManager,
    private val networkMonitor: NetworkMonitor
) {
    private val backoff = ExponentialBackoff()

        /**
     * Save student with offline support
     */
    suspend fun saveStudent(student: Student): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Always save to local database first
            val id = studentDao.insert(student)

            // Save to offline storage
            offlineDataManager.saveOfflineData(student, "student", id.toString())

            // If online, try to sync immediately
            if (networkMonitor.isConnected()) {
                val operation = PendingOperation(
                    id = id.toString(),
                    type = "student",
                    operationType = if (student.id == 0L) PendingOperation.OperationType.CREATE else PendingOperation.OperationType.UPDATE,
                    data = com.google.gson.Gson().toJson(student)
                )
                offlineDataManager.queueOperation(operation)

                // Trigger sync
                syncManager.syncData()
            } else {
                // Queue for later sync
                val operation = PendingOperation(
                    id = id.toString(),
                    type = "student",
                    operationType = if (student.id == 0L) PendingOperation.OperationType.CREATE else PendingOperation.OperationType.UPDATE,
                    data = com.google.gson.Gson().toJson(student)
                )
                offlineDataManager.queueOperation(operation)
            }

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

        /**
     * Delete student with offline support
     */
    suspend fun deleteStudent(student: Student): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete from local database
            studentDao.delete(student)

            // Remove from offline storage
            offlineDataManager.clearOfflineData("student")

            // Queue delete operation
            val operation = PendingOperation(
                id = student.id.toString(),
                type = "student",
                operationType = PendingOperation.OperationType.DELETE,
                data = com.google.gson.Gson().toJson(student)
            )
            offlineDataManager.queueOperation(operation)

            // Trigger sync if online
            if (networkMonitor.isConnected()) {
                syncManager.syncData()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

        /**
     * Get students with offline fallback
     */
    suspend fun getStudents(ownerId: Long): Flow<List<Student>> = flow {
        try {
            // First try to get from local database
            studentDao.getAllActiveStudents(ownerId).collect { localStudents ->
                emit(localStudents)
            }

            // If online, try to sync and get updated data
            if (networkMonitor.isConnected()) {
                val syncResult = syncManager.syncData()
                if (syncResult is SyncResult.Success) {
                    studentDao.getAllActiveStudents(ownerId).collect { updatedStudents ->
                        emit(updatedStudents)
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to offline data
            val offlineStudents = offlineDataManager.getAllOfflineData("student")
                .filterIsInstance<Student>()
                .filter { it.ownerId == ownerId && !it.isActive.not() }
            emit(offlineStudents)
        }
    }

        /**
     * Save lesson with offline support
     */
    suspend fun saveLesson(lesson: Lesson): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Always save to local database first
            val id = lessonDao.insert(lesson)

            // Save to offline storage
            offlineDataManager.saveOfflineData(lesson, "lesson", id.toString())

            // Queue for sync
            val operation = PendingOperation(
                id = id.toString(),
                type = "lesson",
                operationType = if (lesson.id == 0L) PendingOperation.OperationType.CREATE else PendingOperation.OperationType.UPDATE,
                data = com.google.gson.Gson().toJson(lesson)
            )
            offlineDataManager.queueOperation(operation)

            // Trigger sync if online
            if (networkMonitor.isConnected()) {
                syncManager.syncData()
            }

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

        /**
     * Delete lesson with offline support
     */
    suspend fun deleteLesson(lesson: Lesson): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete from local database
            lessonDao.delete(lesson)

            // Queue delete operation
            val operation = PendingOperation(
                id = lesson.id.toString(),
                type = "lesson",
                operationType = PendingOperation.OperationType.DELETE,
                data = com.google.gson.Gson().toJson(lesson)
            )
            offlineDataManager.queueOperation(operation)

            // Trigger sync if online
            if (networkMonitor.isConnected()) {
                syncManager.syncData()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

        /**
     * Get lessons with offline fallback
     */
    suspend fun getLessons(studentId: Long): Flow<List<Lesson>> = flow {
        try {
            // First try to get from local database
            lessonDao.getLessonsByStudentId(studentId, 0L).collect { localLessons -> // TODO: Get actual ownerId
                emit(localLessons)
            }

            // If online, try to sync and get updated data
            if (networkMonitor.isConnected()) {
                val syncResult = syncManager.syncData()
                if (syncResult is SyncResult.Success) {
                    lessonDao.getLessonsByStudentId(studentId, 0L).collect { updatedLessons -> // TODO: Get actual ownerId
                        emit(updatedLessons)
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to offline data
            val offlineLessons = offlineDataManager.getAllOfflineData("lesson")
                .filterIsInstance<Lesson>()
                .filter { it.studentId == studentId }
            emit(offlineLessons)
        }
    }

        /**
     * Save group with offline support
     */
    suspend fun saveGroup(group: StudentGroup): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Always save to local database first
            val id = groupDao.insertGroup(group)

            // Save to offline storage
            offlineDataManager.saveOfflineData(group, "group", id.toString())

            // Queue for sync
            val operation = PendingOperation(
                id = id.toString(),
                type = "group",
                operationType = if (group.id == 0L) PendingOperation.OperationType.CREATE else PendingOperation.OperationType.UPDATE,
                data = com.google.gson.Gson().toJson(group)
            )
            offlineDataManager.queueOperation(operation)

            // Trigger sync if online
            if (networkMonitor.isConnected()) {
                syncManager.syncData()
            }

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

        /**
     * Delete group with offline support
     */
    suspend fun deleteGroup(group: StudentGroup): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete from local database
            groupDao.deleteGroup(group)

            // Queue delete operation
            val operation = PendingOperation(
                id = group.id.toString(),
                type = "group",
                operationType = PendingOperation.OperationType.DELETE,
                data = com.google.gson.Gson().toJson(group)
            )
            offlineDataManager.queueOperation(operation)

            // Trigger sync if online
            if (networkMonitor.isConnected()) {
                syncManager.syncData()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

        /**
     * Get groups with offline fallback
     */
    suspend fun getGroups(ownerId: Long): Flow<List<StudentGroup>> = flow {
        try {
            // First try to get from local database
            groupDao.getAllGroups(ownerId).collect { localGroups ->
                emit(localGroups)
            }

            // If online, try to sync and get updated data
            if (networkMonitor.isConnected()) {
                val syncResult = syncManager.syncData()
                if (syncResult is SyncResult.Success) {
                    groupDao.getAllGroups(ownerId).collect { updatedGroups ->
                        emit(updatedGroups)
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to offline data
            val offlineGroups = offlineDataManager.getAllOfflineData("group")
                .filterIsInstance<StudentGroup>()
                .filter { it.ownerId == ownerId }
            emit(offlineGroups)
        }
    }

    /**
     * Force synchronization of all pending operations
     */
    suspend fun forceSync(): SyncResult = withContext(Dispatchers.IO) {
        if (!networkMonitor.isConnected()) {
            return@withContext SyncResult.NoConnection
        }
        
        syncManager.syncData()
    }

    /**
     * Get sync status
     */
    fun getSyncStatus() = syncManager.getSyncStatus()

    /**
     * Observe sync status changes
     */
    fun observeSyncStatus() = syncManager.observeSyncStatus()

    /**
     * Check if there are pending operations
     */
    fun hasPendingOperations(): Boolean = offlineDataManager.hasPendingOperations()

    /**
     * Get pending operations count
     */
    suspend fun getPendingOperationsCount(): Int = withContext(Dispatchers.IO) {
        offlineDataManager.getPendingOperations().size
    }

    /**
     * Clear all offline data
     */
    suspend fun clearOfflineData(): Boolean = withContext(Dispatchers.IO) {
        offlineDataManager.clearOfflineData()
    }

    /**
     * Start background synchronization
     */
    fun startBackgroundSync() {
        syncManager.startBackgroundSync()
    }

    /**
     * Stop background synchronization
     */
    fun stopBackgroundSync() {
        syncManager.stopBackgroundSync()
    }
} 