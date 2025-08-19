package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.model.DomainAbsence
import gr.eduinvoice.domain.repository.DomainLessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupAbsences @Inject constructor(
    private val repository: DomainLessonRepository
) {
    operator fun invoke(userId: Long): Flow<List<DomainAbsence>> =
        repository.getGroupAbsences(userId)
}
