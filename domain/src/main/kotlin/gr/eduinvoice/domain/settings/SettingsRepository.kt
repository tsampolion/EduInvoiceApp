package gr.eduinvoice.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * Domain interface for settings repository.
 * This allows the app module to depend on domain abstractions rather than data layer implementations.
 * 
 * Added during app→domain migration. Backed by data implementation.
 */
interface SettingsRepository {
    /**
     * Get application settings as a Flow
     */
    val settings: Flow<AppSettings>

    /**
     * Update currency symbol setting
     */
    suspend fun setCurrencySymbol(symbol: String)

    /**
     * Update rounding decimals setting
     */
    suspend fun setRounding(decimals: Int)

    /**
     * Update dark theme setting
     */
    suspend fun setDarkTheme(enabled: Boolean)

    /**
     * Update PDF theme key setting
     */
    suspend fun setPdfThemeKey(key: String)
}
