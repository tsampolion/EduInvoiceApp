package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.model.DomainGroupLessonMaster
import gr.eduinvoice.domain.repository.DomainLessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupLessonMasters @Inject constructor(
    private val repository: DomainLessonRepository
) {
    operator fun invoke(groupId: Long, userId: Long = 0): Flow<List<DomainGroupLessonMaster>> =
        repository.getGroupLessonMasters(groupId, userId)
}
