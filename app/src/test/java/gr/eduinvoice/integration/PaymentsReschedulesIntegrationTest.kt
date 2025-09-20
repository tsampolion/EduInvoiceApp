package gr.eduinvoice.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.PaymentBatchMaster
import gr.eduinvoice.data.model.RescheduleMaster
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentsReschedulesIntegrationTest {

    @get:Rule
    val databaseContainer = TestDatabaseContainer()

    @Test
    fun batch_payment_and_reschedule_flows_execute() = runBlocking {
        val db = databaseContainer.database
        val dao = db.lessonDao()

        // Insert sample lessons for a student
        val userId = 1L
        val studentId = 10L
        val ids = (1..5).map { i ->
            dao.insert(
                Lesson(
                    ownerId = userId,
                    studentId = studentId,
                    date = "2025-01-0${(i % 9) + 1}",
                    startTime = "10:0${i % 6}",
                    durationMinutes = 60,
                    notes = null,
                    isPaid = false,
                    isInvoiced = false
                )
            )
        }

        // Create a payment batch and mark lessons as paid
        val batchId = dao.insertPaymentBatchMaster(
            PaymentBatchMaster(
                ownerId = userId,
                studentId = studentId,
                batchDate = "2025-01-15",
                notes = "Batch payment"
            )
        )

        dao.updatePaidStatus(ids, true, userId)

        // Reschedule - create a master and update lessons by group/time (simulate group=1)
        val rescheduleId = dao.insertRescheduleMaster(
            RescheduleMaster(
                ownerId = userId,
                title = "Reschedule",
                newDate = "2025-01-20",
                newStartTime = "11:00",
                newDurationMinutes = 60,
                notes = null
            )
        )

        // Verify flags updated
        val locked = dao.countLockedLessons(ids, userId)
        assertEquals("All lessons should be marked paid", ids.size, locked)

        // Attach a lesson to reschedule and ensure link stores
        dao.attachLessonToReschedule(rescheduleId, ids.first())
        val linked = dao.getRescheduledLessonIds(rescheduleId)
        assertTrue(linked.contains(ids.first()))
    }
}

