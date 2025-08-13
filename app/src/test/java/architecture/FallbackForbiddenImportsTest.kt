package architecture

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FallbackForbiddenImportsTest {
    @Test
    fun noDataImportsInAppSources() {
        val appSrc = File("app/src/main/java")
        val bad = mutableListOf<String>()
        appSrc.walkTopDown()
            .filter { it.isFile && it.extension in listOf("kt","java") }
            .forEach { f ->
                val t = f.readText()
                if (t.contains("import gr.eduinvoice.data.model") ||
                    t.contains("import gr.eduinvoice.data.database")) {
                    bad += f.path
                }
            }
        assertTrue("Forbidden imports in app module:\n${bad.joinToString("\n")}", bad.isEmpty())
    }
}
