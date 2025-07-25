package gr.eduinvoice.ui.components

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
import gr.eduinvoice.ui.design.Dimensions
import gr.eduinvoice.ui.design.AppColors
import gr.eduinvoice.ui.design.MetricCard
import gr.eduinvoice.R
import gr.eduinvoice.data.model.StudentWithEarnings
import gr.eduinvoice.utils.getFullName

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
                    text = stringResource(R.string.currency_format, studentWithEarnings.student.rate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
                ) {
                    MetricCard(
                        label = stringResource(R.string.week_total),
                        value = stringResource(R.string.currency_format, studentWithEarnings.weekEarnings),
                        modifier = Modifier.weight(1f),
                        containerColor = AppColors.successContainer
                    )
                    MetricCard(
                        label = stringResource(R.string.month_total),
                        value = stringResource(R.string.currency_format, studentWithEarnings.monthEarnings),
                        modifier = Modifier.weight(1f),
                        containerColor = AppColors.tertiaryContainer
                    )
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
