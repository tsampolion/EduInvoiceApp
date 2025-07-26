package gr.eduinvoice.domain.user

import gr.eduinvoice.data.repository.UserRepository
import javax.inject.Inject

class ResetPassword @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(username: String, fullName: String, newPassword: String): Boolean =
        repository.resetPassword(username, fullName, newPassword)
}
