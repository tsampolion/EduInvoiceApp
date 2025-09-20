package gr.eduinvoice.invoice

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.domain.billing.DomainPdfColorScheme
import gr.eduinvoice.domain.billing.DomainPdfGenerator
import gr.eduinvoice.domain.billing.DomainPdfShapes
import gr.eduinvoice.domain.billing.DomainPdfTextStyle
import gr.eduinvoice.domain.billing.DomainPdfTheme
import gr.eduinvoice.domain.billing.DomainPdfTypography
import gr.eduinvoice.domain.billing.DomainPdfSpacing
import gr.eduinvoice.utils.AndroidPdfGenerator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InvoiceModule {

    @Provides
    @Singleton
    fun provideDomainPdfTheme(): DomainPdfTheme {
        // Map app Material 3 default palette to DomainPdfTheme (light scheme defaults)
        val color = DomainPdfColorScheme(
            primary = "#6650A4",
            onPrimary = "#FFFFFF",
            surface = "#FFFFFF",
            onSurface = "#1C1B1F",
            surfaceVariant = "#E7E0EC",
            outline = "#7A757F",
            error = "#B3261E",
            success = "#2E7D32"
        )
        val typo = DomainPdfTypography(
            headlineLarge = DomainPdfTextStyle(24f, 700),
            headlineMedium = DomainPdfTextStyle(20f, 600),
            titleLarge = DomainPdfTextStyle(18f, 600),
            titleMedium = DomainPdfTextStyle(16f, 600),
            bodyLarge = DomainPdfTextStyle(14f, 400),
            bodyMedium = DomainPdfTextStyle(12f, 400),
            labelMedium = DomainPdfTextStyle(10f, 500)
        )
        val spacing = DomainPdfSpacing()
        val shapes = DomainPdfShapes()
        return DomainPdfTheme(color, typo, spacing, shapes)
    }

    @Provides
    @Singleton
    fun provideDomainPdfGenerator(
        @ApplicationContext context: Context,
        theme: DomainPdfTheme
    ): DomainPdfGenerator = AndroidPdfGenerator(context, theme)
}

