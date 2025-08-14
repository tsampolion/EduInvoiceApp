package gr.eduinvoice.ui.students

import gr.eduinvoice.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import gr.eduinvoice.ui.components.ModernSearchBar
import gr.eduinvoice.ui.components.ModernFilterSheet
import gr.eduinvoice.ui.components.FilterOptions
import gr.eduinvoice.ui.components.ModernEmptyStudentsState

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
        topBar = {
            AppTopBar(
                title = stringResource(R.string.students),
                navigationIcon = { NavigationMenuButton(openDrawer) },
                actions = {
                    IconButton(onClick = onViewArchived) {
                        Icon(Icons.Default.Unarchive, contentDescription = stringResource(R.string.archived_students))
                    }
                }
            )
        },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and sort controls
            var searchActive by remember { mutableStateOf(false) }
            val history = remember(uiState.searchQuery) { viewModel.getSearchHistorySnapshot() }
            ModernSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                onVoiceInput = { /* TODO: trigger voice input */ },
                onSearch = { /* No-op, we react to query change */ },
                active = searchActive,
                onActiveChange = { searchActive = it },
                suggestionsContent = {
                    Column(Modifier.fillMaxWidth()) {
                        history.forEach { item ->
                            Text(
                                text = item,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
                                    .clickable { viewModel.updateSearchQuery(item) }
                            )
                            HorizontalDivider()
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.PaddingMedium),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                var showFilters by remember { mutableStateOf(false) }
                AssistChip(onClick = { showFilters = true }, label = { Text("Filters") })
                IconButton(onClick = { viewModel.toggleSortOrder() }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                }
                if (showFilters) {
                    ModernFilterSheet(
                        filters = viewModel.filters.collectAsState().value,
                        onFiltersChange = { viewModel.updateFilters(it) },
                        onDismiss = { showFilters = false }
                    )
                }
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
