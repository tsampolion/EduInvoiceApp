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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupUseCases: GroupUseCases,
    private val studentUseCases: StudentUseCases,
    savedStateHandle: SavedStateHandle,
    private val currentUserProvider: CurrentUserProvider
) : ViewModel() {
    val groupId: Long = savedStateHandle.get<Long>("groupId") ?: 0L

    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    private var originalStudents: Set<Long> = emptySet()

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

    fun saveGroup(overrideClass: Boolean = false, overrideBilling: Boolean = false) {
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
                selected.forEach { studentId ->
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

    fun deleteGroup() {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            val state = _uiState.value
            val group = DomainStudentGroup(id = groupId, ownerId = userId, name = state.name)
            groupUseCases.deleteGroup(group, userId)
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
