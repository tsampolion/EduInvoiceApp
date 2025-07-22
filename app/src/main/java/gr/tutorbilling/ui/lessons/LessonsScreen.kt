package gr.tutorbilling.ui.lessons

import gr.tutorbilling.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import gr.tutorbilling.ui.design.AppColors
import gr.tutorbilling.ui.design.AppTopBar
import gr.tutorbilling.ui.design.Dimensions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.tutorbilling.data.database.LessonWithStudent
import gr.tutorbilling.utils.getFullName
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    onBack: () -> Unit,
    onLessonClick: (Long, Long) -> Unit,
    onAddLesson: () -> Unit,
    onInvoice: (Long?) -> Unit,
    onPastInvoices: () -> Unit,
    viewModel: LessonsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Lessons",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddLesson,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Lesson")
            }
        }
    ) { padding ->
        if (uiState.lessons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.no_lessons))
            }
        } else {
            val grouped = uiState.lessons
                .groupBy { it.student.id }
                .toList()
                .sortedBy { (_, lessons) -> lessons.first().student.getFullName() }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                grouped.forEach { (studentId, lessons) ->
                    val studentName = lessons.first().student.getFullName()
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .testTag("header_${'$'}studentId")
                        ) {
                            Text(
                                text = studentName,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(Dimensions.PaddingMedium)
                            )
                        }
                    }
                    items(lessons, key = { it.lesson.id }) { item ->
                        LessonItem(
                            lessonWithStudent = item,
                            onClick = { onLessonClick(item.student.id, item.lesson.id) },
                            onPaidChange = { viewModel.updatePaid(item.lesson.id, it) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    when (val dialog = uiState.dialog) {
        is LessonDialog.AlreadyInvoiced -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text(stringResource(R.string.already_invoiced_title)) },
                confirmButton = {
                    TextButton(onClick = { viewModel.applyPaidStatus(dialog.lessonId, dialog.paid) }) {
                        Text(stringResource(R.string.force_toggle))
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = { onPastInvoices(); viewModel.dismissDialog() }) {
                            Text(stringResource(R.string.view_past_invoice))
                        }
                        TextButton(onClick = { viewModel.dismissDialog() }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            )
        }
        is LessonDialog.GenerateInvoice -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text(stringResource(R.string.generate_invoice)) },
                text = { Text(stringResource(R.string.generate_invoice_prompt)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.applyPaidStatus(dialog.lessonId, true)
                        onInvoice(dialog.studentId)
                    }) { Text(stringResource(R.string.generate_invoice)) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.applyPaidStatus(dialog.lessonId, true) }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
        null -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonItem(
    lessonWithStudent: LessonWithStudent,
    onClick: () -> Unit,
    onPaidChange: (Boolean) -> Unit
) {
    val lesson = lessonWithStudent.lesson
    val student = lessonWithStudent.student
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.getFullName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = LocalDate.parse(lesson.date)
                        .format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${lesson.startTime} • ${lesson.durationMinutes} min",
                    style = MaterialTheme.typography.bodyMedium
                )
                lesson.notes?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "€%.2f".format(lessonWithStudent.calculateFee()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Paid")
                    Spacer(Modifier.width(4.dp))
                    Checkbox(checked = lesson.isPaid, onCheckedChange = onPaidChange)
                }
            }
        }
    }
}
