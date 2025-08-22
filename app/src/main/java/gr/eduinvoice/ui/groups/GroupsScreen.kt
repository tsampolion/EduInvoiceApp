package gr.eduinvoice.ui.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import gr.eduinvoice.ui.components.EdgeToEdgeScaffold
import gr.eduinvoice.ui.components.ModernEmptyGroupsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import gr.eduinvoice.ui.design.NavigationMenuButton
import gr.eduinvoice.ui.design.SlimHeader
import gr.eduinvoice.ui.components.ModernSearchFilterSheet
import gr.eduinvoice.ui.components.FilterOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import gr.eduinvoice.domain.model.DomainAbsence
import gr.eduinvoice.domain.lesson.GetGroupAbsences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    openDrawer: () -> Unit,
    onGroupClick: (Long) -> Unit,
    onAddGroup: () -> Unit,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EdgeToEdgeScaffold(
        topBar = { },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGroup) {
                Icon(Icons.Default.Add, contentDescription = "Add Group")
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            var selectedTab by remember { mutableStateOf(0) }
            Column(Modifier.fillMaxSize()) {
            SlimHeader(title = "Groups")
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Groups") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Absences") })
            }

            var showSheet by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.PaddingMedium),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssistChip(onClick = { showSheet = true }, label = { Text("Search & Filter") })
            }
            if (showSheet) {
                val state = viewModel.uiState.collectAsStateWithLifecycle().value
                ModernSearchFilterSheet(
                    title = "Groups",
                    query = state.searchQuery,
                    onQueryChange = { viewModel.updateQuery(it) },
                    sortAscending = state.sortAscending,
                    onToggleSort = { viewModel.toggleSort() },
                    filters = gr.eduinvoice.ui.components.FilterOptions(),
                    onFiltersChange = { f -> viewModel.updateFilters(f) },
                    onDismiss = { showSheet = false }
                )
            }
            when (selectedTab) {
                0 -> {
                    if (uiState.groups.isEmpty()) {
                        ModernEmptyGroupsState(onCreateGroup = onAddGroup)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            items(uiState.groups) { group ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onGroupClick(group.id) }
                                            .padding(Dimensions.PaddingMedium)
                                    ) {
                                        Text(text = group.name, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> GroupsAbsencesList(viewModel = viewModel)
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

@Composable
private fun GroupsAbsencesList(viewModel: GroupsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val absences by viewModel.absences.collectAsStateWithLifecycle()
    if (absences.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No absences recorded", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        val groupedByGroup = remember(absences) { absences.groupBy { it.groupId } }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            groupedByGroup.forEach { (groupId, groupAbsences) ->
                val groupName = uiState.groups.firstOrNull { it.id == groupId }?.name ?: "Group #$groupId"
                item(key = "group_header_$groupId") {
                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
                    )
                }
                val groupedByStudent = groupAbsences.groupBy { it.studentId }
                groupedByStudent.forEach { (studentId, studentAbsences) ->
                    item(key = "student_header_${groupId}_${studentId}") {
                        val first = studentAbsences.firstOrNull()
                        val display = listOfNotNull(first?.studentName, first?.studentSurname).joinToString(" ").ifBlank { "Student #$studentId" }
                        Text(text = display, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium))
                    }
                    items(studentAbsences, key = { it.id }) { a ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimensions.PaddingMedium),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = a.date, style = MaterialTheme.typography.titleSmall)
                                    Text(text = a.startTime, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
