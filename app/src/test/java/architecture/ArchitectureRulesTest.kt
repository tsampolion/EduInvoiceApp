package architecture

import com.lemonappdev.konsist.api.Konsist
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureRulesTest {

    @Test
    fun `app must not import data layer`() {
        val prohibited = listOf("gr.eduinvoice.data.model", "gr.eduinvoice.data.database")
        val files = Konsist.scopeFromProject().files.filter { it.path.contains("/app/") }
        files.forEach { file ->
            val bad = file.imports.map { it.name }.filter { n -> prohibited.any(n::startsWith) }
            assertTrue("Forbidden imports in ${file.path}: $bad", bad.isEmpty())
        }
    }

    @Test
    fun `domain must not import app or data`() {
        val prohibited = listOf("gr.eduinvoice.app", "gr.eduinvoice.ui", "gr.eduinvoice.data")
        val files = Konsist.scopeFromProject().files.filter { it.path.contains("/domain/") }
        files.forEach { file ->
            val bad = file.imports.map { it.name }.filter { n -> prohibited.any(n::startsWith) }
            assertTrue("Domain imports forbidden packages in ${file.path}: $bad", bad.isEmpty())
        }
    }

    @Test
    fun `data must not import app`() {
        val prohibited = listOf("gr.eduinvoice.app", "gr.eduinvoice.ui")
        val files = Konsist.scopeFromProject().files.filter { it.path.contains("/data/") }
        files.forEach { file ->
            val bad = file.imports.map { it.name }.filter { n -> prohibited.any(n::startsWith) }
            assertTrue("Data imports app/ui in ${file.path}: $bad", bad.isEmpty())
        }
    }
}


