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
import gr.eduinvoice.ui.design.AppColors
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
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.ui.components.ClickableReadOnlyField
import gr.eduinvoice.utils.getFullName
import androidx.compose.ui.graphics.toArgb
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    onBack: () -> Unit,
    defaultStudentId: Long? = null,
    viewModel: InvoiceViewModel = hiltViewModel()
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
    val context = LocalContext.current
    var showConfirm by remember { mutableStateOf(false) }
    var generatedInvoice by remember { mutableStateOf<Uri?>(null) }

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
                        val invoiceNumber = System.currentTimeMillis().toString()
                        val uri = createInvoicePdf(
                            context = context,
                            directory = File(context.filesDir, "invoices"),
                            lessons = selected,
                            invoiceNumber = invoiceNumber,
                            colorScheme = colors,
                            typography = fonts
                        )
                        viewModel.markAsPaid(selected.map { it.lesson.id })
                        generatedInvoice = uri
                        showConfirm = false
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
private fun LessonRow(item: LessonWithStudent, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(item.student.getFullName(), style = MaterialTheme.typography.bodyMedium)
            Text(LocalDate.parse(item.lesson.date).format(DateTimeFormatter.ofPattern("dd MMM")))
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

fun createInvoicePdf(
    context: android.content.Context,
    directory: File,
    lessons: List<LessonWithStudent>,
    invoiceNumber: String,
    colorScheme: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography,
    tutorName: String = "Tutor Name",
    tutorAddress: String = "123 Education Lane"
): Uri {
    val pdf = android.graphics.pdf.PdfDocument()
    val width = 595
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(width, 842, 1).create()
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas

    val headerPaint = android.graphics.Paint().apply {
        color = colorScheme.primary.toArgb()
    }
    canvas.drawRect(0f, 0f, width.toFloat(), 80f, headerPaint)
    val logo = android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.tutorbilling_logo)
    canvas.drawBitmap(logo, 20f, 10f, null)

    val titlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = colorScheme.onPrimary.toArgb()
        textSize = typography.titleLarge.fontSize.value * 2
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    canvas.drawText(tutorName, 100f, 40f, titlePaint)
    canvas.drawText(tutorAddress, 100f, 60f, titlePaint)

    val infoPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = colorScheme.onBackground.toArgb()
        textSize = typography.titleMedium.fontSize.value * 2
    }
    canvas.drawText("Invoice #$invoiceNumber", width - 200f, 40f, infoPaint)

    var y = 110
    val linePaint = android.graphics.Paint().apply {
        color = colorScheme.outline.toArgb()
        strokeWidth = 1f
    }
    val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = colorScheme.onBackground.toArgb()
        textSize = typography.bodyMedium.fontSize.value * 2
    }
    canvas.drawText("Date", 40f, y.toFloat(), infoPaint)
    canvas.drawText("Student", 180f, y.toFloat(), infoPaint)
    canvas.drawText("Amount", width - 120f, y.toFloat(), infoPaint)
    y += 10
    canvas.drawLine(40f, y.toFloat(), width - 40f, y.toFloat(), linePaint)
    y += 20

    var total = 0.0
    lessons.forEach { item ->
        canvas.drawText(item.lesson.date, 40f, y.toFloat(), textPaint)
        canvas.drawText(item.student.getFullName(), 180f, y.toFloat(), textPaint)
        val amount = item.calculateFee()
        total += amount
        canvas.drawText("€%.2f".format(amount), width - 120f, y.toFloat(), textPaint)
        y += 20
    }
    y += 10
    canvas.drawLine(40f, y.toFloat(), width - 40f, y.toFloat(), linePaint)
    y += 25
    canvas.drawText("Total: €%.2f".format(total), width - 120f, y.toFloat(), infoPaint)

    pdf.finishPage(page)
    if (!directory.exists()) directory.mkdirs()
    val file = File(directory, "invoice-$invoiceNumber.pdf")
    FileOutputStream(file).use { pdf.writeTo(it) }
    pdf.close()
    logo.recycle()
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentDropdown(students: List<gr.eduinvoice.data.model.Student>, selectedId: Long?, onSelect: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = students.firstOrNull { it.id == selectedId }?.getFullName() ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Student") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            students.forEach { student ->
                DropdownMenuItem(text = { Text(student.getFullName()) }, onClick = {
                    onSelect(student.id)
                    expanded = false
                })
            }
        }
    }
}
