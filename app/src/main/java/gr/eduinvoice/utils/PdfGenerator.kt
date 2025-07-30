package gr.eduinvoice.utils

import android.content.Context
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
        val pdf = PdfDocument()
        val width = 595
        val pageInfo = PdfDocument.PageInfo.Builder(width, 842, 1).create()
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
            invoiceNumber,
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
            110,
            width,
            infoPaint,
            textPaint,
            linePaint,
            currencySymbol
        )

        canvas.drawText(
            "Total: $currencySymbol%.2f".format(total),
            width - 120f,
            (y + 25).toFloat(),
            infoPaint
        )

        return try {
            val uri = writePdfToFile(context, pdf, page, logo, directory, invoiceNumber)
            Result.success(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate invoice", e)
            Result.failure(e)
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
        canvas.drawRect(0f, 0f, width.toFloat(), 80f, headerPaint)
        val logo = BitmapFactory.decodeResource(context.resources, gr.eduinvoice.R.drawable.tutorbilling_logo)
        canvas.drawBitmap(logo, 20f, 10f, null)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorScheme.onPrimary.toArgb()
            textSize = typography.titleLarge.fontSize.value * 2
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(tutorName, 100f, 40f, titlePaint)
        canvas.drawText(tutorAddress, 100f, 60f, titlePaint)
        canvas.drawText("Invoice #$invoiceNumber", width - 200f, 40f, infoPaint)
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
        canvas.drawText("Date", 40f, y.toFloat(), infoPaint)
        canvas.drawText("Student", 180f, y.toFloat(), infoPaint)
        canvas.drawText("Amount", width - 120f, y.toFloat(), infoPaint)
        y += 10
        canvas.drawLine(40f, y.toFloat(), width - 40f, y.toFloat(), linePaint)
        y += 20
        var total = 0.0
        lessons.forEach { item ->
            canvas.drawText(item.lesson.date, 40f, y.toFloat(), textPaint)
            canvas.drawText(item.student.getFullName(), 180f, y.toFloat(), textPaint)
            val amount = item.calculateFee()
            total += amount
            canvas.drawText("$currencySymbol%.2f".format(amount), width - 120f, y.toFloat(), textPaint)
            y += 20
        }
        y += 10
        canvas.drawLine(40f, y.toFloat(), width - 40f, y.toFloat(), linePaint)
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

