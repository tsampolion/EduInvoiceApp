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
import gr.eduinvoice.ui.design.NavigationMenuButton
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
import androidx.compose.foundation.layout.statusBarsPadding
import gr.eduinvoice.ui.design.SlimHeader

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
                    actions = {
                        IconButton(onClick = onViewArchived) {
                            Icon(Icons.Default.Unarchive, contentDescription = stringResource(R.string.archived_students))
                        }
                    }
                )
                // Bottom-sheet search & filter
                val sortAscending by viewModel.sortAscending.collectAsStateWithLifecycle()
                var showSheet by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.PaddingMedium),
                    horizontalArrangement = Arrangement.Start
                ) {
                    AssistChip(onClick = { showSheet = true }, label = { Text("Search & Filter") })
                }
                if (showSheet) {
                    ModernSearchFilterSheet(
                        title = stringResource(R.string.students),
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::updateSearchQuery,
                        sortAscending = sortAscending,
                        onToggleSort = viewModel::toggleSortOrder,
                        filters = viewModel.filters.collectAsStateWithLifecycle().value,
                        onFiltersChange = viewModel::updateFilters,
                        onDismiss = { showSheet = false }
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
            NavigationMenuButton(
                onClick = openDrawer,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(8.dp)
            )
        }
    }
}
