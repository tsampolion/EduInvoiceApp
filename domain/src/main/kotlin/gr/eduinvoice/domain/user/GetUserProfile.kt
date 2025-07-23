package gr.eduinvoice.domain.user

import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfile @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(id: Long): Flow<User?> = repository.getUserById(id)
}
