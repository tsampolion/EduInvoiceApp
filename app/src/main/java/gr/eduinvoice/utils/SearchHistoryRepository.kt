package gr.eduinvoice.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val KEY_HISTORY = stringPreferencesKey("search_history_csv")
    private val maxHistory = 10

    val history: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[KEY_HISTORY]?.split('|')?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun add(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        dataStore.edit { prefs ->
            val current = prefs[KEY_HISTORY]?.split('|')?.toMutableList() ?: mutableListOf()
            current.remove(q)
            current.add(0, q)
            while (current.size > maxHistory) current.removeLast()
            prefs[KEY_HISTORY] = current.joinToString("|")
        }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(KEY_HISTORY) }
    }
}


