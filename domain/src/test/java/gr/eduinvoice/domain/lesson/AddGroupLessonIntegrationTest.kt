package gr.eduinvoice.domain.lesson

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.GroupStudentCrossRef
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
class AddGroupLessonIntegrationTest {

    private lateinit var db: EduInvoiceDatabase
    private lateinit var repository: TutorBillingRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TutorBillingRepository(db.studentDao(), db.lessonDao(), db.groupDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addsLessonForEachStudentInGroup() = runBlocking {
        val userId = 1L
        val s1 = db.studentDao().insert(
            Student(ownerId = userId, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        )
        val s2 = db.studentDao().insert(
            Student(ownerId = userId, name = "Bob", surname = "", parentMobile = "", className = "", rate = 15.0)
        )
        val gId = db.groupDao().insertGroup(StudentGroup(ownerId = userId, name = "Group A"))
        db.groupDao().insertCrossRef(GroupStudentCrossRef(groupId = gId, studentId = s1, ownerId = userId))
        db.groupDao().insertCrossRef(GroupStudentCrossRef(groupId = gId, studentId = s2, ownerId = userId))

        val lesson = Lesson(ownerId = userId, studentId = 0, date = "2024-01-05", startTime = "10:00", durationMinutes = 60)
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
        val s1 = db.studentDao().insert(Student(ownerId = userId, name = "Ann", surname = "", parentMobile = "", className = "", rate = 12.0))
        val s2 = db.studentDao().insert(Student(ownerId = userId, name = "Ben", surname = "", parentMobile = "", className = "", rate = 18.0))
        val s3 = db.studentDao().insert(Student(ownerId = userId, name = "Cat", surname = "", parentMobile = "", className = "", rate = 20.0))
        val gId = db.groupDao().insertGroup(StudentGroup(ownerId = userId, name = "Group B"))
        db.groupDao().insertCrossRef(GroupStudentCrossRef(groupId = gId, studentId = s1, ownerId = userId))
        db.groupDao().insertCrossRef(GroupStudentCrossRef(groupId = gId, studentId = s2, ownerId = userId))
        db.groupDao().insertCrossRef(GroupStudentCrossRef(groupId = gId, studentId = s3, ownerId = userId))

        val lesson = Lesson(ownerId = userId, studentId = 0, date = "2024-02-01", startTime = "09:00", durationMinutes = 90)
        val useCase = AddGroupLesson(repository)
        val ids = useCase(gId, lesson, userId)

        assertEquals(3, ids.size)
        val lessons = db.lessonDao().getAllLessons(userId).first()
        assertEquals(3, lessons.size)
    }

    @Test
    fun partialFailureRollsBack() = runBlocking {
        val userId = 1L
        val s1 = db.studentDao().insert(Student(ownerId = userId, name = "Dan", surname = "", parentMobile = "", className = "", rate = 10.0))
        val s2 = db.studentDao().insert(Student(ownerId = userId, name = "Eve", surname = "", parentMobile = "", className = "", rate = 12.0))
        val gId = db.groupDao().insertGroup(StudentGroup(ownerId = userId, name = "Group C"))
        db.groupDao().insertCrossRef(GroupStudentCrossRef(groupId = gId, studentId = s1, ownerId = userId))
        db.groupDao().insertCrossRef(GroupStudentCrossRef(groupId = gId, studentId = s2, ownerId = userId))

        val lesson = Lesson(id = 1, ownerId = userId, studentId = 0, date = "2024-03-01", startTime = "08:00", durationMinutes = 60)
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
