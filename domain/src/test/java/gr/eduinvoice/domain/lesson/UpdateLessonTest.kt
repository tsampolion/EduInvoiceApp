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
class UpdateLessonTest {

    private lateinit var db: EduInvoiceDatabase
    private lateinit var repository: TutorBillingRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TutorBillingRepository(
            db.studentDao(),
            db.lessonDao(),
            db.groupDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun updatesLesson() = runBlocking {
        val studentId = db.studentDao().insert(Student(name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0))
        val id = db.lessonDao().insert(Lesson(studentId = studentId, date = "2024-01-04", startTime = "10:00", durationMinutes = 60))
        val useCase = UpdateLesson(repository)
        useCase(Lesson(id = id, studentId = studentId, date = "2024-01-04", startTime = "10:00", durationMinutes = 90))
        val lesson = db.lessonDao().getLessonById(id).first()
        assertEquals(90, lesson?.durationMinutes)
    }
}
