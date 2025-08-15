package gr.eduinvoice.data.adapter

import gr.eduinvoice.domain.repository.DomainUserRepository
import gr.eduinvoice.domain.model.DomainUser
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DomainUserRepositoryAdapter @Inject constructor(
    private val userRepository: UserRepository,
    private val eduInvoiceRepository: gr.eduinvoice.data.repository.EduInvoiceRepository
) : DomainUserRepository {

    override suspend fun createUser(user: DomainUser): Long =
        userRepository.createUser(user.toDataModel())

    override suspend fun authenticateUser(username: String, password: String): DomainUser? =
        userRepository.authenticate(username, password)?.toDomainModel()

    override fun getUserProfile(userId: Long): Flow<DomainUser?> =
        userRepository.getUserById(userId).map { it?.toDomainModel() }

    override suspend fun updateUser(user: DomainUser) =
        userRepository.updateUser(user.toDataModel())

    override suspend fun resetPassword(
        username: String,
        fullName: String,
        code: String,
        newPassword: String
    ): Boolean = userRepository.resetPassword(username, fullName, code, newPassword)

    override suspend fun deleteAccount(userId: Long) {
        eduInvoiceRepository.deleteAccount(userId)
    }

    private fun DomainUser.toDataModel(): User = User(
        id = id,
        username = username,
        passwordHash = passwordHash,
        fullName = fullName,
        subjectSpecialty = subjectSpecialty,
        yearsExperience = yearsExperience
    )

    private fun User.toDomainModel(): DomainUser = DomainUser(
        id = id,
        username = username,
        passwordHash = passwordHash,
        fullName = fullName,
        subjectSpecialty = subjectSpecialty,
        yearsExperience = yearsExperience
    )
}
