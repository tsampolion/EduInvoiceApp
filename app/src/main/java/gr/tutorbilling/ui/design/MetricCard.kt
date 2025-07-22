package gr.tutorbilling.ui.design

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.primaryContainer
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor)) {
        Column(
            modifier = Modifier.padding(Dimensions.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
