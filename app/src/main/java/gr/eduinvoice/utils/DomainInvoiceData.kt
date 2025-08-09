package gr.eduinvoice.utils

import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.ui.model.UiInvoiceLesson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Data class for invoice information using Domain types
 */
data class DomainInvoiceData(
    val student: DomainStudent,
    val lessons: List<UiInvoiceLesson>,
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
