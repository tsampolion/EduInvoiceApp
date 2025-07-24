package gr.eduinvoice.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = viewModel::updateFullName,
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::updateUsername,
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.subjectSpecialty,
                onValueChange = viewModel::updateSubjectSpecialty,
                label = { Text("Subject Specialty") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = if (uiState.yearsExperience == 0) "" else uiState.yearsExperience.toString(),
                onValueChange = viewModel::updateYearsExperience,
                label = { Text("Years Experience") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { viewModel.register(onRegistered) }, modifier = Modifier.fillMaxWidth()) {
                Text("Register")
            }
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        }
    }
}
