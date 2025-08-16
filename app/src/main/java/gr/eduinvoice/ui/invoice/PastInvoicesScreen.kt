package gr.eduinvoice.ui.invoice

import gr.eduinvoice.R
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import gr.eduinvoice.ui.design.AppTopBar
import gr.eduinvoice.ui.design.Dimensions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import gr.eduinvoice.utils.archiveInvoice
import gr.eduinvoice.utils.deleteInvoice
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastInvoicesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val invoicesDir = remember { File(context.filesDir, "invoices") }
    var invoices by remember { mutableStateOf<List<File>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun refresh() {
        invoices = invoicesDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Scaffold(
        topBar = { },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (invoices.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_invoices))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimensions.PaddingMedium, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Past Invoices", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    }
                }
                items(invoices) { file ->
                    var expanded by remember { mutableStateOf(false) }
                    ListItem(
                        headlineContent = { Text(file.name) },
                        modifier = Modifier.clickable {
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        },
                        trailingContent = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.archive_invoice)) },
                                    onClick = {
                                        try {
                                            archiveInvoice(file)
                                            refresh()
                                        } catch (e: IOException) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    e.message ?: "Failed to archive invoice"
                                                )
                                            }
                                        } finally {
                                            expanded = false
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete_invoice)) },
                                    onClick = {
                                        try {
                                            deleteInvoice(file)
                                            refresh()
                                        } catch (e: IOException) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    e.message ?: "Failed to delete invoice"
                                                )
                                            }
                                        } finally {
                                            expanded = false
                                        }
                                    }
                                )
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
