package gr.eduinvoice.ui.invoice

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class InvoicePdfTest {
    @Test
    fun createInvoicePdfWritesToFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dir = kotlin.io.path.createTempDirectory().toFile()
        val lesson = Lesson(id = 1, studentId = 1, date = "2024-01-01", startTime = "10:00", durationMinutes = 60)
        val student = Student(id = 1, name = "Bob", surname = "", parentMobile = "", className = "A", rate = 10.0)
        val lessons = listOf(LessonWithStudent(lesson, student))

        val uri = createInvoicePdf(context, dir, lessons, "001", lightColorScheme(), Typography())

        val file = File(dir, "invoice-001.pdf")
        assertNotNull(uri)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
    }
}
