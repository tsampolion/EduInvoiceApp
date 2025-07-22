package gr.tutorbilling.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.tutorbilling.data.dao.GroupDao
import gr.tutorbilling.data.dao.StudentDao
import gr.tutorbilling.data.database.EduInvoiceDatabase
import gr.tutorbilling.data.model.GroupStudentCrossRef
import gr.tutorbilling.data.model.Student
import gr.tutorbilling.data.model.StudentGroup
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

    private lateinit var db: EduInvoiceDatabase
    private lateinit var groupDao: GroupDao
    private lateinit var studentDao: StudentDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
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
