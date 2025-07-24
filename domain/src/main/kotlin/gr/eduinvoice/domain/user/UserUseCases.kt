package gr.eduinvoice.domain.user

import javax.inject.Inject

data class UserUseCases @Inject constructor(
    val createUser: CreateUser,
    val authenticateUser: AuthenticateUser,
    val getUserProfile: GetUserProfile,
    val updateUser: UpdateUser
)
