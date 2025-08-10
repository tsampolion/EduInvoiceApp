package architecture

import com.lemonappdev.konsist.api.Konsist
import org.junit.Test

class AppLayerDependenciesTest {
    @Test
    fun `app should not import data layer packages`() {
        Konsist
            .scopeFromProject()
            .packages
            .filter { it.name.startsWith("gr.eduinvoice") }
            .classes()
            .assertNotContainImports(
                listOf(
                    "gr.eduinvoice.data.model",
                    "gr.eduinvoice.data.database",
                    "gr.eduinvoice.data.adapter",
                    "gr.eduinvoice.data.concurrency",
                    "gr.eduinvoice.data.repository",
                    "gr.eduinvoice.data.dao",
                    "gr.eduinvoice.data.mapper"
                )
            )
    }

    @Test
    fun `app should only depend on domain layer for business logic`() {
        Konsist
            .scopeFromProject()
            .packages
            .filter { it.name.startsWith("gr.eduinvoice") }
            .classes()
            .assertNotContainImports(
                listOf(
                    "gr.eduinvoice.data.*"
                )
            )
    }
}
