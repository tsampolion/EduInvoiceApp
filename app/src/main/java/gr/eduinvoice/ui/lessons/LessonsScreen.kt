package gr.eduinvoice.ui.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import gr.eduinvoice.ui.components.EdgeToEdgeScaffold
import gr.eduinvoice.ui.components.ModernEmptyLessonsState
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import gr.eduinvoice.ui.design.NavigationMenuButton
import gr.eduinvoice.ui.design.SlimHeader
import androidx.compose.material3.HorizontalDivider
import gr.eduinvoice.ui.components.ModernSearchFilterSheet
import gr.eduinvoice.ui.components.FilterOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import gr.eduinvoice.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import gr.eduinvoice.ui.model.UiLessonWithStudent
import gr.eduinvoice.ui.components.VirtualLessonList
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    openDrawer: () -> Unit,
    onLessonClick: (Long, Long, Long) -> Unit,
    onAddLesson: () -> Unit,
    onInvoice: (Long?) -> Unit,
    onPastInvoices: () -> Unit,
    viewModel: LessonsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EdgeToEdgeScaffold(
        topBar = { },
        floatingActionButton = {
            if (uiState.lessons.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onAddLesson,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Lesson")
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                SlimHeader(title = stringResource(R.string.lessons))
                // Bottom-sheet search & filter
                val query by viewModel.searchQuery.collectAsStateWithLifecycle()
                var showSheet by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.PaddingMedium),
                    horizontalArrangement = Arrangement.Start
                ) {
                    AssistChip(onClick = { showSheet = true }, label = { Text("Search & Filter") })
                }
                if (showSheet) {
                    ModernSearchFilterSheet(
                        title = stringResource(R.string.lessons),
                        query = query,
                        onQueryChange = viewModel::updateSearchQuery,
                        sortAscending = null,
                        onToggleSort = null,
                        filters = viewModel.filters.collectAsStateWithLifecycle().value,
                        onFiltersChange = viewModel::updateFilters,
                        onDismiss = { showSheet = false }
                    )
                }
                if (uiState.lessons.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ModernEmptyLessonsState(onAddLesson = onAddLesson)
                    }
                } else {
                    VirtualLessonList(
                        lessons = uiState.lessons,
                        onLessonClick = { studentId, lessonId, groupId ->
                            onLessonClick(studentId, lessonId, groupId)
                        },
                        onPaidChange = { lessonId, isPaid ->
                            viewModel.updatePaid(lessonId, isPaid)
                        },
                        modifier = Modifier.fillMaxSize(),
                        onLoadMore = { viewModel.loadMoreLessons() },
                        isLoadingMore = uiState.isLoadingMore
                    )
                }
            }
            NavigationMenuButton(
                onClick = openDrawer,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(8.dp)
            )
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
        is LessonDialog.GenerateInvoice -> {}
        null -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonItem(
    lessonWithStudent: UiLessonWithStudent,
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
                    text = "${student.name} ${student.surname}",
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
