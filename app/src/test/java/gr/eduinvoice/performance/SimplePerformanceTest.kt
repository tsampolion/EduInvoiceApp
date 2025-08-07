package gr.eduinvoice.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.utils.BackgroundProcessor
import gr.eduinvoice.utils.DataCache
import gr.eduinvoice.utils.GlobalCache
import gr.eduinvoice.utils.GlobalBackgroundProcessor
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Simplified performance validation tests
 *
 * These tests validate the core performance optimizations implemented in Task 1.5:
 * - Background processing
 * - Data caching
 * - Memory efficiency
 */
@RunWith(AndroidJUnit4::class)
class SimplePerformanceTest {

    private lateinit var backgroundProcessor: BackgroundProcessor

    @Before
    fun setup() = runTest {
        // Clear any existing cache
        GlobalCache.clearCache()
        
        // Initialize background processor
        backgroundProcessor = BackgroundProcessor()
        GlobalBackgroundProcessor.initialize(backgroundProcessor)
    }

    @After
    fun cleanup() = runTest {
        GlobalCache.clearCache()
        GlobalBackgroundProcessor.cleanup()
    }

    @Test
    fun `test background processor performance`() = runTest {
        val startTime = System.currentTimeMillis()
        var isCompleted = false
        
        // Execute a background task
        val job = GlobalBackgroundProcessor.executeTask(
            task = {
                // Simulate some work
                kotlinx.coroutines.delay(100)
            },
            onComplete = {
                isCompleted = true
            },
            onError = { /* Handle error */ }
        )
        
        // Wait for completion
        job?.join()
        
        val executionTime = System.currentTimeMillis() - startTime
        
        // Verify results
        assertTrue("Should complete successfully", isCompleted)
        assertTrue("Should complete in reasonable time", executionTime < 500)
    }

    @Test
    fun `test background processor with result`() = runTest {
        var result: String? = null
        var isCompleted = false
        
        // Execute a background task that returns a result
        val job = GlobalBackgroundProcessor.executeTaskWithResult(
            task = {
                // Simulate some work
                kotlinx.coroutines.delay(50)
                "Test Result"
            },
            onComplete = { res ->
                result = res
                isCompleted = true
            },
            onError = { /* Handle error */ }
        )
        
        // Wait for completion
        job?.join()
        
        // Verify results
        assertTrue("Should complete successfully", isCompleted)
        assertEquals("Test Result", result)
    }

    @Test
    fun `test background processor with progress`() = runTest {
        val progressUpdates = mutableListOf<Float>()
        var isCompleted = false
        
        // Execute a background task with progress updates
        val job = GlobalBackgroundProcessor.executeTaskWithProgress(
            task = { onProgress ->
                // Simulate work with progress updates
                repeat(5) { step ->
                    kotlinx.coroutines.delay(20)
                    onProgress((step + 1) / 5f)
                }
            },
            onProgress = { progress ->
                progressUpdates.add(progress)
            },
            onComplete = {
                isCompleted = true
            },
            onError = { /* Handle error */ }
        )
        
        // Wait for completion
        job?.join()
        
        // Verify results
        assertTrue("Should complete successfully", isCompleted)
        assertTrue("Should have progress updates", progressUpdates.isNotEmpty())
        assertTrue("Final progress should be 1.0", progressUpdates.last() >= 1.0f)
    }

    @Test
    fun `test caching performance`() = runTest {
        val testKey = "test_key"
        val testData = "test_data"
        
        // First cache operation
        val firstCacheStart = System.currentTimeMillis()
        GlobalCache.cacheData(testKey, testData, kotlin.time.Duration.parse("PT1M")) // 1 minute TTL
        val firstCacheTime = System.currentTimeMillis() - firstCacheStart
        
        // Retrieve from cache
        val retrieveStart = System.currentTimeMillis()
        val retrievedData = GlobalCache.getCachedData(testKey) as String?
        val retrieveTime = System.currentTimeMillis() - retrieveStart
        
        // Verify results
        assertEquals(testData, retrievedData)
        assertTrue("Cache operations should be fast", firstCacheTime < 100)
        assertTrue("Cache retrieval should be fast", retrieveTime < 100)
    }

    @Test
    fun `test caching with TTL`() = runTest {
        val testKey = "ttl_test_key"
        val testData = "ttl_test_data"
        
        // Cache data with short TTL
        GlobalCache.cacheData(testKey, testData, kotlin.time.Duration.parse("PT0.1S")) // 100ms TTL
        
        // Retrieve immediately
        val immediateData = GlobalCache.getCachedData(testKey) as String?
        assertEquals(testData, immediateData)
        
        // Wait for TTL to expire
        kotlinx.coroutines.delay(150)
        
        // Try to retrieve after TTL expiration
        val expiredData = GlobalCache.getCachedData(testKey)
        assertEquals(null, expiredData)
    }

    @Test
    fun `test memory efficiency`() = runTest {
        // Get initial memory usage
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        // Perform multiple cache operations
        repeat(100) { index ->
            GlobalCache.cacheData("key_$index", "data_$index", kotlin.time.Duration.parse("PT1M"))
        }
        
        // Get final memory usage
        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = finalMemory - initialMemory
        
        // Memory increase should be reasonable (less than 5MB for 100 cache entries)
        val maxExpectedMemoryIncrease = 5 * 1024 * 1024L // 5MB
        assertTrue("Memory increase should be under 5MB", memoryIncrease < maxExpectedMemoryIncrease)
    }

    @Test
    fun `test concurrent background processing`() = runTest {
        val jobs = mutableListOf<kotlinx.coroutines.Job?>()
        val completedTasks = mutableListOf<Int>()
        
        // Start multiple background tasks concurrently
        repeat(5) { taskId ->
            val job = GlobalBackgroundProcessor.executeTask(
                task = {
                    kotlinx.coroutines.delay(50)
                },
                onComplete = {
                    completedTasks.add(taskId)
                },
                onError = { /* Handle error */ }
            )
            jobs.add(job)
        }
        
        // Wait for all to complete
        jobs.forEach { it?.join() }
        
        // Verify results
        assertEquals(5, completedTasks.size)
        assertTrue("All tasks should complete", completedTasks.containsAll(listOf(0, 1, 2, 3, 4)))
    }

    @Test
    fun `test background processor cancellation`() = runTest {
        var isCancelled = false
        var isCompleted = false
        
        // Start a long-running task
        val job = GlobalBackgroundProcessor.executeTask(
            task = {
                kotlinx.coroutines.delay(1000) // 1 second delay
            },
            onComplete = {
                isCompleted = true
            },
            onError = { /* Handle error */ }
        )
        
        // Cancel after a short delay
        kotlinx.coroutines.delay(100)
        job?.cancel()
        
        // Wait a bit more
        kotlinx.coroutines.delay(200)
        
        // Verify cancellation
        assertFalse("Should not complete when cancelled", isCompleted)
        assertTrue("Job should be cancelled", job?.isCancelled == true)
    }

    @Test
    fun `test cache statistics`() = runTest {
        // Clear cache to start fresh
        GlobalCache.clearCache()
        
        // Perform some cache operations
        GlobalCache.cacheData("key1", "data1", kotlin.time.Duration.parse("PT1M"))
        GlobalCache.cacheData("key2", "data2", kotlin.time.Duration.parse("PT1M"))
        
        // Retrieve data (should be cache hits)
        GlobalCache.getCachedData("key1")
        GlobalCache.getCachedData("key2")
        
        // Try to retrieve non-existent key (should be cache miss)
        GlobalCache.getCachedData("nonexistent")
        
        // Get cache statistics
        val stats = GlobalCache.getCacheStats()
        
        // Verify statistics
        assertTrue("Should have cache entries", stats.totalEntries > 0)
        assertTrue("Should have valid entries", stats.validEntries > 0)
    }

    @Test
    fun `test background processor state management`() = runTest {
        // Check initial state
        assertFalse("Should not be processing initially", 
                   backgroundProcessor.isCurrentlyProcessing())
        
        var isProcessing = false
        
        // Start a task
        val job = GlobalBackgroundProcessor.executeTask(
            task = {
                kotlinx.coroutines.delay(100)
            },
            onComplete = { /* Complete */ },
            onError = { /* Handle error */ }
        )
        
        // Check processing state
        assertTrue("Should be processing", backgroundProcessor.isCurrentlyProcessing())
        
        // Wait for completion
        job?.join()
        
        // Check final state
        assertFalse("Should not be processing after completion", 
                   backgroundProcessor.isCurrentlyProcessing())
    }
} 