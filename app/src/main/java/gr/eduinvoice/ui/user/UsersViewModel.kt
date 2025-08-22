package gr.eduinvoice.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.domain.user.UserUseCases
import gr.eduinvoice.domain.model.DomainUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val useCases: UserUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val users = useCases.getAllUsers()
                _uiState.value = _uiState.value.copy(
                    users = users,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load users",
                    isLoading = false
                )
            }
        }
    }

    fun refreshUsers() {
        loadUsers()
    }

    fun updateUser(user: DomainUser) {
        viewModelScope.launch {
            try {
                useCases.updateUser(user)
                loadUsers() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update user"
                )
            }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            try {
                useCases.deleteAccount(userId)
                loadUsers() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete user"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class UsersUiState(
    val users: List<DomainUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
