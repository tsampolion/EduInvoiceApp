package gr.eduinvoice.ui.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.model.DomainRateTypes
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.group.GroupUseCases
import gr.eduinvoice.domain.user.CurrentUserProvider
import android.database.sqlite.SQLiteException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lessonUseCases: LessonUseCases,
    private val studentUseCases: StudentUseCases,
    private val groupUseCases: GroupUseCases,
    private val currentUserProvider: CurrentUserProvider
) : ViewModel() {

    companion object {
        const val MIN_DURATION = 60
        const val MAX_DURATION = 180
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val studentId: Long? = savedStateHandle.get<Long>("studentId")
    private val lessonId: Long? = savedStateHandle.get<Long>("lessonId")
    private val initialGroupId: Long? = savedStateHandle.get<Long>("groupId")

    private fun initialState(): LessonUiState {
        val nowDate = LocalDate.now().format(dateFormatter)
        val nowTime = LocalTime.now().withSecond(0).withNano(0).format(timeFormatter)
        return if (lessonId == null || lessonId == 0L) {
            LessonUiState(date = nowDate, startTime = nowTime)
        } else {
            LessonUiState()
        }
    }

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    private val groupMembers = mutableMapOf<Long, List<DomainStudent>>()

    // Navigation callback
    private var onNavigateBack: (() -> Unit)? = null

    fun setNavigationCallback(callback: () -> Unit) {
        onNavigateBack = callback
    }

    fun getGroupMembers(groupId: Long): List<DomainStudent> = groupMembers[groupId] ?: emptyList()

    init {
        loadStudentInfo()
        loadGroups()
        if (lessonId != null && lessonId != 0L) {
            loadLesson()
        }
        initialGroupId?.takeIf { it != 0L }?.let { gid ->
            _uiState.update { it.copy(isGroupLesson = true) }
            updateSelectedGroup(gid)
        }
    }

    private fun loadStudentInfo() {
        viewModelScope.launch {
            currentUserProvider.loggedInUserId
                .filterNotNull()
                .flatMapLatest { uid -> studentUseCases.getActiveStudents(uid) }
                .collect { list ->
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

    private fun loadGroups() {
        viewModelScope.launch {
            currentUserProvider.loggedInUserId
                .filterNotNull()
                .flatMapLatest { uid -> groupUseCases.getAllGroups(uid) }
                .collect { groups ->
                    _uiState.update { it.copy(availableGroups = groups) }
                }
        }
    }

    private fun loadLesson() {
        viewModelScope.launch {
            lessonId?.takeIf { it != 0L }?.let { id ->
                val userId = currentUserProvider.loggedInUserId.first() ?: 0L
                lessonUseCases.getLessonById(id, userId).collect { lesson ->
                    lesson?.let { l ->
                        _uiState.update { state ->
                            state.copy(
                                date = LocalDate.parse(l.date).format(dateFormatter),
                                startTime = l.startTime,
                                durationMinutes = l.durationMinutes.toString(),
                                notes = l.notes ?: "",
                                isEditMode = false,
                                isPaid = l.isPaid,
                                selectedGroupId = l.groupId,
                                isGroupLesson = l.groupId != null
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
        val number = digits.toIntOrNull()?.coerceIn(0, MAX_DURATION) ?: 0
        val sanitized = number.takeIf { it > 0 }?.toString() ?: ""
        _uiState.update { it.copy(durationMinutes = sanitized) }
    }

    fun updateSelectedStudent(id: Long) {
        viewModelScope.launch {
            val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: 0L
            studentUseCases.getStudentById(id, uid).collect { student ->
                student?.let { s ->
                    _uiState.update {
                        it.copy(
                            selectedStudentId = id,
                            studentName = s.name,
                            studentRate = s.rate,
                            rateType = s.rateType,
                            selectedGroupId = null,
                            isGroupLesson = false
                        )
                    }
                }
            }
        }
    }

    fun updateSelectedGroup(id: Long) {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            groupUseCases.getGroupStudents(id, userId).collect { students ->
                groupMembers[id] = students
                val absentMap = students.associate { it.id to false }
                _uiState.update { it.copy(selectedGroupId = id, selectedStudentId = null, isGroupLesson = true, markAbsences = false, absentStudents = absentMap) }
            }
        }
    }

    fun toggleGroupLesson(value: Boolean) {
        _uiState.update {
            it.copy(
                isGroupLesson = value,
                selectedGroupId = if (value) it.selectedGroupId else null,
                selectedStudentId = if (value) null else it.selectedStudentId,
                markAbsences = false,
                absentStudents = emptyMap()
            )
        }
    }

    fun toggleMarkAbsences(value: Boolean) {
        _uiState.update { state ->
            val currentGroupId = state.selectedGroupId
            if (value && currentGroupId != null) {
                val members = groupMembers[currentGroupId] ?: emptyList()
                val map = members.associate { it.id to false }
                state.copy(markAbsences = true, absentStudents = map)
            } else {
                state.copy(markAbsences = false, absentStudents = emptyMap())
            }
        }
    }

    fun toggleStudentAbsent(studentId: Long) {
        _uiState.update { state ->
            val current = state.absentStudents[studentId] ?: false
            state.copy(absentStudents = state.absentStudents + (studentId to !current))
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
        val hasStudent = if (state.isGroupLesson) state.selectedGroupId != null else state.selectedStudentId != null
        val validDateTime = isValidDate(state.date) && isValidTime(state.startTime)
        return if (state.rateType == DomainRateTypes.PER_LESSON) {
            hasStudent && validDateTime
        } else {
            val duration = state.durationMinutes.toIntOrNull() ?: 0
            hasStudent && validDateTime && duration in MIN_DURATION..MAX_DURATION
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun saveLesson() {
        viewModelScope.launch {
            val state = _uiState.value
            var duration = state.durationMinutes.toIntOrNull() ?: 0
            if (state.rateType == DomainRateTypes.PER_LESSON) {
                duration = MIN_DURATION
            } else {
                if (duration <= 0) duration = MIN_DURATION
                if (duration < MIN_DURATION) duration = MIN_DURATION
                if (duration > MAX_DURATION) duration = MAX_DURATION
                _uiState.update { it.copy(durationMinutes = duration.toString()) }
            }
            if (!isFormValid()) return@launch

            try {
                val sId = state.selectedStudentId
                if (state.selectedGroupId != null) {
                    val lesson = DomainLesson(
                        studentId = 0,
                        date = LocalDate.parse(state.date, dateFormatter).toString(),
                        startTime = state.startTime,
                        durationMinutes = duration,
                        notes = state.notes.ifBlank { null },
                        isPaid = state.isPaid
                    )
                    val userId = currentUserProvider.loggedInUserId.first() ?: 0L
                    val base = lesson.copy(groupId = state.selectedGroupId)
                    val members = groupMembers[state.selectedGroupId!!] ?: emptyList()
                    val presentMembers = if (state.markAbsences) {
                        members.filter { m -> state.absentStudents[m.id] != true }
                    } else members
                    if (presentMembers.isNotEmpty()) {
                        // We call addGroupLesson which expands to all current members. To respect absences,
                        // fall back to per-student adds when absences are marked.
                        if (state.markAbsences) {
                            val absentIds = members.filter { m -> state.absentStudents[m.id] == true }.map { it.id }
                            lessonUseCases.addGroupLessonWithAbsences(state.selectedGroupId!!, base, absentIds, userId)
                        } else {
                            lessonUseCases.addGroupLesson(state.selectedGroupId!!, base, userId)
                        }
                    }
                } else if (sId != null) {
                    if (lessonId == null || lessonId == 0L) {
                        val lesson = DomainLesson(
                            studentId = sId,
                            date = LocalDate.parse(state.date, dateFormatter).toString(),
                            startTime = state.startTime,
                            durationMinutes = duration,
                            notes = state.notes.ifBlank { null },
                            isPaid = state.isPaid
                        )
                        lessonUseCases.addLesson(lesson)
                    } else {
                        val lesson = DomainLesson(
                            id = lessonId,
                            studentId = sId,
                            date = LocalDate.parse(state.date, dateFormatter).toString(),
                            startTime = state.startTime,
                            durationMinutes = duration,
                            notes = state.notes.ifBlank { null },
                            isPaid = state.isPaid
                        )
                        lessonUseCases.updateLesson(lesson)
                    }
                }

                _uiState.update { it.copy(isEditMode = false) }

                // Navigate back on main thread
                withContext(Dispatchers.Main) {
                    onNavigateBack?.invoke()
                }
            } catch (e: SQLiteException) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(errorMessage = "Database error while saving lesson: ${e.message}") }
                }
            } catch (e: IllegalArgumentException) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(errorMessage = "Invalid lesson data: ${e.message}") }
                }
            }
        }
    }

    fun deleteLesson() {
        viewModelScope.launch {
            lessonId?.takeIf { it != 0L }?.let { id ->
                val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: 0L
                lessonUseCases.deleteLesson(id, uid)

                // Navigate back on main thread
                withContext(Dispatchers.Main) {
                    onNavigateBack?.invoke()
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun calculateFee(): Double {
        val state = _uiState.value
        val duration = state.durationMinutes.toIntOrNull() ?: 0
        return if (state.selectedGroupId != null) {
            val studentsAll = groupMembers[state.selectedGroupId!!] ?: emptyList()
            val students = if (state.markAbsences) studentsAll.filter { st -> state.absentStudents[st.id] != true } else studentsAll
            students.sumOf { student ->
                if (state.rateType == DomainRateTypes.PER_LESSON) {
                    student.rate
                } else {
                    (duration.coerceAtLeast(MIN_DURATION) / 60.0) * student.rate
                }
            }
        } else if (state.rateType == DomainRateTypes.PER_LESSON) {
            state.studentRate
        } else {
            (duration.coerceAtLeast(MIN_DURATION) / 60.0) * state.studentRate
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
    val rateType: String = DomainRateTypes.HOURLY,
    val availableStudents: List<DomainStudent> = emptyList(),
    val selectedStudentId: Long? = null,
    val availableGroups: List<gr.eduinvoice.domain.model.DomainStudentGroup> = emptyList(),
    val selectedGroupId: Long? = null,
    val isGroupLesson: Boolean = false,
    val isEditMode: Boolean = true,
    val isPaid: Boolean = false,
    val errorMessage: String? = null,
    val markAbsences: Boolean = false,
    val absentStudents: Map<Long, Boolean> = emptyMap()
)
