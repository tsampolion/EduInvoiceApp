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
import kotlinx.coroutines.flow.combine

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupUseCases: GroupUseCases,
    private val currentUserProvider: CurrentUserProvider,
    private val getGroupAbsences: GetGroupAbsences
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()
    private val _query = MutableStateFlow("")
    private val _sortAscending = MutableStateFlow(true)
    fun updateQuery(q: String) { _query.value = q }
    fun toggleSort() { _sortAscending.value = !_sortAscending.value }

    init {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            combine(groupUseCases.getAllGroups(userId), _query, _sortAscending) { groups, q, asc ->
                val filtered = if (q.isBlank()) groups else groups.filter { it.name.contains(q, ignoreCase = true) }
                val sorted = filtered.sortedWith(compareBy { it.name.lowercase() })
                val ordered = if (asc) sorted else sorted.asReversed()
                GroupsUiState(groups = ordered, searchQuery = q, sortAscending = asc)
            }.collect { state -> _uiState.value = state }
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
    val groups: List<gr.eduinvoice.domain.model.DomainStudentGroup> = emptyList(),
    val searchQuery: String = "",
    val sortAscending: Boolean = true
)
