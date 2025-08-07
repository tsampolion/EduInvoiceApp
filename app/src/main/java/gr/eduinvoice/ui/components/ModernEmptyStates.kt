package gr.eduinvoice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ModernEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun ModernEmptyStudentsState(
    onAddStudent: () -> Unit
) {
    ModernEmptyState(
        icon = Icons.Default.People,
        title = "No Students Yet",
        message = "Start by adding your first student to begin tracking lessons and generating invoices.",
        actionText = "Add Student",
        onAction = onAddStudent
    )
}

@Composable
fun ModernEmptyLessonsState(
    onAddLesson: () -> Unit
) {
    ModernEmptyState(
        icon = Icons.Default.Schedule,
        title = "No Lessons Recorded",
        message = "Record your first lesson to start building your teaching history and generating invoices.",
        actionText = "Add Lesson",
        onAction = onAddLesson
    )
}

@Composable
fun ModernEmptyInvoicesState(
    onGenerateInvoice: () -> Unit
) {
    ModernEmptyState(
        icon = Icons.Default.Receipt,
        title = "No Invoices Generated",
        message = "Generate your first invoice to start tracking your earnings and managing payments.",
        actionText = "Generate Invoice",
        onAction = onGenerateInvoice
    )
}

@Composable
fun ModernEmptyGroupsState(
    onCreateGroup: () -> Unit
) {
    ModernEmptyState(
        icon = Icons.Default.Group,
        title = "No Groups Created",
        message = "Create groups to organize your students and manage group lessons more efficiently.",
        actionText = "Create Group",
        onAction = onCreateGroup
    )
}

@Composable
fun ModernEmptySearchState(
    query: String
) {
    ModernEmptyState(
        icon = Icons.Default.Search,
        title = "No Results Found",
        message = "No items match your search for \"$query\". Try adjusting your search terms or filters.",
        actionText = "Clear Search",
        onAction = { /* Clear search functionality */ }
    )
}

@Composable
fun ModernEmptyRevenueState(
    onAddLesson: () -> Unit
) {
    ModernEmptyState(
        icon = Icons.Default.TrendingUp,
        title = "No Revenue Data",
        message = "Start recording lessons to see your revenue trends and earnings analytics.",
        actionText = "Add Lesson",
        onAction = onAddLesson
    )
}

@Composable
fun ModernEmptyBackupState(
    onCreateBackup: () -> Unit
) {
    ModernEmptyState(
        icon = Icons.Default.Backup,
        title = "No Backups Available",
        message = "Create your first backup to ensure your data is safe and can be restored if needed.",
        actionText = "Create Backup",
        onAction = onCreateBackup
    )
}

@Composable
fun ModernEmptySettingsState(
    onConfigureSettings: () -> Unit
) {
    ModernEmptyState(
        icon = Icons.Default.Settings,
        title = "Settings Not Configured",
        message = "Configure your app settings to personalize your experience and optimize your workflow.",
        actionText = "Configure Settings",
        onAction = onConfigureSettings
    )
}

@Composable
fun ModernEmptyErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    ModernEmptyState(
        icon = Icons.Default.Error,
        title = "Something Went Wrong",
        message = errorMessage,
        actionText = "Try Again",
        onAction = onRetry
    )
}

@Composable
fun ModernEmptyOfflineState(
    onRetry: () -> Unit
) {
    ModernEmptyState(
        icon = Icons.Default.WifiOff,
        title = "No Internet Connection",
        message = "Please check your internet connection and try again to access this feature.",
        actionText = "Retry",
        onAction = onRetry
    )
}

@Composable
fun ModernEmptyPermissionState(
    permission: String,
    onGrantPermission: () -> Unit
) {
    ModernEmptyState(
        icon = Icons.Default.Security,
        title = "Permission Required",
        message = "This feature requires $permission permission to function properly.",
        actionText = "Grant Permission",
        onAction = onGrantPermission
    )
}
