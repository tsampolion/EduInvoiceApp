package gr.eduinvoice.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.test.support.extensions.createTestStudent
import gr.eduinvoice.test.support.extensions.createTestGroup
import gr.eduinvoice.test.support.extensions.createTestCrossRef
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.data.BouncyCastleTestRunner

@RunWith(BouncyCastleTestRunner::class)
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
        val groupId = groupDao.insertGroup(createTestGroup(name = "Group A", ownerId = 7))
        val studentId = studentDao.insert(createTestStudent(name = "Alice", rate = 10.0, ownerId = 7))
        groupDao.insertCrossRef(createTestCrossRef(groupId = groupId, studentId = studentId, ownerId = 7))

        db.openHelper.readableDatabase.query(
            "SELECT ownerId FROM group_student_cross_ref WHERE groupId = ? AND studentId = ?",
            arrayOf(groupId.toString(), studentId.toString())
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals(7L, cursor.getLong(0))
        }

        val students = groupDao.getStudentsForGroup(groupId, 7).first()
        assertEquals(1, students.size)
        assertEquals(studentId, students.first().id)
    }
}
