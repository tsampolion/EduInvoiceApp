package gr.tsambala.tutorbilling.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.tsambala.tutorbilling.data.database.LessonWithStudent
import gr.tsambala.tutorbilling.domain.lesson.LessonUseCases
import gr.tsambala.tutorbilling.utils.getFullName
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
                val grouped = list
                    .groupBy { it.student.id }
                    .toList()
                    .sortedBy { (_, lessons) -> lessons.first().student.getFullName() }
                    .associate { it.first to it.second }
                _uiState.update { it.copy(lessons = grouped) }
            }
        }
    }

    fun updatePaid(lessonId: Long, paid: Boolean) {
        viewModelScope.launch {
            lessonUseCases.updateLessonPaidStatus(listOf(lessonId), paid)
        }
    }
}

data class LessonsUiState(
    val lessons: Map<Long, List<LessonWithStudent>> = emptyMap()
)
