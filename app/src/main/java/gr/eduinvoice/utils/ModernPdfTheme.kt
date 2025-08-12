package gr.eduinvoice.utils

data class ModernPdfTheme(
    val colorScheme: PdfColorScheme,
    val typography: PdfTypography,
    val spacing: PdfSpacing,
    val shapes: PdfShapes
)

data class PdfColorScheme(
    val primary: Int,
    val onPrimary: Int,
    val surface: Int,
    val onSurface: Int,
    val surfaceVariant: Int,
    val outline: Int,
    val error: Int,
    val success: Int
)

data class PdfTypography(
    val headlineLarge: PdfTextStyle,
    val headlineMedium: PdfTextStyle,
    val titleLarge: PdfTextStyle,
    val titleMedium: PdfTextStyle,
    val bodyLarge: PdfTextStyle,
    val bodyMedium: PdfTextStyle,
    val labelMedium: PdfTextStyle
)

data class PdfTextStyle(
    val fontSize: Float,
    val fontWeight: Int,
    val letterSpacing: Float = 0f,
    val lineHeight: Float = 1.2f
)

data class PdfSpacing(
    val small: Float = 8f,
    val medium: Float = 16f,
    val large: Float = 24f
)

data class PdfShapes(
    val radiusSmall: Float = 8f,
    val radiusMedium: Float = 16f,
    val radiusLarge: Float = 24f
)

object PdfThemes {
    val Default = ModernPdfTheme(
        colorScheme = PdfColorScheme(
            primary = 0xFF6750A4.toInt(),
            onPrimary = 0xFFFFFFFF.toInt(),
            surface = 0xFFFFFFFF.toInt(),
            onSurface = 0xFF1C1B1F.toInt(),
            surfaceVariant = 0xFFE7E0EC.toInt(),
            outline = 0xFF7A757F.toInt(),
            error = 0xFFB3261E.toInt(),
            success = 0xFF2E7D32.toInt()
        ),
        typography = PdfTypography(
            headlineLarge = PdfTextStyle(24f, 700),
            headlineMedium = PdfTextStyle(20f, 600),
            titleLarge = PdfTextStyle(18f, 600),
            titleMedium = PdfTextStyle(16f, 600),
            bodyLarge = PdfTextStyle(14f, 400),
            bodyMedium = PdfTextStyle(12f, 400),
            labelMedium = PdfTextStyle(10f, 500)
        ),
        spacing = PdfSpacing(),
        shapes = PdfShapes()
    )
}
