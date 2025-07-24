package gr.eduinvoice.domain.user

import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.UserRepository
import javax.inject.Inject

class UpdateUser @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User) = repository.updateUser(user)
}
