package gr.eduinvoice.domain.repository

import gr.eduinvoice.domain.model.DomainUser
import kotlinx.coroutines.flow.Flow

interface DomainUserRepository {
    suspend fun createUser(user: DomainUser): Long
    suspend fun authenticateUser(username: String, password: String): DomainUser?
    fun getUserProfile(userId: Long): Flow<DomainUser?>
    suspend fun updateUser(user: DomainUser)
    suspend fun resetPassword(
        username: String,
        fullName: String,
        code: String,
        newPassword: String
    ): Boolean
}
