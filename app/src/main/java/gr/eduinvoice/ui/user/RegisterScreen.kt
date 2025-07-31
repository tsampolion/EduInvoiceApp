package gr.eduinvoice.ui.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.R
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import gr.eduinvoice.ui.design.FormCard
import gr.eduinvoice.ui.design.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit,
    onSettings: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.register),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
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
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(Dimensions.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
        ) {
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium * 2))
            Image(
                painter = painterResource(R.drawable.tutorbilling_logo),
                contentDescription = stringResource(R.string.app_logo_desc),
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
            )
            FormCard(containerColor = AppColors.primaryContainer) {
                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::updateFullName,
                    label = { Text(stringResource(R.string.full_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            FormCard(containerColor = AppColors.primaryContainer) {
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = viewModel::updateUsername,
                    label = { Text(stringResource(R.string.username)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            FormCard(containerColor = AppColors.primaryContainer) {
                OutlinedTextField(
                    value = uiState.subjectSpecialty,
                    onValueChange = viewModel::updateSubjectSpecialty,
                    label = { Text(stringResource(R.string.subject_specialty)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            FormCard(containerColor = AppColors.primaryContainer) {
                var expanded by remember { mutableStateOf(false) }
                val ranges = listOf(
                    stringResource(R.string.years_experience_range_0_5),
                    stringResource(R.string.years_experience_range_6_10),
                    stringResource(R.string.years_experience_range_11_15),
                    stringResource(R.string.years_experience_range_16_20),
                    stringResource(R.string.years_experience_range_20_plus)
                )
                val currentRange = when (uiState.yearsExperience) {
                    in 0..5 -> stringResource(R.string.years_experience_range_0_5)
                    in 6..10 -> stringResource(R.string.years_experience_range_6_10)
                    in 11..15 -> stringResource(R.string.years_experience_range_11_15)
                    in 16..20 -> stringResource(R.string.years_experience_range_16_20)
                    else -> stringResource(R.string.years_experience_range_20_plus)
                }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = currentRange,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.years_experience)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ranges.forEach { rangeLabel ->
                            DropdownMenuItem(
                                text = { Text(rangeLabel) },
                                onClick = {
                                    expanded = false
                                    viewModel.updateYearsExperience(rangeLabel)
                                }
                            )
                        }
                    }
                }
            }
            FormCard(containerColor = AppColors.primaryContainer) {
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            FormCard(containerColor = AppColors.primaryContainer) {
                Button(
                    onClick = { viewModel.register(onRegistered) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.register))
                }
            }
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        }
    }
}
