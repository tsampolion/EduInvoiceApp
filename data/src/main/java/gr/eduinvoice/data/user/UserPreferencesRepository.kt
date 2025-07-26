package gr.eduinvoice.data.user

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import gr.eduinvoice.data.user.ENCRYPTED_PREFIX
import gr.eduinvoice.data.user.PassphraseCrypto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPrefsDataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : CurrentUserProvider {
    private val crypto = PassphraseCrypto(context)
    private object Keys {
        val LOGGED_IN_USER = longPreferencesKey("logged_in_user")
        val DB_PASSPHRASE = stringPreferencesKey("db_passphrase")
    }

    override val loggedInUserId: Flow<Long?> = context.userPrefsDataStore.data.map { prefs ->
        prefs[Keys.LOGGED_IN_USER]
    }

    suspend fun getDbPassphrase(): String {
        var stored = context.userPrefsDataStore.data.first()[Keys.DB_PASSPHRASE]
        if (stored == null) {
            val pass = crypto.generatePassphrase()
            stored = crypto.encrypt(pass)
            context.userPrefsDataStore.edit { it[Keys.DB_PASSPHRASE] = stored!! }
            return pass
        }
        if (!crypto.isEncrypted(stored)) {
            val enc = crypto.encrypt(stored)
            context.userPrefsDataStore.edit { it[Keys.DB_PASSPHRASE] = enc }
            stored = enc
        }
        return crypto.decrypt(stored)
    }

    suspend fun setDbPassphrase(passphrase: String) {
        val enc = crypto.encrypt(passphrase)
        context.userPrefsDataStore.edit { it[Keys.DB_PASSPHRASE] = enc }
    }

    suspend fun setLoggedInUser(id: Long?) {
        context.userPrefsDataStore.edit {
            if (id != null) it[Keys.LOGGED_IN_USER] = id else it.remove(Keys.LOGGED_IN_USER)
        }
    }
}