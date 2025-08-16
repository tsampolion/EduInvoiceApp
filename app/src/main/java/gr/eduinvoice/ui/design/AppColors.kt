package gr.eduinvoice.ui.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val primaryContainer: Color
        @Composable
        get() = MaterialTheme.colorScheme.primaryContainer

    val secondaryContainer: Color
        @Composable
        get() = MaterialTheme.colorScheme.secondaryContainer

    val tertiaryContainer: Color
        @Composable
        get() = MaterialTheme.colorScheme.tertiaryContainer

    val successContainer: Color
        @Composable
        get() = Color(0xFFDFF5E1)

    val errorContainer: Color
        @Composable
        get() = MaterialTheme.colorScheme.errorContainer
}
