package fakes

import gr.eduinvoice.domain.user.CurrentUserProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCurrentUserProvider : CurrentUserProvider {
    private val userId = MutableStateFlow<Long?>(1L)

    fun setUserId(newUserId: Long?) {
        userId.value = newUserId
    }

    override val loggedInUserId: Flow<Long?> = userId
}
