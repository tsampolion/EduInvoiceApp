package gr.eduinvoice.utils

import android.content.Context
import android.graphics.pdf.PdfDocument
import gr.eduinvoice.ui.model.UiInvoiceLesson
import gr.eduinvoice.utils.DomainInvoiceData
import java.io.File
import java.io.FileOutputStream

class ModernPdfGenerator(
    private val context: Context,
    private val theme: ModernPdfTheme
) {
    fun generateInvoice(
        invoiceData: DomainInvoiceData,
        outputFile: File
    ): Result<java.net.URI> {
        return try {
            val pdf = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas

            val components = ModernPdfComponents(theme)
            // Header
            components.drawHeader(
                canvas,
                595f,
                100f,
                "${invoiceData.student.name} ${invoiceData.student.surname}",
                invoiceData.student.className,
                invoiceData.invoiceNumber,
                invoiceData.invoiceDate.toString()
            )

            // Table
            val endY = components.drawLessonsTable(
                canvas,
                invoiceData.lessons,
                140f,
                595f
            )

            pdf.finishPage(page)
            FileOutputStream(outputFile).use { out -> pdf.writeTo(out) }
            pdf.close()
            Result.success(outputFile.toURI())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
