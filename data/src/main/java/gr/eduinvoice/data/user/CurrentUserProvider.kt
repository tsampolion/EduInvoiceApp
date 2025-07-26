package gr.eduinvoice.data.user

import kotlinx.coroutines.flow.Flow

interface CurrentUserProvider {
    val loggedInUserId: Flow<Long?>
}
