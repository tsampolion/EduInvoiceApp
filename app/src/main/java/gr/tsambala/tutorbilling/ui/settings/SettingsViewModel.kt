package gr.tsambala.tutorbilling.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import gr.tsambala.tutorbilling.data.auth.AccountRepository
import gr.tsambala.tutorbilling.data.auth.GoogleAccount
import gr.tsambala.tutorbilling.data.settings.AppSettings
import gr.tsambala.tutorbilling.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    val account: StateFlow<GoogleAccount?> = accountRepository.account.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        viewModelScope.launch {
            repository.settings.collectLatest { _settings.value = it }
        }
    }

    fun updateCurrencySymbol(symbol: String) {
        viewModelScope.launch { repository.setCurrencySymbol(symbol) }
    }

    fun updateRounding(decimals: Int) {
        viewModelScope.launch { repository.setRounding(decimals) }
    }

    fun updateDarkTheme(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkTheme(enabled) }
    }

    fun saveGoogleAccount(account: GoogleSignInAccount) {
        viewModelScope.launch { accountRepository.saveAccount(account) }
    }

    fun signOut() {
        viewModelScope.launch { accountRepository.clearAccount() }
    }
}
