package gr.eduinvoice.ui.user

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import gr.eduinvoice.R
import gr.eduinvoice.domain.user.UserUseCases
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.utils.ErrorHandler
import gr.eduinvoice.utils.RetryManager
import gr.eduinvoice.analytics.ErrorReporter
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
    
    // Error handling components
    private val errorHandler = ErrorHandler(context)
    private val retryManager = RetryManager()
    private val errorReporter = ErrorReporter(context)

    fun updateUsername(value: String) { _uiState.value = _uiState.value.copy(username = value) }
    fun updatePassword(value: String) { _uiState.value = _uiState.value.copy(password = value) }

    fun login(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                // Use retry manager for authentication with automatic retry
                val result = retryManager.executeWithRetry(
                    operation = {
                        useCases.authenticateUser(_uiState.value.username, _uiState.value.password)
                    },
                    maxRetries = 2,
                    retryId = "login_${_uiState.value.username}",
                    shouldRetry = { error ->
                        errorHandler.shouldRetry(error)
                    },
                    onRetry = { error, attempt ->
                        errorReporter.reportError(error, "LoginViewModel_Retry_$attempt")
                    }
                )
                
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        prefs.setLoggedInUser(user.id)
                        onSuccess(user.id)
                    } else {
                        // Authentication failed - invalid credentials
                        val errorResult = errorHandler.handleError(
                            Exception("Invalid username or password"), 
                            "Authentication"
                        )
                        _uiState.value = _uiState.value.copy(
                            error = errorResult.userMessage,
                            isLoading = false
                        )
                    }
                } else {
                    // Handle authentication error
                    val error = result.exceptionOrNull() ?: Exception("Unknown authentication error")
                    val errorResult = errorHandler.handleError(error, "Authentication")
                    
                    errorReporter.reportError(error, "LoginViewModel")
                    
                    _uiState.value = _uiState.value.copy(
                        error = errorResult.userMessage,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // Handle unexpected errors
                val errorResult = errorHandler.handleError(e, "LoginViewModel")
                errorReporter.reportError(e, "LoginViewModel_Unexpected")
                
                _uiState.value = _uiState.value.copy(
                    error = errorResult.userMessage,
                    isLoading = false
                )
            }
        }
    }

    fun dismissError() { _uiState.value = _uiState.value.copy(error = null) }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val error: String? = null,
    val isLoading: Boolean = false
)
