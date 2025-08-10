package architecture

import com.lemonappdev.konsist.api.Konsist
import org.junit.Test

class DomainLayerUsageTest {
    @Test
    fun `app should use domain models for business logic`() {
        Konsist
            .scopeFromProject()
            .packages
            .filter { it.name.startsWith("gr.eduinvoice") }
            .classes()
            .assertContainImports(
                listOf(
                    "gr.eduinvoice.domain.model.DomainLesson",
                    "gr.eduinvoice.domain.model.DomainStudent",
                    "gr.eduinvoice.domain.model.DomainUser"
                )
            )
    }

    @Test
    fun `app should not use data models directly in UI components`() {
        Konsist
            .scopeFromProject()
            .packages
            .filter { it.name.startsWith("gr.eduinvoice.ui") }
            .classes()
            .assertNotContainImports(
                listOf(
                    "gr.eduinvoice.data.model.Lesson",
                    "gr.eduinvoice.data.model.Student",
                    "gr.eduinvoice.data.model.User"
                )
            )
    }
}
