package gr.tutorbilling.ui.revenue

import androidx.compose.runtime.Stable
import gr.tutorbilling.data.model.Student

@Stable
data class StudentDebt(
    val student: Student,
    val amount: Double
)
