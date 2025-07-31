package gr.eduinvoice.ui.user

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import gr.eduinvoice.R
import gr.eduinvoice.domain.user.UserUseCases
import gr.eduinvoice.data.user.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val useCases: UserUseCases,
    private val prefs: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateUsername(value: String) { _uiState.value = _uiState.value.copy(username = value) }
    fun updatePassword(value: String) { _uiState.value = _uiState.value.copy(password = value) }

    fun login(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val user = useCases.authenticateUser(_uiState.value.username, _uiState.value.password)
            if (user != null) {
                prefs.setLoggedInUser(user.id)
                onSuccess(user.id)
            } else {
                _uiState.value = _uiState.value.copy(error = context.getString(R.string.error_invalid_password))
            }
        }
    }

    fun dismissError() { _uiState.value = _uiState.value.copy(error = null) }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val error: String? = null
)
