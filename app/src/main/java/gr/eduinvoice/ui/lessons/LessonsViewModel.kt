package gr.eduinvoice.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.utils.getFullName
import gr.eduinvoice.utils.GlobalCache
import gr.eduinvoice.data.user.CurrentUserProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val lessonUseCases: LessonUseCases,
    private val currentUserProvider: CurrentUserProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonsUiState())
    val uiState: StateFlow<LessonsUiState> = _uiState.asStateFlow()

    private val pageSize = 50
    
    init {
        loadLessonsWithPagination()
    }
    
    private fun loadLessonsWithPagination() {
        viewModelScope.launch {
            currentUserProvider.loggedInUserId
                .filterNotNull()
                .flatMapLatest { uid -> 
                    flow { emit(loadLessonsWithCaching(uid, 0)) }
                }
                .collect { lessons ->
                    _uiState.update { 
                        it.copy(
                            lessons = lessons,
                            currentPage = 0,
                            hasMoreData = lessons.size >= pageSize
                        ) 
                    }
                }
        }
    }
    
    private suspend fun loadLessonsWithCaching(uid: Long, page: Int): List<LessonWithStudent> {
        val cacheKey = "lessons_${uid}_$page"
        
        // Try to get from cache first
        val cachedData = GlobalCache.getCachedDataTyped<List<LessonWithStudent>>(cacheKey)
        if (cachedData != null) {
            return cachedData
        }
        
        // Load from database with pagination
        val lessons = lessonUseCases.getLessonsWithStudentsPaginated(uid, pageSize, page * pageSize)
        
        // Sort lessons
        val sortedLessons = lessons.sortedWith(
            compareByDescending<LessonWithStudent> { it.lesson.date }
                .thenByDescending { it.lesson.startTime }
        )
        
        // Cache the result
        GlobalCache.cacheData(cacheKey, sortedLessons)
        
        return sortedLessons
    }
    
    fun loadMoreLessons() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMoreData) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            
            val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: return@launch
            val nextPage = _uiState.value.currentPage + 1
            
            try {
                val newLessons = loadLessonsWithCaching(uid, nextPage)
                
                if (newLessons.isNotEmpty()) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            lessons = currentState.lessons + newLessons,
                            currentPage = nextPage,
                            hasMoreData = newLessons.size >= pageSize,
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

    fun updatePaid(lessonId: Long, paid: Boolean) {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            val invoiced = lessonUseCases.isLessonInvoiced(lessonId, userId).first() ?: false
            val lesson = _uiState.value.lessons.find { it.lesson.id == lessonId }
            if (invoiced) {
                _uiState.update { it.copy(dialog = LessonDialog.AlreadyInvoiced(lessonId, paid)) }
            } else if (paid && lesson != null) {
                _uiState.update { it.copy(dialog = LessonDialog.GenerateInvoice(lessonId, lesson.student.id)) }
            } else {
                lessonUseCases.updateLessonPaidStatus(listOf(lessonId), paid, userId)
            }
        }
    }

    fun applyPaidStatus(lessonId: Long, paid: Boolean) {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            lessonUseCases.updateLessonPaidStatus(listOf(lessonId), paid, userId)
        }
        _uiState.update { it.copy(dialog = null) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialog = null) }
    }
}

data class LessonsUiState(
    val lessons: List<LessonWithStudent> = emptyList(),
    val dialog: LessonDialog? = null,
    val isLoadingMore: Boolean = false,
    val hasMoreData: Boolean = true,
    val currentPage: Int = 0
)

sealed interface LessonDialog {
    data class AlreadyInvoiced(val lessonId: Long, val paid: Boolean) : LessonDialog
    data class GenerateInvoice(val lessonId: Long, val studentId: Long) : LessonDialog
}
