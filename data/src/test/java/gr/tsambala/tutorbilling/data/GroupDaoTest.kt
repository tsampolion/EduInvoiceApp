package gr.tsambala.tutorbilling.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.tsambala.tutorbilling.data.dao.GroupDao
import gr.tsambala.tutorbilling.data.dao.StudentDao
import gr.tsambala.tutorbilling.data.database.TutorBillingDatabase
import gr.tsambala.tutorbilling.data.model.GroupStudentCrossRef
import gr.tsambala.tutorbilling.data.model.Student
import gr.tsambala.tutorbilling.data.model.StudentGroup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GroupDaoTest {

    private lateinit var db: TutorBillingDatabase
    private lateinit var groupDao: GroupDao
    private lateinit var studentDao: StudentDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TutorBillingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        groupDao = db.groupDao()
        studentDao = db.studentDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertGroupAndStudents() = runBlocking {
        val groupId = groupDao.insertGroup(StudentGroup(name = "Group A"))
        val studentId = studentDao.insert(Student(name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0))
        groupDao.insertCrossRef(GroupStudentCrossRef(groupId = groupId, studentId = studentId))
        val students = groupDao.getStudentsForGroup(groupId).first()
        assertEquals(1, students.size)
        assertEquals(studentId, students.first().id)
    }
}
