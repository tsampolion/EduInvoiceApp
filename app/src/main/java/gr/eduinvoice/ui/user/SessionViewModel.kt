package gr.eduinvoice.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.data.user.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository
) : ViewModel() {
    val currentUserId: StateFlow<Long?> = prefs.loggedInUserId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val isLoggedIn: StateFlow<Boolean> = currentUserId.map { it != null }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    fun setLoggedInUser(id: Long) {
        viewModelScope.launch { prefs.setLoggedInUser(id) }
    }

    fun logout() {
        viewModelScope.launch { prefs.setLoggedInUser(null) }
    }
}