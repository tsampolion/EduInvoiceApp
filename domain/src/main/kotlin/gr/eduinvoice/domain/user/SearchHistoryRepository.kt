package gr.eduinvoice.domain.user

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    val history: Flow<List<String>>
    suspend fun add(query: String)
    suspend fun clear()
}
