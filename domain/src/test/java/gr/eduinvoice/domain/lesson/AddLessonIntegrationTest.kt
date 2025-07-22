package gr.eduinvoice.domain.lesson

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.repository.TutorBillingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddLessonIntegrationTest {

    private lateinit var db: EduInvoiceDatabase
    private lateinit var repository: TutorBillingRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TutorBillingRepository(db.studentDao(), db.lessonDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addsLessonToDatabase() = runBlocking {
        val studentId = db.studentDao().insert(Student(name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0))
        val lesson = Lesson(studentId = studentId, date = "2024-01-03", startTime = "10:00", durationMinutes = 60)
        val useCase = AddLesson(repository)
        useCase(lesson)
        val lessons = db.lessonDao().getLessonsByStudentId(studentId).first()
        assertEquals(1, lessons.size)
    }
}
