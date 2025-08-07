package gr.eduinvoice.utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfThemeManager @Inject constructor() {
    private var currentTheme: ModernPdfTheme = PdfThemes.Default
    fun getTheme(): ModernPdfTheme = currentTheme
    fun setTheme(theme: ModernPdfTheme) { currentTheme = theme }
}


