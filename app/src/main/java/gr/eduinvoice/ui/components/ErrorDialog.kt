package gr.eduinvoice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import gr.eduinvoice.R
import gr.eduinvoice.utils.ErrorHandler
import gr.eduinvoice.utils.ErrorType
import gr.eduinvoice.utils.RecoveryAction
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException

/**
 * User-friendly error dialog with recovery options
 */
@Composable
fun ErrorDialog(
    error: Throwable,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onReport: (() -> Unit)? = null,
    showDetails: Boolean = false
) {
    val context = LocalContext.current
    val errorHandler = remember { ErrorHandler(context) }
    val errorResult = remember(error) { errorHandler.handleError(error) }
    
    var expanded by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Error icon
                Icon(
                    imageVector = getErrorIcon(errorResult.errorType),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = getErrorColor(errorResult.errorType)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Error title
                Text(
                    text = getErrorTitle(errorResult.errorType),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Error message
                Text(
                    text = errorResult.userMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Recovery suggestion
                if (errorResult.recoveryAction != RecoveryAction.RETRY) {
                    Text(
                        text = getRecoverySuggestion(errorResult.recoveryAction),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Show details toggle
                if (showDetails) {
                    TextButton(
                        onClick = { expanded = !expanded }
                    ) {
                        Text(
                            text = if (expanded) stringResource(R.string.hide_details) else stringResource(R.string.show_details)
                        )
                    }
                    
                    if (expanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error.message ?: "Unknown error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dismiss button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.dismiss))
                    }
                    
                    // Report button (optional)
                    if (onReport != null) {
                        OutlinedButton(
                            onClick = onReport,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.report))
                        }
                    }
                    
                    // Retry button
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f),
                        enabled = errorResult.shouldRetry
                    ) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

/**
 * Gets appropriate icon for error type
 */
@Composable
private fun getErrorIcon(errorType: ErrorType) = when (errorType) {
    ErrorType.NETWORK_TIMEOUT,
    ErrorType.NETWORK_NO_CONNECTION,
    ErrorType.NETWORK_ERROR -> Icons.Default.Warning
    ErrorType.PERMISSION_ERROR -> Icons.Default.Error
    ErrorType.MEMORY_ERROR -> Icons.Default.Error
    else -> Icons.Default.Info
}

/**
 * Gets appropriate color for error type
 */
@Composable
private fun getErrorColor(errorType: ErrorType) = when (errorType) {
    ErrorType.NETWORK_TIMEOUT,
    ErrorType.NETWORK_NO_CONNECTION,
    ErrorType.NETWORK_ERROR -> MaterialTheme.colorScheme.tertiary
    ErrorType.PERMISSION_ERROR,
    ErrorType.MEMORY_ERROR -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.primary
}

/**
 * Gets error title based on error type
 */
@Composable
private fun getErrorTitle(errorType: ErrorType) = when (errorType) {
    ErrorType.NETWORK_TIMEOUT -> stringResource(R.string.error_network_timeout)
    ErrorType.NETWORK_NO_CONNECTION -> stringResource(R.string.error_no_connection)
    ErrorType.NETWORK_ERROR -> stringResource(R.string.error_network_error)
    ErrorType.IO_ERROR -> stringResource(R.string.error_io_error)
    ErrorType.PERMISSION_ERROR -> stringResource(R.string.error_permission_denied)
    ErrorType.VALIDATION_ERROR -> stringResource(R.string.error_validation_failed)
    ErrorType.STATE_ERROR -> stringResource(R.string.error_invalid_state)
    ErrorType.MEMORY_ERROR -> stringResource(R.string.error_memory_error)
    ErrorType.UNKNOWN_ERROR -> stringResource(R.string.error_unknown)
}

/**
 * Gets recovery suggestion based on recovery action
 */
@Composable
private fun getRecoverySuggestion(recoveryAction: RecoveryAction) = when (recoveryAction) {
    RecoveryAction.CHECK_NETWORK -> stringResource(R.string.suggestion_check_network)
    RecoveryAction.REQUEST_PERMISSIONS -> stringResource(R.string.suggestion_check_permissions)
    RecoveryAction.VALIDATE_INPUT -> stringResource(R.string.suggestion_check_input)
    RecoveryAction.RESTART_APP -> stringResource(R.string.suggestion_restart_app)
    RecoveryAction.CLEAR_MEMORY -> stringResource(R.string.suggestion_clear_memory)
    else -> ""
}

/**
 * Simple error dialog for quick error display
 */
@Composable
fun SimpleErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(stringResource(R.string.error_occurred))
        },
        text = {
            Text(message)
        },
        confirmButton = {
            if (onRetry != null) {
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            } else {
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.ok))
                }
            }
        },
        dismissButton = {
            if (onRetry != null) {
                OutlinedButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dismiss))
                }
            }
        }
    )
}

/**
 * Network-specific error dialog
 */
@Composable
fun NetworkErrorDialog(
    error: Throwable,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onCheckSettings: () -> Unit
) {
    val isConnectionError = error is UnknownHostException || 
                           (error is IOException && error.message?.contains("connection") == true)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        },
        title = {
            Text(
                text = if (isConnectionError) {
                    stringResource(R.string.error_no_connection)
                } else {
                    stringResource(R.string.error_network_timeout)
                }
            )
        },
        text = {
            Text(
                text = if (isConnectionError) {
                    stringResource(R.string.error_no_connection_message)
                } else {
                    stringResource(R.string.error_network_timeout_message)
                }
            )
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCheckSettings) {
                Text(stringResource(R.string.check_settings))
            }
        }
    )
} 