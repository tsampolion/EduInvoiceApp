package gr.tutorbilling.domain.lesson

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.tutorbilling.data.database.EduInvoiceDatabase
import gr.tutorbilling.data.model.Lesson
import gr.tutorbilling.data.model.Student
import gr.tutorbilling.data.repository.EduInvoiceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GetStudentLessonsTest {

    private lateinit var db: EduInvoiceDatabase
    private lateinit var repository: EduInvoiceRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = EduInvoiceRepository(db.studentDao(), db.lessonDao(), db.groupDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun returnsLessonsForStudent() = runBlocking {
        val studentId = db.studentDao().insert(Student(name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0))
        db.lessonDao().insert(Lesson(studentId = studentId, date = "2024-01-01", startTime = "10:00", durationMinutes = 60))
        val useCase = GetStudentLessons(repository)
        val lessons = useCase(studentId).first()
        assertEquals(1, lessons.size)
    }
}
