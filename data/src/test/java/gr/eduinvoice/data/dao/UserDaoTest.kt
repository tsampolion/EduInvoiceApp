package gr.eduinvoice.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.User
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserDaoTest {
    private lateinit var db: EduInvoiceDatabase
    private lateinit var dao: UserDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.userDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndQueryByUsername() = runBlocking {
        dao.insert(
            User(
                username = "bob",
                passwordHash = "pass",
                fullName = "Bob",
                subjectSpecialty = "Math",
                yearsExperience = 5
            )
        )
        val user = dao.getByUsername("bob")
        assertEquals("Bob", user?.fullName)
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun insertingDuplicateUsernameFails() = runBlocking {
        dao.insert(User(username = "bob", passwordHash = "p1", fullName = "B"))
        dao.insert(User(username = "bob", passwordHash = "p2", fullName = "B2"))
    }
}
