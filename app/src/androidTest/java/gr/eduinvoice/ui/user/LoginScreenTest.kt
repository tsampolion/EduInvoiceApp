package gr.eduinvoice.ui.user

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import gr.eduinvoice.data.dao.UserDao
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.PasswordHasher
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.domain.user.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
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
            override suspend fun getByUsername(username: String): User? = if (username == user.username) user else null
        }
        val repo = UserRepository(dao)
        val useCases = UserUseCases(
            createUser = CreateUser(repo),
            authenticateUser = AuthenticateUser(repo),
            getUserProfile = GetUserProfile(repo),
            updateUser = UpdateUser(repo),
            resetPassword = ResetPassword(repo)
        )
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = UserPreferencesRepository(context)
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
