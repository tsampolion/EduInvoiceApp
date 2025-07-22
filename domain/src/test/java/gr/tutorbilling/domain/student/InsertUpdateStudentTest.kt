package gr.tutorbilling.domain.student

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.tutorbilling.data.database.EduInvoiceDatabase
import gr.tutorbilling.data.model.Student
import gr.tutorbilling.data.repository.StudentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InsertUpdateStudentTest {

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
    fun insertAndUpdateStudent() = runBlocking {
        val insert = InsertStudent(repository)
        val update = UpdateStudent(repository)
        val id = insert(Student(name = "Alice", surname = "", parentMobile = "", className = "A", rate = 10.0))
        update(Student(id = id, name = "Alice", surname = "", parentMobile = "", className = "B", rate = 12.0))
        val student = db.studentDao().getStudentByIdAny(id).first()
        assertEquals("B", student?.className)
        assertEquals(12.0, student?.rate ?: 0.0, 0.0)
    }
}
