package gr.eduinvoice.ui.revenue

import androidx.compose.runtime.Stable
import gr.eduinvoice.domain.model.DomainStudent

@Stable
data class StudentDebt(
    val student: DomainStudent,
    val amount: Double
)
