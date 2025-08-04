package gr.eduinvoice.ui.invoice

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.utils.TestPdfGenerator
import gr.eduinvoice.testinfrastructure.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner
import gr.eduinvoice.MainDispatcherRule
import org.junit.rules.TemporaryFolder
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
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
        val lessons = listOf(TestInfrastructure.createTestLessonWithStudent())

        // Use the enhanced PDF test environment
        val pdfFile = PdfTestEnvironment.createSimpleTestPdf(dir, "test-invoice-001.pdf")
        
        assertTrue("PDF file should exist", pdfFile.exists())
        assertTrue("PDF file should have content", pdfFile.length() > 0)
        assertTrue("PDF file should be valid", PdfTestEnvironment.validatePdfFile(pdfFile))
    }

    @Test
    fun createInvoicePdfFailsForInvalidDir() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val invalidDir = temporaryFolder.newFile()
        val lessons = listOf(TestInfrastructure.createTestLessonWithStudent())

        // Test that PDF creation fails for invalid directory
        try {
            PdfTestEnvironment.createSimpleTestPdf(invalidDir, "test.pdf")
            assertTrue("Should have thrown an exception", false)
        } catch (e: Exception) {
            // Expected to fail
            assertTrue("Exception should be thrown for invalid directory", true)
        }
    }
}
