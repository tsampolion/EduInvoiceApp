package gr.eduinvoice.ui.invoice

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.analytics.PerformanceTraces
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.user.CurrentUserProvider
import gr.eduinvoice.invoice.InvoiceService
import gr.eduinvoice.ui.model.UiInvoiceLesson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
@kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class InvoiceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lessonUseCases: LessonUseCases,
    private val studentUseCases: StudentUseCases,
    private val currentUserProvider: CurrentUserProvider,
    private val invoiceService: InvoiceService
) : ViewModel() {

    private val defaultStudentId: Long? =
        ((savedStateHandle["id"] as? Long ?: -1L)
            .takeIf { it != -1L }
            ?: (savedStateHandle["studentId"] as? Long)?.takeIf { it > 0L })

    private val _startDate = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    private val _endDate = MutableStateFlow(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()))
    val startDate: StateFlow<LocalDate> = _startDate.asStateFlow()
    val endDate: StateFlow<LocalDate> = _endDate.asStateFlow()

    val students: StateFlow<List<DomainStudent>> =
        currentUserProvider.loggedInUserId
            .filterNotNull()
            .flatMapLatest { uid -> studentUseCases.getActiveStudents(uid) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedStudentId = MutableStateFlow<Long?>(null)
    val selectedStudentId: StateFlow<Long?> = _selectedStudentId.asStateFlow()

    private val _lessons = MutableStateFlow<List<UiInvoiceLesson>>(emptyList())
    val lessons: StateFlow<List<UiInvoiceLesson>> = _lessons.asStateFlow()

    private val _selectedLessons = MutableStateFlow<Set<Long>>(emptySet())
    val selectedLessons: StateFlow<Set<Long>> = _selectedLessons.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()
    private val _generatedInvoiceUri = MutableStateFlow<Uri?>(null)
    val generatedInvoiceUri: StateFlow<Uri?> = _generatedInvoiceUri.asStateFlow()
    private val _generatedInvoiceFile = MutableStateFlow<File?>(null)
    val generatedInvoiceFile: StateFlow<File?> = _generatedInvoiceFile.asStateFlow()

    fun dismissError() { _errorMessage.value = null }

    init {
        defaultStudentId?.let { _selectedStudentId.value = it }
        viewModelScope.launch {
            combine(_startDate, _endDate, _selectedStudentId, students) { start, end, id, studentList ->
                if (id == null) {
                    _lessons.value = emptyList()
                    _selectedLessons.value = emptySet()
                } else {
                    lessonUseCases
                        .getLessonsWithStudentsByStudentAndDateRange(id, start.toString(), end.toString())
                        .collect { lessonList ->
                            val uiLessons = PerformanceTraces.trace("invoice_lessons_map") {
                                val studentMap = studentList.associateBy { it.id }
                                lessonList.mapNotNull { lesson ->
                                    studentMap[lesson.studentId]?.let { student ->
                                        UiInvoiceLesson(lesson, student)
                                    }
                                }
                            }
                            _lessons.value = uiLessons
                            _selectedLessons.value = emptySet()
                        }
                }
            }.collect { }
        }
    }

    fun updateStartDate(date: LocalDate) {
        if (date <= _endDate.value) {
            _startDate.value = date
        } else {
            _errorMessage.value = "Start date must be on or before end date"
        }
    }

    fun updateEndDate(date: LocalDate) {
        if (date >= _startDate.value) {
            _endDate.value = date
        } else {
            _errorMessage.value = "End date must be on or after start date"
        }
    }
    fun selectStudent(id: Long) { _selectedStudentId.value = id }

    fun toggleLesson(id: Long) {
        _selectedLessons.value = _selectedLessons.value.toMutableSet().also { set ->
            if (set.contains(id)) set.remove(id) else set.add(id)
        }
    }

    fun selectAll() {
        _selectedLessons.value = _lessons.value.map { it.id }.toSet()
    }

    fun createInvoiceAndMark(ids: List<Long>, invoiceNumber: String, invoiceDate: String, notes: String?) {
        viewModelScope.launch {
            val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: 0L
            val studentId = _selectedStudentId.value ?: return@launch
            try {
                lessonUseCases.createInvoiceMasterAndMarkLessons(studentId, invoiceNumber, invoiceDate, notes, ids, uid)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to finalize invoice"
            }
        }
    }

    fun setGenerating(generating: Boolean) {
        _isGenerating.value = generating
    }

    fun generateAndFinalize(optionalInvoiceNumber: String? = null) {
        viewModelScope.launch {
            val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: return@launch
            val studentId = _selectedStudentId.value ?: return@launch
            val selectedIds = _selectedLessons.value
            val selectedLessons = _lessons.value.filter { selectedIds.contains(it.id) }.map { it.lesson }
            if (selectedLessons.isEmpty()) {
                _errorMessage.value = "Select at least one lesson"
                return@launch
            }
            val student = students.value.firstOrNull { it.id == studentId } ?: return@launch
            try {
                _isGenerating.value = true
                val result = invoiceService.generate(student, selectedLessons, optionalInvoiceNumber, null)
                result.fold(
                    onSuccess = { data ->
                        _generatedInvoiceUri.value = data.uri
                        _generatedInvoiceFile.value = data.file
                        createInvoiceAndMark(selectedLessons.map { it.id }, data.invoiceNumber, LocalDate.now().toString(), null)
                    },
                    onFailure = { e ->
                        _errorMessage.value = e.message ?: "Failed to create invoice"
                    }
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun clearGenerated() {
        _generatedInvoiceUri.value = null
        _generatedInvoiceFile.value = null
    }
}
