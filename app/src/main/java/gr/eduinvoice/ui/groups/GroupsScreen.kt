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
import gr.eduinvoice.ui.components.ModernSearchFilterSheet
import gr.eduinvoice.ui.components.FilterOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp

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
            if (uiState.groups.isNotEmpty()) {
                FloatingActionButton(onClick = onAddGroup) {
                    Icon(Icons.Default.Add, contentDescription = "Add Group")
                }
            }
            NavigationMenuButton(openDrawer)
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Slim header row
            Text(
                text = "Groups",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp)
            )

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
                ModernSearchFilterSheet(
                    title = "Groups",
                    query = "",
                    onQueryChange = { /* TODO: implement query in GroupsViewModel */ },
                    sortAscending = null,
                    onToggleSort = null,
                    filters = null,
                    onFiltersChange = null,
                    onDismiss = { showSheet = false }
                )
            }
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
    }
}
