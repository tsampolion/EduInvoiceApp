package gr.eduinvoice.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UnpaidLessonsDateRangeTest {

	@get:Rule
	val databaseContainer = TestDatabaseContainer()

	@Test
	fun dateRange_unpaid_queries_return_expected_rows() = runBlocking {
		val db = databaseContainer.database
		val lessonDao: LessonDao = db.lessonDao()
		val studentDao = db.studentDao()

		val userId = 1L
		val studentId = studentDao.insertStudent(
			Student(
				ownerId = userId,
				name = "Alice",
				surname = "A",
				parentMobile = "",
				className = "A1",
				rate = 10.0,
				rateType = "per_lesson"
			)
		)

		// Insert unpaid lessons across a range and some paid to ensure filter works
		val dates = listOf("2025-02-01", "2025-02-05", "2025-02-10", "2025-03-01")
		val ids = dates.mapIndexed { idx, d ->
			lessonDao.insert(
				Lesson(
					ownerId = userId,
					studentId = studentId,
					date = d,
					startTime = "09:00",
					durationMinutes = 60,
					isPaid = idx % 2 == 0, // mark some as paid
					isInvoiced = false,
					notes = null
				)
			)
		}

		val unpaidFeb = lessonDao.getUnpaidLessonsInDateRange("2025-02-01", "2025-02-28", userId).first()
		assertTrue(unpaidFeb.all { it.isPaid == false })
		assertEquals(1, unpaidFeb.size) // Only 2025-02-05 expected unpaid in Feb

		val unpaidStudentFeb = lessonDao.getUnpaidLessonsByStudentAndDateRange(studentId, "2025-02-01", "2025-02-28", userId).first()
		assertEquals(unpaidFeb.map { it.id }.toSet(), unpaidStudentFeb.map { it.id }.toSet())
	}
}

