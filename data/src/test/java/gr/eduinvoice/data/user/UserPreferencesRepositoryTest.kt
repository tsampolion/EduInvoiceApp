package gr.eduinvoice.data.user

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class UserPreferencesRepositoryTest {
    private lateinit var repo: UserPreferencesRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repo = UserPreferencesRepository(context, context.userPrefsDataStore)
    }

    @Test
    fun loginAndLogoutFlow() = runBlocking {
        repo.setLoggedInUser(5)
        assertEquals(5L, repo.loggedInUserId.first())
        repo.setLoggedInUser(null)
        assertEquals(null, repo.loggedInUserId.first())
    }

    @Test
    fun databasePassphraseDefault() = runBlocking {
        val default = repo.getDbPassphrase()
        assertTrue(default.isNotEmpty())
        repo.setDbPassphrase("secret")
        assertEquals("secret", repo.getDbPassphrase())
    }

    @Test
    fun databasePassphraseIoErrorFallsBack() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val failingStore = object : DataStore<Preferences> {
            override val data = flow<Preferences> { throw IOException("boom") }
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences = emptyPreferences()
        }
        val repo = UserPreferencesRepository(context, failingStore)
        val pass = repo.getDbPassphrase()
        assertTrue(pass.isNotEmpty())
    }
}
