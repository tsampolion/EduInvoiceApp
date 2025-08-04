package gr.eduinvoice.ui.invoice

import android.content.Context
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import gr.eduinvoice.testinfrastructure.*
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.RateTypes
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner
import java.io.File

/**
 * Enhanced PDF tests using the new test infrastructure
 */
@RunWith(BouncyCastleTestRunner::class)
class InvoicePdfEnhancedTest : gr.eduinvoice.TestBase() {
    
    private lateinit var pdfTestEnvironment: PdfTestEnvironment
    private lateinit var testContext: Context
    private lateinit var testDataManager: TestDataManager
    
    @Before
    fun setUp() {
        pdfTestEnvironment = PdfTestEnvironment()
        testContext = TestInfrastructure.getTestContext()
        testDataManager = TestDataManager()
        
        // Initialize test environment
        pdfTestEnvironment.initialize()
    }
    
    @After
    fun tearDown() {
        pdfTestEnvironment.cleanup()
        testDataManager.clearTestData()
    }
    
    @Test
    fun testPdfTestEnvironmentInitialization() {
        // Test that the PDF test environment initializes correctly
        val testDir = pdfTestEnvironment.initialize()
        
        assertTrue("Test directory should exist", testDir.exists())
        assertTrue("Test directory should be a directory", testDir.isDirectory)
        assertTrue("Test directory should be writable", testDir.canWrite())
    }
    
    @Test
    fun testSimplePdfCreation() {
        // Test basic PDF creation without complex content
        val testDir = pdfTestEnvironment.initialize()
        val pdfFile = PdfTestEnvironment.createSimpleTestPdf(testDir, "simple-test.pdf")
        
        assertTrue("PDF file should exist", pdfFile.exists())
        assertTrue("PDF file should have content", pdfFile.length() > 0)
        assertTrue("PDF file should be valid", PdfTestEnvironment.validatePdfFile(pdfFile))
    }
    
    @Test
    fun testInvoicePdfCreation() {
        // Test invoice PDF creation with lesson data
        val lessons = PdfContentTest.createTestInvoiceData()
        val pdfFile = pdfTestEnvironment.createTestInvoicePdf(lessons, "INV-TEST-001")
        
        assertTrue("Invoice PDF should exist", pdfFile.exists())
        assertTrue("Invoice PDF should have content", pdfFile.length() > 0)
        assertTrue("Invoice PDF should be valid", PdfTestEnvironment.validatePdfFile(pdfFile))
        
        // Verify file size is reasonable (not empty, not too large)
        val fileSize = PdfTestEnvironment.getPdfFileSize(pdfFile)
        assertTrue("PDF file size should be reasonable", fileSize > 100 && fileSize < 100000)
    }
    
    @Test
    fun testPdfContentGeneration() {
        // Test PDF content generation without actual PDF creation
        val lessons = PdfContentTest.createTestInvoiceData()
        val content = PdfContentTest.generateInvoiceContent(lessons, "INV-TEST-002")
        
        // Validate content structure
        assertTrue("Content should be valid", PdfContentTest.validateInvoiceContent(content))
        
        // Validate content contains expected elements
        assertTrue("Content should contain invoice header", content.contains("Invoice INV-TEST-002"))
        assertTrue("Content should contain date", content.contains("Date:"))
        assertTrue("Content should contain lessons section", content.contains("Lessons:"))
        assertTrue("Content should contain total", content.contains("Total: €"))
        
        // Validate lesson count
        val lessonCount = PdfContentTest.extractLessonCount(content)
        assertEquals("Should have correct number of lessons", 2, lessonCount)
        
        // Validate total amount
        val total = PdfContentTest.extractTotalAmount(content)
        assertNotNull("Total amount should be extractable", total)
        assertTrue("Total amount should be positive", total!! > 0)
    }
    
    @Test
    fun testFeeCalculationLogic() {
        // Test fee calculation logic independently
        val fees = PdfContentTest.testFeeCalculation()
        
        // Validate hourly rate calculations
        val hourly60min = fees["hourly_60min"]!!
        val hourly90min = fees["hourly_90min"]!!
        assertEquals("60-minute hourly lesson should cost €25.00", 25.0, hourly60min, 0.01)
        assertEquals("90-minute hourly lesson should cost €37.50", 37.5, hourly90min, 0.01)
        
        // Validate per-lesson rate calculations
        val perLesson60min = fees["per_lesson_60min"]!!
        val perLesson90min = fees["per_lesson_90min"]!!
        assertEquals("60-minute per-lesson should cost €30.00", 30.0, perLesson60min, 0.01)
        assertEquals("90-minute per-lesson should cost €30.00", 30.0, perLesson90min, 0.01)
    }
    
    @Test
    fun testInvoiceContentValidation() {
        // Test invoice content validation
        val validator = InvoiceContentValidator()
        val lessons = PdfContentTest.createTestInvoiceData()
        val content = PdfContentTest.generateInvoiceContent(lessons, "INV-TEST-003")
        
        // Test structure validation
        val structureResult = validator.validateInvoiceStructure(content)
        assertTrue("Invoice structure should be valid: ${structureResult.errors}", structureResult.isValid)
        
        // Test lesson fees validation
        val feesResult = validator.validateLessonFees(content, lessons)
        assertTrue("Lesson fees should be valid: ${feesResult.errors}", feesResult.isValid)
        
        // Test total amount validation
        val totalResult = validator.validateTotalAmount(content, lessons)
        assertTrue("Total amount should be valid: ${totalResult.errors}", totalResult.isValid)
    }
    
    @Test
    fun testTestPdfGenerator() {
        // Test the enhanced TestPdfGenerator
        val lessons = PdfContentTest.createTestInvoiceData()
        val uri = pdfTestEnvironment.createPdfUsingGenerator(testContext, lessons, "INV-TEST-004")
        
        assertNotNull("URI should not be null", uri)
        assertTrue("URI should be a file URI", uri.scheme == "file")
        
        val file = File(uri.path!!)
        assertTrue("Generated PDF file should exist", file.exists())
        assertTrue("Generated PDF file should have content", file.length() > 0)
    }
    
    @Test
    fun testPdfWithDifferentRateTypes() {
        // Test PDF generation with different rate types
        val hourlyStudent = TestInfrastructure.createTestStudent(
            name = "Hourly Student",
            rate = 20.0,
            rateType = RateTypes.HOURLY
        )
        
        val perLessonStudent = TestInfrastructure.createTestStudent(
            name = "Per Lesson Student",
            rate = 25.0,
            rateType = RateTypes.PER_LESSON
        )
        
        val hourlyLesson = TestInfrastructure.createTestLesson(
            studentId = 1L,
            durationMinutes = 120 // 2 hours
        )
        
        val perLessonLesson = TestInfrastructure.createTestLesson(
            studentId = 2L,
            durationMinutes = 90 // 1.5 hours
        )
        
        val lessons = listOf(
            LessonWithStudent(hourlyLesson, hourlyStudent),
            LessonWithStudent(perLessonLesson, perLessonStudent)
        )
        
        val content = PdfContentTest.generateInvoiceContent(lessons, "INV-RATE-TEST")
        
        // Validate that hourly rate is calculated correctly (20 * 2 = 40)
        assertTrue("Content should contain hourly fee", content.contains("Fee: €40.00"))
        
        // Validate that per-lesson rate is fixed (25)
        assertTrue("Content should contain per-lesson fee", content.contains("Fee: €25.00"))
        
        // Validate total (40 + 25 = 65)
        assertTrue("Content should contain correct total", content.contains("Total: €65.00"))
    }
    
    @Test
    fun testPdfFileManagement() {
        // Test that PDF files are properly managed and cleaned up
        val testDir = pdfTestEnvironment.initialize()
        
        // Create multiple PDF files
        val pdf1 = pdfTestEnvironment.createTestInvoicePdf(
            PdfContentTest.createTestInvoiceData(), "INV-001"
        )
        val pdf2 = pdfTestEnvironment.createTestInvoicePdf(
            PdfContentTest.createTestInvoiceData(), "INV-002"
        )
        
        assertTrue("First PDF should exist", pdf1.exists())
        assertTrue("Second PDF should exist", pdf2.exists())
        
        // Clean up
        pdfTestEnvironment.cleanup()
        
        // Files should be cleaned up
        assertFalse("First PDF should be cleaned up", pdf1.exists())
        assertFalse("Second PDF should be cleaned up", pdf2.exists())
    }
} 