package gr.eduinvoice.ui.classes

import gr.eduinvoice.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.testcompat.getFullName
import gr.eduinvoice.ui.design.NavigationMenuButton
import gr.eduinvoice.ui.design.SlimHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    openDrawer: () -> Unit,
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
        topBar = { },
        floatingActionButton = {
            NavigationMenuButton(openDrawer)
        }
    ) { padding ->
        if (uiState.studentsByClass.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                // Use a shared empty state look
                Card {
                    Column(Modifier.padding(Dimensions.PaddingMedium), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(R.string.no_classes), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item { SlimHeader(title = "Classes") }
                uiState.studentsByClass
                    .filterKeys { it != "Unassigned" }
                    .toSortedMap()
                    .forEach { (className, students) ->
                        item {
                            Text(
                                text = className,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
                            )
                            HorizontalDivider()
                        }
                        items(students) { student ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onStudentClick(student.id) }
                                    .padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
                            ) {
                                Text(text = student.getFullName(), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                if (uiState.hasUnassigned) {
                    uiState.studentsByClass["Unassigned"]?.let { students ->
                        item {
                            Text(
                                text = "Unassigned",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
                            )
                            HorizontalDivider()
                        }
                        items(students) { student ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onStudentClick(student.id) }
                                    .padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
                            ) {
                                Text(text = student.getFullName(), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
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
}
