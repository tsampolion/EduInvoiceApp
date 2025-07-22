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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.R
import gr.eduinvoice.ui.components.ArchivedStudentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedStudentsScreen(
    onBack: () -> Unit,
    onStudentClick: (Long) -> Unit,
    viewModel: ArchivedStudentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.archived_students),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
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
                        label = { Text(stringResource(R.string.search)) }
                    )
                    IconButton(onClick = { viewModel.toggleSortOrder() }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.sort))
                    }
                }
            }

            if (uiState.students.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_archived_students),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
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
