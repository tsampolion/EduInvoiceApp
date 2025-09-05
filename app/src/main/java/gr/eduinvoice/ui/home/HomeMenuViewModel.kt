package gr.eduinvoice.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import gr.eduinvoice.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import gr.eduinvoice.domain.billing.calculateFeeWith
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.user.CurrentUserProvider
import gr.eduinvoice.domain.user.PermissionChecker
import gr.eduinvoice.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeMenuViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases,
    private val lessonUseCases: LessonUseCases,
    private val userUseCases: gr.eduinvoice.domain.user.UserUseCases,
    private val currentUserProvider: CurrentUserProvider,
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeMenuUiState())
    val uiState: StateFlow<HomeMenuUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            currentUserProvider.loggedInUserId.filterNotNull().flatMapLatest { uid ->
                if (BuildConfig.DEBUG) {
                    Log.d("HomeMenuViewModel", "Fetching data for user $uid")
                }
                combine(
                    studentUseCases.getActiveStudents(uid),
                    lessonUseCases.getAllLessons(uid),
                    userUseCases.getUserProfile(uid)
                ) { students, lessons, user ->
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            "HomeMenuViewModel",
                            "Loaded ${students.size} students and ${lessons.size} lessons"
                        )
                    }
                    val classCount = students
                        .map { it.className }
                        .filterNot { it.isBlank() || it.equals("unknown", true) }
                        .distinct()
                        .size

                    val userRole = user?.role ?: UserRole.TEACHER
                    
                    HomeMenuUiState(
                        studentCount = students.size,
                        classCount = classCount,
                        lessonCount = lessons.size,
                        isAdmin = permissionChecker.isAdmin(userRole),
                        canManageUsers = permissionChecker.canAccessUserManagement(userRole),
                        canAccessSettings = permissionChecker.canAccessSystemSettings(userRole),
                        canAccessRevenue = permissionChecker.canAccessRevenueAnalytics(userRole)
                    )
                }
            }.collect { state ->
                if (BuildConfig.DEBUG) {
                    Log.d("HomeMenuViewModel", "New UI state -> $state")
                }
                _uiState.value = state
            }
        }
    }
}

data class HomeMenuUiState(
    val studentCount: Int = 0,
    val classCount: Int = 0,
    val lessonCount: Int = 0,
    val isAdmin: Boolean = false,
    val canManageUsers: Boolean = false,
    val canAccessSettings: Boolean = false,
    val canAccessRevenue: Boolean = false
)
