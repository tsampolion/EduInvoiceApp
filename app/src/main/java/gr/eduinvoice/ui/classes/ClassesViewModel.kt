package gr.eduinvoice.ui.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.eduinvoice.domain.student.StudentUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import gr.eduinvoice.domain.user.CurrentUserProvider
import kotlinx.coroutines.flow.first
import gr.eduinvoice.ui.components.FilterOptions

@HiltViewModel
class ClassesViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases,
    private val currentUserProvider: CurrentUserProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassesUiState())
    val uiState: StateFlow<ClassesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    private val _filters = MutableStateFlow(FilterOptions())
    val filters: StateFlow<FilterOptions> = _filters.asStateFlow()

    private var allStudents: List<gr.eduinvoice.domain.model.DomainStudent> = emptyList()

    init {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            studentUseCases.getActiveStudents(userId).collect { students ->
                allStudents = students
                applyFiltersAndSearch()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFiltersAndSearch()
    }

    fun toggleSortOrder() {
        _sortAscending.value = !_sortAscending.value
        applyFiltersAndSearch()
    }

    fun updateFilters(filters: FilterOptions) {
        _filters.value = filters
        applyFiltersAndSearch()
    }

    private fun applyFiltersAndSearch() {
        val filteredStudents = allStudents.filter { student ->
            val matchesSearch = student.getFullName().contains(_searchQuery.value, ignoreCase = true) ||
                    student.className.contains(_searchQuery.value, ignoreCase = true)
            
            // Apply additional filters if needed
            matchesSearch
        }

        val grouped = filteredStudents.groupBy { student ->
            val name = student.className.trim()
            if (name.isBlank() || name.equals("unknown", ignoreCase = true)) {
                "Unassigned"
            } else {
                name
            }
        }

        // Sort classes alphabetically
        val sortedGroups = if (_sortAscending.value) {
            grouped.toSortedMap()
        } else {
            grouped.toSortedMap(compareByDescending { it })
        }

        val hasUnassigned = sortedGroups.containsKey("Unassigned")
        _uiState.value = ClassesUiState(sortedGroups, hasUnassigned)
    }
}

data class ClassesUiState(
    val studentsByClass: Map<String, List<gr.eduinvoice.domain.model.DomainStudent>> = emptyMap(),
    val hasUnassigned: Boolean = false
)
