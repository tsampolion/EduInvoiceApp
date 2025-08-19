package gr.eduinvoice.ui.groups

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.domain.group.GroupUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import gr.eduinvoice.domain.user.CurrentUserProvider
import gr.eduinvoice.domain.lesson.GetGroupAbsences
import gr.eduinvoice.domain.model.DomainAbsence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupUseCases: GroupUseCases,
    private val currentUserProvider: CurrentUserProvider,
    private val getGroupAbsences: GetGroupAbsences
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            groupUseCases.getAllGroups(userId).collect { groups ->
                _uiState.value = GroupsUiState(groups)
            }
        }
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            getGroupAbsences(userId).collect { list ->
                _absences.value = list
            }
        }
    }

    private val _absences = MutableStateFlow<List<DomainAbsence>>(emptyList())
    val absences: StateFlow<List<DomainAbsence>> = _absences.asStateFlow()
}

data class GroupsUiState(
    val groups: List<gr.eduinvoice.domain.model.DomainStudentGroup> = emptyList()
)
