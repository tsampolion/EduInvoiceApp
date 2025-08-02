package gr.eduinvoice.domain.student

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.repository.StudentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ArchiveRestoreStudentTest {

    private lateinit var db: EduInvoiceDatabase
    private lateinit var repository: StudentRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = StudentRepository(db.studentDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun archiveAndRestoreStudent() = runBlocking {
        val insert = InsertStudent(repository)
        val archive = SoftDeleteStudent(repository)
        val restore = RestoreStudent(repository)
        val id = insert(Student(name = "Bob", surname = "", parentMobile = "", className = "B", rate = 15.0))
        archive(id, 0)
        val archived = db.studentDao().getArchivedStudents(0).first()
        assertEquals(1, archived.size)
        restore(id)
        val active = db.studentDao().getAllActiveStudents(0).first()
        assertEquals(id, active.first().id)
    }
}
