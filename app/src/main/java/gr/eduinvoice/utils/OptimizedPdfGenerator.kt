package gr.eduinvoice.utils

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import gr.eduinvoice.ui.model.UiLessonWithStudent
import gr.eduinvoice.domain.model.DomainStudent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Result of PDF generation operation
 */
sealed class PdfResult {
    object Success : PdfResult()
    data class Error(val exception: Throwable) : PdfResult()
    data class Progress(val current: Int, val total: Int) : PdfResult()
}

/**
 * Optimized PDF generator for large invoices with background processing
 */
class OptimizedPdfGenerator(private val context: Context) {
    private val _generationStatus = MutableStateFlow<PdfResult>(PdfResult.Success)
    val generationStatus: StateFlow<PdfResult> = _generationStatus.asStateFlow()

    private var currentJob: Job? = null

    /**
     * Generate PDF asynchronously for large invoices using BackgroundProcessor
     * @param invoiceData The invoice data to generate PDF for
     * @param outputFile The output file to save the PDF
     * @return Job that can be used to cancel the generation
     */
    fun generatePdfAsync(
        invoiceData: InvoiceData,
        outputFile: File
    ): Job? {
        // Cancel any existing generation
        cancelGeneration()

        return GlobalBackgroundProcessor.executeTaskWithProgress(
            task = { progressCallback ->
                _generationStatus.value = PdfResult.Progress(0, 100)

                // Generate PDF in chunks to avoid memory issues
                val pdfDocument = createPdfDocument()

                // Process invoice data in chunks
                val totalLessons = invoiceData.lessons.size
                val chunkSize = 50 // Process 50 lessons at a time

                for (i in 0 until totalLessons step chunkSize) {
                    val endIndex = minOf(i + chunkSize, totalLessons)
                    val chunk = invoiceData.lessons.subList(i, endIndex)

                    // Process chunk
                    processLessonsChunk(pdfDocument, chunk, invoiceData.student)

                    // Update progress
                    val progress = ((i + chunkSize) * 100f / totalLessons).coerceAtMost(90f)
                    _generationStatus.value = PdfResult.Progress(progress.toInt(), 100)
                    progressCallback(progress / 100f)
                }

                // Finalize PDF
                finalizePdf(pdfDocument, outputFile)
            },
            onProgress = { progress ->
                // Progress is already handled in the task
            },
            onComplete = {
                _generationStatus.value = PdfResult.Success
            },
            onError = { exception ->
                _generationStatus.value = PdfResult.Error(exception)
            }
        )?.also { job ->
            currentJob = job
        }
    }

    /**
     * Cancel current PDF generation
     */
    fun cancelGeneration() {
        currentJob?.cancel()
        currentJob = null
        _generationStatus.value = PdfResult.Success
    }

    /**
     * Create PDF document with optimized settings
     */
    private fun createPdfDocument(): PdfDocument {
        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        return PrintedPdfDocument(context, printAttributes)
    }

    /**
     * Process a chunk of lessons for PDF generation
     */
    private suspend fun processLessonsChunk(
        pdfDocument: PdfDocument,
        lessons: List<UiLessonWithStudent>,
        student: DomainStudent
    ) {
        withContext(Dispatchers.Default) {
            // Process lessons in parallel for better performance
            lessons.map { lesson ->
                async {
                    createLessonPage(pdfDocument, lesson, student)
                }
            }.awaitAll()
        }
    }

    /**
     * Create a page for a single lesson
     */
    private fun createLessonPage(
        pdfDocument: PdfDocument,
        lesson: UiLessonWithStudent,
        student: DomainStudent
    ): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        // Create optimized page content
        val canvas = page.canvas
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        var yPosition = 50f

        // Draw student information
        canvas.drawText("Student: ${student.name} ${student.surname}", 50f, yPosition, paint)
        yPosition += 20f

        // Draw lesson information
        canvas.drawText("Date: ${lesson.lesson.date}", 50f, yPosition, paint)
        yPosition += 20f
        canvas.drawText("Time: ${lesson.lesson.startTime}", 50f, yPosition, paint)
        yPosition += 20f
        canvas.drawText("Duration: ${lesson.lesson.durationMinutes} minutes", 50f, yPosition, paint)
        yPosition += 20f

        // Draw notes if available
        lesson.lesson.notes?.let { notes ->
            if (notes.isNotBlank()) {
                canvas.drawText("Notes: $notes", 50f, yPosition, paint)
                yPosition += 20f
            }
        }

        // Draw payment status
        val paymentStatus = if (lesson.lesson.isPaid) "Paid" else "Unpaid"
        canvas.drawText("Status: $paymentStatus", 50f, yPosition, paint)

        pdfDocument.finishPage(page)
        return page
    }

    /**
     * Finalize and save the PDF document
     */
    private suspend fun finalizePdf(pdfDocument: PdfDocument, outputFile: File) {
        withContext(Dispatchers.IO) {
            try {
                val fileOutputStream = FileOutputStream(outputFile)
                pdfDocument.writeTo(fileOutputStream)
                fileOutputStream.close()
                pdfDocument.close()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    /**
     * Generate summary PDF for multiple lessons
     */
    fun generateSummaryPdfAsync(
        lessons: List<UiLessonWithStudent>,
        student: DomainStudent,
        outputFile: File
    ): Job? {
        return generatePdfAsync(
            InvoiceData(student, lessons),
            outputFile
        )
    }

    /**
     * Check if PDF generation is in progress
     */
    fun isGenerating(): Boolean {
        return currentJob?.isActive == true
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        cancelGeneration()
    }
}

/**
 * Data class for invoice information
 */
data class InvoiceData(
    val student: DomainStudent,
    val lessons: List<UiLessonWithStudent>,
    val invoiceDate: LocalDate = LocalDate.now(),
    val invoiceNumber: String = generateInvoiceNumber()
) {
    companion object {
        private var invoiceCounter = 0

        private fun generateInvoiceNumber(): String {
            invoiceCounter++
            val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            return "INV-$date-$invoiceCounter"
        }
    }
}

/**
 * Global optimized PDF generator instance
 */
object GlobalPdfGenerator {
    private var generator: OptimizedPdfGenerator? = null

    fun initialize(context: Context) {
        generator = OptimizedPdfGenerator(context)
    }

    fun generatePdfAsync(
        invoiceData: InvoiceData,
        outputFile: File
    ): Job? {
        return generator?.generatePdfAsync(invoiceData, outputFile)
    }

    fun generateSummaryPdfAsync(
        lessons: List<UiLessonWithStudent>,
        student: DomainStudent,
        outputFile: File
    ): Job? {
        return generator?.generateSummaryPdfAsync(lessons, student, outputFile)
    }

    fun cancelGeneration() {
        generator?.cancelGeneration()
    }

    fun isGenerating(): Boolean {
        return generator?.isGenerating() ?: false
    }

    fun getGenerationStatus(): StateFlow<PdfResult>? {
        return generator?.generationStatus
    }

    fun cleanup() {
        generator?.cleanup()
        generator = null
    }
}
