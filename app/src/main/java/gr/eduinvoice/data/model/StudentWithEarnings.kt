package gr.eduinvoice.data.model

/**
 * Helper data class representing a student along with their earnings.
 */
data class StudentWithEarnings(
    val student: Student,
    val weekEarnings: Double,
    val monthEarnings: Double
)
