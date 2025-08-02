package gr.eduinvoice.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onAllNodesWithText
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import gr.eduinvoice.data.dao.*
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.*
import gr.eduinvoice.data.repository.*
import gr.eduinvoice.data.settings.SettingsRepository
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.data.user.userPrefsDataStore
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.student.*
import gr.eduinvoice.domain.user.*
import gr.eduinvoice.ui.home.HomeMenuScreen
import gr.eduinvoice.ui.home.HomeMenuViewModel
import gr.eduinvoice.FakeUserProvider
import gr.eduinvoice.ui.user.LoginScreen
import gr.eduinvoice.ui.user.LoginViewModel
import gr.eduinvoice.ui.welcome.WelcomeScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var prefs: UserPreferencesRepository
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var backupRepo: BackupRepository
    private lateinit var userUseCases: UserUseCases
    private lateinit var loginViewModel: LoginViewModel

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dao = object : UserDao {
            private val user = User(
                id = 1,
                username = "bob",
                passwordHash = PasswordHasher.hash("pass"),
                fullName = "Bob"
            )
            override suspend fun insert(user: User): Long = 1L
            override suspend fun update(user: User) {}
            override suspend fun delete(user: User) {}
            override fun getUserById(id: Long): Flow<User?> = flowOf(if (id == 1L) user else null)
            override suspend fun getByUsername(username: String): User? =
                if (username == user.username) user else null
        }
        val userRepo = UserRepository(dao)
        userUseCases = UserUseCases(
            createUser = CreateUser(userRepo),
            authenticateUser = AuthenticateUser(userRepo),
            getUserProfile = GetUserProfile(userRepo),
            updateUser = UpdateUser(userRepo),
            resetPassword = ResetPassword(userRepo)
        )
        prefs = UserPreferencesRepository(context, context.userPrefsDataStore)
        settingsRepo = SettingsRepository(context)
        val db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backupRepo = BackupRepository(db)
        loginViewModel = LoginViewModel(userUseCases, prefs, context)
        runBlocking { prefs.setLoggedInUser(null) }
    }

    @Test
    fun welcomeSettingsShowsAuthButtons() {
        val vm = SettingsViewModel(settingsRepo, prefs, userUseCases, backupRepo)
        composeRule.setContent {
            val showSettings = remember { mutableStateOf(false) }
            if (showSettings.value) {
                SettingsScreen(
                    openDrawer = { showSettings.value = false },
                    onPrivacyPolicy = {},
                    onLogin = {},
                    onRegister = {},
                    onLogout = {},
                    onSwitchAccount = {},
                    onProfile = {},
                    viewModel = vm
                )
            } else {
                WelcomeScreen(
                    onSignIn = {},
                    onSignUp = {},
                    onSettings = { showSettings.value = true },
                    onMenuClick = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Sign In").assertExists()
        composeRule.onNodeWithText("Sign Up").assertExists()
    }

    @Test
    fun settingsFromHomeShowsAccountButtons() {
        runBlocking { prefs.setLoggedInUser(1L) }
        val settingsVm = SettingsViewModel(settingsRepo, prefs, userUseCases, backupRepo)
        val studentFlow = MutableStateFlow<List<Student>>(emptyList())
        val lessonFlow = MutableStateFlow<List<Lesson>>(emptyList())
        val studentDao = object : StudentDao {
            override suspend fun insert(student: Student) = 1L
            override suspend fun update(student: Student) {}
            override suspend fun delete(student: Student) {}
            override suspend fun softDeleteStudent(studentId: Long, userId: Long) {}
            override fun getStudentById(studentId: Long, userId: Long): Flow<Student?> = flowOf(null)
            override fun getAllActiveStudents(userId: Long): Flow<List<Student>> = studentFlow.asStateFlow()
            override fun getArchivedStudents(userId: Long): Flow<List<Student>> = flowOf(emptyList())
            override suspend fun restoreStudent(studentId: Long, userId: Long) {}
            override fun getStudentByIdAny(studentId: Long, userId: Long): Flow<Student?> = flowOf(null)
            override suspend fun getActiveStudentCount(userId: Long): Int = 0
            override suspend fun classNameExists(name: String, userId: Long): Int = 0
        }
        val lessonDao = object : LessonDao {
            override suspend fun insert(lesson: Lesson) = 1L
            override suspend fun update(lesson: Lesson) {}
            override suspend fun delete(lesson: Lesson) {}
            override suspend fun deleteById(lessonId: Long, userId: Long) {}
            override fun getLessonById(lessonId: Long, userId: Long): Flow<Lesson?> = flowOf(null)
            override fun getLessonsByStudentId(studentId: Long, userId: Long): Flow<List<Lesson>> =
                flowOf(emptyList())
            override fun getAllLessons(userId: Long): Flow<List<Lesson>> = lessonFlow.asStateFlow()
            override fun getLessonsInDateRange(
                startDate: String,
                endDate: String,
                userId: Long
            ): Flow<List<Lesson>> = flowOf(emptyList())
            override fun getLessonsByStudentAndDateRange(
                studentId: Long,
                startDate: String,
                endDate: String,
                userId: Long
            ): Flow<List<Lesson>> = flowOf(emptyList())
            override fun getUnpaidLessonsByStudentAndDateRange(
                studentId: Long,
                startDate: String,
                endDate: String,
                userId: Long
            ): Flow<List<Lesson>> = flowOf(emptyList())
            override fun getUnpaidLessonsInDateRange(
                startDate: String,
                endDate: String,
                userId: Long
            ): Flow<List<Lesson>> = flowOf(emptyList())
            override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean, userId: Long) {}
            override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean, userId: Long) {}
            override fun isLessonInvoiced(lessonId: Long, userId: Long): Flow<Boolean?> = flowOf(null)
            override fun getLessonsWithStudents(userId: Long): Flow<List<LessonWithStudent>> =
                flowOf(emptyList<LessonWithStudent>())
            override fun getLessonsWithStudentsByStudent(
                studentId: Long,
                userId: Long
            ): Flow<List<LessonWithStudent>> = flowOf(emptyList<LessonWithStudent>())
            override fun getLessonsWithStudentsInDateRange(
                startDate: String,
                endDate: String,
                userId: Long
            ): Flow<List<LessonWithStudent>> = flowOf(emptyList<LessonWithStudent>())
            override fun getLessonsWithStudentsByStudentAndDateRange(
                studentId: Long,
                startDate: String,
                endDate: String,
                userId: Long
            ): Flow<List<LessonWithStudent>> = flowOf(emptyList<LessonWithStudent>())
            override suspend fun insertGroupLessons(lessons: List<Lesson>): List<Long> = lessons.map { it.id }
        }
        val groupDao = object : GroupDao {
            override suspend fun insertGroup(group: StudentGroup) = 1L
            override suspend fun updateGroup(group: StudentGroup) {}
            override suspend fun deleteGroup(group: StudentGroup) {}
            override fun getAllGroups(userId: Long): Flow<List<StudentGroup>> = flowOf(emptyList())
            override fun getGroupById(id: Long, userId: Long): Flow<StudentGroup?> = flowOf(null)
            override suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) {}
            override suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long) {}
            override fun getStudentsForGroup(groupId: Long, userId: Long): Flow<List<Student>> = flowOf(emptyList())
        }
        val studentRepo = StudentRepository(studentDao)
        val groupRepo = GroupRepository(groupDao)
        val billingRepo = TutorBillingRepository(studentDao, lessonDao, groupDao)
        val studentUseCases = StudentUseCases(
            getActiveStudents = GetActiveStudents(studentRepo),
            getArchivedStudents = GetArchivedStudents(studentRepo),
            getStudentById = GetStudentById(studentRepo),
            insertStudent = InsertStudent(studentRepo),
            updateStudent = UpdateStudent(studentRepo),
            softDeleteStudent = SoftDeleteStudent(studentRepo),
            restoreStudent = RestoreStudent(studentRepo),
            getActiveStudentCount = GetActiveStudentCount(studentRepo),
            classNameExists = ClassNameExists(studentRepo)
        )
        val lessonUseCases = LessonUseCases(
            getAllLessons = GetAllLessons(lessonDao),
            getLessonById = GetLessonById(lessonDao),
            getStudentLessons = GetStudentLessons(billingRepo),
            getLessonsWithStudents = GetLessonsWithStudents(lessonDao),
            getLessonsWithStudentsByStudentAndDateRange =
                GetLessonsWithStudentsByStudentAndDateRange(lessonDao),
            addLesson = AddLesson(billingRepo),
            addGroupLesson = AddGroupLesson(billingRepo),
            updateLesson = UpdateLesson(billingRepo),
            deleteLesson = DeleteLesson(lessonDao),
            updateLessonPaidStatus = UpdateLessonPaidStatus(lessonDao),
            updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(lessonDao),
            isLessonInvoiced = IsLessonInvoiced(lessonDao)
        )
        val homeVm = HomeMenuViewModel(studentUseCases, lessonUseCases, FakeUserProvider(1L))
        composeRule.setContent {
            val showSettings = remember { mutableStateOf(false) }
            if (showSettings.value) {
                SettingsScreen(
                    openDrawer = { showSettings.value = false },
                    onPrivacyPolicy = {},
                    onLogin = {},
                    onRegister = {},
                    onLogout = {},
                    onSwitchAccount = {},
                    onProfile = {},
                    viewModel = settingsVm
                )
            } else {
                HomeMenuScreen(
                    onNavigateToStudent = {},
                    onClassesClick = {},
                    onNavigateToGroups = {},
                    onNavigateToLesson = {},
                    onNavigateToNewStudent = {},
                    onNavigateToNewLesson = {},
                    onRevenue = {},
                    onSettings = { showSettings.value = true },
                    onOpenDrawer = { },
                    viewModel = homeVm
                )
            }
        }
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Edit Profile").assertExists()
        composeRule.onNodeWithText("Logout").assertExists()
        composeRule.onNodeWithText("Switch account").assertExists()
    }

    @Test
    fun loginUpdatesSettingsScreen() {
        val settingsVm = SettingsViewModel(settingsRepo, prefs, userUseCases, backupRepo)
        composeRule.setContent {
            val screen = remember { mutableStateOf("login") }
            if (screen.value == "settings") {
                SettingsScreen(
                    openDrawer = { screen.value = "login" },
                    onPrivacyPolicy = {},
                    onLogin = { screen.value = "login" },
                    onRegister = {},
                    onLogout = {},
                    onSwitchAccount = {},
                    onProfile = {},
                    viewModel = settingsVm
                )
            } else {
                LoginScreen(
                    onBack = {},
                    onLoggedIn = { screen.value = "settings" },
                    onResetPassword = {},
                    onSettings = { screen.value = "settings" },
                    viewModel = loginViewModel
                )
            }
        }

        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Sign In").assertExists()
        composeRule.onNodeWithText("Sign In").performClick()
        composeRule.onNodeWithText("Username").performTextInput("bob")
        composeRule.onNodeWithText("Password").performTextInput("pass")
        composeRule.onNodeWithText("Login").performClick()
        composeRule.waitUntil {
            composeRule.onAllNodesWithText("Edit Profile").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Edit Profile").assertExists()
    }
}

