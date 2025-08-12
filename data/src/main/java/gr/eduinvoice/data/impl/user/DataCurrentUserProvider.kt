package gr.eduinvoice.data.impl.user

import gr.eduinvoice.data.user.CurrentUserProvider as DataCurrentUserProvider
import gr.eduinvoice.domain.user.CurrentUserProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data-side implementation of CurrentUserProvider domain interface.
 * Delegates to the existing data layer CurrentUserProvider implementation.
 */
@Singleton
class DataCurrentUserProvider @Inject constructor(
    private val dataCurrentUserProvider: DataCurrentUserProvider
) : CurrentUserProvider {
    
    override val loggedInUserId: Flow<Long?> = dataCurrentUserProvider.loggedInUserId
}
