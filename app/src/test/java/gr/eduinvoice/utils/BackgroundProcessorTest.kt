package gr.eduinvoice.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.After

/**
 * Test class for BackgroundProcessor functionality
 */
class BackgroundProcessorTest {
    
    private lateinit var backgroundProcessor: BackgroundProcessor
    
    @Before
    fun setup() {
        backgroundProcessor = BackgroundProcessor()
    }
    
    @After
    fun cleanup() {
        backgroundProcessor.cleanup()
    }
    
    @Test
    fun `test executeTask completes successfully`() = runTest {
        var completed = false
        var errorOccurred = false
        
        val job = backgroundProcessor.executeTask(
            task = {
                delay(100) // Simulate some work
            },
            onComplete = {
                completed = true
            },
            onError = {
                errorOccurred = true
            }
        )
        
        job.join()
        
        assertTrue("Task should complete successfully", completed)
        assertFalse("No error should occur", errorOccurred)
    }
    
    @Test
    fun `test executeTask handles errors`() = runTest {
        var completed = false
        var errorOccurred = false
        var errorMessage = ""
        
        val job = backgroundProcessor.executeTask(
            task = {
                throw RuntimeException("Test error")
            },
            onComplete = {
                completed = true
            },
            onError = { exception ->
                errorOccurred = true
                errorMessage = exception.message ?: ""
            }
        )
        
        job.join()
        
        assertFalse("Task should not complete successfully", completed)
        assertTrue("Error should be handled", errorOccurred)
        assertEquals("Error message should match", "Test error", errorMessage)
    }
    
    @Test
    fun `test executeTaskWithResult returns correct result`() = runTest {
        var result: String? = null
        var completed = false
        
        val job = backgroundProcessor.executeTaskWithResult(
            task = {
                delay(100) // Simulate some work
                "Test Result"
            },
            onComplete = { res ->
                result = res
                completed = true
            }
        )
        
        job.join()
        
        assertTrue("Task should complete successfully", completed)
        assertEquals("Result should match", "Test Result", result)
    }
    
    @Test
    fun `test executeTaskWithProgress updates progress`() = runTest {
        val progressUpdates = mutableListOf<Float>()
        var completed = false
        
        val job = backgroundProcessor.executeTaskWithProgress(
            task = { progressCallback ->
                for (i in 1..5) {
                    delay(50) // Simulate work
                    progressCallback(i / 5f)
                }
            },
            onProgress = { progress ->
                progressUpdates.add(progress)
            },
            onComplete = {
                completed = true
            }
        )
        
        job.join()
        
        assertTrue("Task should complete successfully", completed)
        assertTrue("Should have progress updates", progressUpdates.isNotEmpty())
        assertTrue("Progress should be between 0 and 1", progressUpdates.all { it in 0f..1f })
    }
    
    @Test
    fun `test isCurrentlyProcessing returns correct state`() = runTest {
        assertFalse("Should not be processing initially", backgroundProcessor.isCurrentlyProcessing())
        
        val job = backgroundProcessor.executeTask(
            task = {
                delay(200) // Simulate work
            }
        )
        
        // Check during processing
        assertTrue("Should be processing", backgroundProcessor.isCurrentlyProcessing())
        
        job.join()
        
        // Check after completion
        assertFalse("Should not be processing after completion", backgroundProcessor.isCurrentlyProcessing())
    }
    
    @Test
    fun `test cancelAll stops all tasks`() = runTest {
        var task1Completed = false
        var task2Completed = false
        
        val job1 = backgroundProcessor.executeTask(
            task = {
                delay(500) // Long running task
            },
            onComplete = {
                task1Completed = true
            }
        )
        
        val job2 = backgroundProcessor.executeTask(
            task = {
                delay(500) // Long running task
            },
            onComplete = {
                task2Completed = true
            }
        )
        
        // Cancel all tasks
        backgroundProcessor.cancelAll()
        
        // Wait a bit
        delay(100)
        
        assertFalse("Task1 should not complete after cancellation", task1Completed)
        assertFalse("Task2 should not complete after cancellation", task2Completed)
        assertFalse("Should not be processing after cancellation", backgroundProcessor.isCurrentlyProcessing())
    }
    
    @Test
    fun `test GlobalBackgroundProcessor functionality`() = runTest {
        // Initialize global processor
        GlobalBackgroundProcessor.initialize(backgroundProcessor)
        
        var completed = false
        
        val job = GlobalBackgroundProcessor.executeTask(
            task = {
                delay(100) // Simulate work
            },
            onComplete = {
                completed = true
            }
        )
        
        assertNotNull("Job should not be null", job)
        job?.join()
        
        assertTrue("Task should complete successfully", completed)
        
        // Cleanup
        GlobalBackgroundProcessor.cleanup()
    }
} 