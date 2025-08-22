package gr.eduinvoice.domain.user

import gr.eduinvoice.domain.model.DomainUser
import gr.eduinvoice.domain.repository.DomainUserRepository
import javax.inject.Inject

class GetAllUsers @Inject constructor(
    private val repository: DomainUserRepository
) {
    suspend operator fun invoke(): List<DomainUser> = repository.getAllUsers()
}
