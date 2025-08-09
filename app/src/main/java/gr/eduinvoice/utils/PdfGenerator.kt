package gr.eduinvoice.utils

import android.content.Context
import gr.eduinvoice.testcompat.getFullName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import gr.eduinvoice.data.database.LessonWithStudent
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object PdfGenerator {
    private const val TAG = "PdfGenerator"

    // Units are PostScript points (1 pt = 1/72 inch)
    private const val PAGE_WIDTH_PT = 595 // A4 width
    private const val PAGE_HEIGHT_PT = 842 // A4 height
    private const val HEADER_HEIGHT_PT = 80f
    private const val LEFT_MARGIN_PT = 40f
    private const val CONTENT_START_Y_PT = 110
    private const val STUDENT_COLUMN_X_PT = 180f
    private const val AMOUNT_COLUMN_RIGHT_OFFSET_PT = 120f
    private const val TOTAL_Y_OFFSET_PT = 25f
    private const val LOGO_X_OFFSET_PT = 20f
    private const val LOGO_Y_OFFSET_PT = 10f
    private const val TUTOR_TEXT_X_PT = 100f
    private const val TUTOR_NAME_Y_PT = 40f
    private const val TUTOR_ADDRESS_Y_PT = 60f
    private const val INVOICE_NUMBER_X_OFFSET_PT = 200f

    fun createInvoicePdf(
        context: Context,
        directory: File,
        lessons: List<LessonWithStudent>,
        invoiceNumber: String,
        colorScheme: ColorScheme,
        typography: Typography,
        tutorName: String = "Tutor Name",
        tutorAddress: String = "123 Education Lane",
        currencySymbol: String = "€"
    ): Result<Uri> {
        val sanitizedInvoice = invoiceNumber.filter { it.isLetterOrDigit() }
        if (sanitizedInvoice.isEmpty()) {
            Log.e(TAG, "Invalid invoice number: $invoiceNumber")
            return Result.failure(IllegalArgumentException("Invoice number must be alphanumeric"))
        }

        val pdf = PdfDocument()
        val width = PAGE_WIDTH_PT
        val pageInfo = PdfDocument.PageInfo.Builder(width, PAGE_HEIGHT_PT, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorScheme.onBackground.toArgb()
            textSize = typography.titleMedium.fontSize.value * 2
        }
        val logo = drawInvoiceHeader(
            context,
            canvas,
            width,
            sanitizedInvoice,
            colorScheme,
            typography,
            infoPaint,
            tutorName,
            tutorAddress
        )

        val linePaint = Paint().apply {
            color = colorScheme.outline.toArgb()
            strokeWidth = 1f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorScheme.onBackground.toArgb()
            textSize = typography.bodyMedium.fontSize.value * 2
        }

        val (y, total) = drawInvoiceRows(
            canvas,
            lessons,
            CONTENT_START_Y_PT,
            width,
            infoPaint,
            textPaint,
            linePaint,
            currencySymbol
        )

        canvas.drawText(
            "Total: $currencySymbol%.2f".format(total),
            width - AMOUNT_COLUMN_RIGHT_OFFSET_PT,
            (y + TOTAL_Y_OFFSET_PT).toFloat(),
            infoPaint
        )

        return try {
            val uri = writePdfToFile(context, pdf, page, logo, directory, sanitizedInvoice)
            Result.success(uri)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write invoice PDF", e)
            Result.failure(IOException("Writing invoice PDF failed: ${e.message}", e))
        }
    }

    private fun drawInvoiceHeader(
        context: Context,
        canvas: Canvas,
        width: Int,
        invoiceNumber: String,
        colorScheme: ColorScheme,
        typography: Typography,
        infoPaint: Paint,
        tutorName: String,
        tutorAddress: String,
    ): Bitmap {
        val headerPaint = Paint().apply { color = colorScheme.primary.toArgb() }
        canvas.drawRect(0f, 0f, width.toFloat(), HEADER_HEIGHT_PT, headerPaint)
        val logo =
            BitmapFactory.decodeResource(context.resources, gr.eduinvoice.R.drawable.tutorbilling_logo)
        canvas.drawBitmap(logo, LOGO_X_OFFSET_PT, LOGO_Y_OFFSET_PT, null)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorScheme.onPrimary.toArgb()
            textSize = typography.titleLarge.fontSize.value * 2
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(tutorName, TUTOR_TEXT_X_PT, TUTOR_NAME_Y_PT, titlePaint)
        canvas.drawText(tutorAddress, TUTOR_TEXT_X_PT, TUTOR_ADDRESS_Y_PT, titlePaint)
        canvas.drawText(
            "Invoice #$invoiceNumber",
            width - INVOICE_NUMBER_X_OFFSET_PT,
            TUTOR_NAME_Y_PT,
            infoPaint
        )
        return logo
    }

    private fun drawInvoiceRows(
        canvas: Canvas,
        lessons: List<LessonWithStudent>,
        startY: Int,
        width: Int,
        infoPaint: Paint,
        textPaint: Paint,
        linePaint: Paint,
        currencySymbol: String
    ): Pair<Int, Double> {
        var y = startY
        canvas.drawText("Date", LEFT_MARGIN_PT, y.toFloat(), infoPaint)
        canvas.drawText("Student", STUDENT_COLUMN_X_PT, y.toFloat(), infoPaint)
        canvas.drawText("Amount", width - AMOUNT_COLUMN_RIGHT_OFFSET_PT, y.toFloat(), infoPaint)
        y += 10
        canvas.drawLine(LEFT_MARGIN_PT, y.toFloat(), width - LEFT_MARGIN_PT, y.toFloat(), linePaint)
        y += 20
        var total = 0.0
        lessons.forEach { item ->
            canvas.drawText(item.lesson.date, LEFT_MARGIN_PT, y.toFloat(), textPaint)
            canvas.drawText(item.student.getFullName(), STUDENT_COLUMN_X_PT, y.toFloat(), textPaint)
            val amount = item.calculateFee()
            total += amount
            canvas.drawText(
                "$currencySymbol%.2f".format(amount),
                width - AMOUNT_COLUMN_RIGHT_OFFSET_PT,
                y.toFloat(),
                textPaint
            )
            y += 20
        }
        y += 10
        canvas.drawLine(LEFT_MARGIN_PT, y.toFloat(), width - LEFT_MARGIN_PT, y.toFloat(), linePaint)
        return y to total
    }

    @Throws(IOException::class)
    private fun writePdfToFile(
        context: Context,
        pdf: PdfDocument,
        page: PdfDocument.Page,
        logo: Bitmap,
        directory: File,
        invoiceNumber: String
    ): Uri {
        try {
            pdf.finishPage(page)
            if (!directory.exists() && !directory.mkdirs()) {
                throw IOException("Could not create directory")
            }
            val file = File(directory, "invoice-$invoiceNumber.pdf")
            FileOutputStream(file).use { pdf.writeTo(it) }
            return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } finally {
            pdf.close()
            logo.recycle()
        }
    }
}

