package gr.eduinvoice.utils

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.model.DomainStudentGroup
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.data.user.CurrentUserProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

data class SearchResult(
    val students: List<DomainStudent> = emptyList(),
    val lessons: List<DomainLesson> = emptyList(),
    val groups: List<DomainStudentGroup> = emptyList()
)

@Singleton
class ModernSearchRepository @Inject constructor(
    private val studentUseCases: StudentUseCases,
    private val lessonUseCases: LessonUseCases,
    private val currentUserProvider: CurrentUserProvider,
    private val searchHistoryRepository: SearchHistoryRepository
) {
    private val history = ArrayDeque<String>()
    private val maxHistory = 10

    fun getHistory(): List<String> = history.toList()

    private fun addToHistory(q: String) {
        if (q.isBlank()) return
        history.remove(q)
        history.addFirst(q)
        while (history.size > maxHistory) history.removeLast()
    }

    suspend fun searchAll(query: String, limit: Int = 50): SearchResult {
        val userId = currentUserProvider.loggedInUserId.first() ?: 0L

        val students = if (query.isBlank()) emptyList() else {
            val base = studentUseCases.searchStudentsPaginated(userId, query, limit * 3, 0)
            base.sortedBy { fuzzyScore("${it.name} ${it.surname} ${it.className}", query) }
                .take(limit)
        }

        val lessons = if (query.isBlank()) emptyList() else {
            lessonUseCases.getAllLessons(userId).first()
                .sortedBy { l -> fuzzyScore("${l.notes ?: ""} ${l.date} ${l.startTime}", query) }
                .take(limit)
        }

        // Groups are not paginated in current use cases; return empty for now
        val result = SearchResult(
            students = students,
            lessons = lessons,
            groups = emptyList()
        )
        addToHistory(query)
        // Persist to DataStore
        searchHistoryRepository.add(query)
        // persist asynchronously (callers should launch)
        return result
    }

    // Simple fuzzy scoring: lower is better. Combines substring index and Levenshtein distance.
    private fun fuzzyScore(text: String, query: String): Int {
        val normText = text.lowercase()
        val normQuery = query.lowercase()
        val idx = normText.indexOf(normQuery).let { if (it == -1) Int.MAX_VALUE / 2 else it }
        return idx + levenshtein(normText, normQuery)
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val costs = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var last = i - 1
            costs[0] = i
            for (j in 1..b.length) {
                val cur = costs[j]
                val cost = if (a[i - 1] == b[j - 1]) last else min(min(costs[j], costs[j - 1]), last) + 1
                last = cur
                costs[j] = cost
            }
        }
        return costs[b.length]
    }
}


