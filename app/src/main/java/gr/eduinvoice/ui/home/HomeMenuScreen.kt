package gr.eduinvoice.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import gr.eduinvoice.ui.components.EdgeToEdgeScaffold
import android.util.Log
import gr.eduinvoice.BuildConfig
import gr.eduinvoice.ui.design.AppColors
import gr.eduinvoice.ui.design.Dimensions
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMenuScreen(
    onNavigateToStudent: () -> Unit,
    onClassesClick: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToLesson: () -> Unit,
    onNavigateToNewStudent: () -> Unit,
    onNavigateToNewLesson: () -> Unit,
    onRevenue: () -> Unit,
    onSettings: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: HomeMenuViewModel = hiltViewModel()
) {
    var showFabMenu by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (BuildConfig.DEBUG) {
        Log.d("HomeMenuScreen", "Collected uiState -> $uiState")
    }

    val successContainer = AppColors.successContainer
    val errorContainer = AppColors.errorContainer
    val studentContainerColor = if (uiState.studentCount > 0) successContainer else errorContainer
    if (BuildConfig.DEBUG) {
        Log.d("HomeMenuScreen", "Student button color recalculated for count ${uiState.studentCount}")
    }
    val studentButtonColors = ButtonDefaults.buttonColors(
        containerColor = studentContainerColor
    )


    EdgeToEdgeScaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.PaddingMedium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Added for better alignment
            ) {
                FloatingActionButton(
                    onClick = onRevenue,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) { Icon(Icons.Default.BarChart, contentDescription = "Revenue") }

                // Box for the FAB and its DropdownMenu
                Box(
                    contentAlignment = Alignment.Center // Ensures FAB is centered if Box is larger
                    // Modifier.menuAnchor() removed - it's not a standard modifier here
                ) {
                    FloatingActionButton(
                        onClick = { showFabMenu = !showFabMenu },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) { Icon(Icons.Default.Add, contentDescription = "Add") }

                    DropdownMenu(
                        expanded = showFabMenu,
                        onDismissRequest = { showFabMenu = false }
                        // The DropdownMenu will anchor to the Box by default
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Student") },
                            onClick = {
                                showFabMenu = false
                                onNavigateToNewStudent()
                            }
                        )
                        DropdownMenuItem(text = { Text("Add Lesson") }, onClick = {
                                showFabMenu = false
                                onNavigateToNewLesson()
                            })
                    }
                }

                FloatingActionButton(
                    onClick = onSettings,
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) { Icon(Icons.Default.Settings, contentDescription = "Settings") }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = Dimensions.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Image(
                painterResource(R.drawable.tutorbilling_logo),
                contentDescription = stringResource(R.string.app_logo_desc),
                modifier = Modifier.size(200.dp)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onNavigateToStudent,
                    modifier = Modifier.fillMaxWidth(),
                    colors = studentButtonColors
                ) { Text("Students") }
                Button(
                    onClick = onClassesClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.secondaryContainer)
                ) { Text("Classes") }
                Button(
                    onClick = onNavigateToGroups,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.secondaryContainer)
                ) { Text("Groups") }
                Button(
                    onClick = onNavigateToLesson,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.tertiaryContainer)
                ) { Text("Lessons") }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
