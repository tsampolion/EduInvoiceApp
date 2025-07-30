package gr.eduinvoice.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.data.settings.AppSettings
import gr.eduinvoice.data.settings.SettingsRepository
import gr.eduinvoice.domain.user.UserUseCases
import gr.eduinvoice.data.repository.BackupRepository
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.data.model.User
import android.database.sqlite.SQLiteException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val prefs: UserPreferencesRepository,
    private val userUseCases: UserUseCases,
    private val backupRepository: BackupRepository
) : ViewModel() {

    data class SettingsUiState(
        val settings: AppSettings = AppSettings(),
        val user: User? = null
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.settings,
                prefs.loggedInUserId.flatMapLatest { id ->
                    id?.let { userUseCases.getUserProfile(it) } ?: flowOf(null)
                }
            ) { settings, user ->
                SettingsUiState(settings, user)
            }.collect { _uiState.value = it }
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

    suspend fun exportBackup(): String = backupRepository.exportJson()

    suspend fun restoreBackup(json: String): Boolean = try {
        backupRepository.restoreFromJson(json).isSuccess
    } catch (e: SerializationException) {
        _errorMessage.value = "Invalid backup data: ${e.message}".trim()
        false
    } catch (e: SQLiteException) {
        _errorMessage.value =
            "Database error while restoring backup: ${e.message}".trim()
        false
    }

    fun dismissError() { _errorMessage.value = null }
}
