package gr.eduinvoice.ui.students

import gr.eduinvoice.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
            FloatingActionButton(
                onClick = onAddStudent,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and sort controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.PaddingMedium),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.weight(1f),
                    label = { Text("Search") }
                )
                IconButton(onClick = { viewModel.toggleSortOrder() }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                }
            }

            // Virtual scrolling list for students
            if (uiState.students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_students),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
