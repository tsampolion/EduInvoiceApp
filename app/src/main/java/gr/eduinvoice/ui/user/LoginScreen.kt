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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.provider.Settings
import android.view.autofill.AutofillManager
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
    onResetPassword: () -> Unit,
    onSettings: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val autofillManager = remember { context.getSystemService(AutofillManager::class.java) }
    var showAutofillDialog by remember { mutableStateOf(autofillManager?.hasEnabledAutofillServices() == false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.login),
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
            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::updateUsername,
                label = { Text(stringResource(R.string.username)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                label = { Text(stringResource(R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { viewModel.login { onLoggedIn() } }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.login))
            }
            TextButton(onClick = onResetPassword, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.forgot_password))
            }
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        }
        if (showAutofillDialog) {
            AlertDialog(
                onDismissRequest = { showAutofillDialog = false },
                text = { Text(stringResource(R.string.autofill_setup_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        showAutofillDialog = false
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE))
                    }) {
                        Text(stringResource(R.string.settings))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAutofillDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}
