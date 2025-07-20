package gr.tsambala.tutorbilling.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.tsambala.tutorbilling.data.dao.StudentDao
import gr.tsambala.tutorbilling.data.database.TutorBillingDatabase
import gr.tsambala.tutorbilling.data.model.Student
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StudentDaoTest {

    private lateinit var db: TutorBillingDatabase
    private lateinit var dao: StudentDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TutorBillingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.studentDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndQueryStudent() = runBlocking {
        val id = dao.insert(Student(name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0))
        val students = dao.getAllActiveStudents().first()
        assertEquals(1, students.size)
        assertEquals(id, students.first().id)
    }

    @Test
    fun softDeleteAndRestoreStudent() = runBlocking {
        val id = dao.insert(Student(name = "Bob", surname = "", parentMobile = "", className = "", rate = 12.0))
        dao.softDeleteStudent(id)
        val archived = dao.getArchivedStudents().first()
        assertEquals(1, archived.size)
        dao.restoreStudent(id)
        val active = dao.getAllActiveStudents().first()
        assertEquals(id, active.first().id)
    }

    @Test
    fun getStudentByIdAnyReturnsArchived() = runBlocking {
        val id = dao.insert(Student(name = "Carl", surname = "", parentMobile = "", className = "", rate = 15.0))
        dao.softDeleteStudent(id)
        val student = dao.getStudentByIdAny(id).first()
        assertNotNull(student)
        assertEquals(false, student?.isActive)
    }
}
