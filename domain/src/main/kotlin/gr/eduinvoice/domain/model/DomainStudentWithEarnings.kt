package gr.eduinvoice.domain.model

/**
 * Helper data class representing a student along with their earnings.
 */
data class DomainStudentWithEarnings(
    val student: DomainStudent,
    val weekEarnings: Double,
    val monthEarnings: Double
)
