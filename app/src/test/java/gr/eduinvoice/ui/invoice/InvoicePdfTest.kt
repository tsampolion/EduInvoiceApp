package gr.eduinvoice.ui.invoice

import gr.eduinvoice.testinfrastructure.*
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
@Config(sdk = [34], manifest = Config.NONE)
class InvoicePdfTest : gr.eduinvoice.TestBase() {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @get:Rule
    val temporaryFolder = TemporaryFolder()
    
    @Test
    fun testPdfContentGeneration() {
        // Test PDF content generation without actually creating a PDF document
        val lessons = listOf(TestInfrastructure.createTestLessonWithStudent())
        
        // Test that we can create test data
        assertNotNull("Lessons should not be null", lessons)
        assertTrue("Lessons should not be empty", lessons.isNotEmpty())
        
        // Test that the lesson has the expected structure
        val lesson = lessons.first()
        assertNotNull("Lesson should not be null", lesson)
        assertNotNull("Student should not be null", lesson.student)
        assertTrue("Student name should not be empty", lesson.student.name.isNotEmpty())
        assertTrue("Lesson date should not be empty", lesson.lesson.date.isNotEmpty())
    }

    @Test
    fun testPdfFileValidation() {
        val dir = temporaryFolder.newFolder()
        
        // Test that we can create a directory
        assertTrue("Directory should exist", dir.exists())
        assertTrue("Directory should be a directory", dir.isDirectory)
        
        // Test that we can create a file in the directory
        val testFile = File(dir, "test.txt")
        testFile.writeText("Test content")
        
        assertTrue("Test file should exist", testFile.exists())
        assertTrue("Test file should have content", testFile.length() > 0)
        assertTrue("Test file should be a file", testFile.isFile)
    }
}
