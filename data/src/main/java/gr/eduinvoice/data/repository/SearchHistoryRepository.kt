package gr.eduinvoice.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import gr.eduinvoice.domain.user.SearchHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DataSearchHistoryRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SearchHistoryRepository {
    private val KEY_HISTORY = stringPreferencesKey("search_history_csv")
    private val maxHistory = 10

    override val history: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[KEY_HISTORY]?.split('|')?.filter { it.isNotBlank() } ?: emptyList()
    }

    override suspend fun add(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        dataStore.edit { prefs ->
            val current = prefs[KEY_HISTORY]?.split('|')?.toMutableList() ?: mutableListOf()
            current.remove(q)
            current.add(0, q)
            while (current.size > maxHistory) current.removeAt(current.size - 1)
            prefs[KEY_HISTORY] = current.joinToString("|")
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.remove(KEY_HISTORY) }
    }
}
