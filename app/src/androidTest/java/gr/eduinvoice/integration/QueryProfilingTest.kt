package gr.eduinvoice.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueryProfilingTest {

	@get:Rule
	val databaseContainer = TestDatabaseContainer()

	@Test
	fun unpaid_date_range_query_is_fast_enough() = runBlocking {
		val db = databaseContainer.database
		val lessonDao: LessonDao = db.lessonDao()
		val studentDao = db.studentDao()

		val userId = 1L
		val studentId = studentDao.insertStudent(
			Student(ownerId = userId, name = "Bob", surname = "B", parentMobile = "", className = "B1", rate = 12.0, rateType = "per_lesson")
		)

		// Insert a moderate dataset
		repeat(500) { i ->
			val day = (i % 28) + 1
			val date = "2025-02-%02d".format(day)
			lessonDao.insert(
				Lesson(
					ownerId = userId,
					studentId = studentId,
					date = date,
					startTime = "10:00",
					durationMinutes = 60,
					isPaid = (i % 3 == 0),
					isInvoiced = false,
					notes = null
				)
			)
		}

		val start = System.nanoTime()
		val results = lessonDao.getUnpaidLessonsInDateRange("2025-02-01", "2025-02-28", userId).first()
		val elapsedMs = (System.nanoTime() - start) / 1_000_000

		// Basic sanity: query should complete quickly in test env (< 200ms typically)
		assertTrue("Query took ${elapsedMs}ms, expected < 500ms", elapsedMs < 500)
	}
}

