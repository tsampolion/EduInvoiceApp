package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import gr.eduinvoice.domain.model.DomainRescheduleMaster
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRescheduleMasters @Inject constructor(
    private val repository: DomainLessonRepository
) {
    operator fun invoke(userId: Long): Flow<List<DomainRescheduleMaster>> = repository.getRescheduleMasters(userId)
}


