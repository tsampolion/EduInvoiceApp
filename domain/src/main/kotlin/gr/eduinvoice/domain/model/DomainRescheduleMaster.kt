package gr.eduinvoice.domain.model

data class DomainRescheduleMaster(
    val id: Long = 0,
    val ownerId: Long = 0,
    val title: String? = null,
    val newDate: String,
    val newStartTime: String,
    val newDurationMinutes: Int,
    val notes: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)


