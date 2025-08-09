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
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.data.dao.*
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.testfixtures.TestDbFactory
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
import gr.eduinvoice.testinfrastructure.AndroidTestInfrastructure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import gr.eduinvoice.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenFlowTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var prefs: UserPreferencesRepository
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var backupRepo: BackupRepository
    private lateinit var userUseCases: UserUseCases
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var db: EduInvoiceDatabase
    private lateinit var userDao: UserDao
    private lateinit var studentDao: StudentDao
    private lateinit var lessonDao: LessonDao
    private lateinit var groupDao: GroupDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = TestDbFactory.createInMemory(ctx)
        userDao = db.userDao()
        studentDao = db.studentDao()
        lessonDao = db.lessonDao()
        groupDao = db.groupDao()
        
        // Insert test user
        runBlocking {
            val user = AndroidTestInfrastructure.AndroidTestDataFactory.createTestUser(
                username = "bob",
                fullName = "Bob"
            ).copy(passwordHash = PasswordHasher.hash("pass"))
            userDao.insert(user)
        }
        
        val userRepo = UserRepository(userDao)
        userUseCases = UserUseCases(
            createUser = CreateUser(userRepo),
            authenticateUser = AuthenticateUser(userRepo),
            getUserProfile = GetUserProfile(userRepo),
            updateUser = UpdateUser(userRepo),
            resetPassword = ResetPassword(userRepo)
        )
        prefs = UserPreferencesRepository(ctx, ctx.userPrefsDataStore)
        settingsRepo = SettingsRepository(ctx)
        backupRepo = BackupRepository(ctx, db)
        loginViewModel = LoginViewModel(userUseCases, prefs, ctx)
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

