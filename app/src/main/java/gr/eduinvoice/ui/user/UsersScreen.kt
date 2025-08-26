package gr.eduinvoice.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.ui.design.SlimHeader
import gr.eduinvoice.ui.design.Dimensions
import gr.eduinvoice.domain.model.DomainUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    openDrawer: () -> Unit,
    onUserClick: (Long) -> Unit,
    viewModel: UsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<DomainUser?>(null) }
    var showEditDialog by remember { mutableStateOf<DomainUser?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = { }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                SlimHeader(
                    title = "User Management",
                    onMenuClick = openDrawer,
                    actions = {
                        IconButton(
                            onClick = { viewModel.refreshUsers() }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh users")
                        }
                    }
                )

                if (uiState.users.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No users found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Dimensions.PaddingMedium)
                    ) {
                        items(uiState.users) { user ->
                            UserCard(
                                user = user,
                                onUserClick = onUserClick,
                                onEditClick = { showEditDialog = user },
                                onDeleteClick = { showDeleteDialog = user },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Delete confirmation dialog
        showDeleteDialog?.let { user ->
            // Prevent admin deletion
            if (user.username == "admin") {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Admin Profile Protected") },
                    text = {
                        Text("The admin profile cannot be deleted. This is a system-critical account that must remain active.")
                    },
                    confirmButton = {
                        TextButton(onClick = { showDeleteDialog = null }) {
                            Text("OK")
                        }
                    }
                )
            } else {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Delete User") },
                    text = {
                        Text("Are you sure you want to delete user '${user.username}'? This action cannot be undone.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteUser(user.id)
                                showDeleteDialog = null
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        // Edit user dialog
        showEditDialog?.let { user ->
            EditUserDialog(
                user = user,
                onDismiss = { showEditDialog = null },
                onSave = { updatedUser ->
                    viewModel.updateUser(updatedUser)
                    showEditDialog = null
                }
            )
        }
    }
}

@Composable
private fun UserCard(
    user: DomainUser,
    onUserClick: (Long) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onUserClick(user.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    // Show admin badge
                    if (user.username == "admin") {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (user.subjectSpecialty.isNotBlank()) {
                    Text(
                        text = user.subjectSpecialty,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (user.yearsExperience > 0) {
                    Text(
                        text = "${user.yearsExperience} years experience",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit user",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Only show delete button for non-admin users
                if (user.username != "admin") {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete user",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    // Show protected indicator for admin
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Admin profile protected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditUserDialog(
    user: DomainUser,
    onDismiss: () -> Unit,
    onSave: (DomainUser) -> Unit
) {
    var username by remember { mutableStateOf(user.username) }
    var fullName by remember { mutableStateOf(user.fullName) }
    var subjectSpecialty by remember { mutableStateOf(user.subjectSpecialty) }
    var yearsExperience by remember { mutableStateOf(user.yearsExperience.toString()) }

    val isAdmin = user.username == "admin"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAdmin, // Disable username editing for admin
                    supportingText = if (isAdmin) {
                        { Text("Admin username cannot be changed") }
                    } else null
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subjectSpecialty,
                    onValueChange = { subjectSpecialty = it },
                    label = { Text("Subject Specialty") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = yearsExperience,
                    onValueChange = { yearsExperience = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Years Experience") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedUser = user.copy(
                        username = username,
                        fullName = fullName,
                        subjectSpecialty = subjectSpecialty,
                        yearsExperience = yearsExperience.toIntOrNull() ?: 0
                    )
                    onSave(updatedUser)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
