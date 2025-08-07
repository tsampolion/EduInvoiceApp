package gr.eduinvoice.domain.lesson

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.concurrency.ConcurrencyController
import gr.eduinvoice.data.concurrency.ConcurrencyStats
import gr.eduinvoice.data.concurrency.HealthCheckResult
import gr.eduinvoice.data.concurrency.OperationType
import gr.eduinvoice.data.concurrency.OperationPriority
import gr.eduinvoice.data.concurrency.TransactionIsolationLevel
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.repository.TutorBillingRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Creates a mock ConcurrencyController for testing
 */
fun createMockConcurrencyController(): ConcurrencyController {
    return mockk<ConcurrencyController>(relaxed = true) {
        coEvery { 
            executeSafeOperation(any(), any(), any(), any(), any(), any()) 
        } answers {
            val operation = firstArg<suspend () -> Any>()
            try {
                kotlin.Result.success(operation())
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
        
        coEvery { 
            executeReadOnlyOperation(any(), any()) 
        } answers {
            val operation = firstArg<suspend () -> Any>()
            try {
                kotlin.Result.success(operation())
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
        
        coEvery { 
            executeBatchSafeOperations(any(), any(), any(), any(), any()) 
        } answers {
            val operations = firstArg<List<suspend () -> Any>>()
            try {
                val results = mutableListOf<Any>()
                for (operation in operations) {
                    results.add(operation())
                }
                kotlin.Result.success(results)
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
        
        coEvery { performHealthCheck() } returns HealthCheckResult(isHealthy = true)
        every { getConcurrencyStatistics() } returns ConcurrencyStats()
        every { getActiveResourceLocks() } returns emptySet()
        coEvery { releaseAllResourceLocks() } returns Unit
        coEvery { emergencyCleanup() } returns Unit
    }
}

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
        val mockConcurrencyController = createMockConcurrencyController()
        repository = TutorBillingRepository(db.studentDao(), db.lessonDao(), db.groupDao(), mockConcurrencyController)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addsLessonSuccessfully() = runBlocking {
        val userId = 1L
        val studentId = db.studentDao().insert(
            Student(ownerId = userId, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        )

        val lesson = Lesson(ownerId = userId, studentId = studentId, date = "2024-01-05", startTime = "10:00", durationMinutes = 60)
        val useCase = AddLesson(repository)
        val lessonId = useCase(lesson, userId)

        val savedLesson = db.lessonDao().getLessonById(lessonId, userId).first()
        assertEquals(lesson.date, savedLesson.date)
        assertEquals(lesson.startTime, savedLesson.startTime)
        assertEquals(lesson.durationMinutes, savedLesson.durationMinutes)
    }
}
