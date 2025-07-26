package gr.eduinvoice

import gr.eduinvoice.data.user.CurrentUserProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserProvider(id: Long?) : CurrentUserProvider {
    private val _id = MutableStateFlow(id)
    override val loggedInUserId: Flow<Long?> = _id
}
