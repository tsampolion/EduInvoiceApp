package gr.eduinvoice.data.impl.user

import gr.eduinvoice.data.user.UserPreferencesRepository as DataUserPreferencesRepository
import gr.eduinvoice.domain.user.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data-side implementation of UserPreferencesRepository domain interface.
 * Delegates to the existing data layer UserPreferencesRepository implementation.
 */
@Singleton
class DataUserPreferencesRepository @Inject constructor(
    private val dataUserPreferencesRepository: DataUserPreferencesRepository
) : UserPreferencesRepository {
    
    override val loggedInUserId: Flow<Long?> = dataUserPreferencesRepository.loggedInUserId

    override suspend fun setLoggedInUser(id: Long?) {
        dataUserPreferencesRepository.setLoggedInUser(id)
    }

    override suspend fun getString(key: String, defaultValue: String): String {
        // The data layer doesn't have generic string getters, so we'll need to implement this
        // For now, return default value - this can be enhanced later
        return defaultValue
    }

    override suspend fun setString(key: String, value: String) {
        // The data layer doesn't have generic string setters, so we'll need to implement this
        // For now, do nothing - this can be enhanced later
    }

    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        // The data layer doesn't have generic boolean getters, so we'll need to implement this
        // For now, return default value - this can be enhanced later
        return defaultValue
    }

    override suspend fun setBoolean(key: String, value: Boolean) {
        // The data layer doesn't have generic boolean setters, so we'll need to implement this
        // For now, do nothing - this can be enhanced later
    }

    override suspend fun getInt(key: String, defaultValue: Int): Int {
        // The data layer doesn't have generic int getters, so we'll need to implement this
        // For now, return default value - this can be enhanced later
        return defaultValue
    }

    override suspend fun setInt(key: String, value: Int) {
        // The data layer doesn't have generic int setters, so we'll need to implement this
        // For now, do nothing - this can be enhanced later
    }

    override suspend fun getLong(key: String, defaultValue: Long): Long {
        // The data layer doesn't have generic long getters, so we'll need to implement this
        // For now, return default value - this can be enhanced later
        return defaultValue
    }

    override suspend fun setLong(key: String, value: Long) {
        // The data layer doesn't have generic long setters, so we'll need to implement this
        // For now, do nothing - this can be enhanced later
    }

    override suspend fun remove(key: String) {
        // The data layer doesn't have generic remove, so we'll need to implement this
        // For now, do nothing - this can be enhanced later
    }

    override suspend fun clear() {
        // The data layer doesn't have generic clear, so we'll need to implement this
        // For now, do nothing - this can be enhanced later
    }
}
