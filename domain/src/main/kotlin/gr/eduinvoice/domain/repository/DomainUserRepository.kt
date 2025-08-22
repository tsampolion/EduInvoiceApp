package gr.eduinvoice.domain.repository

import gr.eduinvoice.domain.model.DomainUser
import kotlinx.coroutines.flow.Flow

interface DomainUserRepository {
    suspend fun createUser(user: DomainUser): Long
    suspend fun authenticateUser(username: String, password: String): DomainUser?
    fun getUserProfile(userId: Long): Flow<DomainUser?>
    suspend fun getAllUsers(): List<DomainUser>
    suspend fun updateUser(user: DomainUser)
    suspend fun resetPassword(
        username: String,
        newPassword: String
    ): Boolean
    suspend fun deleteAccount(userId: Long)
    suspend fun createAdminUserIfNotExists()
}
