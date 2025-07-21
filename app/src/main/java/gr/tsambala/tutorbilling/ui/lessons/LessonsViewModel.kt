package gr.tsambala.tutorbilling.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.tsambala.tutorbilling.data.database.LessonWithStudent
import gr.tsambala.tutorbilling.domain.lesson.LessonUseCases
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val lessonUseCases: LessonUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonsUiState())
    val uiState: StateFlow<LessonsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            lessonUseCases.getLessonsWithStudents().collect { list ->
                _uiState.update { it.copy(lessons = list) }
            }
        }
    }

    fun updatePaid(lessonId: Long, paid: Boolean) {
        viewModelScope.launch {
            val invoiced = lessonUseCases.isLessonInvoiced(lessonId).first() ?: false
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
