package gr.tsambala.tutorbilling.ui.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.tsambala.tutorbilling.data.model.StudentWithEarnings
import gr.tsambala.tutorbilling.domain.lesson.LessonUseCases
import gr.tsambala.tutorbilling.domain.student.StudentUseCases
import gr.tsambala.tutorbilling.utils.EarningsCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedStudentsViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases,
    private val lessonUseCases: LessonUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentsUiState())
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    init {
        loadStudentsWithEarnings()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSortOrder() {
        _sortAscending.value = !_sortAscending.value
    }

    private fun loadStudentsWithEarnings() {
        viewModelScope.launch {
            combine(
                studentUseCases.getArchivedStudents(),
                lessonUseCases.getAllLessons(),
                searchQuery,
                sortAscending
            ) { students, lessons, query, ascending ->
                var filtered = if (query.isBlank()) students else students.filter {
                    it.name.contains(query, true)
                }
                filtered = if (ascending) filtered.sortedBy { it.name }
                else filtered.sortedByDescending { it.name }

                filtered.map { student ->
                    val (weekEarnings, monthEarnings) = EarningsCalculator.calculate(student, lessons)
                    StudentWithEarnings(
                        student = student,
                        weekEarnings = weekEarnings,
                        monthEarnings = monthEarnings
                    )
                }
            }.collect { studentsWithEarnings ->
                _uiState.update {
                    it.copy(
                        students = studentsWithEarnings,
                        searchQuery = _searchQuery.value
                    )
                }
            }
        }
    }

    fun restoreStudent(studentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            studentUseCases.restoreStudent(studentId)
        }
    }
}
