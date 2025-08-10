package gr.eduinvoice.domain.user

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.domain.BouncyCastleTestRunner

@RunWith(BouncyCastleTestRunner::class)
class ResetPasswordTest {
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
    fun resetPasswordWithCorrectDetails() = runBlocking {
        repository.createUser(
            User(
                username = "bob",
                passwordHash = "old",
                fullName = "Bob",
                subjectSpecialty = "Math",
                yearsExperience = 1
            )
        )
        val useCase = ResetPassword(repository)
        val result = useCase("bob", "Bob", "123456", "new")
        assertTrue(result)
        val auth = repository.authenticate("bob", "new")
        assertTrue(auth != null)
    }

    @Test
    fun resetPasswordWithWrongFullName() = runBlocking {
        repository.createUser(
            User(
                username = "bob",
                passwordHash = "old",
                fullName = "Bob",
                subjectSpecialty = "Math",
                yearsExperience = 1
            )
        )
        val useCase = ResetPassword(repository)
        val result = useCase("bob", "Bob", "wrong", "new")
        assertFalse(result)
    }
}
