package gr.eduinvoice.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.DatabaseConstants
import gr.eduinvoice.data.di.DatabaseModule
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.data.user.userPrefsDataStore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.data.BouncyCastleTestRunner
import org.junit.Assert.assertTrue

@RunWith(BouncyCastleTestRunner::class)
class DatabaseProviderRecoveryTest : TestBase() {
    private lateinit var context: Context
    private lateinit var prefs: UserPreferencesRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        prefs = UserPreferencesRepository(context, context.userPrefsDataStore)
        runBlocking { prefs.setDbPassphrase("secret") }
    }

    @After
    fun tearDown() {
        context.deleteDatabase(DatabaseConstants.DATABASE_NAME)
    }

    @Test
    fun recreatesBadDbOnProviderInvocation() = runBlocking {
        val dbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
        dbFile.parentFile?.mkdirs()
        dbFile.writeBytes(ByteArray(32) { 3 })
        val originalSize = dbFile.length()

        val db = DatabaseModule.provideEduInvoiceDatabase(context, prefs)
        assertTrue(dbFile.length() != originalSize)
        db.close()
    }
}
