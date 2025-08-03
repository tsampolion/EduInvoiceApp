package gr.eduinvoice.ui.user

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner

@RunWith(BouncyCastleTestRunner::class)
class RegisterViewModelTest : gr.eduinvoice.TestBase() {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    private class FakeUserDao : UserDao {
        val users = mutableListOf<User>()
        override suspend fun insert(user: User): Long {
            if (users.any { it.username == user.username }) {
                throw SQLiteConstraintException("username exists")
            }
            users += user
            return (users.indexOf(user) + 1).toLong()
        }
        override suspend fun update(user: User) {}
        override suspend fun delete(user: User) {}
        override fun getUserById(id: Long): Flow<User?> = flowOf(users.find { it.id == id })
        override suspend fun getByUsername(username: String): User? = users.find { it.username == username }
    }

    private val userDao = FakeUserDao()
    private val repo = UserRepository(userDao)
    private val useCases = UserUseCases(
        createUser = CreateUser(repo),
        authenticateUser = AuthenticateUser(repo),
        getUserProfile = GetUserProfile(repo),
        updateUser = UpdateUser(repo),
        resetPassword = ResetPassword(repo)
    )
    private val context = ApplicationProvider.getApplicationContext<Context>()
    // Use a mock DataStore for tests to avoid file system issues
    private val mockDataStore = object : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
        private val prefs = MutableStateFlow(androidx.datastore.preferences.core.emptyPreferences())
        override val data: kotlinx.coroutines.flow.Flow<androidx.datastore.preferences.core.Preferences> = prefs.asStateFlow()
        override suspend fun updateData(transform: suspend (androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences): androidx.datastore.preferences.core.Preferences {
            val newPrefs = transform(prefs.value)
            prefs.value = newPrefs
            return newPrefs
        }
    }
    private val prefs = UserPreferencesRepository(context, mockDataStore)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulRegistrationUpdatesPrefs() = runTest {
        val vm = RegisterViewModel(useCases, prefs)
        vm.updateUsername("alice")
        vm.updatePassword("secret")
        vm.updateFullName("Alice")
        vm.updateSubjectSpecialty("Math")
        vm.updateYearsExperience("0-5")
        var called = false
        vm.register { called = true }
        advanceUntilIdle()
        assertEquals(1L, prefs.loggedInUserId.first())
        assertEquals(true, called)
        assertNull(vm.uiState.value.error)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateYearsExperienceParsesRange() = runTest {
        val vm = RegisterViewModel(useCases, prefs)
        vm.updateYearsExperience("11-15")
        assertEquals(11, vm.uiState.value.yearsExperience)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun duplicateUsernameShowsError() = runTest {
        userDao.users += User(
            id = 1,
            username = "bob",
            passwordHash = "x",
            fullName = "Bob",
            subjectSpecialty = "Math",
            yearsExperience = 2
        )
        val vm = RegisterViewModel(useCases, prefs)
        vm.updateUsername("bob")
        vm.updatePassword("secret")
        vm.updateFullName("Bob")
        vm.updateSubjectSpecialty("Math")
        vm.updateYearsExperience("6-10")
        vm.register {}
        advanceUntilIdle()
        assertNull(prefs.loggedInUserId.first())
        assertEquals("Username already exists", vm.uiState.value.error)
    }
}
