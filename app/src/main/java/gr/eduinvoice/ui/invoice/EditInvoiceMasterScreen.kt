package gr.eduinvoice.ui.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.ui.design.SlimHeader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EditInvoiceMasterScreen(
    masterId: Long,
    onBack: () -> Unit,
    viewModel: EditInvoiceMasterViewModel = hiltViewModel()
) {
    LaunchedEffect(masterId) { viewModel.load(masterId) }
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val bus = gr.eduinvoice.ui.components.LocalSnackbarBus.current

    Scaffold(topBar = { }, snackbarHost = { }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            SlimHeader(title = "Edit Invoice", onBack = onBack)
            OutlinedTextField(
                value = ui.invoiceNumber,
                onValueChange = {},
                readOnly = true,
                label = { Text("Invoice #") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = ui.invoiceDate,
                onValueChange = viewModel::updateDate,
                label = { Text("Invoice Date (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = ui.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = {
                    viewModel.save(onSuccess = {
                        bus.show("Invoice updated")
                        onBack()
                    }, onError = { msg -> bus.show(msg) })
                }, modifier = Modifier.weight(1f), enabled = ui.isValid()) { Text("Save") }
            }
        }
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class EditInvoiceMasterViewModel @javax.inject.Inject constructor(
    private val lessonUseCases: gr.eduinvoice.domain.lesson.LessonUseCases,
    private val currentUserProvider: gr.eduinvoice.domain.user.CurrentUserProvider
) : androidx.lifecycle.ViewModel() {
    data class Ui(
        val id: Long = 0,
        val invoiceNumber: String = "",
        val invoiceDate: String = LocalDate.now().toString(),
        val notes: String = ""
    ) {
        fun isValid(): Boolean = runCatching { LocalDate.parse(invoiceDate, DateTimeFormatter.ISO_DATE) }.isSuccess
    }

    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(Ui())
    val uiState: kotlinx.coroutines.flow.StateFlow<Ui> = _uiState

    fun updateDate(value: String) { _uiState.value = _uiState.value.copy(invoiceDate = value) }
    fun updateNotes(value: String) { _uiState.value = _uiState.value.copy(notes = value) }

    fun load(id: Long) {
        viewModelScope.launch {
            val uid = currentUserProvider.loggedInUserId.first() ?: 0L
            lessonUseCases.getInvoiceMasterById(id, uid).collect { master ->
                master?.let {
                    _uiState.value = Ui(
                        id = it.id,
                        invoiceNumber = it.invoiceNumber,
                        invoiceDate = it.invoiceDate,
                        notes = it.notes ?: ""
                    )
                }
            }
        }
    }

    fun save(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val uid = currentUserProvider.loggedInUserId.first() ?: 0L
            val st = _uiState.value
            try {
                val updated = gr.eduinvoice.domain.model.DomainInvoiceMaster(
                    id = st.id,
                    studentId = 0,
                    invoiceNumber = st.invoiceNumber,
                    invoiceDate = st.invoiceDate,
                    notes = st.notes
                )
                lessonUseCases.updateInvoiceMaster(updated, uid)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update invoice")
            }
        }
    }
}
