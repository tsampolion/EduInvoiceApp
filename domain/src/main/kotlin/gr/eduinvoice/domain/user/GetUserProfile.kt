package gr.eduinvoice.domain.user

import gr.eduinvoice.domain.model.DomainUser
import gr.eduinvoice.domain.repository.DomainUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfile @Inject constructor(
    private val repository: DomainUserRepository
) {
    operator fun invoke(id: Long): Flow<DomainUser?> = repository.getUserProfile(id)
}
