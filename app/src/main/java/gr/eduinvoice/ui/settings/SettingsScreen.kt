package gr.eduinvoice.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import gr.eduinvoice.ui.design.AppColors
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    onSwitchAccount: () -> Unit,
    onProfile: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
            Text(stringResource(R.string.general), style = MaterialTheme.typography.titleMedium)
            SettingCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = state.settings.currencySymbol,
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
            SettingCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = state.settings.roundingDecimals.toString(),
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

            Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)
            SettingCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.dark_theme))
                    Switch(
                        checked = state.settings.darkTheme,
                        onCheckedChange = viewModel::updateDarkTheme
                    )
                }
            }
            Text(stringResource(R.string.account), style = MaterialTheme.typography.titleMedium)
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

            Button(
                onClick = onPrivacyPolicy,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(stringResource(R.string.privacy_policy))
            }
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

