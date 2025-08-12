package gr.eduinvoice.domain.settings

/**
 * Domain interface for application settings.
 * This allows the app module to depend on domain abstractions rather than data layer implementations.
 *
 * Added during app→domain migration. Backed by data implementation.
 */
interface AppSettings {
    /**
     * Get the dark theme setting
     */
    val darkTheme: Boolean

    /**
     * Get the currency symbol setting
     */
    val currencySymbol: String

    /**
     * Get the rounding decimals setting
     */
    val roundingDecimals: Int

    /**
     * Get the PDF theme key setting
     */
    val pdfThemeKey: String
}
