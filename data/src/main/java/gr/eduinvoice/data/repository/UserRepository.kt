package gr.eduinvoice.data.repository

import gr.eduinvoice.data.dao.UserDao
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.PasswordHasher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val dao: UserDao
) {
    suspend fun createUser(user: User): Long {
        val hashed = user.copy(passwordHash = PasswordHasher.hash(user.passwordHash))
        return dao.insert(hashed)
    }
    fun getUserById(id: Long): Flow<User?> = dao.getUserById(id)
    suspend fun getByUsername(username: String): User? = dao.getByUsername(username)
    suspend fun updateUser(user: User) = dao.update(user)
    suspend fun deleteUser(user: User) = dao.delete(user)
    suspend fun deleteUserById(userId: Long) = dao.deleteById(userId)

    suspend fun authenticate(username: String, password: String): User? {
        val user = dao.getByUsername(username) ?: return null
        val valid = PasswordHasher.verify(password, user.passwordHash)
        if (!valid) return null
        if (PasswordHasher.needsMigration(user.passwordHash)) {
            val updated = user.copy(passwordHash = PasswordHasher.hash(password))
            dao.update(updated)
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
        val user = dao.getByUsername(username) ?: return false
        if (user.fullName != fullName || code != "123456") return false
        val updated = user.copy(passwordHash = PasswordHasher.hash(newPassword))
        dao.update(updated)
        return true
    }
}
