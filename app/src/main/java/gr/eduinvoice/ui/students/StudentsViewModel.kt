package gr.eduinvoice.ui.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.ui.model.UiStudentWithEarnings
import gr.eduinvoice.ui.mappers.withEarnings
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.utils.EarningsCalculator
import gr.eduinvoice.utils.ModernSearchRepository
import gr.eduinvoice.utils.ModernFilterManager
import gr.eduinvoice.utils.SearchHistoryRepository
import gr.eduinvoice.ui.components.FilterOptions
import gr.eduinvoice.utils.GlobalCache
import gr.eduinvoice.domain.user.CurrentUserProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import gr.eduinvoice.analytics.PerformanceTraces

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases,
    private val lessonUseCases: LessonUseCases,
    private val currentUserProvider: CurrentUserProvider,
    private val modernSearchRepository: ModernSearchRepository,
    private val modernFilterManager: ModernFilterManager,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentsUiState())
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    private val _filters = MutableStateFlow(FilterOptions())
    val filters: StateFlow<FilterOptions> = _filters.asStateFlow()

    fun updateFilters(newFilters: FilterOptions) {
        _filters.value = newFilters
    }

    init {
        loadStudentsWithEarnings()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getSearchHistorySnapshot(): List<String> = modernSearchRepository.getHistory()

    fun toggleSortOrder() {
        _sortAscending.value = !_sortAscending.value
    }

    private val pageSize = 50

    private fun loadStudentsWithEarnings() {
        viewModelScope.launch {
            currentUserProvider.loggedInUserId.filterNotNull().flatMapLatest { uid ->
                combine(
                    searchQuery,
                    sortAscending,
                    filters
                ) { query, ascending, currentFilters ->
                    loadStudentsWithCaching(uid, query, ascending, 0, currentFilters)
                }
            }.collect { studentsWithEarnings ->
                _uiState.update {
                    it.copy(
                        students = studentsWithEarnings,
                        searchQuery = _searchQuery.value,
                        currentPage = 0,
                        hasMoreData = studentsWithEarnings.size >= pageSize
                    )
                }
            }
        }
    }

    private suspend fun loadStudentsWithCaching(
        uid: Long,
        query: String,
        ascending: Boolean,
        page: Int,
        filters: FilterOptions
    ): List<UiStudentWithEarnings> {
        val cacheKey = "students_${uid}_${query}_${ascending}_$page"

        // Try to get from cache first
        val cachedData = GlobalCache.getCachedDataTyped<List<UiStudentWithEarnings>>(cacheKey)
        if (cachedData != null) {
            return cachedData
        }

        // Use modern search when query present
        val baseStudents = if (query.isBlank()) {
            studentUseCases.getStudentsPaginated(uid, pageSize, page * pageSize)
        } else {
            modernSearchRepository.searchAll(query, pageSize).students
        }
        val students = modernFilterManager.applyStudentFilters(baseStudents, filters)

        // Get lessons for earnings calculation
        var lessons = lessonUseCases.getAllLessons(uid).first()
        // Apply date range to lessons before earnings calc
        lessons = PerformanceTraces.trace("apply_lesson_date_range") {
            modernFilterManager.applyLessonDateRange(lessons, filters.dateRange)
        }

        // Calculate earnings and create UiStudentWithEarnings
        val studentsWithEarnings = PerformanceTraces.trace("earnings_calculation") {
            students.map { student ->
                val (weekEarnings, monthEarnings) = EarningsCalculator.calculate(student, lessons)
                student.withEarnings(weekEarnings, monthEarnings)
            }
        }

        // Sort if needed
        val sortedStudents = if (ascending) {
            studentsWithEarnings.sortedBy { it.student.name }
        } else {
            studentsWithEarnings.sortedByDescending { it.student.name }
        }

        // Cache the result
        GlobalCache.cacheData(cacheKey, sortedStudents)

        return sortedStudents
    }

    fun loadMoreStudents() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMoreData) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: return@launch
            val nextPage = _uiState.value.currentPage + 1

            try {
                val newStudents = loadStudentsWithCaching(
                    uid,
                    _uiState.value.searchQuery,
                    _sortAscending.value,
                    nextPage,
                    _filters.value
                )

                if (newStudents.isNotEmpty()) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            students = currentState.students + newStudents,
                            currentPage = nextPage,
                            hasMoreData = newStudents.size >= pageSize,
                            isLoadingMore = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(hasMoreData = false, isLoadingMore = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun deleteStudent(studentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: 0L
            studentUseCases.softDeleteStudent(studentId, uid)
        }
    }
}

data class StudentsUiState(
    val students: List<UiStudentWithEarnings> = emptyList(),
    val searchQuery: String = "",
    val isLoadingMore: Boolean = false,
    val hasMoreData: Boolean = true,
    val currentPage: Int = 0
)
