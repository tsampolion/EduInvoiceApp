package gr.eduinvoice.ui.model

import gr.eduinvoice.domain.model.DomainStudent

data class UiStudentWithEarnings(
    val student: DomainStudent,
    val weekEarnings: Double,
    val monthEarnings: Double
)
