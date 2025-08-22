package gr.eduinvoice.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class SnackbarBus {
    var message: String? by mutableStateOf(null)
    fun show(msg: String) { message = msg }
    fun consume() { message = null }
}

val LocalSnackbarBus = staticCompositionLocalOf { SnackbarBus() }
