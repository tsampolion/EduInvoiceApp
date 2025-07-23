package gr.eduinvoice.data.user

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPrefsDataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LOGGED_IN_USER = longPreferencesKey("logged_in_user")
    }

    val loggedInUserId: Flow<Long?> = context.userPrefsDataStore.data.map { prefs ->
        prefs[Keys.LOGGED_IN_USER]
    }

    suspend fun setLoggedInUser(id: Long?) {
        context.userPrefsDataStore.edit {
            if (id != null) it[Keys.LOGGED_IN_USER] = id else it.remove(Keys.LOGGED_IN_USER)
        }
    }
}