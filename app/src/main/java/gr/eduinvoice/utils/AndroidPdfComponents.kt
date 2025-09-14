package gr.eduinvoice.utils

import android.graphics.*
import gr.eduinvoice.domain.billing.DomainPdfTheme
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.billing.BillingService
import gr.eduinvoice.domain.billing.calculateFeeWith

class AndroidPdfComponents(private val theme: DomainPdfTheme) {

    fun drawHeader(
        canvas: Canvas,
        width: Float,
        height: Float,
        tutorName: String,
        tutorAddress: String,
        invoiceNumber: String,
        date: String
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(0f, 0f, width, height)

        // Convert hex color to int
        val primaryColor = android.graphics.Color.parseColor(theme.colorScheme.primary)
        paint.shader = LinearGradient(
            0f, 0f, width, 0f,
            intArrayOf(primaryColor, primaryColor),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, theme.shapes.radiusLarge, theme.shapes.radiusLarge, paint)

        drawText(canvas, tutorName, 24f, 24f, theme.typography.headlineLarge, theme.colorScheme.onPrimary)
        drawText(canvas, tutorAddress, 24f, 54f, theme.typography.bodyMedium, theme.colorScheme.onPrimary)

        drawText(
            canvas,
            "Invoice #$invoiceNumber",
            width - 220f,
            30f,
            theme.typography.titleMedium,
            theme.colorScheme.onPrimary
        )
        drawText(canvas, date, width - 220f, 54f, theme.typography.bodyMedium, theme.colorScheme.onPrimary)
    }

    fun drawLessonsTable(
        canvas: Canvas,
        lessons: List<DomainLesson>,
        student: DomainStudent,
        startY: Float,
        width: Float
    ): Float {
        var y = startY
        val rowHeight = 24f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val alt = android.graphics.Color.parseColor(theme.colorScheme.surfaceVariant)

        lessons.forEachIndexed { index, lesson ->
            if (index % 2 == 0) {
                paint.color = alt
                canvas.drawRect(0f, y - rowHeight + 4f, width, y + 4f, paint)
            }

            val fee = lesson.calculateFeeWith(student)

            drawText(canvas, lesson.date ?: "", 24f, y, theme.typography.bodyLarge, theme.colorScheme.onSurface)
            drawText(canvas, "€%.2f".format(fee), width - 80f, y, theme.typography.bodyLarge, theme.colorScheme.onSurface)
            y += rowHeight
        }
        return y
    }

    private fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        style: gr.eduinvoice.domain.billing.DomainPdfTextStyle,
        color: String
    ) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.parseColor(color)
            this.textSize = style.fontSize
            this.typeface = Typeface.create(Typeface.DEFAULT, style.fontWeight)
        }
        canvas.drawText(text, x, y, p)
    }
}
