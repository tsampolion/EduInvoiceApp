package gr.eduinvoice.data.impl.settings

import gr.eduinvoice.data.settings.SettingsRepository as DataSettingsRepository
import gr.eduinvoice.data.settings.AppSettings as DataAppSettings
import gr.eduinvoice.domain.settings.SettingsRepository
import gr.eduinvoice.domain.settings.AppSettings
import gr.eduinvoice.data.impl.settings.DataAppSettings as DomainDataAppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data-side implementation of SettingsRepository domain interface.
 * Delegates to the existing data layer SettingsRepository implementation.
 */
@Singleton
class DataSettingsRepository @Inject constructor(
    private val dataSettingsRepository: DataSettingsRepository
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataSettingsRepository.settings.map { dataSettings ->
        DomainDataAppSettings(dataSettings)
    }

    override suspend fun setCurrencySymbol(symbol: String) {
        dataSettingsRepository.setCurrencySymbol(symbol)
    }

    override suspend fun setRounding(decimals: Int) {
        dataSettingsRepository.setRounding(decimals)
    }

    override suspend fun setDarkTheme(enabled: Boolean) {
        dataSettingsRepository.setDarkTheme(enabled)
    }

    override suspend fun setPdfThemeKey(key: String) {
        dataSettingsRepository.setPdfThemeKey(key)
    }
}
