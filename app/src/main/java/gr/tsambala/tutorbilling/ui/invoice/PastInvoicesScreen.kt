package gr.tsambala.tutorbilling.ui.invoice

import gr.tsambala.tutorbilling.R
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastInvoicesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val invoicesDir = remember { File(context.filesDir, "invoices") }
    var invoices by remember { mutableStateOf<List<File>>(emptyList()) }

    LaunchedEffect(Unit) {
        invoices = invoicesDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Past Invoices") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { padding ->
        if (invoices.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_invoices))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(invoices) { file ->
                    ListItem(
                        headlineContent = { Text(file.name) },
                        modifier = Modifier.clickable {
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
