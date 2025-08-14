package gr.eduinvoice.stress

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.system.measureTimeMillis

class StressTest {
    @Test
    fun concurrentOperations_underMemoryBudget() = runBlocking {
        val n = 200
        val jobs = (1..n).map {
            async(Dispatchers.Default) {
                val data = ByteArray(64 * 1024) // 64KB
                data.fill(1)
                data.sum()
            }
        }
        val time = measureTimeMillis { jobs.awaitAll() }
        assert(time < 6_000)
    }
}
