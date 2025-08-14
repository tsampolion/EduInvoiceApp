package gr.eduinvoice.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun EdgeToEdgeScaffold(
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val activity = LocalView.current.context as? Activity
    SideEffect {
        val window = activity?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    Scaffold(
        topBar = topBar ?: {},
        bottomBar = bottomBar ?: {},
        floatingActionButton = floatingActionButton ?: {},
        content = content
    )
}

@Composable
fun ModernScaffold(
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    EdgeToEdgeScaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        content = content
    )
}

@Composable
fun ModernContentScaffold(
    content: @Composable (PaddingValues) -> Unit
) {
    EdgeToEdgeScaffold(
        content = content
    )
}

@Composable
fun ModernTopBarScaffold(
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    EdgeToEdgeScaffold(
        topBar = topBar,
        content = content
    )
}

@Composable
fun ModernBottomBarScaffold(
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    EdgeToEdgeScaffold(
        bottomBar = bottomBar,
        content = content
    )
}
