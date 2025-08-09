package gr.eduinvoice.domain.user

import gr.eduinvoice.domain.model.DomainUser
import gr.eduinvoice.domain.repository.DomainUserRepository
import javax.inject.Inject

class UpdateUser @Inject constructor(
    private val repository: DomainUserRepository
) {
    suspend operator fun invoke(user: DomainUser) = repository.updateUser(user)
}
