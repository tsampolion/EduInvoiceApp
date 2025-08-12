package gr.eduinvoice.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simplified background processor for heavy operations
 *
 * This implementation avoids complex type inference issues by using simple function types
 * and provides a clean API for executing background tasks with progress tracking.
 */
@Singleton
class BackgroundProcessor @Inject constructor() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    /**
     * Execute a background task with progress tracking
     *
     * @param task The suspend function to execute in the background
     * @param onComplete Optional callback when task completes successfully
     * @param onError Optional callback when task fails
     * @return Job that can be used to cancel the task
     */
    fun executeTask(
        task: suspend () -> Unit,
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ): Job {
        _isProcessing.value = true

        return coroutineScope.launch {
            try {
                task()
                onComplete()
            } catch (e: Exception) {
                onError(e)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Execute a background task that returns a result
     *
     * @param task The suspend function to execute in the background
     * @param onComplete Callback with the result when task completes successfully
     * @param onError Optional callback when task fails
     * @return Job that can be used to cancel the task
     */
    fun <T> executeTaskWithResult(
        task: suspend () -> T,
        onComplete: (T) -> Unit,
        onError: (Throwable) -> Unit = {}
    ): Job {
        _isProcessing.value = true

        return coroutineScope.launch {
            try {
                val result = task()
                onComplete(result)
            } catch (e: Exception) {
                onError(e)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Execute a task with progress updates
     *
     * @param task The suspend function that accepts a progress callback
     * @param onProgress Callback for progress updates (0.0 to 1.0)
     * @param onComplete Optional callback when task completes successfully
     * @param onError Optional callback when task fails
     * @return Job that can be used to cancel the task
     */
    fun executeTaskWithProgress(
        task: suspend ((Float) -> Unit) -> Unit,
        onProgress: (Float) -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ): Job {
        _isProcessing.value = true

        return coroutineScope.launch {
            try {
                task { progress ->
                    onProgress(progress.coerceIn(0f, 1f))
                }
                onComplete()
            } catch (e: Exception) {
                onError(e)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Cancel all background tasks
     */
    fun cancelAll() {
        coroutineScope.cancel()
        _isProcessing.value = false
    }

    /**
     * Check if any background task is currently processing
     */
    fun isCurrentlyProcessing(): Boolean = _isProcessing.value

    /**
     * Clean up resources
     */
    fun cleanup() {
        cancelAll()
    }
}

/**
 * Global background processor instance for easy access
 *
 * This provides a convenient way to access background processing capabilities
 * throughout the application without dependency injection.
 */
object GlobalBackgroundProcessor {
    private var processor: BackgroundProcessor? = null

    /**
     * Initialize the global background processor
     *
     * @param backgroundProcessor The BackgroundProcessor instance to use
     */
    fun initialize(backgroundProcessor: BackgroundProcessor) {
        processor = backgroundProcessor
    }

    /**
     * Execute a background task
     *
     * @param task The suspend function to execute in the background
     * @param onComplete Optional callback when task completes successfully
     * @param onError Optional callback when task fails
     * @return Job that can be used to cancel the task, or null if not initialized
     */
    fun executeTask(
        task: suspend () -> Unit,
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ): Job? {
        return processor?.executeTask(task, onComplete, onError)
    }

    /**
     * Execute a background task that returns a result
     *
     * @param task The suspend function to execute in the background
     * @param onComplete Callback with the result when task completes successfully
     * @param onError Optional callback when task fails
     * @return Job that can be used to cancel the task, or null if not initialized
     */
    fun <T> executeTaskWithResult(
        task: suspend () -> T,
        onComplete: (T) -> Unit,
        onError: (Throwable) -> Unit = {}
    ): Job? {
        return processor?.executeTaskWithResult(task, onComplete, onError)
    }

    /**
     * Execute a task with progress updates
     *
     * @param task The suspend function that accepts a progress callback
     * @param onProgress Callback for progress updates (0.0 to 1.0)
     * @param onComplete Optional callback when task completes successfully
     * @param onError Optional callback when task fails
     * @return Job that can be used to cancel the task, or null if not initialized
     */
    fun executeTaskWithProgress(
        task: suspend ((Float) -> Unit) -> Unit,
        onProgress: (Float) -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ): Job? {
        return processor?.executeTaskWithProgress(task, onProgress, onComplete, onError)
    }

    /**
     * Check if any background task is currently processing
     *
     * @return true if processing, false otherwise
     */
    fun isProcessing(): Boolean = processor?.isCurrentlyProcessing() ?: false

    /**
     * Get the processing state flow
     *
     * @return StateFlow<Boolean> indicating processing state, or null if not initialized
     */
    fun getProcessingState(): StateFlow<Boolean>? = processor?.isProcessing

    /**
     * Cancel all background tasks
     */
    fun cancelAll() {
        processor?.cancelAll()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        processor?.cleanup()
        processor = null
    }
}
