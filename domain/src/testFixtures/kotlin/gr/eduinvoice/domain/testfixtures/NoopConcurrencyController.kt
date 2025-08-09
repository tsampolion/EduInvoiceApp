package gr.eduinvoice.domain.testfixtures

import gr.eduinvoice.data.concurrency.ConcurrencyController
import gr.eduinvoice.data.concurrency.OperationType
import gr.eduinvoice.data.concurrency.OperationPriority
import gr.eduinvoice.data.concurrency.TransactionIsolationLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * No-op implementation of ConcurrencyController for testing
 * Provides immediate execution without concurrency overhead
 */
class NoopConcurrencyController : ConcurrencyController {
    
    override suspend fun <T> executeSafeOperation(
        operation: suspend () -> T,
        operationType: OperationType,
        resourceId: String,
        priority: OperationPriority,
        useTransaction: Boolean,
        isolationLevel: TransactionIsolationLevel
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun <T> executeReadOnlyOperation(
        operation: suspend () -> T,
        resourceId: String
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getOperationQueueSize(): Flow<Int> = flowOf(0)

    override fun getActiveOperations(): Flow<List<String>> = flowOf(emptyList())

    override suspend fun waitForIdle() {
        // No-op: always idle
    }

    override suspend fun shutdown() {
        // No-op: nothing to shut down
    }

    companion object {
        fun create(): NoopConcurrencyController = NoopConcurrencyController()
    }
}
