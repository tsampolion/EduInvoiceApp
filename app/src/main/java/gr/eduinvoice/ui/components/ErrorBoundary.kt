package gr.eduinvoice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gr.eduinvoice.R
import gr.eduinvoice.analytics.ErrorReporter
import gr.eduinvoice.utils.ErrorHandler
import kotlinx.coroutines.launch
import dagger.hilt.android.EntryPointAccessors

/**
 * ErrorBoundary composable that catches and handles errors in the UI tree.
 * Provides user-friendly error display and recovery options.
 */
@Composable
fun ErrorBoundary(
    onError: (Throwable) -> Unit = {},
    showErrorDialog: Boolean = true,
    content: @Composable () -> Unit
) {
    var error by remember { mutableStateOf<Throwable?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Error handler and reporter instances
    val errorHandler = remember { ErrorHandler(context) }
    val errorReporter = remember { ErrorReporter(context) }

    DisposableEffect(Unit) {
        onDispose {
            // Cleanup if needed
        }
    }

    if (error != null) {
        // Report error to analytics
        LaunchedEffect(error) {
            error?.let {
                errorReporter.reportError(it, "ErrorBoundary")
                onError(it)
            }
        }

        if (showErrorDialog) {
            showDialog = true
        }

        // Show error UI
        ErrorFallbackUI(
            error = error!!,
            onRetry = {
                error = null
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            }
        )
    } else {
        // Wrap content in error catching
        CompositionLocalProvider(
            LocalErrorHandler provides errorHandler
        ) {
            content()
        }
    }

    // Error dialog
    if (showDialog && error != null) {
        ErrorDialog(
            error = error!!,
            onRetry = {
                error = null
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            }
        )
    }
}

/**
 * Fallback UI shown when an error occurs in the ErrorBoundary
 */
@Composable
private fun ErrorFallbackUI(
    error: Throwable,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.error_something_went_wrong),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.error_try_again_later),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.dismiss))
            }

            Button(
                onClick = onRetry
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

/**
 * CompositionLocal for providing error handler to child composables
 */
val LocalErrorHandler = compositionLocalOf<ErrorHandler> {
    error("No ErrorHandler provided")
}

/**
 * Extension function to get error handler from composition local
 */
@Composable
fun getErrorHandler(): ErrorHandler {
    return LocalErrorHandler.current
}
