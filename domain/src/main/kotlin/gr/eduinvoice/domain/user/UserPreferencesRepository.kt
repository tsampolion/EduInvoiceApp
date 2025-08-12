package gr.eduinvoice.domain.user

import kotlinx.coroutines.flow.Flow

/**
 * Domain interface for user preferences repository.
 * This allows the app module to depend on domain abstractions rather than data layer implementations.
 *
 * Added during app→domain migration. Backed by data implementation.
 */
interface UserPreferencesRepository {
    /**
     * Get the logged in user ID as a Flow
     */
    val loggedInUserId: Flow<Long?>

    /**
     * Set the logged in user ID
     */
    suspend fun setLoggedInUser(id: Long?)

    /**
     * Get a string preference value
     */
    suspend fun getString(key: String, defaultValue: String = ""): String

    /**
     * Set a string preference value
     */
    suspend fun setString(key: String, value: String)

    /**
     * Get a boolean preference value
     */
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    /**
     * Set a boolean preference value
     */
    suspend fun setBoolean(key: String, value: Boolean)

    /**
     * Get an integer preference value
     */
    suspend fun getInt(key: String, defaultValue: Int = 0): Int

    /**
     * Set an integer preference value
     */
    suspend fun setInt(key: String, value: Int)

    /**
     * Get a long preference value
     */
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long

    /**
     * Set a long preference value
     */
    suspend fun setLong(key: String, value: Long)

    /**
     * Remove a preference
     */
    suspend fun remove(key: String)

    /**
     * Clear all preferences
     */
    suspend fun clear()
}
