package gr.eduinvoice.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.ui.model.UiLessonWithStudent
import gr.eduinvoice.ui.mappers.with
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.testcompat.getFullName
import gr.eduinvoice.data.cache.DataCache
import gr.eduinvoice.domain.user.CurrentUserProvider
import kotlinx.coroutines.flow.*
import gr.eduinvoice.ui.components.FilterOptions
import kotlinx.coroutines.launch
import javax.inject.Inject
import gr.eduinvoice.analytics.PerformanceTraces

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val lessonUseCases: LessonUseCases,
    private val currentUserProvider: CurrentUserProvider,
    private val dataCache: DataCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonsUiState())
    val uiState: StateFlow<LessonsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filters = MutableStateFlow(FilterOptions())
    val filters: StateFlow<FilterOptions> = _filters.asStateFlow()

    private val pageSize = 50

    init {
        loadLessonsWithPagination()
    }

    private fun loadLessonsWithPagination() {
        viewModelScope.launch {
            currentUserProvider.loggedInUserId
                .filterNotNull()
                .flatMapLatest { uid ->
                    combine(
                        lessonUseCases.getAllLessons(uid),
                        lessonUseCases.getLessonsWithStudents(uid),
                        searchQuery,
                        filters
                    ) { lessons, lessonsWithStudents, query, _ ->
                        // For now, we'll use the lessons directly and create UI DTOs
                        // In a real implementation, we'd need to get students separately
                        val filteredLessons = PerformanceTraces.trace("search_filter_lessons") {
                            if (query.isBlank()) {
                                gr.eduinvoice.utils.ModernFilterManager().applyLessonDateRange(lessons, _filters.value.dateRange)
                            } else {
                                val all = gr.eduinvoice.utils.ModernFilterManager().applyLessonDateRange(lessons, _filters.value.dateRange)
                                all.filter { l ->
                                    val hay = "${l.notes ?: ""} ${l.date} ${l.startTime}".lowercase()
                                    hay.contains(query.lowercase())
                                }
                            }
                        }

                        // For now, create UI DTOs with placeholder students
                        // TODO: Get actual students and create proper UI DTOs
                        filteredLessons.map { lesson ->
                            UiLessonWithStudent(
                                lesson = lesson,
                                student = DomainStudent(
                                    id = lesson.studentId,
                                    name = "Student ${lesson.studentId}",
                                    surname = "",
                                    className = "",
                                    rate = 0.0,
                                    rateType = "hourly",
                                    isActive = true
                                )
                            )
                        }.sortedWith(
                            compareByDescending<UiLessonWithStudent> { it.lesson.date }
                                .thenByDescending { it.lesson.startTime }
                        )
                    }
                }
                .collect { lessons ->
                    _uiState.update {
                        it.copy(
                            lessons = lessons,
                            searchQuery = _searchQuery.value,
                            currentPage = 0,
                            hasMoreData = _searchQuery.value.isBlank() && lessons.size >= pageSize
                        )
                    }
                }
        }
    }

    private suspend fun loadLessonsWithCaching(uid: Long, page: Int): List<UiLessonWithStudent> {
        val cacheKey = "lessons_${uid}_$page"

        // Try to get from cache first
        val cachedData = dataCache.getCachedDataTyped<List<UiLessonWithStudent>>(cacheKey)
        if (cachedData != null) {
            return cachedData
        }

        // Load from database with pagination
        val lessons = lessonUseCases.getLessonsWithStudentsPaginated(uid, pageSize, page * pageSize)

        // Create UI DTOs with placeholder students for now
        // TODO: Get actual students and create proper UI DTOs
        val uiLessons = lessons.map { lesson ->
            UiLessonWithStudent(
                lesson = lesson,
                student = DomainStudent(
                    id = lesson.studentId,
                    name = "Student ${lesson.studentId}",
                    surname = "",
                    className = "",
                    rate = 0.0,
                    rateType = "hourly",
                    isActive = true
                )
            )
        }

        // Sort lessons
        val sortedLessons = uiLessons.sortedWith(
            compareByDescending<UiLessonWithStudent> { it.lesson.date }
                .thenByDescending { it.lesson.startTime }
        )

        // Cache the result
        dataCache.cacheData(cacheKey, sortedLessons)

        return sortedLessons
    }

    fun loadMoreLessons() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMoreData || _searchQuery.value.isNotBlank()) return

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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilters(newFilters: FilterOptions) {
        _filters.value = newFilters
    }

    fun updatePaid(lessonId: Long, paid: Boolean) {
        viewModelScope.launch {
            val userId = currentUserProvider.loggedInUserId.first() ?: 0L
            val invoiced = lessonUseCases.isLessonInvoiced(lessonId, userId)
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

    fun createPaymentBatch(ids: List<Long>, batchDate: String, notes: String?) {
        viewModelScope.launch {
            val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: 0L
            try {
                lessonUseCases.createPaymentBatchAndMarkLessons(null, batchDate, notes, ids, uid)
                _uiState.update { it.copy(snackbarMessage = "Marked ${ids.size} lesson(s) paid") }
            } catch (e: Exception) {
                // surface minimal error; could be improved with UiEvents
                _uiState.update { it.copy(dialog = null, snackbarMessage = e.message ?: "Failed to mark paid") }
            }
        }
    }

    fun bulkReschedule(ids: List<Long>, newDate: String, newTime: String, newDuration: Int, notes: String?) {
        viewModelScope.launch {
            val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: 0L
            try {
                lessonUseCases.createRescheduleMasterAndApply(ids, newDate, newTime, newDuration, notes, uid)
                _uiState.update { it.copy(snackbarMessage = "Rescheduled ${ids.size} lesson(s)") }
            } catch (e: Exception) {
                // surface minimal error; could be improved with UiEvents
                _uiState.update { it.copy(dialog = null, snackbarMessage = e.message ?: "Reschedule blocked") }
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun createInvoiceForSelected(ids: List<Long>, invoiceDate: String, notes: String?) {
        viewModelScope.launch {
            val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: 0L
            try {
                // Create a simple invoice number based on timestamp
                val invoiceNumber = System.currentTimeMillis().toString()
                // For simplicity, use studentId = 0 for mixed-student batches; repository layer expects a Long
                lessonUseCases.createInvoiceMasterAndMarkLessons(0, invoiceNumber, invoiceDate, notes, ids, uid)
                _uiState.update { it.copy(snackbarMessage = "Created invoice for ${ids.size} lesson(s)") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = e.message ?: "Failed to create invoice") }
            }
        }
    }
}

data class LessonsUiState(
    val lessons: List<UiLessonWithStudent> = emptyList(),
    val dialog: LessonDialog? = null,
    val snackbarMessage: String? = null,
    val isLoadingMore: Boolean = false,
    val hasMoreData: Boolean = true,
    val currentPage: Int = 0,
    val searchQuery: String = ""
)

sealed interface LessonDialog {
    data class AlreadyInvoiced(val lessonId: Long, val paid: Boolean) : LessonDialog
    data class GenerateInvoice(val lessonId: Long, val studentId: Long) : LessonDialog
}

// Financial guard-aware delete action can be implemented from UI by catching repository errors
