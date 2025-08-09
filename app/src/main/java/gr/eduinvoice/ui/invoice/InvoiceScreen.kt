package gr.eduinvoice.ui.invoice

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MenuAnchorType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import gr.eduinvoice.R
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.ui.model.UiInvoiceLesson
import gr.eduinvoice.ui.components.ClickableReadOnlyField
import gr.eduinvoice.testcompat.getFullName
import gr.eduinvoice.ui.settings.SettingsViewModel
import gr.eduinvoice.ui.profile.ProfileViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    onBack: () -> Unit,
    defaultStudentId: Long? = null,
    viewModel: InvoiceViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(defaultStudentId) {
        defaultStudentId?.let { viewModel.selectStudent(it) }
    }
    val startDate by viewModel.startDate.collectAsStateWithLifecycle()
    val endDate by viewModel.endDate.collectAsStateWithLifecycle()
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val selectedStudentId by viewModel.selectedStudentId.collectAsStateWithLifecycle()
    val selectedLessons by viewModel.selectedLessons.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val settings = settingsState.settings
    val user = profileState.user
    val context = LocalContext.current
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var showConfirm by remember { mutableStateOf(false) }
    var generatedInvoice by remember { mutableStateOf<Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Invoice",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.PaddingMedium),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
                Button(
                    onClick = { showConfirm = true },
                    modifier = Modifier.weight(1f),
                    enabled = selectedLessons.isNotEmpty()
                ) { Text("Create Invoice") }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(Dimensions.PaddingMedium)) {
            StudentDropdown(students, selectedStudentId, onSelect = viewModel::selectStudent)
            DateField("Start", startDate) { date -> viewModel.updateStartDate(date) }
            DateField("End", endDate) { date -> viewModel.updateEndDate(date) }

            if (lessons.isNotEmpty()) {
                TextButton(onClick = { viewModel.selectAll() }) {
                    Text("Select All")
                }
            }

            if (lessons.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_lessons_short))
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(lessons) { item ->
                        LessonRow(
                            item = item,
                            checked = selectedLessons.contains(item.lesson.id),
                            onToggle = { viewModel.toggleLesson(item.lesson.id) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                title = { Text("Create Invoice") },
                text = { Text("Generate PDF and mark lessons as paid?") },
                confirmButton = {
                    val colors = MaterialTheme.colorScheme
                    val fonts = MaterialTheme.typography
                TextButton(onClick = {
                        val selected = lessons.filter { selectedLessons.contains(it.lesson.id) }
                        val rawNumber = System.currentTimeMillis().toString()
                        val invoiceNumber = rawNumber.replace(Regex("[^A-Za-z0-9_-]"), "_")
                        val selectedStudent = students.firstOrNull { it.id == selectedStudentId } ?: return@TextButton
                        val invoiceData = gr.eduinvoice.utils.DomainInvoiceData(
                            student = selectedStudent,
                            lessons = selected
                        )
                        val outDir = File(context.filesDir, "invoices").apply { mkdirs() }
                        val outFile = File(outDir, "${invoiceNumber}.pdf")
                        val theme = gr.eduinvoice.utils.PdfThemes.Default
                        val result = gr.eduinvoice.utils.DomainPdfGenerator(context, theme).generateInvoice(invoiceData, outFile)
                        result.fold(
                            onSuccess = { jUri ->
                                viewModel.markAsPaid(selected.map { it.lesson.id })
                                generatedInvoice = android.net.Uri.parse(jUri.toString())
                                showConfirm = false
                            },
                            onFailure = {
                                scope.launch { snackbarHostState.showSnackbar("Failed to create invoice") }
                            }
                        )
                    }) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
                }
            )
        }
        generatedInvoice?.let { uri ->
            AlertDialog(
                onDismissRequest = { generatedInvoice = null },
                title = { Text("Invoice Created") },
                text = { Text("Share or print the invoice?") },
                confirmButton = {
                    TextButton(onClick = {
                        val pdfFile = uri.toFile()
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(
                                Intent.EXTRA_STREAM,
                                FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    pdfFile
                                )
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(share, null))
                        generatedInvoice = null
                    }) { Text("Share") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        val pdfFile = uri.toFile()
                        val printManager =
                            context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
                        printManager.print(
                            "invoice",
                            gr.eduinvoice.utils.PdfFilePrintAdapter(context, pdfFile),
                            null
                        )
                        generatedInvoice = null
                    }) { Text("Print") }
                }
            )
        }
    }
}

@Composable
private fun LessonRow(item: UiInvoiceLesson, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("${item.student.name} ${item.student.surname}", style = MaterialTheme.typography.bodyMedium)
            Text(LocalDate.parse(item.date).format(DateTimeFormatter.ofPattern("dd MMM")))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("€%.2f".format(item.calculateFee()))
            Spacer(Modifier.width(8.dp))
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, date: LocalDate, onDate: (LocalDate) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    ClickableReadOnlyField(
        value = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        onClick = { showPicker = true },
        label = { Text(label) },
    )
    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDate(java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentDropdown(students: List<DomainStudent>, selectedId: Long?, onSelect: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = students.firstOrNull { it.id == selectedId }?.let { "${it.name} ${it.surname}" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Student") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            students.forEach { student ->
                DropdownMenuItem(text = { Text("${student.name} ${student.surname}") }, onClick = {
                    onSelect(student.id)
                    expanded = false
                })
            }
        }
    }
}
