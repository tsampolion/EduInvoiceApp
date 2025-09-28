package gr.eduinvoice.invoice

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import gr.eduinvoice.domain.billing.DomainInvoiceData
import gr.eduinvoice.domain.billing.DomainPdfGenerator
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfGenerator: DomainPdfGenerator
) {

    data class ResultData(
        val uri: Uri,
        val file: File,
        val invoiceNumber: String,
        val absolutePath: String
    )

    fun generate(
        student: DomainStudent,
        lessons: List<DomainLesson>,
        invoiceNumber: String? = null,
        invoiceDate: LocalDate? = null
    ): Result<ResultData> {
        return try {
            val data = if (invoiceNumber == null && invoiceDate == null) {
                DomainInvoiceData(student = student, lessons = lessons)
            } else {
                val num = invoiceNumber ?: ("INV-" + System.currentTimeMillis())
                val date = invoiceDate ?: LocalDate.now()
                DomainInvoiceData(student = student, lessons = lessons, invoiceDate = date, invoiceNumber = num)
            }

            val safeNumber = data.invoiceNumber.replace(Regex("[^A-Za-z0-9_-]"), "_")
            val outDir = File(context.filesDir, "invoices").apply { mkdirs() }
            val outFile = File(outDir, "$safeNumber.pdf")

            val gen = pdfGenerator.generateInvoice(data, outFile)
            gen.fold(
                onSuccess = {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        outFile
                    )
                    Result.success(ResultData(uri = uri, file = outFile, invoiceNumber = data.invoiceNumber, absolutePath = outFile.absolutePath))
                },
                onFailure = { e -> Result.failure(e) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

