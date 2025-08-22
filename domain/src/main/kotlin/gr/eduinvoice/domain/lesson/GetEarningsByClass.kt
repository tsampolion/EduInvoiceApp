package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEarningsByClass @Inject constructor(
    private val repository: DomainLessonRepository
) {
    operator fun invoke(startDate: String, endDate: String, userId: Long): Flow<List<Pair<String, Double>>> =
        repository.getEarningsByClass(startDate, endDate, userId)
}


