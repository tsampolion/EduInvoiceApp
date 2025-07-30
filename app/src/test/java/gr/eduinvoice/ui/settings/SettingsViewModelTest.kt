package gr.eduinvoice.ui.settings

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.MainDispatcherRule
import gr.eduinvoice.data.dao.UserDao
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.BackupRepository
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.data.settings.SettingsRepository
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.domain.user.UserUseCases
import gr.eduinvoice.domain.user.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    private lateinit var db: EduInvoiceDatabase
    private lateinit var backupRepo: BackupRepository
    private lateinit var prefs: UserPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backupRepo = BackupRepository(db)
        prefs = UserPreferencesRepository(context)
        val userDao = object : UserDao {
            override suspend fun insert(user: User) = 0L
            override suspend fun update(user: User) {}
            override suspend fun delete(user: User) {}
            override fun getUserById(id: Long): Flow<User?> = flowOf(null)
            override suspend fun getByUsername(username: String): User? = null
        }
        val userRepo = UserRepository(userDao)
        val userUseCases = UserUseCases(
            createUser = CreateUser(userRepo),
            authenticateUser = AuthenticateUser(userRepo),
            getUserProfile = GetUserProfile(userRepo),
            updateUser = UpdateUser(userRepo),
            resetPassword = ResetPassword(userRepo)
        )
        val settingsRepo = SettingsRepository(context)
        viewModel = SettingsViewModel(settingsRepo, prefs, userUseCases, backupRepo)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun restoreBackupInvalidJsonEmitsError() = runTest {
        viewModel.restoreBackup("{invalid")
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun restoreBackupDatabaseErrorEmitsError() = runTest {
        val dump = BackupRepository.BackupDump(
            students = listOf(
                gr.eduinvoice.data.model.Student(id = 1, name = "A", surname = "", parentMobile = "", className = "A", rate = 10.0),
                gr.eduinvoice.data.model.Student(id = 1, name = "B", surname = "", parentMobile = "", className = "A", rate = 10.0)
            ),
            lessons = emptyList(),
            groups = emptyList(),
            crossRefs = emptyList(),
            users = emptyList()
        )
        val json = Json.encodeToString(BackupRepository.BackupDump.serializer(), dump)
        viewModel.restoreBackup(json)
        advanceUntilIdle()
        assertTrue(viewModel.errorMessage.value?.contains("Database") == true)
    }
}
