package gr.tutorbilling.ui.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.tutorbilling.data.model.Student
import gr.tutorbilling.data.model.StudentWithEarnings
import gr.tutorbilling.domain.student.StudentUseCases
import gr.tutorbilling.domain.lesson.LessonUseCases
import gr.tutorbilling.utils.EarningsCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentsViewModel @Inject constructor(
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
                studentUseCases.getActiveStudents(),
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

    fun deleteStudent(studentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            studentUseCases.softDeleteStudent(studentId)
        }
    }
}

data class StudentsUiState(
    val students: List<StudentWithEarnings> = emptyList(),
    val searchQuery: String = ""
)
