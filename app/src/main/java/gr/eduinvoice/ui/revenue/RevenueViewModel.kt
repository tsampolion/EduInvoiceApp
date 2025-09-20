package gr.eduinvoice.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.analytics.ReportingRepository
import gr.eduinvoice.data.database.EarningsByClassRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RevenueViewModel @Inject constructor(
    private val reportingRepository: ReportingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevenueUiState())
    val uiState: StateFlow<RevenueUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            // Example: current month
            val start = java.time.LocalDate.now().withDayOfMonth(1).toString()
            val end = java.time.LocalDate.now().withDayOfMonth(java.time.LocalDate.now().lengthOfMonth()).toString()
            val userId = 1L

            reportingRepository.earningsByClass(start, end, userId).collect { rows ->
                _uiState.update { it.copy(earningsByClass = rows) }
            }
        }
    }
}

data class RevenueUiState(
    val dailyRevenue: Double = 0.0,
    val weeklyRevenue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val monthlyUnpaid: Double = 0.0,
    val monthlyPaid: Double = 0.0,
    val debts: List<StudentDebt> = emptyList(),
    val earningsByClass: List<EarningsByClassRow> = emptyList()
)
