package gr.eduinvoice.ui.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.domain.model.DomainStudentGroup
import gr.eduinvoice.domain.model.DomainRateTypes
import gr.eduinvoice.domain.group.GroupUseCases
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.user.CurrentUserProvider
import gr.eduinvoice.utils.ClassOptions
import gr.eduinvoice.domain.lesson.LessonUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupUseCases: GroupUseCases,
    private val studentUseCases: StudentUseCases,
    private val lessonUseCases: LessonUseCases,
    savedStateHandle: SavedStateHandle,
    private val currentUserProvider: CurrentUserProvider
) : ViewModel() {
    val groupId: Long = savedStateHandle.get<Long>("groupId") ?: 0L

    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    private var originalStudents: Set<Long> = emptySet()
    private var originalClassName: String = ""
    private var originalRateType: String = DomainRateTypes.HOURLY
    private var originalRate: Double = 0.0
    val lessonHistory: StateFlow<List<gr.eduinvoice.domain.model.DomainGroupLessonMaster>> =
        currentUserProvider.loggedInUserId.filterNotNull().flatMapLatest { uid ->
            if (groupId != 0L) lessonUseCases.getGroupLessonMasters(groupId, uid) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            val students = studentUseCases.getActiveStudents(userId).first()
            val selectedIds = if (groupId != 0L) {
                groupUseCases.getGroupStudents(groupId, userId).first().map { it.id }.toSet()
            } else emptySet()
            originalStudents = selectedIds
            val selections = students.map {
                StudentSelection(
                    id = it.id,
                    name = it.name,
                    surname = it.surname,
                    selected = it.id in selectedIds,
                    className = it.className,
                    rateType = it.rateType,
                    rate = it.rate
                )
            }
            val group = if (groupId != 0L) groupUseCases.getGroupById(groupId, userId).first() else null
            val existingClass = group?.className ?: ""
            originalClassName = existingClass
            originalRateType = group?.rateType ?: DomainRateTypes.HOURLY
            originalRate = group?.rate ?: 0.0
            val (selectedClass, customClass) = if (
                existingClass.isNotBlank() && existingClass !in ClassOptions.DEFAULT
            ) {
                "Custom" to existingClass
            } else {
                existingClass to ""
            }
            _uiState.value = GroupUiState(
                name = group?.name ?: "",
                students = selections,
                selectedClass = if (selectedClass.isNotBlank()) selectedClass else "",
                customClass = customClass,
                rate = if (group != null && group.rate > 0.0) group.rate.toString() else "",
                rateType = group?.rateType ?: DomainRateTypes.HOURLY
            )
        }
    }

    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun toggleStudent(id: Long) {
        _uiState.value = _uiState.value.copy(
            students = _uiState.value.students.map {
                if (it.id == id) it.copy(selected = !it.selected) else it
            }
        )
    }

    fun updateSelectedClass(value: String) {
        _uiState.value = _uiState.value.copy(selectedClass = value)
    }

    fun updateCustomClass(value: String) {
        _uiState.value = _uiState.value.copy(customClass = value)
    }

    fun updateRateType(value: String) {
        _uiState.value = _uiState.value.copy(rateType = value)
    }

    fun updateRate(value: String) {
        _uiState.value = _uiState.value.copy(rate = value)
    }

    fun saveGroup(
        overrideClass: Boolean = false,
        overrideBilling: Boolean = false,
        overrideTargets: Set<Long>? = null
    ) {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            val state = _uiState.value
            val className = if (state.selectedClass == "Custom") state.customClass else state.selectedClass
            val rate = state.rate.toDoubleOrNull() ?: 0.0
            val group = DomainStudentGroup(
                id = groupId,
                ownerId = userId,
                name = state.name,
                className = className,
                rate = rate,
                rateType = state.rateType
            )
            val id =
                if (groupId == 0L) {
                    groupUseCases.insertGroup(group)
                } else {
                    groupUseCases.updateGroup(group)
                    groupId
                }

            val selected = state.students.filter { it.selected }.map { it.id }.toSet()
            val toAdd = selected - originalStudents
            val toRemove = originalStudents - selected
            toAdd.forEach { groupUseCases.addStudentToGroup(id, it, userId) }
            toRemove.forEach { groupUseCases.removeStudentFromGroup(id, it, userId) }

            if (overrideClass || overrideBilling) {
                val targets: Set<Long> = overrideTargets ?: selected
                targets.forEach { studentId ->
                    val current = studentUseCases.getStudentById(studentId, userId).first()
                    current?.let { s ->
                        val updated = s.copy(
                            className = if (overrideClass) className else s.className,
                            rateType = if (overrideBilling) state.rateType else s.rateType,
                            rate = if (overrideBilling) rate else s.rate
                        )
                        studentUseCases.updateStudent(updated)
                    }
                }
            }
        }
    }

    fun isClassChanged(): Boolean {
        val state = _uiState.value
        val className = if (state.selectedClass == "Custom") state.customClass else state.selectedClass
        return groupId != 0L && className != originalClassName
    }

    fun isBillingChanged(): Boolean {
        val state = _uiState.value
        val rate = state.rate.toDoubleOrNull() ?: 0.0
        return groupId != 0L && (state.rateType != originalRateType || rate != originalRate)
    }

    fun getToAddSelections(): List<StudentSelection> {
        val selected = _uiState.value.students.filter { it.selected }.map { it.id }.toSet()
        val toAdd = selected - originalStudents
        return _uiState.value.students.filter { it.id in toAdd }
    }

    fun getToAddIds(): Set<Long> = getToAddSelections().map { it.id }.toSet()

    fun hasToAddClassMismatch(targetClass: String): Boolean =
        getToAddSelections().any { it.className != targetClass }

    fun hasToAddBillingMismatch(targetRateType: String): Boolean =
        getToAddSelections().any { it.rateType != targetRateType }

    fun deleteGroup() {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            val state = _uiState.value
            val group = DomainStudentGroup(id = groupId, ownerId = userId, name = state.name)
            groupUseCases.deleteGroup(group, userId)
        }
    }

    fun archiveGroup() {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            if (groupId != 0L) {
                groupUseCases.archiveGroup(groupId, userId)
            }
        }
    }
}

data class StudentSelection(
    val id: Long,
    val name: String,
    val surname: String,
    val selected: Boolean,
    val className: String,
    val rateType: String,
    val rate: Double
)

data class GroupUiState(
    val name: String = "",
    val students: List<StudentSelection> = emptyList(),
    val selectedClass: String = "",
    val customClass: String = "",
    val rate: String = "",
    val rateType: String = DomainRateTypes.HOURLY
)
