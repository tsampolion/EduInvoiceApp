package gr.eduinvoice.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
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
    onAddGroupLesson: (Long) -> Unit,
    onMemberClick: (Long) -> Unit,
    onEditGroupMaster: (Long, Long) -> Unit = { _, _ -> },
    viewModel: GroupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClassWarning by remember { mutableStateOf(false) }
    var showBillingWarning by remember { mutableStateOf(false) }
    var pendingClassMismatch by remember { mutableStateOf(false) }
    var pendingBillingMismatch by remember { mutableStateOf(false) }
    var classConfirmed by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = { },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (viewModel.groupId != 0L) {
                FloatingActionButton(onClick = { onAddGroupLesson(viewModel.groupId) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Group Lesson")
                }
            }
        }
    ) { padding ->
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
                        var showArchive by remember { mutableStateOf(false) }
                        var showDelete by remember { mutableStateOf(false) }
                        TextButton(onClick = { showArchive = true }) { Text("Archive") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { showDelete = true }) { Text("Delete") }
                        if (showArchive) {
                            AlertDialog(
                                onDismissRequest = { showArchive = false },
                                title = { Text("Archive Group") },
                                text = { Text("Archive this group? It will be hidden from the main list.") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.archiveGroup()
                                        LaunchedEffect(Unit) {
                                            snackbarHostState.showSnackbar("Group archived")
                                        }
                                        showArchive = false
                                        onBack()
                                    }) { Text("Archive") }
                                },
                                dismissButton = { TextButton(onClick = { showArchive = false }) { Text("Cancel") } }
                            )
                        }
                        if (showDelete) {
                            AlertDialog(
                                onDismissRequest = { showDelete = false },
                                title = { Text("Delete Group") },
                                text = { Text("Are you sure you want to permanently delete this group?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.deleteGroup()
                                        LaunchedEffect(Unit) {
                                            snackbarHostState.showSnackbar("Group deleted")
                                        }
                                        showDelete = false
                                        onBack()
                                    }) { Text("Delete") }
                                },
                                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
                            )
                        }
                    }
                    TextButton(onClick = {
                        if (!viewModel.isFormValid()) {
                            val state = viewModel.uiState.value
                            val targetClass = if (state.selectedClass == "Custom") state.customClass else state.selectedClass
                            val toAddIds = viewModel.getToAddIds()
                            // Mismatch checks for added students only
                            val classMismatchToAdd = viewModel.getToAddSelections().any { it.className != targetClass }
                            val billingMismatchToAdd = viewModel.getToAddSelections().any { it.rateType != state.rateType }
                            // Detect edits to existing group that affect all current members
                            val classChangedForExisting = viewModel.isClassChanged()
                            val billingChangedForExisting = viewModel.isBillingChanged()

                            pendingClassMismatch = classMismatchToAdd || classChangedForExisting
                            pendingBillingMismatch = billingMismatchToAdd || billingChangedForExisting
                            classConfirmed = false

                            if (pendingClassMismatch) {
                                showClassWarning = true
                            } else if (pendingBillingMismatch) {
                                showBillingWarning = true
                            } else {
                                // No mismatches requiring confirmation
                                viewModel.saveGroup(
                                    overrideClass = false,
                                    overrideBilling = false
                                )
                                LaunchedEffect(Unit) { snackbarHostState.showSnackbar("Group saved") }
                                onBack()
                            }
                        }
                    }, enabled = viewModel.isFormValid()) { Text("Save") }
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
                    DropdownMenuItem(
                        text = { Text("Custom") },
                        onClick = {
                            viewModel.updateSelectedClass("Custom")
                            classExpanded = false
                        }
                    )
                }
            }

            val errorMessage = viewModel.uiState.collectAsStateWithLifecycle().value.errorMessage
            if (!errorMessage.isNullOrBlank()) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Action blocked") },
                    text = { Text(errorMessage) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                    }
                )
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

            var rateError by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = uiState.rate,
                onValueChange = {
                    rateError = false
                    viewModel.updateRate(it)
                },
                label = { Text(if (uiState.rateType == DomainRateTypes.HOURLY) "Hourly Rate (€)*" else "Lesson Fee (€)*") },
                isError = rateError,
                supportingText = { if (rateError) Text("Required") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("€") }
            )

            val members = remember(uiState.students) { uiState.students.filter { it.selected } }
            val memberCount = members.size
            val groupRate = uiState.rate.toDoubleOrNull() ?: 0.0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Group Members ($memberCount)", style = MaterialTheme.typography.titleMedium)
                if (groupRate > 0.0 && memberCount > 0) {
                    val total = groupRate * memberCount
                    val suffix = if (uiState.rateType == DomainRateTypes.HOURLY) "/h" else " per lesson"
                    Text(
                        text = "€" + String.format("%.2f", groupRate) + " × $memberCount = €" + String.format("%.2f", total) + suffix,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            if (members.isEmpty()) {
                Text(text = "No members selected")
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    members.forEach { m ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimensions.PaddingSmall),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onMemberClick(m.id) }
                                ) {
                                    Text(text = "${m.name} ${m.surname}", style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AssistChip(onClick = {}, label = { Text(m.className.ifBlank { "No class" }) })
                                        val billingText = if (m.rateType == DomainRateTypes.HOURLY) {
                                            "Hourly €" + String.format("%.2f", m.rate)
                                        } else {
                                            "Per Lesson €" + String.format("%.2f", m.rate)
                                        }
                                        AssistChip(onClick = {}, label = { Text(billingText) })
                                    }
                                }
                                IconButton(onClick = { viewModel.toggleStudent(m.id) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove from group")
                                }
                            }
                        }
                    }
                }
            }

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

            if (viewModel.groupId != 0L) {
                Spacer(Modifier.height(8.dp))
                Text(text = "Group Lesson History", style = MaterialTheme.typography.titleMedium)
                val history by viewModel.lessonHistory.collectAsStateWithLifecycle()
                if (history.isEmpty()) {
                    Text("No group lessons yet.")
                } else {
                    LazyColumn {
                        items(history) { master ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimensions.PaddingSmall),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Date: ${master.date}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Time: ${master.startTime}", style = MaterialTheme.typography.bodySmall)
                                        Text("Duration: ${master.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                                        val absentCount by viewModel.getAbsentCount(master.id).collectAsState(initial = 0)
                                        if (absentCount > 0) {
                                            Text("Absences: $absentCount", style = MaterialTheme.typography.bodySmall)
                                        }
                                        master.notes?.let { Text("Notes: $it", style = MaterialTheme.typography.bodySmall) }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(onClick = { onEditGroupMaster(viewModel.groupId, master.id) }) { Text("Edit") }
                                        var showDelete by remember { mutableStateOf(false) }
                                        TextButton(onClick = { showDelete = true }) { Text("Delete") }
                                        if (showDelete) {
                                            AlertDialog(
                                                onDismissRequest = { showDelete = false },
                                                title = { Text("Delete Group Lesson") },
                                                text = { Text("Are you sure you want to delete this group lesson?") },
                                                confirmButton = {
                                                    TextButton(onClick = {
                                                        viewModel.deleteGroupLesson(master.id)
                                                        showDelete = false
                                                    }) { Text("Delete") }
                                                },
                                                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showClassWarning) {
                AlertDialog(
                    onDismissRequest = { showClassWarning = false },
                    title = { Text("Warning") },
                    text = {
                        val text = buildString {
                            val state = viewModel.uiState.value
                            val targetClass = if (state.selectedClass == "Custom") state.customClass else state.selectedClass
                            if (viewModel.isClassChanged()) {
                                append("You are about to change the class for this group to '")
                                append(targetClass)
                                append("'. This will overwrite this setting for all current members.\n\n")
                            }
                            if (viewModel.hasToAddClassMismatch(targetClass)) {
                                append("Warning: Some newly added students are not in '")
                                append(targetClass)
                                append("'. Adding them will overwrite their current class.\n")
                            }
                        }
                        Text(text)
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showClassWarning = false
                            classConfirmed = true
                            if (pendingBillingMismatch) {
                                showBillingWarning = true
                            } else {
                                // If class changed for existing, override all selected; if only toAdd mismatch, override just added
                                val targets = if (viewModel.isClassChanged()) null else viewModel.getToAddIds()
                                viewModel.saveGroup(overrideClass = true, overrideBilling = false, overrideTargets = targets)
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
                    text = {
                        val text = buildString {
                            val state = viewModel.uiState.value
                            if (viewModel.isBillingChanged()) {
                                append("You are about to change the billing for this group. This will overwrite billing settings for all current members.\n\n")
                            }
                            if (viewModel.getToAddSelections().any { it.rateType != state.rateType }) {
                                append("Warning: Some newly added students have different billing. Saving will overwrite their billing settings.\n")
                            }
                        }
                        Text(text)
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showBillingWarning = false
                            val targets = if (viewModel.isBillingChanged()) null else viewModel.getToAddIds()
                            viewModel.saveGroup(overrideClass = classConfirmed, overrideBilling = true, overrideTargets = targets)
                            onBack()
                        }) { Text("Proceed") }
                    },
                    dismissButton = { TextButton(onClick = { showBillingWarning = false }) { Text("Cancel") } }
                )
            }
        }
    }
}
