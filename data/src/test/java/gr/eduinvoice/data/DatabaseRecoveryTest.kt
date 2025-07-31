package gr.eduinvoice.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.DatabaseConstants
import gr.eduinvoice.data.di.DatabaseModule
import gr.eduinvoice.data.user.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.assertTrue

@RunWith(RobolectricTestRunner::class)
class DatabaseRecoveryTest {
    private lateinit var context: Context
    private lateinit var prefs: UserPreferencesRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        prefs = UserPreferencesRepository(context)
        runBlocking { prefs.setDbPassphrase("secret") }
    }

    @After
    fun tearDown() {
        context.deleteDatabase(DatabaseConstants.DATABASE_NAME)
    }

    @Test
    fun recreatesCorruptDatabaseFile() = runBlocking {
        val dbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
        dbFile.parentFile?.mkdirs()
        dbFile.writeBytes(ByteArray(32) { 1 })
        val originalSize = dbFile.length()

        val db = DatabaseModule.provideEduInvoiceDatabase(context, prefs)
        val students = db.studentDao().getAllActiveStudents(0).first()
        assertTrue(students.isEmpty())
        assertTrue(dbFile.length() != originalSize)
        db.close()
    }
}
