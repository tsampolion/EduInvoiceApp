package gr.eduinvoice.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import gr.eduinvoice.domain.model.DomainRateTypes
import gr.eduinvoice.utils.ClassOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    onBack: () -> Unit,
    viewModel: GroupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClassWarning by remember { mutableStateOf(false) }
    var showBillingWarning by remember { mutableStateOf(false) }
    var pendingClassMismatch by remember { mutableStateOf(false) }
    var pendingBillingMismatch by remember { mutableStateOf(false) }
    var classConfirmed by remember { mutableStateOf(false) }

    Scaffold(topBar = { }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (viewModel.groupId == 0L) "Add Group" else "Edit Group", style = MaterialTheme.typography.titleLarge)
                Row {
                    if (viewModel.groupId != 0L) {
                        var showDelete by remember { mutableStateOf(false) }
                        TextButton(onClick = { showDelete = true }) { Text("Delete") }
                        if (showDelete) {
                            AlertDialog(
                                onDismissRequest = { showDelete = false },
                                title = { Text("Delete Group") },
                                text = { Text("Are you sure you want to delete this group?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.deleteGroup()
                                        showDelete = false
                                        onBack()
                                    }) { Text("Delete") }
                                },
                                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
                            )
                        }
                    }
                    val nameState = viewModel.uiState.collectAsStateWithLifecycle().value.name
                    var nameErrorHeader by remember { mutableStateOf(false) }
                    TextButton(onClick = {
                        if (nameState.isBlank()) {
                            nameErrorHeader = true
                        } else {
                            val state = viewModel.uiState.value
                            val targetClass = if (state.selectedClass == "Custom") state.customClass else state.selectedClass
                            val selectedStudents = state.students.filter { it.selected }
                            val classMismatch = selectedStudents.any { it.className != targetClass }
                            val billingMismatch = selectedStudents.any { it.rateType != state.rateType }
                            pendingClassMismatch = classMismatch
                            pendingBillingMismatch = billingMismatch
                            classConfirmed = false
                            if (classMismatch) {
                                showClassWarning = true
                            } else if (billingMismatch) {
                                showBillingWarning = true
                            } else {
                                viewModel.saveGroup(overrideClass = false, overrideBilling = false)
                                onBack()
                            }
                        }
                    }) { Text("Save") }
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            }
            var nameError by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = uiState.name,
                onValueChange = {
                    nameError = false
                    viewModel.updateName(it)
                },
                isError = nameError,
                supportingText = { if (nameError) Text("Required") },
                label = { Text("Group Name*") },
                modifier = Modifier.fillMaxWidth()
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
                    label = { Text("Group Class*") },
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
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("€") }
            )

            Text(text = "Students", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(uiState.students) { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = item.selected,
                            onCheckedChange = { viewModel.toggleStudent(item.id) }
                        )
                        Text(text = item.name)
                    }
                }
            }

            if (showClassWarning) {
                AlertDialog(
                    onDismissRequest = { showClassWarning = false },
                    title = { Text("Warning") },
                    text = { Text("Warning: Some selected students are not in ${uiState.selectedClass.ifBlank { "the selected class" }}. Adding them to this group will overwrite their current class. Do you want to proceed?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showClassWarning = false
                            classConfirmed = true
                            if (pendingBillingMismatch) {
                                showBillingWarning = true
                            } else {
                                viewModel.saveGroup(overrideClass = true, overrideBilling = false)
                                onBack()
                            }
                        }) { Text("Proceed") }
                    },
                    dismissButton = { TextButton(onClick = { showClassWarning = false }) { Text("Cancel") } }
                )
            }
            if (showBillingWarning) {
                AlertDialog(
                    onDismissRequest = { showBillingWarning = false },
                    title = { Text("Warning") },
                    text = { Text("Warning: The billing method for some selected students differs from the group's. This will overwrite their individual billing settings. Do you want to proceed?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showBillingWarning = false
                            viewModel.saveGroup(overrideClass = classConfirmed, overrideBilling = true)
                            onBack()
                        }) { Text("Proceed") }
                    },
                    dismissButton = { TextButton(onClick = { showBillingWarning = false }) { Text("Cancel") } }
                )
            }
        }
    }
}
