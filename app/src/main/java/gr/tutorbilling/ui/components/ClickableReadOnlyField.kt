package gr.tutorbilling.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun ClickableReadOnlyField(
    value: String,
    onClick: () -> Unit,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = label,
        singleLine = singleLine,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
    )
}
