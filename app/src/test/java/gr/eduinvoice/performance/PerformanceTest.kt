package gr.eduinvoice.performance

import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.random.Random

class PerformanceTest {
    @Test
    fun searchResponse_under500ms() {
        val dataset = (1..10_000).map { "Item ${Random.nextInt()}" }
        val key = dataset[5000]
        val query = key.substring(0, 6)
        val elapsed = measureTimeMillis {
            dataset.filter { it.contains(query) }
        }
        assert(elapsed < 500)
    }
}
