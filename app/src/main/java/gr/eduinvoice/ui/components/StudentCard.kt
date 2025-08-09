package gr.eduinvoice.ui.components

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
import gr.eduinvoice.ui.design.Dimensions
import gr.eduinvoice.ui.design.MetricCard
import gr.eduinvoice.ui.design.AppColors
import androidx.compose.ui.res.stringResource
import gr.eduinvoice.R
import gr.eduinvoice.data.model.StudentWithEarnings
import gr.eduinvoice.data.model.RateTypes
import gr.eduinvoice.testcompat.getFullName

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

                val rateLabel = if (studentWithEarnings.student.rateType == RateTypes.PER_LESSON) "lesson" else "hour"
                Text(
                    text = "€${studentWithEarnings.student.rate}/$rateLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
                ) {
                    MetricCard(
                        label = "This week",
                        value = "€%.2f".format(studentWithEarnings.weekEarnings),
                        modifier = Modifier.weight(1f),
                        containerColor = AppColors.successContainer
                    )
                    MetricCard(
                        label = "This month",
                        value = "€%.2f".format(studentWithEarnings.monthEarnings),
                        modifier = Modifier.weight(1f),
                        containerColor = AppColors.tertiaryContainer
                    )
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

