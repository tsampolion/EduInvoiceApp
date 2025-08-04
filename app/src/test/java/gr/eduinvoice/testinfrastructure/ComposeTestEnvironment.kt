package gr.eduinvoice.testinfrastructure

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import gr.eduinvoice.BouncyCastleTestRunner
import gr.eduinvoice.MainDispatcherRule
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Enhanced Compose test environment with proper Robolectric configuration
 */
@RunWith(BouncyCastleTestRunner::class)
@Config(
    sdk = [34],
    manifest = Config.NONE,
    application = ComposeTestApplication::class
)
abstract class ComposeTestEnvironment {
    
    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @get:Rule
    val enhancedDispatcherRule = EnhancedTestDispatcherRule(UnconfinedTestDispatcher())
    
    /**
     * Get test context for Compose operations
     */
    protected fun getTestContext(): Context = ApplicationProvider.getApplicationContext()
    
    /**
     * Get instrumentation context
     */
    protected fun getInstrumentationContext(): Context = InstrumentationRegistry.getInstrumentation().targetContext
    
    /**
     * Wait for Compose to be idle and stable
     */
    protected fun waitForComposeIdle() {
        composeTestRule.waitForIdle()
    }
    
    /**
     * Set up Compose content with proper context
     */
    protected fun setComposeContent(content: @androidx.compose.runtime.Composable () -> Unit) {
        composeTestRule.setContent {
            content()
        }
        waitForComposeIdle()
    }
}

/**
 * Test application for Compose UI testing
 */
class ComposeTestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any test-specific application setup
    }
}

/**
 * Base class for Compose UI tests with enhanced configuration
 */
abstract class ComposeTestBase : ComposeTestEnvironment() {
    
    /**
     * Initialize test environment
     */
    protected fun initializeTestEnvironment() {
        // Additional test environment setup if needed
    }
    
    /**
     * Clean up test environment
     */
    protected fun cleanupTestEnvironment() {
        // Additional test environment cleanup if needed
    }
}

/**
 * Enhanced test dispatcher rule specifically for Compose testing
 */
class ComposeTestDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : EnhancedTestDispatcherRule(dispatcher) {
    
    override fun starting(description: org.junit.runner.Description) {
        super.starting(description)
        // Additional Compose-specific setup
    }
    
    override fun finished(description: org.junit.runner.Description) {
        super.finished(description)
        // Additional Compose-specific cleanup
    }
}

/**
 * Compose UI test utilities
 */
object ComposeTestUtils {
    
    /**
     * Create a test context for Compose operations
     */
    fun createTestContext(): Context {
        return ApplicationProvider.getApplicationContext()
    }
    
    /**
     * Wait for a specific condition in Compose
     */
    suspend fun waitForCondition(
        timeoutMillis: Long = 5000L,
        condition: () -> Boolean
    ): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (condition()) {
                return true
            }
            kotlinx.coroutines.delay(100)
        }
        return false
    }
    
    /**
     * Validate Compose test environment
     */
    fun validateTestEnvironment(): Boolean {
        return try {
            val context = createTestContext()
            context != null
        } catch (e: Exception) {
            false
        }
    }
} 