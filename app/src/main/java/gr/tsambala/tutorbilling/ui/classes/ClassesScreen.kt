package gr.tsambala.tutorbilling.ui.classes

import gr.tsambala.tutorbilling.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.tsambala.tutorbilling.utils.getFullName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    onBack: () -> Unit,
    onStudentClick: (Long) -> Unit,
    viewModel: ClassesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.hasUnassigned) {
        if (uiState.hasUnassigned) {
            snackbarHostState.showSnackbar("Some students are unassigned to a class")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Classes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (uiState.studentsByClass.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_classes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                uiState.studentsByClass
                    .filterKeys { it != "Unassigned" }
                    .toSortedMap()
                    .forEach { (className, students) ->
                        item {
                            Text(
                                text = className,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                            HorizontalDivider()
                        }
                        items(students) { student ->
                            Text(
                                text = student.getFullName(),
                                modifier = Modifier
                                    .padding(horizontal = 32.dp, vertical = 8.dp)
                                    .clickable { onStudentClick(student.id) }
                            )
                        }
                    }

                if (uiState.hasUnassigned) {
                    uiState.studentsByClass["Unassigned"]?.let { students ->
                        item {
                            Text(
                                text = "Unassigned",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                            HorizontalDivider()
                        }
                        items(students) { student ->
                            Text(
                                text = student.getFullName(),
                                modifier = Modifier
                                    .padding(horizontal = 32.dp, vertical = 8.dp)
                                    .clickable { onStudentClick(student.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
