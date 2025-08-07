package gr.eduinvoice.domain.lesson

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.concurrency.ConcurrencyController
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.domain.testinfrastructure.createMockConcurrencyController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Using shared mock from test infrastructure */

@RunWith(RobolectricTestRunner::class)
class GetStudentLessonsTest {

    private lateinit var db: EduInvoiceDatabase
    private lateinit var repository: TutorBillingRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val mockConcurrencyController = createMockConcurrencyController()
        repository = TutorBillingRepository(db.studentDao(), db.lessonDao(), db.groupDao(), mockConcurrencyController)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun returnsLessonsForStudent() = runBlocking {
        val userId = 1L
        val studentId = db.studentDao().insert(
            Student(ownerId = userId, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        )

        val lesson1 = Lesson(ownerId = userId, studentId = studentId, date = "2024-01-05", startTime = "10:00", durationMinutes = 60)
        val lesson2 = Lesson(ownerId = userId, studentId = studentId, date = "2024-01-06", startTime = "11:00", durationMinutes = 90)
        db.lessonDao().insert(lesson1)
        db.lessonDao().insert(lesson2)

        val useCase = GetStudentLessons(repository)
        val lessons = useCase(studentId, userId).first()

        assertEquals(2, lessons.size)
        assertEquals("2024-01-05", lessons[0].date)
        assertEquals("2024-01-06", lessons[1].date)
    }
}
