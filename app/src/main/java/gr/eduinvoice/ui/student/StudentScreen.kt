package gr.eduinvoice.ui.student

import gr.eduinvoice.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.util.Patterns
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.domain.model.DomainRateTypes
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.utils.ClassOptions
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import gr.eduinvoice.ui.design.SlimHeader
import gr.eduinvoice.ui.design.AppColors
import gr.eduinvoice.ui.design.MetricCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    studentId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToLesson: (Long, Long) -> Unit,
    onAddLesson: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showArchiveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { },
        floatingActionButton = {
            if (!uiState.isEditMode && viewModel.studentId != 0L) {
                FloatingActionButton(onClick = onAddLesson) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_lesson))
                }
            }
        }
    ) { paddingValues ->
        SlimHeader(
            title = when {
                uiState.isEditMode && viewModel.studentId == 0L -> stringResource(R.string.add_student)
                uiState.isEditMode -> stringResource(R.string.edit_student)
                else -> "${uiState.name} ${uiState.surname}".trim()
            },
            onBack = onNavigateBack,
            actions = {
                if (!uiState.isEditMode && viewModel.studentId != 0L) {
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                    }
                    IconButton(onClick = { showArchiveDialog = true }) {
                        Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.archive))
                    }
                }
            }
        )
        if (uiState.isEditMode) {
            StudentEditForm(
                uiState = uiState,
                viewModel = viewModel,
                onSave = {
                    viewModel.saveStudent()
                },
                onCancel = {
                    if (viewModel.studentId == 0L) {
                        onNavigateBack()
                    } else {
                        viewModel.toggleEditMode()
                    }
                },
                modifier = modifier.padding(paddingValues)
            )
        } else {
            StudentDetailView(
                uiState = uiState,
                viewModel = viewModel,
                onLessonClick = onNavigateToLesson,
                modifier = modifier.padding(paddingValues)
            )
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
                        viewModel.deleteStudent()
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

@Composable
private fun StudentMetricsRow(uiState: StudentUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PaddingMedium),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard(
            label = stringResource(R.string.week_total),
            value = "€%.2f".format(uiState.weekEarnings),
            modifier = Modifier.weight(1f),
            containerColor = AppColors.successContainer
        )
        MetricCard(
            label = stringResource(R.string.month_total),
            value = "€%.2f".format(uiState.monthEarnings),
            modifier = Modifier.weight(1f),
            containerColor = AppColors.tertiaryContainer
        )
        MetricCard(
            label = stringResource(R.string.total),
            value = "€%.2f".format(uiState.totalEarnings),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StudentDetailView(
    uiState: StudentUiState,
    viewModel: StudentViewModel,
    onLessonClick: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.PaddingMedium),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.PaddingMedium)
                ) {
                    Text(
                        text = "${uiState.name} ${uiState.surname}".trim(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "€${uiState.rate}/hour",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        item { StudentMetricsRow(uiState = uiState) }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.lessons),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
            )
            HorizontalDivider()
        }

        if (uiState.lessons.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_lessons_prompt),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(
                items = uiState.lessons,
                key = { it.id }
            ) { lesson ->
                val rate = uiState.rate.toDoubleOrNull() ?: 0.0
                val fee = (lesson.durationMinutes / 60.0) * rate
                LessonCard(
                    lesson = lesson,
                    fee = fee,
                    onLessonClick = { onLessonClick(lesson.id, lesson.groupId ?: 0L) },
                    onDeleteClick = { viewModel.deleteLesson(lesson.id) }
                )
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonCard(
    lesson: DomainLesson,
    fee: Double,
    onLessonClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onLessonClick,
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
                    text = LocalDate.parse(lesson.date)
                        .format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    style = MaterialTheme.typography.titleSmall
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
                    text = "€%.2f".format(fee),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_lesson)) },
            text = { Text(stringResource(R.string.delete_lesson_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentEditForm(
    uiState: StudentUiState,
    viewModel: StudentViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nameError = uiState.name.isBlank()
    val rateValue = uiState.rate.toDoubleOrNull()
    val rateError = rateValue == null || rateValue <= 0.0
    var showContactWarning by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(Dimensions.PaddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::updateName,
            label = { Text("First Name*") },
            isError = nameError,
            supportingText = { if (nameError) Text("Required") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        val surnameError = uiState.surname.isBlank()
        OutlinedTextField(
            value = uiState.surname,
            onValueChange = viewModel::updateSurname,
            label = { Text("Surname*") },
            isError = surnameError,
            supportingText = { if (surnameError) Text("Required") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        val mobileError = uiState.parentMobile.isNotBlank() &&
            (uiState.parentMobile.length != 10 ||
                uiState.parentMobile.any { !it.isDigit() })
        OutlinedTextField(
            value = uiState.parentMobile,
            onValueChange = viewModel::updateParentMobile,
            label = { Text("Parent's Mobile") },
            isError = mobileError,
            supportingText = { if (mobileError) Text("10-digit number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        val emailError = uiState.parentEmail.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS.matcher(uiState.parentEmail).matches()
        OutlinedTextField(
            value = uiState.parentEmail,
            onValueChange = viewModel::updateParentEmail,
            label = { Text("Parent's Email") },
            isError = emailError,
            supportingText = { if (emailError) Text("Invalid email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        var classExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = classExpanded,
            onExpandedChange = { classExpanded = !classExpanded }
        ) {
            OutlinedTextField(
                value = uiState.selectedClass,
                onValueChange = {},
                readOnly = true,
                label = { Text("Class*") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(classExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = classExpanded,
                onDismissRequest = { classExpanded = false }
            ) {
                ClassOptions.DEFAULT.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.updateSelectedClass(option)
                            classExpanded = false
                        }
                    )
                }
            }
        }

        val customClassError = uiState.selectedClass == "Custom" && uiState.customClass.isBlank()
        if (uiState.selectedClass == "Custom") {
            OutlinedTextField(
                value = uiState.customClass,
                onValueChange = viewModel::updateCustomClass,
                label = { Text("Class Description*") },
                isError = customClassError,
                supportingText = { if (customClassError) Text("Required") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        var typeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded }
        ) {
            OutlinedTextField(
                value = if (uiState.rateType == DomainRateTypes.HOURLY) "Hourly" else "Per Lesson",
                onValueChange = {},
                readOnly = true,
                label = { Text("Billing Method") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Hourly") },
                    onClick = {
                        viewModel.updateRateType(DomainRateTypes.HOURLY)
                        typeExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Per Lesson") },
                    onClick = {
                        viewModel.updateRateType(DomainRateTypes.PER_LESSON)
                        typeExpanded = false
                    }
                )
            }
        }

        OutlinedTextField(
            value = uiState.rate,
            onValueChange = viewModel::updateRate,
            label = { Text(if (uiState.rateType == DomainRateTypes.HOURLY) "Hourly Rate (€)*" else "Lesson Fee (€)*") },
            isError = rateError,
            supportingText = { if (rateError) Text("Required") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            prefix = { Text("€") }
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = {
                    if (uiState.parentMobile.isBlank() && uiState.parentEmail.isBlank()) {
                        showContactWarning = true
                    } else {
                        onSave()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = uiState.name.isNotBlank() &&
                    uiState.surname.isNotBlank() &&
                    rateValue != null && rateValue > 0 &&
                    uiState.selectedClass.isNotBlank() &&
                    (uiState.selectedClass != "Custom" || uiState.customClass.isNotBlank()) &&
                    (uiState.parentEmail.isBlank() ||
                        Patterns.EMAIL_ADDRESS.matcher(uiState.parentEmail).matches())
            ) {
                Text(stringResource(R.string.save))
            }
        }
        if (showContactWarning) {
            AlertDialog(
                onDismissRequest = { showContactWarning = false },
                title = { Text(stringResource(R.string.contact_details_missing_title)) },
                text = { Text(stringResource(R.string.contact_details_missing_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        showContactWarning = false
                        onSave()
                    }) { Text(stringResource(R.string.proceed)) }
                },
                dismissButton = {
                    TextButton(onClick = { showContactWarning = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }
    }
}
