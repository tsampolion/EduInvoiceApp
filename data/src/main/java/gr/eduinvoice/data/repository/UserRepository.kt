package gr.eduinvoice.data.repository

import gr.eduinvoice.data.dao.UserDao
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.PasswordHasher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class UserRepository @Inject constructor(
    private val dao: UserDao
) {
    suspend fun createUser(user: User): Long = withContext(Dispatchers.IO) {
        val hashed = user.copy(passwordHash = PasswordHasher.hash(user.passwordHash))
        dao.insert(hashed)
    }
    fun getUserById(id: Long): Flow<User?> = dao.getUserById(id)
    suspend fun getByUsername(username: String): User? = withContext(Dispatchers.IO) { dao.getByUsername(username) }
    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) { dao.update(user) }
    suspend fun deleteUser(user: User) = withContext(Dispatchers.IO) { dao.delete(user) }
    suspend fun deleteUserById(userId: Long) = withContext(Dispatchers.IO) { dao.deleteById(userId) }

    suspend fun authenticate(username: String, password: String): User? {
        val user = withContext(Dispatchers.IO) { dao.getByUsername(username) } ?: return null
        val valid = PasswordHasher.verify(password, user.passwordHash)
        if (!valid) return null
        if (PasswordHasher.needsMigration(user.passwordHash)) {
            val updated = user.copy(passwordHash = PasswordHasher.hash(password))
            withContext(Dispatchers.IO) { dao.update(updated) }
            return updated
        }
        return user
    }

    suspend fun resetPassword(
        username: String,
        fullName: String,
        code: String,
        newPassword: String
    ): Boolean {
        val user = withContext(Dispatchers.IO) { dao.getByUsername(username) } ?: return false
        if (user.fullName != fullName || code != "123456") return false
        val updated = user.copy(passwordHash = PasswordHasher.hash(newPassword))
        withContext(Dispatchers.IO) { dao.update(updated) }
        return true
    }
}
