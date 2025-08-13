package architecture

import com.lemonappdev.konsist.api.Konsist
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLayerDependenciesTest {
    @Test
    fun `app must not import data layer types`() {
        val prohibited = listOf(
            "gr.eduinvoice.data.model",
            "gr.eduinvoice.data.database"
        )
        val files = Konsist.scopeFromProject()
            .files
            .filter { it.path.replace('\\','/').contains("/app/") }

        files.forEach { file ->
            val bad = file.imports.map { it.name }
                .filter { name -> prohibited.any { prefix -> name.startsWith(prefix) } }
            assertTrue("Forbidden imports in ${file.path}: $bad", bad.isEmpty())
        }
    }
}
