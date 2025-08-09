package gr.eduinvoice.domain.user

import gr.eduinvoice.domain.model.DomainUser
import gr.eduinvoice.domain.repository.DomainUserRepository
import javax.inject.Inject

class CreateUser @Inject constructor(
    private val repository: DomainUserRepository
) {
    suspend operator fun invoke(user: DomainUser): Long = repository.createUser(user)
}
