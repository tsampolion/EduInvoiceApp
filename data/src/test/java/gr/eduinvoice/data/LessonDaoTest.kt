package gr.eduinvoice.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.data.BouncyCastleTestRunner

@RunWith(BouncyCastleTestRunner::class)
class LessonDaoTest {

    private lateinit var db: EduInvoiceDatabase
    private lateinit var lessonDao: LessonDao
    private lateinit var studentDao: StudentDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        lessonDao = db.lessonDao()
        studentDao = db.studentDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndQueryLessonsByStudent() = runBlocking {
        val studentId = studentDao.insert(Student(name = "Alice", surname = "", parentMobile = "", className = "A", rate = 10.0))
        lessonDao.insert(Lesson(studentId = studentId, date = "2024-01-01", startTime = "10:00", durationMinutes = 60))
        val lessons = lessonDao.getLessonsByStudentId(studentId, 0).first()
        assertEquals(1, lessons.size)
        assertEquals(studentId, lessons.first().studentId)
    }

    @Test
    fun updateLessonPaidStatus() = runBlocking {
        val studentId = studentDao.insert(Student(name = "Bob", surname = "", parentMobile = "", className = "B", rate = 15.0))
        val id = lessonDao.insert(Lesson(studentId = studentId, date = "2024-01-02", startTime = "11:00", durationMinutes = 90))
        lessonDao.updatePaidStatus(listOf(id), true, 0)
        val lesson = lessonDao.getLessonById(id, 0).first()
        assertEquals(true, lesson?.isPaid)
    }

    @Test
    fun updateLessonInvoicedStatus() = runBlocking {
        val studentId = studentDao.insert(Student(name = "Cara", surname = "", parentMobile = "", className = "C", rate = 12.0))
        val id = lessonDao.insert(Lesson(studentId = studentId, date = "2024-01-03", startTime = "12:00", durationMinutes = 60))
        lessonDao.updateInvoicedStatus(listOf(id), true, 0)
        val invoiced = lessonDao.isLessonInvoiced(id, 0).first()
        assertEquals(true, invoiced)
    }

    @Test
    fun updatePaidStatusRejectsDifferentUser() = runBlocking {
        val studentId = studentDao.insert(Student(name = "Dan", surname = "", parentMobile = "", className = "D", rate = 14.0))
        val id = lessonDao.insert(Lesson(studentId = studentId, date = "2024-01-04", startTime = "13:00", durationMinutes = 60))
        lessonDao.updatePaidStatus(listOf(id), true, 1)
        val lesson = lessonDao.getLessonById(id, 0).first()
        assertEquals(false, lesson?.isPaid)
    }

    @Test
    fun updateInvoicedStatusRejectsDifferentUser() = runBlocking {
        val studentId = studentDao.insert(Student(name = "Eve", surname = "", parentMobile = "", className = "E", rate = 11.0))
        val id = lessonDao.insert(Lesson(studentId = studentId, date = "2024-01-05", startTime = "14:00", durationMinutes = 60))
        lessonDao.updateInvoicedStatus(listOf(id), true, 1)
        val invoiced = lessonDao.isLessonInvoiced(id, 0).first()
        assertEquals(false, invoiced)
    }
}
