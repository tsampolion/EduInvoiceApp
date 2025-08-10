package architecture

import com.lemonappdev.konsist.api.Konsist
import org.junit.Test

class ArchitectureValidationTest {
    
    @Test
    fun `validate complete architecture boundaries`() {
        // 1. App should not import any data layer packages
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

        // 2. App should use domain models for business logic
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

        // 3. UI components should not directly use data models
        Konsist
            .scopeFromProject()
            .packages
            .filter { it.name.startsWith("gr.eduinvoice.ui") }
            .classes()
            .assertNotContainImports(
                listOf(
                    "gr.eduinvoice.data.model.Lesson",
                    "gr.eduinvoice.data.model.Student",
                    "gr.eduinvoice.data.model.User",
                    "gr.eduinvoice.data.model.RateTypes"
                )
            )

        // 4. ViewModels should not directly use data models
        Konsist
            .scopeFromProject()
            .packages
            .filter { it.name.startsWith("gr.eduinvoice.ui") && it.name.endsWith("ViewModel") }
            .classes()
            .assertNotContainImports(
                listOf(
                    "gr.eduinvoice.data.model.*",
                    "gr.eduinvoice.data.database.*"
                )
            )
    }

    @Test
    fun `validate layer separation`() {
        // App layer should only depend on domain and external libraries
        val appClasses = Konsist
            .scopeFromProject()
            .packages
            .filter { it.name.startsWith("gr.eduinvoice") }
            .classes()

        // Check that no data layer imports exist
        appClasses.assertNotContainImports(
            listOf("gr.eduinvoice.data.*")
        )

        // Check that domain layer imports exist (indicating proper usage)
        appClasses.assertContainImports(
            listOf("gr.eduinvoice.domain.*")
        )
    }
}
