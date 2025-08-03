package gr.eduinvoice.ui.user

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.R
import gr.eduinvoice.MainDispatcherRule
import gr.eduinvoice.data.dao.UserDao
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.data.user.userPrefsDataStore
import gr.eduinvoice.domain.user.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner

@RunWith(BouncyCastleTestRunner::class)
class LoginViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val storedUser = User(
        id = 1,
        username = "bob",
        passwordHash = gr.eduinvoice.data.repository.PasswordHasher.hash("pass"),
        fullName = "Bob"
    )

    private val userDao = object : UserDao {
        override suspend fun insert(user: User) = 0L
        override suspend fun update(user: User) {}
        override suspend fun delete(user: User) {}
        override fun getUserById(id: Long): Flow<User?> = flowOf(storedUser.takeIf { it.id == id })
        override suspend fun getByUsername(username: String): User? = storedUser.takeIf { it.username == username }
    }
    private val userRepo = UserRepository(userDao)
    private val useCases = UserUseCases(
        createUser = CreateUser(userRepo),
        authenticateUser = AuthenticateUser(userRepo),
        getUserProfile = GetUserProfile(userRepo),
        updateUser = UpdateUser(userRepo),
        resetPassword = ResetPassword(userRepo)
    )
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val prefs = UserPreferencesRepository(context, context.userPrefsDataStore)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulLoginUpdatesPrefs() = runTest {
        val vm = LoginViewModel(useCases, prefs, context)
        vm.updateUsername("bob")
        vm.updatePassword("pass")
        var callbackId: Long? = null
        vm.login { callbackId = it }
        advanceUntilIdle()
        assertEquals(1L, prefs.loggedInUserId.first())
        assertEquals(1L, callbackId)
        assertNull(vm.uiState.value.error)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun failedLoginSetsError() = runTest {
        val vm = LoginViewModel(useCases, prefs, context)
        vm.updateUsername("bob")
        vm.updatePassword("wrong")
        var called = false
        vm.login { called = true }
        advanceUntilIdle()
        assertNull(prefs.loggedInUserId.first())
        assertEquals(false, called)
        assertEquals(context.getString(R.string.error_invalid_password), vm.uiState.value.error)
    }
}
