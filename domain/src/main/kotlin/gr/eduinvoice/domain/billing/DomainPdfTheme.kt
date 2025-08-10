package gr.eduinvoice.domain.billing

/**
 * PDF theme configuration in the domain layer
 * This allows the domain to define styling without Android dependencies
 */
data class DomainPdfTheme(
    val colorScheme: DomainPdfColorScheme,
    val typography: DomainPdfTypography,
    val spacing: DomainPdfSpacing,
    val shapes: DomainPdfShapes
)

data class DomainPdfColorScheme(
    val primary: String, // Hex color as string
    val onPrimary: String,
    val surface: String,
    val onSurface: String,
    val surfaceVariant: String,
    val outline: String,
    val error: String,
    val success: String
)

data class DomainPdfTypography(
    val headlineLarge: DomainPdfTextStyle,
    val headlineMedium: DomainPdfTextStyle,
    val titleLarge: DomainPdfTextStyle,
    val titleMedium: DomainPdfTextStyle,
    val bodyLarge: DomainPdfTextStyle,
    val bodyMedium: DomainPdfTextStyle,
    val labelMedium: DomainPdfTextStyle
)

data class DomainPdfTextStyle(
    val fontSize: Float,
    val fontWeight: Int,
    val letterSpacing: Float = 0f,
    val lineHeight: Float = 1.2f
)

data class DomainPdfSpacing(
    val small: Float = 8f,
    val medium: Float = 16f,
    val large: Float = 24f
)

data class DomainPdfShapes(
    val radiusSmall: Float = 8f,
    val radiusMedium: Float = 16f,
    val radiusLarge: Float = 24f
)

object DomainPdfThemes {
    val Default = DomainPdfTheme(
        colorScheme = DomainPdfColorScheme(
            primary = "#6750A4",
            onPrimary = "#FFFFFF",
            surface = "#FFFFFF",
            onSurface = "#1C1B1F",
            surfaceVariant = "#E7E0EC",
            outline = "#7A757F",
            error = "#B3261E",
            success = "#2E7D32"
        ),
        typography = DomainPdfTypography(
            headlineLarge = DomainPdfTextStyle(24f, 700),
            headlineMedium = DomainPdfTextStyle(20f, 600),
            titleLarge = DomainPdfTextStyle(18f, 600),
            titleMedium = DomainPdfTextStyle(16f, 600),
            bodyLarge = DomainPdfTextStyle(14f, 400),
            bodyMedium = DomainPdfTextStyle(12f, 400),
            labelMedium = DomainPdfTextStyle(10f, 500)
        ),
        spacing = DomainPdfSpacing(),
        shapes = DomainPdfShapes()
    )
}
