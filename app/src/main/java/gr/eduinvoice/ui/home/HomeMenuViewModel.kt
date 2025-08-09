package gr.eduinvoice.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import gr.eduinvoice.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import gr.eduinvoice.domain.billing.calculateFeeWith
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.data.user.CurrentUserProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeMenuViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases,
    private val lessonUseCases: LessonUseCases,
    private val currentUserProvider: CurrentUserProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeMenuUiState())
    val uiState: StateFlow<HomeMenuUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            currentUserProvider.loggedInUserId.filterNotNull().flatMapLatest { uid ->
                if (BuildConfig.DEBUG) {
                    Log.d("HomeMenuViewModel", "Fetching data for user $uid")
                }
                combine(
                    studentUseCases.getActiveStudents(uid),
                    lessonUseCases.getAllLessons(uid)
                ) { students, lessons ->
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            "HomeMenuViewModel",
                            "Loaded ${students.size} students and ${lessons.size} lessons"
                        )
                    }
                    val classCount = students
                        .map { it.className }
                        .filterNot { it.isBlank() || it.equals("unknown", true) }
                        .distinct()
                        .size

                val today = LocalDate.now()
                val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val monthStart = today.withDayOfMonth(1)
                val monthEnd = today.withDayOfMonth(today.lengthOfMonth())

                val weekTotal = lessons.filter { lesson ->
                    val date = LocalDate.parse(lesson.date)
                    !date.isBefore(weekStart) && !date.isAfter(weekEnd)
                }.sumOf { lesson ->
                    val student = students.firstOrNull { it.id == lesson.studentId }
                    student?.let { lesson.calculateFeeWith(it) } ?: 0.0
                }

                val monthTotal = lessons.filter { lesson ->
                    val date = LocalDate.parse(lesson.date)
                    !date.isBefore(monthStart) && !date.isAfter(monthEnd)
                }.sumOf { lesson ->
                    val student = students.firstOrNull { it.id == lesson.studentId }
                    student?.let { lesson.calculateFeeWith(it) } ?: 0.0
                }
                    HomeMenuUiState(
                        studentCount = students.size,
                        classCount = classCount,
                        lessonCount = lessons.size,
                        weekRevenue = weekTotal,
                        monthRevenue = monthTotal
                    )
                }
            }.collect { state ->
                if (BuildConfig.DEBUG) {
                    Log.d("HomeMenuViewModel", "New UI state -> $state")
                }
                _uiState.value = state
            }
        }
    }
}

data class HomeMenuUiState(
    val studentCount: Int = 0,
    val classCount: Int = 0,
    val lessonCount: Int = 0,
    val weekRevenue: Double = 0.0,
    val monthRevenue: Double = 0.0
)
