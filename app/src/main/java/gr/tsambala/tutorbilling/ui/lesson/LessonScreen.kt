package gr.tsambala.tutorbilling.ui.lesson

import gr.tsambala.tutorbilling.R
import androidx.compose.foundation.layout.*
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import gr.tsambala.tutorbilling.ui.design.AppColors
import gr.tsambala.tutorbilling.ui.design.AppTopBar
import gr.tsambala.tutorbilling.ui.design.Dimensions
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.foundation.clickable
import gr.tsambala.tutorbilling.ui.components.ClickableReadOnlyField
import gr.tsambala.tutorbilling.utils.getFullName
import androidx.compose.runtime.*
import gr.tsambala.tutorbilling.data.model.RateTypes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    studentId: Long?,
    lessonId: Long,
    onNavigateBack: () -> Unit,
    viewModel: LessonViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (lessonId == 0L) "Add Lesson" else "Edit Lesson",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showDelete by remember { mutableStateOf(false) }
                    if (lessonId != 0L) {
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }

                    if (showDelete) {
                        AlertDialog(
                            onDismissRequest = { showDelete = false },
                            title = { Text("Delete Lesson") },
                            text = { Text("Are you sure you want to delete this lesson?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteLesson()
                                    showDelete = false
                                }) {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(Dimensions.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
        ) {
            // Student picker
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = uiState.selectedStudentId?.let { id ->
                        uiState.availableStudents.firstOrNull { it.id == id }?.getFullName()
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Student*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    uiState.availableStudents.forEach { student ->
                        DropdownMenuItem(
                            text = { Text(student.getFullName()) },
                            onClick = {
                                viewModel.updateSelectedStudent(student.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Date input
            var showDatePicker by remember { mutableStateOf(false) }
            val currentDate = try {
                LocalDate.parse(uiState.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            } catch (_: Exception) {
                LocalDate.now()
            }
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = currentDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            ClickableReadOnlyField(
                value = uiState.date,
                onClick = {
                    Log.d("LessonScreen", "Date field clicked -> showDatePicker from $showDatePicker to true")
                    showDatePicker = true
                },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                viewModel.updateDate(date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                                Log.d("LessonScreen", "Date selected -> ${'$'}date")
                            }
                            Log.d("LessonScreen", "Date picker dismissed -> showDatePicker false")
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            Log.d("LessonScreen", "Date picker canceled -> showDatePicker false")
                            showDatePicker = false
                        }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // Time input
            var showTimePicker by remember { mutableStateOf(false) }
            val (startHour, startMinute) = uiState.startTime.split(":").mapNotNull { it.toIntOrNull() }
                .let { if (it.size == 2) it[0] to it[1] else LocalTime.now().hour to LocalTime.now().minute }
            val timePickerState = rememberTimePickerState(startHour, startMinute, true)
            ClickableReadOnlyField(
                value = uiState.startTime,
                onClick = {
                    Log.d("LessonScreen", "Time field clicked -> showTimePicker from $showTimePicker to true")
                    showTimePicker = true
                },
                label = { Text("Start Time") },
                modifier = Modifier.fillMaxWidth()
            )
            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.updateStartTime(
                                "%02d:%02d".format(
                                    timePickerState.hour,
                                    timePickerState.minute
                                )
                            )
                            Log.d("LessonScreen", "Time selected -> ${'$'}{timePickerState.hour}:${'$'}{timePickerState.minute}")
                            Log.d("LessonScreen", "Time picker dismissed -> showTimePicker false")
                            showTimePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            Log.d("LessonScreen", "Time picker canceled -> showTimePicker false")
                            showTimePicker = false
                        }) { Text("Cancel") }
                    },
                    title = { Text("Select time") },
                    text = {
                        TimePicker(state = timePickerState)
                    }
                )
            }

            if (uiState.rateType == RateTypes.HOURLY) {
                OutlinedTextField(
                    value = uiState.durationMinutes,
                    onValueChange = viewModel::updateDuration,
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.durationMinutes.toIntOrNull()?.let { it < 60 } ?: false,
                    supportingText = {
                        if ((uiState.durationMinutes.toIntOrNull() ?: 0) < 60) Text("Minimum 60")
                    }
                )
            }

            // Fee calculation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.PaddingMedium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lesson Fee",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "€%.2f".format(if (uiState.rateType == RateTypes.PER_LESSON) uiState.studentRate else viewModel.calculateFee()),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text(stringResource(R.string.lesson_note)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Paid")
                Spacer(Modifier.width(8.dp))
                Switch(checked = uiState.isPaid, onCheckedChange = viewModel::updatePaid)
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }
                Button(
                    onClick = {
                        viewModel.saveLesson()
                        if (lessonId == 0L) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = viewModel.isFormValid()
                ) { Text("Save") }
            }
        }
    }
}
