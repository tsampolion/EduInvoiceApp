package gr.eduinvoice.ui.students

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.R
import gr.eduinvoice.ui.components.ArchivedStudentCard
import gr.eduinvoice.ui.components.ModernSearchFilterSheet
import gr.eduinvoice.ui.components.MasterActionBox
import gr.eduinvoice.ui.components.ActionButton
import gr.eduinvoice.ui.components.ContextAwareSearchFilterSheet
import gr.eduinvoice.ui.components.FilterContext
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Tune
import gr.eduinvoice.ui.design.SlimHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedStudentsScreen(
    onBack: () -> Unit,
    onStudentClick: (Long) -> Unit,
    viewModel: ArchivedStudentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            SlimHeader(
                title = stringResource(R.string.archived_students),
                onBack = onBack
            )

            // Master Action Box with consolidated functionality
            var isActionBoxExpanded by remember { mutableStateOf(false) }
            var showSearchFilterSheet by remember { mutableStateOf(false) }
            val sortAscending by viewModel.sortAscending.collectAsStateWithLifecycle()

            MasterActionBox(
                title = "Archived Student Management",
                isExpanded = isActionBoxExpanded,
                onToggle = { isActionBoxExpanded = !isActionBoxExpanded },
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSearchFilterClick = { showSearchFilterSheet = true },
                actions = listOf(
                    ActionButton(
                        label = "Restore Selected",
                        icon = Icons.Default.Restore,
                        onClick = { /* Bulk restore functionality */ },
                        backgroundColor = MaterialTheme.colorScheme.primary
                    ),
                    ActionButton(
                        label = "Advanced Filters",
                        icon = Icons.Default.Tune,
                        onClick = { showSearchFilterSheet = true },
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ),
                modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
            )

            // Context-aware search and filter sheet
            if (showSearchFilterSheet) {
                ContextAwareSearchFilterSheet(
                    context = FilterContext.Students,
                    title = stringResource(R.string.archived_students),
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    sortAscending = sortAscending,
                    onToggleSort = viewModel::toggleSortOrder,
                    onDismiss = { showSearchFilterSheet = false }
                )
            }

            // Content list
            if (uiState.students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_archived_students),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = uiState.students,
                        key = { it.student.id }
                    ) { studentWithEarnings ->
                        ArchivedStudentCard(
                            studentWithEarnings = studentWithEarnings,
                            onStudentClick = { onStudentClick(studentWithEarnings.student.id) },
                            onRestoreClick = { viewModel.restoreStudent(studentWithEarnings.student.id) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
