package gr.eduinvoice.domain.user

import gr.eduinvoice.domain.model.DomainUser
import gr.eduinvoice.domain.repository.DomainUserRepository
import javax.inject.Inject

class AuthenticateUser @Inject constructor(
    private val repository: DomainUserRepository
) {
    suspend operator fun invoke(username: String, password: String): DomainUser? =
        repository.authenticateUser(username, password)
}
