package gr.tsambala.tutorbilling.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    signInClient: GoogleSignInClient,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val account by viewModel.account.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            task.result?.let { viewModel.saveGoogleAccount(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Settings") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = settings.currencySymbol,
                onValueChange = viewModel::updateCurrencySymbol,
                label = { Text("Currency symbol") }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Decimal places: ${settings.roundingDecimals}")
                Slider(
                    value = settings.roundingDecimals.toFloat(),
                    onValueChange = { viewModel.updateRounding(it.toInt()) },
                    valueRange = 0f..2f,
                    steps = 1,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dark theme")
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = settings.darkTheme,
                    onCheckedChange = viewModel::updateDarkTheme
                )
            }
            if (account == null) {
                Button(onClick = { launcher.launch(signInClient.signInIntent) }) {
                    Text("Sign in with Google")
                }
            } else {
                Text("Signed in as ${'$'}{account.displayName}")
                Button(onClick = viewModel::signOut) {
                    Text("Sign out")
                }
            }
        }
    }
}
