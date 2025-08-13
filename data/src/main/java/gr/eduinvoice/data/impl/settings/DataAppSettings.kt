package gr.eduinvoice.data.impl.settings

import gr.eduinvoice.data.settings.AppSettings as DataAppSettings
import gr.eduinvoice.domain.settings.AppSettings
import javax.inject.Inject

/**
 * Data-side implementation of AppSettings domain interface.
 * Wraps the existing data layer AppSettings data class.
 */
class DataAppSettings @Inject constructor(
    private val dataAppSettings: DataAppSettings
) : AppSettings {

    override val darkTheme: Boolean = dataAppSettings.darkTheme
    override val currencySymbol: String = dataAppSettings.currencySymbol
    override val roundingDecimals: Int = dataAppSettings.roundingDecimals
    override val pdfThemeKey: String = dataAppSettings.pdfThemeKey
}
