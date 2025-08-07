package gr.eduinvoice.ui.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentWithEarnings
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.utils.EarningsCalculator
import gr.eduinvoice.utils.GlobalCache
import gr.eduinvoice.data.user.CurrentUserProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases,
    private val lessonUseCases: LessonUseCases,
    private val currentUserProvider: CurrentUserProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentsUiState())
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    init {
        loadStudentsWithEarnings()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSortOrder() {
        _sortAscending.value = !_sortAscending.value
    }

    private val pageSize = 50
    
    private fun loadStudentsWithEarnings() {
        viewModelScope.launch {
            currentUserProvider.loggedInUserId.filterNotNull().flatMapLatest { uid ->
                combine(
                    searchQuery,
                    sortAscending
                ) { query, ascending ->
                    loadStudentsWithCaching(uid, query, ascending, 0)
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
        page: Int
    ): List<StudentWithEarnings> {
        val cacheKey = "students_${uid}_${query}_${ascending}_$page"
        
        // Try to get from cache first
        val cachedData = GlobalCache.getCachedDataTyped<List<StudentWithEarnings>>(cacheKey)
        if (cachedData != null) {
            return cachedData
        }
        
        // Load from database with pagination
        val students = if (query.isBlank()) {
            studentUseCases.getStudentsPaginated(uid, pageSize, page * pageSize)
        } else {
            studentUseCases.searchStudentsPaginated(uid, query, pageSize, page * pageSize)
        }
        
        // Get lessons for earnings calculation
        val lessons = lessonUseCases.getAllLessons(uid).first()
        
        // Calculate earnings and create StudentWithEarnings
        val studentsWithEarnings = students.map { student ->
            val (weekEarnings, monthEarnings) = EarningsCalculator.calculate(student, lessons)
            StudentWithEarnings(
                student = student,
                weekEarnings = weekEarnings,
                monthEarnings = monthEarnings
            )
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
                    nextPage
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
    val students: List<StudentWithEarnings> = emptyList(),
    val searchQuery: String = "",
    val isLoadingMore: Boolean = false,
    val hasMoreData: Boolean = true,
    val currentPage: Int = 0
)
