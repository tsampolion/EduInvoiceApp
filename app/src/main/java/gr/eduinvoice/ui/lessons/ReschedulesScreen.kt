package gr.eduinvoice.ui.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.ui.design.SlimHeader

@Composable
fun ReschedulesScreen(
    onBack: () -> Unit,
    viewModel: ReschedulesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { }) { padding ->
        Column(Modifier.padding(padding)) {
            SlimHeader(title = "Reschedules", onBack = onBack)
            if (uiState.masters.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No reschedules yet")
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(uiState.masters) { m ->
                        ListItem(
                            headlineContent = { Text("${m.newDate} ${m.newStartTime}") },
                            supportingContent = { Text(m.notes ?: "") }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@dagger.hilt.android.lifecycle.HiltViewModel
class ReschedulesViewModel @javax.inject.Inject constructor(
    private val lessonUseCases: gr.eduinvoice.domain.lesson.LessonUseCases,
    private val currentUserProvider: gr.eduinvoice.domain.user.CurrentUserProvider
) : androidx.lifecycle.ViewModel() {
    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(ReschedulesUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<ReschedulesUiState> = _uiState

    init {
        viewModelScope.launch {
            currentUserProvider.loggedInUserId
                .filterNotNull()
                .flatMapLatest { uid -> lessonUseCases.getRescheduleMasters(uid) }
                .collect { list ->
                    _uiState.value = ReschedulesUiState(masters = list)
                }
        }
    }
}

data class ReschedulesUiState(
    val masters: List<gr.eduinvoice.domain.model.DomainRescheduleMaster> = emptyList()
)


