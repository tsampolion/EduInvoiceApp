package gr.tsambala.tutorbilling.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gr.tsambala.tutorbilling.R
import gr.tsambala.tutorbilling.data.model.StudentWithEarnings
import gr.tsambala.tutorbilling.utils.getFullName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedStudentCard(
    studentWithEarnings: StudentWithEarnings,
    onStudentClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStudentClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = studentWithEarnings.student.getFullName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.currency_format, studentWithEarnings.student.rate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.week_total),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.currency_format, studentWithEarnings.weekEarnings),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.month_total),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.currency_format, studentWithEarnings.monthEarnings),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            IconButton(onClick = { showDialog = true }) {
                Icon(
                    Icons.Default.Unarchive,
                    contentDescription = stringResource(R.string.restore)
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.restore)) },
            text = { Text(stringResource(R.string.restore_student_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestoreClick()
                        showDialog = false
                    }
                ) { Text(stringResource(R.string.restore)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
