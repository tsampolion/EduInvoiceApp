package gr.tsambala.tutorbilling.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import gr.tsambala.tutorbilling.ui.design.AppTopBar
import gr.tsambala.tutorbilling.ui.design.Dimensions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import gr.tsambala.tutorbilling.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.privacy_policy),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            Text(stringResource(R.string.privacy_policy_text))
            val url = stringResource(R.string.privacy_policy_url)
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }) {
                Text(stringResource(R.string.open_in_browser))
            }
        }
    }
}
