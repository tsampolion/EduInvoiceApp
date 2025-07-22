package gr.tutorbilling.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gr.tutorbilling.ui.design.Dimensions
import androidx.compose.ui.res.stringResource
import gr.tutorbilling.R
import gr.tutorbilling.data.model.StudentWithEarnings
import gr.tutorbilling.utils.getFullName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCard(
    studentWithEarnings: StudentWithEarnings,
    onStudentClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showArchiveDialog by remember { mutableStateOf(false) }

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
                .padding(Dimensions.PaddingMedium),
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
                    text = "€${studentWithEarnings.student.rate}/hour",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column {
                        Text(
                            text = "This week",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "€%.2f".format(studentWithEarnings.weekEarnings),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column {
                        Text(
                            text = "This month",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "€%.2f".format(studentWithEarnings.monthEarnings),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            IconButton(onClick = { showArchiveDialog = true }) {
                Icon(
                    Icons.Default.Archive,
                    contentDescription = stringResource(R.string.archive)
                )
            }
        }
    }

    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text(stringResource(R.string.archive)) },
            text = { Text(stringResource(R.string.archive_student_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showArchiveDialog = false
                    }
                ) {
                    Text(stringResource(R.string.archive))
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

