package gr.eduinvoice.data.user

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserPreferencesRepositoryTest {
    private lateinit var repo: UserPreferencesRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repo = UserPreferencesRepository(context)
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
        assertEquals("eduinvoice", default)
        repo.setDbPassphrase("secret")
        assertEquals("secret", repo.getDbPassphrase())
    }
}
