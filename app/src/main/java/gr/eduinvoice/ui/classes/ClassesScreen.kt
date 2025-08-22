package gr.eduinvoice.ui.classes

import gr.eduinvoice.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.testcompat.getFullName

import gr.eduinvoice.ui.design.SlimHeader
import gr.eduinvoice.ui.components.EdgeToEdgeScaffold
import gr.eduinvoice.ui.components.ModernEmptyClassesState
import gr.eduinvoice.ui.components.ModernSearchFilterSheet
import gr.eduinvoice.ui.components.FilterOptions

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

    EdgeToEdgeScaffold(
        topBar = { },
        bottomBar = { SnackbarHost(hostState = snackbarHostState) },

    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .semantics {
                    contentDescription = "Classes screen showing students organized by class"
                    testTag = "classes_screen"
                }
        ) {
            AnimatedVisibility(
                visible = uiState.studentsByClass.isEmpty(),
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
                    targetOffsetY = { -it / 3 },
                    animationSpec = tween(200, easing = FastOutLinearInEasing)
                )
            ) {
                ModernEmptyClassesState()
            }
            
            AnimatedVisibility(
                visible = uiState.studentsByClass.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(300, delayMillis = 100)) + slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("classes_list")
                        .semantics {
                            contentDescription = "List of classes and students"
                        }
                ) {
                    item { 
                        var showInfoDialog by remember { mutableStateOf(false) }
                        val sortAscending by viewModel.sortAscending.collectAsStateWithLifecycle()
                        
                        SlimHeader(
                            title = "Classes",
                            onMenuClick = openDrawer,
                            actions = {
                                IconButton(
                                    onClick = { showInfoDialog = true },
                                    modifier = Modifier
                                        .testTag("classes_info_button")
                                        .semantics {
                                            contentDescription = "Show classes information"
                                            role = Role.Button
                                        }
                                ) {
                                                                    Icon(
                                    Icons.Default.Info, 
                                    contentDescription = "Classes info"
                                )
                                }
                                IconButton(
                                    onClick = { viewModel.toggleSortOrder() },
                                    modifier = Modifier
                                        .testTag("classes_sort_button")
                                        .semantics {
                                            contentDescription = if (sortAscending) 
                                                "Sort classes descending" 
                                            else 
                                                "Sort classes ascending"
                                            role = Role.Button
                                        }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Sort, 
                                        contentDescription = "Sort classes"
                                    )
                                }
                            }
                        )
                        
                        if (showInfoDialog) {
                            val totalClasses = uiState.studentsByClass.keys.filterNot { it == "Unassigned" }.size
                            val totalStudents = uiState.studentsByClass.values.flatten().size
                            val unassignedCount = uiState.studentsByClass["Unassigned"]?.size ?: 0
                            
                            AlertDialog(
                                onDismissRequest = { showInfoDialog = false },
                                title = { Text("Classes Information") },
                                text = {
                                    Column {
                                        Text("Total Classes: $totalClasses")
                                        Text("Total Students: $totalStudents")
                                        if (unassignedCount > 0) {
                                            Text("Unassigned Students: $unassignedCount")
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showInfoDialog = false }) {
                                        Text("OK")
                                    }
                                },
                                modifier = Modifier.testTag("classes_info_dialog")
                            )
                        }
                    }
                    
                    // Search & Filter
                    item {
                        var showSheet by remember { mutableStateOf(false) }
                        val chipScale by animateFloatAsState(
                            targetValue = if (showSheet) 0.95f else 1f,
                            animationSpec = tween(150),
                            label = "chipScale"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimensions.PaddingMedium)
                                .graphicsLayer {
                                    scaleX = chipScale
                                    scaleY = chipScale
                                },
                            horizontalArrangement = Arrangement.Start
                        ) {
                            AssistChip(
                                onClick = { showSheet = true }, 
                                label = { Text("Search & Filter") },
                                modifier = Modifier
                                    .testTag("search_filter_chip")
                                    .semantics {
                                        contentDescription = "Open search and filter options"
                                        role = Role.Button
                                    }
                            )
                        }
                        if (showSheet) {
                            val sortAscending by viewModel.sortAscending.collectAsStateWithLifecycle()
                            ModernSearchFilterSheet(
                                title = "Classes",
                                query = viewModel.searchQuery.collectAsStateWithLifecycle().value,
                                onQueryChange = viewModel::updateSearchQuery,
                                sortAscending = sortAscending,
                                onToggleSort = viewModel::toggleSortOrder,
                                filters = viewModel.filters.collectAsStateWithLifecycle().value,
                                onFiltersChange = viewModel::updateFilters,
                                onDismiss = { showSheet = false }
                            )
                        }
                    }
                    
                    uiState.studentsByClass
                        .filterKeys { it != "Unassigned" }
                        .toSortedMap()
                        .forEach { (className, students) ->
                            item {
                                val scale by animateFloatAsState(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "cardScale"
                                )
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                        .testTag("class_header_$className")
                                        .semantics {
                                            contentDescription = "Class: $className"
                                            heading()
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Text(
                                        text = className,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(Dimensions.PaddingMedium),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                HorizontalDivider()
                            }
                            items(students) { student ->
                                var isPressed by remember { mutableStateOf(false) }
                                val studentCardScale by animateFloatAsState(
                                    targetValue = if (isPressed) 0.96f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "studentCardScale"
                                )
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
                                        .graphicsLayer {
                                            scaleX = studentCardScale
                                            scaleY = studentCardScale
                                        }
                                        .testTag("student_card_${student.id}")
                                        .semantics {
                                            contentDescription = "Student: ${student.getFullName()}"
                                            role = Role.Button
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onStudentClick(student.id) }
                                            .padding(Dimensions.PaddingMedium)
                                    ) {
                                        Text(
                                            text = student.getFullName(), 
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }

                    if (uiState.hasUnassigned) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
                                    .testTag("unassigned_header")
                                    .semantics {
                                        contentDescription = "Unassigned students section"
                                        heading()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(
                                    text = "Unassigned",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(Dimensions.PaddingMedium),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            HorizontalDivider()
                        }
                        uiState.studentsByClass["Unassigned"]?.let { students ->
                            items(students) { student ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onStudentClick(student.id) }
                                            .padding(Dimensions.PaddingMedium)
                                    ) {
                                        Text(
                                            text = student.getFullName(), 
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }


        }
    }
}
