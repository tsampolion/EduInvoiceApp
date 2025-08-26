package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.model.DomainGroupLessonMaster
import gr.eduinvoice.domain.repository.DomainLessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupLessonMasterById @Inject constructor(
    private val repository: DomainLessonRepository
) {
    operator fun invoke(masterId: Long, userId: Long = 0): Flow<DomainGroupLessonMaster?> =
        repository.getGroupLessonMasterById(masterId, userId)
}
