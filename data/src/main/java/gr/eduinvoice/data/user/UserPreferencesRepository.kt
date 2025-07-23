package gr.eduinvoice.data.user

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
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
        val LOGGED_IN = booleanPreferencesKey("logged_in")
    }

    val isLoggedIn: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[Keys.LOGGED_IN] ?: false
    }

    suspend fun setLoggedIn(value: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.LOGGED_IN] = value }
    }
}
