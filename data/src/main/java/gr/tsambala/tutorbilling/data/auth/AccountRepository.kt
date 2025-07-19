package gr.tsambala.tutorbilling.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.accountDataStore by preferencesDataStore("account")

data class GoogleAccount(
    val email: String,
    val displayName: String
)

@Singleton
class AccountRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val EMAIL = stringPreferencesKey("email")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
    }

    val account: Flow<GoogleAccount?> = context.accountDataStore.data.map { prefs ->
        val email = prefs[Keys.EMAIL]
        val name = prefs[Keys.DISPLAY_NAME] ?: ""
        if (email == null) null else GoogleAccount(email, name)
    }

    suspend fun saveAccount(account: GoogleSignInAccount) {
        context.accountDataStore.edit {
            it[Keys.EMAIL] = account.email ?: ""
            it[Keys.DISPLAY_NAME] = account.displayName ?: ""
        }
    }

    suspend fun clearAccount() {
        context.accountDataStore.edit { it.clear() }
    }
}
