package gr.eduinvoice.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.domain.billing.calculateFeeWith
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.data.user.CurrentUserProvider
import kotlinx.coroutines.flow.first
import gr.eduinvoice.ui.revenue.StudentDebt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class RevenueViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases,
    private val lessonUseCases: LessonUseCases,
    private val currentUserProvider: CurrentUserProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevenueUiState())
    val uiState: StateFlow<RevenueUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                studentUseCases.getActiveStudents(),
                lessonUseCases.getAllLessons()
            ) { students, lessons ->
                val studentMap = students.associateBy { it.id }

                val today = LocalDate.now()
                val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val monthStart = today.withDayOfMonth(1)
                val monthEnd = today.withDayOfMonth(today.lengthOfMonth())

                val dayTotal = lessons.filter { it.date == today.toString() }
                    .sumOf { lesson ->
                        val student = studentMap[lesson.studentId] ?: return@sumOf 0.0
                        lesson.calculateFeeWith(student)
                    }

                val weekTotal = lessons.filter { lesson ->
                    val date = LocalDate.parse(lesson.date)
                    !date.isBefore(weekStart) && !date.isAfter(weekEnd)
                }.sumOf { lesson ->
                    val student = studentMap[lesson.studentId] ?: return@sumOf 0.0
                    lesson.calculateFeeWith(student)
                }

                val monthTotal = lessons.filter { lesson ->
                    val date = LocalDate.parse(lesson.date)
                    !date.isBefore(monthStart) && !date.isAfter(monthEnd)
                }.sumOf { lesson ->
                    val student = studentMap[lesson.studentId] ?: return@sumOf 0.0
                    lesson.calculateFeeWith(student)
                }

                val (paidTotal, unpaidTotal) = lessons.filter { lesson ->
                    val date = LocalDate.parse(lesson.date)
                    !date.isBefore(monthStart) && !date.isAfter(monthEnd)
                }.partition { it.isPaid }.let { (paid, unpaid) ->
                    val paidSum = paid.sumOf { l ->
                        val s = studentMap[l.studentId] ?: return@sumOf 0.0
                        l.calculateFeeWith(s)
                    }
                    val unpaidSum = unpaid.sumOf { l ->
                        val s = studentMap[l.studentId] ?: return@sumOf 0.0
                        l.calculateFeeWith(s)
                    }
                    paidSum to unpaidSum
                }

                val debts = lessons
                    .filter { !it.isPaid }
                    .groupBy { it.studentId }
                    .mapNotNull { (id, lns) ->
                        val student = studentMap[id] ?: return@mapNotNull null
                        val total = lns.sumOf { it.calculateFeeWith(student) }
                        if (total > 0) StudentDebt(student, total) else null
                    }
                    .sortedBy { it.student.name }

                RevenueUiState(
                    dailyRevenue = dayTotal,
                    weeklyRevenue = weekTotal,
                    monthlyRevenue = monthTotal,
                    monthlyPaid = paidTotal,
                    monthlyUnpaid = unpaidTotal,
                    debts = debts
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun markLessonsPaid(studentId: Long) {
        viewModelScope.launch {
            val ids = lessonUseCases.getStudentLessons(studentId)
                .first()
                .filter { !it.isPaid }
                .map { it.id }
            if (ids.isNotEmpty()) {
                val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: 0L
                lessonUseCases.updateLessonPaidStatus(ids, true, uid)
            }
        }
    }
}

data class RevenueUiState(
    val dailyRevenue: Double = 0.0,
    val weeklyRevenue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val monthlyPaid: Double = 0.0,
    val monthlyUnpaid: Double = 0.0,
    val debts: List<StudentDebt> = emptyList()
)
