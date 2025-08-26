package gr.eduinvoice.ui.students

import gr.eduinvoice.R
import androidx.compose.foundation.layout.*
// Removed unused LazyColumn/items imports
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.ui.components.StudentCard
import gr.eduinvoice.ui.components.VirtualStudentList
import gr.eduinvoice.ui.components.ModernSearchFilterSheet
import gr.eduinvoice.ui.components.FilterOptions
import gr.eduinvoice.ui.components.ModernEmptyStudentsState
import gr.eduinvoice.ui.components.MasterActionBox
import gr.eduinvoice.ui.components.ActionButton
import gr.eduinvoice.ui.components.ContextAwareSearchFilterSheet
import gr.eduinvoice.ui.components.FilterContext
import androidx.compose.foundation.layout.statusBarsPadding
import gr.eduinvoice.ui.design.SlimHeader
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Tune

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    onNavigateToStudent: (Long) -> Unit,
    onAddStudent: () -> Unit,
    openDrawer: () -> Unit,
    onViewArchived: () -> Unit,
    viewModel: StudentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { },
        floatingActionButton = {
            if (uiState.students.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onAddStudent,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Student")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                SlimHeader(
                    title = stringResource(R.string.students),
                    onMenuClick = openDrawer,
                    actions = {
                        IconButton(onClick = onViewArchived) {
                            Icon(Icons.Default.Unarchive, contentDescription = stringResource(R.string.archived_students))
                        }
                    }
                )

                // Master Action Box with consolidated functionality
                var isActionBoxExpanded by remember { mutableStateOf(false) }
                var showSearchFilterSheet by remember { mutableStateOf(false) }
                val sortAscending by viewModel.sortAscending.collectAsStateWithLifecycle()

                MasterActionBox(
                    title = "Student Management",
                    isExpanded = isActionBoxExpanded,
                    onToggle = { isActionBoxExpanded = !isActionBoxExpanded },
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onSearchFilterClick = { showSearchFilterSheet = true },
                    actions = listOf(
                        ActionButton(
                            label = "Add Student",
                            icon = Icons.Default.PersonAdd,
                            onClick = onAddStudent,
                            backgroundColor = MaterialTheme.colorScheme.primary
                        ),
                        ActionButton(
                            label = "View Archived",
                            icon = Icons.Default.Archive,
                            onClick = onViewArchived,
                            backgroundColor = MaterialTheme.colorScheme.secondary
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
                        title = stringResource(R.string.students),
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::updateSearchQuery,
                        sortAscending = sortAscending,
                        onToggleSort = viewModel::toggleSortOrder,
                        onDismiss = { showSearchFilterSheet = false }
                    )
                }

                // Virtual scrolling list for students
                if (uiState.students.isEmpty()) {
                    ModernEmptyStudentsState(onAddStudent = onAddStudent)
                } else {
                    VirtualStudentList(
                        students = uiState.students,
                        onStudentClick = { studentId -> onNavigateToStudent(studentId) },
                        onDeleteClick = { studentId -> viewModel.deleteStudent(studentId) },
                        modifier = Modifier.weight(1f),
                        onLoadMore = { viewModel.loadMoreStudents() },
                        isLoadingMore = uiState.isLoadingMore
                    )
                }
            }

        }
    }
}
