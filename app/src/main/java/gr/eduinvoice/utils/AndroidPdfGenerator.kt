package gr.eduinvoice.utils

import android.content.Context
import android.graphics.pdf.PdfDocument
import gr.eduinvoice.domain.billing.DomainInvoiceData
import gr.eduinvoice.domain.billing.DomainPdfGenerator
import gr.eduinvoice.domain.billing.DomainPdfTheme
import java.io.File
import java.io.FileOutputStream
import gr.eduinvoice.analytics.PerformanceTraces
import gr.eduinvoice.utils.MemoryMonitor

/**
 * Android implementation of DomainPdfGenerator
 * This bridges the domain interface with Android-specific PDF generation
 */
class AndroidPdfGenerator(
    private val context: Context,
    private val theme: DomainPdfTheme
) : DomainPdfGenerator {

    override fun generateInvoice(
        invoiceData: DomainInvoiceData,
        outputFile: File
    ): Result<String> {
        return try {
            val pdf = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas

            val components = AndroidPdfComponents(theme)

            // Header
            components.drawHeader(
                canvas,
                595f,
                100f,
                "${invoiceData.student.name} ${invoiceData.student.surname}",
                invoiceData.student.className ?: "",
                invoiceData.invoiceNumber,
                invoiceData.invoiceDate.toString()
            )

            // Table
            val endY = components.drawLessonsTable(
                canvas,
                invoiceData.lessons,
                invoiceData.student,
                140f,
                595f
            )

            pdf.finishPage(page)
            // Guard memory during IO and large buffer write
            MemoryMonitor(context).guardOperation(thresholdPct = 92) {
                FileOutputStream(outputFile).use { out ->
                    PerformanceTraces.trace("pdf_write") {
                        pdf.writeTo(out)
                    }
                }
            }
            pdf.close()

            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
