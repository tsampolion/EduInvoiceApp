package gr.eduinvoice.domain.lesson

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.test.support.fakes.NoopConcurrencyController
import gr.eduinvoice.test.support.extensions.createTestStudent
import gr.eduinvoice.test.support.extensions.createTestLesson
import gr.eduinvoice.test.support.extensions.createTestGroup
import gr.eduinvoice.test.support.extensions.createTestCrossRef
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
class AddGroupLessonIntegrationTest {

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
    fun addsLessonForEachStudentInGroup() = runBlocking {
        val userId = 1L
        val s1 = db.studentDao().insert(
            createTestStudent(ownerId = userId, name = "Alice", rate = 10.0)
        )
        val s2 = db.studentDao().insert(
            createTestStudent(ownerId = userId, name = "Bob", rate = 15.0)
        )
        val gId = db.groupDao().insertGroup(createTestGroup(ownerId = userId, name = "Group A"))
        db.groupDao().insertCrossRef(createTestCrossRef(groupId = gId, studentId = s1, ownerId = userId))
        db.groupDao().insertCrossRef(createTestCrossRef(groupId = gId, studentId = s2, ownerId = userId))

        val lesson = createTestLesson(studentId = 0, ownerId = userId, date = "2024-01-05", durationMinutes = 60)
        val useCase = AddGroupLesson(repository)
        useCase(gId, lesson, userId)

        val l1 = db.lessonDao().getLessonsByStudentId(s1, userId).first()
        val l2 = db.lessonDao().getLessonsByStudentId(s2, userId).first()
        assertEquals(1, l1.size)
        assertEquals(1, l2.size)
        assertEquals(10.0, l1.first().durationMinutes / 60.0 * 10.0, 0.0)
        assertEquals(15.0, l2.first().durationMinutes / 60.0 * 15.0, 0.0)
    }

    @Test
    fun returnsIdsForInsertedLessons() = runBlocking {
        val userId = 1L
        val s1 = db.studentDao().insert(createTestStudent(ownerId = userId, name = "Ann", rate = 12.0))
        val s2 = db.studentDao().insert(createTestStudent(ownerId = userId, name = "Ben", rate = 18.0))
        val s3 = db.studentDao().insert(createTestStudent(ownerId = userId, name = "Cat", rate = 20.0))
        val gId = db.groupDao().insertGroup(createTestGroup(ownerId = userId, name = "Group B"))
        db.groupDao().insertCrossRef(createTestCrossRef(groupId = gId, studentId = s1, ownerId = userId))
        db.groupDao().insertCrossRef(createTestCrossRef(groupId = gId, studentId = s2, ownerId = userId))
        db.groupDao().insertCrossRef(createTestCrossRef(groupId = gId, studentId = s3, ownerId = userId))

        val lesson = createTestLesson(studentId = 0, ownerId = userId, date = "2024-02-01", durationMinutes = 90)
        val useCase = AddGroupLesson(repository)
        val ids = useCase(gId, lesson, userId)

        assertEquals(3, ids.size)
        val lessons = db.lessonDao().getAllLessons(userId).first()
        assertEquals(3, lessons.size)
    }

    @Test
    fun partialFailureRollsBack() = runBlocking {
        val userId = 1L
        val s1 = db.studentDao().insert(createTestStudent(ownerId = userId, name = "Dan", rate = 10.0))
        val s2 = db.studentDao().insert(createTestStudent(ownerId = userId, name = "Eve", rate = 12.0))
        val gId = db.groupDao().insertGroup(createTestGroup(ownerId = userId, name = "Group C"))
        db.groupDao().insertCrossRef(createTestCrossRef(groupId = gId, studentId = s1, ownerId = userId))
        db.groupDao().insertCrossRef(createTestCrossRef(groupId = gId, studentId = s2, ownerId = userId))

        val lesson = createTestLesson(id = 1, studentId = 0, ownerId = userId, date = "2024-03-01", durationMinutes = 60)
        val useCase = AddGroupLesson(repository)

        try {
            useCase(gId, lesson, userId)
            // If we reach here, the test should fail
            throw AssertionError("Expected exception was not thrown")
        } catch (e: Exception) {
            // Expected exception
        }

        val lessons = db.lessonDao().getAllLessons(userId).first()
        assertEquals(0, lessons.size)
    }
}
