package gr.tsambala.tutorbilling.ui.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.model.Student
import gr.tsambala.tutorbilling.data.model.RateTypes
import gr.tsambala.tutorbilling.domain.lesson.LessonUseCases
import gr.tsambala.tutorbilling.domain.student.StudentUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lessonUseCases: LessonUseCases,
    private val studentUseCases: StudentUseCases
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val studentId: Long? = savedStateHandle.get<Long>("studentId")
    private val lessonId: Long? = savedStateHandle.get<Long>("lessonId")

    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    // Navigation callback
    private var onNavigateBack: (() -> Unit)? = null

    fun setNavigationCallback(callback: () -> Unit) {
        onNavigateBack = callback
    }

    init {
        loadStudentInfo()
        if (lessonId != null && lessonId != 0L) {
            loadLesson()
        } else {
            // Set default values for new lesson
            _uiState.update {
                it.copy(
                    date = LocalDate.now().format(dateFormatter),
                    startTime = LocalTime.now().withSecond(0).withNano(0).format(timeFormatter)
                )
            }
        }
    }

    private fun loadStudentInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            studentUseCases.getActiveStudents().collect { list ->
                val selectedId = studentId?.takeIf { it != 0L } ?: _uiState.value.selectedStudentId
                val selectedStudent = list.firstOrNull { it.id == selectedId }
                _uiState.update { state ->
                    state.copy(
                        availableStudents = list,
                        selectedStudentId = selectedStudent?.id,
                        studentName = selectedStudent?.name ?: state.studentName,
                        studentRate = selectedStudent?.rate ?: state.studentRate,
                        rateType = selectedStudent?.rateType ?: state.rateType
                    )
                }
            }
        }
    }

    private fun loadLesson() {
        viewModelScope.launch(Dispatchers.IO) {
            lessonId?.takeIf { it != 0L }?.let { id ->
                lessonUseCases.getLessonById(id).collect { lesson ->
                    lesson?.let { l ->
                        _uiState.update { state ->
                            state.copy(
                                date = LocalDate.parse(l.date).format(dateFormatter),
                                startTime = l.startTime,
                                durationMinutes = l.durationMinutes.toString(),
                                notes = l.notes ?: "",
                                isEditMode = false,
                                isPaid = l.isPaid
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateDate(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    fun updateStartTime(time: String) {
        _uiState.update { it.copy(startTime = time) }
    }

    fun updateDuration(duration: String) {
        val digits = duration.filter { it.isDigit() }
        val number = digits.toIntOrNull()?.coerceIn(0, 180) ?: 0
        val sanitized = number.takeIf { it > 0 }?.toString() ?: ""
        _uiState.update { it.copy(durationMinutes = sanitized) }
    }

    fun updateSelectedStudent(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            studentUseCases.getStudentById(id).collect { student ->
                student?.let { s ->
                    _uiState.update {
                        it.copy(
                            selectedStudentId = id,
                            studentName = s.name,
                            studentRate = s.rate,
                            rateType = s.rateType
                        )
                    }
                }
            }
        }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun updatePaid(paid: Boolean) {
        _uiState.update { it.copy(isPaid = paid) }
    }

    private fun isValidDate(value: String): Boolean = try {
        LocalDate.parse(value, dateFormatter)
        true
    } catch (_: Exception) { false }

    private fun isValidTime(value: String): Boolean = try {
        LocalTime.parse(value, timeFormatter)
        true
    } catch (_: Exception) { false }

    fun isFormValid(): Boolean {
        val state = _uiState.value
        val hasStudent = state.selectedStudentId != null
        val validDateTime = isValidDate(state.date) && isValidTime(state.startTime)
        return if (state.rateType == RateTypes.PER_LESSON) {
            hasStudent && validDateTime
        } else {
            val duration = state.durationMinutes.toIntOrNull() ?: 0
            hasStudent && validDateTime && duration in 60..180
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun saveLesson() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            var duration = state.durationMinutes.toIntOrNull() ?: 0
            if (state.rateType == RateTypes.PER_LESSON) {
                duration = 60
            } else {
                if (duration <= 0) duration = 60
                if (duration < 60) duration = 60
                if (duration > 180) duration = 180
                _uiState.update { it.copy(durationMinutes = duration.toString()) }
            }
            if (!isFormValid()) return@launch

            val sId = state.selectedStudentId
            sId?.let {
                if (lessonId == null || lessonId == 0L) {
                    val lesson = Lesson(
                        studentId = it,
                        date = LocalDate.parse(state.date, dateFormatter).toString(),
                        startTime = state.startTime,
                        durationMinutes = duration,
                        notes = state.notes.ifBlank { null },
                        isPaid = state.isPaid
                    )
                    lessonUseCases.insertLesson(lesson)
                } else {
                    lessonId?.let { lId ->
                        val lesson = Lesson(
                            id = lId,
                            studentId = it,
                            date = LocalDate.parse(state.date, dateFormatter).toString(),
                            startTime = state.startTime,
                            durationMinutes = duration,
                            notes = state.notes.ifBlank { null },
                            isPaid = state.isPaid
                        )
                        lessonUseCases.updateLesson(lesson)
                    }
                }
            }

            _uiState.update { it.copy(isEditMode = false) }

            // Navigate back on main thread
            withContext(Dispatchers.Main) {
                onNavigateBack?.invoke()
            }
        }
    }

    fun deleteLesson() {
        viewModelScope.launch(Dispatchers.IO) {
            lessonId?.takeIf { it != 0L }?.let { id ->
                lessonUseCases.deleteLesson(id)

                // Navigate back on main thread
                withContext(Dispatchers.Main) {
                    onNavigateBack?.invoke()
                }
            }
        }
    }

    fun calculateFee(): Double {
        val state = _uiState.value
        val duration = state.durationMinutes.toIntOrNull() ?: 0
        return if (state.rateType == RateTypes.PER_LESSON) {
            state.studentRate
        } else {
            (duration.coerceAtLeast(60) / 60.0) * state.studentRate
        }
    }
}

data class LessonUiState(
    val date: String = "",
    val startTime: String = "",
    val durationMinutes: String = "",
    val notes: String = "",
    val studentName: String = "",
    val studentRate: Double = 0.0,
    val rateType: String = RateTypes.HOURLY,
    val availableStudents: List<Student> = emptyList(),
    val selectedStudentId: Long? = null,
    val isEditMode: Boolean = true,
    val isPaid: Boolean = false
)
