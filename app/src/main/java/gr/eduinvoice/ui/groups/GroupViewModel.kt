package gr.eduinvoice.ui.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.domain.group.GroupUseCases
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupUseCases: GroupUseCases,
    private val studentUseCases: StudentUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val groupId: Long = savedStateHandle.get<Long>("groupId") ?: 0L

    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    private var originalStudents: Set<Long> = emptySet()

    init {
        viewModelScope.launch {
            val students = studentUseCases.getActiveStudents().first()
            val selectedIds = if (groupId != 0L) {
                groupUseCases.getGroupStudents(groupId).first().map { it.id }.toSet()
            } else emptySet()
            originalStudents = selectedIds
            val selections = students.map { StudentSelection(it.id, it.name, it.surname, it.id in selectedIds) }
            val name = if (groupId != 0L) groupUseCases.getGroupById(groupId).first()?.name ?: "" else ""
            _uiState.value = GroupUiState(name = name, students = selections)
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

    fun saveGroup(userId: Long) {
        viewModelScope.launch {
            val state = _uiState.value
            val group = StudentGroup(id = groupId, name = state.name)
            val id = if (groupId == 0L) groupUseCases.insertGroup(group) else {
                groupUseCases.updateGroup(group)
                groupId
            }

            val selected = state.students.filter { it.selected }.map { it.id }.toSet()
            val toAdd = selected - originalStudents
            val toRemove = originalStudents - selected
            toAdd.forEach { groupUseCases.addStudentToGroup(id, it, userId) }
            toRemove.forEach { groupUseCases.removeStudentFromGroup(id, it) }
        }
    }
}

data class StudentSelection(val id: Long, val name: String, val surname: String, val selected: Boolean)

data class GroupUiState(
    val name: String = "",
    val students: List<StudentSelection> = emptyList()
)
