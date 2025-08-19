// StudentViewModel.kt - Fixed navigation thread issue
package gr.eduinvoice.ui.student

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.model.DomainRateTypes
import gr.eduinvoice.domain.billing.calculateFeeWith
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.group.GroupUseCases
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.model.DomainAbsence
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.user.CurrentUserProvider
import gr.eduinvoice.utils.EarningsCalculator
import gr.eduinvoice.utils.ClassOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Patterns
import android.database.sqlite.SQLiteException
import gr.eduinvoice.utils.ErrorHandler
import gr.eduinvoice.utils.RetryManager
import gr.eduinvoice.analytics.ErrorReporter
import javax.inject.Inject
import gr.eduinvoice.data.cache.DataCache

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases,
    private val lessonUseCases: LessonUseCases,
    savedStateHandle: SavedStateHandle,
    private val currentUserProvider: CurrentUserProvider,
    @ApplicationContext private val context: Context,
    private val dataCache: DataCache,
    private val groupUseCases: GroupUseCases
) : ViewModel() {

    val studentId: Long = savedStateHandle.get<Long>("studentId") ?: 0L

    private val classOptions = ClassOptions.DEFAULT

    // UI State
    private val _uiState = MutableStateFlow(StudentUiState(isEditMode = studentId == 0L))
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    // Error handling components
    private val errorHandler = ErrorHandler(context)
    private val retryManager = RetryManager()
    private val errorReporter = ErrorReporter(context)

    // Navigation callback
    private var onNavigateBack: (() -> Unit)? = null

    init {
        if (studentId > 0) {
            loadData()
        }
    }

    fun setNavigationCallback(callback: () -> Unit) {
        onNavigateBack = callback
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                currentUserProvider.loggedInUserId
                    .filterNotNull()
                    .flatMapLatest { uid -> studentAndLessonsFlow(uid) }
                    .catch { e ->
                        val errorResult = errorHandler.handleError(e, "StudentViewModel_LoadData")
                        errorReporter.reportError(e, "StudentViewModel_LoadData")
                        _uiState.update { it.copy(errorMessage = errorResult.userMessage) }
                    }
                    .collect { (student, lessons) -> mapToUiState(student, lessons) }
            } catch (e: Exception) {
                val errorResult = errorHandler.handleError(e, "StudentViewModel_LoadData")
                errorReporter.reportError(e, "StudentViewModel_LoadData")
                _uiState.update { it.copy(errorMessage = errorResult.userMessage) }
            }
        }

        // Observe student's group assignment and absences
        viewModelScope.launch {
            currentUserProvider.loggedInUserId
                .filterNotNull()
                .flatMapLatest { uid -> groupUseCases.getStudentGroups(studentId, uid) }
                .collect { groups ->
                    val name = groups.firstOrNull()?.name
                    _uiState.update { it.copy(groupName = name) }
                }
        }

        viewModelScope.launch {
            currentUserProvider.loggedInUserId
                .filterNotNull()
                .flatMapLatest { uid -> lessonUseCases.getAbsencesForStudent(studentId, uid) }
                .collect { list ->
                    _uiState.update { it.copy(absences = list) }
                }
        }
    }

    private fun studentAndLessonsFlow(userId: Long): Flow<Pair<DomainStudent?, List<DomainLesson>>> =
        combine(
            studentUseCases.getStudentById(studentId, userId),
            lessonUseCases.getStudentLessons(studentId, userId)
        ) { student, lessons -> student to lessons }

    // groupName and absences are exposed via uiState

    private fun mapToUiState(student: DomainStudent?, lessons: List<DomainLesson>) {
        val (week, month) = student?.let { EarningsCalculator.calculate(it, lessons) } ?: (0.0 to 0.0)
        val total = student?.let { lessons.sumOf { l -> l.calculateFeeWith(it) } } ?: 0.0
        _uiState.update { currentState ->
            val existingClass = student?.className ?: ""
            val (selectedClass, customClass) = if (
                existingClass.isNotBlank() && existingClass !in classOptions
            ) {
                "Custom" to existingClass
            } else {
                existingClass to ""
            }
            currentState.copy(
                student = student,
                name = if (currentState.isEditMode) currentState.name else student?.name ?: "",
                surname = if (currentState.isEditMode) currentState.surname else student?.surname ?: "",
                parentMobile = if (currentState.isEditMode) currentState.parentMobile else student?.parentMobile ?: "",
                parentEmail = if (currentState.isEditMode) currentState.parentEmail else student?.parentEmail ?: "",
                selectedClass = if (currentState.isEditMode) currentState.selectedClass else selectedClass,
                customClass = if (currentState.isEditMode) currentState.customClass else customClass,
                rate = if (currentState.isEditMode) currentState.rate else student?.rate?.toString() ?: "",
                rateType = if (currentState.isEditMode) {
                    currentState.rateType
                } else {
                    student?.rateType ?: DomainRateTypes.HOURLY
                },
                isActive = if (currentState.isEditMode) currentState.isActive else student?.isActive ?: true,
                lessons = lessons,
                weekEarnings = week,
                monthEarnings = month,
                totalEarnings = total
            )
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, hasChanges = true) }
    }

    fun updateSurname(value: String) {
        _uiState.update { it.copy(surname = value, hasChanges = true) }
    }

    fun updateParentMobile(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(parentMobile = digitsOnly, hasChanges = true) }
    }

    fun updateParentEmail(value: String) {
        _uiState.update { it.copy(parentEmail = value, hasChanges = true) }
    }

    fun updateSelectedClass(value: String) {
        _uiState.update {
            val custom = if (value == "Custom") it.customClass else ""
            it.copy(selectedClass = value, customClass = custom, hasChanges = true)
        }
    }

    fun updateCustomClass(value: String) {
        _uiState.update { it.copy(customClass = value, hasChanges = true) }
    }

    fun updateRate(rate: String) {
        val sanitized = buildString {
            var dotSeen = false
            for (ch in rate) {
                when {
                    ch.isDigit() -> append(ch)
                    ch == '.' && !dotSeen -> {
                        append(ch)
                        dotSeen = true
                    }
                }
            }
        }
        _uiState.update { it.copy(rate = sanitized, hasChanges = true) }
    }

    fun updateRateType(type: String) {
        _uiState.update { it.copy(rateType = type, hasChanges = true) }
    }

    fun toggleActive() {
        _uiState.update { it.copy(isActive = !it.isActive, hasChanges = true) }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    private fun validate(state: StudentUiState): Pair<Double, String>? {
        val rate = state.rate.toDoubleOrNull()?.takeIf { it > 0 }
        if (rate == null) {
            _uiState.update { it.copy(errorMessage = "Invalid rate") }
            return null
        }

        if (state.parentEmail.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS.matcher(state.parentEmail).matches()
        ) {
            _uiState.update { it.copy(errorMessage = "Invalid email") }
            return null
        }

        val className = if (state.selectedClass == "Custom") state.customClass.trim() else state.selectedClass
        if (className.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Class required") }
            return null
        }

        return rate to className
    }

    private fun buildStudent(rate: Double, className: String, userId: Long, state: StudentUiState): DomainStudent {
        val mobile = state.parentMobile.ifBlank { "" }
        return if (studentId > 0) {
            DomainStudent(
                id = studentId,
                name = state.name,
                surname = state.surname,
                parentMobile = mobile,
                parentEmail = state.parentEmail.ifBlank { null },
                className = className,
                rate = rate,
                rateType = state.rateType,
                isActive = state.isActive
            )
        } else {
            DomainStudent(
                ownerId = userId,
                name = state.name,
                surname = state.surname,
                parentMobile = mobile,
                parentEmail = state.parentEmail.ifBlank { null },
                className = className,
                rate = rate,
                rateType = state.rateType,
                isActive = state.isActive
            )
        }
    }

    fun saveStudent() {
        val state = _uiState.value
        val validation = validate(state) ?: return
        val (rate, className) = validation

        viewModelScope.launch { performSave(rate, className, state) }
    }

    private suspend fun performSave(rate: Double, className: String, state: StudentUiState) {
        _uiState.update { it.copy(isLoading = true) }

        try {
            // Use retry manager for save operation
            val result = retryManager.executeWithRetry(
                operation = {
                    val userId = currentUserProvider.loggedInUserId.first() ?: 0L
                    if (state.selectedClass == "Custom" && studentUseCases.classNameExists(className, userId)) {
                        throw IllegalArgumentException("Class already exists")
                    }
                    val student = buildStudent(rate, className, userId, state)
                    saveToRepository(student)
                    student
                },
                maxRetries = 2,
                retryId = "save_student_${studentId}",
                shouldRetry = { error ->
                    errorHandler.shouldRetry(error)
                },
                onRetry = { error, attempt ->
                    errorReporter.reportError(error, "StudentViewModel_Save_Retry_$attempt")
                }
            )

            if (result.isSuccess) {
                // Clear cached lists so screens reload fresh data
                dataCache.clearCache()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false) }
                    navigateBack()
                }
            } else {
                val error = result.exceptionOrNull() ?: Exception("Unknown save error")
                val errorResult = errorHandler.handleError(error, "StudentViewModel_Save")
                errorReporter.reportError(error, "StudentViewModel_Save")

                _uiState.update {
                    it.copy(isLoading = false, errorMessage = errorResult.userMessage)
                }
            }
        } catch (e: Exception) {
            val errorResult = errorHandler.handleError(e, "StudentViewModel_Save")
            errorReporter.reportError(e, "StudentViewModel_Save")

            _uiState.update {
                it.copy(isLoading = false, errorMessage = errorResult.userMessage)
            }
        }
    }

    private suspend fun saveToRepository(student: DomainStudent) {
        if (studentId > 0) {
            studentUseCases.updateStudent(student)
        } else {
            studentUseCases.insertStudent(student)
        }
    }

    private fun navigateBack() {
        onNavigateBack?.invoke()
    }

    fun deleteStudent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val result = retryManager.executeWithRetry(
                    operation = {
                        _uiState.value.student?.let { student ->
                            val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: 0L
                            // Financial edit guard: prevent archive/delete when the student has any paid/invoiced lessons
                            val lessons = lessonUseCases.getStudentLessons(student.id, uid).first()
                            val hasLocked = lessons.any { it.isPaid || it.isInvoiced }
                            if (hasLocked) throw IllegalStateException("Cannot archive student with paid or invoiced lessons")
                            studentUseCases.softDeleteStudent(student.id, uid)
                            student
                        } ?: throw IllegalArgumentException("No student to delete")
                    },
                    maxRetries = 2,
                    retryId = "delete_student_${studentId}",
                    shouldRetry = { error ->
                        errorHandler.shouldRetry(error)
                    },
                    onRetry = { error, attempt ->
                        errorReporter.reportError(error, "StudentViewModel_Delete_Retry_$attempt")
                    }
                )

                if (result.isSuccess) {
                    // Clear loading and navigate back on main thread
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isLoading = false) }
                        onNavigateBack?.invoke()
                    }
                } else {
                    val error = result.exceptionOrNull() ?: Exception("Unknown delete error")
                    val errorResult = errorHandler.handleError(error, "StudentViewModel_Delete")
                    errorReporter.reportError(error, "StudentViewModel_Delete")

                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = errorResult.userMessage)
                    }
                }
            } catch (e: Exception) {
                val errorResult = errorHandler.handleError(e, "StudentViewModel_Delete")
                errorReporter.reportError(e, "StudentViewModel_Delete")

                _uiState.update {
                    it.copy(isLoading = false, errorMessage = errorResult.userMessage)
                }
            }
        }
    }

    fun deleteLesson(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = currentUserProvider.loggedInUserId.firstOrNull() ?: 0L
            lessonUseCases.deleteLesson(id, uid)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class StudentUiState(
    val student: DomainStudent? = null,
    val name: String = "",
    val surname: String = "",
    val parentMobile: String = "",
    val parentEmail: String = "",
    val selectedClass: String = "",
    val customClass: String = "",
    val rate: String = "",
    val rateType: String = DomainRateTypes.HOURLY,
    val isActive: Boolean = true,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val hasChanges: Boolean = false,
    val errorMessage: String? = null,
    val lessons: List<DomainLesson> = emptyList(),
    val weekEarnings: Double = 0.0,
    val monthEarnings: Double = 0.0,
    val totalEarnings: Double = 0.0
    ,
    val groupName: String? = null,
    val absences: List<DomainAbsence> = emptyList()
)
