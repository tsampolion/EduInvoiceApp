package gr.tsambala.tutorbilling.ui.invoice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.tsambala.tutorbilling.data.database.LessonWithStudent
import gr.tsambala.tutorbilling.data.model.Student
import gr.tsambala.tutorbilling.domain.lesson.LessonUseCases
import gr.tsambala.tutorbilling.domain.student.StudentUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lessonUseCases: LessonUseCases,
    private val studentUseCases: StudentUseCases
) : ViewModel() {

    private val defaultStudentId: Long? =
        (savedStateHandle["id"] as? Long ?: -1L).takeIf { it != -1L }

    private val _startDate = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    private val _endDate = MutableStateFlow(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()))
    val startDate: StateFlow<LocalDate> = _startDate.asStateFlow()
    val endDate: StateFlow<LocalDate> = _endDate.asStateFlow()

    val students: StateFlow<List<Student>> =
        studentUseCases.getActiveStudents()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedStudentId = MutableStateFlow<Long?>(null)
    val selectedStudentId: StateFlow<Long?> = _selectedStudentId.asStateFlow()

    private val _lessons = MutableStateFlow<List<LessonWithStudent>>(emptyList())
    val lessons: StateFlow<List<LessonWithStudent>> = _lessons.asStateFlow()

    private val _selectedLessons = MutableStateFlow<Set<Long>>(emptySet())
    val selectedLessons: StateFlow<Set<Long>> = _selectedLessons.asStateFlow()

    init {
        defaultStudentId?.let { _selectedStudentId.value = it }
        viewModelScope.launch {
            combine(_startDate, _endDate, _selectedStudentId) { s, e, id -> Triple(s, e, id) }
                .collect { (start, end, id) ->
                    if (id == null) {
                        _lessons.value = emptyList()
                        _selectedLessons.value = emptySet()
                    } else {
                        lessonUseCases
                            .getLessonsWithStudentsByStudentAndDateRange(id, start.toString(), end.toString())
                            .collect { list ->
                                _lessons.value = list
                                _selectedLessons.value = emptySet()
                            }
                    }
                }
        }
    }

    fun updateStartDate(date: LocalDate) { _startDate.value = date }
    fun updateEndDate(date: LocalDate) { _endDate.value = date }
    fun selectStudent(id: Long) { _selectedStudentId.value = id }

    fun toggleLesson(id: Long) {
        _selectedLessons.value = _selectedLessons.value.toMutableSet().also { set ->
            if (set.contains(id)) set.remove(id) else set.add(id)
        }
    }

    fun selectAll() {
        _selectedLessons.value = _lessons.value.map { it.lesson.id }.toSet()
    }

    fun markAsPaid(ids: List<Long>) {
        viewModelScope.launch {
            lessonUseCases.updateLessonPaidStatus(ids, true)
            lessonUseCases.updateLessonInvoicedStatus(ids, true)
        }
    }
}
