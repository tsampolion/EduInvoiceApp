package architecture

import com.lemonappdev.konsist.api.Konsist
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureRulesTest {
    @Test
    fun `app must not import data except allowed cache`() {
        val prohibitedPrefixes = listOf("gr.eduinvoice.data")
        val allowedPrefixes = listOf(
            // Allow DI-provided stateless utilities used by UI layer
            "gr.eduinvoice.data.cache"
        )
        val files = Konsist.scopeFromProject().files.filter { it.path.replace('\\','/').contains("/app/") }
        files.forEach { file ->
            val imports = file.imports.map { it.name }
            val violations = imports.filter { imp ->
                prohibitedPrefixes.any { p -> imp.startsWith(p) } &&
                        allowedPrefixes.none { a -> imp.startsWith(a) }
            }
            assertTrue("Forbidden app->data imports in ${file.path}: $violations", violations.isEmpty())
        }
    }

    @Test
    fun `domain must not import app or data`() {
        val prohibitedPrefixes = listOf("gr.eduinvoice.data", "gr.eduinvoice.ui", "gr.eduinvoice.app")
        val files = Konsist.scopeFromProject().files.filter { it.path.replace('\\','/').contains("/domain/") }
        files.forEach { file ->
            val imports = file.imports.map { it.name }
            val violations = imports.filter { imp -> prohibitedPrefixes.any { p -> imp.startsWith(p) } }
            assertTrue("Forbidden domain imports in ${file.path}: $violations", violations.isEmpty())
        }
    }

    @Test
    fun `data must not import app ui`() {
        val prohibitedPrefixes = listOf("gr.eduinvoice.ui", "gr.eduinvoice.app")
        val files = Konsist.scopeFromProject().files.filter { it.path.replace('\\','/').contains("/data/") }
        files.forEach { file ->
            val imports = file.imports.map { it.name }
            val violations = imports.filter { imp -> prohibitedPrefixes.any { p -> imp.startsWith(p) } }
            assertTrue("Forbidden data->app/ui imports in ${file.path}: $violations", violations.isEmpty())
        }
    }
}
