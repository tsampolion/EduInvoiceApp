package gr.eduinvoice.ui.lesson

import gr.eduinvoice.R
import androidx.compose.foundation.layout.*
import android.util.Log
import gr.eduinvoice.BuildConfig
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import gr.eduinvoice.ui.design.AppColors
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.foundation.clickable
import gr.eduinvoice.ui.components.ClickableReadOnlyField
import gr.eduinvoice.testcompat.getFullName
import androidx.compose.runtime.*
import gr.eduinvoice.domain.model.DomainRateTypes
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
import gr.eduinvoice.domain.model.DomainStudent

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

    Scaffold(topBar = { }) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(Dimensions.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (lessonId == 0L) stringResource(R.string.add_lesson) else stringResource(R.string.edit_lesson), style = MaterialTheme.typography.titleLarge)
                Row {
                    var showDelete by remember { mutableStateOf(false) }
                    if (lessonId != 0L) {
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                    if (showDelete) {
                        AlertDialog(
                            onDismissRequest = { showDelete = false },
                            title = { Text(stringResource(R.string.delete_lesson)) },
                            text = { Text(stringResource(R.string.delete_lesson_confirmation)) },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteLesson()
                                    showDelete = false
                                }) {
                                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDelete = false }) { Text(stringResource(R.string.cancel)) }
                            }
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Group Lesson")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = uiState.isGroupLesson, onCheckedChange = viewModel::toggleGroupLesson)
            }
            if (studentId == null) {
                // If launched for a group from Group Details, set group mode
                LaunchedEffect(Unit) {
                    // already handled in ViewModel init via SavedStateHandle
                }
            }

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                val displayText = if (uiState.isGroupLesson) {
                    uiState.selectedGroupId?.let { id ->
                        uiState.availableGroups.firstOrNull { it.id == id }?.name
                    } ?: ""
                } else {
                    uiState.selectedStudentId?.let { id ->
                        uiState.availableStudents.firstOrNull { it.id == id }?.getFullName()
                    } ?: ""
                }
                OutlinedTextField(
                    value = displayText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (uiState.isGroupLesson) "Group*" else "Student*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (uiState.isGroupLesson) {
                        uiState.availableGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    viewModel.updateSelectedGroup(group.id)
                                    expanded = false
                                }
                            )
                        }
                    } else {
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
            }

            // Absences toggle for group lessons
            if (uiState.isGroupLesson) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Mark Absences?")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = uiState.markAbsences, onCheckedChange = viewModel::toggleMarkAbsences)
                }
                if (uiState.markAbsences && uiState.selectedGroupId != null) {
                    val members = remember(uiState.selectedGroupId, uiState.absentStudents) {
                        viewModel.getGroupMembers(uiState.selectedGroupId!!)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        members.forEach { member ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = uiState.absentStudents[member.id] == true,
                                    onCheckedChange = { viewModel.toggleStudentAbsent(member.id) }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(text = member.name + " " + member.surname)
                            }
                        }
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
                    if (BuildConfig.DEBUG) {
                        Log.d("LessonScreen", "Date field clicked -> showDatePicker from $showDatePicker to true")
                    }
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
                                if (BuildConfig.DEBUG) {
                                    Log.d("LessonScreen", "Date selected -> ${'$'}date")
                                }
                            }
                            if (BuildConfig.DEBUG) {
                                Log.d("LessonScreen", "Date picker dismissed -> showDatePicker false")
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            if (BuildConfig.DEBUG) {
                                Log.d("LessonScreen", "Date picker canceled -> showDatePicker false")
                            }
                            showDatePicker = false
                        }) { Text(stringResource(R.string.cancel)) }
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
                    if (BuildConfig.DEBUG) {
                        Log.d("LessonScreen", "Time field clicked -> showTimePicker from $showTimePicker to true")
                    }
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
                            if (BuildConfig.DEBUG) {
                                Log.d("LessonScreen", "Time selected -> ${'$'}{timePickerState.hour}:${'$'}{timePickerState.minute}")
                                Log.d("LessonScreen", "Time picker dismissed -> showTimePicker false")
                            }
                            showTimePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            if (BuildConfig.DEBUG) {
                                Log.d("LessonScreen", "Time picker canceled -> showTimePicker false")
                            }
                            showTimePicker = false
                        }) { Text(stringResource(R.string.cancel)) }
                    },
                    title = { Text("Select time") },
                    text = {
                        TimePicker(state = timePickerState)
                    }
                )
            }

            if (uiState.rateType == DomainRateTypes.HOURLY) {
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
                    containerColor = AppColors.secondaryContainer
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
                        text = "€%.2f".format(if (uiState.rateType == DomainRateTypes.PER_LESSON) uiState.studentRate else viewModel.calculateFee()),
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
                ) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = {
                        viewModel.saveLesson()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = viewModel.isFormValid()
                ) { Text(stringResource(R.string.save)) }
            }
        }
    }
}
