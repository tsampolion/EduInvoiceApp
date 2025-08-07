package gr.eduinvoice.domain.testinfrastructure

import gr.eduinvoice.data.concurrency.ConcurrencyController
import gr.eduinvoice.data.concurrency.ConcurrencyStats
import gr.eduinvoice.data.concurrency.HealthCheckResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

/**
 * Shared test mocks for domain tests
 */
fun createMockConcurrencyController(): ConcurrencyController {
    return mockk(relaxed = true) {
        // Generic suspend methods
        coEvery {
            executeSafeOperation<Any?>(operation = any(), operationType = any(), resourceId = any(), priority = any(), useTransaction = any(), isolationLevel = any())
        } coAnswers {
            val op = firstArg<suspend () -> Any?>()
            kotlin.runCatching { op.invoke() }
        }

        coEvery {
            executeReadOnlyOperation<Any?>(operation = any(), resourceId = any())
        } coAnswers {
            val op = firstArg<suspend () -> Any?>()
            kotlin.runCatching { op.invoke() }
        }

        coEvery {
            executeBatchSafeOperations<Any?>(operations = any(), operationType = any(), resourceId = any(), priority = any(), useTransaction = any())
        } coAnswers {
            val ops = firstArg<List<suspend () -> Any?>>()
            kotlin.runCatching { ops.map { it.invoke() } }
        }

        // Health and metrics
        coEvery { performHealthCheck() } returns HealthCheckResult(isHealthy = true)
        every { getConcurrencyStatistics() } returns ConcurrencyStats()
        every { getActiveResourceLocks() } returns emptySet()
        coEvery { releaseAllResourceLocks() } returns Unit
        coEvery { emergencyCleanup() } returns Unit
    }
}


