package gr.eduinvoice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class FilterContext {
    object Students : FilterContext()
    object Lessons : FilterContext()
    object Groups : FilterContext()
    object Classes : FilterContext()
    object Invoices : FilterContext()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextAwareSearchFilterSheet(
    context: FilterContext,
    title: String,
    query: String,
    onQueryChange: (String) -> Unit,
    sortAscending: Boolean? = null,
    onToggleSort: (() -> Unit)? = null,
    onFiltersApply: ((Map<String, Set<String>>) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember(query) { mutableStateOf(TextFieldValue(query)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Search field
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onQueryChange(it.text)
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search...") },
                singleLine = true
            )

            // Sort option
            if (onToggleSort != null && sortAscending != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    AssistChip(
                        onClick = onToggleSort,
                        label = { Text(if (sortAscending) "Sort: A→Z" else "Sort: Z→A") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) }
                    )
                }
            }

            // Context-specific filters
            var appliedFilters by remember { mutableStateOf(mapOf<String, Set<String>>()) }
            
            when (context) {
                is FilterContext.Students -> StudentsFilters { filters ->
                    appliedFilters = filters
                }
                is FilterContext.Lessons -> LessonsFilters { filters ->
                    appliedFilters = filters
                }
                is FilterContext.Groups -> GroupsFilters { filters ->
                    appliedFilters = filters
                }
                is FilterContext.Classes -> ClassesFilters { filters ->
                    appliedFilters = filters
                }
                is FilterContext.Invoices -> InvoicesFilters { filters ->
                    appliedFilters = filters
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        onQueryChange("")
                        text = TextFieldValue("")
                        appliedFilters = mapOf()
                        onFiltersApply?.invoke(mapOf())
                    }
                ) {
                    Text("Clear All")
                }
                Button(onClick = {
                    onFiltersApply?.invoke(appliedFilters)
                    onDismiss()
                }) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun StudentsFilters(onFiltersChange: (Map<String, Set<String>>) -> Unit) {
    var activeSelected by remember { mutableStateOf(true) }
    var inactiveSelected by remember { mutableStateOf(false) }
    var archivedSelected by remember { mutableStateOf(false) }
    var selectedClasses by remember { mutableStateOf(setOf<String>()) }
    
    // Update filters whenever selections change
    LaunchedEffect(activeSelected, inactiveSelected, archivedSelected, selectedClasses) {
        val statusFilters = mutableSetOf<String>()
        if (activeSelected) statusFilters.add("active")
        if (inactiveSelected) statusFilters.add("inactive")
        if (archivedSelected) statusFilters.add("archived")
        
        onFiltersChange(mapOf(
            "status" to statusFilters,
            "classes" to selectedClasses
        ))
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Status", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = activeSelected,
                onClick = { activeSelected = !activeSelected },
                label = { Text("Active") }
            )
            FilterChip(
                selected = inactiveSelected,
                onClick = { inactiveSelected = !inactiveSelected },
                label = { Text("Inactive") }
            )
            FilterChip(
                selected = archivedSelected,
                onClick = { archivedSelected = !archivedSelected },
                label = { Text("Archived") }
            )
        }
        
        Text("Class Level", style = MaterialTheme.typography.titleMedium)
        val classOptions = listOf("A1", "A2", "B1", "B2", "C1", "C2", "Custom")
        LazyColumn(
            modifier = Modifier.heightIn(max = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(classOptions.chunked(3)) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { classLevel ->
                        FilterChip(
                            selected = selectedClasses.contains(classLevel),
                            onClick = {
                                selectedClasses = if (selectedClasses.contains(classLevel)) {
                                    selectedClasses - classLevel
                                } else {
                                    selectedClasses + classLevel
                                }
                            },
                            label = { Text(classLevel) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonsFilters(onFiltersChange: (Map<String, Set<String>>) -> Unit) {
    var paidSelected by remember { mutableStateOf(false) }
    var unpaidSelected by remember { mutableStateOf(true) }
    var invoicedSelected by remember { mutableStateOf(false) }
    var selectedDuration by remember { mutableStateOf(setOf<Int>()) }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Payment Status", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = paidSelected,
                onClick = { paidSelected = !paidSelected },
                label = { Text("Paid") }
            )
            FilterChip(
                selected = unpaidSelected,
                onClick = { unpaidSelected = !unpaidSelected },
                label = { Text("Unpaid") }
            )
            FilterChip(
                selected = invoicedSelected,
                onClick = { invoicedSelected = !invoicedSelected },
                label = { Text("Invoiced") }
            )
        }
        
        Text("Duration", style = MaterialTheme.typography.titleMedium)
        val durations = listOf(30, 45, 60, 90, 120)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            durations.forEach { duration ->
                FilterChip(
                    selected = selectedDuration.contains(duration),
                    onClick = {
                        selectedDuration = if (selectedDuration.contains(duration)) {
                            selectedDuration - duration
                        } else {
                            selectedDuration + duration
                        }
                    },
                    label = { Text("${duration}m") }
                )
            }
        }
    }
}

@Composable
private fun GroupsFilters(onFiltersChange: (Map<String, Set<String>>) -> Unit) {
    var activeSelected by remember { mutableStateOf(true) }
    var inactiveSelected by remember { mutableStateOf(false) }
    var maxStudents by remember { mutableStateOf(setOf<String>()) }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Status", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = activeSelected,
                onClick = { activeSelected = !activeSelected },
                label = { Text("Active") }
            )
            FilterChip(
                selected = inactiveSelected,
                onClick = { inactiveSelected = !inactiveSelected },
                label = { Text("Inactive") }
            )
        }
        
        Text("Max Students", style = MaterialTheme.typography.titleMedium)
        val maxOptions = listOf("1-5", "6-10", "10+", "Unlimited")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            maxOptions.forEach { option ->
                FilterChip(
                    selected = maxStudents.contains(option),
                    onClick = {
                        maxStudents = if (maxStudents.contains(option)) {
                            maxStudents - option
                        } else {
                            maxStudents + option
                        }
                    },
                    label = { Text(option) }
                )
            }
        }
    }
}

@Composable
private fun ClassesFilters(onFiltersChange: (Map<String, Set<String>>) -> Unit) {
    var activeSelected by remember { mutableStateOf(true) }
    var inactiveSelected by remember { mutableStateOf(false) }
    var selectedLevels by remember { mutableStateOf(setOf<String>()) }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Status", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = activeSelected,
                onClick = { activeSelected = !activeSelected },
                label = { Text("Active") }
            )
            FilterChip(
                selected = inactiveSelected,
                onClick = { inactiveSelected = !inactiveSelected },
                label = { Text("Inactive") }
            )
        }
        
        Text("Level", style = MaterialTheme.typography.titleMedium)
        val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            levels.forEach { level ->
                FilterChip(
                    selected = selectedLevels.contains(level),
                    onClick = {
                        selectedLevels = if (selectedLevels.contains(level)) {
                            selectedLevels - level
                        } else {
                            selectedLevels + level
                        }
                    },
                    label = { Text(level) }
                )
            }
        }
    }
}

@Composable
private fun InvoicesFilters(onFiltersChange: (Map<String, Set<String>>) -> Unit) {
    var paidSelected by remember { mutableStateOf(false) }
    var unpaidSelected by remember { mutableStateOf(true) }
    var overdueSelected by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Status", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = paidSelected,
                onClick = { paidSelected = !paidSelected },
                label = { Text("Paid") }
            )
            FilterChip(
                selected = unpaidSelected,
                onClick = { unpaidSelected = !unpaidSelected },
                label = { Text("Unpaid") }
            )
            FilterChip(
                selected = overdueSelected,
                onClick = { overdueSelected = !overdueSelected },
                label = { Text("Overdue") }
            )
        }
    }
}
