package gr.eduinvoice.ui.user

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import gr.eduinvoice.data.dao.UserDao
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.PasswordHasher
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.data.user.userPrefsDataStore
import gr.eduinvoice.domain.user.*
import gr.eduinvoice.data.testfixtures.TestDbFactory
import gr.eduinvoice.testinfrastructure.AndroidTestInfrastructure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var viewModel: LoginViewModel
    private lateinit var userDao: UserDao

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = TestDbFactory.createInMemory(context)
        userDao = db.userDao()
        
        // Insert test user
        runBlocking {
            val user = AndroidTestInfrastructure.AndroidTestDataFactory.createTestUser(
                username = "bob",
                fullName = "Bob"
            ).copy(passwordHash = PasswordHasher.hash("pass"))
            userDao.insert(user)
        }
        
        val repo = UserRepository(userDao)
        val useCases = UserUseCases(
            createUser = CreateUser(repo),
            authenticateUser = AuthenticateUser(repo),
            getUserProfile = GetUserProfile(repo),
            updateUser = UpdateUser(repo),
            resetPassword = ResetPassword(repo)
        )
        val prefs = UserPreferencesRepository(context, context.userPrefsDataStore)
        viewModel = LoginViewModel(useCases, prefs, context)
    }

    @Test
    fun successfulLoginCallsCallback() {
        var loggedIn = false
        composeRule.setContent {
            LoginScreen(
                onBack = {},
                onLoggedIn = { loggedIn = true },
                onResetPassword = {},
                onSettings = {},
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithText("Username").performTextInput("bob")
        composeRule.onNodeWithText("Password").performTextInput("pass")
        composeRule.onNodeWithText("Login").performClick()

        composeRule.waitUntil { loggedIn }
    }
}
