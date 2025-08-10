package gr.eduinvoice.domain.billing

import java.io.File

/**
 * Interface for PDF generation in the domain layer
 * This allows the domain to define the contract without Android dependencies
 */
interface DomainPdfGenerator {
    fun generateInvoice(
        invoiceData: DomainInvoiceData,
        outputFile: File
    ): Result<String> // Returns file path as string for platform independence
}
