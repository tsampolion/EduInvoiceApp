package gr.eduinvoice.domain.user

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.data.model.User
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthenticateUserTest {
    private lateinit var db: EduInvoiceDatabase
    private lateinit var repository: UserRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = UserRepository(db.userDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun authenticateUserWithCorrectCredentials() = runBlocking {
        repository.createUser(
            User(
                username = "alice",
                passwordHash = "password",
                fullName = "Alice",
                subjectSpecialty = "Physics",
                yearsExperience = 3
            )
        )
        val useCase = AuthenticateUser(repository)
        val user = useCase("alice", "password")
        assertNotNull(user)
    }

    @Test
    fun authenticateUserWithWrongCredentials() = runBlocking {
        repository.createUser(
            User(
                username = "alice",
                passwordHash = "password",
                fullName = "Alice",
                subjectSpecialty = "Physics",
                yearsExperience = 3
            )
        )
        val useCase = AuthenticateUser(repository)
        val user = useCase("alice", "wrong")
        assertNull(user)
    }
}
