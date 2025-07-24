package gr.eduinvoice.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.domain.user.UserUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val useCases: UserUseCases,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    data class ProfileUiState(
        val user: User? = null,
        val fullName: String = "",
        val subjectSpecialty: String = "",
        val yearsExperience: Int = 0
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.loggedInUserId.flatMapLatest { id ->
                id?.let { useCases.getUserProfile(it) } ?: flowOf(null)
            }.collect { user ->
                _uiState.value = ProfileUiState(
                    user = user,
                    fullName = user?.fullName ?: "",
                    subjectSpecialty = user?.subjectSpecialty ?: "",
                    yearsExperience = user?.yearsExperience ?: 0
                )
            }
        }
    }

    fun updateFullName(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value)
    }

    fun updateSubjectSpecialty(value: String) {
        _uiState.value = _uiState.value.copy(subjectSpecialty = value)
    }

    fun updateYearsExperience(value: String) {
        val years = value.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(yearsExperience = years)
    }

    fun saveProfile(onDone: () -> Unit) {
        val current = _uiState.value
        val user = current.user ?: return
        viewModelScope.launch {
            useCases.updateUser(
                user.copy(
                    fullName = current.fullName,
                    subjectSpecialty = current.subjectSpecialty,
                    yearsExperience = current.yearsExperience
                )
            )
            onDone()
        }
    }
}
