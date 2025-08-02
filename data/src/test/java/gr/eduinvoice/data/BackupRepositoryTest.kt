package gr.eduinvoice.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.repository.BackupRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BackupRepositoryTest {
    private lateinit var db: EduInvoiceDatabase
    private lateinit var repo: BackupRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = BackupRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun exportAndRestoreRoundTrip() = runBlocking {
        db.studentDao().insert(Student(name = "Test", surname = "", parentMobile = "", className = "A", rate = 10.0))
        val json = repo.exportJson()
        db.clearAllTables()
        val result = repo.restoreFromJson(json)
        assert(result.isSuccess)
        val students = db.studentDao().getAllActiveStudents(0).first()
        assertEquals(1, students.size)
        assertEquals("Test", students.first().name)
    }

    @Test
    fun restoreFailsOnInvalidJson() = runBlocking {
        val result = repo.restoreFromJson("{}")
        assert(result.isFailure)
    }

    @Test
    fun restoreFailsOnDatabaseError() = runBlocking {
        val json = """{"students":[],"lessons":[],"groups":[],"crossRefs":[],"users":[]}"""
        db.close()
        val result = repo.restoreFromJson(json)
        assert(result.isFailure)
    }
}
