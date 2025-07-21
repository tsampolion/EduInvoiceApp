package gr.tsambala.tutorbilling.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import gr.tsambala.tutorbilling.ui.design.AppColors
import gr.tsambala.tutorbilling.ui.design.AppTopBar
import gr.tsambala.tutorbilling.ui.design.Dimensions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.tsambala.tutorbilling.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            SettingCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = settings.currencySymbol,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency") },
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
                        value = settings.roundingDecimals.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Decimal places") },
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
            SettingCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dark theme")
                    Switch(
                        checked = settings.darkTheme,
                        onCheckedChange = viewModel::updateDarkTheme
                    )
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

