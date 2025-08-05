package gr.eduinvoice.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.ui.components.ErrorBoundary
import gr.eduinvoice.utils.ErrorHandler
import gr.eduinvoice.analytics.ErrorReporter
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorBoundaryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var errorHandler: ErrorHandler
    private lateinit var errorReporter: ErrorReporter

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        errorHandler = ErrorHandler(context)
        errorReporter = ErrorReporter(context)
    }

    @Test
    fun errorBoundary_CatchesAndDisplaysError() {
        var errorCaught = false
        
        composeTestRule.setContent {
            ErrorBoundary(
                onError = { error ->
                    errorCaught = true
                }
            ) {
                ErrorThrowingComposable()
            }
        }

        // Wait for error to be caught and displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            errorCaught
        }

        // Verify error UI is displayed
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please try again later").assertIsDisplayed()
    }

    @Test
    fun errorBoundary_RetryButtonWorks() = runTest {
        var retryCount = 0
        
        composeTestRule.setContent {
            ErrorBoundary(
                onError = { error ->
                    // Error handler
                }
            ) {
                if (retryCount == 0) {
                    ErrorThrowingComposable()
                } else {
                    // Success case after retry
                    SuccessComposable()
                }
            }
        }

        // Wait for error to be displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Something went wrong").fetchSemanticsNodes().isNotEmpty()
        }

        // Click retry button
        composeTestRule.onNodeWithText("Retry").performClick()
        retryCount++

        // Verify success content is displayed after retry
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Success!").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun errorBoundary_ReportsErrorToAnalytics() = runTest {
        var errorReported = false
        
        composeTestRule.setContent {
            ErrorBoundary(
                onError = { error ->
                    errorReporter.reportError(error, "ErrorBoundaryTest")
                    errorReported = true
                }
            ) {
                ErrorThrowingComposable()
            }
        }

        // Wait for error to be reported
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            errorReported
        }

        // Verify error was reported
        assert(errorReported)
    }

    @Test
    fun errorBoundary_HandlesMultipleErrors() = runTest {
        var errorCount = 0
        
        composeTestRule.setContent {
            ErrorBoundary(
                onError = { error ->
                    errorCount++
                }
            ) {
                MultipleErrorComposable()
            }
        }

        // Wait for errors to be caught
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            errorCount > 0
        }

        // Verify error boundary handled the errors gracefully
        assert(errorCount > 0)
    }

    @Composable
    private fun ErrorThrowingComposable() {
        remember {
            throw RuntimeException("Test error for ErrorBoundary")
        }
    }

    @Composable
    private fun SuccessComposable() {
        // This composable should render successfully
    }

    @Composable
    private fun MultipleErrorComposable() {
        remember {
            // Simulate multiple errors
            throw RuntimeException("Multiple error test")
        }
    }
} 