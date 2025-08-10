package gr.eduinvoice.domain.billing

import org.junit.Assert.*
import org.junit.Test

class DomainPdfThemeTest {
    
    @Test
    fun `should create default theme with correct values`() {
        val theme = DomainPdfThemes.Default
        
        assertEquals("#6750A4", theme.colorScheme.primary)
        assertEquals("#FFFFFF", theme.colorScheme.onPrimary)
        assertEquals("#FFFFFF", theme.colorScheme.surface)
        assertEquals("#1C1B1F", theme.colorScheme.onSurface)
    }
    
    @Test
    fun `should create custom theme with correct values`() {
        val colorScheme = DomainPdfColorScheme(
            primary = "#FF0000",
            onPrimary = "#FFFFFF",
            surface = "#000000",
            onSurface = "#FFFFFF",
            surfaceVariant = "#333333",
            outline = "#666666",
            error = "#FF0000",
            success = "#00FF00"
        )
        
        val typography = DomainPdfTypography(
            headlineLarge = DomainPdfTextStyle(32f, 800),
            headlineMedium = DomainPdfTextStyle(28f, 700),
            titleLarge = DomainPdfTextStyle(24f, 600),
            titleMedium = DomainPdfTextStyle(20f, 600),
            bodyLarge = DomainPdfTextStyle(16f, 400),
            bodyMedium = DomainPdfTextStyle(14f, 400),
            labelMedium = DomainPdfTextStyle(12f, 500)
        )
        
        val spacing = DomainPdfSpacing(
            small = 4f,
            medium = 8f,
            large = 16f
        )
        
        val shapes = DomainPdfShapes(
            radiusSmall = 4f,
            radiusMedium = 8f,
            radiusLarge = 16f
        )
        
        val theme = DomainPdfTheme(
            colorScheme = colorScheme,
            typography = typography,
            spacing = spacing,
            shapes = shapes
        )
        
        assertEquals("#FF0000", theme.colorScheme.primary)
        assertEquals(32f, theme.typography.headlineLarge.fontSize, 0.001f)
        assertEquals(4f, theme.spacing.small, 0.001f)
        assertEquals(4f, theme.shapes.radiusSmall, 0.001f)
    }
    
    @Test
    fun `should have correct default spacing values`() {
        val spacing = DomainPdfSpacing()
        
        assertEquals(8f, spacing.small, 0.001f)
        assertEquals(16f, spacing.medium, 0.001f)
        assertEquals(24f, spacing.large, 0.001f)
    }
    
    @Test
    fun `should have correct default shape values`() {
        val shapes = DomainPdfShapes()
        
        assertEquals(8f, shapes.radiusSmall, 0.001f)
        assertEquals(16f, shapes.radiusMedium, 0.001f)
        assertEquals(24f, shapes.radiusLarge, 0.001f)
    }
    
    @Test
    fun `should have correct default text style values`() {
        val textStyle = DomainPdfTextStyle(fontSize = 16f, fontWeight = 400)
        
        assertEquals(16f, textStyle.fontSize, 0.001f)
        assertEquals(400, textStyle.fontWeight)
        assertEquals(0f, textStyle.letterSpacing, 0.001f)
        assertEquals(1.2f, textStyle.lineHeight, 0.001f)
    }
}
