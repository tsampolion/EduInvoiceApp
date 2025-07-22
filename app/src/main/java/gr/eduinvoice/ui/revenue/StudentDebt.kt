package gr.eduinvoice.ui.revenue

import androidx.compose.runtime.Stable
import gr.eduinvoice.data.model.Student

@Stable
data class StudentDebt(
    val student: Student,
    val amount: Double
)
