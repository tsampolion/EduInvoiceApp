package gr.eduinvoice.ui.invoice

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.utils.TestPdfGenerator
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner
import gr.eduinvoice.MainDispatcherRule
import org.junit.rules.TemporaryFolder
import org.robolectric.annotation.Config
import java.io.File
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

@RunWith(BouncyCastleTestRunner::class)
@Config(sdk = [34])
class InvoicePdfTest : gr.eduinvoice.TestBase() {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @get:Rule
    val temporaryFolder = TemporaryFolder()
    @Test
    fun createInvoicePdfWritesToFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dir = temporaryFolder.newFolder()
        val lesson = Lesson(id = 1, studentId = 1, ownerId = 1L, date = "2024-01-01", startTime = "10:00", durationMinutes = 60)
        val student = Student(id = 1, ownerId = 1L, name = "Bob", surname = "", parentMobile = "", className = "A", rate = 10.0)
        val lessons = listOf(LessonWithStudent(lesson, student))

        val result = TestPdfGenerator.createInvoicePdf(context, dir, lessons, "001", lightColorScheme(), Typography())
        val uri = result.getOrNull()

        val file = File(dir, "invoice-001.pdf")
        assertNotNull(uri)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
    }

    @Test
    fun createInvoicePdfFailsForInvalidDir() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val invalidDir = temporaryFolder.newFile()
        val lesson = Lesson(id = 1, studentId = 1, ownerId = 1L, date = "2024-01-01", startTime = "10:00", durationMinutes = 60)
        val student = Student(id = 1, ownerId = 1L, name = "Bob", surname = "", parentMobile = "", className = "A", rate = 10.0)
        val lessons = listOf(LessonWithStudent(lesson, student))

        val result = TestPdfGenerator.createInvoicePdf(context, invalidDir, lessons, "002", lightColorScheme(), Typography())

        assertTrue(result.isFailure)
    }
}
