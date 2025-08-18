package gr.eduinvoice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchFilterSheet(
    title: String = "Search & Filter",
    query: String,
    onQueryChange: (String) -> Unit,
    sortAscending: Boolean? = null,
    onToggleSort: (() -> Unit)? = null,
    filters: FilterOptions? = null,
    onFiltersChange: ((FilterOptions) -> Unit)? = null,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onQueryChange(it.text)
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search…") },
                singleLine = true
            )

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

            if (filters != null && onFiltersChange != null) {
                // Inline a simplified copy of ModernFilterSheet's content
                Text(text = "Status", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val activeSelected = filters.status.contains("active")
                    val inactiveSelected = filters.status.contains("inactive")
                    ModernChip(text = "Active", selected = activeSelected) {
                        val new = filters.status.toMutableSet()
                        if (activeSelected) new.remove("active") else new.add("active")
                        onFiltersChange(filters.copy(status = new))
                    }
                    ModernChip(text = "Inactive", selected = inactiveSelected) {
                        val new = filters.status.toMutableSet()
                        if (inactiveSelected) new.remove("inactive") else new.add("inactive")
                        onFiltersChange(filters.copy(status = new))
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(text = "Date Range", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                val startState = rememberDatePickerState(initialSelectedDateMillis = filters.dateRange.first)
                val endState = rememberDatePickerState(initialSelectedDateMillis = filters.dateRange.second)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(text = "From", style = MaterialTheme.typography.labelMedium)
                        DatePicker(state = startState, showModeToggle = false)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(text = "To", style = MaterialTheme.typography.labelMedium)
                        DatePicker(state = endState, showModeToggle = false)
                    }
                }
                LaunchedEffect(startState.selectedDateMillis, endState.selectedDateMillis) {
                    onFiltersChange(filters.copy(dateRange = (startState.selectedDateMillis to endState.selectedDateMillis)))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = {
                    onQueryChange("")
                    if (filters != null && onFiltersChange != null) onFiltersChange(FilterOptions())
                    text = TextFieldValue("")
                }) { Text("Clear All") }

                Button(onClick = onDismiss) { Text("Apply") }
            }
        }
    }
}
