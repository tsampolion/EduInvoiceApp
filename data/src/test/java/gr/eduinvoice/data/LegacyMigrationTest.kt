package gr.eduinvoice.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.DatabaseConstants
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.database.MIGRATION_12_13
import gr.eduinvoice.data.di.DatabaseModule
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.user.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LegacyMigrationTest {
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
    fun migratesPlainDatabase() = runBlocking {
        val plainDb = Room.databaseBuilder(
            context,
            EduInvoiceDatabase::class.java,
            DatabaseConstants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(false)
            .addMigrations(MIGRATION_12_13)
            .build()
        plainDb.studentDao().insert(
            Student(name = "Legacy", surname = "", parentMobile = "", className = "A", rate = 10.0)
        )
        plainDb.close()

        val db = DatabaseModule.provideEduInvoiceDatabase(context, prefs)
        val students = db.studentDao().getAllActiveStudents(0).first()
        assertEquals(1, students.size)
        assertEquals("Legacy", students.first().name)
        db.close()
    }
}
