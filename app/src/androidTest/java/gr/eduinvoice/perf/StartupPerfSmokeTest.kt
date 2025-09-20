package gr.eduinvoice.perf

import android.os.SystemClock
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import gr.eduinvoice.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class StartupPerfSmokeTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun coldStart_underBudget() {
        val args = InstrumentationRegistry.getArguments()
        val budgetMs = args.getString("STARTUP_BUDGET_MS")?.toLongOrNull() ?: 5000L

        val start = SystemClock.uptimeMillis()

        // Ensure activity is launched and moved to Resumed
        activityRule.scenario.onActivity { /* no-op */ }

        // Wait for UI to become idle (best-effort on emulator)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        SystemClock.sleep(100)

        val duration = SystemClock.uptimeMillis() - start
        assertTrue("Cold start ${duration}ms exceeded budget ${budgetMs}ms", duration < budgetMs)
    }

    @Test
    fun initialMemory_underBudget() {
        val args = InstrumentationRegistry.getArguments()
        val budgetMb = args.getString("INITIAL_MEMORY_BUDGET_MB")?.toLongOrNull() ?: 150L

        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        assertTrue("Initial memory ${used}MB exceeded budget ${budgetMb}MB", used < budgetMb)
    }
}

