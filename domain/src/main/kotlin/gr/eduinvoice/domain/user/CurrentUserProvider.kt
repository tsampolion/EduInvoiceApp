package gr.eduinvoice.domain.user

import kotlinx.coroutines.flow.Flow

/**
 * Domain interface for providing current user information.
 * This allows the app module to depend on domain abstractions rather than data layer implementations.
 * 
 * Added during app→domain migration. Backed by data implementation.
 */
interface CurrentUserProvider {
    /**
     * Get the current user's ID as a Flow
     */
    val loggedInUserId: Flow<Long?>
}
