package gr.eduinvoice.domain.user

import gr.eduinvoice.domain.repository.DomainUserRepository
import javax.inject.Inject

class CreateAdminUserIfNotExists @Inject constructor(
    private val repository: DomainUserRepository
) {
    suspend operator fun invoke() = repository.createAdminUserIfNotExists()
}
