package gr.eduinvoice.testinfrastructure

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.RateTypes
import gr.eduinvoice.utils.PdfGenerator
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class PdfTestEnvironment {
    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 50
        private const val LINE_HEIGHT = 20
        private const val FONT_SIZE = 12f
        
        fun createTestPdfDocument(): PdfDocument {
            return PdfDocument()
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
        
        fun validatePdfFile(file: File): Boolean {
            return file.exists() && file.length() > 0 && file.extension.lowercase() == "pdf"
        }
        
        fun getPdfFileSize(file: File): Long {
            return if (file.exists()) file.length() else 0L
        }
        
        fun createSimpleTestPdf(directory: File, filename: String = "test.pdf"): File {
            val pdf = PdfDocument()
            try {
                val page = createTestPdfPage(pdf, "Simple Test PDF Content")
                pdf.finishPage(page)
                
                val file = File(directory, filename)
                FileOutputStream(file).use { outputStream ->
                    pdf.writeTo(outputStream)
                }
                return file
            } finally {
                pdf.close()
            }
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
        yPosition += LINE_HEIGHT * 2
        
        // Draw date
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        canvas.drawText("Date: $currentDate", MARGIN.toFloat(), yPosition, textPaint)
        yPosition += LINE_HEIGHT * 2
        
        // Draw lessons
        canvas.drawText("Lessons:", MARGIN.toFloat(), yPosition, textPaint)
        yPosition += LINE_HEIGHT
        
        lessons.forEach { lessonWithStudent ->
            val lesson = lessonWithStudent.lesson
            val student = lessonWithStudent.student
            
            val lessonText = "${lesson.date} - ${student.name} - ${lesson.startTime} (${lesson.durationMinutes} min)"
            canvas.drawText(lessonText, MARGIN + 20f, yPosition, textPaint)
            yPosition += LINE_HEIGHT
            
            // Calculate and display fee
            val fee = calculateLessonFee(lesson, student)
            val feeText = "Fee: €${String.format("%.2f", fee)}"
            canvas.drawText(feeText, MARGIN + 40f, yPosition, textPaint)
            yPosition += LINE_HEIGHT * 1.5f
        }
        
        // Draw total
        val total = lessons.sumOf { lessonWithStudent ->
            calculateLessonFee(lessonWithStudent.lesson, lessonWithStudent.student)
        }
        val totalText = "Total: €${String.format("%.2f", total)}"
        canvas.drawText(totalText, MARGIN.toFloat(), yPosition, titlePaint)
        
        return page
    }
    
    /**
     * Calculate lesson fee based on student rate type
     */
    private fun calculateLessonFee(lesson: Lesson, student: Student): Double {
        return when (student.rateType) {
            RateTypes.PER_LESSON -> student.rate
            RateTypes.HOURLY -> {
                val hours = lesson.durationMinutes / 60.0
                student.rate * hours
            }
            else -> student.rate
        }
    }
    
    /**
     * Create a test PDF using the existing PdfGenerator (for compatibility)
     */
    fun createPdfUsingGenerator(
        context: Context,
        lessons: List<LessonWithStudent>,
        invoiceNumber: String = "INV-${System.currentTimeMillis()}"
    ): Uri {
        val directory = testDirectory ?: initialize()
        val generator = TestPdfGenerator()
        return generator.createInvoicePdf(context, directory, lessons, invoiceNumber)
    }
}

/**
 * Enhanced TestPdfGenerator with proper resource management
 */
class TestPdfGenerator {
    
    fun createInvoicePdf(
        context: Context,
        directory: File,
        lessons: List<LessonWithStudent>,
        invoiceNumber: String
    ): Uri {
        val pdf = PdfDocument()
        
        try {
            // Create page
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas
            
            // Draw content
            drawInvoiceContent(canvas, lessons, invoiceNumber)
            
            pdf.finishPage(page)
            
            // Ensure directory exists
            if (!directory.exists() && !directory.mkdirs()) {
                throw IOException("Could not create directory: ${directory.absolutePath}")
            }
            
            // Write to file
            val file = File(directory, "invoice-$invoiceNumber.pdf")
            FileOutputStream(file).use { outputStream ->
                pdf.writeTo(outputStream)
            }
            
            // Return URI
            return Uri.fromFile(file)
            
        } finally {
            // Ensure document is closed
            pdf.close()
        }
    }
    
    private fun drawInvoiceContent(
        canvas: Canvas,
        lessons: List<LessonWithStudent>,
        invoiceNumber: String
    ) {
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
        
        // Draw title
        canvas.drawText("Invoice $invoiceNumber", 50f, yPosition, textPaint)
        yPosition += 30f
        
        // Draw lessons
        lessons.forEach { lessonWithStudent ->
            val lesson = lessonWithStudent.lesson
            val student = lessonWithStudent.student
            
            val lessonText = "${lesson.date} - ${student.name} - ${lesson.startTime}"
            canvas.drawText(lessonText, 50f, yPosition, textPaint)
            yPosition += 20f
        }
    }
} 