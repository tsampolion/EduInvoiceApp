package gr.eduinvoice.testinfrastructure

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

/**
 * Enhanced PDF test environment with proper lifecycle management
 */
class PdfTestEnvironment {
    
    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 50
        private const val FONT_SIZE = 12f
        
        fun validatePdfFile(file: File): Boolean {
            return file.exists() && file.length() > 0 && file.extension.lowercase() == "pdf"
        }
        
        fun getPdfFileSize(file: File): Long {
            return if (file.exists()) file.length() else 0L
        }
        
        fun createSimpleTestPdf(directory: File, filename: String = "test.pdf"): File {
            val pdf = PdfDocument()
            val page = createTestPdfPage(pdf, "Simple Test PDF Content")
            pdf.finishPage(page)
            
            val file = File(directory, filename)
            FileOutputStream(file).use { outputStream ->
                pdf.writeTo(outputStream)
            }
            pdf.close()
            return file
        }
        
        fun createTestPdfPage(document: PdfDocument, content: String = "Test PDF Content"): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            // Draw background
            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)
            
            // Draw content
            val textPaint = TextPaint().apply {
                color = android.graphics.Color.BLACK
                textSize = FONT_SIZE
                typeface = Typeface.DEFAULT
            }
            canvas.drawText(content, MARGIN.toFloat(), MARGIN.toFloat(), textPaint)
            
            return page
        }
    }
    
    private var testDirectory: File? = null
    private val createdFiles = mutableListOf<File>()
    
    fun initialize(): File {
        if (testDirectory == null) {
            testDirectory = TestInfrastructure.createTestDirectory()
        }
        return testDirectory!!
    }
    
    fun cleanup() {
        createdFiles.forEach { file ->
            if (file.exists()) {
                file.delete()
            }
        }
        createdFiles.clear()
        
        testDirectory?.let { dir ->
            if (dir.exists()) {
                TestInfrastructure.cleanupTestResources(dir)
            }
        }
        testDirectory = null
    }
    
    fun createTestInvoicePdf(
        lessons: List<LessonWithStudent>,
        invoiceNumber: String = "INV-${System.currentTimeMillis()}"
    ): File {
        val directory = testDirectory ?: initialize()
        val pdf = PdfDocument()
        
        try {
            val page = createInvoicePage(pdf, lessons, invoiceNumber)
            pdf.finishPage(page)
            
            val file = File(directory, "invoice-$invoiceNumber.pdf")
            FileOutputStream(file).use { outputStream ->
                pdf.writeTo(outputStream)
            }
            
            createdFiles.add(file)
            return file
        } finally {
            pdf.close()
        }
    }
    
    private fun createInvoicePage(
        document: PdfDocument,
        lessons: List<LessonWithStudent>,
        invoiceNumber: String
    ): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        // Draw background
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)
        
        val textPaint = TextPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = FONT_SIZE
            typeface = Typeface.DEFAULT
        }
        
        val titlePaint = TextPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = FONT_SIZE * 1.5f
            typeface = Typeface.DEFAULT_BOLD
        }
        
        var yPosition = MARGIN.toFloat()
        
        // Draw title
        canvas.drawText("Invoice $invoiceNumber", MARGIN.toFloat(), yPosition, titlePaint)
        yPosition += FONT_SIZE * 2
        
        // Draw date
        canvas.drawText("Date: ${LocalDate.now()}", MARGIN.toFloat(), yPosition, textPaint)
        yPosition += FONT_SIZE * 1.5f
        
        // Draw lessons
        canvas.drawText("Lessons:", MARGIN.toFloat(), yPosition, textPaint)
        yPosition += FONT_SIZE * 1.2f
        
        lessons.forEach { lessonWithStudent ->
            val lesson = lessonWithStudent.lesson
            val student = lessonWithStudent.student
            
            val lessonText = "${lesson.date} - ${student.name} - ${lesson.durationMinutes}min"
            canvas.drawText(lessonText, MARGIN + 20f, yPosition, textPaint)
            yPosition += FONT_SIZE * 1.1f
        }
        
        return page
    }
}

/**
 * Test PDF Generator for enhanced testing
 */
class TestPdfGenerator {
    
    fun createInvoicePdf(
        lessons: List<LessonWithStudent>,
        invoiceNumber: String = "INV-${System.currentTimeMillis()}"
    ): File {
        val pdf = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas
            
            // Draw background
            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, 595f, 842f, paint)
            
            // Draw content
            val textPaint = TextPaint().apply {
                color = android.graphics.Color.BLACK
                textSize = 12f
                typeface = Typeface.DEFAULT
            }
            
            var yPosition = 50f
            canvas.drawText("Invoice $invoiceNumber", 50f, yPosition, textPaint)
            yPosition += 30f
            
            lessons.forEach { lessonWithStudent ->
                val lesson = lessonWithStudent.lesson
                val student = lessonWithStudent.student
                val text = "${lesson.date} - ${student.name} - ${lesson.durationMinutes}min"
                canvas.drawText(text, 70f, yPosition, textPaint)
                yPosition += 20f
            }
            
            pdf.finishPage(page)
            
            val file = File.createTempFile("test-invoice-", ".pdf")
            FileOutputStream(file).use { outputStream ->
                pdf.writeTo(outputStream)
            }
            return file
        } finally {
            pdf.close()
        }
    }
} 