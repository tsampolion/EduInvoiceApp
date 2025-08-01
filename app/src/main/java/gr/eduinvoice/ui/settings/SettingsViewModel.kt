package gr.eduinvoice.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import gr.eduinvoice.BuildConfig
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
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
                    id?.let {
                        userUseCases.getUserProfile(it).catch { e ->
                            if (BuildConfig.DEBUG) {
                                Log.e("SettingsViewModel", "Error fetching user $it", e)
                            }
                            emit(null)
                        }
                    } ?: flowOf(null)
                }
            ) { settings, user ->
                SettingsUiState(settings, user)
            }.collect { state ->
                if (BuildConfig.DEBUG) {
                    Log.d("SettingsViewModel", "Collected user -> ${'$'}{state.user}")
                }
                _uiState.value = state
            }
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

    suspend fun restoreBackup(json: String): Boolean {
        return try {
            Json.parseToJsonElement(json)
            backupRepository.restoreFromJson(json).isSuccess
        } catch (e: SerializationException) {
            _errorMessage.value = "Invalid backup data: ${e.message}".trim()
            false
        } catch (e: SQLiteException) {
            _errorMessage.value =
                "Database error while restoring backup: ${e.message}".trim()
            false
        }
    }


    fun dismissError() { _errorMessage.value = null }
}
