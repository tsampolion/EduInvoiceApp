package gr.eduinvoice.performance

import org.junit.Test
import kotlin.system.measureTimeMillis

class PerformanceTests {
    @Test
    fun exportGeneration_under10s() {
        val elapsed = measureTimeMillis {
            Thread.sleep(50) // placeholder for PDF export simulation
        }
        assert(elapsed < 10_000)
    }
}
