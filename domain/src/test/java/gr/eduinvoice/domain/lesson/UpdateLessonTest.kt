package gr.eduinvoice.domain.lesson

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.repository.TutorBillingRepository
// TODO: Move to module-local fixtures (Option A)
// import gr.eduinvoice.test.support.fakes.NoopConcurrencyController
// import gr.eduinvoice.test.support.extensions.createTestStudent
// import gr.eduinvoice.test.support.extensions.createTestLesson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Using new test support infrastructure */

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
        val concurrencyController = NoopConcurrencyController()
        repository = TutorBillingRepository(db.studentDao(), db.lessonDao(), db.groupDao(), concurrencyController)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun updatesLessonSuccessfully() = runBlocking {
        val userId = 1L
        val student = createTestStudent(ownerId = userId, name = "Alice", rate = 10.0)
        val studentId = db.studentDao().insert(student)

        val lesson = createTestLesson(studentId = studentId, ownerId = userId, date = "2024-01-05", durationMinutes = 60)
        val lessonId = db.lessonDao().insert(lesson)

        val updatedLesson = lesson.copy(
            id = lessonId,
            date = "2024-01-06",
            startTime = "11:00",
            durationMinutes = 90
        )

        val useCase = UpdateLesson(repository)
        useCase(updatedLesson)

        val savedLesson = db.lessonDao().getLessonById(lessonId, userId).first()
        assertEquals("2024-01-06", savedLesson?.date)
        assertEquals("11:00", savedLesson?.startTime)
        assertEquals(90, savedLesson?.durationMinutes)
    }
}
