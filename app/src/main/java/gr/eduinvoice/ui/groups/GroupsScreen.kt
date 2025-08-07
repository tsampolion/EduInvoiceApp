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
import gr.eduinvoice.ui.components.ModernSearchBar
import gr.eduinvoice.ui.components.ModernFilterSheet
import gr.eduinvoice.ui.components.FilterOptions

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
        topBar = {
            AppTopBar(
                title = "Groups",
                navigationIcon = { NavigationMenuButton(openDrawer) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGroup) {
                Icon(Icons.Default.Add, contentDescription = "Add Group")
            }
        }
    ) { padding ->
        var searchActive by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }
        ModernSearchBar(
            query = query,
            onQueryChange = { query = it },
            onVoiceInput = {},
            onSearch = {},
            active = searchActive,
            onActiveChange = { searchActive = it }
        )
        var showFilters by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.PaddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AssistChip(onClick = { showFilters = true }, label = { Text("Filters") })
        }
        if (showFilters) {
            ModernFilterSheet(
                filters = FilterOptions(),
                onFiltersChange = { /* Future: add filtering for groups */ },
                onDismiss = { showFilters = false }
            )
        }
        if (uiState.groups.isEmpty()) {
            ModernEmptyGroupsState(onCreateGroup = onAddGroup)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(uiState.groups) { group ->
                    Text(
                        text = group.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGroupClick(group.id) }
                            .padding(Dimensions.PaddingMedium)
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
