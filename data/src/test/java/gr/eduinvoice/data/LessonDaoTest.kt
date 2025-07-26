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
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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
        lessonDao.updatePaidStatus(listOf(id), true)
        val lesson = lessonDao.getLessonById(id, 0).first()
        assertEquals(true, lesson?.isPaid)
    }

    @Test
    fun updateLessonInvoicedStatus() = runBlocking {
        val studentId = studentDao.insert(Student(name = "Cara", surname = "", parentMobile = "", className = "C", rate = 12.0))
        val id = lessonDao.insert(Lesson(studentId = studentId, date = "2024-01-03", startTime = "12:00", durationMinutes = 60))
        lessonDao.updateInvoicedStatus(listOf(id), true)
        val invoiced = lessonDao.isLessonInvoiced(id, 0).first()
        assertEquals(true, invoiced)
    }
}
