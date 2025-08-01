package gr.eduinvoice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.domain.user.UserUseCases
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SharedUserViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val useCases: UserUseCases
) : ViewModel() {
    val currentUser: StateFlow<User?> =
        prefs.loggedInUserId.flatMapLatest { id ->
            id?.let { useCases.getUserProfile(it) } ?: flowOf(null)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val isLoggedIn: StateFlow<Boolean> =
        currentUser.map { it != null }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )
}
