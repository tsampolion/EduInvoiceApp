package gr.eduinvoice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.input.TextFieldValue

// Base filter options with common fields
data class FilterOptions(
    val dateRange: Pair<Long?, Long?> = null to null,
    val status: Set<String> = emptySet(),
    val classes: Set<String> = emptySet(),
    val customFilters: Map<String, Set<String>> = emptyMap()
)

// Context-specific filter models
data class StudentFilterOptions(
    val dateRange: Pair<Long?, Long?> = null to null,
    val status: Set<String> = emptySet(), // active, inactive, archived
    val classes: Set<String> = emptySet(), // A1, A2, B1, B2, C1, C2, Custom
    val subjectSpecialty: Set<String> = emptySet(),
    val yearsExperience: Set<Int> = emptySet()
)

data class LessonFilterOptions(
    val dateRange: Pair<Long?, Long?> = null to null,
    val paymentStatus: Set<String> = emptySet(), // paid, unpaid, invoiced
    val studentId: Set<Long> = emptySet(),
    val groupId: Set<Long> = emptySet(),
    val duration: Set<Int> = emptySet() // 30, 45, 60, 90, 120 minutes
)

data class GroupFilterOptions(
    val dateRange: Pair<Long?, Long?> = null to null,
    val status: Set<String> = emptySet(), // active, inactive
    val maxStudents: Set<Int> = emptySet(), // 1-10, 10+, unlimited
    val subjectSpecialty: Set<String> = emptySet()
)

data class ClassFilterOptions(
    val dateRange: Pair<Long?, Long?> = null to null,
    val level: Set<String> = emptySet(), // A1, A2, B1, B2, C1, C2
    val status: Set<String> = emptySet(), // active, inactive
    val maxCapacity: Set<Int> = emptySet()
)

data class InvoiceFilterOptions(
    val dateRange: Pair<Long?, Long?> = null to null,
    val status: Set<String> = emptySet(), // paid, unpaid, overdue
    val studentId: Set<Long> = emptySet(),
    val amountRange: Pair<Double?, Double?> = null to null
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ModernFilterSheet(
    filters: FilterOptions,
    onFiltersChange: (FilterOptions) -> Unit,
    onDismiss: () -> Unit,
    headerContent: @Composable () -> Unit = {},
    bodyContent: @Composable () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Filter Options",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            headerContent()
            // Status chips
            Text(text = "Status", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(16.dp))
            Text(text = "Date Range", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            val startState = rememberDatePickerState(initialSelectedDateMillis = filters.dateRange.first)
            val endState = rememberDatePickerState(initialSelectedDateMillis = filters.dateRange.second)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(text = "From", style = MaterialTheme.typography.labelMedium)
                    DatePicker(
                        state = startState,
                        showModeToggle = false
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(text = "To", style = MaterialTheme.typography.labelMedium)
                    DatePicker(
                        state = endState,
                        showModeToggle = false
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LaunchedEffect(startState.selectedDateMillis, endState.selectedDateMillis) {
                onFiltersChange(filters.copy(dateRange = (startState.selectedDateMillis to endState.selectedDateMillis)))
            }
            Spacer(Modifier.height(8.dp))
            bodyContent()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { onFiltersChange(FilterOptions()) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear All")
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Filters")
                }
            }

        }
    }
}
