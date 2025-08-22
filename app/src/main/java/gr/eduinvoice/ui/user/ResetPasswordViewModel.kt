package gr.eduinvoice.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.domain.user.UserUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val useCases: UserUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun updateUsername(value: String) { _uiState.value = _uiState.value.copy(username = value) }
    fun updatePassword(value: String) { _uiState.value = _uiState.value.copy(password = value) }
    fun updateConfirmPassword(value: String) { _uiState.value = _uiState.value.copy(confirmPassword = value) }

    fun resetPassword(onDone: () -> Unit) {
        val state = _uiState.value
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Passwords do not match")
            return
        }
        viewModelScope.launch {
            val success = useCases.resetPassword(state.username, state.password)
            if (success) {
                onDone()
            } else {
                _uiState.value = state.copy(error = "Invalid details")
            }
        }
    }

    fun dismissError() { _uiState.value = _uiState.value.copy(error = null) }
}

data class ResetPasswordUiState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val error: String? = null
)
