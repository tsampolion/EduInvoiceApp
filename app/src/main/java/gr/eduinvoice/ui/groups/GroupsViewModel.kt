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

@HiltViewModel
class GroupsViewModel @Inject constructor(
    groupUseCases: GroupUseCases
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            groupUseCases.getAllGroups().collect { groups ->
                _uiState.value = GroupsUiState(groups)
            }
        }
    }
}

data class GroupsUiState(
    val groups: List<gr.eduinvoice.data.model.StudentGroup> = emptyList()
)
