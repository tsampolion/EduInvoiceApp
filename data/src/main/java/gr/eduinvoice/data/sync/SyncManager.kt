package gr.eduinvoice.data.sync

import android.content.Context
import gr.eduinvoice.data.repository.OfflineDataManager
import gr.eduinvoice.data.repository.PendingOperation
import gr.eduinvoice.data.utils.NetworkMonitor
import gr.eduinvoice.data.utils.ExponentialBackoff
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages data synchronization between local and remote data sources
 */
@Singleton
class SyncManager @Inject constructor(
    private val context: Context,
    private val offlineDataManager: OfflineDataManager,
    private val networkMonitor: NetworkMonitor,
    private val conflictResolver: ConflictResolver
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val backoff = ExponentialBackoff()
    private val isSyncing = AtomicBoolean(false)
    private val syncStatusFlow = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    
    private var backgroundSyncJob: Job? = null
    private var connectivityJob: Job? = null

    init {
        startConnectivityMonitoring()
    }

    /**
     * Perform manual data synchronization
     */
    suspend fun syncData(): SyncResult = withContext(Dispatchers.IO) {
        if (isSyncing.get()) {
            return@withContext SyncResult.AlreadyInProgress
        }

        if (!networkMonitor.isConnected()) {
            return@withContext SyncResult.NoConnection
        }

        isSyncing.set(true)
        syncStatusFlow.value = SyncStatus.Syncing

        try {
            val pendingOperations = offlineDataManager.getPendingOperations()
            if (pendingOperations.isEmpty()) {
                return@withContext SyncResult.NoDataToSync
            }

            var successCount = 0
            var failureCount = 0
            val conflicts = mutableListOf<SyncConflict>()

            for (operation in pendingOperations) {
                val result = processOperation(operation)
                when (result) {
                    is SyncOperationResult.Success -> {
                        offlineDataManager.removePendingOperation(operation)
                        successCount++
                    }
                    is SyncOperationResult.Failure -> {
                        failureCount++
                        if (result.shouldRetry && operation.retryCount < 3) {
                            // Re-queue with increased retry count
                            val updatedOperation = operation.copy(retryCount = operation.retryCount + 1)
                            offlineDataManager.queueOperation(updatedOperation)
                        }
                    }
                    is SyncOperationResult.Conflict -> {
                        conflicts.add(result.conflict)
                    }
                }
            }

            val finalResult = when {
                failureCount == 0 && conflicts.isEmpty() -> SyncResult.Success(successCount)
                conflicts.isNotEmpty() -> SyncResult.Conflicts(conflicts)
                else -> SyncResult.PartialSuccess(successCount, failureCount)
            }

            syncStatusFlow.value = SyncStatus.Completed(finalResult)
            finalResult

        } catch (e: Exception) {
            syncStatusFlow.value = SyncStatus.Failed(e)
            SyncResult.Failure(e)
        } finally {
            isSyncing.set(false)
        }
    }

    /**
     * Start background synchronization
     */
    fun startBackgroundSync() {
        if (backgroundSyncJob?.isActive == true) return

        backgroundSyncJob = scope.launch {
            while (isActive) {
                try {
                    if (networkMonitor.isConnected() && offlineDataManager.hasPendingOperations()) {
                        syncData()
                    }
                    delay(BACKGROUND_SYNC_INTERVAL)
                } catch (e: Exception) {
                    // Log error but continue background sync
                    delay(BACKGROUND_SYNC_INTERVAL * 2)
                }
            }
        }
    }

    /**
     * Stop background synchronization
     */
    fun stopBackgroundSync() {
        backgroundSyncJob?.cancel()
        backgroundSyncJob = null
    }

    /**
     * Get current sync status
     */
    fun getSyncStatus(): SyncStatus = syncStatusFlow.value

    /**
     * Observe sync status changes
     */
    fun observeSyncStatus(): StateFlow<SyncStatus> = syncStatusFlow.asStateFlow()

    /**
     * Process a single pending operation
     */
    private suspend fun processOperation(operation: PendingOperation): SyncOperationResult {
        val result = backoff.executeWithRetry(
            operation = {
                when (operation.operationType) {
                    PendingOperation.OperationType.CREATE -> createRemoteData(operation)
                    PendingOperation.OperationType.UPDATE -> updateRemoteData(operation)
                    PendingOperation.OperationType.DELETE -> deleteRemoteData(operation)
                }
            },
            shouldRetry = { error, attempt ->
                backoff.shouldRetry(attempt, error)
            },
            onRetry = { error, attempt ->
                // Log retry attempt
            }
        )
        
        return result.getOrThrow()
    }

    /**
     * Create data on remote server
     */
    private suspend fun createRemoteData(operation: PendingOperation): SyncOperationResult {
        // Simulate remote API call
        return try {
            // In real implementation, this would make an API call
            // For now, simulate success
            SyncOperationResult.Success
        } catch (e: Exception) {
            SyncOperationResult.Failure(e, shouldRetry = true)
        }
    }

    /**
     * Update data on remote server
     */
    private suspend fun updateRemoteData(operation: PendingOperation): SyncOperationResult {
        return try {
            // Simulate remote API call with potential conflict
            val hasConflict = operation.retryCount > 0 // Simulate conflict on retry
            
            if (hasConflict) {
                val conflict = SyncConflict(
                    localData = operation.data,
                    remoteData = "remote_data_json",
                    operation = operation
                )
                SyncOperationResult.Conflict(conflict)
            } else {
                SyncOperationResult.Success
            }
        } catch (e: Exception) {
            SyncOperationResult.Failure(e, shouldRetry = true)
        }
    }

    /**
     * Delete data on remote server
     */
    private suspend fun deleteRemoteData(operation: PendingOperation): SyncOperationResult {
        return try {
            // Simulate remote API call
            SyncOperationResult.Success
        } catch (e: Exception) {
            SyncOperationResult.Failure(e, shouldRetry = true)
        }
    }

    /**
     * Start monitoring network connectivity for automatic sync
     */
    private fun startConnectivityMonitoring() {
        connectivityJob = scope.launch {
            networkMonitor.observeConnectivity()
                .distinctUntilChanged()
                .collect { isConnected ->
                    if (isConnected && offlineDataManager.hasPendingOperations()) {
                        // Trigger sync when connection is restored
                        delay(5000) // Wait 5 seconds before syncing
                        syncData()
                    }
                }
        }
    }

    /**
     * Resolve sync conflicts
     */
    suspend fun resolveConflicts(conflicts: List<SyncConflict>): SyncResult {
        return try {
            var resolvedCount = 0
            
            for (conflict in conflicts) {
                val resolution = conflictResolver.resolveConflict(
                    conflict.localData,
                    conflict.remoteData
                )
                
                when (resolution) {
                    is ConflictResolution.UseLocal -> {
                        // Re-queue with local data
                        val operation = conflict.operation.copy(data = conflict.localData)
                        offlineDataManager.queueOperation(operation)
                        resolvedCount++
                    }
                    is ConflictResolution.UseRemote -> {
                        // Update local data with remote data
                        offlineDataManager.saveOfflineData(
                            resolution.data,
                            conflict.operation.type,
                            conflict.operation.id
                        )
                        resolvedCount++
                    }
                    is ConflictResolution.Merge -> {
                        // Save merged data
                        offlineDataManager.saveOfflineData(
                            resolution.mergedData,
                            conflict.operation.type,
                            conflict.operation.id
                        )
                        resolvedCount++
                    }
                }
            }
            
            SyncResult.Success(resolvedCount)
        } catch (e: Exception) {
            SyncResult.Failure(e)
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopBackgroundSync()
        connectivityJob?.cancel()
        scope.cancel()
    }

    companion object {
        private const val BACKGROUND_SYNC_INTERVAL = 300000L // 5 minutes
    }
}

/**
 * Represents the result of a sync operation
 */
sealed class SyncResult {
    object AlreadyInProgress : SyncResult()
    object NoConnection : SyncResult()
    object NoDataToSync : SyncResult()
    data class Success(val count: Int) : SyncResult()
    data class PartialSuccess(val successCount: Int, val failureCount: Int) : SyncResult()
    data class Conflicts(val conflicts: List<SyncConflict>) : SyncResult()
    data class Failure(val error: Exception) : SyncResult()
}

/**
 * Represents the status of synchronization
 */
sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Completed(val result: SyncResult) : SyncStatus()
    data class Failed(val error: Exception) : SyncStatus()
}

/**
 * Represents the result of processing a single operation
 */
sealed class SyncOperationResult {
    object Success : SyncOperationResult()
    data class Failure(val error: Exception, val shouldRetry: Boolean = false) : SyncOperationResult()
    data class Conflict(val conflict: SyncConflict) : SyncOperationResult()
}

/**
 * Represents a sync conflict between local and remote data
 */
data class SyncConflict(
    val localData: String,
    val remoteData: String,
    val operation: PendingOperation
) 