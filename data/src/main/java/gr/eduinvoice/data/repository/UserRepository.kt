package gr.eduinvoice.data.repository

import gr.eduinvoice.data.dao.UserDao
import gr.eduinvoice.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val dao: UserDao
) {
    suspend fun createUser(user: User): Long = dao.insert(user)
    fun getUserById(id: Long): Flow<User?> = dao.getUserById(id)
    suspend fun getByUsername(username: String): User? = dao.getByUsername(username)
    suspend fun updateUser(user: User) = dao.update(user)
    suspend fun deleteUser(user: User) = dao.delete(user)

    suspend fun authenticate(username: String, passwordHash: String): User? {
        val user = dao.getByUsername(username) ?: return null
        return if (user.passwordHash == passwordHash) user else null
    }
}
