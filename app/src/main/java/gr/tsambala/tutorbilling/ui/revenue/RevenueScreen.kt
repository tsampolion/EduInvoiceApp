package gr.tsambala.tutorbilling.ui.revenue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import gr.tsambala.tutorbilling.ui.design.AppColors
import gr.tsambala.tutorbilling.ui.design.AppTopBar
import gr.tsambala.tutorbilling.ui.design.Dimensions
import gr.tsambala.tutorbilling.ui.design.MetricCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Intent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.tsambala.tutorbilling.ui.settings.SettingsViewModel
import gr.tsambala.tutorbilling.utils.formatAsCurrency
import gr.tsambala.tutorbilling.ui.revenue.StudentDebt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueScreen(
    onBack: () -> Unit,
    onInvoice: (Long?) -> Unit,
    onPastInvoices: () -> Unit,
    viewModel: RevenueViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Revenue",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = "Daily",
                    value = uiState.dailyRevenue.formatAsCurrency(
                        settings.currencySymbol,
                        settings.roundingDecimals
                    ),
                    modifier = Modifier.weight(1f),
                    containerColor = AppColors.primaryContainer
                )
                MetricCard(
                    label = "Weekly",
                    value = uiState.weeklyRevenue.formatAsCurrency(
                        settings.currencySymbol,
                        settings.roundingDecimals
                    ),
                    modifier = Modifier.weight(1f),
                    containerColor = AppColors.successContainer
                )
                MetricCard(
                    label = "Monthly",
                    value = uiState.monthlyRevenue.formatAsCurrency(
                        settings.currencySymbol,
                        settings.roundingDecimals
                    ),
                    modifier = Modifier.weight(1f),
                    containerColor = AppColors.tertiaryContainer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = "Unpaid",
                    value = uiState.monthlyUnpaid.formatAsCurrency(
                        settings.currencySymbol,
                        settings.roundingDecimals
                    ),
                    modifier = Modifier.weight(1f),
                    containerColor = AppColors.errorContainer
                )
                MetricCard(
                    label = "Paid",
                    value = uiState.monthlyPaid.formatAsCurrency(
                        settings.currencySymbol,
                        settings.roundingDecimals
                    ),
                    modifier = Modifier.weight(1f),
                    containerColor = AppColors.successContainer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onInvoice(null) },
                    modifier = Modifier.weight(1f)
                ) { Text("New Invoice") }
                OutlinedButton(
                    onClick = onPastInvoices,
                    modifier = Modifier.weight(1f)
                ) { Text("Past Invoices") }
            }

            if (uiState.debts.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.debts, key = { it.student.id }) { debt ->
                        StudentDebtRow(
                            debt = debt,
                            onInvoice = { onInvoice(debt.student.id) },
                            onMarkPaid = { viewModel.markLessonsPaid(debt.student.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun StudentDebtRow(
    debt: StudentDebt,
    onInvoice: () -> Unit,
    onMarkPaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(modifier = modifier) {
        Column(Modifier.padding(Dimensions.PaddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(debt.student.name, style = MaterialTheme.typography.titleMedium)
                Text(debt.amount.formatAsCurrency())
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onInvoice, modifier = Modifier.weight(1f)) { Text("Invoice") }
                OutlinedButton(onClick = onMarkPaid, modifier = Modifier.weight(1f)) { Text("Mark Paid") }
                OutlinedButton(
                    onClick = {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Please pay your outstanding balance of " + debt.amount.formatAsCurrency()
                            )
                        }
                        context.startActivity(Intent.createChooser(share, null))
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Reminder") }
            }
        }
    }
}
