package gr.eduinvoice.ui.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onReschedules: () -> Unit,
    batchStudentId: Long? = null,
    openPayOnStart: Boolean = false,
    viewModel: LessonsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    androidx.compose.runtime.LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    EdgeToEdgeScaffold(
        topBar = { },
        bottomBar = { SnackbarHost(hostState = snackbarHostState) },
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
                SlimHeader(
                    title = stringResource(R.string.lessons),
                    actions = {
                        TextButton(onClick = { onPastInvoices() }) { Text("Past Invoices") }
                        TextButton(onClick = onReschedules) { Text("Reschedules") }
                    }
                )
                // Batch actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.PaddingMedium, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var showPaySheet by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(openPayOnStart) }
                    var showRescheduleSheet by remember { mutableStateOf(false) }
                    var showInvoiceSheet by remember { mutableStateOf(false) }
                    AssistChip(onClick = { showPaySheet = true }, label = { Text("Mark Paid (Batch)") })
                    AssistChip(onClick = { showRescheduleSheet = true }, label = { Text("Bulk Reschedule") })
                    AssistChip(onClick = { showInvoiceSheet = true }, label = { Text("Batch Invoice") })
                    if (showPaySheet) {
                        val scopedLessons = batchStudentId?.let { id -> uiState.lessons.filter { it.lesson.studentId == id } } ?: uiState.lessons
                        BatchPaySheet(
                            lessons = scopedLessons,
                            onDismiss = { showPaySheet = false },
                            onConfirm = { ids, date, notes ->
                                viewModel.createPaymentBatch(ids, date, notes)
                                showPaySheet = false
                            }
                        )
                    }
                    if (showRescheduleSheet) {
                        BulkRescheduleSheet(
                            lessons = uiState.lessons,
                            onDismiss = { showRescheduleSheet = false },
                            onConfirm = { ids, newDate, newTime, newDuration, notes ->
                                viewModel.bulkReschedule(ids, newDate, newTime, newDuration, notes)
                                showRescheduleSheet = false
                            }
                        )
                    }
                    if (showInvoiceSheet) {
                        BatchInvoiceSheet(
                            lessons = uiState.lessons,
                            onDismiss = { showInvoiceSheet = false },
                            onConfirm = { ids, invoiceDate, notes ->
                                viewModel.createInvoiceForSelected(ids, invoiceDate, notes)
                                showInvoiceSheet = false
                            }
                        )
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchPaySheet(
    lessons: List<UiLessonWithStudent>,
    onDismiss: () -> Unit,
    onConfirm: (ids: List<Long>, batchDate: String, notes: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var notes by remember { mutableStateOf("") }
    val today = LocalDate.now().toString()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Mark Paid (Batch)", style = MaterialTheme.typography.titleLarge)
            val alreadyPaid = lessons.count { it.lesson.isPaid }
            val alreadyInvoiced = lessons.count { it.lesson.isInvoiced }
            if (alreadyPaid > 0 || alreadyInvoiced > 0) {
                Text(text = "Info: ${alreadyPaid} paid, ${alreadyInvoiced} invoiced in list", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LazyColumn(Modifier.heightIn(max = 300.dp)) {
                items(lessons) { item ->
                    val id = item.lesson.id
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "${item.student.name} ${item.student.surname} • ${item.lesson.date}")
                        Checkbox(checked = selected.contains(id), onCheckedChange = {
                            selected = selected.toMutableSet().apply { if (contains(id)) remove(id) else add(id) }
                        })
                    }
                }
            }
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth())
            val selectedPaid = lessons.count { selected.contains(it.lesson.id) && it.lesson.isPaid }
            val selectedInvoiced = lessons.count { selected.contains(it.lesson.id) && it.lesson.isInvoiced }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Selected: ${selected.size}")
                if (selectedPaid > 0 || selectedInvoiced > 0) {
                    Text(text = "${selectedPaid} already paid, ${selectedInvoiced} invoiced", color = MaterialTheme.colorScheme.error)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = { onConfirm(selected.toList(), today, notes.ifBlank { null }) }, enabled = selected.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("Confirm") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BulkRescheduleSheet(
    lessons: List<UiLessonWithStudent>,
    onDismiss: () -> Unit,
    onConfirm: (ids: List<Long>, newDate: String, newTime: String, newDuration: Int, notes: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var notes by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf("18:00") }
    var duration by remember { mutableStateOf("60") }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Bulk Reschedule", style = MaterialTheme.typography.titleLarge)
            val lockedCount = lessons.count { it.lesson.isPaid || it.lesson.isInvoiced }
            if (lockedCount > 0) {
                Text(text = "Warning: ${lockedCount} lesson(s) are paid/invoiced and will be blocked", color = MaterialTheme.colorScheme.error)
            }
            LazyColumn(Modifier.heightIn(max = 300.dp)) {
                items(lessons) { item ->
                    val id = item.lesson.id
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "${item.student.name} ${item.student.surname} • ${item.lesson.date} ${item.lesson.startTime}")
                        Checkbox(checked = selected.contains(id), onCheckedChange = {
                            selected = selected.toMutableSet().apply { if (contains(id)) remove(id) else add(id) }
                        })
                    }
                }
            }
            OutlinedTextField(value = date.toString(), onValueChange = { runCatching { date = LocalDate.parse(it) } }, label = { Text("New date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("New time (HH:mm)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = duration, onValueChange = { duration = it.filter { ch -> ch.isDigit() }.take(3) }, label = { Text("Duration (minutes)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth())
            val selectedLocked = lessons.count { selected.contains(it.lesson.id) && (it.lesson.isPaid || it.lesson.isInvoiced) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Selected: ${selected.size}")
                if (selectedLocked > 0) {
                    Text(text = "${selectedLocked} locked", color = MaterialTheme.colorScheme.error)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = { onConfirm(selected.toList(), date.toString(), time, duration.toIntOrNull() ?: 60, notes.ifBlank { null }) }, enabled = selected.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("Apply") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchInvoiceSheet(
    lessons: List<UiLessonWithStudent>,
    onDismiss: () -> Unit,
    onConfirm: (ids: List<Long>, invoiceDate: String, notes: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var notes by remember { mutableStateOf("") }
    var invoiceDate by remember { mutableStateOf(LocalDate.now().toString()) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Batch Invoice", style = MaterialTheme.typography.titleLarge)
            val alreadyInvoiced = lessons.count { it.lesson.isInvoiced }
            if (alreadyInvoiced > 0) {
                Text(text = "Info: ${alreadyInvoiced} already invoiced in list", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LazyColumn(Modifier.heightIn(max = 300.dp)) {
                items(lessons) { item ->
                    val id = item.lesson.id
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "${item.student.name} ${item.student.surname} • ${item.lesson.date}")
                        Checkbox(checked = selected.contains(id), onCheckedChange = {
                            selected = selected.toMutableSet().apply { if (contains(id)) remove(id) else add(id) }
                        })
                    }
                }
            }
            OutlinedTextField(value = invoiceDate, onValueChange = { invoiceDate = it }, label = { Text("Invoice date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Selected: ${selected.size}")
                val selectedInvoiced = lessons.count { selected.contains(it.lesson.id) && it.lesson.isInvoiced }
                if (selectedInvoiced > 0) {
                    Text(text = "${selectedInvoiced} already invoiced", color = MaterialTheme.colorScheme.error)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = { onConfirm(selected.toList(), invoiceDate, notes.ifBlank { null }) }, enabled = selected.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("Create") }
            }
        }
    }
}
