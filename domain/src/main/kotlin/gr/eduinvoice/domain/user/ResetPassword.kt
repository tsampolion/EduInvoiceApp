package gr.eduinvoice.domain.user

import gr.eduinvoice.domain.repository.DomainUserRepository
import javax.inject.Inject

class ResetPassword @Inject constructor(
    private val repository: DomainUserRepository
) {
    suspend operator fun invoke(
        username: String,
        newPassword: String
    ): Boolean =
        repository.resetPassword(username, newPassword)
}
