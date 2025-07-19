package gr.tsambala.tutorbilling.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val currencySymbol: String = "€",
    val roundingDecimals: Int = 2,
    val darkTheme: Boolean = false
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val CURRENCY = stringPreferencesKey("currency_symbol")
        val ROUNDING = intPreferencesKey("rounding_decimals")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            currencySymbol = prefs[Keys.CURRENCY] ?: "€",
            roundingDecimals = (prefs[Keys.ROUNDING] ?: 2).coerceIn(0, 2),
            darkTheme = prefs[Keys.DARK_THEME] ?: false
        )
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.settingsDataStore.edit { it[Keys.CURRENCY] = symbol }
    }

    suspend fun setRounding(decimals: Int) {
        context.settingsDataStore.edit { it[Keys.ROUNDING] = decimals.coerceIn(0, 2) }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.DARK_THEME] = enabled }
    }
}
