package gr.eduinvoice.testinfrastructure

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import gr.eduinvoice.BouncyCastleTestRunner
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

/**
 * Base class for all Android instrumented tests with standardized configuration
 * Provides common setup, teardown, and utilities for UI automation and device testing
 */
@RunWith(AndroidJUnit4::class)
abstract class AndroidBaseTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val databaseContainer = TestDatabaseContainer()

    protected val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    protected val uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    protected lateinit var database: EduInvoiceDatabase
    protected lateinit var androidTestEnvironment: AndroidTestInfrastructure.AndroidTestEnvironment

    @Before
    open fun setUp() {
        database = databaseContainer.createTestDatabase()
        androidTestEnvironment = AndroidTestInfrastructure.createAndroidTestEnvironment(database)
    }

    @After
    open fun tearDown() {
        databaseContainer.cleanupTestDatabase()
    }

    // Common test utilities
    protected fun createTestUser() = AndroidTestInfrastructure.AndroidTestDataFactory.createTestUser()
    protected fun createTestStudent() = AndroidTestInfrastructure.AndroidTestDataFactory.createTestStudent()
    protected fun createTestLesson() = AndroidTestInfrastructure.AndroidTestDataFactory.createTestLesson()
    protected fun createTestGroup() = AndroidTestInfrastructure.AndroidTestDataFactory.createTestGroup()

    // UI testing utilities
    protected fun waitForElement(composeTestRule: ComposeContentTestRule, timeoutMillis: Long = 5000L) =
        AndroidTestInfrastructure.UiTestUtils.waitForElement(composeTestRule, timeoutMillis)

    protected fun waitForIdle(composeTestRule: ComposeContentTestRule) =
        AndroidTestInfrastructure.UiTestUtils.waitForIdle(composeTestRule)

    protected fun takeScreenshot(name: String) =
        AndroidTestInfrastructure.UiTestUtils.takeScreenshot(name)

    protected fun scrollToElement(composeTestRule: ComposeContentTestRule, element: String) =
        AndroidTestInfrastructure.UiTestUtils.scrollToElement(composeTestRule, element)

    protected fun performClick(composeTestRule: ComposeContentTestRule, element: String) =
        AndroidTestInfrastructure.UiTestUtils.performClick(composeTestRule, element)

    protected fun performTextInput(composeTestRule: ComposeContentTestRule, element: String, text: String) =
        AndroidTestInfrastructure.UiTestUtils.performTextInput(composeTestRule, element, text)

    protected fun assertElementExists(composeTestRule: ComposeContentTestRule, element: String) =
        AndroidTestInfrastructure.UiTestUtils.assertElementExists(composeTestRule, element)

    protected fun assertElementDoesNotExist(composeTestRule: ComposeContentTestRule, element: String) =
        AndroidTestInfrastructure.UiTestUtils.assertElementDoesNotExist(composeTestRule, element)

    // Performance utilities
    protected fun measureUiResponseTime(operation: () -> Unit): Long =
        AndroidTestInfrastructure.UiPerformanceUtils.measureUiResponseTime(operation)

    protected fun measureScreenLoadTime(composeTestRule: ComposeContentTestRule, operation: () -> Unit): Long =
        AndroidTestInfrastructure.UiPerformanceUtils.measureScreenLoadTime(composeTestRule, operation)

    protected fun measureScrollPerformance(composeTestRule: ComposeContentTestRule, element: String): Long =
        AndroidTestInfrastructure.UiPerformanceUtils.measureScrollPerformance(composeTestRule, element)

    protected fun getMemoryUsage(): Long =
        AndroidTestInfrastructure.UiPerformanceUtils.getMemoryUsage()

    protected fun measureMemoryUsage(operation: () -> Unit): Long =
        AndroidTestInfrastructure.UiPerformanceUtils.measureMemoryUsage(operation)

    // Accessibility utilities
    protected fun assertAccessibilityLabel(composeTestRule: ComposeContentTestRule, element: String, label: String) =
        AndroidTestInfrastructure.AccessibilityTestUtils.assertAccessibilityLabel(composeTestRule, element, label)

    protected fun assertClickableElement(composeTestRule: ComposeContentTestRule, element: String) =
        AndroidTestInfrastructure.AccessibilityTestUtils.assertClickableElement(composeTestRule, element)

    protected fun assertNonClickableElement(composeTestRule: ComposeContentTestRule, element: String) =
        AndroidTestInfrastructure.AccessibilityTestUtils.assertNonClickableElement(composeTestRule, element)

    protected fun assertTouchTargetSize(composeTestRule: ComposeContentTestRule, element: String) =
        AndroidTestInfrastructure.AccessibilityTestUtils.assertTouchTargetSize(composeTestRule, element)

    protected fun assertFocusOrder(composeTestRule: ComposeContentTestRule, elements: List<String>) =
        AndroidTestInfrastructure.AccessibilityTestUtils.assertFocusOrder(composeTestRule, elements)
}
