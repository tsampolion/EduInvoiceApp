package gr.tsambala.tutorbilling.domain.lesson

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.tsambala.tutorbilling.data.database.TutorBillingDatabase
import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.model.Student
import gr.tsambala.tutorbilling.data.model.StudentGroup
import gr.tsambala.tutorbilling.data.model.GroupStudentCrossRef
import gr.tsambala.tutorbilling.data.repository.TutorBillingRepository
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

    private lateinit var db: TutorBillingDatabase
    private lateinit var repository: TutorBillingRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TutorBillingDatabase::class.java)
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
        val s1 = db.studentDao().insert(Student(name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0))
        val s2 = db.studentDao().insert(Student(name = "Bob", surname = "", parentMobile = "", className = "", rate = 15.0))
        val gId = db.groupDao().insertGroup(StudentGroup(name = "Group A"))
        db.groupDao().insertCrossRef(GroupStudentCrossRef(groupId = gId, studentId = s1))
        db.groupDao().insertCrossRef(GroupStudentCrossRef(groupId = gId, studentId = s2))

        val lesson = Lesson(studentId = 0, date = "2024-01-05", startTime = "10:00", durationMinutes = 60)
        val useCase = AddGroupLesson(repository)
        useCase(gId, lesson)

        val l1 = db.lessonDao().getLessonsByStudentId(s1).first()
        val l2 = db.lessonDao().getLessonsByStudentId(s2).first()
        assertEquals(1, l1.size)
        assertEquals(1, l2.size)
        assertEquals(10.0, l1.first().durationMinutes / 60.0 * 10.0, 0.0)
        assertEquals(15.0, l2.first().durationMinutes / 60.0 * 15.0, 0.0)
    }
}
