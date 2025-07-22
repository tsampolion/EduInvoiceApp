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

@HiltViewModel
class ClassesViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassesUiState())
    val uiState: StateFlow<ClassesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            studentUseCases.getActiveStudents().map { students ->
                val grouped = students.groupBy { student ->
                    val name = student.className.trim()
                    if (name.isBlank() || name.equals("unknown", ignoreCase = true)) {
                        "Unassigned"
                    } else {
                        name
                    }
                }
                val hasUnassigned = grouped.containsKey("Unassigned")
                ClassesUiState(grouped, hasUnassigned)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

data class ClassesUiState(
    val studentsByClass: Map<String, List<gr.eduinvoice.data.model.Student>> = emptyMap(),
    val hasUnassigned: Boolean = false
)
