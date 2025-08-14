package gr.eduinvoice.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    onBack: () -> Unit,
    viewModel: GroupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (viewModel.groupId == 0L) "Add Group" else "Edit Group",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.groupId != 0L) {
                        var showDelete by remember { mutableStateOf(false) }
                        TextButton(onClick = { showDelete = true }) { Text("Delete") }
                        if (showDelete) {
                            AlertDialog(
                                onDismissRequest = { showDelete = false },
                                title = { Text("Delete Group") },
                                text = { Text("Are you sure you want to delete this group?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.deleteGroup()
                                        showDelete = false
                                        onBack()
                                    }) { Text("Delete") }
                                },
                                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
                            )
                        }
                    }
                    TextButton(onClick = { viewModel.saveGroup(); onBack() }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(text = "Students", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(uiState.students) { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = item.selected,
                            onCheckedChange = { viewModel.toggleStudent(item.id) }
                        )
                        Text(text = item.name)
                    }
                }
            }
        }
    }
}
