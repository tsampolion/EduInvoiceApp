package gr.eduinvoice.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.data.model.User
import android.database.sqlite.SQLiteConstraintException
import gr.eduinvoice.domain.user.UserUseCases
import gr.eduinvoice.data.user.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val useCases: UserUseCases,
    private val prefs: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateUsername(value: String) { _uiState.value = _uiState.value.copy(username = value) }
    fun updatePassword(value: String) { _uiState.value = _uiState.value.copy(password = value) }
    fun updateFullName(value: String) { _uiState.value = _uiState.value.copy(fullName = value) }
    fun updateSubjectSpecialty(value: String) { _uiState.value = _uiState.value.copy(subjectSpecialty = value) }
    fun updateYearsExperience(value: String) {
        val years = value.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(yearsExperience = years)
    }

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = User(
                username = _uiState.value.username,
                passwordHash = _uiState.value.password,
                fullName = _uiState.value.fullName,
                subjectSpecialty = _uiState.value.subjectSpecialty,
                yearsExperience = _uiState.value.yearsExperience
            )
            try {
                val id = useCases.createUser(user)
                prefs.setLoggedInUser(id)
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _uiState.value = _uiState.value.copy(error = "Username already exists")
            }
        }
    }

    fun dismissError() { _uiState.value = _uiState.value.copy(error = null) }
}

data class RegisterUiState(
    val username: String = "",
    val password: String = "",
    val fullName: String = "",
    val subjectSpecialty: String = "",
    val yearsExperience: Int = 0,
    val error: String? = null
)