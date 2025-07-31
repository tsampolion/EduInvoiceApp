package gr.eduinvoice.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.utils.getFullName
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

    init {
        viewModelScope.launch {
            currentUserProvider.loggedInUserId.filterNotNull().flatMapLatest { uid ->
                lessonUseCases.getLessonsWithStudents(uid)
            }.collect { list ->
                val sorted = list.sortedBy { it.student.getFullName() }
                _uiState.update { it.copy(lessons = sorted) }
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
                lessonUseCases.updateLessonPaidStatus(listOf(lessonId), paid)
            }
        }
    }

    fun applyPaidStatus(lessonId: Long, paid: Boolean) {
        viewModelScope.launch { lessonUseCases.updateLessonPaidStatus(listOf(lessonId), paid) }
        _uiState.update { it.copy(dialog = null) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialog = null) }
    }
}

data class LessonsUiState(
    val lessons: List<LessonWithStudent> = emptyList(),
    val dialog: LessonDialog? = null
)

sealed interface LessonDialog {
    data class AlreadyInvoiced(val lessonId: Long, val paid: Boolean) : LessonDialog
    data class GenerateInvoice(val lessonId: Long, val studentId: Long) : LessonDialog
}
