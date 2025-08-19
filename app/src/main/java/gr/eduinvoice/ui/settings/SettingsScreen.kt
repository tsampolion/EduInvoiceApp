package gr.eduinvoice.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import android.util.Log
import gr.eduinvoice.BuildConfig
import gr.eduinvoice.ui.design.AppColors
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import gr.eduinvoice.ui.design.NavigationMenuButton
import gr.eduinvoice.ui.design.SlimHeader
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHostState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    openDrawer: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    onSwitchAccount: () -> Unit,
    onProfile: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val json = viewModel.exportBackup()
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(json.toByteArray())
                }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                context.contentResolver.openInputStream(it)?.use { ins ->
                    val json = ins.readBytes().decodeToString()
                    val success = viewModel.restoreBackup(json)
                    val msg = if (success) {
                        context.getString(R.string.backup_restored)
                    } else {
                        context.getString(R.string.invalid_backup)
                    }
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(Dimensions.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
            ) {
                SlimHeader(title = stringResource(R.string.settings))
                HorizontalDivider()
                Text(stringResource(R.string.general), style = MaterialTheme.typography.titleMedium)
                state.settings?.let { settings ->
                    SettingCard(containerColor = AppColors.primaryContainer) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = settings.currencySymbol,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.currency)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                currencies.forEach { (symbol, label) ->
                                    DropdownMenuItem(text = { Text("$symbol - $label") }, onClick = {
                                        viewModel.updateCurrencySymbol(symbol)
                                        expanded = false
                                    })
                                }
                            }
                        }
                    }
                    SettingCard(containerColor = AppColors.secondaryContainer) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = settings.roundingDecimals.toString(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.decimal_places)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                listOf(0, 1, 2).forEach { option ->
                                    DropdownMenuItem(text = { Text(option.toString()) }, onClick = {
                                        viewModel.updateRounding(option)
                                        expanded = false
                                    })
                                }
                            }
                        }
                    }
                }

                Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)
                SettingCard(containerColor = AppColors.tertiaryContainer) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.dark_theme))
                        Switch(
                            checked = state.settings?.darkTheme == true,
                            onCheckedChange = viewModel::updateDarkTheme
                        )
                    }
                }
                state.settings?.let { settings ->
                    SettingCard(containerColor = AppColors.tertiaryContainer) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = settings.pdfThemeKey,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("PDF Theme") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                listOf("default").forEach { key ->
                                    DropdownMenuItem(text = { Text(key) }, onClick = {
                                        viewModel.updatePdfTheme(key)
                                        expanded = false
                                    })
                                }
                            }
                        }
                    }
                } // <-- THIS WAS THE MISSING BRACE
                Text(stringResource(R.string.account), style = MaterialTheme.typography.titleMedium)
                val loggedIn = state.user != null
                if (BuildConfig.DEBUG) {
                    Log.d("SettingsScreen", "Account section loggedIn -> $loggedIn")
                }
                state.user?.let { user ->
                    Text(user.fullName.ifBlank { user.username }, style = MaterialTheme.typography.bodyLarge)
                    Button(
                        onClick = onProfile,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.edit_profile))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onLogout, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.logout))
                        }
                        Button(onClick = onSwitchAccount, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.switch_account))
                        }
                    }

                    var showConfirm by remember { mutableStateOf(false) }
                    if (showConfirm) {
                        AlertDialog(
                            onDismissRequest = { showConfirm = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    showConfirm = false
                                    viewModel.deleteAccount { onLogout() }
                                }) { Text(stringResource(R.string.delete)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirm = false }) { Text(stringResource(R.string.cancel)) }
                            },
                            text = { Text(stringResource(R.string.delete_account_confirmation)) }
                        )
                    }
                    Button(
                        onClick = { showConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.delete_account), color = Color.Red) }
                } ?: run {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onLogin, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.sign_in))
                        }
                        Button(onClick = onRegister, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.sign_up))
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { exportLauncher.launch("eduinvoice-backup.json") }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.backup_export))
                    }
                    Button(onClick = { importLauncher.launch(arrayOf("application/json")) }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.backup_restore))
                    }
                }

                Button(
                    onClick = onPrivacyPolicy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.errorContainer)
                ) {
                    Text(stringResource(R.string.privacy_policy))
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
private fun SettingCard(
    containerColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(Dimensions.PaddingMedium), content = content)
    }
}

private val currencies = listOf(
    "€" to "Euros",
    "$" to "Dollars",
    "£" to "Pounds",
    "¥" to "Yen",
    "₹" to "Rupees",
    "₽" to "Rubles",
    "₩" to "Won",
    "₣" to "Francs",
    "₺" to "Lira",
    "₱" to "Pesos"
)