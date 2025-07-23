package gr.eduinvoice.domain.user

import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.UserRepository
import javax.inject.Inject

class AuthenticateUser @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(username: String, passwordHash: String): User? =
        repository.authenticate(username, passwordHash)
}
