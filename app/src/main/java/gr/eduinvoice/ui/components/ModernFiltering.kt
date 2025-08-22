package gr.eduinvoice.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class FilterOptions(
    val dateRange: Pair<Long?, Long?> = null to null,
    val status: Set<String> = emptySet(),
    val classes: Set<String> = emptySet()
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
