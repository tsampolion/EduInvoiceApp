package gr.eduinvoice.domain.model

data class DomainInvoiceMaster(
    val id: Long = 0,
    val ownerId: Long = 0,
    val studentId: Long,
    val invoiceNumber: String,
    val invoiceDate: String,
    val notes: String? = null,
    val isArchived: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)


